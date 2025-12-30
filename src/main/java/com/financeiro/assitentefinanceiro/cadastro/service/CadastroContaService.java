package com.financeiro.assitentefinanceiro.cadastro.service;

import com.financeiro.assitentefinanceiro.cadastro.domain.dto.DadosContaDTO;
import com.financeiro.assitentefinanceiro.cadastro.reposiitory.DadosContaRepository;
import com.financeiro.assitentefinanceiro.cadastro.domain.DadosConta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Supplier;

@Service
public class CadastroContaService {

    private static final Logger logger = LoggerFactory.getLogger(CadastroContaService.class);
    private final DadosContaRepository repository;

    public CadastroContaService(DadosContaRepository repository) {
        this.repository = repository;
    }

    @Transactional
    @CacheEvict(value = "contas", allEntries = true)
    public DadosConta cadastrarConta(DadosContaDTO dadosContaDTO) {
        return executarComTratamentoErro(() -> {
            validarDadosContaDTO(dadosContaDTO);

            logger.info("Iniciando cadastro de nova conta para o responsável: {}", dadosContaDTO.responsavel());

            if (repository.existsByNumeroConta(dadosContaDTO.numeroConta())) {
                logger.warn("Tentativa de cadastro com número de conta já existente: {}", dadosContaDTO.numeroConta());
                throw new IllegalArgumentException("Já existe uma conta cadastrada com este número");
            }

            DadosConta novaConta = DadosConta.fromDTO(dadosContaDTO);
            DadosConta contaSalva = repository.save(novaConta);

            logger.info("Conta cadastrada com sucesso. ID: {}, Responsável: {}", contaSalva.getId(),
                    contaSalva.getResponsavel());
            return contaSalva;
        }, "cadastrar conta");
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "contas", key = "'all'")
    public List<DadosConta> listarContas() {
        return executarComTratamentoErro(() -> {
            logger.info("Buscando lista de todas as contas");
            List<DadosConta> contas = repository.findAll();
            logger.info("Total de contas encontradas: {}", contas.size());
            return contas;
        }, "listar contas");
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "contas", key = "#id")
    public DadosConta buscarContaPorId(Long id) {
        return executarComTratamentoErro(() -> {
            validarId(id);

            logger.info("Buscando conta por ID: {}", id);

            DadosConta conta = repository.findById(id).orElseThrow(() -> {
                logger.error("Conta não encontrada com ID: {}", id);
                return new IllegalArgumentException("Conta não encontrada com ID: " + id);
            });
            logger.info("Conta encontrada com sucesso. ID: {}, Responsável: {}", conta.getId(), conta.getResponsavel());
            return conta;
        }, "buscar conta por ID");
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "contas", key = "'numero_' + #numeroConta")
    public DadosConta buscarContaPorNumero(String numeroConta) {
        return executarComTratamentoErro(() -> {
            validarCampoObrigatorio(numeroConta, "número da conta");

            logger.info("Buscando conta por número: {}", numeroConta);
            DadosConta conta = repository.findByNumeroConta(numeroConta).orElseThrow(() -> {
                logger.error("Conta não encontrada com número: {}", numeroConta);
                return new IllegalArgumentException("Conta não encontrada com número: " + numeroConta);
            });
            logger.info("Conta encontrada com sucesso. ID: {}, Número: {}, Responsável: {}", conta.getId(),
                    conta.getNumeroConta(), conta.getResponsavel());
            return conta;
        }, "buscar conta por número");
    }

    @Transactional
    @CacheEvict(value = "contas", allEntries = true)
    public void apagarConta(Long id) {
        executarComTratamentoErro(() -> {
            validarId(id);

            logger.info("Iniciando processo de exclusão da conta ID: {}", id);

            if (!repository.existsById(id)) {
                logger.error("Tentativa de exclusão de conta inexistente. ID: {}", id);
                throw new IllegalArgumentException("Conta não encontrada com ID: " + id);
            }

            repository.deleteById(id);
            logger.info("Conta excluída com sucesso. ID: {}", id);
            return null;
        }, "apagar conta");
    }

    @Transactional
    @CacheEvict(value = "contas", allEntries = true)
    public DadosConta atualizarConta(Long id, DadosContaDTO dadosContaDTO) {
        return executarComTratamentoErro(() -> {
            validarDadosContaDTO(dadosContaDTO);
            validarId(id);

            logger.info("Iniciando atualização da conta ID: {} para o responsável: {}", id,
                    dadosContaDTO.responsavel());

            DadosConta contaExistente = repository.findById(id).orElseThrow(() -> {
                logger.error("Conta não encontrada com ID: {}", id);
                return new IllegalArgumentException("Conta não encontrada com ID: " + id);
            });

            if (repository.existsByNumeroContaAndIdNot(dadosContaDTO.numeroConta(), id)) {
                logger.warn("Tentativa de atualização com número de conta já existente: {}",
                        dadosContaDTO.numeroConta());
                throw new IllegalArgumentException("Já existe outra conta cadastrada com este número");
            }

            contaExistente.atualizarDados(dadosContaDTO.banco(), dadosContaDTO.numeroAgencia(),
                    dadosContaDTO.numeroConta(), dadosContaDTO.tipoConta(), dadosContaDTO.responsavel());

            DadosConta contaAtualizada = repository.save(contaExistente);
            logger.info("Conta atualizada com sucesso. ID: {}, Responsável: {}", contaAtualizada.getId(),
                    contaAtualizada.getResponsavel());

            return contaAtualizada;
        }, "atualizar conta");
    }

    public DadosContaDTO converterEntidadeParaDTO(DadosConta conta) {
        return DadosContaDTO.fromDadosConta(conta);
    }

    public void verificarEstadoBanco() {
        try {
            long totalContas = repository.count();
            logger.info("=== ESTADO DO BANCO ===");
            logger.info("Total de contas na base: {}", totalContas);

            if (totalContas > 0) {
                List<DadosConta> todasContas = repository.findAll();
                logger.info("Detalhes das contas:");
                todasContas.forEach(conta -> {
                    logger.info("  - ID: {}, Tipo: {}, Banco: {}, Responsável: {}", conta.getId(),
                            conta.getId().getClass().getSimpleName(), conta.getBanco(), conta.getResponsavel());
                });
            } else {
                logger.info("Nenhuma conta encontrada na base de dados");
            }
            logger.info("=== FIM ESTADO BANCO ===");
        } catch (Exception e) {
            logger.error("Erro ao verificar estado do banco", e);
        }
    }

    private void validarDadosContaDTO(DadosContaDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Dados da conta são obrigatórios");
        }

        validarCampoObrigatorio(dto.banco(), "banco");
        validarCampoObrigatorio(dto.numeroAgencia(), "número da agência");
        validarCampoObrigatorio(dto.numeroConta(), "número da conta");
        validarCampoObrigatorio(dto.tipoConta(), "tipo da conta");
        validarCampoObrigatorio(dto.responsavel(), "responsável");

        logger.debug("Validação de dados da conta realizada com sucesso");
    }

    private void validarId(Long id) {
        if (id == null || id <= 0) {
            logger.error("ID inválido recebido: {}", id);
            throw new IllegalArgumentException("ID inválido");
        }
        logger.debug("ID validado com sucesso: {}", id);
    }

    private void validarCampoObrigatorio(String valor, String nomeCampo) {
        if (valor == null || valor.trim().isEmpty()) {
            logger.error("Campo {} está vazio", nomeCampo);
            throw new IllegalArgumentException(
                    nomeCampo.substring(0, 1).toUpperCase() + nomeCampo.substring(1) + " é obrigatório");
        }
    }

    private <T> T executarComTratamentoErro(Supplier<T> operacao, String nomeOperacao) {
        try {
            return operacao.get();
        } catch (IllegalArgumentException e) {
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
}
