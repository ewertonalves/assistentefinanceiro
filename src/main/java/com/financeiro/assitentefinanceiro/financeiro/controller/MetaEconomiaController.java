package com.financeiro.assitentefinanceiro.financeiro.controller;

import com.financeiro.assitentefinanceiro.ai.service.AssistenteFinanceiroService;
import com.financeiro.assitentefinanceiro.common.ApiResponse;
import com.financeiro.assitentefinanceiro.financeiro.domain.MetaEconomia;
import com.financeiro.assitentefinanceiro.financeiro.domain.dto.MetaEconomiaDTO;
import com.financeiro.assitentefinanceiro.financeiro.enums.TipoMeta;
import com.financeiro.assitentefinanceiro.financeiro.service.MetaEconomiaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "Metas e Economia", description = "Operações de gerenciamento de metas de economia")
@RestController
@RequestMapping("/api/v1/metas")
public class MetaEconomiaController {

    private final MetaEconomiaService service;
    private final AssistenteFinanceiroService assistenteService;
    private static final Logger logger = LoggerFactory.getLogger(MetaEconomiaController.class);

    public MetaEconomiaController(MetaEconomiaService service, AssistenteFinanceiroService assistenteService) {
        this.service = service;
        this.assistenteService = assistenteService;
    }

    @Operation(summary = "Criar meta de economia", description = "Cria uma nova meta de economia")
    @PostMapping
    public ResponseEntity<MetaEconomiaDTO> criarMeta(@RequestBody MetaEconomiaDTO metaDTO) {
        try {
            logger.info("Solicitação para criar meta de economia. Nome: {}, Valor: {}, Conta: {}", 
                metaDTO.nome(), metaDTO.valorMeta(), metaDTO.contaId());
            
            MetaEconomia meta = service.criarMeta(metaDTO);
            MetaEconomiaDTO metaSalvaDTO = service.converterEntidadeParaDTO(meta);
            
            logger.info("Meta criada com sucesso. ID: {}, Nome: {}, Valor: {}", 
                metaSalvaDTO.id(), metaSalvaDTO.nome(), metaSalvaDTO.valorMeta());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(metaSalvaDTO);
        } catch (IllegalArgumentException e) {
            logger.warn("Erro de validação ao criar meta: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            logger.error("Erro ao criar meta de economia", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Listar metas", description = "Retorna uma lista de todas as metas de economia")
    @GetMapping
    public ResponseEntity<List<MetaEconomiaDTO>> listarMetas() {
        try {
            logger.info("Solicitação para listar todas as metas de economia");
            List<MetaEconomia> metas = service.listarMetas();
            List<MetaEconomiaDTO> metasDTO = metas.stream()
                .map(service::converterEntidadeParaDTO)
                .toList();
            logger.info("Lista de metas retornada com sucesso. Total: {}", metasDTO.size());
            return ResponseEntity.ok(metasDTO);
        } catch (Exception e) {
            logger.error("Erro ao listar metas", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Buscar meta por ID", description = "Retorna os dados de uma meta específica pelo ID")
    @GetMapping("/{id}")
    public ResponseEntity<MetaEconomiaDTO> buscarMetaPorId(
            @Parameter(description = "ID da meta") @PathVariable Long id) {
        try {
            logger.info("Solicitação para buscar meta por ID: {}", id);
            MetaEconomia meta = service.buscarMetaPorId(id);
            MetaEconomiaDTO metaDTO = service.converterEntidadeParaDTO(meta);
            logger.info("Meta encontrada com sucesso. ID: {}, Nome: {}, Progresso: {}%", 
                metaDTO.id(), metaDTO.nome(), metaDTO.percentualConcluido());
            return ResponseEntity.ok(metaDTO);
        } catch (IllegalArgumentException e) {
            logger.warn("Meta não encontrada: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            logger.error("Erro ao buscar meta por ID", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Buscar metas por conta", description = "Retorna metas de uma conta específica")
    @GetMapping("/conta/{contaId}")
    public ResponseEntity<List<MetaEconomiaDTO>> buscarMetasPorConta(
            @Parameter(description = "ID da conta") @PathVariable Long contaId) {
        try {
            logger.info("Solicitação para buscar metas da conta: {}", contaId);
            List<MetaEconomia> metas = service.buscarMetasPorConta(contaId);
            List<MetaEconomiaDTO> metasDTO = metas.stream()
                .map(service::converterEntidadeParaDTO)
                .toList();
            logger.info("Metas da conta {} retornadas com sucesso. Total: {}", contaId, metasDTO.size());
            return ResponseEntity.ok(metasDTO);
        } catch (IllegalArgumentException e) {
            logger.warn("Erro de validação ao buscar metas por conta: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            logger.error("Erro ao buscar metas por conta", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Buscar metas ativas por conta", description = "Retorna apenas metas ativas de uma conta")
    @GetMapping("/conta/{contaId}/ativas")
    public ResponseEntity<List<MetaEconomiaDTO>> buscarMetasAtivasPorConta(
            @Parameter(description = "ID da conta") @PathVariable Long contaId) {
        try {
            logger.info("Solicitação para buscar metas ativas da conta: {}", contaId);
            List<MetaEconomia> metas = service.buscarMetasAtivasPorConta(contaId);
            List<MetaEconomiaDTO> metasDTO = metas.stream()
                .map(service::converterEntidadeParaDTO)
                .toList();
            logger.info("Metas ativas da conta {} retornadas com sucesso. Total: {}", contaId, metasDTO.size());
            return ResponseEntity.ok(metasDTO);
        } catch (IllegalArgumentException e) {
            logger.warn("Erro de validação ao buscar metas ativas: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            logger.error("Erro ao buscar metas ativas", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Buscar metas vencidas por conta", description = "Retorna metas vencidas de uma conta")
    @GetMapping("/conta/{contaId}/vencidas")
    public ResponseEntity<Map<String, Object>> buscarMetasVencidasPorConta(
            @Parameter(description = "ID da conta") @PathVariable Long contaId) {
        try {
            logger.info("Solicitacao para buscar metas vencidas da conta: {}", contaId);
            List<MetaEconomia> metas = service.buscarMetasVencidasPorConta(contaId);
            List<MetaEconomiaDTO> metasDTO = metas.stream()
                .map(service::converterEntidadeParaDTO)
                .toList();
            
            Map<String, Object> response = new HashMap<>();
            response.put("sucesso", true);
            response.put("contaId", contaId);
            response.put("totalMetasVencidas", metasDTO.size());
            response.put("metas", metasDTO);
            response.put("timestamp", java.time.LocalDateTime.now());
            
            logger.info("Metas vencidas da conta {} retornadas com sucesso. Total: {}", contaId, metasDTO.size());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Erro de validacao ao buscar metas vencidas: {}", e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("sucesso", false);
            errorResponse.put("mensagem", e.getMessage());
            errorResponse.put("timestamp", java.time.LocalDateTime.now());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            logger.error("Erro ao buscar metas vencidas", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("sucesso", false);
            errorResponse.put("mensagem", "Erro interno ao buscar metas vencidas");
            errorResponse.put("timestamp", java.time.LocalDateTime.now());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Operation(summary = "Buscar metas por tipo", description = "Retorna metas de uma conta por tipo específico")
    @GetMapping("/conta/{contaId}/tipo/{tipoMeta}")
    public ResponseEntity<List<MetaEconomiaDTO>> buscarMetasPorTipo(
            @Parameter(description = "ID da conta") @PathVariable Long contaId,
            @Parameter(description = "Tipo da meta") @PathVariable TipoMeta tipoMeta) {
        try {
            logger.info("Solicitação para buscar metas da conta {} do tipo {}", contaId, tipoMeta);
            List<MetaEconomia> metas = service.buscarMetasPorTipo(contaId, tipoMeta);
            List<MetaEconomiaDTO> metasDTO = metas.stream()
                .map(service::converterEntidadeParaDTO)
                .toList();
            logger.info("Metas do tipo {} retornadas com sucesso. Total: {}", tipoMeta, metasDTO.size());
            return ResponseEntity.ok(metasDTO);
        } catch (IllegalArgumentException e) {
            logger.warn("Erro de validação ao buscar metas por tipo: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            logger.error("Erro ao buscar metas por tipo", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Atualizar meta", description = "Atualiza os dados de uma meta existente")
    @PutMapping("/{id}")
    public ResponseEntity<MetaEconomiaDTO> atualizarMeta(
            @Parameter(description = "ID da meta") @PathVariable Long id,
            @RequestBody MetaEconomiaDTO metaDTO) {
        try {
            logger.info("Solicitação para atualizar meta. ID: {}, Nome: {}, Valor: {}", 
                id, metaDTO.nome(), metaDTO.valorMeta());
            
            MetaEconomia meta = service.atualizarMeta(id, metaDTO);
            MetaEconomiaDTO metaAtualizadaDTO = service.converterEntidadeParaDTO(meta);
            
            logger.info("Meta atualizada com sucesso. ID: {}, Nome: {}, Progresso: {}%", 
                metaAtualizadaDTO.id(), metaAtualizadaDTO.nome(), metaAtualizadaDTO.percentualConcluido());
            
            return ResponseEntity.ok(metaAtualizadaDTO);
        } catch (IllegalArgumentException e) {
            logger.warn("Erro de validação ao atualizar meta: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            logger.error("Erro ao atualizar meta", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Atualizar progresso da meta", description = "Adiciona valor ao progresso de uma meta")
    @PutMapping("/{id}/progresso")
    public ResponseEntity<MetaEconomiaDTO> atualizarProgressoMeta(
            @Parameter(description = "ID da meta") @PathVariable Long id,
            @Parameter(description = "Valor a ser adicionado ao progresso") @RequestParam BigDecimal valorAdicionado) {
        try {
            logger.info("Solicitação para atualizar progresso da meta. ID: {}, Valor: {}", id, valorAdicionado);
            MetaEconomia meta = service.atualizarProgressoMeta(id, valorAdicionado);
            MetaEconomiaDTO metaAtualizadaDTO = service.converterEntidadeParaDTO(meta);
            logger.info("Progresso da meta atualizado com sucesso. ID: {}, Progresso: {}%", 
                id, metaAtualizadaDTO.percentualConcluido());
            return ResponseEntity.ok(metaAtualizadaDTO);
        } catch (IllegalArgumentException e) {
            logger.warn("Erro de validação ao atualizar progresso: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            logger.error("Erro ao atualizar progresso da meta", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Excluir meta", description = "Remove uma meta pelo ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirMeta(
            @Parameter(description = "ID da meta") @PathVariable Long id) {
        try {
            logger.info("Solicitação para excluir meta. ID: {}", id);
            service.excluirMeta(id);
            logger.info("Meta excluída com sucesso. ID: {}", id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (IllegalArgumentException e) {
            logger.warn("Meta não encontrada para exclusão: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            logger.error("Erro ao excluir meta", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Pausar meta", description = "Pausa uma meta ativa")
    @PostMapping("/{id}/pausar")
    public ResponseEntity<MetaEconomiaDTO> pausarMeta(
            @Parameter(description = "ID da meta") @PathVariable Long id) {
        try {
            logger.info("Solicitação para pausar meta. ID: {}", id);
            MetaEconomia meta = service.pausarMeta(id);
            MetaEconomiaDTO metaPausadaDTO = service.converterEntidadeParaDTO(meta);
            logger.info("Meta pausada com sucesso. ID: {}", id);
            return ResponseEntity.ok(metaPausadaDTO);
        } catch (IllegalArgumentException e) {
            logger.warn("Erro de validação ao pausar meta: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            logger.error("Erro ao pausar meta", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Reativar meta", description = "Reativa uma meta pausada")
    @PostMapping("/{id}/reativar")
    public ResponseEntity<MetaEconomiaDTO> reativarMeta(
            @Parameter(description = "ID da meta") @PathVariable Long id) {
        try {
            logger.info("Solicitacao para reativar meta. ID: {}", id);
            MetaEconomia meta = service.reativarMeta(id);
            MetaEconomiaDTO metaReativadaDTO = service.converterEntidadeParaDTO(meta);
            logger.info("Meta reativada com sucesso. ID: {}", id);
            return ResponseEntity.ok(metaReativadaDTO);
        } catch (IllegalArgumentException e) {
            logger.warn("Erro de validacao ao reativar meta: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .header("X-Error-Message", e.getMessage())
                    .build();
        } catch (Exception e) {
            logger.error("Erro ao reativar meta", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Verificar metas vencidas", description = "Verifica e atualiza status de metas vencidas")
    @PostMapping("/verificar-vencidas")
    public ResponseEntity<Map<String, Object>> verificarMetasVencidas() {
        try {
            logger.info("Solicitacao para verificar metas vencidas");
            int metasVencidas = service.verificarMetasVencidas();
            
            Map<String, Object> response = new HashMap<>();
            response.put("sucesso", true);
            response.put("mensagem", "Verificacao de metas vencidas concluida com sucesso");
            response.put("metasMarcadasComoVencidas", metasVencidas);
            response.put("timestamp", java.time.LocalDateTime.now());
            
            logger.info("Verificacao de metas vencidas concluida com sucesso. {} metas marcadas como vencidas", metasVencidas);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erro ao verificar metas vencidas", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("sucesso", false);
            errorResponse.put("mensagem", "Erro interno ao verificar metas vencidas");
            errorResponse.put("timestamp", java.time.LocalDateTime.now());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Operation(summary = "Criar meta com análise de IA", 
               description = "Cria uma nova meta de economia com análise de viabilidade usando IA")
    @PostMapping("/com-analise-ia")
    public ResponseEntity<ApiResponse<MetaEconomiaDTO>> criarMetaComAnaliseIA(@RequestBody MetaEconomiaDTO metaDTO) {
        try {
            logger.info("Solicitação para criar meta com análise de IA. Nome: {}", metaDTO.nome());
            
            assistenteService.analisarViabilidadeMeta(metaDTO);
            logger.info("Análise de viabilidade concluída para meta: {}", metaDTO.nome());
            
            MetaEconomia meta = service.criarMeta(metaDTO);
            MetaEconomiaDTO metaSalvaDTO = service.converterEntidadeParaDTO(meta);
            
            logger.info("Meta criada com análise de IA. ID: {}, Nome: {}", 
                metaSalvaDTO.id(), metaSalvaDTO.nome());
            
                return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.sucesso("Meta criada com análise de viabilidade", metaSalvaDTO));
                
        } catch (IllegalArgumentException e) {
            logger.warn("Erro de validação ao criar meta com IA: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.erro("Erro de validação: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Erro ao criar meta com análise de IA", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.erro("Erro interno: " + e.getMessage()));
        }
    }

    @Operation(summary = "Gerar plano de ação com IA", 
               description = "Gera um plano de ação personalizado para uma meta usando IA")
    @GetMapping("/{id}/plano-acao")
    public ResponseEntity<ApiResponse<String>> gerarPlanoAcao(
            @Parameter(description = "ID da meta") @PathVariable Long id) {
        try {
            logger.info("Solicitação para gerar plano de ação. Meta ID: {}", id);
            
            String planoAcao = assistenteService.gerarPlanoAcao(id);
            
            logger.info("Plano de ação gerado com sucesso para meta ID: {}", id);
            return ResponseEntity.ok(ApiResponse.sucesso("Plano de ação gerado com sucesso", planoAcao));
            
        } catch (IllegalArgumentException e) {
            logger.warn("Erro ao gerar plano de ação: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.erro("Erro: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Erro ao gerar plano de ação", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.erro("Erro interno: " + e.getMessage()));
        }
    }

    @Operation(summary = "Sugestões de otimização com IA", 
               description = "Gera sugestões de otimização para todas as metas de uma conta usando IA")
    @GetMapping("/conta/{contaId}/sugestoes-otimizacao")
    public ResponseEntity<ApiResponse<String>> gerarSugestoesOtimizacao(
            @Parameter(description = "ID da conta") @PathVariable Long contaId) {
        try {
            logger.info("Solicitação para gerar sugestões de otimização. Conta ID: {}", contaId);
            
            String sugestoes = assistenteService.sugerirOtimizacoes(contaId);
            
            logger.info("Sugestões de otimização geradas com sucesso para conta ID: {}", contaId);
            return ResponseEntity.ok(ApiResponse.sucesso("Sugestões geradas com sucesso", sugestoes));
            
        } catch (IllegalArgumentException e) {
            logger.warn("Erro ao gerar sugestões: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.erro("Erro: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Erro ao gerar sugestões de otimização", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.erro("Erro interno: " + e.getMessage()));
        }
    }
}
