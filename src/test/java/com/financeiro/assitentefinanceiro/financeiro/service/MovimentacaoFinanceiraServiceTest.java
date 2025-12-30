package com.financeiro.assitentefinanceiro.financeiro.service;

import com.financeiro.assitentefinanceiro.cadastro.domain.DadosConta;
import com.financeiro.assitentefinanceiro.cadastro.service.CadastroContaService;
import com.financeiro.assitentefinanceiro.financeiro.domain.MovimentacaoFinanceira;
import com.financeiro.assitentefinanceiro.financeiro.domain.dto.MovimentacaoFinanceiraDTO;
import com.financeiro.assitentefinanceiro.financeiro.domain.dto.RelatorioDadosDTO;
import com.financeiro.assitentefinanceiro.financeiro.domain.dto.RelatorioPDFParametrosDTO;
import com.financeiro.assitentefinanceiro.financeiro.enums.*;
import com.financeiro.assitentefinanceiro.financeiro.repository.MovimentacaoFinanceiraRepository;
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
@DisplayName("Testes unitários para MovimentacaoFinanceiraService")
class MovimentacaoFinanceiraServiceTest {

    @Mock
    private MovimentacaoFinanceiraRepository repository;

    @Mock
    private CadastroContaService contaService;

    @InjectMocks
    private MovimentacaoFinanceiraService service;

    private DadosConta contaTeste;
    private MovimentacaoFinanceiraDTO movimentacaoDTO;
    private MovimentacaoFinanceira movimentacao;

    @BeforeEach
    void setUp() {
        contaTeste = TestDataBuilder.dadosConta().build();
        movimentacaoDTO = TestDataBuilder.movimentacaoFinanceiraDTO().build();
        movimentacao = TestDataBuilder.movimentacaoFinanceira().comConta(contaTeste).build();
    }

    @Test
    @DisplayName("Deve registrar movimentação financeira com sucesso")
    void deveRegistrarMovimentacaoComSucesso() {
        when(contaService.buscarContaPorId(anyLong())).thenReturn(contaTeste);
        when(repository.sumValorByContaIdAndTipoMovimentacao(anyLong(), eq(TipoMovimentacao.RECEITA)))
                .thenReturn(Optional.of(5000.0));
        when(repository.sumValorByContaIdAndTipoMovimentacao(anyLong(), eq(TipoMovimentacao.DESPESA)))
                .thenReturn(Optional.of(0.0));
        when(repository.save(any(MovimentacaoFinanceira.class))).thenReturn(movimentacao);

        MovimentacaoFinanceira resultado = service.registrarMovimentacao(movimentacaoDTO);

        assertNotNull(resultado);
        assertEquals(movimentacao.getId(), resultado.getId());
        assertEquals(StatusMovimentacao.CONCLUIDA, resultado.getStatus());
        verify(contaService).buscarContaPorId(movimentacaoDTO.contaId());
        verify(repository).save(any(MovimentacaoFinanceira.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar registrar movimentação com dados inválidos")
    void deveLancarExcecaoComDadosInvalidos() {
        MovimentacaoFinanceiraDTO dtoInvalido = TestDataBuilder.movimentacaoFinanceiraDTO()
                .comValor(BigDecimal.ZERO)
                .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.registrarMovimentacao(dtoInvalido));

        assertEquals("Valor deve ser maior que zero", exception.getMessage());
        verify(contaService, never()).buscarContaPorId(anyLong());
        verify(repository, never()).save(any(MovimentacaoFinanceira.class));
    }

    @Test
    @DisplayName("Deve registrar movimentação com data futura com sucesso")
    void deveRegistrarMovimentacaoComDataFutura() {
        MovimentacaoFinanceiraDTO dtoComDataFutura = TestDataBuilder.movimentacaoFinanceiraDTO()
                .comDataMovimentacao(LocalDate.now().plusDays(2))
                .build();

        when(contaService.buscarContaPorId(anyLong())).thenReturn(contaTeste);
        when(repository.sumValorByContaIdAndTipoMovimentacao(anyLong(), eq(TipoMovimentacao.RECEITA)))
                .thenReturn(Optional.of(5000.0));
        when(repository.sumValorByContaIdAndTipoMovimentacao(anyLong(), eq(TipoMovimentacao.DESPESA)))
                .thenReturn(Optional.of(0.0));
        when(repository.save(any(MovimentacaoFinanceira.class))).thenReturn(movimentacao);

        MovimentacaoFinanceira resultado = service.registrarMovimentacao(dtoComDataFutura);

        assertNotNull(resultado);
        assertEquals(movimentacao.getId(), resultado.getId());
        verify(contaService).buscarContaPorId(dtoComDataFutura.contaId());
        verify(repository).save(any(MovimentacaoFinanceira.class));
    }

    @Test
    @DisplayName("Deve listar todas as movimentações com sucesso")
    void deveListarTodasMovimentacoesComSucesso() {
        List<MovimentacaoFinanceira> movimentacoes = List.of(movimentacao);
        when(repository.findAll()).thenReturn(movimentacoes);

        List<MovimentacaoFinanceira> resultado = service.listarMovimentacoes();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(movimentacao.getId(), resultado.get(0).getId());
        verify(repository).findAll();
    }

    @Test
    @DisplayName("Deve buscar movimentação por ID com sucesso")
    void deveBuscarMovimentacaoPorIdComSucesso() {
        when(repository.findById(anyLong())).thenReturn(Optional.of(movimentacao));

        MovimentacaoFinanceira resultado = service.buscarMovimentacaoPorId(1L);

        assertNotNull(resultado);
        assertEquals(movimentacao.getId(), resultado.getId());
        verify(repository).findById(1L);
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar movimentação inexistente")
    void deveLancarExcecaoAoBuscarMovimentacaoInexistente() {
        when(repository.findById(anyLong())).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.buscarMovimentacaoPorId(999L));

        assertEquals("Movimentação não encontrada com ID: 999", exception.getMessage());
    }

    @Test
    @DisplayName("Deve buscar movimentações por conta com sucesso")
    void deveBuscarMovimentacoesPorContaComSucesso() {
        List<MovimentacaoFinanceira> movimentacoes = List.of(movimentacao);
        when(contaService.buscarContaPorId(anyLong())).thenReturn(contaTeste);
        when(repository.findByContaId(anyLong())).thenReturn(movimentacoes);

        List<MovimentacaoFinanceira> resultado = service.buscarMovimentacoesPorConta(1L);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(contaService).buscarContaPorId(1L);
        verify(repository).findByContaId(1L);
    }

    @Test
    @DisplayName("Deve buscar movimentações por período com sucesso")
    void deveBuscarMovimentacoesPorPeriodoComSucesso() {
        LocalDate dataInicio = LocalDate.now().minusDays(30);
        LocalDate dataFim = LocalDate.now();
        List<MovimentacaoFinanceira> movimentacoes = List.of(movimentacao);

        when(contaService.buscarContaPorId(anyLong())).thenReturn(contaTeste);
        when(repository.findByContaIdAndPeriodo(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(movimentacoes);

        List<MovimentacaoFinanceira> resultado = service.buscarMovimentacoesPorPeriodo(1L, dataInicio, dataFim);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(repository).findByContaIdAndPeriodo(1L, dataInicio, dataFim);
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar movimentações com período inválido")
    void deveLancarExcecaoComPeriodoInvalido() {
        LocalDate dataInicio = LocalDate.now();
        LocalDate dataFim = LocalDate.now().minusDays(1);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.buscarMovimentacoesPorPeriodo(1L, dataInicio, dataFim));

        assertEquals("Data de início deve ser anterior à data de fim", exception.getMessage());
    }

    @Test
    @DisplayName("Deve buscar movimentações por tipo com sucesso")
    void deveBuscarMovimentacoesPorTipoComSucesso() {
        List<MovimentacaoFinanceira> movimentacoes = List.of(movimentacao);
        when(contaService.buscarContaPorId(anyLong())).thenReturn(contaTeste);
        when(repository.findByContaIdAndTipoMovimentacao(anyLong(), any(TipoMovimentacao.class)))
                .thenReturn(movimentacoes);

        List<MovimentacaoFinanceira> resultado = service.buscarMovimentacoesPorTipo(1L, TipoMovimentacao.RECEITA);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(repository).findByContaIdAndTipoMovimentacao(1L, TipoMovimentacao.RECEITA);
    }

    @Test
    @DisplayName("Deve atualizar movimentação com sucesso")
    void deveAtualizarMovimentacaoComSucesso() {
        MovimentacaoFinanceiraDTO dtoAtualizado = TestDataBuilder.movimentacaoFinanceiraDTO()
                .comId(1L)
                .comValor(new BigDecimal("1500.00"))
                .build();

        when(repository.findById(anyLong())).thenReturn(Optional.of(movimentacao));
        when(contaService.buscarContaPorId(anyLong())).thenReturn(contaTeste);
        when(repository.sumValorByContaIdAndTipoMovimentacao(anyLong(), eq(TipoMovimentacao.RECEITA)))
                .thenReturn(Optional.of(5000.0));
        when(repository.sumValorByContaIdAndTipoMovimentacao(anyLong(), eq(TipoMovimentacao.DESPESA)))
                .thenReturn(Optional.of(0.0));
        when(repository.save(any(MovimentacaoFinanceira.class))).thenReturn(movimentacao);

        MovimentacaoFinanceira resultado = service.atualizarMovimentacao(1L, dtoAtualizado);

        assertNotNull(resultado);
        verify(repository).findById(1L);
        verify(repository).save(any(MovimentacaoFinanceira.class));
    }

    @Test
    @DisplayName("Deve excluir movimentação com sucesso")
    void deveExcluirMovimentacaoComSucesso() {
        when(repository.existsById(anyLong())).thenReturn(true);

        assertDoesNotThrow(() -> service.excluirMovimentacao(1L));

        verify(repository).existsById(1L);
        verify(repository).deleteById(1L);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar excluir movimentação inexistente")
    void deveLancarExcecaoAoExcluirMovimentacaoInexistente() {
        when(repository.existsById(anyLong())).thenReturn(false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.excluirMovimentacao(999L));

        assertEquals("Movimentação não encontrada com ID: 999", exception.getMessage());
        verify(repository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Deve estornar movimentação com sucesso")
    void deveEstornarMovimentacaoComSucesso() {
        MovimentacaoFinanceira movimentacaoParaEstornar = TestDataBuilder.movimentacaoFinanceira()
                .comStatus(StatusMovimentacao.CONCLUIDA)
                .build();

        when(repository.findById(anyLong())).thenReturn(Optional.of(movimentacaoParaEstornar));
        when(repository.save(any(MovimentacaoFinanceira.class))).thenReturn(movimentacaoParaEstornar);

        MovimentacaoFinanceira resultado = service.estornarMovimentacao(1L);

        assertNotNull(resultado);
        assertEquals(StatusMovimentacao.ESTORNADA, resultado.getStatus());
        verify(repository).findById(1L);
        verify(repository).save(any(MovimentacaoFinanceira.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar estornar movimentação já estornada")
    void deveLancarExcecaoAoEstornarMovimentacaoJaEstornada() {
        MovimentacaoFinanceira movimentacaoEstornada = TestDataBuilder.movimentacaoFinanceira()
                .comStatus(StatusMovimentacao.ESTORNADA)
                .build();

        when(repository.findById(anyLong())).thenReturn(Optional.of(movimentacaoEstornada));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.estornarMovimentacao(1L));

        assertEquals("Movimentação já está estornada", exception.getMessage());
        verify(repository, never()).save(any(MovimentacaoFinanceira.class));
    }

    @Test
    @DisplayName("Deve calcular saldo atual com sucesso")
    void deveCalcularSaldoAtualComSucesso() {
        when(repository.sumValorByContaIdAndTipoMovimentacao(1L, TipoMovimentacao.RECEITA))
                .thenReturn(Optional.of(10000.0));
        when(repository.sumValorByContaIdAndTipoMovimentacao(1L, TipoMovimentacao.DESPESA))
                .thenReturn(Optional.of(3000.0));

        BigDecimal saldo = service.calcularSaldoAtual(1L);

        assertNotNull(saldo);
        assertEquals(new BigDecimal("7000.0"), saldo);
        verify(repository).sumValorByContaIdAndTipoMovimentacao(1L, TipoMovimentacao.RECEITA);
        verify(repository).sumValorByContaIdAndTipoMovimentacao(1L, TipoMovimentacao.DESPESA);
    }

    @Test
    @DisplayName("Deve calcular saldo zero quando não há movimentações")
    void deveCalcularSaldoZeroQuandoNaoHaMovimentacoes() {
        when(repository.sumValorByContaIdAndTipoMovimentacao(1L, TipoMovimentacao.RECEITA))
                .thenReturn(Optional.empty());
        when(repository.sumValorByContaIdAndTipoMovimentacao(1L, TipoMovimentacao.DESPESA))
                .thenReturn(Optional.empty());

        BigDecimal saldo = service.calcularSaldoAtual(1L);

        assertNotNull(saldo);
        assertEquals(BigDecimal.ZERO, saldo);
    }

    @Test
    @DisplayName("Deve converter entidade para DTO com sucesso")
    void deveConverterEntidadeParaDTOComSucesso() {
        MovimentacaoFinanceiraDTO dto = service.converterEntidadeParaDTO(movimentacao);

        assertNotNull(dto);
        assertEquals(movimentacao.getId(), dto.id());
        assertEquals(movimentacao.getTipoMovimentacao(), dto.tipoMovimentacao());
        assertEquals(movimentacao.getValor(), dto.valor());
        assertEquals(movimentacao.getDescricao(), dto.descricao());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar usar ID inválido")
    void deveLancarExcecaoComIdInvalido() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.buscarMovimentacaoPorId(null));

        assertEquals("ID inválido", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar usar ID zero")
    void deveLancarExcecaoComIdZero() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.buscarMovimentacaoPorId(0L));

        assertEquals("ID inválido", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar usar ID negativo")
    void deveLancarExcecaoComIdNegativo() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.buscarMovimentacaoPorId(-1L));

        assertEquals("ID inválido", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar registrar movimentação com categoria nula")
    void deveLancarExcecaoComCategoriaNula() {
        MovimentacaoFinanceiraDTO dtoComCategoriaNula = TestDataBuilder.movimentacaoFinanceiraDTO()
                .comCategoria(null)
                .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.registrarMovimentacao(dtoComCategoriaNula));

        assertEquals("Categoria e obrigatoria", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar registrar movimentação com fonte de movimentação nula")
    void deveLancarExcecaoComFonteMovimentacaoNula() {
        MovimentacaoFinanceiraDTO dtoComFonteNula = TestDataBuilder.movimentacaoFinanceiraDTO()
                .comFonteMovimentacao(null)
                .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.registrarMovimentacao(dtoComFonteNula));

        assertEquals("Fonte da movimentacao e obrigatoria", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar registrar movimentação com tipo de movimentação nulo")
    void deveLancarExcecaoComTipoMovimentacaoNulo() {
        MovimentacaoFinanceiraDTO dtoComTipoNulo = TestDataBuilder.movimentacaoFinanceiraDTO()
                .comTipoMovimentacao(null)
                .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.registrarMovimentacao(dtoComTipoNulo));

        assertEquals("Tipo de movimentação é obrigatório", exception.getMessage());
    }

    @Test
    @DisplayName("Deve buscar dados do relatório com sucesso")
    void deveBuscarDadosRelatorioComSucesso() {
        LocalDate dataInicio = LocalDate.now().minusMonths(1);
        LocalDate dataFim = LocalDate.now();
        RelatorioPDFParametrosDTO parametros = TestDataBuilder.relatorioPDFParametrosDTO()
                .comDataInicio(dataInicio)
                .comDataFim(dataFim)
                .build();
        List<MovimentacaoFinanceira> movimentacoes = List.of(movimentacao);

        when(contaService.buscarContaPorId(anyLong())).thenReturn(contaTeste);
        when(repository.findByContaIdAndPeriodo(anyLong(), any(), any())).thenReturn(movimentacoes);

        RelatorioDadosDTO resultado = service.buscarDadosRelatorio(parametros);

        assertNotNull(resultado);
        assertEquals(parametros.tituloRelatorio(), resultado.tituloRelatorio());
        assertEquals(contaTeste.getBanco(), resultado.conta().banco());
        assertEquals(contaTeste.getNumeroAgencia(), resultado.conta().numeroAgencia());
        assertEquals(contaTeste.getNumeroConta(), resultado.conta().numeroConta());
        assertEquals(contaTeste.getResponsavel(), resultado.conta().responsavel());
        assertEquals(1, resultado.movimentacoes().size());
        assertNotNull(resultado.totalReceitas());
        assertNotNull(resultado.totalDespesas());
        assertNotNull(resultado.saldoLiquido());
        assertNotNull(resultado.saldoAtual());
        verify(contaService).buscarContaPorId(parametros.contaId());
        verify(repository).findByContaIdAndPeriodo(parametros.contaId(), dataInicio, dataFim);
    }

    @Test
    @DisplayName("Deve buscar dados do relatório com filtros de período")
    void deveBuscarDadosRelatorioComFiltrosPeriodo() {
        LocalDate dataInicio = LocalDate.now().minusMonths(1);
        LocalDate dataFim = LocalDate.now();
        RelatorioPDFParametrosDTO parametros = TestDataBuilder.relatorioPDFParametrosDTO()
                .comDataInicio(dataInicio)
                .comDataFim(dataFim)
                .build();
        List<MovimentacaoFinanceira> movimentacoes = List.of(movimentacao);

        when(contaService.buscarContaPorId(anyLong())).thenReturn(contaTeste);
        when(repository.findByContaIdAndPeriodo(anyLong(), any(), any())).thenReturn(movimentacoes);

        RelatorioDadosDTO resultado = service.buscarDadosRelatorio(parametros);

        assertNotNull(resultado);
        assertEquals(dataInicio, resultado.dataInicio());
        assertEquals(dataFim, resultado.dataFim());
        verify(repository).findByContaIdAndPeriodo(parametros.contaId(), dataInicio, dataFim);
    }

    @Test
    @DisplayName("Deve buscar dados do relatório com filtro de tipo de movimentação")
    void deveBuscarDadosRelatorioComFiltroTipoMovimentacao() {
        RelatorioPDFParametrosDTO parametros = TestDataBuilder.relatorioPDFParametrosDTO()
                .comTipoMovimentacao(TipoMovimentacao.RECEITA)
                .comDataInicio(null)
                .comDataFim(null)
                .build();
        List<MovimentacaoFinanceira> movimentacoes = List.of(movimentacao);

        when(contaService.buscarContaPorId(anyLong())).thenReturn(contaTeste);
        when(repository.findByContaIdAndTipoMovimentacao(anyLong(), any())).thenReturn(movimentacoes);

        RelatorioDadosDTO resultado = service.buscarDadosRelatorio(parametros);

        assertNotNull(resultado);
        assertEquals(TipoMovimentacao.RECEITA.toString(), resultado.tipoMovimentacao());
        verify(repository).findByContaIdAndTipoMovimentacao(parametros.contaId(), TipoMovimentacao.RECEITA);
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar dados com conta inválida")
    void deveLancarExcecaoAoBuscarDadosComContaInvalida() {
        RelatorioPDFParametrosDTO parametros = TestDataBuilder.relatorioPDFParametrosDTO()
                .comContaId(999L)
                .build();

        when(contaService.buscarContaPorId(anyLong())).thenThrow(new IllegalArgumentException("Conta não encontrada"));

        assertThrows(IllegalArgumentException.class, () -> service.buscarDadosRelatorio(parametros));
        verify(contaService).buscarContaPorId(parametros.contaId());
    }
}
