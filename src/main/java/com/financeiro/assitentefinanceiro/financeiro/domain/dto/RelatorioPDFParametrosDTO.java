package com.financeiro.assitentefinanceiro.financeiro.domain.dto;

import com.financeiro.assitentefinanceiro.financeiro.enums.TipoMovimentacao;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Parâmetros para geração de relatório PDF dinâmico")
public record RelatorioPDFParametrosDTO(
        @Schema(description = "ID da conta para filtrar movimentações", example = "1", required = true)
        Long contaId,

        @Schema(description = "Data de início do período (opcional)", example = "2024-01-01")
        LocalDate dataInicio,

        @Schema(description = "Data de fim do período (opcional)", example = "2024-12-31")
        LocalDate dataFim,

        @Schema(description = "Tipo de movimentação para filtrar (opcional)", example = "RECEITA")
        TipoMovimentacao tipoMovimentacao,

        @Schema(description = "Título do relatório (opcional)", example = "Relatório de Movimentações Financeiras")
        String tituloRelatorio,

        @Schema(description = "Incluir resumo financeiro no relatório", example = "true")
        Boolean incluirResumo
) {
    public RelatorioPDFParametrosDTO {
        if (contaId == null || contaId <= 0) {
            throw new IllegalArgumentException("ID da conta é obrigatório");
        }
        if (dataInicio != null && dataFim != null && dataInicio.isAfter(dataFim)) {
            throw new IllegalArgumentException("Data de início deve ser anterior à data de fim");
        }
        if (tituloRelatorio == null || tituloRelatorio.trim().isEmpty()) {
            tituloRelatorio = "Relatório de Movimentações Financeiras";
        }
        if (incluirResumo == null) {
            incluirResumo = true;
        }
    }
}

