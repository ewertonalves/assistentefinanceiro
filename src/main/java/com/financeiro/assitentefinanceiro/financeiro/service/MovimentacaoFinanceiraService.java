package com.financeiro.assitentefinanceiro.financeiro.service;

import com.financeiro.assitentefinanceiro.cadastro.domain.DadosConta;
import com.financeiro.assitentefinanceiro.cadastro.service.CadastroContaService;
import com.financeiro.assitentefinanceiro.financeiro.domain.MovimentacaoFinanceira;
import com.financeiro.assitentefinanceiro.financeiro.domain.dto.MovimentacaoFinanceiraDTO;
import com.financeiro.assitentefinanceiro.financeiro.domain.dto.RelatorioDadosDTO;
import com.financeiro.assitentefinanceiro.financeiro.domain.dto.RelatorioPDFParametrosDTO;
import com.financeiro.assitentefinanceiro.financeiro.enums.StatusMovimentacao;
import com.financeiro.assitentefinanceiro.financeiro.enums.TipoMovimentacao;
import com.financeiro.assitentefinanceiro.financeiro.repository.MovimentacaoFinanceiraRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Supplier;

@Service
public class MovimentacaoFinanceiraService {

    private static final Logger logger = LoggerFactory.getLogger(MovimentacaoFinanceiraService.class);
    private final MovimentacaoFinanceiraRepository repository;
    private final CadastroContaService contaService;

    public MovimentacaoFinanceiraService(MovimentacaoFinanceiraRepository repository, 
            CadastroContaService contaService) {
        this.repository = repository;
        this.contaService = contaService;
    }

    @Transactional
    public MovimentacaoFinanceira registrarMovimentacao(MovimentacaoFinanceiraDTO movimentacaoDTO) {
        return executarComTratamentoErro(() -> {
            validarMovimentacaoDTO(movimentacaoDTO);

            logger.info("Iniciando registro de movimentação financeira. Tipo: {}, Valor: {}, Conta: {}",
                    movimentacaoDTO.tipoMovimentacao(), movimentacaoDTO.valor(), movimentacaoDTO.contaId());

            DadosConta conta = contaService.buscarContaPorId(movimentacaoDTO.contaId());

            BigDecimal saldoAnterior = calcularSaldoAtual(conta.getId());
            BigDecimal saldoAtual = calcularNovoSaldo(saldoAnterior, movimentacaoDTO.valor(),
                    movimentacaoDTO.tipoMovimentacao());

            MovimentacaoFinanceira movimentacao = new MovimentacaoFinanceira(
                    movimentacaoDTO.tipoMovimentacao(),
                    movimentacaoDTO.valor(),
                    movimentacaoDTO.descricao(),
                    movimentacaoDTO.categoria(),
                    movimentacaoDTO.dataMovimentacao(),
                    movimentacaoDTO.status() != null ? movimentacaoDTO.status() : StatusMovimentacao.CONCLUIDA,
                    movimentacaoDTO.fonteMovimentacao(),
                    movimentacaoDTO.observacoes(),
                    conta,
                    saldoAnterior,
                    saldoAtual,
                    movimentacaoDTO.arquivoOrigem(),
                    movimentacaoDTO.identificadorExterno());

            MovimentacaoFinanceira movimentacaoSalva = repository.save(movimentacao);

            logger.info("Movimentação registrada com sucesso. ID: {}, Tipo: {}, Valor: {}, Saldo: {} -> {}",
                    movimentacaoSalva.getId(), movimentacaoSalva.getTipoMovimentacao(),
                    movimentacaoSalva.getValor(), saldoAnterior, saldoAtual);

            return movimentacaoSalva;
        }, "registrar movimentação financeira");
    }

    public List<MovimentacaoFinanceira> listarMovimentacoes() {
        return executarComTratamentoErro(() -> {
            logger.info("Buscando lista de todas as movimentações financeiras");
            List<MovimentacaoFinanceira> movimentacoes = repository.findAll();
            logger.info("Total de movimentações encontradas: {}", movimentacoes.size());
            return movimentacoes;
        }, "listar movimentações");
    }

    public MovimentacaoFinanceira buscarMovimentacaoPorId(Long id) {
        return executarComTratamentoErro(() -> {
            validarId(id);

            logger.info("Buscando movimentação por ID: {}", id);
            MovimentacaoFinanceira movimentacao = repository.findById(id).orElseThrow(() -> {
                logger.error("Movimentação não encontrada com ID: {}", id);
                return new IllegalArgumentException("Movimentação não encontrada com ID: " + id);
            });
            logger.info("Movimentação encontrada com sucesso. ID: {}, Tipo: {}, Valor: {}",
                    movimentacao.getId(), movimentacao.getTipoMovimentacao(), movimentacao.getValor());
            return movimentacao;
        }, "buscar movimentação por ID");
    }

    public List<MovimentacaoFinanceira> buscarMovimentacoesPorConta(Long contaId) {
        return executarComTratamentoErro(() -> {
            validarId(contaId);
            contaService.buscarContaPorId(contaId);

            logger.info("Buscando movimentações da conta ID: {}", contaId);
            List<MovimentacaoFinanceira> movimentacoes = repository.findByContaId(contaId);
            logger.info("Total de movimentações encontradas para conta {}: {}", contaId, movimentacoes.size());
            return movimentacoes;
        }, "buscar movimentações por conta");
    }

    public List<MovimentacaoFinanceira> buscarMovimentacoesPorPeriodo(Long contaId, LocalDate dataInicio,
            LocalDate dataFim) {
        return executarComTratamentoErro(() -> {
            validarId(contaId);
            validarPeriodo(dataInicio, dataFim);
            contaService.buscarContaPorId(contaId);

            logger.info("Buscando movimentações da conta {} no período: {} a {}", contaId, dataInicio, dataFim);
            List<MovimentacaoFinanceira> movimentacoes = repository.findByContaIdAndPeriodo(contaId, dataInicio,
                    dataFim);
            logger.info("Total de movimentações encontradas no período: {}", movimentacoes.size());
            return movimentacoes;
        }, "buscar movimentações por período");
    }

    public List<MovimentacaoFinanceira> buscarMovimentacoesPorTipo(Long contaId, TipoMovimentacao tipoMovimentacao) {
        return executarComTratamentoErro(() -> {
            validarId(contaId);
            validarTipoMovimentacao(tipoMovimentacao);
            contaService.buscarContaPorId(contaId);

            logger.info("Buscando movimentações do tipo {} para conta {}", tipoMovimentacao, contaId);
            List<MovimentacaoFinanceira> movimentacoes = repository.findByContaIdAndTipoMovimentacao(contaId,
                    tipoMovimentacao);
            logger.info("Total de movimentações do tipo {} encontradas: {}", tipoMovimentacao, movimentacoes.size());
            return movimentacoes;
        }, "buscar movimentações por tipo");
    }

    @Transactional
    public MovimentacaoFinanceira atualizarMovimentacao(Long id, MovimentacaoFinanceiraDTO movimentacaoDTO) {
        return executarComTratamentoErro(() -> {
            validarMovimentacaoDTO(movimentacaoDTO);
            validarId(id);

            logger.info("Iniciando atualização da movimentação ID: {}", id);

            MovimentacaoFinanceira movimentacaoExistente = buscarMovimentacaoPorId(id);
            DadosConta conta = contaService.buscarContaPorId(movimentacaoDTO.contaId());

            BigDecimal saldoAnterior = calcularSaldoAtual(conta.getId());
            BigDecimal saldoAtual = calcularNovoSaldo(saldoAnterior, movimentacaoDTO.valor(),
                    movimentacaoDTO.tipoMovimentacao());

            movimentacaoExistente.atualizarDados(
                    movimentacaoDTO.tipoMovimentacao(),
                    movimentacaoDTO.valor(),
                    movimentacaoDTO.descricao(),
                    movimentacaoDTO.categoria(),
                    movimentacaoDTO.dataMovimentacao(),
                    movimentacaoDTO.status(),
                    movimentacaoDTO.observacoes(),
                    saldoAnterior,
                    saldoAtual);

            MovimentacaoFinanceira movimentacaoAtualizada = repository.save(movimentacaoExistente);
            logger.info("Movimentação atualizada com sucesso. ID: {}, Tipo: {}, Valor: {}",
                    movimentacaoAtualizada.getId(), movimentacaoAtualizada.getTipoMovimentacao(),
                    movimentacaoAtualizada.getValor());

            return movimentacaoAtualizada;
        }, "atualizar movimentação");
    }

    @Transactional
    public void excluirMovimentacao(Long id) {
        executarComTratamentoErro(() -> {
            validarId(id);

            logger.info("Iniciando exclusão da movimentação ID: {}", id);

            if (!repository.existsById(id)) {
                logger.error("Tentativa de exclusão de movimentação inexistente. ID: {}", id);
                throw new IllegalArgumentException("Movimentação não encontrada com ID: " + id);
            }

            repository.deleteById(id);
            logger.info("Movimentação excluída com sucesso. ID: {}", id);
            return null;
        }, "excluir movimentação");
    }

    @Transactional
    public MovimentacaoFinanceira estornarMovimentacao(Long id) {
        return executarComTratamentoErro(() -> {
            validarId(id);

            logger.info("Iniciando estorno da movimentação ID: {}", id);

            MovimentacaoFinanceira movimentacao = buscarMovimentacaoPorId(id);

            if (StatusMovimentacao.ESTORNADA.equals(movimentacao.getStatus())) {
                logger.warn("Movimentação já está estornada. ID: {}", id);
                throw new IllegalArgumentException("Movimentação já está estornada");
            }

            movimentacao.estornar();
            MovimentacaoFinanceira movimentacaoEstornada = repository.save(movimentacao);

            logger.info("Movimentação estornada com sucesso. ID: {}", id);
            return movimentacaoEstornada;
        }, "estornar movimentação");
    }

    public MovimentacaoFinanceiraDTO converterEntidadeParaDTO(MovimentacaoFinanceira movimentacao) {
        return MovimentacaoFinanceiraDTO.fromMovimentacaoFinanceira(movimentacao);
    }

    public BigDecimal calcularSaldoAtual(Long contaId) {
        return executarComTratamentoErro(() -> {
            validarId(contaId);

            BigDecimal totalReceitas = repository
                    .sumValorByContaIdAndTipoMovimentacao(contaId, TipoMovimentacao.RECEITA)
                    .map(BigDecimal::valueOf).orElse(BigDecimal.ZERO);

            BigDecimal totalDespesas = repository
                    .sumValorByContaIdAndTipoMovimentacao(contaId, TipoMovimentacao.DESPESA)
                    .map(BigDecimal::valueOf).orElse(BigDecimal.ZERO);

            BigDecimal saldoAtual = totalReceitas.subtract(totalDespesas);
            logger.debug("Saldo calculado para conta {}: Receitas: {}, Despesas: {}, Saldo: {}",
                    contaId, totalReceitas, totalDespesas, saldoAtual);

            return saldoAtual;
        }, "calcular saldo atual");
    }

    private BigDecimal calcularNovoSaldo(BigDecimal saldoAnterior, BigDecimal valor,
            TipoMovimentacao tipoMovimentacao) {
        if (TipoMovimentacao.RECEITA.equals(tipoMovimentacao)) {
            return saldoAnterior.add(valor);
        } else if (TipoMovimentacao.DESPESA.equals(tipoMovimentacao)) {
            return saldoAnterior.subtract(valor);
        }
        return saldoAnterior;
    }

    private void validarMovimentacaoDTO(MovimentacaoFinanceiraDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Dados da movimentação são obrigatórios");
        }

        validarCampoObrigatorio(dto.descricao(), "descrição");
        validarId(dto.contaId());
        validarTipoMovimentacao(dto.tipoMovimentacao());

        Object[][] camposObrigatorios = {
                { dto.valor(), "Valor deve ser maior que zero", (Runnable) () -> {
                    if (dto.valor() == null || dto.valor().compareTo(BigDecimal.ZERO) <= 0)
                        throw new IllegalArgumentException("Valor deve ser maior que zero");
                } },
                { dto.dataMovimentacao(), "Data da movimentação é obrigatória", (Runnable) () -> {
                    if (dto.dataMovimentacao() == null)
                        throw new IllegalArgumentException("Data da movimentação é obrigatória");
                } },
                { dto.categoria(), "Categoria e obrigatoria", (Runnable) () -> {
                    if (dto.categoria() == null)
                        throw new IllegalArgumentException("Categoria e obrigatoria");
                } },
                { dto.fonteMovimentacao(), "Fonte da movimentacao e obrigatoria", (Runnable) () -> {
                    if (dto.fonteMovimentacao() == null)
                        throw new IllegalArgumentException("Fonte da movimentacao e obrigatoria");
                } }
        };

        for (Object[] campo : camposObrigatorios) {
            ((Runnable) campo[2]).run();
        }

        logger.debug("Validação de dados da movimentação realizada com sucesso");
    }

    private void validarId(Long id) {
        if (id == null || id <= 0) {
            logger.error("ID inválido recebido: {}", id);
            throw new IllegalArgumentException("ID inválido");
        }
        logger.debug("ID validado com sucesso: {}", id);
    }

    private void validarPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        String erro = (dataInicio == null || dataFim == null) ? "Data de início e fim são obrigatórias"
                : (dataInicio.isAfter(dataFim)) ? "Data de início deve ser anterior à data de fim"
                        : (dataInicio.isAfter(LocalDate.now())) ? "Data de início não pode ser futura" : null;

        if (erro != null) {
            throw new IllegalArgumentException(erro);
        }
    }

    private void validarTipoMovimentacao(TipoMovimentacao tipoMovimentacao) {
        if (tipoMovimentacao == null) {
            throw new IllegalArgumentException("Tipo de movimentação é obrigatório");
        }
    }

    private void validarCampoObrigatorio(String valor, String nomeCampo) {
        if (valor == null || valor.trim().isEmpty()) {
            logger.error("Campo {} está vazio", nomeCampo);
            throw new IllegalArgumentException(
                    nomeCampo.substring(0, 1).toUpperCase() + nomeCampo.substring(1) + " é obrigatório");
        }
    }

    public RelatorioDadosDTO buscarDadosRelatorio(RelatorioPDFParametrosDTO parametros) {
        return executarComTratamentoErro(() -> {
            logger.info("Iniciando busca de dados do relatório. Conta: {}, Período: {} a {}",
                    parametros.contaId(), parametros.dataInicio(), parametros.dataFim());

            DadosConta conta = contaService.buscarContaPorId(parametros.contaId());

            List<MovimentacaoFinanceira> movimentacoes = buscarMovimentacoesFiltradas(parametros);
            List<MovimentacaoFinanceiraDTO> movimentacoesDTO = movimentacoes.stream()
                    .map(this::converterEntidadeParaDTO)
                    .toList();

            BigDecimal totalReceitas = movimentacoes.stream()
                    .filter(m -> TipoMovimentacao.RECEITA.equals(m.getTipoMovimentacao())
                            && StatusMovimentacao.CONCLUIDA.equals(m.getStatus()))
                    .map(MovimentacaoFinanceira::getValor)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalDespesas = movimentacoes.stream()
                    .filter(m -> TipoMovimentacao.DESPESA.equals(m.getTipoMovimentacao())
                            && StatusMovimentacao.CONCLUIDA.equals(m.getStatus()))
                    .map(MovimentacaoFinanceira::getValor)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal saldoAtual = calcularSaldoAtual(parametros.contaId());
            BigDecimal saldoLiquido = totalReceitas.subtract(totalDespesas);

            RelatorioDadosDTO.DadosContaResumoDTO contaResumo = 
                    RelatorioDadosDTO.DadosContaResumoDTO.fromDadosConta(conta);

            logger.info("Dados do relatório buscados com sucesso. Movimentações: {}, Total Receitas: {}, Total Despesas: {}",
                    movimentacoes.size(), totalReceitas, totalDespesas);

            return new RelatorioDadosDTO(
                    parametros.tituloRelatorio(),
                    contaResumo,
                    LocalDate.now(),
                    movimentacoesDTO,
                    totalReceitas,
                    totalDespesas,
                    saldoLiquido,
                    saldoAtual,
                    parametros.dataInicio(),
                    parametros.dataFim(),
                    parametros.tipoMovimentacao() != null ? parametros.tipoMovimentacao().toString() : null
            );
        }, "buscar dados do relatório");
    }

    private List<MovimentacaoFinanceira> buscarMovimentacoesFiltradas(RelatorioPDFParametrosDTO parametros) {
        if (parametros.tipoMovimentacao() != null && parametros.dataInicio() != null && parametros.dataFim() != null) {
            return repository.findByContaIdAndTipoMovimentacaoAndPeriodo(
                    parametros.contaId(),
                    parametros.tipoMovimentacao(),
                    parametros.dataInicio(),
                    parametros.dataFim());
        } else if (parametros.tipoMovimentacao() != null) {
            return repository.findByContaIdAndTipoMovimentacao(parametros.contaId(), parametros.tipoMovimentacao());
        } else if (parametros.dataInicio() != null && parametros.dataFim() != null) {
            validarPeriodo(parametros.dataInicio(), parametros.dataFim());
            return repository.findByContaIdAndPeriodo(parametros.contaId(), parametros.dataInicio(),
                    parametros.dataFim());
        } else {
            return repository.findByContaId(parametros.contaId());
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
