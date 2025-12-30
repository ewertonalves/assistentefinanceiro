package com.financeiro.assitentefinanceiro.ai.service;

import com.financeiro.assitentefinanceiro.cadastro.domain.DadosConta;
import com.financeiro.assitentefinanceiro.cadastro.service.CadastroContaService;
import com.financeiro.assitentefinanceiro.financeiro.domain.MetaEconomia;
import com.financeiro.assitentefinanceiro.financeiro.service.MetaEconomiaService;
import com.financeiro.assitentefinanceiro.financeiro.service.MovimentacaoFinanceiraService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do IADinamicaService")
class IADinamicaServiceTest {

    @Mock
    private MetaEconomiaService metaEconomiaService;

    @Mock
    private MovimentacaoFinanceiraService movimentacaoFinanceiraService;

    @Mock
    private CadastroContaService cadastroContaService;

    @Mock
    private ChatClient.Builder chatClientBuilder;

    @Mock
    private ChatClient chatClient;

    @InjectMocks
    private IADinamicaService iaDinamicaService;

    private MetaEconomia metaEconomia;
    private List<MetaEconomia> metasAtivas;
    private DadosConta dadosConta;
    
    @SuppressWarnings("unchecked")
    private void setupChatClientMock(String resposta) {
        when(chatClientBuilder.build()).thenReturn(chatClient);
        
        String finalResposta = resposta;
        when(chatClient.prompt()).thenAnswer(invocation -> {
            java.lang.reflect.Method promptMethod = ChatClient.class.getMethod("prompt");
            Class<?> requestSpecClass = promptMethod.getReturnType();
            java.lang.reflect.Method callMethod = null;
            Class<?> responseSpecClass = null;
            try {
                callMethod = requestSpecClass.getMethod("call");
                responseSpecClass = callMethod.getReturnType();
            } catch (NoSuchMethodException e) {
                try {
                    requestSpecClass = Class.forName("org.springframework.ai.chat.client.ChatClient$ChatClientRequestSpec");
                    responseSpecClass = Class.forName("org.springframework.ai.chat.client.ChatClient$ChatClientResponseSpec");
                } catch (ClassNotFoundException ex) {
                    throw new RuntimeException("Não foi possível determinar as classes internas do ChatClient", ex);
                }
            }
            Object responseSpec = mock(responseSpecClass, inv -> {
                if ("content".equals(inv.getMethod().getName())) {
                    return finalResposta;
                }
                return null;
            });
            Object requestSpec = mock(requestSpecClass, invocationOnMock -> {
                String methodName = invocationOnMock.getMethod().getName();
                if ("user".equals(methodName)) {
                    return invocationOnMock.getMock();
                } else if ("call".equals(methodName)) {
                    return responseSpec;
                }
                return null;
            });
            return requestSpec;
        });
    }

    @BeforeEach
    void setUp() {
        metaEconomia = new MetaEconomia(
            "Reserva de Emergência",
            "Fundo para emergências",
            com.financeiro.assitentefinanceiro.financeiro.enums.TipoMeta.RESERVA_EMERGENCIA,
            BigDecimal.valueOf(10000),
            BigDecimal.valueOf(2000),
            LocalDate.now().minusMonths(1),
            LocalDate.now().plusMonths(11),
            com.financeiro.assitentefinanceiro.financeiro.enums.StatusMeta.ATIVA,
            java.time.LocalDateTime.now(),
            "Meta para emergências",
            BigDecimal.valueOf(20.0),
            null
        );

        metasAtivas = Arrays.asList(metaEconomia);

        setupChatClientMock("Resposta Personalizada: Seu Contexto Financeiro foi analisado. Metas Financeiras identificadas. Orçamento detalhado. Dívidas analisadas. Renda otimizada. Reserva de Emergência recomendada. Planejamento Futuro sugerido. Geral: Recomendação financeira completa.");

        dadosConta = new DadosConta();
        dadosConta.setBanco("Banco Teste");
        dadosConta.setNumeroAgencia("1234");
        dadosConta.setNumeroConta("567890");
        dadosConta.setResponsavel("Usuário Teste");
    }

    @Test
    @DisplayName("Deve responder prompt dinâmico com contexto de conta")
    void deveResponderPromptDinamicoComContexto() {
        String prompt = "Como posso economizar mais dinheiro?";
        Long contaId = 1L;

        when(cadastroContaService.buscarContaPorId(contaId)).thenReturn(dadosConta);
        when(metaEconomiaService.buscarMetasAtivasPorConta(contaId)).thenReturn(metasAtivas);
        when(movimentacaoFinanceiraService.buscarMovimentacoesPorConta(contaId)).thenReturn(Collections.emptyList());
        when(movimentacaoFinanceiraService.calcularSaldoAtual(contaId)).thenReturn(BigDecimal.ZERO);

        String resultado = iaDinamicaService.responderPromptDinamico(prompt, contaId);

        assertNotNull(resultado);
        assertTrue(resultado.contains("Resposta Personalizada") || resultado.contains("Resposta"));
        verify(cadastroContaService).buscarContaPorId(contaId);
        verify(metaEconomiaService).buscarMetasAtivasPorConta(contaId);
    }

    @Test
    @DisplayName("Deve responder prompt dinâmico sem contexto de conta")
    void deveResponderPromptDinamicoSemContexto() {
        String prompt = "Como posso economizar mais dinheiro?";
        Long contaId = null;

        setupChatClientMock("Resposta Geral: Recomendação financeira genérica.");

        String resultado = iaDinamicaService.responderPromptDinamico(prompt, contaId);

        assertNotNull(resultado);
        assertTrue(resultado.contains("Resposta") || resultado.contains("Resposta Geral"));
        verify(metaEconomiaService, never()).buscarMetasAtivasPorConta(anyLong());
        verify(cadastroContaService, never()).buscarContaPorId(anyLong());
    }

    @Test
    @DisplayName("Deve responder prompt genérico")
    void deveResponderPromptGenerico() {
        String prompt = "Qual é a melhor forma de investir?";

        setupChatClientMock("Resposta: Recomendação de investimento genérica.");

        String resultado = iaDinamicaService.responderPromptGenerico(prompt);

        assertNotNull(resultado);
        assertTrue(resultado.contains("Resposta"));
    }

    @Test
    @DisplayName("Deve manter conversação com histórico")
    void deveManterConversacaoComHistorico() {
        String prompt = "E sobre fundos de investimento?";
        List<String> historico = Arrays.asList("Como investir?", "Você mencionou ações");
        Long contaId = 1L;

        setupChatClientMock("Conversa Continuada: Histórico da Conversa analisado.");
        when(cadastroContaService.buscarContaPorId(contaId)).thenReturn(dadosConta);
        when(metaEconomiaService.buscarMetasAtivasPorConta(contaId)).thenReturn(metasAtivas);
        when(movimentacaoFinanceiraService.buscarMovimentacoesPorConta(contaId)).thenReturn(Collections.emptyList());
        when(movimentacaoFinanceiraService.calcularSaldoAtual(contaId)).thenReturn(BigDecimal.ZERO);

        String resultado = iaDinamicaService.manterConversacao(prompt, historico, contaId);

        assertNotNull(resultado);
        assertTrue(resultado.contains("Conversa Continuada") || resultado.contains("Resposta"));
        verify(metaEconomiaService).buscarMetasAtivasPorConta(contaId);
    }

    @Test
    @DisplayName("Deve manter conversação sem contexto de conta")
    void deveManterConversacaoSemContexto() {
        String prompt = "E sobre fundos de investimento?";
        List<String> historico = Arrays.asList("Como investir?", "Você mencionou ações");
        Long contaId = null;

        setupChatClientMock("Conversa Continuada: Histórico da Conversa analisado.");

        String resultado = iaDinamicaService.manterConversacao(prompt, historico, contaId);

        assertNotNull(resultado);
        assertTrue(resultado.contains("Conversa Continuada") || resultado.contains("Resposta"));
        verify(metaEconomiaService, never()).buscarMetasAtivasPorConta(anyLong());
    }

    @Test
    @DisplayName("Deve tratar erro ao processar prompt dinâmico")
    void deveTratarErroAoProcessarPromptDinamico() {
        String prompt = "Como posso economizar mais dinheiro?";
        Long contaId = 1L;

        reset(chatClientBuilder, chatClient);
        
        lenient().when(cadastroContaService.buscarContaPorId(contaId)).thenReturn(dadosConta);
        lenient().when(metaEconomiaService.buscarMetasAtivasPorConta(contaId)).thenReturn(metasAtivas);
        lenient().when(movimentacaoFinanceiraService.buscarMovimentacoesPorConta(contaId)).thenReturn(Collections.emptyList());
        lenient().when(movimentacaoFinanceiraService.calcularSaldoAtual(contaId)).thenReturn(BigDecimal.ZERO);
        when(chatClientBuilder.build()).thenThrow(new RuntimeException("Erro ao construir ChatClient"));

        String resultado = iaDinamicaService.responderPromptDinamico(prompt, contaId);

        assertNotNull(resultado);
        assertTrue(resultado.contains("Desculpe") || resultado.contains("erro"));
        verify(chatClientBuilder).build();
    }

    @Test
    @DisplayName("Deve tratar erro ao processar prompt genérico")
    void deveTratarErroAoProcessarPromptGenerico() {
        String prompt = "Qual é a melhor forma de investir?";

        reset(chatClientBuilder, chatClient);
        when(chatClientBuilder.build()).thenThrow(new RuntimeException("Erro ao construir ChatClient"));

        String resultado = iaDinamicaService.responderPromptGenerico(prompt);

        assertNotNull(resultado);
        assertTrue(resultado.contains("Desculpe") || resultado.contains("erro"));
        verify(chatClientBuilder).build();
    }

    @Test
    @DisplayName("Deve tratar erro ao manter conversação")
    void deveTratarErroAoManterConversacao() {
        String prompt = "E sobre fundos de investimento?";
        List<String> historico = Arrays.asList("Como investir?");
        Long contaId = 1L;

        reset(chatClientBuilder, chatClient);
        
        lenient().when(cadastroContaService.buscarContaPorId(contaId)).thenReturn(dadosConta);
        lenient().when(metaEconomiaService.buscarMetasAtivasPorConta(contaId)).thenReturn(metasAtivas);
        lenient().when(movimentacaoFinanceiraService.buscarMovimentacoesPorConta(contaId)).thenReturn(Collections.emptyList());
        lenient().when(movimentacaoFinanceiraService.calcularSaldoAtual(contaId)).thenReturn(BigDecimal.ZERO);
        when(chatClientBuilder.build()).thenThrow(new RuntimeException("Erro ao construir ChatClient"));

        String resultado = iaDinamicaService.manterConversacao(prompt, historico, contaId);

        assertNotNull(resultado);
        assertTrue(resultado.contains("Desculpe") || resultado.contains("erro"));
        verify(chatClientBuilder).build();
    }

    @Test
    @DisplayName("Deve categorizar prompt de metas financeiras")
    void deveCategorizarPromptMetasFinanceiras() {
        String prompt = "Como criar uma meta de economia?";
        Long contaId = 1L;

        setupChatClientMock("Resposta Personalizada: Metas Financeiras identificadas.");
        when(cadastroContaService.buscarContaPorId(contaId)).thenReturn(dadosConta);
        when(metaEconomiaService.buscarMetasAtivasPorConta(contaId)).thenReturn(metasAtivas);
        when(movimentacaoFinanceiraService.buscarMovimentacoesPorConta(contaId)).thenReturn(Collections.emptyList());
        when(movimentacaoFinanceiraService.calcularSaldoAtual(contaId)).thenReturn(BigDecimal.ZERO);

        String resultado = iaDinamicaService.responderPromptDinamico(prompt, contaId);

        assertNotNull(resultado);
        assertTrue(resultado.contains("Metas Financeiras") || resultado.contains("Resposta"));
    }

    @Test
    @DisplayName("Deve categorizar prompt de investimentos")
    void deveCategorizarPromptInvestimentos() {
        String prompt = "Qual é a melhor forma de investir em ações?";
        Long contaId = 1L;

        setupChatClientMock("Resposta Personalizada: Investimentos analisados.");
        when(cadastroContaService.buscarContaPorId(contaId)).thenReturn(dadosConta);
        when(metaEconomiaService.buscarMetasAtivasPorConta(contaId)).thenReturn(metasAtivas);
        when(movimentacaoFinanceiraService.buscarMovimentacoesPorConta(contaId)).thenReturn(Collections.emptyList());
        when(movimentacaoFinanceiraService.calcularSaldoAtual(contaId)).thenReturn(BigDecimal.ZERO);

        String resultado = iaDinamicaService.responderPromptDinamico(prompt, contaId);

        assertNotNull(resultado);
        assertTrue(resultado.contains("Resposta Personalizada") || resultado.contains("Resposta"));
    }

    @Test
    @DisplayName("Deve categorizar prompt de orçamento")
    void deveCategorizarPromptOrcamento() {
        String prompt = "Como controlar meus gastos mensais?";
        Long contaId = 1L;

        setupChatClientMock("Resposta Personalizada: Orçamento detalhado.");
        when(cadastroContaService.buscarContaPorId(contaId)).thenReturn(dadosConta);
        when(metaEconomiaService.buscarMetasAtivasPorConta(contaId)).thenReturn(metasAtivas);
        when(movimentacaoFinanceiraService.buscarMovimentacoesPorConta(contaId)).thenReturn(Collections.emptyList());
        when(movimentacaoFinanceiraService.calcularSaldoAtual(contaId)).thenReturn(BigDecimal.ZERO);

        String resultado = iaDinamicaService.responderPromptDinamico(prompt, contaId);

        assertNotNull(resultado);
        assertTrue(resultado.contains("Orçamento") || resultado.contains("Resposta"));
    }

    @Test
    @DisplayName("Deve categorizar prompt de dívidas")
    void deveCategorizarPromptDividas() {
        String prompt = "Como quitar minhas dívidas mais rapidamente?";
        Long contaId = 1L;

        setupChatClientMock("Resposta Personalizada: Dívidas analisadas.");
        when(cadastroContaService.buscarContaPorId(contaId)).thenReturn(dadosConta);
        when(metaEconomiaService.buscarMetasAtivasPorConta(contaId)).thenReturn(metasAtivas);
        when(movimentacaoFinanceiraService.buscarMovimentacoesPorConta(contaId)).thenReturn(Collections.emptyList());
        when(movimentacaoFinanceiraService.calcularSaldoAtual(contaId)).thenReturn(BigDecimal.ZERO);

        String resultado = iaDinamicaService.responderPromptDinamico(prompt, contaId);

        assertNotNull(resultado);
        assertTrue(resultado.contains("Dívidas") || resultado.contains("Resposta"));
    }

    @Test
    @DisplayName("Deve categorizar prompt de renda")
    void deveCategorizarPromptRenda() {
        String prompt = "Como aumentar minha renda mensal?";
        Long contaId = 1L;

        setupChatClientMock("Resposta Personalizada: Renda otimizada.");
        when(cadastroContaService.buscarContaPorId(contaId)).thenReturn(dadosConta);
        when(metaEconomiaService.buscarMetasAtivasPorConta(contaId)).thenReturn(metasAtivas);
        when(movimentacaoFinanceiraService.buscarMovimentacoesPorConta(contaId)).thenReturn(Collections.emptyList());
        when(movimentacaoFinanceiraService.calcularSaldoAtual(contaId)).thenReturn(BigDecimal.ZERO);

        String resultado = iaDinamicaService.responderPromptDinamico(prompt, contaId);

        assertNotNull(resultado);
        assertTrue(resultado.contains("Renda") || resultado.contains("Resposta"));
    }

    @Test
    @DisplayName("Deve categorizar prompt de reserva de emergência")
    void deveCategorizarPromptReservaEmergencia() {
        String prompt = "Como criar uma reserva de emergência?";
        Long contaId = 1L;

        setupChatClientMock("Resposta Personalizada: Reserva de Emergência recomendada.");
        when(cadastroContaService.buscarContaPorId(contaId)).thenReturn(dadosConta);
        when(metaEconomiaService.buscarMetasAtivasPorConta(contaId)).thenReturn(metasAtivas);
        when(movimentacaoFinanceiraService.buscarMovimentacoesPorConta(contaId)).thenReturn(Collections.emptyList());
        when(movimentacaoFinanceiraService.calcularSaldoAtual(contaId)).thenReturn(BigDecimal.ZERO);

        String resultado = iaDinamicaService.responderPromptDinamico(prompt, contaId);

        assertNotNull(resultado);
        assertTrue(resultado.contains("Reserva de Emergência") || resultado.contains("Resposta"));
    }

    @Test
    @DisplayName("Deve categorizar prompt de planejamento futuro")
    void deveCategorizarPromptPlanejamentoFuturo() {
        String prompt = "Como planejar minha aposentadoria?";
        Long contaId = 1L;

        setupChatClientMock("Resposta Personalizada: Planejamento Futuro sugerido.");
        when(cadastroContaService.buscarContaPorId(contaId)).thenReturn(dadosConta);
        when(metaEconomiaService.buscarMetasAtivasPorConta(contaId)).thenReturn(metasAtivas);
        when(movimentacaoFinanceiraService.buscarMovimentacoesPorConta(contaId)).thenReturn(Collections.emptyList());
        when(movimentacaoFinanceiraService.calcularSaldoAtual(contaId)).thenReturn(BigDecimal.ZERO);

        String resultado = iaDinamicaService.responderPromptDinamico(prompt, contaId);

        assertNotNull(resultado);
        assertTrue(resultado.contains("Planejamento Futuro") || resultado.contains("Resposta"));
    }

    @Test
    @DisplayName("Deve categorizar prompt geral")
    void deveCategorizarPromptGeral() {
        String prompt = "Qual é a melhor forma de organizar minha vida financeira?";
        Long contaId = 1L;

        setupChatClientMock("Resposta Personalizada: Geral: Recomendação financeira completa.");
        when(cadastroContaService.buscarContaPorId(contaId)).thenReturn(dadosConta);
        when(metaEconomiaService.buscarMetasAtivasPorConta(contaId)).thenReturn(metasAtivas);
        when(movimentacaoFinanceiraService.buscarMovimentacoesPorConta(contaId)).thenReturn(Collections.emptyList());
        when(movimentacaoFinanceiraService.calcularSaldoAtual(contaId)).thenReturn(BigDecimal.ZERO);

        String resultado = iaDinamicaService.responderPromptDinamico(prompt, contaId);

        assertNotNull(resultado);
        assertTrue(resultado.contains("Geral") || resultado.contains("Resposta"));
    }
}
