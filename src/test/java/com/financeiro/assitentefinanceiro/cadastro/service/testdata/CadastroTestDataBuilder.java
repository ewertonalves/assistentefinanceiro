package com.financeiro.assitentefinanceiro.cadastro.service.testdata;

import com.financeiro.assitentefinanceiro.cadastro.domain.DadosConta;
import com.financeiro.assitentefinanceiro.cadastro.domain.dto.DadosContaDTO;

public class CadastroTestDataBuilder {

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

        public DadosContaBuilder comNumeroAgencia(String numeroAgencia) {
            this.numeroAgencia = numeroAgencia;
            return this;
        }

        public DadosContaBuilder comNumeroConta(String numeroConta) {
            this.numeroConta = numeroConta;
            return this;
        }

        public DadosContaBuilder comTipoConta(String tipoConta) {
            this.tipoConta = tipoConta;
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

    public static class DadosContaDTOBuilder {
        private Long id = null;
        private String banco = "Banco do Brasil";
        private String numeroAgencia = "1234";
        private String numeroConta = "567890";
        private String tipoConta = "Corrente";
        private String responsavel = "João Silva";

        public DadosContaDTOBuilder comId(Long id) {
            this.id = id;
            return this;
        }

        public DadosContaDTOBuilder comBanco(String banco) {
            this.banco = banco;
            return this;
        }

        public DadosContaDTOBuilder comNumeroAgencia(String numeroAgencia) {
            this.numeroAgencia = numeroAgencia;
            return this;
        }

        public DadosContaDTOBuilder comNumeroConta(String numeroConta) {
            this.numeroConta = numeroConta;
            return this;
        }

        public DadosContaDTOBuilder comTipoConta(String tipoConta) {
            this.tipoConta = tipoConta;
            return this;
        }

        public DadosContaDTOBuilder comResponsavel(String responsavel) {
            this.responsavel = responsavel;
            return this;
        }

        public DadosContaDTO build() {
            return new DadosContaDTO(id, banco, numeroAgencia, numeroConta, tipoConta, responsavel);
        }
    }

    public static DadosContaBuilder dadosConta() {
        return new DadosContaBuilder();
    }

    public static DadosContaDTOBuilder dadosContaDTO() {
        return new DadosContaDTOBuilder();
    }
}
