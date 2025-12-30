package com.financeiro.assitentefinanceiro.cadastro.service;

import com.financeiro.assitentefinanceiro.cadastro.domain.DadosConta;
import com.financeiro.assitentefinanceiro.cadastro.domain.dto.DadosContaDTO;
import com.financeiro.assitentefinanceiro.cadastro.reposiitory.DadosContaRepository;
import com.financeiro.assitentefinanceiro.cadastro.service.testdata.CadastroTestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes unitários para CadastroContaService")
class CadastroContaServiceTest {

    @Mock
    private DadosContaRepository repository;

    @InjectMocks
    private CadastroContaService service;

    private DadosConta contaTeste;
    private DadosContaDTO contaDTOTeste;

    @BeforeEach
    void setUp() {
        contaTeste = CadastroTestDataBuilder.dadosConta().build();
        contaDTOTeste = CadastroTestDataBuilder.dadosContaDTO().build();
    }

    @Test
    @DisplayName("Deve cadastrar conta com sucesso")
    void deveCadastrarContaComSucesso() {
        when(repository.save(any(DadosConta.class))).thenReturn(contaTeste);

        DadosConta resultado = service.cadastrarConta(contaDTOTeste);

        assertNotNull(resultado);
        assertEquals(contaTeste.getId(), resultado.getId());
        assertEquals(contaTeste.getBanco(), resultado.getBanco());
        assertEquals(contaTeste.getResponsavel(), resultado.getResponsavel());
        verify(repository).save(any(DadosConta.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar cadastrar conta com dados inválidos")
    void deveLancarExcecaoComDadosInvalidos() {
        DadosContaDTO dtoInvalido = CadastroTestDataBuilder.dadosContaDTO()
            .comBanco("")
            .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> service.cadastrarConta(dtoInvalido));
        
        assertEquals("Banco é obrigatório", exception.getMessage());
        verify(repository, never()).save(any(DadosConta.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar cadastrar conta com responsável vazio")
    void deveLancarExcecaoComResponsavelVazio() {
        DadosContaDTO dtoInvalido = CadastroTestDataBuilder.dadosContaDTO()
            .comResponsavel("")
            .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> service.cadastrarConta(dtoInvalido));
        
        assertEquals("Responsável é obrigatório", exception.getMessage());
    }

    @Test
    @DisplayName("Deve listar todas as contas com sucesso")
    void deveListarTodasContasComSucesso() {
        List<DadosConta> contas = List.of(contaTeste);
        when(repository.findAll()).thenReturn(contas);

        List<DadosConta> resultado = service.listarContas();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(contaTeste.getId(), resultado.get(0).getId());
        verify(repository).findAll();
    }

    @Test
    @DisplayName("Deve buscar conta por ID com sucesso")
    void deveBuscarContaPorIdComSucesso() {
        when(repository.findById(anyLong())).thenReturn(Optional.of(contaTeste));

        DadosConta resultado = service.buscarContaPorId(1L);

        assertNotNull(resultado);
        assertEquals(contaTeste.getId(), resultado.getId());
        assertEquals(contaTeste.getBanco(), resultado.getBanco());
        verify(repository).findById(1L);
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar conta inexistente")
    void deveLancarExcecaoAoBuscarContaInexistente() {
        when(repository.findById(anyLong())).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> service.buscarContaPorId(999L));
        
        assertEquals("Conta não encontrada com ID: 999", exception.getMessage());
    }


    @Test
    @DisplayName("Deve atualizar conta com sucesso")
    void deveAtualizarContaComSucesso() {
        DadosContaDTO dtoAtualizado = CadastroTestDataBuilder.dadosContaDTO()
            .comId(1L)
            .comBanco("Banco Itaú")
            .comResponsavel("Maria Santos")
            .build();
        
        when(repository.findById(anyLong())).thenReturn(Optional.of(contaTeste));
        when(repository.save(any(DadosConta.class))).thenReturn(contaTeste);

        DadosConta resultado = service.atualizarConta(1L, dtoAtualizado);

        assertNotNull(resultado);
        verify(repository).findById(1L);
        verify(repository).save(any(DadosConta.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar atualizar conta inexistente")
    void deveLancarExcecaoAoAtualizarContaInexistente() {
        DadosContaDTO dtoAtualizado = CadastroTestDataBuilder.dadosContaDTO()
            .comId(999L)
            .build();
        
        when(repository.findById(anyLong())).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> service.atualizarConta(999L, dtoAtualizado));
        
        assertEquals("Conta não encontrada com ID: 999", exception.getMessage());
        verify(repository, never()).save(any(DadosConta.class));
    }


    @Test
    @DisplayName("Deve converter entidade para DTO com sucesso")
    void deveConverterEntidadeParaDTOComSucesso() {
        DadosContaDTO dto = service.converterEntidadeParaDTO(contaTeste);

        assertNotNull(dto);
        assertEquals(contaTeste.getId(), dto.id());
        assertEquals(contaTeste.getBanco(), dto.banco());
        assertEquals(contaTeste.getNumeroAgencia(), dto.numeroAgencia());
        assertEquals(contaTeste.getNumeroConta(), dto.numeroConta());
        assertEquals(contaTeste.getTipoConta(), dto.tipoConta());
        assertEquals(contaTeste.getResponsavel(), dto.responsavel());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar usar ID inválido")
    void deveLancarExcecaoComIdInvalido() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> service.buscarContaPorId(null));
        
        assertEquals("ID inválido", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar usar ID zero")
    void deveLancarExcecaoComIdZero() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> service.buscarContaPorId(0L));
        
        assertEquals("ID inválido", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar usar ID negativo")
    void deveLancarExcecaoComIdNegativo() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> service.buscarContaPorId(-1L));
        
        assertEquals("ID inválido", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar cadastrar conta com banco nulo")
    void deveLancarExcecaoComBancoNulo() {
        DadosContaDTO dtoInvalido = CadastroTestDataBuilder.dadosContaDTO()
            .comBanco(null)
            .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> service.cadastrarConta(dtoInvalido));
        
        assertEquals("Banco é obrigatório", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar cadastrar conta com responsável nulo")
    void deveLancarExcecaoComResponsavelNulo() {
        DadosContaDTO dtoInvalido = CadastroTestDataBuilder.dadosContaDTO()
            .comResponsavel(null)
            .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> service.cadastrarConta(dtoInvalido));
        
        assertEquals("Responsável é obrigatório", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar cadastrar conta com número da agência vazio")
    void deveLancarExcecaoComNumeroAgenciaVazio() {
        DadosContaDTO dtoInvalido = CadastroTestDataBuilder.dadosContaDTO()
            .comNumeroAgencia("")
            .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> service.cadastrarConta(dtoInvalido));
        
        assertEquals("Número da agência é obrigatório", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar cadastrar conta com número da conta vazio")
    void deveLancarExcecaoComNumeroContaVazio() {
        DadosContaDTO dtoInvalido = CadastroTestDataBuilder.dadosContaDTO()
            .comNumeroConta("")
            .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> service.cadastrarConta(dtoInvalido));
        
        assertEquals("Número da conta é obrigatório", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar cadastrar conta com tipo de conta vazio")
    void deveLancarExcecaoComTipoContaVazio() {
        DadosContaDTO dtoInvalido = CadastroTestDataBuilder.dadosContaDTO()
            .comTipoConta("")
            .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> service.cadastrarConta(dtoInvalido));
        
        assertEquals("Tipo da conta é obrigatório", exception.getMessage());
    }

    @Test
    @DisplayName("Deve verificar estado do banco com sucesso")
    void deveVerificarEstadoBancoComSucesso() {
        when(repository.count()).thenReturn(2L);
        List<DadosConta> contas = List.of(contaTeste, CadastroTestDataBuilder.dadosConta().comId(2L).build());
        when(repository.findAll()).thenReturn(contas);

        assertDoesNotThrow(() -> service.verificarEstadoBanco());

        verify(repository).count();
        verify(repository).findAll();
    }

    @Test
    @DisplayName("Deve verificar estado do banco vazio com sucesso")
    void deveVerificarEstadoBancoVazioComSucesso() {
        when(repository.count()).thenReturn(0L);

        assertDoesNotThrow(() -> service.verificarEstadoBanco());

        verify(repository).count();
        verify(repository, never()).findAll();
    }
}
