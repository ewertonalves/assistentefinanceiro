package com.financeiro.assitentefinanceiro.financeiro.domain;

import com.financeiro.assitentefinanceiro.cadastro.domain.DadosConta;
import com.financeiro.assitentefinanceiro.financeiro.domain.dto.MovimentacaoFinanceiraDTO;
import com.financeiro.assitentefinanceiro.financeiro.enums.CategoriaFinanceira;
import com.financeiro.assitentefinanceiro.financeiro.enums.FonteMovimentacao;
import com.financeiro.assitentefinanceiro.financeiro.enums.StatusMovimentacao;
import com.financeiro.assitentefinanceiro.financeiro.enums.TipoMovimentacao;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "movimentacao_financeira")
public class MovimentacaoFinanceira {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoMovimentacao tipoMovimentacao;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valor;

    @Column(nullable = false, length = 500)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoriaFinanceira categoria;

    @Column(nullable = false)
    private LocalDate dataMovimentacao;

    @Column(nullable = false)
    private LocalDateTime dataRegistro;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusMovimentacao status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FonteMovimentacao fonteMovimentacao;

    @Column(length = 1000)
    private String observacoes;

    @Column(precision = 15, scale = 2)
    private BigDecimal saldoAnterior;

    @Column(precision = 15, scale = 2)
    private BigDecimal saldoAtual;

    @Column(length = 100)
    private String arquivoOrigem;

    @Column(length = 50)
    private String identificadorExterno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conta_id", nullable = false)
    private DadosConta conta;

    public MovimentacaoFinanceira(TipoMovimentacao tipoMovimentacao, BigDecimal valor, String descricao,
                                  CategoriaFinanceira categoria, LocalDate dataMovimentacao,
                                  StatusMovimentacao status, FonteMovimentacao fonteMovimentacao,
                                  String observacoes, DadosConta conta, BigDecimal saldoAnterior,
                                  BigDecimal saldoAtual) {
        this.tipoMovimentacao = tipoMovimentacao;
        this.valor = valor;
        this.descricao = descricao.trim();
        this.categoria = categoria;
        this.dataMovimentacao = dataMovimentacao;
        this.dataRegistro = LocalDateTime.now();
        this.status = status;
        this.fonteMovimentacao = fonteMovimentacao;
        this.observacoes = observacoes != null ? observacoes.trim() : null;
        this.conta = conta;
        this.saldoAnterior = saldoAnterior;
        this.saldoAtual = saldoAtual;
    }

    public MovimentacaoFinanceira(TipoMovimentacao tipoMovimentacao, BigDecimal valor, String descricao,
                                  CategoriaFinanceira categoria, LocalDate dataMovimentacao,
                                  StatusMovimentacao status, FonteMovimentacao fonteMovimentacao,
                                  String observacoes, DadosConta conta, BigDecimal saldoAnterior,
                                  BigDecimal saldoAtual, String arquivoOrigem, String identificadorExterno) {
        this.tipoMovimentacao = tipoMovimentacao;
        this.valor = valor;
        this.descricao = descricao.trim();
        this.categoria = categoria;
        this.dataMovimentacao = dataMovimentacao;
        this.dataRegistro = LocalDateTime.now();
        this.status = status;
        this.fonteMovimentacao = fonteMovimentacao;
        this.observacoes = observacoes != null ? observacoes.trim() : null;
        this.conta = conta;
        this.saldoAnterior = saldoAnterior;
        this.saldoAtual = saldoAtual;
        this.arquivoOrigem = arquivoOrigem;
        this.identificadorExterno = identificadorExterno;
    }

    public static MovimentacaoFinanceira fromDTO(MovimentacaoFinanceiraDTO dto, DadosConta conta) {
        return new MovimentacaoFinanceira(
                dto.tipoMovimentacao(),
                dto.valor(),
                dto.descricao(),
                dto.categoria(),
                dto.dataMovimentacao(),
                dto.status(),
                dto.fonteMovimentacao(),
                dto.observacoes(),
                conta,
                dto.saldoAnterior(),
                dto.saldoAtual(),
                dto.arquivoOrigem(),
                dto.identificadorExterno()
        );
    }

    public void atualizarDados(TipoMovimentacao tipoMovimentacao, BigDecimal valor, String descricao,
                               CategoriaFinanceira categoria, LocalDate dataMovimentacao,
                               StatusMovimentacao status, String observacoes,
                               BigDecimal saldoAnterior, BigDecimal saldoAtual) {
        this.tipoMovimentacao = tipoMovimentacao;
        this.valor = valor;
        this.descricao = descricao.trim();
        this.categoria = categoria;
        this.dataMovimentacao = dataMovimentacao;
        this.status = status;
        this.observacoes = observacoes != null ? observacoes.trim() : null;
        this.saldoAnterior = saldoAnterior;
        this.saldoAtual = saldoAtual;
    }

    public void marcarComoConcluida() {
        this.status = StatusMovimentacao.CONCLUIDA;
    }

    public void marcarComoCancelada() {
        this.status = StatusMovimentacao.CANCELADA;
    }

    public void estornar() {
        this.status = StatusMovimentacao.ESTORNADA;
    }
}
