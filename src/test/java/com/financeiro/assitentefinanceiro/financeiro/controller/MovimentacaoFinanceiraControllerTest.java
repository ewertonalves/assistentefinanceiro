package com.financeiro.assitentefinanceiro.financeiro.controller;

import com.financeiro.assitentefinanceiro.financeiro.domain.dto.RelatorioDadosDTO;
import com.financeiro.assitentefinanceiro.financeiro.domain.dto.RelatorioPDFParametrosDTO;
import com.financeiro.assitentefinanceiro.financeiro.enums.TipoMovimentacao;
import com.financeiro.assitentefinanceiro.financeiro.service.MovimentacaoFinanceiraService;
import com.financeiro.assitentefinanceiro.financeiro.service.testdata.TestDataBuilder;
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

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do MovimentacaoFinanceiraController")
class MovimentacaoFinanceiraControllerTest {

    @Mock
    private MovimentacaoFinanceiraService service;

    @InjectMocks
    private MovimentacaoFinanceiraController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    @DisplayName("Deve buscar dados do relatório com sucesso")
    void deveBuscarDadosRelatorioComSucesso() throws Exception {
        RelatorioPDFParametrosDTO parametros = TestDataBuilder.relatorioPDFParametrosDTO().build();
        RelatorioDadosDTO dadosRelatorio = TestDataBuilder.relatorioDadosDTO().build();

        when(service.buscarDadosRelatorio(any(RelatorioPDFParametrosDTO.class)))
                .thenReturn(dadosRelatorio);

        mockMvc.perform(post("/api/v1/movimentacoes/relatorio/dados")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(parametros)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.tituloRelatorio").exists())
                .andExpect(jsonPath("$.conta").exists())
                .andExpect(jsonPath("$.movimentacoes").exists());
    }

    @Test
    @DisplayName("Deve buscar dados do relatório com filtros de período")
    void deveBuscarDadosRelatorioComFiltrosPeriodo() throws Exception {
        LocalDate dataInicio = LocalDate.now().minusMonths(1);
        LocalDate dataFim = LocalDate.now();
        RelatorioPDFParametrosDTO parametros = TestDataBuilder.relatorioPDFParametrosDTO()
                .comDataInicio(dataInicio)
                .comDataFim(dataFim)
                .build();
        RelatorioDadosDTO dadosRelatorio = TestDataBuilder.relatorioDadosDTO().build();

        when(service.buscarDadosRelatorio(any(RelatorioPDFParametrosDTO.class)))
                .thenReturn(dadosRelatorio);

        mockMvc.perform(post("/api/v1/movimentacoes/relatorio/dados")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(parametros)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Deve buscar dados do relatório com filtro de tipo de movimentação")
    void deveBuscarDadosRelatorioComFiltroTipoMovimentacao() throws Exception {
        RelatorioPDFParametrosDTO parametros = TestDataBuilder.relatorioPDFParametrosDTO()
                .comTipoMovimentacao(TipoMovimentacao.RECEITA)
                .build();
        RelatorioDadosDTO dadosRelatorio = TestDataBuilder.relatorioDadosDTO().build();

        when(service.buscarDadosRelatorio(any(RelatorioPDFParametrosDTO.class)))
                .thenReturn(dadosRelatorio);

        mockMvc.perform(post("/api/v1/movimentacoes/relatorio/dados")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(parametros)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Deve retornar erro 400 ao buscar dados com parâmetros inválidos")
    void deveRetornarErro400ComParametrosInvalidos() throws Exception {
        RelatorioPDFParametrosDTO parametros = TestDataBuilder.relatorioPDFParametrosDTO().build();

        when(service.buscarDadosRelatorio(any(RelatorioPDFParametrosDTO.class)))
                .thenThrow(new IllegalArgumentException("ID da conta é obrigatório"));

        mockMvc.perform(post("/api/v1/movimentacoes/relatorio/dados")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(parametros)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar erro 500 ao ocorrer erro interno na busca de dados")
    void deveRetornarErro500ComErroInterno() throws Exception {
        RelatorioPDFParametrosDTO parametros = TestDataBuilder.relatorioPDFParametrosDTO().build();

        when(service.buscarDadosRelatorio(any(RelatorioPDFParametrosDTO.class)))
                .thenThrow(new RuntimeException("Erro interno"));

        mockMvc.perform(post("/api/v1/movimentacoes/relatorio/dados")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(parametros)))
                .andExpect(status().isInternalServerError());
    }
}
