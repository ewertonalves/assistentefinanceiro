package com.financeiro.assitentefinanceiro.ai.controller;

import com.financeiro.assitentefinanceiro.ai.service.AssistenteFinanceiroService;
import com.financeiro.assitentefinanceiro.common.ApiResponse;
import com.financeiro.assitentefinanceiro.financeiro.domain.dto.MetaEconomiaDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai/assistente-financeiro")
@Tag(name = "Assistente Financeiro IA", description = "Endpoints para assistência financeira com IA")
public class AssistenteFinanceiroController {

    private static final Logger logger = LoggerFactory.getLogger(AssistenteFinanceiroController.class);

    private final AssistenteFinanceiroService assistenteService;

    public AssistenteFinanceiroController(AssistenteFinanceiroService assistenteService) {
        this.assistenteService = assistenteService;
    }

    @GetMapping("/plano-acao/{metaId}")
    @Operation(summary = "Gerar plano de ação para meta", description = "Gera um plano de ação personalizado para uma meta financeira específica")
    public ResponseEntity<ApiResponse<String>> gerarPlanoAcao(@Parameter(description = "ID da meta financeira") @PathVariable Long metaId) {

        logger.info("Solicitação de plano de ação para meta ID: {}", metaId);

        try {
            String planoAcao = assistenteService.gerarPlanoAcao(metaId);

            logger.info("Plano de ação gerado com sucesso para meta ID: {}", metaId);
            return ResponseEntity.ok(ApiResponse.sucesso("Plano de ação gerado com sucesso", planoAcao));

        } catch (Exception e) {
            logger.error("Erro ao gerar plano de ação para meta {}: {}", metaId, e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.erro("Erro ao gerar plano de ação: " + e.getMessage()));
        }
    }

    @PostMapping("/analisar-viabilidade")
    @Operation(summary = "Analisar viabilidade de meta", description = "Analisa se uma meta financeira proposta é viável baseada na situação atual")
    public ResponseEntity<ApiResponse<String>> analisarViabilidadeMeta(@RequestBody MetaEconomiaDTO metaDTO) {

        logger.info("Solicitação de análise de viabilidade para meta: {}", metaDTO.nome());

        try {
            String analise = assistenteService.analisarViabilidadeMeta(metaDTO);

            logger.info("Análise de viabilidade concluída para meta: {}", metaDTO.nome());
            return ResponseEntity.ok(ApiResponse.sucesso("Análise de viabilidade concluída", analise));

        } catch (Exception e) {
            logger.error("Erro ao analisar viabilidade da meta: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.erro("Erro ao analisar viabilidade: " + e.getMessage()));
        }
    }

    @GetMapping("/sugestoes-otimizacao/{contaId}")
    @Operation(summary = "Sugerir otimizações", description = "Gera sugestões de otimização para melhorar o progresso das metas ativas")
    public ResponseEntity<ApiResponse<String>> sugerirOtimizacoes(@Parameter(description = "ID da conta") @PathVariable Long contaId) {

        logger.info("Solicitação de sugestões de otimização para conta ID: {}", contaId);

        try {
            String sugestoes = assistenteService.sugerirOtimizacoes(contaId);

            logger.info("Sugestões de otimização geradas com sucesso para conta ID: {}", contaId);
            return ResponseEntity.ok(ApiResponse.sucesso("Sugestões de otimização geradas", sugestoes));

        } catch (Exception e) {
            logger.error("Erro ao gerar sugestões de otimização: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.erro("Erro ao gerar sugestões: " + e.getMessage()));
        }
    }

    @GetMapping("/status")
    @Operation(summary = "Status do assistente", description = "Verifica se o assistente financeiro está funcionando corretamente")
    public ResponseEntity<ApiResponse<String>> verificarStatus() {
        logger.info("Verificando status do assistente financeiro");

        try {
            String status = "Assistente financeiro operacional e pronto para ajudar!";

            return ResponseEntity.ok(ApiResponse.sucesso(status, "Assistente funcionando normalmente"));

        } catch (Exception e) {
            logger.error("Erro ao verificar status do assistente: {}", e.getMessage());
            return ResponseEntity.status(500).body(ApiResponse.erro("Assistente temporariamente indisponível: " + e.getMessage()));
        }
    }
}
