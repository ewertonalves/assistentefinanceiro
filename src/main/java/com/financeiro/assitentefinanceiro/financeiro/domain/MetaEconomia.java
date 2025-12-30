package com.financeiro.assitentefinanceiro.financeiro.domain;

import com.financeiro.assitentefinanceiro.cadastro.domain.DadosConta;
import com.financeiro.assitentefinanceiro.financeiro.domain.dto.MetaEconomiaDTO;
import com.financeiro.assitentefinanceiro.financeiro.enums.StatusMeta;
import com.financeiro.assitentefinanceiro.financeiro.enums.TipoMeta;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "meta_economia")
public class MetaEconomia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String nome;

    @Column(length = 1000)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoMeta tipoMeta;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valorMeta;

    @Column(precision = 15, scale = 2)
    private BigDecimal valorAtual;

    @Column(nullable = false)
    private LocalDate dataInicio;

    @Column(nullable = false)
    private LocalDate dataFim;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusMeta status;

    @Column(nullable = false)
    private LocalDateTime dataRegistro;

    @Column(length = 1000)
    private String observacoes;

    @Column(precision = 5, scale = 2)
    private BigDecimal percentualConcluido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conta_id", nullable = false)
    private DadosConta conta;

    public MetaEconomia(String nome, String descricao, TipoMeta tipoMeta, BigDecimal valorMeta,
                        LocalDate dataInicio, LocalDate dataFim, String observacoes, DadosConta conta) {
        this.nome = nome.trim();
        this.descricao = descricao != null ? descricao.trim() : null;
        this.tipoMeta = tipoMeta;
        this.valorMeta = valorMeta;
        this.valorAtual = BigDecimal.ZERO;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.status = StatusMeta.ATIVA;
        this.dataRegistro = LocalDateTime.now();
        this.observacoes = observacoes != null ? observacoes.trim() : null;
        this.conta = conta;
        this.percentualConcluido = BigDecimal.ZERO;
    }

    public MetaEconomia(String nome, String descricao, TipoMeta tipoMeta, BigDecimal valorMeta,
                        BigDecimal valorAtual, LocalDate dataInicio, LocalDate dataFim, 
                        StatusMeta status, LocalDateTime dataRegistro, String observacoes,
                        BigDecimal percentualConcluido, DadosConta conta) {
        this.nome = nome.trim();
        this.descricao = descricao != null ? descricao.trim() : null;
        this.tipoMeta = tipoMeta;
        this.valorMeta = valorMeta;
        this.valorAtual = valorAtual != null ? valorAtual : BigDecimal.ZERO;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.status = status != null ? status : StatusMeta.ATIVA;
        this.dataRegistro = dataRegistro != null ? dataRegistro : LocalDateTime.now();
        this.observacoes = observacoes != null ? observacoes.trim() : null;
        this.conta = conta;
        this.percentualConcluido = percentualConcluido != null ? percentualConcluido : BigDecimal.ZERO;
    }

    public static MetaEconomia fromDTO(MetaEconomiaDTO dto, DadosConta conta) {
        return new MetaEconomia(
                dto.nome(),
                dto.descricao(),
                dto.tipoMeta(),
                dto.valorMeta(),
                dto.valorAtual(),
                dto.dataInicio(),
                dto.dataFim(),
                dto.status(),
                dto.dataRegistro(),
                dto.observacoes(),
                dto.percentualConcluido(),
                conta
        );
    }

    public void atualizarProgresso(BigDecimal valorAdicionado) {
        this.valorAtual = this.valorAtual.add(valorAdicionado);
        this.percentualConcluido = this.valorAtual.divide(this.valorMeta, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));

        if (this.valorAtual.compareTo(this.valorMeta) >= 0) {
            this.status = StatusMeta.CONCLUIDA;
        }
    }

    public void atualizarDados(String nome, String descricao, TipoMeta tipoMeta,
                               BigDecimal valorMeta, LocalDate dataInicio, LocalDate dataFim,
                               String observacoes) {
        this.nome = nome.trim();
        this.descricao = descricao != null ? descricao.trim() : null;
        this.tipoMeta = tipoMeta;
        this.valorMeta = valorMeta;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.observacoes = observacoes != null ? observacoes.trim() : null;

        this.percentualConcluido = this.valorAtual.divide(this.valorMeta, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
    }

    public boolean isConcluida() {
        return StatusMeta.CONCLUIDA.equals(this.status);
    }

    public boolean isVencida() {
        return LocalDate.now().isAfter(this.dataFim) && !isConcluida();
    }

    public void pausar() {
        this.status = StatusMeta.PAUSADA;
    }

    public void reativar() {
        this.status = StatusMeta.ATIVA;
    }

    public void marcarComoVencida() {
        this.status = StatusMeta.VENCIDA;
    }
}
