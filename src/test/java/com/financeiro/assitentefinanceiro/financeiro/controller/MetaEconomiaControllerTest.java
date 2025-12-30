package com.financeiro.assitentefinanceiro.financeiro.controller;

import com.financeiro.assitentefinanceiro.ai.service.AssistenteFinanceiroService;
import com.financeiro.assitentefinanceiro.financeiro.domain.MetaEconomia;
import com.financeiro.assitentefinanceiro.financeiro.domain.dto.MetaEconomiaDTO;
import com.financeiro.assitentefinanceiro.financeiro.enums.StatusMeta;
import com.financeiro.assitentefinanceiro.financeiro.enums.TipoMeta;
import com.financeiro.assitentefinanceiro.financeiro.service.MetaEconomiaService;
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
@DisplayName("Testes do MetaEconomiaController")
class MetaEconomiaControllerTest {

    @Mock
    private MetaEconomiaService metaEconomiaService;

    @Mock
    private AssistenteFinanceiroService assistenteService;

    @InjectMocks
    private MetaEconomiaController metaEconomiaController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private MetaEconomiaDTO metaDTO;
    private MetaEconomia meta;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(metaEconomiaController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        metaDTO = new MetaEconomiaDTO(
            1L,
            "Reserva de Emergência",
            "Fundo para emergências",
            TipoMeta.RESERVA_EMERGENCIA,
            BigDecimal.valueOf(10000),
            BigDecimal.valueOf(2000),
            LocalDate.now(),
            LocalDate.now().plusMonths(12),
            StatusMeta.ATIVA,
            LocalDateTime.now(),
            "Meta para emergências",
            BigDecimal.valueOf(20.0),
            1L
        );

        meta = new MetaEconomia(
            "Reserva de Emergência",
            "Fundo para emergências",
            TipoMeta.RESERVA_EMERGENCIA,
            BigDecimal.valueOf(10000),
            BigDecimal.valueOf(2000),
            LocalDate.now(),
            LocalDate.now().plusMonths(12),
            StatusMeta.ATIVA,
            LocalDateTime.now(),
            "Meta para emergências",
            BigDecimal.valueOf(20.0),
            null
        );
    }

    @Test
    @DisplayName("Deve criar meta com análise de IA")
    void deveCriarMetaComAnaliseIA() throws Exception {
        when(metaEconomiaService.criarMeta(any(MetaEconomiaDTO.class)))
            .thenReturn(meta);
        when(assistenteService.analisarViabilidadeMeta(any(MetaEconomiaDTO.class)))
            .thenReturn("Análise de viabilidade da IA");
        when(metaEconomiaService.converterEntidadeParaDTO(any(MetaEconomia.class)))
            .thenReturn(metaDTO);

        mockMvc.perform(post("/api/v1/metas/com-analise-ia")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(metaDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.dados.id").value(1L))
                .andExpect(jsonPath("$.dados.nome").value("Reserva de Emergência"))
                .andExpect(jsonPath("$.mensagem").value("Meta criada com análise de viabilidade"));
    }

    @Test
    @DisplayName("Deve gerar plano de ação com IA")
    void deveGerarPlanoAcaoComIA() throws Exception {
        Long metaId = 1L;

        when(assistenteService.gerarPlanoAcao(anyLong()))
            .thenReturn("Plano de ação detalhado da IA");

        mockMvc.perform(get("/api/v1/metas/{id}/plano-acao", metaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.dados").value("Plano de ação detalhado da IA"))
                .andExpect(jsonPath("$.mensagem").value("Plano de ação gerado com sucesso"));
    }

    @Test
    @DisplayName("Deve gerar sugestões de otimização com IA")
    void deveGerarSugestoesOtimizacaoComIA() throws Exception {
        Long contaId = 1L;

        when(assistenteService.sugerirOtimizacoes(anyLong()))
            .thenReturn("Sugestões de otimização da IA");

        mockMvc.perform(get("/api/v1/metas/conta/{contaId}/sugestoes-otimizacao", contaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.dados").value("Sugestões de otimização da IA"))
                .andExpect(jsonPath("$.mensagem").value("Sugestões geradas com sucesso"));
    }

    @Test
    @DisplayName("Deve tratar erro ao criar meta com análise de IA")
    void deveTratarErroAoCriarMetaComAnaliseIA() throws Exception {
        when(metaEconomiaService.criarMeta(any(MetaEconomiaDTO.class)))
            .thenThrow(new RuntimeException("Erro na IA"));

        mockMvc.perform(post("/api/v1/metas/com-analise-ia")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(metaDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.sucesso").value(false))
                .andExpect(jsonPath("$.mensagem").value("Erro interno: Erro na IA"));
    }

    @Test
    @DisplayName("Deve tratar erro ao gerar plano de ação")
    void deveTratarErroAoGerarPlanoAcao() throws Exception {
        Long metaId = 1L;

        when(assistenteService.gerarPlanoAcao(anyLong()))
            .thenThrow(new RuntimeException("Erro na IA"));

        mockMvc.perform(get("/api/v1/metas/{id}/plano-acao", metaId))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.sucesso").value(false))
                .andExpect(jsonPath("$.mensagem").value("Erro interno: Erro na IA"));
    }

    @Test
    @DisplayName("Deve tratar erro ao gerar sugestões de otimização")
    void deveTratarErroAoGerarSugestoesOtimizacao() throws Exception {
        Long contaId = 1L;

        when(assistenteService.sugerirOtimizacoes(anyLong()))
            .thenThrow(new RuntimeException("Erro na IA"));

        mockMvc.perform(get("/api/v1/metas/conta/{contaId}/sugestoes-otimizacao", contaId))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.sucesso").value(false))
                .andExpect(jsonPath("$.mensagem").value("Erro interno: Erro na IA"));
    }

    @Test
    @DisplayName("Deve tratar erro de validação ao criar meta com análise de IA")
    void deveTratarErroDeValidacaoAoCriarMetaComAnaliseIA() throws Exception {
        when(metaEconomiaService.criarMeta(any(MetaEconomiaDTO.class)))
            .thenThrow(new IllegalArgumentException("Dados inválidos"));

        mockMvc.perform(post("/api/v1/metas/com-analise-ia")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(metaDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.sucesso").value(false))
                .andExpect(jsonPath("$.mensagem").value("Erro de validação: Dados inválidos"));
    }

    @Test
    @DisplayName("Deve tratar erro de validação ao gerar plano de ação")
    void deveTratarErroDeValidacaoAoGerarPlanoAcao() throws Exception {
        Long metaId = 1L;

        when(assistenteService.gerarPlanoAcao(anyLong()))
            .thenThrow(new IllegalArgumentException("Meta não encontrada"));

        mockMvc.perform(get("/api/v1/metas/{id}/plano-acao", metaId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.sucesso").value(false))
                .andExpect(jsonPath("$.mensagem").value("Erro: Meta não encontrada"));
    }

    @Test
    @DisplayName("Deve tratar erro de validação ao gerar sugestões de otimização")
    void deveTratarErroDeValidacaoAoGerarSugestoesOtimizacao() throws Exception {
        Long contaId = 1L;

        when(assistenteService.sugerirOtimizacoes(anyLong()))
            .thenThrow(new IllegalArgumentException("Conta não encontrada"));

        mockMvc.perform(get("/api/v1/metas/conta/{contaId}/sugestoes-otimizacao", contaId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.sucesso").value(false))
                .andExpect(jsonPath("$.mensagem").value("Erro: Conta não encontrada"));
    }

    @Test
    @DisplayName("Deve criar meta com análise de IA com dados válidos")
    void deveCriarMetaComAnaliseIAComDadosValidos() throws Exception {
        MetaEconomiaDTO metaValida = new MetaEconomiaDTO(
            2L,
            "Viagem para Europa",
            "Economizar para viagem",
            TipoMeta.VIAGEM,
            BigDecimal.valueOf(15000),
            BigDecimal.valueOf(3000),
            LocalDate.now(),
            LocalDate.now().plusMonths(12),
            StatusMeta.ATIVA,
            LocalDateTime.now(),
            "Meta para viagem",
            BigDecimal.valueOf(20.0),
            2L
        );

        MetaEconomia metaSalva = new MetaEconomia(
            "Viagem para Europa",
            "Economizar para viagem",
            TipoMeta.VIAGEM,
            BigDecimal.valueOf(15000),
            BigDecimal.valueOf(3000),
            LocalDate.now(),
            LocalDate.now().plusMonths(12),
            StatusMeta.ATIVA,
            LocalDateTime.now(),
            "Meta para viagem",
            BigDecimal.valueOf(20.0),
            null
        );

        when(metaEconomiaService.criarMeta(any(MetaEconomiaDTO.class)))
            .thenReturn(metaSalva);
        when(metaEconomiaService.converterEntidadeParaDTO(any(MetaEconomia.class)))
            .thenReturn(metaValida);

        mockMvc.perform(post("/api/v1/metas/com-analise-ia")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(metaValida)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.dados.nome").value("Viagem para Europa"))
                .andExpect(jsonPath("$.dados.tipoMeta").value("VIAGEM"));
    }

    @Test
    @DisplayName("Deve gerar plano de ação para meta existente")
    void deveGerarPlanoAcaoParaMetaExistente() throws Exception {
        Long metaId = 2L;

        when(assistenteService.gerarPlanoAcao(anyLong()))
            .thenReturn("Plano de ação personalizado para a meta");

        mockMvc.perform(get("/api/v1/metas/{id}/plano-acao", metaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.dados").value("Plano de ação personalizado para a meta"));
    }

    @Test
    @DisplayName("Deve gerar sugestões de otimização para conta existente")
    void deveGerarSugestoesOtimizacaoParaContaExistente() throws Exception {
        Long contaId = 2L;

        when(assistenteService.sugerirOtimizacoes(anyLong()))
            .thenReturn("Sugestões personalizadas para a conta");

        mockMvc.perform(get("/api/v1/metas/conta/{contaId}/sugestoes-otimizacao", contaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.dados").value("Sugestões personalizadas para a conta"));
    }
}
