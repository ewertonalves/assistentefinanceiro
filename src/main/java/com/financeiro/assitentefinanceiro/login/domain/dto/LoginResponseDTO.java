package com.financeiro.assitentefinanceiro.login.domain.dto;

public record LoginResponseDTO(
    String token,
    String tipoToken,
    Long expiracaoEm,
    UsuarioResponseDTO usuario
) {
    public static LoginResponseDTO criar(String token, Long expiracao, UsuarioResponseDTO usuario) {
        return new LoginResponseDTO(token, "Bearer", expiracao, usuario);
    }
}
