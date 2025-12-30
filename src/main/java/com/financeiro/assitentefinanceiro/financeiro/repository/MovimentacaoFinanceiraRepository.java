package com.financeiro.assitentefinanceiro.financeiro.repository;

import com.financeiro.assitentefinanceiro.financeiro.domain.MovimentacaoFinanceira;
import com.financeiro.assitentefinanceiro.financeiro.enums.CategoriaFinanceira;
import com.financeiro.assitentefinanceiro.financeiro.enums.StatusMovimentacao;
import com.financeiro.assitentefinanceiro.financeiro.enums.TipoMovimentacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MovimentacaoFinanceiraRepository extends JpaRepository<MovimentacaoFinanceira, Long> {

    List<MovimentacaoFinanceira> findByContaId(Long contaId);

    List<MovimentacaoFinanceira> findByContaIdAndTipoMovimentacao(Long contaId, TipoMovimentacao tipoMovimentacao);

    List<MovimentacaoFinanceira> findByContaIdAndCategoria(Long contaId, CategoriaFinanceira categoria);

    List<MovimentacaoFinanceira> findByContaIdAndDataMovimentacaoBetween(Long contaId, LocalDate dataInicio, LocalDate dataFim);

    List<MovimentacaoFinanceira> findByContaIdAndStatus(Long contaId, StatusMovimentacao status);

    @Query("SELECT m FROM MovimentacaoFinanceira m WHERE m.conta.id = :contaId AND m.dataMovimentacao BETWEEN :dataInicio AND :dataFim ORDER BY m.dataMovimentacao DESC")
    List<MovimentacaoFinanceira> findByContaIdAndPeriodo(@Param("contaId") Long contaId, 
        @Param("dataInicio") LocalDate dataInicio, 
        @Param("dataFim") LocalDate dataFim);

    @Query("SELECT m FROM MovimentacaoFinanceira m WHERE m.conta.id = :contaId AND m.tipoMovimentacao = :tipoMovimentacao AND m.dataMovimentacao BETWEEN :dataInicio AND :dataFim ORDER BY m.dataMovimentacao DESC")
    List<MovimentacaoFinanceira> findByContaIdAndTipoMovimentacaoAndPeriodo(@Param("contaId") Long contaId,
        @Param("tipoMovimentacao") TipoMovimentacao tipoMovimentacao,
        @Param("dataInicio") LocalDate dataInicio,
        @Param("dataFim") LocalDate dataFim);

    Optional<MovimentacaoFinanceira> findByIdentificadorExterno(String identificadorExterno);

    List<MovimentacaoFinanceira> findByArquivoOrigem(String arquivoOrigem);

    @Query("SELECT COUNT(m) FROM MovimentacaoFinanceira m WHERE m.conta.id = :contaId")
    long countByContaId(@Param("contaId") Long contaId);

    @Query("SELECT SUM(m.valor) FROM MovimentacaoFinanceira m WHERE m.conta.id = :contaId AND m.tipoMovimentacao = :tipoMovimentacao AND m.status = 'CONCLUIDA'")
    Optional<Double> sumValorByContaIdAndTipoMovimentacao(@Param("contaId") Long contaId, 
        @Param("tipoMovimentacao") TipoMovimentacao tipoMovimentacao);
}
