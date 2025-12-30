package com.financeiro.assitentefinanceiro.login.domain;

import io.swagger.v3.oas.annotations.media.Schema;

public class JwtRequest {
    
    @Schema(description = "Email do usuário (obrigatório)", example = "joao@exemplo.com", required = true)
    private String email;
    
    @Schema(description = "Senha do usuário (obrigatório)", example = "minhasenha123", required = true)
    private String senha;

    public JwtRequest() {
    }

    public JwtRequest(String email, String senha) {
        this.email = email;
        this.senha = senha;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }
}
