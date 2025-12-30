package com.financeiro.assitentefinanceiro.ai.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.financeiro.assitentefinanceiro.ai.service.AiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import com.financeiro.assitentefinanceiro.common.ApiResponse;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin
@Tag(name = "Inteligência Artificial", description = "Endpoints para integração com IA - Requer autenticação JWT")
@SecurityRequirement(name = "Bearer Authentication")
public class AiController {

    private static final Logger logger = LoggerFactory.getLogger(AiController.class);

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @Operation(summary = "Chat com IA", description = "Envia uma mensagem para a IA e recebe uma resposta")
    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<Map<String, Object>>> chatWithAi(@RequestBody Map<String, String> request) {
        try {
            String message = request.get("message");
            if (message == null || message.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.erro("Mensagem obrigatória", "Por favor, informe uma mensagem para enviar à IA."));
            }

            logger.info("Processando requisição de chat com IA: {}", message.substring(0, Math.min(message.length(), 50)) + "...");

            String aiResponse = aiService.processarChat(message);

            Map<String, Object> responseData = Map.of(
                    "mensagem_enviada", message,
                    "resposta_ia", aiResponse,
                    "timestamp", System.currentTimeMillis(),
                    "fonte", "openai_com_fallback"
            );

            logger.info("Resposta da IA processada com sucesso");
            return ResponseEntity.ok(
                    ApiResponse.sucesso("Resposta da IA gerada com sucesso!", responseData)
            );

        } catch (Exception e) {
            logger.error("Erro ao processar chat com IA: {}", e.getMessage());

            if (e.getMessage() != null && e.getMessage().contains("quota")) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(ApiResponse.erro("Serviço temporariamente indisponível",
                                "A cota da API OpenAI foi excedida. Tente novamente mais tarde ou configure uma chave válida."));
            }

            if (e.getMessage() != null && (e.getMessage().contains("401") || e.getMessage().contains("authentication"))) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.erro("Configuração da IA inválida",
                                "Chave da API OpenAI inválida ou não configurada. Verifique a configuração."));
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.erro("Erro interno",
                            "Não foi possível processar sua mensagem no momento. Tente novamente."));
        }
    }

    @Operation(summary = "Sugestão de nome", description = "Gera sugestões de nomes para usuários")
    @GetMapping("/suggest-names")
    public ResponseEntity<ApiResponse<Map<String, Object>>> suggestNames(@RequestParam(defaultValue = "5") int count) {
        try {
            if (count <= 0 || count > 20) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.erro("Quantidade inválida", "Informe um número entre 1 e 20 para a quantidade de sugestões."));
            }

            logger.info("Gerando {} sugestões de nomes brasileiros", count);

            List<String> suggestions = aiService.gerarSugestaoNomes(count);

            Map<String, Object> responseData = Map.of(
                    "sugestoes", suggestions,
                    "quantidade_solicitada", count,
                    "quantidade_retornada", suggestions.size(),
                    "timestamp", System.currentTimeMillis(),
                    "fonte", "openai_com_fallback"
            );

            logger.info("Sugestões de nomes processadas com sucesso");
            return ResponseEntity.ok(
                    ApiResponse.sucesso("Sugestões de nomes geradas com sucesso!", responseData)
            );

        } catch (Exception e) {
            logger.error("Erro ao gerar sugestões de nomes: {}", e.getMessage());
            if (e.getMessage() != null && e.getMessage().contains("quota")) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(ApiResponse.erro("Serviço temporariamente indisponível",
                                "A cota da API OpenAI foi excedida. Tente novamente mais tarde ou configure uma chave válida."));
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.erro("Erro interno",
                            "Não foi possível gerar sugestões no momento. Tente novamente."));
        }
    }
}
