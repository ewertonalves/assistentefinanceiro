package com.financeiro.assitentefinanceiro.cadastro.controller;

import com.financeiro.assitentefinanceiro.cadastro.domain.DadosConta;
import com.financeiro.assitentefinanceiro.cadastro.domain.dto.DadosContaDTO;
import com.financeiro.assitentefinanceiro.cadastro.service.CadastroContaService;
import com.financeiro.assitentefinanceiro.cadastro.service.testdata.CadastroTestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes unitários para CadastroContaController")
class CadastroContaControllerTest {

    @Mock
    private CadastroContaService service;

    @InjectMocks
    private CadastroContaController controller;

    private DadosConta contaTeste;
    private DadosContaDTO contaDTOTeste;

    @BeforeEach
    void setUp() {
        contaTeste = CadastroTestDataBuilder.dadosConta().build();
        contaDTOTeste = CadastroTestDataBuilder.dadosContaDTO().build();
    }


    @Test
    @DisplayName("Deve listar todas as contas com sucesso")
    void deveListarTodasContasComSucesso() {
        List<DadosConta> contas = List.of(contaTeste);
        when(service.listarContas()).thenReturn(contas);
        when(service.converterEntidadeParaDTO(any(DadosConta.class))).thenReturn(contaDTOTeste);

        ResponseEntity<List<DadosContaDTO>> resposta = controller.listarContas();

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertNotNull(resposta.getBody());
        assertEquals(1, resposta.getBody().size());
        verify(service).listarContas();
    }

    @Test
    @DisplayName("Deve retornar erro 500 ao listar contas com erro interno")
    void deveRetornarErro500AoListarContasComErroInterno() {
        doThrow(new RuntimeException("Erro interno"))
            .when(service).listarContas();

        ResponseEntity<List<DadosContaDTO>> resposta = controller.listarContas();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resposta.getStatusCode());
        assertNull(resposta.getBody());
    }

    @Test
    @DisplayName("Deve buscar conta por ID com sucesso")
    void deveBuscarContaPorIdComSucesso() {
        when(service.buscarContaPorId(anyLong())).thenReturn(contaTeste);
        when(service.converterEntidadeParaDTO(any(DadosConta.class))).thenReturn(contaDTOTeste);

        ResponseEntity<DadosContaDTO> resposta = controller.buscarContaPorId(1L);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertNotNull(resposta.getBody());
        assertEquals(contaDTOTeste.banco(), resposta.getBody().banco());
        verify(service).buscarContaPorId(1L);
    }

    @Test
    @DisplayName("Deve retornar erro 404 ao buscar conta inexistente")
    void deveRetornarErro404AoBuscarContaInexistente() {
        doThrow(new IllegalArgumentException("Conta não encontrada com ID: 999"))
            .when(service).buscarContaPorId(999L);

        ResponseEntity<DadosContaDTO> resposta = controller.buscarContaPorId(999L);

        assertEquals(HttpStatus.NOT_FOUND, resposta.getStatusCode());
        assertNull(resposta.getBody());
    }

    @Test
    @DisplayName("Deve retornar erro 500 ao buscar conta com erro interno")
    void deveRetornarErro500AoBuscarContaComErroInterno() {
        doThrow(new RuntimeException("Erro interno"))
            .when(service).buscarContaPorId(anyLong());

        ResponseEntity<DadosContaDTO> resposta = controller.buscarContaPorId(1L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resposta.getStatusCode());
        assertNull(resposta.getBody());
    }


    @Test
    @DisplayName("Deve atualizar conta com sucesso")
    void deveAtualizarContaComSucesso() {
        DadosContaDTO dtoAtualizado = CadastroTestDataBuilder.dadosContaDTO()
            .comId(1L)
            .comBanco("Banco Itaú")
            .build();
        
        when(service.atualizarConta(anyLong(), any(DadosContaDTO.class))).thenReturn(contaTeste);
        when(service.converterEntidadeParaDTO(any(DadosConta.class))).thenReturn(dtoAtualizado);

        ResponseEntity<DadosContaDTO> resposta = controller.atualizarConta(1L, dtoAtualizado);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertNotNull(resposta.getBody());
        verify(service).atualizarConta(1L, dtoAtualizado);
    }

    @Test
    @DisplayName("Deve retornar erro 400 ao tentar atualizar conta inexistente")
    void deveRetornarErro400AoAtualizarContaInexistente() {
        DadosContaDTO dtoAtualizado = CadastroTestDataBuilder.dadosContaDTO()
            .comId(999L)
            .build();
        
        doThrow(new IllegalArgumentException("Conta não encontrada com ID: 999"))
            .when(service).atualizarConta(999L, dtoAtualizado);

        ResponseEntity<DadosContaDTO> resposta = controller.atualizarConta(999L, dtoAtualizado);

        assertEquals(HttpStatus.BAD_REQUEST, resposta.getStatusCode());
        assertNull(resposta.getBody());
    }

    @Test
    @DisplayName("Deve retornar erro 400 ao tentar atualizar conta com dados inválidos")
    void deveRetornarErro400AoAtualizarContaComDadosInvalidos() {
        DadosContaDTO dtoInvalido = CadastroTestDataBuilder.dadosContaDTO()
            .comId(1L)
            .comBanco("")
            .build();
        
        doThrow(new IllegalArgumentException("Banco é obrigatório"))
            .when(service).atualizarConta(1L, dtoInvalido);

        ResponseEntity<DadosContaDTO> resposta = controller.atualizarConta(1L, dtoInvalido);

        assertEquals(HttpStatus.BAD_REQUEST, resposta.getStatusCode());
        assertNull(resposta.getBody());
    }

    @Test
    @DisplayName("Deve retornar erro 500 ao atualizar conta com erro interno")
    void deveRetornarErro500AoAtualizarContaComErroInterno() {
        DadosContaDTO dtoAtualizado = CadastroTestDataBuilder.dadosContaDTO()
            .comId(1L)
            .build();
        
        doThrow(new RuntimeException("Erro interno"))
            .when(service).atualizarConta(1L, dtoAtualizado);

        ResponseEntity<DadosContaDTO> resposta = controller.atualizarConta(1L, dtoAtualizado);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resposta.getStatusCode());
        assertNull(resposta.getBody());
    }

}
