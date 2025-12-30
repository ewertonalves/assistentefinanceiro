package com.financeiro.assitentefinanceiro.config;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

@Configuration
public class OllamaConfig {

    private static final Logger logger = LoggerFactory.getLogger(OllamaConfig.class);
    private final OllamaProperties ollamaProperties;
    @Getter
    private boolean configurationValid = false;

    public OllamaConfig(OllamaProperties ollamaProperties) {
        this.ollamaProperties = ollamaProperties;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void validateOllamaConfiguration() {
        String baseUrl = ollamaProperties.getBaseUrl();
        String apiKey = ollamaProperties.getApiKey();
        boolean isCloud = baseUrl != null && baseUrl.contains("api.ollama.com");

        logger.info("Validando configuração do Ollama...");

        if (isCloud) {
            validateCloudConfiguration(baseUrl, apiKey);
        } else {
            validateLocalConfiguration(baseUrl, apiKey);
        }

        if (configurationValid) {
            logger.info("Configuração do Ollama: VALIDADA");
        } else {
            logger.warn("Configuração do Ollama: ATENÇÃO NECESSÁRIA");
        }
    }

    private void validateCloudConfiguration(String baseUrl, String apiKey) {
        logger.info("Modo: Ollama Cloud");
        logger.info("Base URL: {}", baseUrl);

        if (apiKey == null || apiKey.trim().isEmpty()) {
            logger.error("ERRO: OLLAMA_API_KEY não configurada!");
            logger.error("A aplicação está configurada para usar Ollama Cloud, mas a API key não foi fornecida.");
            logger.error("");
            logger.error("SOLUÇÕES:");
            logger.error("1. Execute o script de configuração:");
            logger.error("   Windows: setup-ollama-env.bat");
            logger.error("   Linux/macOS: source setup-ollama-env.sh");
            logger.error("");
            logger.error("2. Configure manualmente a variável de ambiente:");
            logger.error("   Windows: set OLLAMA_API_KEY=sua_api_key");
            logger.error("   Linux/macOS: export OLLAMA_API_KEY=sua_api_key");
            logger.error("");
            logger.error("3. Ou altere para Ollama Local no application.properties:");
            logger.error("   ollama.base-url=http://localhost:11434");
            configurationValid = false;
        } else {
            if (apiKey.length() < 20) {
                logger.warn("ATENÇÃO: API key parece estar incompleta (menos de 20 caracteres)");
                configurationValid = false;
            } else {
                String maskedKey = maskApiKey(apiKey);
                logger.info("API Key: {} (configurada)", maskedKey);
                logger.info("Modelo: {}", ollamaProperties.getModel());
                logger.info("Status: CONFIGURADO COM SUCESSO");
                configurationValid = true;
            }
        }
    }

    private void validateLocalConfiguration(String baseUrl, String apiKey) {
        logger.info("Modo: Ollama Local");
        logger.info("Base URL: {}", baseUrl != null ? baseUrl : "http://localhost:11434");
        logger.info("Modelo: {}", ollamaProperties.getModel());

        if (apiKey != null && !apiKey.trim().isEmpty()) {
            logger.debug("API key fornecida, mas não é necessária para Ollama Local");
        }

        logger.info("Status: CONFIGURADO (Ollama Local não requer API key)");
        configurationValid = true;
    }

    private String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() < 10) {
            return "***";
        }
        int visibleChars = Math.min(8, apiKey.length() / 4);
        return apiKey.substring(0, visibleChars) + "..." + apiKey.substring(apiKey.length() - 4);
    }

    public String getBaseUrl() {
        return ollamaProperties.getBaseUrl();
    }

    public String getModel() {
        return ollamaProperties.getModel();
    }

    public String getApiKey() {
        return ollamaProperties.getApiKey();
    }
}
