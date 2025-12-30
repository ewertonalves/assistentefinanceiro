package com.financeiro.assitentefinanceiro.financeiro.domain.dto;

import com.financeiro.assitentefinanceiro.cadastro.domain.DadosConta;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "Dados do relatório financeiro para geração de PDF no frontend")
public record RelatorioDadosDTO(
        @Schema(description = "Título do relatório", example = "Relatório de Movimentações Financeiras")
        String tituloRelatorio,

        @Schema(description = "Dados da conta")
        DadosContaResumoDTO conta,

        @Schema(description = "Data de geração do relatório", example = "2024-01-15")
        LocalDate dataGeracao,

        @Schema(description = "Lista de movimentações financeiras")
        List<MovimentacaoFinanceiraDTO> movimentacoes,

        @Schema(description = "Total de receitas", example = "15000.00")
        BigDecimal totalReceitas,

        @Schema(description = "Total de despesas", example = "5000.00")
        BigDecimal totalDespesas,

        @Schema(description = "Saldo líquido (receitas - despesas)", example = "10000.00")
        BigDecimal saldoLiquido,

        @Schema(description = "Saldo atual da conta", example = "10000.00")
        BigDecimal saldoAtual,

        @Schema(description = "Período do relatório (data início)")
        LocalDate dataInicio,

        @Schema(description = "Período do relatório (data fim)")
        LocalDate dataFim,

        @Schema(description = "Tipo de movimentação filtrado")
        String tipoMovimentacao
) {
    public record DadosContaResumoDTO(
            @Schema(description = "Nome do banco", example = "Inter")
            String banco,

            @Schema(description = "Número da agência", example = "0001")
            String numeroAgencia,

            @Schema(description = "Número da conta", example = "0954")
            String numeroConta,

            @Schema(description = "Nome do responsável", example = "João Silva")
            String responsavel
    ) {
        public static DadosContaResumoDTO fromDadosConta(DadosConta conta) {
            return new DadosContaResumoDTO(
                    conta.getBanco(),
                    conta.getNumeroAgencia(),
                    conta.getNumeroConta(),
                    conta.getResponsavel()
            );
        }
    }
}

