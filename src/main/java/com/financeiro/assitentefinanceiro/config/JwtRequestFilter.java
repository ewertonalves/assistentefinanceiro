package com.financeiro.assitentefinanceiro.config;

import com.financeiro.assitentefinanceiro.login.service.UsuarioService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtRequestFilter.class);

    private final UsuarioService usuarioService;
    private final JwtTokenUtil jwtTokenUtil;

    public JwtRequestFilter(UsuarioService usuarioService, JwtTokenUtil jwtTokenUtil) {
        this.usuarioService = usuarioService;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain chain) throws ServletException, IOException {

        String requestPath = request.getRequestURI();
        logger.debug("Processando requisição para: {}", requestPath);

        if (requestPath.startsWith("/api/auth/") ||
                requestPath.startsWith("/api/v1/auth/") ||
                requestPath.startsWith("/api/monitoring/") ||
                requestPath.startsWith("/swagger-ui") ||
                requestPath.startsWith("/v3/api-docs") ||
                requestPath.startsWith("/api-docs") ||
                requestPath.startsWith("/h2-console") ||
                requestPath.startsWith("/actuator")) {
            logger.debug("Endpoint público detectado, pulando validação JWT: {}", requestPath);
            chain.doFilter(request, response);
            return;
        }

        final String requestTokenHeader = request.getHeader("Authorization");

        String username = null;
        String jwtToken = null;

        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            try {
                username = jwtTokenUtil.getUsernameFromToken(jwtToken);
                logger.debug("Token JWT válido para usuário: {}", username);
            } catch (Exception e) {
                logger.warn("Token JWT inválido ou expirado: {}", e.getMessage());
            }
        } else {
            logger.debug("Nenhum token JWT fornecido para endpoint protegido: {}", request.getRequestURI());
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = this.usuarioService.loadUserByUsername(username);

                if (jwtTokenUtil.validateToken(jwtToken, userDetails)) {
                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                    logger.debug("Usuário autenticado com sucesso: {}", username);
                } else {
                    logger.warn("Token JWT inválido para usuário: {}", username);
                }
            } catch (Exception e) {
                logger.error("Erro ao validar token JWT para usuário {}: {}", username, e.getMessage());
            }
        }

        chain.doFilter(request, response);
    }
}
