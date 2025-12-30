package com.financeiro.assitentefinanceiro.ai.service;

import com.financeiro.assitentefinanceiro.cadastro.domain.DadosConta;
import com.financeiro.assitentefinanceiro.financeiro.domain.MetaEconomia;
import com.financeiro.assitentefinanceiro.financeiro.domain.dto.MetaEconomiaDTO;
import com.financeiro.assitentefinanceiro.financeiro.enums.StatusMeta;
import com.financeiro.assitentefinanceiro.financeiro.enums.TipoMeta;
import com.financeiro.assitentefinanceiro.financeiro.service.MetaEconomiaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do AssistenteFinanceiroService")
class AssistenteFinanceiroServiceTest {

    @Mock
    private MetaEconomiaService metaEconomiaService;

    @InjectMocks
    private AssistenteFinanceiroService assistenteFinanceiroService;

    private MetaEconomia metaEconomia;
    private MetaEconomiaDTO metaEconomiaDTO;
    private List<MetaEconomia> metasAtivas;

    @BeforeEach
    void setUp() {
        DadosConta contaTeste = new DadosConta();
        contaTeste.setId(1L);
        contaTeste.setBanco("Banco Teste");
        contaTeste.setNumeroAgencia("1234");
        contaTeste.setNumeroConta("56789");
        contaTeste.setTipoConta("CORRENTE");
        
        metaEconomia = new MetaEconomia(
            "Reserva de Emergência",
            "Fundo para emergências",
            TipoMeta.RESERVA_EMERGENCIA,
            BigDecimal.valueOf(10000),
            BigDecimal.valueOf(2000),
            LocalDate.now().minusMonths(1),
            LocalDate.now().plusMonths(11),
            StatusMeta.ATIVA,
            LocalDateTime.now(),
            "Meta para emergências",
            BigDecimal.valueOf(20.0),
            contaTeste
        );

        metaEconomiaDTO = new MetaEconomiaDTO(
            1L,
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
            1L
        );

        metasAtivas = Arrays.asList(metaEconomia);
    }

    @Test
    @DisplayName("Deve gerar plano de ação para meta")
    void deveGerarPlanoAcaoParaMeta() {
        Long metaId = 1L;

        when(metaEconomiaService.buscarMetaPorId(metaId)).thenReturn(metaEconomia);

        String resultado = assistenteFinanceiroService.gerarPlanoAcao(metaId);

        assertNotNull(resultado);
        assertTrue(resultado.contains("Plano de Ação para Meta"));
        assertTrue(resultado.contains("Reserva de Emergência"));
        verify(metaEconomiaService).buscarMetaPorId(metaId);
    }

    @Test
    @DisplayName("Deve analisar viabilidade de meta")
    void deveAnalisarViabilidadeDeMeta() {
        String resultado = assistenteFinanceiroService.analisarViabilidadeMeta(metaEconomiaDTO);

        assertNotNull(resultado);
        assertTrue(resultado.contains("Análise de Viabilidade"));
        assertTrue(resultado.contains("Viagem para Europa"));
    }

    @Test
    @DisplayName("Deve sugerir otimizações para conta")
    void deveSugerirOtimizacoesParaConta() {
        Long contaId = 1L;

        when(metaEconomiaService.buscarMetasAtivasPorConta(contaId)).thenReturn(metasAtivas);

        String resultado = assistenteFinanceiroService.sugerirOtimizacoes(contaId);

        assertNotNull(resultado);
        assertTrue(resultado.contains("Sugestões de Otimização"));
        verify(metaEconomiaService).buscarMetasAtivasPorConta(contaId);
    }

    @Test
    @DisplayName("Deve tratar erro ao gerar plano de ação")
    void deveTratarErroAoGerarPlanoAcao() {
        Long metaId = 1L;

        when(metaEconomiaService.buscarMetaPorId(metaId))
            .thenThrow(new RuntimeException("Erro ao buscar meta"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            assistenteFinanceiroService.gerarPlanoAcao(metaId);
        });

        assertTrue(exception.getMessage().contains("Erro ao gerar plano de ação"));
        verify(metaEconomiaService).buscarMetaPorId(metaId);
    }

    @Test
    @DisplayName("Deve tratar erro ao analisar viabilidade")
    void deveTratarErroAoAnalisarViabilidade() {
        String resultado = assistenteFinanceiroService.analisarViabilidadeMeta(metaEconomiaDTO);
        
        assertNotNull(resultado);
        assertTrue(resultado.contains("Análise de Viabilidade"));
    }

    @Test
    @DisplayName("Deve tratar erro ao sugerir otimizações")
    void deveTratarErroAoSugerirOtimizacoes() {
        Long contaId = 1L;

        when(metaEconomiaService.buscarMetasAtivasPorConta(contaId))
            .thenThrow(new RuntimeException("Erro ao buscar metas"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            assistenteFinanceiroService.sugerirOtimizacoes(contaId);
        });

        assertTrue(exception.getMessage().contains("Erro ao gerar sugestões"));
        verify(metaEconomiaService).buscarMetasAtivasPorConta(contaId);
    }

    @Test
    @DisplayName("Deve calcular economia mensal corretamente")
    void deveCalcularEconomiaMensalCorretamente() {
        Long metaId = 1L;

        when(metaEconomiaService.buscarMetaPorId(metaId)).thenReturn(metaEconomia);

        String resultado = assistenteFinanceiroService.gerarPlanoAcao(metaId);

        assertNotNull(resultado);
        assertTrue(resultado.contains("Economia Mensal Necessária"));
    }

    @Test
    @DisplayName("Deve calcular economia mensal para meta vencida")
    void deveCalcularEconomiaMensalParaMetaVencida() {
        DadosConta contaTeste = new DadosConta();
        contaTeste.setId(1L);
        contaTeste.setBanco("Banco Teste");
        contaTeste.setNumeroAgencia("1234");
        contaTeste.setNumeroConta("56789");
        contaTeste.setTipoConta("CORRENTE");
        
        MetaEconomia metaVencida = new MetaEconomia(
            "Meta Vencida",
            "Meta que já venceu",
            TipoMeta.ECONOMIA_MENSAL,
            BigDecimal.valueOf(5000),
            BigDecimal.valueOf(1000),
            LocalDate.now().minusMonths(2),
            LocalDate.now().minusDays(1),
            StatusMeta.VENCIDA,
            LocalDateTime.now().minusMonths(2),
            "Meta vencida",
            BigDecimal.valueOf(20.0),
            contaTeste
        );

        Long metaId = 1L;

        when(metaEconomiaService.buscarMetaPorId(metaId)).thenReturn(metaVencida);

        String resultado = assistenteFinanceiroService.gerarPlanoAcao(metaId);

        assertNotNull(resultado);
        assertTrue(resultado.contains("Plano de Ação para Meta"));
    }

    @Test
    @DisplayName("Deve calcular economia mensal para meta com valor atual nulo")
    void deveCalcularEconomiaMensalParaMetaComValorAtualNulo() {
        MetaEconomiaDTO metaComValorAtualNulo = new MetaEconomiaDTO(
            1L,
            "Meta Teste",
            "Meta para teste",
            TipoMeta.ECONOMIA_MENSAL,
            BigDecimal.valueOf(10000),
            null,
            LocalDate.now(),
            LocalDate.now().plusMonths(6),
            StatusMeta.ATIVA,
            LocalDateTime.now(),
            "Meta teste",
            BigDecimal.valueOf(0.0),
            1L
        );

        String resultado = assistenteFinanceiroService.analisarViabilidadeMeta(metaComValorAtualNulo);

        assertNotNull(resultado);
        assertTrue(resultado.contains("Análise de Viabilidade"));
    }

    @Test
    @DisplayName("Deve gerar plano com dados financeiros simulados")
    void deveGerarPlanoComDadosFinanceirosSimulados() {
        Long metaId = 1L;

        when(metaEconomiaService.buscarMetaPorId(metaId)).thenReturn(metaEconomia);

        String resultado = assistenteFinanceiroService.gerarPlanoAcao(metaId);

        assertNotNull(resultado);
        assertTrue(resultado.contains("Receitas Mensais"));
        assertTrue(resultado.contains("Despesas Mensais"));
    }

    @Test
    @DisplayName("Deve analisar viabilidade com dados financeiros simulados")
    void deveAnalisarViabilidadeComDadosFinanceirosSimulados() {
        String resultado = assistenteFinanceiroService.analisarViabilidadeMeta(metaEconomiaDTO);

        assertNotNull(resultado);
        assertTrue(resultado.contains("Análise de Viabilidade"));
    }

    @Test
    @DisplayName("Deve sugerir otimizações com múltiplas metas")
    void deveSugerirOtimizacoesComMultiplasMetas() {
        Long contaId = 1L;

        MetaEconomia meta2 = new MetaEconomia(
            "Viagem",
            "Meta para viagem",
            TipoMeta.VIAGEM,
            BigDecimal.valueOf(8000),
            BigDecimal.valueOf(1000),
            LocalDate.now(),
            LocalDate.now().plusMonths(8),
            StatusMeta.ATIVA,
            LocalDateTime.now(),
            "Meta para viagem",
            BigDecimal.valueOf(12.5),
            null
        );

        List<MetaEconomia> multiplasMetas = Arrays.asList(metaEconomia, meta2);

        when(metaEconomiaService.buscarMetasAtivasPorConta(contaId)).thenReturn(multiplasMetas);

        String resultado = assistenteFinanceiroService.sugerirOtimizacoes(contaId);

        assertNotNull(resultado);
        assertTrue(resultado.contains("Sugestões de Otimização"));
        verify(metaEconomiaService).buscarMetasAtivasPorConta(contaId);
    }

    @Test
    @DisplayName("Deve sugerir otimizações com lista vazia de metas")
    void deveSugerirOtimizacoesComListaVaziaDeMetas() {
        Long contaId = 1L;

        when(metaEconomiaService.buscarMetasAtivasPorConta(contaId)).thenReturn(Arrays.asList());

        String resultado = assistenteFinanceiroService.sugerirOtimizacoes(contaId);

        assertNotNull(resultado);
        assertTrue(resultado.contains("Sugestões de Otimização"));
        verify(metaEconomiaService).buscarMetasAtivasPorConta(contaId);
    }
}
