package com.financeiro.assitentefinanceiro.ai.controller;

import com.financeiro.assitentefinanceiro.ai.domain.dto.PromptRequestDTO;
import com.financeiro.assitentefinanceiro.ai.service.IADinamicaService;
import com.financeiro.assitentefinanceiro.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai/dinamica")
@Tag(name = "IA Dinâmica", description = "Endpoints para IA que responde a qualquer prompt")
public class IADinamicaController {

    private static final Logger logger = LoggerFactory.getLogger(IADinamicaController.class);

    private final IADinamicaService iaDinamicaService;

    public IADinamicaController(IADinamicaService iaDinamicaService) {
        this.iaDinamicaService = iaDinamicaService;
    }

    @PostMapping("/responder")
    @Operation(summary = "Responder a qualquer prompt", description = "IA dinâmica que responde a qualquer pergunta com contexto financeiro")
    public ResponseEntity<ApiResponse<String>> responderPrompt(@RequestBody Map<String, Object> request) {
        
        String prompt = (String) request.get("prompt");
        Long contaId = request.get("contaId") != null ? 
            Long.valueOf(request.get("contaId").toString()) : null;
        
        logger.info("Solicitação de resposta dinâmica. Prompt: '{}', Conta: {}", prompt, contaId);
        
        try {
            if (prompt == null || prompt.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.erro("Prompt é obrigatório"));
            }
            
            String resposta = contaId != null ? 
                iaDinamicaService.responderPromptDinamico(prompt, contaId) :
                iaDinamicaService.responderPromptGenerico(prompt);
            
            logger.info("Resposta dinâmica gerada com sucesso");
            return ResponseEntity.ok(ApiResponse.sucesso("Resposta gerada com sucesso", resposta));
            
        } catch (Exception e) {
            logger.error("Erro ao gerar resposta dinâmica: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.erro("Erro ao processar prompt: " + e.getMessage()));
        }
    }

    @PostMapping("/responder-simples")
    @Operation(summary = "Responder prompt simples", description = "IA dinâmica para prompts simples sem contexto de conta")
    public ResponseEntity<ApiResponse<String>> responderPromptSimples(@RequestBody Map<String, String> request) {
        
        String prompt = request.get("prompt");
        
        logger.info("Solicitação de resposta simples. Prompt: '{}'", prompt);
        
        try {
            if (prompt == null || prompt.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.erro("Prompt é obrigatório"));
            }
            
            String resposta = iaDinamicaService.responderPromptGenerico(prompt);
            
            logger.info("Resposta simples gerada com sucesso");
            return ResponseEntity.ok(ApiResponse.sucesso("Resposta gerada com sucesso", resposta));
            
        } catch (Exception e) {
            logger.error("Erro ao gerar resposta simples: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.erro("Erro ao processar prompt: " + e.getMessage()));
        }
    }

    @PostMapping("/conversacao")
    @Operation(summary = "Manter conversação", description = "IA dinâmica que mantém contexto de conversação")
    public ResponseEntity<ApiResponse<String>> manterConversacao(@RequestBody Map<String, Object> request) {
        
        String prompt = (String) request.get("prompt");
        @SuppressWarnings("unchecked")
        List<String> historico = (List<String>) request.get("historico");
        Long contaId = request.get("contaId") != null ? 
            Long.valueOf(request.get("contaId").toString()) : null;
        
        logger.info("Solicitação de conversação. Prompt: '{}', Histórico: {} mensagens, Conta: {}", 
            prompt, historico != null ? historico.size() : 0, contaId);
        
        try {
            if (prompt == null || prompt.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.erro("Prompt é obrigatório"));
            }
            
            String resposta = iaDinamicaService.manterConversacao(prompt, historico, contaId);
            
            logger.info("Conversação mantida com sucesso");
            return ResponseEntity.ok(ApiResponse.sucesso("Conversação continuada", resposta));
            
        } catch (Exception e) {
            logger.error("Erro ao manter conversação: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.erro("Erro ao processar conversação: " + e.getMessage()));
        }
    }

    @GetMapping("/status")
    @Operation(summary = "Status da IA dinâmica", description = "Verifica se a IA dinâmica está funcionando")
    public ResponseEntity<ApiResponse<String>> verificarStatus() {
        logger.info("Verificando status da IA dinâmica");
        
        try {
            String status = "IA dinâmica operacional e pronta para responder a qualquer pergunta!";
            
            return ResponseEntity.ok(ApiResponse.sucesso(status, "IA dinâmica funcionando normalmente"));
            
        } catch (Exception e) {
            logger.error("Erro ao verificar status da IA dinâmica: {}", e.getMessage());
            return ResponseEntity.status(500)
                .body(ApiResponse.erro("IA dinâmica temporariamente indisponível: " + e.getMessage()));
        }
    }

    @PostMapping("/teste")
    @Operation(summary = "Teste rápido da IA", description = "Endpoint para testar rapidamente a IA dinâmica")
    public ResponseEntity<ApiResponse<String>> testeRapido(@Parameter(description = "Pergunta para testar a IA") @RequestParam String pergunta) {
        
        logger.info("Teste rápido da IA. Pergunta: '{}'", pergunta);
        
        try {
            String resposta = iaDinamicaService.responderPromptGenerico(pergunta);
            
            logger.info("Teste rápido concluído com sucesso");
            return ResponseEntity.ok(ApiResponse.sucesso("Teste realizado com sucesso", resposta));
            
        } catch (Exception e) {
            logger.error("Erro no teste rápido: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.erro("Erro no teste: " + e.getMessage()));
        }
    }

    @PostMapping("/responder-dto")
    @Operation(summary = "Responder usando DTO", description = "IA dinâmica usando DTO estruturado")
    public ResponseEntity<ApiResponse<String>> responderComDTO(@RequestBody PromptRequestDTO request) {
        
        logger.info("Solicitação com DTO. Prompt: '{}', Conta: {}", request.prompt(), request.contaId());
        
        try {
            String resposta;
            
            if (request.historico() != null && !request.historico().isEmpty()) {
                resposta = iaDinamicaService.manterConversacao(request.prompt(), request.historico(), request.contaId());
            } else if (request.contaId() != null) {
                resposta = iaDinamicaService.responderPromptDinamico(request.prompt(), request.contaId());
            } else {
                resposta = iaDinamicaService.responderPromptGenerico(request.prompt());
            }
            
            logger.info("Resposta com DTO gerada com sucesso");
            return ResponseEntity.ok(ApiResponse.sucesso("Resposta gerada com sucesso", resposta));
            
        } catch (Exception e) {
            logger.error("Erro ao gerar resposta com DTO: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.erro("Erro ao processar prompt: " + e.getMessage()));
        }
    }
}
