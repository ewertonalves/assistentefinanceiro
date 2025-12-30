package com.financeiro.assitentefinanceiro.login.controller;

import com.financeiro.assitentefinanceiro.common.ApiResponse;
import com.financeiro.assitentefinanceiro.login.domain.dto.LoginResponseDTO;
import com.financeiro.assitentefinanceiro.login.domain.dto.RegistroUsuarioDTO;
import com.financeiro.assitentefinanceiro.login.domain.dto.AtualizacaoUsuarioDTO;
import com.financeiro.assitentefinanceiro.login.domain.dto.UsuarioResponseDTO;
import com.financeiro.assitentefinanceiro.config.JwtTokenUtil;
import com.financeiro.assitentefinanceiro.login.domain.JwtRequest;
import com.financeiro.assitentefinanceiro.login.domain.Usuario;
import com.financeiro.assitentefinanceiro.login.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin
@Tag(name = "Autenticação", description = "Endpoints para autenticação e registro de usuários")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final UsuarioService usuarioService;
    private final MeterRegistry meterRegistry;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    public AuthController(AuthenticationManager authenticationManager, JwtTokenUtil jwtTokenUtil, UsuarioService usuarioService, MeterRegistry meterRegistry) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenUtil = jwtTokenUtil;
        this.usuarioService = usuarioService;
        this.meterRegistry = meterRegistry;
    }

    @Operation(summary = "Fazer login", description = "Autentica um usuário e retorna um token JWT - use seu EMAIL para login")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> createAuthenticationToken(@RequestBody JwtRequest authenticationRequest) {
        Counter.builder("assitentefinanceiro_login_attempts_total")
                .description("Total de tentativas de login")
                .register(meterRegistry)
                .increment();

        try {
            String[][] validacoes = {
                    {authenticationRequest == null ? null : "ok", "Dados obrigatórios", "Informe email e senha para fazer login."},
                    {authenticationRequest != null ? authenticationRequest.getEmail() : null, "Email obrigatório", "Informe seu email para fazer login."},
                    {authenticationRequest != null ? authenticationRequest.getSenha() : null, "Senha obrigatória", "Informe sua senha para fazer login."}
            };

            for (String[] validacao : validacoes) {
                if (validacao[0] == null || (!"ok".equals(validacao[0]) && validacao[0].trim().isEmpty())) {
                    return ResponseEntity.badRequest()
                            .body(ApiResponse.erro(validacao[1], validacao[2]));
                }
            }

            if (authenticationRequest == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.erro("Dados obrigatórios", "Informe email e senha para fazer login."));
            }

            logger.info("Tentativa de login para o email: {}", authenticationRequest.getEmail());

            authenticate(authenticationRequest.getEmail(), authenticationRequest.getSenha());

            final UserDetails userDetails = usuarioService.loadUserByUsername(authenticationRequest.getEmail());
            final String token = jwtTokenUtil.generateToken(userDetails);

            Usuario usuario = (Usuario) userDetails;
            UsuarioResponseDTO usuarioDTO = UsuarioResponseDTO.fromUsuario(usuario);
            LoginResponseDTO loginResponse = new LoginResponseDTO(token, "Bearer", jwtExpiration, usuarioDTO);

            logger.info("Login realizado com sucesso para o usuário: {}", authenticationRequest.getEmail());

            Counter.builder("assitentefinanceiro_login_success_total")
                    .description("Total de logins bem-sucedidos")
                    .register(meterRegistry)
                    .increment();

            return ResponseEntity.ok(
                    ApiResponse.sucesso("Login realizado com sucesso! Bem-vindo(a), " + usuario.getNome() + "!", loginResponse)
            );

        } catch (BadCredentialsException e) {
            Counter.builder("assitentefinanceiro_login_failures_total")
                    .description("Total de falhas de login")
                    .tag("reason", "invalid_credentials")
                    .register(meterRegistry)
                    .increment();

            logger.warn("Tentativa de login com credenciais inválidas para: {}", 
                authenticationRequest != null ? authenticationRequest.getEmail() : "null");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.erro("Credenciais inválidas", "Email ou senha incorretos. Verifique suas informações e tente novamente."));

        } catch (DisabledException e) {
            Counter.builder("assitentefinanceiro_login_failures_total")
                    .description("Total de falhas de login")
                    .tag("reason", "account_disabled")
                    .register(meterRegistry)
                    .increment();

            logger.warn("Tentativa de login com conta desabilitada para: {}", 
                authenticationRequest != null ? authenticationRequest.getEmail() : "null");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.erro("Conta desabilitada", "Sua conta foi desabilitada. Entre em contato com o administrador."));

        } catch (Exception e) {
            Counter.builder("assitentefinanceiro_login_failures_total")
                    .description("Total de falhas de login")
                    .tag("reason", "internal_error")
                    .register(meterRegistry)
                    .increment();

            logger.error("Erro inesperado durante login para {}: {}", 
                authenticationRequest != null ? authenticationRequest.getEmail() : "null", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.erro("Erro interno do servidor", "Ocorreu um erro inesperado. Tente novamente em alguns instantes."));
        }
    }

    @Operation(summary = "Registrar usuário", description = "Cria um novo usuário no sistema - apenas nome, email e senha")
    @PostMapping("/registrarUsuario")
    public ResponseEntity<ApiResponse<UsuarioResponseDTO>> registerUser(@RequestBody RegistroUsuarioDTO registroDTO) {
        try {
            logger.info("Tentativa de registro para o email: {}", registroDTO != null ? registroDTO.email() : "null");

            Usuario novoUsuario = usuarioService.criarUsuario(registroDTO);
            UsuarioResponseDTO usuarioResponse = UsuarioResponseDTO.fromUsuario(novoUsuario);

            logger.info("Usuário registrado com sucesso: {} (ID: {})", novoUsuario.getEmail(), novoUsuario.getId());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.sucesso("Conta criada com sucesso! Bem-vindo(a), " + novoUsuario.getNome() + "!", usuarioResponse));

        } catch (IllegalArgumentException e) {
            logger.warn("Erro de validação no registro: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.erro("Dados inválidos", e.getMessage()));

        } catch (Exception e) {
            logger.error("Erro inesperado durante registro: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.erro("Erro interno do servidor", "Não foi possível criar sua conta no momento. Tente novamente em alguns instantes."));
        }
    }

    @Operation(summary = "Listar todos os usuários", description = "Lista todos os usuários cadastrados no sistema")
    @GetMapping("/listarUsuarios")
    public ResponseEntity<ApiResponse<List<UsuarioResponseDTO>>> listarUsuarios() {
        try {
            logger.info("Solicitação para listar todos os usuários");

            List<Usuario> usuarios = usuarioService.listarTodos();
            List<UsuarioResponseDTO> usuariosResponse = usuarios.stream()
                    .map(UsuarioResponseDTO::fromUsuario)
                    .collect(Collectors.toList());

            logger.info("Lista de usuários retornada com sucesso. Total: {}", usuarios.size());

            return ResponseEntity.ok(
                    ApiResponse.sucesso("Usuários listados com sucesso", usuariosResponse)
            );

        } catch (Exception e) {
            logger.error("Erro ao listar usuários: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.erro("Erro interno do servidor", "Não foi possível listar os usuários. Tente novamente mais tarde."));
        }
    }

    @Operation(summary = "Buscar usuário por ID", description = "Retorna os dados de um usuário específico pelo ID")
    @GetMapping("/buscarUsuario/{id}")
    public ResponseEntity<ApiResponse<UsuarioResponseDTO>> buscarUsuarioPorId(@PathVariable Long id) {
        try {
            logger.info("Buscando usuário com ID: {}", id);

            Usuario usuario = usuarioService.buscarPorId(id);
            UsuarioResponseDTO usuarioResponse = UsuarioResponseDTO.fromUsuario(usuario);

            logger.info("Usuário encontrado com sucesso: {}", usuario.getEmail());

            return ResponseEntity.ok(
                    ApiResponse.sucesso("Usuário encontrado com sucesso", usuarioResponse)
            );

        } catch (IllegalArgumentException e) {
            logger.warn("ID inválido fornecido: {}", id);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.erro("ID inválido", e.getMessage()));

        } catch (UsernameNotFoundException e) {
            logger.warn("Usuário não encontrado com ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.erro("Usuário não encontrado", e.getMessage()));

        } catch (Exception e) {
            logger.error("Erro ao buscar usuário com ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.erro("Erro interno do servidor", "Não foi possível buscar o usuário. Tente novamente mais tarde."));
        }
    }

    @Operation(summary = "Buscar usuário por email", description = "Retorna os dados de um usuário específico pelo Email")
    @GetMapping("/buscarUsuarioPorEmail/{email}")
    public ResponseEntity<ApiResponse<UsuarioResponseDTO>> buscarUsuarioPorId(@PathVariable String email) {
        try {
            logger.info("Buscando usuário com email: {}", email);

            Usuario usuario = usuarioService.buscarPorEmail(email);
            UsuarioResponseDTO usuarioResponse = UsuarioResponseDTO.fromUsuario(usuario);

            return ResponseEntity.ok(
                    ApiResponse.sucesso("Usuário encontrado com sucesso", usuarioResponse)
            );
        } catch (IllegalArgumentException e) {
            logger.warn("Email inválido fornecido: {}", email);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.erro("Email inválido", e.getMessage()));

        } catch (UsernameNotFoundException e) {
            logger.warn("Usuário não encontrado com Email: {}", email);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.erro("Usuário não encontrado", e.getMessage()));

        } catch (Exception e) {
            logger.error("Erro ao buscar usuário com Email {}: {}", email, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.erro("Erro interno do servidor", "Não foi possível buscar o usuário. Tente novamente mais tarde."));
        }
    }


    @Operation(summary = "Atualizar usuário", description = "Atualiza os dados de um usuário - nome, email, senha (opcional) e role")
    @PutMapping("/atualizarUsuario/{id}")
    public ResponseEntity<ApiResponse<UsuarioResponseDTO>> atualizarUsuario(
            @PathVariable Long id,
            @RequestBody AtualizacaoUsuarioDTO atualizacaoDTO) {
        try {
            logger.info("Tentativa de atualização para o usuário ID: {}", id);

            Usuario usuarioAtualizado = usuarioService.atualizarUsuarioComDTO(id, atualizacaoDTO);
            UsuarioResponseDTO usuarioResponse = UsuarioResponseDTO.fromUsuario(usuarioAtualizado);

            logger.info("Usuário atualizado com sucesso: {}", usuarioAtualizado.getEmail());

            return ResponseEntity.ok(
                    ApiResponse.sucesso("Dados atualizados com sucesso! " + usuarioAtualizado.getNome(), usuarioResponse)
            );

        } catch (IllegalArgumentException e) {
            logger.warn("Dados inválidos na atualização: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.erro("Dados inválidos", e.getMessage()));

        } catch (UsernameNotFoundException e) {
            logger.warn("Usuário não encontrado para atualização: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.erro("Usuário não encontrado", e.getMessage()));

        } catch (Exception e) {
            logger.error("Erro ao atualizar usuário: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.erro("Erro interno do servidor", "Não foi possível atualizar o usuário. Tente novamente mais tarde."));
        }
    }

    private void authenticate(String email, String senha) throws Exception {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email não pode estar vazio");
        }
        if (senha == null || senha.trim().isEmpty()) {
            throw new IllegalArgumentException("Senha não pode estar vazia");
        }

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, senha));
        } catch (DisabledException e) {
            throw new Exception("Usuário desativado", e);
        } catch (BadCredentialsException e) {
            throw new Exception("Credenciais inválidas", e);
        }
    }
}
