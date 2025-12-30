package com.financeiro.assitentefinanceiro.ai.service;

import com.financeiro.assitentefinanceiro.financeiro.domain.MetaEconomia;
import com.financeiro.assitentefinanceiro.financeiro.domain.dto.MetaEconomiaDTO;
import com.financeiro.assitentefinanceiro.financeiro.service.MetaEconomiaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AssistenteFinanceiroService {

    private static final Logger logger = LoggerFactory.getLogger(AssistenteFinanceiroService.class);

    private final MetaEconomiaService metaEconomiaService;

    public AssistenteFinanceiroService(MetaEconomiaService metaEconomiaService) {
        this.metaEconomiaService = metaEconomiaService;
    }

    public String gerarPlanoAcao(Long metaId) {
        logger.info("Gerando plano de ação para meta ID: {}", metaId);

        try {
            MetaEconomia meta = metaEconomiaService.buscarMetaPorId(metaId);
            Map<String, Object> dadosFinanceiros = coletarDadosFinanceiros(meta.getConta().getId());
            BigDecimal economiaMensal = calcularEconomiaMensal(meta);
            BigDecimal capacidadeAtual = calcularCapacidadeAtual(dadosFinanceiros);
            return gerarPlanoComIA(meta, economiaMensal, capacidadeAtual, dadosFinanceiros);
        } catch (Exception e) {
            logger.error("Erro ao gerar plano de ação para meta {}: {}", metaId, e.getMessage());
            throw new RuntimeException("Erro ao gerar plano de ação: " + e.getMessage());
        }
    }

    public String analisarViabilidadeMeta(MetaEconomiaDTO metaDTO) {
        logger.info("Analisando viabilidade da meta: {}", metaDTO.nome());

        try {
            BigDecimal economiaMensal = calcularEconomiaMensal(metaDTO);
            Map<String, Object> dadosFinanceiros = coletarDadosFinanceiros(metaDTO.contaId());
            BigDecimal capacidadeAtual = calcularCapacidadeAtual(dadosFinanceiros);
            return analisarViabilidadeComIA(metaDTO, economiaMensal, capacidadeAtual, dadosFinanceiros);
        } catch (Exception e) {
            logger.error("Erro ao analisar viabilidade da meta: {}", e.getMessage());
            throw new RuntimeException("Erro ao analisar viabilidade: " + e.getMessage());
        }
    }

    public String sugerirOtimizacoes(Long contaId) {
        logger.info("Gerando sugestões de otimização para conta ID: {}", contaId);

        try {
            List<MetaEconomia> metas = metaEconomiaService.buscarMetasAtivasPorConta(contaId);
            Map<String, Object> dadosFinanceiros = coletarDadosFinanceiros(contaId);

            return gerarSugestoesComIA(metas, dadosFinanceiros);

        } catch (Exception e) {
            logger.error("Erro ao gerar sugestões de otimização: {}", e.getMessage());
            throw new RuntimeException("Erro ao gerar sugestões: " + e.getMessage());
        }
    }

    private BigDecimal calcularEconomiaMensal(MetaEconomia meta) {
        long mesesRestantes = java.time.temporal.ChronoUnit.MONTHS.between(LocalDate.now(), meta.getDataFim());

        if (mesesRestantes <= 0) {
            return meta.getValorMeta().subtract(meta.getValorAtual());
        }

        return meta.getValorMeta().subtract(meta.getValorAtual()).divide(BigDecimal.valueOf(mesesRestantes), 2, RoundingMode.UP);
    }

    private BigDecimal calcularEconomiaMensal(MetaEconomiaDTO metaDTO) {
        long mesesRestantes = java.time.temporal.ChronoUnit.MONTHS.between(LocalDate.now(), metaDTO.dataFim());

        if (mesesRestantes <= 0) {
            return metaDTO.valorMeta().subtract(metaDTO.valorAtual() != null ? metaDTO.valorAtual() : BigDecimal.ZERO);
        }

        return metaDTO.valorMeta().subtract(metaDTO.valorAtual() != null ? metaDTO.valorAtual() : BigDecimal.ZERO).divide(BigDecimal.valueOf(mesesRestantes), 2, RoundingMode.UP);
    }

    private Map<String, Object> coletarDadosFinanceiros(Long contaId) {
        Map<String, Object> dados = new HashMap<>();

        try {
            dados.put("receitasMensais", BigDecimal.valueOf(5000));
            dados.put("despesasMensais", BigDecimal.valueOf(3500));
            dados.put("saldoAtual", BigDecimal.valueOf(2000));
            dados.put("periodoAnalise", "3 meses");

            logger.debug("Dados financeiros coletados para conta {}: {}", contaId, dados);

        } catch (Exception e) {
            logger.warn("Erro ao coletar dados financeiros: {}", e.getMessage());
            dados.put("receitasMensais", BigDecimal.valueOf(5000));
            dados.put("despesasMensais", BigDecimal.valueOf(3500));
            dados.put("saldoAtual", BigDecimal.valueOf(2000));
            dados.put("periodoAnalise", "3 meses");
        }

        return dados;
    }

    private BigDecimal calcularCapacidadeAtual(Map<String, Object> dadosFinanceiros) {
        BigDecimal receitas = (BigDecimal) dadosFinanceiros.get("receitasMensais");
        BigDecimal despesas = (BigDecimal) dadosFinanceiros.get("despesasMensais");

        return receitas.subtract(despesas);
    }

    private String gerarPlanoComIA(MetaEconomia meta, BigDecimal economiaMensal, BigDecimal capacidadeAtual, Map<String, Object> dadosFinanceiros) {
        return String.format("""
            # Plano de Ação para Meta: %s
            
            ## Análise da Meta
            - **Valor da Meta:** R$ %.2f
            - **Valor Atual:** R$ %.2f
            - **Economia Mensal Necessária:** R$ %.2f
            - **Data Limite:** %s
            
            ## Situação Financeira Atual
            - **Receitas Mensais:** R$ %.2f
            - **Despesas Mensais:** R$ %.2f
            - **Capacidade de Economia:** R$ %.2f
            - **Saldo Atual:** R$ %.2f
            
            ## Recomendações
            1. **Corte de Despesas:** Identifique gastos desnecessários
            2. **Realocação de Recursos:** Otimize o orçamento mensal
            3. **Investimentos:** Considere aplicações de curto prazo
            4. **Cronograma:** Acompanhe o progresso mensalmente
            
            ## Próximos Passos
            - Revisar orçamento mensal
            - Estabelecer metas intermediárias
            - Monitorar progresso semanalmente
            """,
            meta.getNome(),
            meta.getValorMeta(),
            meta.getValorAtual(),
            economiaMensal,
            meta.getDataFim().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
            (BigDecimal) dadosFinanceiros.get("receitasMensais"),
            (BigDecimal) dadosFinanceiros.get("despesasMensais"),
            capacidadeAtual,
            (BigDecimal) dadosFinanceiros.get("saldoAtual")
        );
    }

    private String analisarViabilidadeComIA(MetaEconomiaDTO metaDTO, BigDecimal economiaMensal, BigDecimal capacidadeAtual, Map<String, Object> dadosFinanceiros) {
        return String.format("""
            # Análise de Viabilidade: %s
            
            ## Dados da Meta
            - **Valor:** R$ %.2f
            - **Economia Mensal Necessária:** R$ %.2f
            - **Período:** %s a %s
            
            ## Situação Financeira
            - **Receitas Mensais:** R$ %.2f
            - **Despesas Mensais:** R$ %.2f
            - **Capacidade de Economia:** R$ %.2f
            
            ## Avaliação
            - **Viabilidade:** %s
            - **Comprometimento da Renda:** %.1f%%
            - **Recomendação:** %s
            
            ## Observações
            %s
            """,
            metaDTO.nome(),
            metaDTO.valorMeta(),
            economiaMensal,
            metaDTO.dataInicio().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
            metaDTO.dataFim().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
            (BigDecimal) dadosFinanceiros.get("receitasMensais"),
            (BigDecimal) dadosFinanceiros.get("despesasMensais"),
            capacidadeAtual,
            economiaMensal.compareTo(capacidadeAtual) <= 0 ? "VIÁVEL" : "NÃO VIÁVEL",
            economiaMensal.divide((BigDecimal) dadosFinanceiros.get("receitasMensais"), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)),
            economiaMensal.compareTo(capacidadeAtual) <= 0 ? "Meta pode ser alcançada com disciplina" : "Ajuste necessário no orçamento",
            economiaMensal.compareTo(capacidadeAtual) <= 0 ? 
                "Continue focado e mantenha a disciplina financeira." : 
                "Considere reduzir o valor da meta ou aumentar a capacidade de economia."
        );
    }

    private String gerarSugestoesComIA(List<MetaEconomia> metas, Map<String, Object> dadosFinanceiros) {
        StringBuilder metasInfo = new StringBuilder();
        for (MetaEconomia meta : metas) {
            metasInfo.append(String.format("- %s: R$ %.2f / R$ %.2f (%.1f%%)\n", 
                meta.getNome(), meta.getValorAtual(), meta.getValorMeta(), meta.getPercentualConcluido()));
        }

        return String.format("""
            # Sugestões de Otimização Financeira
            
            ## Metas Ativas
            %s
            
            ## Situação Financeira
            - **Receitas Mensais:** R$ %.2f
            - **Despesas Mensais:** R$ %.2f
            - **Saldo Atual:** R$ %.2f
            
            ## Estratégias Recomendadas
            1. **Priorização:** Foque nas metas com maior retorno
            2. **Economia:** Reduza gastos desnecessários
            3. **Investimentos:** Considere aplicações de renda fixa
            4. **Orçamento:** Revise mensalmente as despesas
            5. **Automação:** Configure transferências automáticas
            
            ## Plano de Ação Mensal
            - Semana 1: Revisar orçamento
            - Semana 2: Ajustar gastos
            - Semana 3: Aplicar economias
            - Semana 4: Avaliar progresso
            
            ## Dicas Práticas
            - Use a regra 50/30/20 (necessidades/desejos/poupança)
            - Evite compras por impulso
            - Compare preços antes de comprar
            - Aproveite promoções e descontos
            """,
            metasInfo.toString(),
            (BigDecimal) dadosFinanceiros.get("receitasMensais"),
            (BigDecimal) dadosFinanceiros.get("despesasMensais"),
            (BigDecimal) dadosFinanceiros.get("saldoAtual")
        );
    }
}
