package com.financeiro.assitentefinanceiro.ai.controller;

import com.financeiro.assitentefinanceiro.ai.service.AssistenteFinanceiroService;
import com.financeiro.assitentefinanceiro.financeiro.domain.dto.MetaEconomiaDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do AssistenteFinanceiroController")
class AssistenteFinanceiroControllerTest {

    @Mock
    private AssistenteFinanceiroService assistenteFinanceiroService;

    @InjectMocks
    private AssistenteFinanceiroController assistenteFinanceiroController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(assistenteFinanceiroController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    @DisplayName("Deve gerar plano de ação para meta")
    void deveGerarPlanoAcaoParaMeta() throws Exception {
        Long metaId = 1L;

        when(assistenteFinanceiroService.gerarPlanoAcao(anyLong()))
            .thenReturn("Plano de ação detalhado para a meta");

        mockMvc.perform(get("/api/ai/assistente-financeiro/plano-acao/{metaId}", metaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.dados").value("Plano de ação detalhado para a meta"))
                .andExpect(jsonPath("$.mensagem").value("Plano de ação gerado com sucesso"));
    }

    @Test
    @DisplayName("Deve analisar viabilidade de meta")
    void deveAnalisarViabilidadeDeMeta() throws Exception {
        MetaEconomiaDTO metaDTO = new MetaEconomiaDTO(
            1L,
            "Viagem para Europa",
            "Economizar para viagem",
            com.financeiro.assitentefinanceiro.financeiro.enums.TipoMeta.VIAGEM,
            BigDecimal.valueOf(15000),
            BigDecimal.valueOf(3000),
            LocalDate.now(),
            LocalDate.now().plusMonths(12),
            com.financeiro.assitentefinanceiro.financeiro.enums.StatusMeta.ATIVA,
            LocalDateTime.now(),
            "Meta para viagem",
            BigDecimal.valueOf(20.0),
            1L
        );

        when(assistenteFinanceiroService.analisarViabilidadeMeta(any(MetaEconomiaDTO.class)))
            .thenReturn("Análise de viabilidade da meta");

        mockMvc.perform(post("/api/ai/assistente-financeiro/analisar-viabilidade")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(metaDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.dados").value("Análise de viabilidade da meta"))
                .andExpect(jsonPath("$.mensagem").value("Análise de viabilidade concluída"));
    }

    @Test
    @DisplayName("Deve sugerir otimizações para conta")
    void deveSugerirOtimizacoesParaConta() throws Exception {
        Long contaId = 1L;

        when(assistenteFinanceiroService.sugerirOtimizacoes(anyLong()))
            .thenReturn("Sugestões de otimização para as metas");

        mockMvc.perform(get("/api/ai/assistente-financeiro/sugestoes-otimizacao/{contaId}", contaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.dados").value("Sugestões de otimização para as metas"))
                .andExpect(jsonPath("$.mensagem").value("Sugestões de otimização geradas"));
    }

    @Test
    @DisplayName("Deve verificar status do assistente")
    void deveVerificarStatusDoAssistente() throws Exception {
        mockMvc.perform(get("/api/ai/assistente-financeiro/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.dados").value("Assistente funcionando normalmente"))
                .andExpect(jsonPath("$.mensagem").value("Assistente financeiro operacional e pronto para ajudar!"));
    }

    @Test
    @DisplayName("Deve tratar erro ao gerar plano de ação")
    void deveTratarErroAoGerarPlanoAcao() throws Exception {
        Long metaId = 1L;

        when(assistenteFinanceiroService.gerarPlanoAcao(anyLong()))
            .thenThrow(new RuntimeException("Erro ao gerar plano de ação"));

        mockMvc.perform(get("/api/ai/assistente-financeiro/plano-acao/{metaId}", metaId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.sucesso").value(false))
                .andExpect(jsonPath("$.mensagem").value("Erro ao gerar plano de ação: Erro ao gerar plano de ação"));
    }

    @Test
    @DisplayName("Deve tratar erro ao analisar viabilidade")
    void deveTratarErroAoAnalisarViabilidade() throws Exception {
        MetaEconomiaDTO metaDTO = new MetaEconomiaDTO(
            1L,
            "Meta Teste",
            "Meta para teste",
            com.financeiro.assitentefinanceiro.financeiro.enums.TipoMeta.ECONOMIA_MENSAL,
            BigDecimal.valueOf(10000),
            BigDecimal.valueOf(1000),
            LocalDate.now(),
            LocalDate.now().plusMonths(6),
            com.financeiro.assitentefinanceiro.financeiro.enums.StatusMeta.ATIVA,
            LocalDateTime.now(),
            "Meta teste",
            BigDecimal.valueOf(10.0),
            1L
        );

        when(assistenteFinanceiroService.analisarViabilidadeMeta(any(MetaEconomiaDTO.class)))
            .thenThrow(new RuntimeException("Erro ao analisar viabilidade"));

        mockMvc.perform(post("/api/ai/assistente-financeiro/analisar-viabilidade")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(metaDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.sucesso").value(false))
                .andExpect(jsonPath("$.mensagem").value("Erro ao analisar viabilidade: Erro ao analisar viabilidade"));
    }

    @Test
    @DisplayName("Deve tratar erro ao sugerir otimizações")
    void deveTratarErroAoSugerirOtimizacoes() throws Exception {
        Long contaId = 1L;

        when(assistenteFinanceiroService.sugerirOtimizacoes(anyLong()))
            .thenThrow(new RuntimeException("Erro ao gerar sugestões"));

        mockMvc.perform(get("/api/ai/assistente-financeiro/sugestoes-otimizacao/{contaId}", contaId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.sucesso").value(false))
                .andExpect(jsonPath("$.mensagem").value("Erro ao gerar sugestões: Erro ao gerar sugestões"));
    }

    @Test
    @DisplayName("Deve tratar erro ao verificar status")
    void deveTratarErroAoVerificarStatus() throws Exception {
        // O status não chama o serviço, então este teste verifica se o endpoint funciona
        mockMvc.perform(get("/api/ai/assistente-financeiro/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true));
    }

    @Test
    @DisplayName("Deve gerar plano de ação com meta ID válido")
    void deveGerarPlanoAcaoComMetaIdValido() throws Exception {
        Long metaId = 999L;

        when(assistenteFinanceiroService.gerarPlanoAcao(anyLong()))
            .thenReturn("Plano de ação para meta ID 999");

        mockMvc.perform(get("/api/ai/assistente-financeiro/plano-acao/{metaId}", metaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.dados").value("Plano de ação para meta ID 999"));
    }

    @Test
    @DisplayName("Deve sugerir otimizações com conta ID válido")
    void deveSugerirOtimizacoesComContaIdValido() throws Exception {
        Long contaId = 999L;

        when(assistenteFinanceiroService.sugerirOtimizacoes(anyLong()))
            .thenReturn("Sugestões para conta ID 999");

        mockMvc.perform(get("/api/ai/assistente-financeiro/sugestoes-otimizacao/{contaId}", contaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.dados").value("Sugestões para conta ID 999"));
    }

    @Test
    @DisplayName("Deve analisar viabilidade com meta DTO válido")
    void deveAnalisarViabilidadeComMetaDTOValido() throws Exception {
        MetaEconomiaDTO metaDTO = new MetaEconomiaDTO(
            999L,
            "Meta de Teste",
            "Meta para teste de viabilidade",
            com.financeiro.assitentefinanceiro.financeiro.enums.TipoMeta.INVESTIMENTO_ESPECIFICO,
            BigDecimal.valueOf(50000),
            BigDecimal.valueOf(5000),
            LocalDate.now(),
            LocalDate.now().plusMonths(24),
            com.financeiro.assitentefinanceiro.financeiro.enums.StatusMeta.ATIVA,
            LocalDateTime.now(),
            "Meta de teste",
            BigDecimal.valueOf(10.0),
            999L
        );

        when(assistenteFinanceiroService.analisarViabilidadeMeta(any(MetaEconomiaDTO.class)))
            .thenReturn("Análise de viabilidade para meta de teste");

        mockMvc.perform(post("/api/ai/assistente-financeiro/analisar-viabilidade")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(metaDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.dados").value("Análise de viabilidade para meta de teste"));
    }
}
