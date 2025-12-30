package com.financeiro.assitentefinanceiro.login.domain.dto;

import com.financeiro.assitentefinanceiro.login.domain.Usuario;

public record UsuarioResponseDTO(
    Long id,
    String nome, 
    String email,
    String perfil,
    boolean ativo
) {
    public static UsuarioResponseDTO fromUsuario(Usuario usuario) {
        return new UsuarioResponseDTO(
            usuario.getId(),
            usuario.getNome(),
            usuario.getEmail(), 
            usuario.getRole().name(),
            usuario.isEnabled()
        );
    }
}
