package com.financeiro.assitentefinanceiro.financeiro.repository;

import com.financeiro.assitentefinanceiro.financeiro.domain.MetaEconomia;
import com.financeiro.assitentefinanceiro.financeiro.enums.StatusMeta;
import com.financeiro.assitentefinanceiro.financeiro.enums.TipoMeta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MetaEconomiaRepository extends JpaRepository<MetaEconomia, Long> {

    List<MetaEconomia> findByContaId(Long contaId);
    List<MetaEconomia> findByContaIdAndStatus(Long contaId, StatusMeta status);
    List<MetaEconomia> findByContaIdAndTipoMeta(Long contaId, TipoMeta tipoMeta);

    @Query("SELECT m FROM MetaEconomia m WHERE m.conta.id = :contaId AND m.status = 'ATIVA' ORDER BY m.dataFim ASC")
    List<MetaEconomia> findMetasAtivasByContaId(@Param("contaId") Long contaId);

    @Query("SELECT m FROM MetaEconomia m WHERE m.conta.id = :contaId AND m.dataFim < :dataAtual AND m.status != 'CONCLUIDA'")
    List<MetaEconomia> findMetasVencidasByContaId(@Param("contaId") Long contaId, @Param("dataAtual") LocalDate dataAtual);

    @Query("SELECT m FROM MetaEconomia m WHERE m.conta.id = :contaId AND m.dataFim BETWEEN :dataInicio AND :dataFim ORDER BY m.dataFim ASC")
    List<MetaEconomia> findMetasByContaIdAndPeriodo(@Param("contaId") Long contaId, 
        @Param("dataInicio") LocalDate dataInicio, 
        @Param("dataFim") LocalDate dataFim);

    @Query("SELECT COUNT(m) FROM MetaEconomia m WHERE m.conta.id = :contaId")
    long countByContaId(@Param("contaId") Long contaId);

    @Query("SELECT COUNT(m) FROM MetaEconomia m WHERE m.conta.id = :contaId AND m.status = 'CONCLUIDA'")
    long countConcluidasByContaId(@Param("contaId") Long contaId);

    @Query("SELECT m FROM MetaEconomia m WHERE m.conta.id = :contaId AND m.percentualConcluido >= :percentualMinimo ORDER BY m.percentualConcluido DESC")
    List<MetaEconomia> findMetasComProgressoMinimo(@Param("contaId") Long contaId, @Param("percentualMinimo") Double percentualMinimo);
}
