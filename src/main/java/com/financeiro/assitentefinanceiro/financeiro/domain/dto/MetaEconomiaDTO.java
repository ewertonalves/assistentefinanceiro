package com.financeiro.assitentefinanceiro.financeiro.domain.dto;

import com.financeiro.assitentefinanceiro.financeiro.domain.MetaEconomia;
import com.financeiro.assitentefinanceiro.financeiro.enums.StatusMeta;
import com.financeiro.assitentefinanceiro.financeiro.enums.TipoMeta;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record MetaEconomiaDTO(
        @Schema(description = "ID da meta", example = "1", required = true)
        Long id,

        @Schema(description = "Nome da meta", example = "Reserva de Emergência", required = true)
        String nome,

        @Schema(description = "Descrição da meta", example = "Economizar para emergências")
        String descricao,

        @Schema(description = "Tipo da meta", example = "RESERVA_EMERGENCIA", required = true)
        TipoMeta tipoMeta,

        @Schema(description = "Valor da meta", example = "10000.00", required = true)
        BigDecimal valorMeta,

        @Schema(description = "Valor atual economizado", example = "2500.00")
        BigDecimal valorAtual,

        @Schema(description = "Data de início", example = "2024-01-01", required = true)
        LocalDate dataInicio,

        @Schema(description = "Data de fim", example = "2024-12-31", required = true)
        LocalDate dataFim,

        @Schema(description = "Status da meta", example = "ATIVA", required = true)
        StatusMeta status,

        @Schema(description = "Data de registro", example = "2024-01-01T10:00:00")
        LocalDateTime dataRegistro,

        @Schema(description = "Observações", example = "Meta para emergências médicas")
        String observacoes,

        @Schema(description = "Percentual concluído", example = "25.00")
        BigDecimal percentualConcluido,

        @Schema(description = "ID da conta relacionada", example = "1", required = true)
        Long contaId
) {

    public static MetaEconomiaDTO paraCadastro(String nome, String descricao, TipoMeta tipoMeta, BigDecimal valorMeta, LocalDate dataInicio, LocalDate dataFim, String observacoes, Long contaId) {
        return new MetaEconomiaDTO(null, nome.trim(), descricao != null ? descricao.trim() : null, tipoMeta, valorMeta, BigDecimal.ZERO, dataInicio, dataFim, StatusMeta.ATIVA, LocalDateTime.now(), observacoes != null ? observacoes.trim() : null, BigDecimal.ZERO, contaId);
    }

    public static MetaEconomiaDTO paraAtualizacao(Long id, String nome, String descricao, TipoMeta tipoMeta, BigDecimal valorMeta, LocalDate dataInicio, LocalDate dataFim, String observacoes, Long contaId) {
        return new MetaEconomiaDTO(id, nome.trim(), descricao != null ? descricao.trim() : null, tipoMeta, valorMeta, null, dataInicio, dataFim, null, null, observacoes != null ? observacoes.trim() : null, null, contaId);
    }

    public static MetaEconomiaDTO fromMetaEconomia(MetaEconomia meta) {
        return new MetaEconomiaDTO(meta.getId(), meta.getNome(), meta.getDescricao(), meta.getTipoMeta(), meta.getValorMeta(), meta.getValorAtual(), meta.getDataInicio(), meta.getDataFim(), meta.getStatus(), meta.getDataRegistro(), meta.getObservacoes(), meta.getPercentualConcluido(), meta.getConta().getId());
    }
}
