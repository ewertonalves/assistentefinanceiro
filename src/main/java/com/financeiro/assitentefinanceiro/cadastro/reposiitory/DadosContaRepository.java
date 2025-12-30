package com.financeiro.assitentefinanceiro.cadastro.reposiitory;

import com.financeiro.assitentefinanceiro.cadastro.domain.DadosConta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DadosContaRepository extends JpaRepository<DadosConta, Long> {

    Optional<DadosConta> findByNumeroConta(String numeroConta);

    boolean existsByNumeroConta(String numeroConta);

    boolean existsByNumeroContaAndIdNot(String numeroConta, Long id);
}
