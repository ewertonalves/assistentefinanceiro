package com.financeiro.assitentefinanceiro.financeiro.domain.dto;

import com.financeiro.assitentefinanceiro.financeiro.domain.MovimentacaoFinanceira;
import com.financeiro.assitentefinanceiro.financeiro.enums.CategoriaFinanceira;
import com.financeiro.assitentefinanceiro.financeiro.enums.FonteMovimentacao;
import com.financeiro.assitentefinanceiro.financeiro.enums.StatusMovimentacao;
import com.financeiro.assitentefinanceiro.financeiro.enums.TipoMovimentacao;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record MovimentacaoFinanceiraDTO(
        @Schema(description = "ID da movimentação", example = "1", required = true)
        Long id,

        @Schema(description = "Tipo da movimentação", example = "RECEITA", required = true)
        TipoMovimentacao tipoMovimentacao,

        @Schema(description = "Valor da movimentação", example = "1500.00", required = true)
        BigDecimal valor,

        @Schema(description = "Descrição da movimentação", example = "Venda de produto", required = true)
        String descricao,

        @Schema(description = "Categoria da movimentação", example = "VENDAS", required = true)
        CategoriaFinanceira categoria,

        @Schema(description = "Data da movimentação", example = "2024-01-15", required = true)
        LocalDate dataMovimentacao,

        @Schema(description = "Data de registro", example = "2024-01-15T10:30:00", required = true)
        LocalDateTime dataRegistro,

        @Schema(description = "Status da movimentação", example = "CONCLUIDA", required = true)
        StatusMovimentacao status,

        @Schema(description = "Fonte da movimentação", example = "MANUAL", required = true)
        FonteMovimentacao fonteMovimentacao,

        @Schema(description = "Observações adicionais", example = "Pagamento à vista")
        String observacoes,

        @Schema(description = "Saldo anterior à movimentação", example = "5000.00")
        BigDecimal saldoAnterior,

        @Schema(description = "Saldo após a movimentação", example = "6500.00")
        BigDecimal saldoAtual,

        @Schema(description = "Nome do arquivo de origem", example = "extrato_jan_2024.csv")
        String arquivoOrigem,

        @Schema(description = "Identificador externo", example = "TXN123456")
        String identificadorExterno,

        @Schema(description = "ID da conta relacionada", example = "1", required = true)
        Long contaId
) {

    public static MovimentacaoFinanceiraDTO paraCadastro(TipoMovimentacao tipoMovimentacao, BigDecimal valor, 
                                                         String descricao, CategoriaFinanceira categoria, 
                                                         LocalDate dataMovimentacao, FonteMovimentacao fonteMovimentacao,
                                                         String observacoes, Long contaId) {
        return new MovimentacaoFinanceiraDTO(null, tipoMovimentacao, valor, descricao.trim(), 
                categoria, dataMovimentacao, LocalDateTime.now(), StatusMovimentacao.PENDENTE,
                fonteMovimentacao, observacoes != null ? observacoes.trim() : null, 
                null, null, null, null, contaId);
    }

    public static MovimentacaoFinanceiraDTO paraAtualizacao(Long id, TipoMovimentacao tipoMovimentacao, BigDecimal valor,
                                                           String descricao, CategoriaFinanceira categoria, 
                                                           LocalDate dataMovimentacao, StatusMovimentacao status,
                                                           String observacoes, Long contaId) {
        return new MovimentacaoFinanceiraDTO(id, tipoMovimentacao, valor, descricao.trim(), 
                categoria, dataMovimentacao, null, status, null,
                observacoes != null ? observacoes.trim() : null, 
                null, null, null, null, contaId);
    }

    public static MovimentacaoFinanceiraDTO fromMovimentacaoFinanceira(MovimentacaoFinanceira movimentacao) {
        return new MovimentacaoFinanceiraDTO(
                movimentacao.getId(),
                movimentacao.getTipoMovimentacao(),
                movimentacao.getValor(),
                movimentacao.getDescricao(),
                movimentacao.getCategoria(),
                movimentacao.getDataMovimentacao(),
                movimentacao.getDataRegistro(),
                movimentacao.getStatus(),
                movimentacao.getFonteMovimentacao(),
                movimentacao.getObservacoes(),
                movimentacao.getSaldoAnterior(),
                movimentacao.getSaldoAtual(),
                movimentacao.getArquivoOrigem(),
                movimentacao.getIdentificadorExterno(),
                movimentacao.getConta().getId()
        );
    }
}
