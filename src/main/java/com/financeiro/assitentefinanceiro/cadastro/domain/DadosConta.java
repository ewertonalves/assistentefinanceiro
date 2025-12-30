package com.financeiro.assitentefinanceiro.cadastro.domain;

import com.financeiro.assitentefinanceiro.cadastro.domain.dto.DadosContaDTO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "dados_conta", indexes = {
    @Index(name = "idx_numero_conta", columnList = "numeroConta", unique = true)
})
public class DadosConta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String banco;

    @Column(nullable = false)
    private String numeroAgencia;

    @Column(nullable = false, unique = true)
    private String numeroConta;

    @Column(nullable = false)
    private String tipoConta;

    @Column(nullable = false)
    private String responsavel;

    public DadosConta(String banco, String numeroAgencia, String numeroConta, String tipoConta, String responsavel) {
        this.banco = banco.trim();
        this.numeroAgencia = numeroAgencia.trim();
        this.numeroConta = numeroConta.trim();
        this.tipoConta = tipoConta.trim();
        this.responsavel = responsavel.trim();
    }

    public static DadosConta fromDTO(DadosContaDTO dto) {
        return new DadosConta(dto.banco(), dto.numeroAgencia(), dto.numeroConta(), dto.tipoConta(), dto.responsavel());
    }

    public void atualizarDados(String banco, String numeroAgencia, String numeroConta, String tipoConta, String responsavel) {
        this.banco = banco.trim();
        this.numeroAgencia = numeroAgencia.trim();
        this.numeroConta = numeroConta.trim();
        this.tipoConta = tipoConta.trim();
        this.responsavel = responsavel.trim();
    }

}
