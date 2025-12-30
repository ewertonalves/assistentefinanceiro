package com.financeiro.assitentefinanceiro.login.domain.dto;

import com.financeiro.assitentefinanceiro.login.domain.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;

public record RegistroUsuarioDTO(
    @Schema(description = "Nome completo do usuário", example = "João Silva", required = true)
    String nome,

    @Schema(description = "Email do usuário (será usado para login)", example = "joao@exemplo.com", required = true)
    String email,

    @Schema(description = "Senha do usuário", example = "minhasenha123", required = true)
    String senha,

    @Schema(description = "Tipo de usuário", example = "USER OR ADMIN", required = true)
    Role role
) {}
