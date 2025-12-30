package com.financeiro.assitentefinanceiro.ai.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class AiService {

    private static final Logger logger = LoggerFactory.getLogger(AiService.class);

    private final ChatClient.Builder chatClientBuilder;
    private final MeterRegistry meterRegistry;

    public AiService(ChatClient.Builder chatClientBuilder, MeterRegistry meterRegistry) {
        this.chatClientBuilder = chatClientBuilder;
        this.meterRegistry = meterRegistry;
    }

    public String processarChat(String message) {
        Counter.builder("assitentefinanceiro_ai_requests_total").description("Total de requisições para IA").register(meterRegistry).increment();

        try {
            logger.info("Tentando processar chat com OpenAI");

            ChatClient chatClient = chatClientBuilder.build();
            String response = chatClient.prompt().user(message).call().content();

            logger.info("Resposta da OpenAI obtida com sucesso");
            return response;

        } catch (Exception e) {
            logger.warn("Falha na OpenAI, usando fallback: {}", e.getMessage());

            Counter.builder("assitentefinanceiro_ai_fallback_total").description("Total de fallbacks da IA").register(meterRegistry).increment();

            return gerarRespostaFallback(message);
        }
    }

    public List<String> gerarSugestaoNomes(int count) {
        try {
            logger.info("Tentando gerar sugestões com OpenAI");

            String prompt = String.format("Gere %d nomes brasileiros únicos para usuários. Retorne apenas os nomes, um por linha, sem numeração.", count);

            ChatClient chatClient = chatClientBuilder.build();
            String response = chatClient.prompt().user(prompt).call().content();

            if (response == null || response.trim().isEmpty()) {
                logger.warn("Resposta vazia da OpenAI para sugestões, usando fallback");
                return gerarSugestoesFallback(count);
            }

            logger.info("Sugestões da OpenAI obtidas com sucesso");
            return Arrays.asList(response.trim().split("\n"));

        } catch (Exception e) {
            logger.warn("Falha na OpenAI para sugestões, usando fallback: {}", e.getMessage());
            return gerarSugestoesFallback(count);
        }
    }

    private String gerarRespostaFallback(String message) {
        String messageLower = message.toLowerCase();

        Map<String[], String> respostas = Map.of(new String[]{"olá", "oi", "hello"}, "Olá! Sou a IA do sistema Assistente Financeiro. Como posso ajudar você hoje? (Modo fallback ativo)", new String[]{"nome", "sugestão"}, "Posso sugerir nomes brasileiros como: João, Maria, Pedro, Ana, Carlos, Fernanda, Lucas, Juliana. Use o endpoint /suggest-names para mais opções!", new String[]{"ajuda", "help"}, "Posso ajudar com:\n- Sugestões de nomes brasileiros\n- Conversas gerais\n- Informações sobre o sistema\n\nObs: Estou em modo fallback (OpenAI indisponível).", new String[]{"sistema", "assitentefinanceiro"}, "O Assistente Financeiro é um sistema de cadastro com autenticação JWT e integração com IA. Oferece endpoints para registro, login, gerenciamento de usuários e chat inteligente.");

        for (Map.Entry<String[], String> entrada : respostas.entrySet()) {
            if (Arrays.stream(entrada.getKey()).anyMatch(messageLower::contains)) {
                return entrada.getValue();
            }
        }

        return String.format("Entendi sua mensagem: '%s'. Esta é uma resposta automática do sistema Assistente Financeiro. " + "A IA real está temporariamente indisponível, mas posso ajudar com informações básicas!", message.length() > 50 ? message.substring(0, 50) + "..." : message);
    }

    private List<String> gerarSugestoesFallback(int count) {
        List<String> nomesBrasileiros = Arrays.asList("João Silva", "Maria Santos", "Pedro Oliveira", "Ana Costa", "Carlos Ferreira", "Fernanda Lima", "Lucas Almeida", "Juliana Rocha", "Rafael Souza", "Camila Pereira", "Bruno Martins", "Larissa Barbosa", "Diego Ribeiro", "Beatriz Carvalho", "Thiago Nascimento", "Gabriela Moreira", "Felipe Araújo", "Mariana Dias", "Rodrigo Castro", "Letícia Gomes", "Eduardo Cardoso", "Isabela Monteiro", "Gustavo Teixeira", "Patrícia Vieira", "Leonardo Pinto");

        return nomesBrasileiros.stream().limit(Math.min(count, nomesBrasileiros.size())).toList();
    }
}
