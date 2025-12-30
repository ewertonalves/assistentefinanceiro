package com.financeiro.assitentefinanceiro.ai.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "DTO para requisições de prompt à IA dinâmica")
public record PromptRequestDTO(
    @Schema(description = "Pergunta ou prompt do usuário", example = "Como posso economizar mais dinheiro?")
    String prompt,
    
    @Schema(description = "ID da conta para contexto financeiro", example = "1")
    Long contaId,
    
    @Schema(description = "Histórico de mensagens da conversação")
    List<String> historico
) {
    
    public PromptRequestDTO {
        if (prompt == null || prompt.trim().isEmpty()) {
            throw new IllegalArgumentException("Prompt é obrigatório");
        }
    }
    
    public static PromptRequestDTO simples(String prompt) {
        return new PromptRequestDTO(prompt, null, null);
    }
    
    public static PromptRequestDTO comConta(String prompt, Long contaId) {
        return new PromptRequestDTO(prompt, contaId, null);
    }
    
    public static PromptRequestDTO comConversacao(String prompt, List<String> historico, Long contaId) {
        return new PromptRequestDTO(prompt, contaId, historico);
    }
}
