package com.financeiro.assitentefinanceiro.cadastro.domain.dto;

import com.financeiro.assitentefinanceiro.cadastro.domain.DadosConta;
import io.swagger.v3.oas.annotations.media.Schema;

public record DadosContaDTO(@Schema(description = "ID da conta", example = "1", required = true) Long id,
                            @Schema(description = "Banco", example = "Banco do Brasil", required = true) String banco,
                            @Schema(description = "Número da agência", example = "123456", required = true) String numeroAgencia,
                            @Schema(description = "Número da conta", example = "123456", required = true) String numeroConta,
                            @Schema(description = "Tipo de conta", example = "Corrente", required = true) String tipoConta,
                            @Schema(description = "Responsável", example = "João Silva", required = true) String responsavel) {

    public static DadosContaDTO paraCadastro(String banco, String numeroAgencia, String numeroConta, String tipoConta, String responsavel) {
        return new DadosContaDTO(null, banco.trim(), numeroAgencia.trim(), numeroConta.trim(), tipoConta.trim(), responsavel.trim());
    }

    public static DadosContaDTO paraAtualizacao(Long id, String banco, String numeroAgencia, String numeroConta, String tipoConta, String responsavel) {
        return new DadosContaDTO(id, banco.trim(), numeroAgencia.trim(), numeroConta.trim(), tipoConta.trim(), responsavel.trim());
    }

    public static DadosContaDTO fromDadosConta(DadosConta conta) {
        return new DadosContaDTO(conta.getId(), conta.getBanco(), conta.getNumeroAgencia(), conta.getNumeroConta(), conta.getTipoConta(), conta.getResponsavel());
    }
}
