package com.financeiro.assitentefinanceiro.login.service;

import com.financeiro.assitentefinanceiro.login.domain.Usuario;
import com.financeiro.assitentefinanceiro.login.domain.dto.RegistroUsuarioDTO;
import com.financeiro.assitentefinanceiro.login.domain.dto.AtualizacaoUsuarioDTO;
import com.financeiro.assitentefinanceiro.login.domain.enums.Role;
import com.financeiro.assitentefinanceiro.login.repository.UsuarioRespository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Supplier;

import org.springframework.dao.DataIntegrityViolationException;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class UsuarioService implements UserDetailsService {

    private final UsuarioRespository usuarioRespository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private MeterRegistry meterRegistry;

    public UsuarioService(UsuarioRespository usuarioRespository, PasswordEncoder passwordEncoder) {
        this.usuarioRespository = usuarioRespository;
        this.passwordEncoder = passwordEncoder;
    }

    private static final Logger logger = LoggerFactory.getLogger(UsuarioService.class);

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return executarComTratamentoErro(() -> {
            validarCampoObrigatorio(email, "email");

            logger.info("Buscando usuário por email: {}", email);
            Usuario usuario = usuarioRespository.findByEmail(email).orElseThrow(() -> {
                logger.error("Usuário não encontrado com email: {}", email);
                return new UsernameNotFoundException("Usuário não encontrado com email: " + email);
            });

            logger.info("Usuário {} identificado como {}", email,
                    "ADMIN".equals(usuario.getRole().name()) ? "administrador" : "usuário comum");

            return usuario;
        }, "buscar usuário por email");
    }

    public Usuario salvarUsuario(Usuario usuario) {
        return executarComTratamentoErro(() -> {
            if (usuario == null) {
                throw new IllegalArgumentException("Usuário não pode ser nulo");
            }

            validarUsuario(usuario);

            logger.info("Iniciando processo de salvamento de novo usuário com email: {}", usuario.getEmail());

            usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
            Usuario usuarioSalvo = usuarioRespository.save(usuario);
            logger.info("Usuário salvo com sucesso. ID: {}", usuarioSalvo.getId());
            return usuarioSalvo;
        }, "salvar usuário");
    }

    public Usuario buscarPorEmail(String email) {
        return executarComTratamentoErro(() -> {
            validarCampoObrigatorio(email, "email");
            logger.info("Buscando usuário por email: {}", email);
            return usuarioRespository.findByEmail(email).orElseThrow(() -> {
                logger.error("Usuário não encontrado com email: {}", email);
                return new UsernameNotFoundException("Usuário não encontrado com email: " + email);
            });
        }, "buscar usuário por email");
    }

    public boolean existePorEmail(String email) {
        return executarComTratamentoErro(() -> {
            validarCampoObrigatorio(email, "email");

            logger.info("Verificando existência de usuário por email: {}", email);
            return usuarioRespository.existsByEmail(email);
        }, "verificar existência de email");
    }

    public List<Usuario> listarTodos() {
        return executarComTratamentoErro(() -> {
            logger.info("Buscando lista de todos os usuários");
            List<Usuario> usuarios = usuarioRespository.findAll();
            logger.info("Total de usuários encontrados: {}", usuarios.size());
            return usuarios;
        }, "listar usuários");
    }

    public Usuario buscarPorId(Long id) {
        return executarComTratamentoErro(() -> {
            if (id == null || id <= 0) {
                throw new IllegalArgumentException("ID inválido");
            }

            logger.info("Buscando usuário por ID: {}", id);
            return usuarioRespository.findById(id).orElseThrow(() -> {
                logger.error("Usuário não encontrado com ID: {}", id);
                return new UsernameNotFoundException("Usuário não encontrado com ID: " + id);
            });
        }, "buscar usuário por ID");
    }

    public Usuario criarUsuario(RegistroUsuarioDTO registroDTO) {
        validarRegistroDTO(registroDTO);

        logger.info("Criando novo usuário com email: {} e role: {}", registroDTO.email(), registroDTO.role());

        Usuario usuario = new Usuario(registroDTO.nome(), registroDTO.email(), registroDTO.senha());
        if (registroDTO.role() != null && registroDTO.role() == Role.ADMIN) {
            usuario.setRole(Role.ADMIN);
            logger.info("Usuário sendo criado como administrador");
        } else {
            usuario.setRole(Role.USER); 
            logger.info("Usuário sendo criado como usuário comum");
        }

        Usuario usuarioSalvo = salvarUsuario(usuario);

        Counter.builder("adsacoma_user_registrations_total")
                .description("Total de registros de usuários")
                .tag("role", usuario.getRole().name())
                .register(meterRegistry)
                .increment();

        return usuarioSalvo;
    }

    public Usuario atualizarUsuarioComDTO(Long id, AtualizacaoUsuarioDTO atualizacaoDTO) {
        return executarComTratamentoErro(() -> {
            validarAtualizacaoDTO(atualizacaoDTO);

            logger.info("Atualizando usuário ID: {} com email: {}", id, atualizacaoDTO.email());

            Usuario usuarioExistente = buscarPorId(id);

            if (!usuarioExistente.getEmail().equals(atualizacaoDTO.email()) &&
                    existePorEmail(atualizacaoDTO.email())) {
                throw new IllegalArgumentException("Este email já está sendo usado por outro usuário");
            }

            usuarioExistente.setNome(atualizacaoDTO.nome().trim());
            usuarioExistente.setEmail(atualizacaoDTO.email().trim().toLowerCase());
            usuarioExistente.setRole(atualizacaoDTO.role());

            if (atualizacaoDTO.senha() != null && !atualizacaoDTO.senha().trim().isEmpty()) {
                usuarioExistente.setSenha(passwordEncoder.encode(atualizacaoDTO.senha()));
            }

            Usuario usuarioAtualizado = usuarioRespository.save(usuarioExistente);
            logger.info("Usuário atualizado com sucesso. ID: {}", usuarioAtualizado.getId());

            return usuarioAtualizado;
        }, "atualizar usuário com DTO");
    }

    private void validarAtualizacaoDTO(AtualizacaoUsuarioDTO atualizacaoDTO) {
        if (atualizacaoDTO == null) {
            throw new IllegalArgumentException("Dados de atualização são obrigatórios");
        }

        validarCampoObrigatorio(atualizacaoDTO.nome(), "nome");
        validarCampoObrigatorio(atualizacaoDTO.email(), "email");

        if (atualizacaoDTO.role() == null) {
            throw new IllegalArgumentException("Role é obrigatória");
        }

        logger.debug("Validação de atualização realizada com sucesso");
    }

    private void validarRegistroDTO(RegistroUsuarioDTO registroDTO) {
        if (registroDTO == null) {
            throw new IllegalArgumentException("Dados do usuário são obrigatórios");
        }

        validarCampoObrigatorio(registroDTO.nome(), "nome");
        validarCampoObrigatorio(registroDTO.email(), "email");
        validarCampoObrigatorio(registroDTO.senha(), "senha");

        if (existePorEmail(registroDTO.email())) {
            logger.warn("Tentativa de registro com email já existente: {}", registroDTO.email());
            throw new IllegalArgumentException("Este email já está sendo usado");
        }
        logger.debug("Validação de registro realizada com sucesso");
    }

    private void validarUsuario(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("Usuário não pode ser nulo");
        }

        validarCampoObrigatorio(usuario.getEmail(), "email");
        validarCampoObrigatorio(usuario.getSenha(), "senha");

        logger.debug("Validação de usuário realizada com sucesso");
    }

    private <T> T executarComTratamentoErro(Supplier<T> operacao, String nomeOperacao) {
        try {
            return operacao.get();
        } catch (IllegalArgumentException | UsernameNotFoundException e) {
            logger.error("Erro de validação em {}: {}", nomeOperacao, e.getMessage());
            throw e;
        } catch (DataIntegrityViolationException e) {
            logger.error("Violação de integridade em {}: {}", nomeOperacao, e.getMessage());
            throw new IllegalArgumentException("Dados inválidos para " + nomeOperacao);
        } catch (Exception e) {
            logger.error("Erro inesperado em {}: {}", nomeOperacao, e.getMessage());
            throw new RuntimeException("Erro interno ao executar " + nomeOperacao + ": " + e.getMessage());
        }
    }

    private void validarCampoObrigatorio(String valor, String nomeCampo) {
        if (valor == null || valor.trim().isEmpty()) {
            logger.error("Campo {} está vazio", nomeCampo);
            throw new IllegalArgumentException(nomeCampo.substring(0, 1).toUpperCase() + nomeCampo.substring(1) + " é obrigatório");
        }
    }
}
