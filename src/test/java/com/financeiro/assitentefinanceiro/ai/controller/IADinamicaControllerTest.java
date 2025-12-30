package com.financeiro.assitentefinanceiro.ai.controller;

import com.financeiro.assitentefinanceiro.ai.domain.dto.PromptRequestDTO;
import com.financeiro.assitentefinanceiro.ai.service.IADinamicaService;
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do IADinamicaController")
class IADinamicaControllerTest {

    @Mock
    private IADinamicaService iaDinamicaService;

    @InjectMocks
    private IADinamicaController iaDinamicaController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(iaDinamicaController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Deve responder prompt com contexto de conta")
    void deveResponderPromptComContexto() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("prompt", "Como posso economizar mais dinheiro?");
        request.put("contaId", 1L);

        when(iaDinamicaService.responderPromptDinamico(any(String.class), anyLong()))
            .thenReturn("Resposta da IA sobre economia");

        mockMvc.perform(post("/api/ai/dinamica/responder")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.dados").value("Resposta da IA sobre economia"))
                .andExpect(jsonPath("$.mensagem").value("Resposta gerada com sucesso"));
    }

    @Test
    @DisplayName("Deve responder prompt sem contexto de conta")
    void deveResponderPromptSemContexto() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("prompt", "Como posso economizar mais dinheiro?");

        when(iaDinamicaService.responderPromptGenerico(any(String.class)))
            .thenReturn("Resposta da IA sobre economia");

        mockMvc.perform(post("/api/ai/dinamica/responder")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.dados").value("Resposta da IA sobre economia"))
                .andExpect(jsonPath("$.mensagem").value("Resposta gerada com sucesso"));
    }

    @Test
    @DisplayName("Deve retornar erro quando prompt estiver vazio")
    void deveRetornarErroQuandoPromptVazio() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("prompt", "");

        mockMvc.perform(post("/api/ai/dinamica/responder")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.sucesso").value(false))
                .andExpect(jsonPath("$.mensagem").value("Prompt é obrigatório"));
    }

    @Test
    @DisplayName("Deve responder prompt simples")
    void deveResponderPromptSimples() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("prompt", "Qual é a melhor forma de investir?");

        when(iaDinamicaService.responderPromptGenerico(any(String.class)))
            .thenReturn("Resposta da IA sobre investimentos");

        mockMvc.perform(post("/api/ai/dinamica/responder-simples")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.dados").value("Resposta da IA sobre investimentos"))
                .andExpect(jsonPath("$.mensagem").value("Resposta gerada com sucesso"));
    }

    @Test
    @DisplayName("Deve manter conversação com histórico")
    void deveManterConversacaoComHistorico() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("prompt", "E sobre fundos de investimento?");
        request.put("historico", Arrays.asList("Como investir?", "Você mencionou ações"));
        request.put("contaId", 1L);

        when(iaDinamicaService.manterConversacao(any(String.class), anyList(), anyLong()))
            .thenReturn("Resposta da IA sobre fundos");

        mockMvc.perform(post("/api/ai/dinamica/conversacao")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.dados").value("Resposta da IA sobre fundos"))
                .andExpect(jsonPath("$.mensagem").value("Conversação continuada"));
    }

    @Test
    @DisplayName("Deve manter conversação sem contexto de conta")
    void deveManterConversacaoSemContexto() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("prompt", "E sobre fundos de investimento?");
        request.put("historico", Arrays.asList("Como investir?", "Você mencionou ações"));

        when(iaDinamicaService.manterConversacao(any(String.class), anyList(), any()))
            .thenReturn("Resposta da IA sobre fundos");

        mockMvc.perform(post("/api/ai/dinamica/conversacao")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.dados").value("Resposta da IA sobre fundos"))
                .andExpect(jsonPath("$.mensagem").value("Conversação continuada"));
    }

    @Test
    @DisplayName("Deve retornar erro quando prompt estiver vazio na conversação")
    void deveRetornarErroQuandoPromptVazioNaConversacao() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("prompt", "");
        request.put("historico", Arrays.asList("Como investir?"));

        mockMvc.perform(post("/api/ai/dinamica/conversacao")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.sucesso").value(false))
                .andExpect(jsonPath("$.mensagem").value("Prompt é obrigatório"));
    }

    @Test
    @DisplayName("Deve testar IA rapidamente")
    void deveTestarIARapidamente() throws Exception {
        String pergunta = "Como criar um orçamento mensal?";

        when(iaDinamicaService.responderPromptGenerico(any(String.class)))
            .thenReturn("Resposta da IA sobre orçamento");

        mockMvc.perform(post("/api/ai/dinamica/teste")
                .param("pergunta", pergunta))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.dados").value("Resposta da IA sobre orçamento"))
                .andExpect(jsonPath("$.mensagem").value("Teste realizado com sucesso"));
    }

    @Test
    @DisplayName("Deve responder usando DTO com contexto de conta")
    void deveResponderUsandoDTOComContexto() throws Exception {
        PromptRequestDTO request = new PromptRequestDTO(
            "Preciso de ajuda para planejar minha aposentadoria",
            1L,
            null
        );

        when(iaDinamicaService.responderPromptDinamico(any(String.class), anyLong()))
            .thenReturn("Resposta da IA sobre aposentadoria");

        mockMvc.perform(post("/api/ai/dinamica/responder-dto")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.dados").value("Resposta da IA sobre aposentadoria"))
                .andExpect(jsonPath("$.mensagem").value("Resposta gerada com sucesso"));
    }

    @Test
    @DisplayName("Deve responder usando DTO com conversação")
    void deveResponderUsandoDTOComConversacao() throws Exception {
        PromptRequestDTO request = new PromptRequestDTO(
            "E sobre fundos de investimento?",
            1L,
            Arrays.asList("Como investir?", "Você mencionou ações")
        );

        when(iaDinamicaService.manterConversacao(any(String.class), anyList(), anyLong()))
            .thenReturn("Resposta da IA sobre fundos");

        mockMvc.perform(post("/api/ai/dinamica/responder-dto")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.dados").value("Resposta da IA sobre fundos"))
                .andExpect(jsonPath("$.mensagem").value("Resposta gerada com sucesso"));
    }

    @Test
    @DisplayName("Deve responder usando DTO sem contexto")
    void deveResponderUsandoDTOSemContexto() throws Exception {
        PromptRequestDTO request = new PromptRequestDTO(
            "Qual é a melhor forma de investir?",
            null,
            null
        );

        when(iaDinamicaService.responderPromptGenerico(any(String.class)))
            .thenReturn("Resposta da IA sobre investimentos");

        mockMvc.perform(post("/api/ai/dinamica/responder-dto")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.dados").value("Resposta da IA sobre investimentos"))
                .andExpect(jsonPath("$.mensagem").value("Resposta gerada com sucesso"));
    }

    @Test
    @DisplayName("Deve verificar status da IA")
    void deveVerificarStatusDaIA() throws Exception {
        mockMvc.perform(get("/api/ai/dinamica/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.dados").value("IA dinâmica funcionando normalmente"))
                .andExpect(jsonPath("$.mensagem").value("IA dinâmica operacional e pronta para responder a qualquer pergunta!"));
    }

    @Test
    @DisplayName("Deve tratar erro ao processar prompt")
    void deveTratarErroAoProcessarPrompt() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("prompt", "Como posso economizar mais dinheiro?");
        request.put("contaId", 1L);

        when(iaDinamicaService.responderPromptDinamico(any(String.class), anyLong()))
            .thenThrow(new RuntimeException("Erro na IA"));

        mockMvc.perform(post("/api/ai/dinamica/responder")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.sucesso").value(false))
                .andExpect(jsonPath("$.mensagem").value("Erro ao processar prompt: Erro na IA"));
    }

    @Test
    @DisplayName("Deve tratar erro ao processar prompt simples")
    void deveTratarErroAoProcessarPromptSimples() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("prompt", "Qual é a melhor forma de investir?");

        when(iaDinamicaService.responderPromptGenerico(any(String.class)))
            .thenThrow(new RuntimeException("Erro na IA"));

        mockMvc.perform(post("/api/ai/dinamica/responder-simples")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.sucesso").value(false))
                .andExpect(jsonPath("$.mensagem").value("Erro ao processar prompt: Erro na IA"));
    }

    @Test
    @DisplayName("Deve tratar erro ao manter conversação")
    void deveTratarErroAoManterConversacao() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("prompt", "E sobre fundos de investimento?");
        request.put("historico", Arrays.asList("Como investir?"));
        request.put("contaId", 1L);

        when(iaDinamicaService.manterConversacao(any(String.class), anyList(), anyLong()))
            .thenThrow(new RuntimeException("Erro na IA"));

        mockMvc.perform(post("/api/ai/dinamica/conversacao")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.sucesso").value(false))
                .andExpect(jsonPath("$.mensagem").value("Erro ao processar conversação: Erro na IA"));
    }

    @Test
    @DisplayName("Deve tratar erro no teste rápido")
    void deveTratarErroNoTesteRapido() throws Exception {
        String pergunta = "Como criar um orçamento mensal?";

        when(iaDinamicaService.responderPromptGenerico(any(String.class)))
            .thenThrow(new RuntimeException("Erro na IA"));

        mockMvc.perform(post("/api/ai/dinamica/teste")
                .param("pergunta", pergunta))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.sucesso").value(false))
                .andExpect(jsonPath("$.mensagem").value("Erro no teste: Erro na IA"));
    }
}
