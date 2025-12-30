package com.financeiro.assitentefinanceiro.config;

import com.financeiro.assitentefinanceiro.login.domain.Usuario;
import com.financeiro.assitentefinanceiro.login.domain.enums.Role;
import com.financeiro.assitentefinanceiro.login.repository.UsuarioRespository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private UsuarioRespository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        criarUsuarioMestre();
    }

    private void criarUsuarioMestre() {
        try {
            String emailMestre = "admin@teste.com";
            
            if (usuarioRepository.existsByEmail(emailMestre)) {
                logger.info("Usuário mestre já existe: {}", emailMestre);
                return;
            }

            String senhaCriptografada = passwordEncoder.encode("123");
            
            Usuario usuarioMestre = new Usuario(
                "Administrador Master",
                emailMestre,
                senhaCriptografada,
                Role.ADMIN
            );

            usuarioRepository.save(usuarioMestre);
            
            logger.info("=== USUÁRIO MESTRE CRIADO COM SUCESSO ===");
            logger.info("Email: {}", emailMestre);
            logger.info("Senha: 123");
            logger.info("Role: {}", Role.ADMIN);
            logger.info("=========================================");
            
        } catch (Exception e) {
            logger.error("Erro ao criar usuário mestre", e);
        }
    }
}
