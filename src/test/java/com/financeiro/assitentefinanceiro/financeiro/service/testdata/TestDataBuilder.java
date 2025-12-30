package com.financeiro.assitentefinanceiro.financeiro.service.testdata;

import com.financeiro.assitentefinanceiro.cadastro.domain.DadosConta;
import com.financeiro.assitentefinanceiro.financeiro.domain.MetaEconomia;
import com.financeiro.assitentefinanceiro.financeiro.domain.MovimentacaoFinanceira;
import com.financeiro.assitentefinanceiro.financeiro.domain.dto.MetaEconomiaDTO;
import com.financeiro.assitentefinanceiro.financeiro.domain.dto.MovimentacaoFinanceiraDTO;
import com.financeiro.assitentefinanceiro.financeiro.domain.dto.RelatorioDadosDTO;
import com.financeiro.assitentefinanceiro.financeiro.domain.dto.RelatorioPDFParametrosDTO;
import com.financeiro.assitentefinanceiro.financeiro.enums.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class TestDataBuilder {

    public static class DadosContaBuilder {
        private Long id = 1L;
        private String banco = "Banco do Brasil";
        private String numeroAgencia = "1234";
        private String numeroConta = "567890";
        private String tipoConta = "Corrente";
        private String responsavel = "João Silva";

        public DadosContaBuilder comId(Long id) {
            this.id = id;
            return this;
        }

        public DadosContaBuilder comBanco(String banco) {
            this.banco = banco;
            return this;
        }

        public DadosContaBuilder comResponsavel(String responsavel) {
            this.responsavel = responsavel;
            return this;
        }

        public DadosConta build() {
            DadosConta conta = new DadosConta();
            conta.setId(id);
            conta.setBanco(banco);
            conta.setNumeroAgencia(numeroAgencia);
            conta.setNumeroConta(numeroConta);
            conta.setTipoConta(tipoConta);
            conta.setResponsavel(responsavel);
            return conta;
        }
    }

    public static class MovimentacaoFinanceiraDTOBuilder {
        private Long id = null;
        private TipoMovimentacao tipoMovimentacao = TipoMovimentacao.RECEITA;
        private BigDecimal valor = new BigDecimal("1000.00");
        private String descricao = "Teste de movimentação";
        private CategoriaFinanceira categoria = CategoriaFinanceira.VENDAS;
        private LocalDate dataMovimentacao = LocalDate.now();
        private LocalDateTime dataRegistro = LocalDateTime.now();
        private StatusMovimentacao status = StatusMovimentacao.CONCLUIDA;
        private FonteMovimentacao fonteMovimentacao = FonteMovimentacao.MANUAL;
        private String observacoes = "Observação de teste";
        private BigDecimal saldoAnterior = new BigDecimal("5000.00");
        private BigDecimal saldoAtual = new BigDecimal("6000.00");
        private String arquivoOrigem = null;
        private String identificadorExterno = null;
        private Long contaId = 1L;

        public MovimentacaoFinanceiraDTOBuilder comId(Long id) {
            this.id = id;
            return this;
        }

        public MovimentacaoFinanceiraDTOBuilder comTipoMovimentacao(TipoMovimentacao tipoMovimentacao) {
            this.tipoMovimentacao = tipoMovimentacao;
            return this;
        }

        public MovimentacaoFinanceiraDTOBuilder comValor(BigDecimal valor) {
            this.valor = valor;
            return this;
        }

        public MovimentacaoFinanceiraDTOBuilder comDescricao(String descricao) {
            this.descricao = descricao;
            return this;
        }

        public MovimentacaoFinanceiraDTOBuilder comCategoria(CategoriaFinanceira categoria) {
            this.categoria = categoria;
            return this;
        }

        public MovimentacaoFinanceiraDTOBuilder comDataMovimentacao(LocalDate dataMovimentacao) {
            this.dataMovimentacao = dataMovimentacao;
            return this;
        }

        public MovimentacaoFinanceiraDTOBuilder comStatus(StatusMovimentacao status) {
            this.status = status;
            return this;
        }

        public MovimentacaoFinanceiraDTOBuilder comFonteMovimentacao(FonteMovimentacao fonteMovimentacao) {
            this.fonteMovimentacao = fonteMovimentacao;
            return this;
        }

        public MovimentacaoFinanceiraDTOBuilder comContaId(Long contaId) {
            this.contaId = contaId;
            return this;
        }

        public MovimentacaoFinanceiraDTOBuilder comSaldoAnterior(BigDecimal saldoAnterior) {
            this.saldoAnterior = saldoAnterior;
            return this;
        }

        public MovimentacaoFinanceiraDTOBuilder comSaldoAtual(BigDecimal saldoAtual) {
            this.saldoAtual = saldoAtual;
            return this;
        }

        public MovimentacaoFinanceiraDTO build() {
            return new MovimentacaoFinanceiraDTO(
                id, tipoMovimentacao, valor, descricao, categoria, dataMovimentacao,
                dataRegistro, status, fonteMovimentacao, observacoes, saldoAnterior,
                saldoAtual, arquivoOrigem, identificadorExterno, contaId
            );
        }
    }

    public static class MovimentacaoFinanceiraBuilder {
        private TipoMovimentacao tipoMovimentacao = TipoMovimentacao.RECEITA;
        private BigDecimal valor = new BigDecimal("1000.00");
        private String descricao = "Teste de movimentação";
        private CategoriaFinanceira categoria = CategoriaFinanceira.VENDAS;
        private LocalDate dataMovimentacao = LocalDate.now();
        private StatusMovimentacao status = StatusMovimentacao.CONCLUIDA;
        private FonteMovimentacao fonteMovimentacao = FonteMovimentacao.MANUAL;
        private String observacoes = "Observação de teste";
        private BigDecimal saldoAnterior = new BigDecimal("5000.00");
        private BigDecimal saldoAtual = new BigDecimal("6000.00");
        private String arquivoOrigem = null;
        private String identificadorExterno = null;
        private DadosConta conta = new DadosContaBuilder().build();

        public MovimentacaoFinanceiraBuilder comTipoMovimentacao(TipoMovimentacao tipoMovimentacao) {
            this.tipoMovimentacao = tipoMovimentacao;
            return this;
        }

        public MovimentacaoFinanceiraBuilder comValor(BigDecimal valor) {
            this.valor = valor;
            return this;
        }

        public MovimentacaoFinanceiraBuilder comStatus(StatusMovimentacao status) {
            this.status = status;
            return this;
        }

        public MovimentacaoFinanceiraBuilder comConta(DadosConta conta) {
            this.conta = conta;
            return this;
        }

        public MovimentacaoFinanceira build() {
            return new MovimentacaoFinanceira(
                tipoMovimentacao, valor, descricao, categoria, dataMovimentacao,
                status, fonteMovimentacao, observacoes, conta, saldoAnterior,
                saldoAtual, arquivoOrigem, identificadorExterno
            );
        }
    }

    public static class MetaEconomiaDTOBuilder {
        private Long id = null;
        private String nome = "Meta de Teste";
        private String descricao = "Descrição da meta de teste";
        private TipoMeta tipoMeta = TipoMeta.ECONOMIA_MENSAL;
        private BigDecimal valorMeta = new BigDecimal("5000.00");
        private BigDecimal valorAtual = BigDecimal.ZERO;
        private LocalDate dataInicio = LocalDate.now();
        private LocalDate dataFim = LocalDate.now().plusMonths(1);
        private StatusMeta status = StatusMeta.ATIVA;
        private LocalDateTime dataRegistro = LocalDateTime.now();
        private String observacoes = "Observação de teste";
        private BigDecimal percentualConcluido = BigDecimal.ZERO;
        private Long contaId = 1L;

        public MetaEconomiaDTOBuilder comId(Long id) {
            this.id = id;
            return this;
        }

        public MetaEconomiaDTOBuilder comNome(String nome) {
            this.nome = nome;
            return this;
        }

        public MetaEconomiaDTOBuilder comTipoMeta(TipoMeta tipoMeta) {
            this.tipoMeta = tipoMeta;
            return this;
        }

        public MetaEconomiaDTOBuilder comValorMeta(BigDecimal valorMeta) {
            this.valorMeta = valorMeta;
            return this;
        }

        public MetaEconomiaDTOBuilder comValorAtual(BigDecimal valorAtual) {
            this.valorAtual = valorAtual;
            return this;
        }

        public MetaEconomiaDTOBuilder comDataInicio(LocalDate dataInicio) {
            this.dataInicio = dataInicio;
            return this;
        }

        public MetaEconomiaDTOBuilder comDataFim(LocalDate dataFim) {
            this.dataFim = dataFim;
            return this;
        }

        public MetaEconomiaDTOBuilder comStatus(StatusMeta status) {
            this.status = status;
            return this;
        }

        public MetaEconomiaDTOBuilder comContaId(Long contaId) {
            this.contaId = contaId;
            return this;
        }

        public MetaEconomiaDTOBuilder comPercentualConcluido(BigDecimal percentualConcluido) {
            this.percentualConcluido = percentualConcluido;
            return this;
        }

        public MetaEconomiaDTO build() {
            return new MetaEconomiaDTO(
                id, nome, descricao, tipoMeta, valorMeta, valorAtual, dataInicio, dataFim,
                status, dataRegistro, observacoes, percentualConcluido, contaId
            );
        }
    }

    public static class MetaEconomiaBuilder {
        private String nome = "Meta de Teste";
        private String descricao = "Descrição da meta de teste";
        private TipoMeta tipoMeta = TipoMeta.ECONOMIA_MENSAL;
        private BigDecimal valorMeta = new BigDecimal("5000.00");
        private BigDecimal valorAtual = BigDecimal.ZERO;
        private LocalDate dataInicio = LocalDate.now();
        private LocalDate dataFim = LocalDate.now().plusMonths(1);
        private StatusMeta status = StatusMeta.ATIVA;
        private LocalDateTime dataRegistro = LocalDateTime.now();
        private String observacoes = "Observação de teste";
        private BigDecimal percentualConcluido = BigDecimal.ZERO;
        private DadosConta conta = new DadosContaBuilder().build();

        public MetaEconomiaBuilder comNome(String nome) {
            this.nome = nome;
            return this;
        }

        public MetaEconomiaBuilder comTipoMeta(TipoMeta tipoMeta) {
            this.tipoMeta = tipoMeta;
            return this;
        }

        public MetaEconomiaBuilder comValorMeta(BigDecimal valorMeta) {
            this.valorMeta = valorMeta;
            return this;
        }

        public MetaEconomiaBuilder comValorAtual(BigDecimal valorAtual) {
            this.valorAtual = valorAtual;
            return this;
        }

        public MetaEconomiaBuilder comDataInicio(LocalDate dataInicio) {
            this.dataInicio = dataInicio;
            return this;
        }

        public MetaEconomiaBuilder comDataFim(LocalDate dataFim) {
            this.dataFim = dataFim;
            return this;
        }

        public MetaEconomiaBuilder comStatus(StatusMeta status) {
            this.status = status;
            return this;
        }

        public MetaEconomiaBuilder comConta(DadosConta conta) {
            this.conta = conta;
            return this;
        }

        public MetaEconomiaBuilder comPercentualConcluido(BigDecimal percentualConcluido) {
            this.percentualConcluido = percentualConcluido;
            return this;
        }

        public MetaEconomia build() {
            return new MetaEconomia(
                nome, descricao, tipoMeta, valorMeta, valorAtual, dataInicio, dataFim,
                status, dataRegistro, observacoes, percentualConcluido, conta
            );
        }
    }

    public static class RelatorioPDFParametrosDTOBuilder {
        private Long contaId = 1L;
        private LocalDate dataInicio = LocalDate.now().minusMonths(1);
        private LocalDate dataFim = LocalDate.now();
        private TipoMovimentacao tipoMovimentacao = null;
        private String tituloRelatorio = "Relatório de Movimentações Financeiras";
        private Boolean incluirResumo = true;

        public RelatorioPDFParametrosDTOBuilder comContaId(Long contaId) {
            this.contaId = contaId;
            return this;
        }

        public RelatorioPDFParametrosDTOBuilder comDataInicio(LocalDate dataInicio) {
            this.dataInicio = dataInicio;
            return this;
        }

        public RelatorioPDFParametrosDTOBuilder comDataFim(LocalDate dataFim) {
            this.dataFim = dataFim;
            return this;
        }

        public RelatorioPDFParametrosDTOBuilder comTipoMovimentacao(TipoMovimentacao tipoMovimentacao) {
            this.tipoMovimentacao = tipoMovimentacao;
            return this;
        }

        public RelatorioPDFParametrosDTOBuilder comTituloRelatorio(String tituloRelatorio) {
            this.tituloRelatorio = tituloRelatorio;
            return this;
        }

        public RelatorioPDFParametrosDTOBuilder comIncluirResumo(Boolean incluirResumo) {
            this.incluirResumo = incluirResumo;
            return this;
        }

        public RelatorioPDFParametrosDTO build() {
            return new RelatorioPDFParametrosDTO(
                contaId, dataInicio, dataFim, tipoMovimentacao, tituloRelatorio, incluirResumo
            );
        }
    }

    public static DadosContaBuilder dadosConta() {
        return new DadosContaBuilder();
    }

    public static MovimentacaoFinanceiraDTOBuilder movimentacaoFinanceiraDTO() {
        return new MovimentacaoFinanceiraDTOBuilder();
    }

    public static MovimentacaoFinanceiraBuilder movimentacaoFinanceira() {
        return new MovimentacaoFinanceiraBuilder();
    }

    public static MetaEconomiaDTOBuilder metaEconomiaDTO() {
        return new MetaEconomiaDTOBuilder();
    }

    public static MetaEconomiaBuilder metaEconomia() {
        return new MetaEconomiaBuilder();
    }

    public static RelatorioPDFParametrosDTOBuilder relatorioPDFParametrosDTO() {
        return new RelatorioPDFParametrosDTOBuilder();
    }

    public static class RelatorioDadosDTOBuilder {
        private String tituloRelatorio = "Relatório de Movimentações Financeiras";
        private RelatorioDadosDTO.DadosContaResumoDTO conta = new RelatorioDadosDTO.DadosContaResumoDTO(
                "Banco do Brasil", "1234", "567890", "João Silva");
        private LocalDate dataGeracao = LocalDate.now();
        private java.util.List<MovimentacaoFinanceiraDTO> movimentacoes = java.util.List.of();
        private BigDecimal totalReceitas = BigDecimal.ZERO;
        private BigDecimal totalDespesas = BigDecimal.ZERO;
        private BigDecimal saldoLiquido = BigDecimal.ZERO;
        private BigDecimal saldoAtual = BigDecimal.ZERO;
        private LocalDate dataInicio = null;
        private LocalDate dataFim = null;
        private String tipoMovimentacao = null;

        public RelatorioDadosDTOBuilder comTituloRelatorio(String tituloRelatorio) {
            this.tituloRelatorio = tituloRelatorio;
            return this;
        }

        public RelatorioDadosDTOBuilder comConta(RelatorioDadosDTO.DadosContaResumoDTO conta) {
            this.conta = conta;
            return this;
        }

        public RelatorioDadosDTOBuilder comMovimentacoes(java.util.List<MovimentacaoFinanceiraDTO> movimentacoes) {
            this.movimentacoes = movimentacoes;
            return this;
        }

        public RelatorioDadosDTOBuilder comTotalReceitas(BigDecimal totalReceitas) {
            this.totalReceitas = totalReceitas;
            return this;
        }

        public RelatorioDadosDTOBuilder comTotalDespesas(BigDecimal totalDespesas) {
            this.totalDespesas = totalDespesas;
            return this;
        }

        public RelatorioDadosDTOBuilder comSaldoLiquido(BigDecimal saldoLiquido) {
            this.saldoLiquido = saldoLiquido;
            return this;
        }

        public RelatorioDadosDTOBuilder comSaldoAtual(BigDecimal saldoAtual) {
            this.saldoAtual = saldoAtual;
            return this;
        }

        public RelatorioDadosDTO build() {
            return new RelatorioDadosDTO(
                    tituloRelatorio, conta, dataGeracao, movimentacoes,
                    totalReceitas, totalDespesas, saldoLiquido, saldoAtual,
                    dataInicio, dataFim, tipoMovimentacao
            );
        }
    }

    public static RelatorioDadosDTOBuilder relatorioDadosDTO() {
        return new RelatorioDadosDTOBuilder();
    }
}
