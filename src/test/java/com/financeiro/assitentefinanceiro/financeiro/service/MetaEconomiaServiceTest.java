package com.financeiro.assitentefinanceiro.financeiro.service;

import com.financeiro.assitentefinanceiro.ai.service.AssistenteFinanceiroService;
import com.financeiro.assitentefinanceiro.cadastro.domain.DadosConta;
import com.financeiro.assitentefinanceiro.cadastro.service.CadastroContaService;
import com.financeiro.assitentefinanceiro.financeiro.domain.MetaEconomia;
import com.financeiro.assitentefinanceiro.financeiro.domain.dto.MetaEconomiaDTO;
import com.financeiro.assitentefinanceiro.financeiro.enums.*;
import com.financeiro.assitentefinanceiro.financeiro.repository.MetaEconomiaRepository;
import com.financeiro.assitentefinanceiro.financeiro.service.testdata.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes unitários para MetaEconomiaService")
class MetaEconomiaServiceTest {

    @Mock
    private MetaEconomiaRepository repository;

    @Mock
    private CadastroContaService contaService;

    @Mock
    private AssistenteFinanceiroService assistenteService;

    @InjectMocks
    private MetaEconomiaService service;

    private DadosConta contaTeste;
    private MetaEconomiaDTO metaDTO;
    private MetaEconomia meta;

    @BeforeEach
    void setUp() {
        contaTeste = TestDataBuilder.dadosConta().build();
        metaDTO = TestDataBuilder.metaEconomiaDTO().build();
        meta = TestDataBuilder.metaEconomia().comConta(contaTeste).build();
    }

    @Test
    @DisplayName("Deve criar meta de economia com sucesso")
    void deveCriarMetaComSucesso() {
        when(contaService.buscarContaPorId(anyLong())).thenReturn(contaTeste);
        when(repository.save(any(MetaEconomia.class))).thenReturn(meta);

        MetaEconomia resultado = service.criarMeta(metaDTO);

        assertNotNull(resultado);
        assertEquals(meta.getId(), resultado.getId());
        assertEquals(meta.getNome(), resultado.getNome());
        assertEquals(StatusMeta.ATIVA, resultado.getStatus());
        verify(contaService).buscarContaPorId(metaDTO.contaId());
        verify(repository).save(any(MetaEconomia.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar criar meta com dados inválidos")
    void deveLancarExcecaoComDadosInvalidos() {
        MetaEconomiaDTO dtoInvalido = TestDataBuilder.metaEconomiaDTO()
            .comValorMeta(BigDecimal.ZERO)
            .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> service.criarMeta(dtoInvalido));
        
        assertEquals("Valor da meta deve ser maior que zero", exception.getMessage());
        verify(contaService, never()).buscarContaPorId(anyLong());
        verify(repository, never()).save(any(MetaEconomia.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar criar meta com data de início futura")
    void deveLancarExcecaoComDataInicioFutura() {
        MetaEconomiaDTO dtoComDataInvalida = TestDataBuilder.metaEconomiaDTO()
            .comDataInicio(LocalDate.now().plusDays(1))
            .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> service.criarMeta(dtoComDataInvalida));
        
        assertEquals("Data de inicio nao pode ser futura", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar criar meta com data de fim anterior à data de início")
    void deveLancarExcecaoComDataFimAnterior() {
        MetaEconomiaDTO dtoComDataFimInvalida = TestDataBuilder.metaEconomiaDTO()
            .comDataFim(LocalDate.now().minusDays(1))
            .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> service.criarMeta(dtoComDataFimInvalida));
        
        assertEquals("Data de inicio deve ser anterior a data de fim", exception.getMessage());
    }

    @Test
    @DisplayName("Deve listar todas as metas com sucesso")
    void deveListarTodasMetasComSucesso() {
        List<MetaEconomia> metas = List.of(meta);
        when(repository.findAll()).thenReturn(metas);

        List<MetaEconomia> resultado = service.listarMetas();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(meta.getId(), resultado.get(0).getId());
        verify(repository).findAll();
    }

    @Test
    @DisplayName("Deve buscar meta por ID com sucesso")
    void deveBuscarMetaPorIdComSucesso() {
        when(repository.findById(anyLong())).thenReturn(Optional.of(meta));

        MetaEconomia resultado = service.buscarMetaPorId(1L);

        assertNotNull(resultado);
        assertEquals(meta.getId(), resultado.getId());
        verify(repository).findById(1L);
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar meta inexistente")
    void deveLancarExcecaoAoBuscarMetaInexistente() {
        when(repository.findById(anyLong())).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> service.buscarMetaPorId(999L));
        
        assertEquals("Meta nao encontrada com ID: 999", exception.getMessage());
    }

    @Test
    @DisplayName("Deve buscar metas por conta com sucesso")
    void deveBuscarMetasPorContaComSucesso() {
        List<MetaEconomia> metas = List.of(meta);
        when(contaService.buscarContaPorId(anyLong())).thenReturn(contaTeste);
        when(repository.findByContaId(anyLong())).thenReturn(metas);

        List<MetaEconomia> resultado = service.buscarMetasPorConta(1L);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(contaService).buscarContaPorId(1L);
        verify(repository).findByContaId(1L);
    }

    @Test
    @DisplayName("Deve buscar metas ativas por conta com sucesso")
    void deveBuscarMetasAtivasPorContaComSucesso() {
        List<MetaEconomia> metas = List.of(meta);
        when(contaService.buscarContaPorId(anyLong())).thenReturn(contaTeste);
        when(repository.findMetasAtivasByContaId(anyLong())).thenReturn(metas);

        List<MetaEconomia> resultado = service.buscarMetasAtivasPorConta(1L);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(repository).findMetasAtivasByContaId(1L);
    }

    @Test
    @DisplayName("Deve buscar metas vencidas por conta com sucesso")
    void deveBuscarMetasVencidasPorContaComSucesso() {
        MetaEconomia metaVencida = TestDataBuilder.metaEconomia()
            .comDataFim(LocalDate.now().minusDays(1))
            .comStatus(StatusMeta.ATIVA)
            .build();
        
        List<MetaEconomia> metas = List.of(metaVencida);
        when(contaService.buscarContaPorId(anyLong())).thenReturn(contaTeste);
        when(repository.findMetasVencidasByContaId(anyLong(), any(LocalDate.class))).thenReturn(metas);

        List<MetaEconomia> resultado = service.buscarMetasVencidasPorConta(1L);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(repository).findMetasVencidasByContaId(1L, LocalDate.now());
    }

    @Test
    @DisplayName("Deve buscar metas por tipo com sucesso")
    void deveBuscarMetasPorTipoComSucesso() {
        List<MetaEconomia> metas = List.of(meta);
        when(contaService.buscarContaPorId(anyLong())).thenReturn(contaTeste);
        when(repository.findByContaIdAndTipoMeta(anyLong(), any(TipoMeta.class))).thenReturn(metas);

        List<MetaEconomia> resultado = service.buscarMetasPorTipo(1L, TipoMeta.ECONOMIA_MENSAL);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(repository).findByContaIdAndTipoMeta(1L, TipoMeta.ECONOMIA_MENSAL);
    }

    @Test
    @DisplayName("Deve atualizar meta com sucesso")
    void deveAtualizarMetaComSucesso() {
        MetaEconomiaDTO dtoAtualizado = TestDataBuilder.metaEconomiaDTO()
            .comId(1L)
            .comNome("Meta Atualizada")
            .comValorMeta(new BigDecimal("10000.00"))
            .build();
        
        when(repository.findById(anyLong())).thenReturn(Optional.of(meta));
        when(repository.save(any(MetaEconomia.class))).thenReturn(meta);

        MetaEconomia resultado = service.atualizarMeta(1L, dtoAtualizado);

        assertNotNull(resultado);
        verify(repository).findById(1L);
        verify(repository).save(any(MetaEconomia.class));
        verify(contaService, never()).buscarContaPorId(anyLong());
    }

    @Test
    @DisplayName("Deve atualizar progresso da meta com sucesso")
    void deveAtualizarProgressoMetaComSucesso() {
        BigDecimal valorAdicionado = new BigDecimal("1000.00");
        MetaEconomia metaComProgresso = TestDataBuilder.metaEconomia()
            .comValorAtual(new BigDecimal("2000.00"))
            .comPercentualConcluido(new BigDecimal("40.00"))
            .build();
        
        when(repository.findById(anyLong())).thenReturn(Optional.of(metaComProgresso));
        when(repository.save(any(MetaEconomia.class))).thenReturn(metaComProgresso);

        MetaEconomia resultado = service.atualizarProgressoMeta(1L, valorAdicionado);

        assertNotNull(resultado);
        verify(repository).findById(1L);
        verify(repository).save(any(MetaEconomia.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar atualizar progresso de meta concluída")
    void deveLancarExcecaoAoAtualizarProgressoMetaConcluida() {
        MetaEconomia metaConcluida = TestDataBuilder.metaEconomia()
            .comStatus(StatusMeta.CONCLUIDA)
            .build();
        
        when(repository.findById(anyLong())).thenReturn(Optional.of(metaConcluida));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> service.atualizarProgressoMeta(1L, new BigDecimal("1000.00")));
        
        assertEquals("Meta ja esta concluida", exception.getMessage());
        verify(repository, never()).save(any(MetaEconomia.class));
    }

    @Test
    @DisplayName("Deve excluir meta com sucesso")
    void deveExcluirMetaComSucesso() {
        when(repository.existsById(anyLong())).thenReturn(true);

        assertDoesNotThrow(() -> service.excluirMeta(1L));

        verify(repository).existsById(1L);
        verify(repository).deleteById(1L);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar excluir meta inexistente")
    void deveLancarExcecaoAoExcluirMetaInexistente() {
        when(repository.existsById(anyLong())).thenReturn(false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> service.excluirMeta(999L));
        
        assertEquals("Meta nao encontrada com ID: 999", exception.getMessage());
        verify(repository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Deve pausar meta com sucesso")
    void devePausarMetaComSucesso() {
        MetaEconomia metaAtiva = TestDataBuilder.metaEconomia()
            .comStatus(StatusMeta.ATIVA)
            .build();
        
        when(repository.findById(anyLong())).thenReturn(Optional.of(metaAtiva));
        when(repository.save(any(MetaEconomia.class))).thenReturn(metaAtiva);

        MetaEconomia resultado = service.pausarMeta(1L);

        assertNotNull(resultado);
        assertEquals(StatusMeta.PAUSADA, resultado.getStatus());
        verify(repository).findById(1L);
        verify(repository).save(any(MetaEconomia.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar pausar meta já pausada")
    void deveLancarExcecaoAoPausarMetaJaPausada() {
        MetaEconomia metaPausada = TestDataBuilder.metaEconomia()
            .comStatus(StatusMeta.PAUSADA)
            .build();
        
        when(repository.findById(anyLong())).thenReturn(Optional.of(metaPausada));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> service.pausarMeta(1L));
        
        assertEquals("Meta ja esta pausada", exception.getMessage());
        verify(repository, never()).save(any(MetaEconomia.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar pausar meta concluída")
    void deveLancarExcecaoAoPausarMetaConcluida() {
        MetaEconomia metaConcluida = TestDataBuilder.metaEconomia()
            .comStatus(StatusMeta.CONCLUIDA)
            .build();
        
        when(repository.findById(anyLong())).thenReturn(Optional.of(metaConcluida));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> service.pausarMeta(1L));
        
        assertEquals("Nao e possivel pausar meta concluida", exception.getMessage());
        verify(repository, never()).save(any(MetaEconomia.class));
    }

    @Test
    @DisplayName("Deve reativar meta com sucesso")
    void deveReativarMetaComSucesso() {
        MetaEconomia metaPausada = TestDataBuilder.metaEconomia()
            .comStatus(StatusMeta.PAUSADA)
            .build();
        
        when(repository.findById(anyLong())).thenReturn(Optional.of(metaPausada));
        when(repository.save(any(MetaEconomia.class))).thenReturn(metaPausada);

        MetaEconomia resultado = service.reativarMeta(1L);

        assertNotNull(resultado);
        assertEquals(StatusMeta.ATIVA, resultado.getStatus());
        verify(repository).findById(1L);
        verify(repository).save(any(MetaEconomia.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar reativar meta não pausada")
    void deveLancarExcecaoAoReativarMetaNaoPausada() {
        MetaEconomia metaAtiva = TestDataBuilder.metaEconomia()
            .comStatus(StatusMeta.ATIVA)
            .build();
        
        when(repository.findById(anyLong())).thenReturn(Optional.of(metaAtiva));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> service.reativarMeta(1L));
        
        assertEquals("Meta nao esta pausada. Status atual: ATIVA. Apenas metas pausadas podem ser reativadas.", exception.getMessage());
        verify(repository, never()).save(any(MetaEconomia.class));
    }

    @Test
    @DisplayName("Deve reativar meta vencida com sucesso")
    void deveReativarMetaVencidaComSucesso() {
        MetaEconomia metaVencida = TestDataBuilder.metaEconomia()
            .comStatus(StatusMeta.PAUSADA)
            .comDataFim(LocalDate.now().minusDays(1))
            .build();
        
        when(repository.findById(anyLong())).thenReturn(Optional.of(metaVencida));
        when(repository.save(any(MetaEconomia.class))).thenReturn(metaVencida);

        MetaEconomia resultado = service.reativarMeta(1L);

        assertNotNull(resultado);
        assertEquals(StatusMeta.ATIVA, resultado.getStatus());
        verify(repository).findById(1L);
        verify(repository).save(any(MetaEconomia.class));
    }

    @Test
    @DisplayName("Deve converter entidade para DTO com sucesso")
    void deveConverterEntidadeParaDTOComSucesso() {
        MetaEconomiaDTO dto = service.converterEntidadeParaDTO(meta);

        assertNotNull(dto);
        assertEquals(meta.getId(), dto.id());
        assertEquals(meta.getNome(), dto.nome());
        assertEquals(meta.getTipoMeta(), dto.tipoMeta());
        assertEquals(meta.getValorMeta(), dto.valorMeta());
    }

    @Test
    @DisplayName("Deve verificar metas vencidas com sucesso")
    void deveVerificarMetasVencidasComSucesso() {
        MetaEconomia metaVencida = TestDataBuilder.metaEconomia()
            .comDataFim(LocalDate.now().minusDays(1))
            .comStatus(StatusMeta.ATIVA)
            .build();
        
        List<MetaEconomia> todasMetas = List.of(metaVencida);
        when(repository.findAll()).thenReturn(todasMetas);
        when(repository.save(any(MetaEconomia.class))).thenReturn(metaVencida);

        int metasVencidas = service.verificarMetasVencidas();

        assertEquals(1, metasVencidas);
        verify(repository).findAll();
        verify(repository).save(any(MetaEconomia.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar usar ID inválido")
    void deveLancarExcecaoComIdInvalido() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> service.buscarMetaPorId(null));
        
        assertEquals("ID inválido", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar usar ID zero")
    void deveLancarExcecaoComIdZero() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> service.buscarMetaPorId(0L));
        
        assertEquals("ID inválido", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar usar ID negativo")
    void deveLancarExcecaoComIdNegativo() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> service.buscarMetaPorId(-1L));
        
        assertEquals("ID inválido", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar usar valor de progresso inválido")
    void deveLancarExcecaoComValorProgressoInvalido() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> service.atualizarProgressoMeta(1L, BigDecimal.ZERO));
        
        assertEquals("Valor do progresso deve ser maior que zero", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar usar tipo de meta nulo")
    void deveLancarExcecaoComTipoMetaNulo() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> service.buscarMetasPorTipo(1L, null));
        
        assertEquals("Tipo de meta e obrigatorio", exception.getMessage());
    }

}
