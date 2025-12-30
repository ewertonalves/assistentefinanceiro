package com.financeiro.assitentefinanceiro.financeiro.controller;

import com.financeiro.assitentefinanceiro.financeiro.domain.MovimentacaoFinanceira;
import com.financeiro.assitentefinanceiro.financeiro.domain.dto.MovimentacaoFinanceiraDTO;
import com.financeiro.assitentefinanceiro.financeiro.domain.dto.RelatorioDadosDTO;
import com.financeiro.assitentefinanceiro.financeiro.domain.dto.RelatorioPDFParametrosDTO;
import com.financeiro.assitentefinanceiro.financeiro.enums.TipoMovimentacao;
import com.financeiro.assitentefinanceiro.financeiro.service.MovimentacaoFinanceiraService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Tag(name = "Movimentações Financeiras", description = "Operações de gerenciamento de movimentações financeiras")
@RestController
@RequestMapping("/api/v1/movimentacoes")
public class MovimentacaoFinanceiraController {

    private final MovimentacaoFinanceiraService service;
    private static final Logger logger = LoggerFactory.getLogger(MovimentacaoFinanceiraController.class);

    public MovimentacaoFinanceiraController(MovimentacaoFinanceiraService service) {
        this.service = service;
    }

    @Operation(summary = "Registrar movimentação financeira", description = "Registra uma nova movimentação financeira")
    @PostMapping
    public ResponseEntity<MovimentacaoFinanceiraDTO> registrarMovimentacao(
            @RequestBody MovimentacaoFinanceiraDTO movimentacaoDTO) {
        try {
            logger.info("Solicitação para registrar movimentação financeira. Tipo: {}, Valor: {}, Conta: {}",
                    movimentacaoDTO.tipoMovimentacao(), movimentacaoDTO.valor(), movimentacaoDTO.contaId());

            MovimentacaoFinanceira movimentacao = service.registrarMovimentacao(movimentacaoDTO);
            MovimentacaoFinanceiraDTO movimentacaoSalvaDTO = service.converterEntidadeParaDTO(movimentacao);

            logger.info("Movimentação registrada com sucesso. ID: {}, Tipo: {}, Valor: {}",
                    movimentacaoSalvaDTO.id(), movimentacaoSalvaDTO.tipoMovimentacao(), movimentacaoSalvaDTO.valor());

            return ResponseEntity.status(HttpStatus.CREATED).body(movimentacaoSalvaDTO);
        } catch (IllegalArgumentException e) {
            logger.warn("Erro de validação ao registrar movimentação: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            logger.error("Erro ao registrar movimentação financeira", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Listar movimentações", description = "Retorna uma lista de todas as movimentações financeiras")
    @GetMapping
    public ResponseEntity<List<MovimentacaoFinanceiraDTO>> listarMovimentacoes() {
        try {
            logger.info("Solicitação para listar todas as movimentações financeiras");
            List<MovimentacaoFinanceira> movimentacoes = service.listarMovimentacoes();
            List<MovimentacaoFinanceiraDTO> movimentacoesDTO = movimentacoes.stream()
                    .map(service::converterEntidadeParaDTO)
                    .toList();
            logger.info("Lista de movimentações retornada com sucesso. Total: {}", movimentacoesDTO.size());
            return ResponseEntity.ok(movimentacoesDTO);
        } catch (Exception e) {
            logger.error("Erro ao listar movimentações", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Buscar movimentação por ID", description = "Retorna os dados de uma movimentação específica pelo ID")
    @GetMapping("/{id}")
    public ResponseEntity<MovimentacaoFinanceiraDTO> buscarMovimentacaoPorId(
            @Parameter(description = "ID da movimentação") @PathVariable Long id) {
        try {
            logger.info("Solicitação para buscar movimentação por ID: {}", id);
            MovimentacaoFinanceira movimentacao = service.buscarMovimentacaoPorId(id);
            MovimentacaoFinanceiraDTO movimentacaoDTO = service.converterEntidadeParaDTO(movimentacao);
            logger.info("Movimentação encontrada com sucesso. ID: {}, Tipo: {}, Valor: {}",
                    movimentacaoDTO.id(), movimentacaoDTO.tipoMovimentacao(), movimentacaoDTO.valor());
            return ResponseEntity.ok(movimentacaoDTO);
        } catch (IllegalArgumentException e) {
            logger.warn("Movimentação não encontrada: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            logger.error("Erro ao buscar movimentação por ID", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Buscar movimentações por conta", description = "Retorna movimentações de uma conta específica")
    @GetMapping("/conta/{contaId}")
    public ResponseEntity<List<MovimentacaoFinanceiraDTO>> buscarMovimentacoesPorConta(
            @Parameter(description = "ID da conta") @PathVariable Long contaId) {
        try {
            logger.info("Solicitação para buscar movimentações da conta: {}", contaId);
            List<MovimentacaoFinanceira> movimentacoes = service.buscarMovimentacoesPorConta(contaId);
            List<MovimentacaoFinanceiraDTO> movimentacoesDTO = movimentacoes.stream()
                    .map(service::converterEntidadeParaDTO)
                    .toList();
            logger.info("Movimentações da conta {} retornadas com sucesso. Total: {}", contaId,
                    movimentacoesDTO.size());
            return ResponseEntity.ok(movimentacoesDTO);
        } catch (IllegalArgumentException e) {
            logger.warn("Erro de validação ao buscar movimentações por conta: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            logger.error("Erro ao buscar movimentações por conta", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Buscar movimentações por período", description = "Retorna movimentações de uma conta em um período específico")
    @GetMapping("/conta/{contaId}/periodo")
    public ResponseEntity<List<MovimentacaoFinanceiraDTO>> buscarMovimentacoesPorPeriodo(
            @Parameter(description = "ID da conta") @PathVariable Long contaId,
            @Parameter(description = "Data de início (yyyy-MM-dd)") @RequestParam LocalDate dataInicio,
            @Parameter(description = "Data de fim (yyyy-MM-dd)") @RequestParam LocalDate dataFim) {
        try {
            logger.info("Solicitação para buscar movimentações da conta {} no período: {} a {}", contaId, dataInicio,
                    dataFim);
            List<MovimentacaoFinanceira> movimentacoes = service.buscarMovimentacoesPorPeriodo(contaId, dataInicio,
                    dataFim);
            List<MovimentacaoFinanceiraDTO> movimentacoesDTO = movimentacoes.stream()
                    .map(service::converterEntidadeParaDTO)
                    .toList();
            logger.info("Movimentações do período retornadas com sucesso. Total: {}", movimentacoesDTO.size());
            return ResponseEntity.ok(movimentacoesDTO);
        } catch (IllegalArgumentException e) {
            logger.warn("Erro de validação ao buscar movimentações por período: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            logger.error("Erro ao buscar movimentações por período", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Buscar movimentações por tipo", description = "Retorna movimentações de uma conta por tipo específico")
    @GetMapping("/conta/{contaId}/tipo/{tipoMovimentacao}")
    public ResponseEntity<List<MovimentacaoFinanceiraDTO>> buscarMovimentacoesPorTipo(
            @Parameter(description = "ID da conta") @PathVariable Long contaId,
            @Parameter(description = "Tipo da movimentação") @PathVariable TipoMovimentacao tipoMovimentacao) {
        try {
            logger.info("Solicitação para buscar movimentações da conta {} do tipo {}", contaId, tipoMovimentacao);
            List<MovimentacaoFinanceira> movimentacoes = service.buscarMovimentacoesPorTipo(contaId, tipoMovimentacao);
            List<MovimentacaoFinanceiraDTO> movimentacoesDTO = movimentacoes.stream()
                    .map(service::converterEntidadeParaDTO)
                    .toList();
            logger.info("Movimentações do tipo {} retornadas com sucesso. Total: {}", tipoMovimentacao,
                    movimentacoesDTO.size());
            return ResponseEntity.ok(movimentacoesDTO);
        } catch (IllegalArgumentException e) {
            logger.warn("Erro de validação ao buscar movimentações por tipo: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            logger.error("Erro ao buscar movimentações por tipo", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Atualizar movimentação", description = "Atualiza os dados de uma movimentação existente")
    @PutMapping("/{id}")
    public ResponseEntity<MovimentacaoFinanceiraDTO> atualizarMovimentacao(
            @Parameter(description = "ID da movimentação") @PathVariable Long id,
            @RequestBody MovimentacaoFinanceiraDTO movimentacaoDTO) {
        try {
            logger.info("Solicitação para atualizar movimentação. ID: {}, Tipo: {}, Valor: {}",
                    id, movimentacaoDTO.tipoMovimentacao(), movimentacaoDTO.valor());

            MovimentacaoFinanceira movimentacao = service.atualizarMovimentacao(id, movimentacaoDTO);
            MovimentacaoFinanceiraDTO movimentacaoAtualizadaDTO = service.converterEntidadeParaDTO(movimentacao);

            logger.info("Movimentação atualizada com sucesso. ID: {}, Tipo: {}, Valor: {}",
                    movimentacaoAtualizadaDTO.id(), movimentacaoAtualizadaDTO.tipoMovimentacao(),
                    movimentacaoAtualizadaDTO.valor());

            return ResponseEntity.ok(movimentacaoAtualizadaDTO);
        } catch (IllegalArgumentException e) {
            logger.warn("Erro de validação ao atualizar movimentação: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            logger.error("Erro ao atualizar movimentação", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Excluir movimentação", description = "Remove uma movimentação pelo ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirMovimentacao(
            @Parameter(description = "ID da movimentação") @PathVariable Long id) {
        try {
            logger.info("Solicitação para excluir movimentação. ID: {}", id);
            service.excluirMovimentacao(id);
            logger.info("Movimentação excluída com sucesso. ID: {}", id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (IllegalArgumentException e) {
            logger.warn("Movimentação não encontrada para exclusão: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            logger.error("Erro ao excluir movimentação", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Estornar movimentação", description = "Estorna uma movimentação existente")
    @PostMapping("/{id}/estornar")
    public ResponseEntity<MovimentacaoFinanceiraDTO> estornarMovimentacao(
            @Parameter(description = "ID da movimentação") @PathVariable Long id) {
        try {
            logger.info("Solicitação para estornar movimentação. ID: {}", id);
            MovimentacaoFinanceira movimentacao = service.estornarMovimentacao(id);
            MovimentacaoFinanceiraDTO movimentacaoEstornadaDTO = service.converterEntidadeParaDTO(movimentacao);
            logger.info("Movimentação estornada com sucesso. ID: {}", id);
            return ResponseEntity.ok(movimentacaoEstornadaDTO);
        } catch (IllegalArgumentException e) {
            logger.warn("Erro de validação ao estornar movimentação: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            logger.error("Erro ao estornar movimentação", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Calcular saldo atual", description = "Calcula o saldo atual de uma conta")
    @GetMapping("/conta/{contaId}/saldo")
    public ResponseEntity<BigDecimal> calcularSaldoAtual(
            @Parameter(description = "ID da conta") @PathVariable Long contaId) {
        try {
            logger.info("Solicitação para calcular saldo da conta: {}", contaId);
            BigDecimal saldo = service.calcularSaldoAtual(contaId);
            logger.info("Saldo calculado com sucesso para conta {}. Valor: {}", contaId, saldo);
            return ResponseEntity.ok(saldo);
        } catch (IllegalArgumentException e) {
            logger.warn("Erro de validação ao calcular saldo: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            logger.error("Erro ao calcular saldo", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Buscar dados do relatório", description = "Retorna os dados do relatório em JSON para geração de PDF no frontend")
    @PostMapping("/relatorio/dados")
    public ResponseEntity<RelatorioDadosDTO> buscarDadosRelatorio(@RequestBody RelatorioPDFParametrosDTO parametros) {
        try {
            logger.info("Solicitação para buscar dados do relatório. Conta: {}, Período: {} a {}, Tipo: {}",
                    parametros.contaId(), parametros.dataInicio(), parametros.dataFim(), parametros.tipoMovimentacao());

            RelatorioDadosDTO dados = service.buscarDadosRelatorio(parametros);

            logger.info("Dados do relatório retornados com sucesso. Movimentações: {}", 
                    dados.movimentacoes().size());

            return ResponseEntity.ok(dados);
        } catch (IllegalArgumentException e) {
            logger.warn("Erro de validação ao buscar dados do relatório: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            logger.error("Erro ao buscar dados do relatório", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
