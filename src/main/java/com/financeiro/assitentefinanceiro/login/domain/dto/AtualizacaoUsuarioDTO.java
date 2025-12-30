package com.financeiro.assitentefinanceiro.login.domain.dto;

import com.financeiro.assitentefinanceiro.login.domain.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;

public record AtualizacaoUsuarioDTO(
    @Schema(description = "Nome completo do usuário", example = "João Silva Atualizado", required = true)
    String nome,

    @Schema(description = "Email do usuário", example = "joao.novo@exemplo.com", required = true) 
    String email,

    @Schema(description = "Nova senha do usuário", example = "novaSenha123", required = false)
    String senha,

    @Schema(description = "Perfil do usuário", example = "USER", allowableValues = {"USER", "ADMIN"}, required = true)
    Role role
) {}
