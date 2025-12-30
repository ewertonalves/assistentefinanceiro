package com.financeiro.assitentefinanceiro.ai.service;

import com.financeiro.assitentefinanceiro.cadastro.domain.DadosConta;
import com.financeiro.assitentefinanceiro.cadastro.service.CadastroContaService;
import com.financeiro.assitentefinanceiro.financeiro.domain.MetaEconomia;
import com.financeiro.assitentefinanceiro.financeiro.domain.MovimentacaoFinanceira;
import com.financeiro.assitentefinanceiro.financeiro.enums.StatusMovimentacao;
import com.financeiro.assitentefinanceiro.financeiro.enums.TipoMovimentacao;
import com.financeiro.assitentefinanceiro.financeiro.service.MetaEconomiaService;
import com.financeiro.assitentefinanceiro.financeiro.service.MovimentacaoFinanceiraService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class IADinamicaService {

    private static final Logger logger = LoggerFactory.getLogger(IADinamicaService.class);

    private final MetaEconomiaService metaEconomiaService;
    private final MovimentacaoFinanceiraService movimentacaoFinanceiraService;
    private final CadastroContaService cadastroContaService;
    private final ChatClient.Builder chatClientBuilder;

    public IADinamicaService(
            MetaEconomiaService metaEconomiaService,
            MovimentacaoFinanceiraService movimentacaoFinanceiraService,
            CadastroContaService cadastroContaService,
            ChatClient.Builder chatClientBuilder) {
        this.metaEconomiaService = metaEconomiaService;
        this.movimentacaoFinanceiraService = movimentacaoFinanceiraService;
        this.cadastroContaService = cadastroContaService;
        this.chatClientBuilder = chatClientBuilder;
    }

    public String responderPromptDinamico(String prompt, Long contaId) {
        logger.info("Processando prompt dinâmico: '{}' para conta: {}", prompt, contaId);

        try {
            Map<String, Object> contextoFinanceiro = coletarContextoFinanceiro(contaId);
            return gerarRespostaComIA(prompt, contextoFinanceiro, null);

        } catch (Exception e) {
            logger.error("Erro ao processar prompt dinâmico: {}", e.getMessage(), e);
            return "Desculpe, ocorreu um erro ao processar sua solicitação. Tente novamente.";
        }
    }

    public String responderPromptGenerico(String prompt) {
        logger.info("Processando prompt genérico: '{}'", prompt);

        try {
            Map<String, Object> contextoVazio = new HashMap<>();
            contextoVazio.put("temConta", false);
            return gerarRespostaComIA(prompt, contextoVazio, null);
        } catch (Exception e) {
            logger.error("Erro ao processar prompt genérico: {}", e.getMessage(), e);
            return "Desculpe, não consegui processar sua solicitação no momento. Tente novamente.";
        }
    }

    public String manterConversacao(String prompt, List<String> historicoConversacao, Long contaId) {
        logger.info("Mantendo conversação com {} mensagens anteriores", 
            historicoConversacao != null ? historicoConversacao.size() : 0);

        try {
            Map<String, Object> contextoFinanceiro = coletarContextoFinanceiro(contaId);
            return gerarRespostaComIA(prompt, contextoFinanceiro, historicoConversacao);
        } catch (Exception e) {
            logger.error("Erro ao processar conversação: {}", e.getMessage(), e);
            return "Desculpe, não consegui manter a conversação no momento. Tente novamente.";
        }
    }

    private Map<String, Object> coletarContextoFinanceiro(Long contaId) {
        Map<String, Object> contexto = new HashMap<>();

        if (contaId != null) {
            try {
                DadosConta conta = cadastroContaService.buscarContaPorId(contaId);
                contexto.put("conta", String.format("%s - Ag: %s, Conta: %s, Responsável: %s", 
                    conta.getBanco(), conta.getNumeroAgencia(), conta.getNumeroConta(), conta.getResponsavel()));

                List<MetaEconomia> metasAtivas = metaEconomiaService.buscarMetasAtivasPorConta(contaId);
                BigDecimal totalMetas = metasAtivas.stream()
                    .map(MetaEconomia::getValorMeta)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal totalAtual = metasAtivas.stream()
                    .map(MetaEconomia::getValorAtual)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                contexto.put("totalMetas", metasAtivas.size());
                contexto.put("valorTotalMetas", totalMetas);
                contexto.put("valorAtualMetas", totalAtual);
                
                if (!metasAtivas.isEmpty()) {
                    String detalhesMetas = metasAtivas.stream()
                        .map(meta -> {
                            BigDecimal percentual = meta.getPercentualConcluido() != null 
                                ? meta.getPercentualConcluido() 
                                : BigDecimal.ZERO;
                            return String.format("%s: R$ %.2f de R$ %.2f (%.2f%%, %s)", 
                                meta.getNome(), 
                                meta.getValorAtual(), 
                                meta.getValorMeta(),
                                percentual.doubleValue(),
                                meta.getStatus());
                        })
                        .collect(Collectors.joining("; "));
                    contexto.put("detalhesMetas", detalhesMetas);
                } else {
                    contexto.put("detalhesMetas", "Nenhuma meta cadastrada");
                }

                List<MovimentacaoFinanceira> movimentacoes = movimentacaoFinanceiraService.buscarMovimentacoesPorConta(contaId);
                List<MovimentacaoFinanceira> movimentacoesConcluidas = movimentacoes.stream()
                    .filter(m -> StatusMovimentacao.CONCLUIDA.equals(m.getStatus()))
                    .collect(Collectors.toList());

                LocalDate inicioMes = LocalDate.now().withDayOfMonth(1);
                LocalDate fimMes = LocalDate.now();
                
                BigDecimal receitasMes = movimentacoesConcluidas.stream()
                    .filter(m -> TipoMovimentacao.RECEITA.equals(m.getTipoMovimentacao()))
                    .filter(m -> !m.getDataMovimentacao().isBefore(inicioMes) && !m.getDataMovimentacao().isAfter(fimMes))
                    .map(MovimentacaoFinanceira::getValor)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal despesasMes = movimentacoesConcluidas.stream()
                    .filter(m -> TipoMovimentacao.DESPESA.equals(m.getTipoMovimentacao()))
                    .filter(m -> !m.getDataMovimentacao().isBefore(inicioMes) && !m.getDataMovimentacao().isAfter(fimMes))
                    .map(MovimentacaoFinanceira::getValor)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal totalReceitas = movimentacoesConcluidas.stream()
                    .filter(m -> TipoMovimentacao.RECEITA.equals(m.getTipoMovimentacao()))
                    .map(MovimentacaoFinanceira::getValor)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal totalDespesas = movimentacoesConcluidas.stream()
                    .filter(m -> TipoMovimentacao.DESPESA.equals(m.getTipoMovimentacao()))
                    .map(MovimentacaoFinanceira::getValor)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal saldoAtual = movimentacaoFinanceiraService.calcularSaldoAtual(contaId);
                BigDecimal capacidadeEconomia = receitasMes.subtract(despesasMes);

                Map<String, BigDecimal> despesasPorCategoria = movimentacoesConcluidas.stream()
                    .filter(m -> TipoMovimentacao.DESPESA.equals(m.getTipoMovimentacao()))
                    .collect(Collectors.groupingBy(
                        m -> m.getCategoria().toString(),
                        Collectors.reducing(BigDecimal.ZERO, MovimentacaoFinanceira::getValor, BigDecimal::add)
                    ));

                String topCategorias = despesasPorCategoria.entrySet().stream()
                    .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                    .limit(5)
                    .map(e -> String.format("%s: R$ %.2f", e.getKey(), e.getValue()))
                    .collect(Collectors.joining("; "));

                contexto.put("temConta", true);
                contexto.put("contaId", contaId);
                contexto.put("receitasMes", receitasMes);
                contexto.put("despesasMes", despesasMes);
                contexto.put("totalReceitas", totalReceitas);
                contexto.put("totalDespesas", totalDespesas);
                contexto.put("saldoAtual", saldoAtual);
                contexto.put("capacidadeEconomia", capacidadeEconomia);
                contexto.put("totalMovimentacoes", movimentacoes.size());
                contexto.put("topCategoriasDespesas", topCategorias);

                logger.debug("Contexto financeiro coletado para conta {}: {}", contaId, contexto);
            } catch (Exception e) {
                logger.warn("Erro ao coletar contexto financeiro para conta {}: {}", contaId, e.getMessage(), e);
                contexto.put("temConta", false);
                contexto.put("erro", e.getMessage());
            }
        } else {
            contexto.put("temConta", false);
        }
        return contexto;
    }


    private String gerarRespostaComIA(String prompt, Map<String, Object> contexto, List<String> historico) {
        try {
            ChatClient chatClient = chatClientBuilder.build();
            
            StringBuilder promptCompleto = new StringBuilder();
            promptCompleto.append("Você é um assistente financeiro especializado. Analise os dados financeiros fornecidos e responda de forma clara, objetiva e com cálculos quando necessário.\n\n");
            
            if (historico != null && !historico.isEmpty()) {
                promptCompleto.append("## Histórico da Conversa:\n");
                for (String msg : historico) {
                    promptCompleto.append("- ").append(msg).append("\n");
                }
                promptCompleto.append("\n");
            }
            
            if (Boolean.TRUE.equals(contexto.get("temConta"))) {
                promptCompleto.append("## Dados Financeiros do Usuário:\n");
                
                if (contexto.get("conta") != null) {
                    promptCompleto.append(String.format("- Conta: %s\n", contexto.get("conta")));
                }
                
                if (contexto.get("saldoAtual") != null) {
                    promptCompleto.append(String.format("- Saldo Atual: R$ %.2f\n", 
                        ((BigDecimal) contexto.get("saldoAtual")).doubleValue()));
                }
                
                if (contexto.get("receitasMes") != null) {
                    promptCompleto.append(String.format("- Receitas do Mês: R$ %.2f\n", 
                        ((BigDecimal) contexto.get("receitasMes")).doubleValue()));
                }
                
                if (contexto.get("despesasMes") != null) {
                    promptCompleto.append(String.format("- Despesas do Mês: R$ %.2f\n", 
                        ((BigDecimal) contexto.get("despesasMes")).doubleValue()));
                }
                
                if (contexto.get("totalReceitas") != null) {
                    promptCompleto.append(String.format("- Total de Receitas: R$ %.2f\n", 
                        ((BigDecimal) contexto.get("totalReceitas")).doubleValue()));
                }
                
                if (contexto.get("totalDespesas") != null) {
                    promptCompleto.append(String.format("- Total de Despesas: R$ %.2f\n", 
                        ((BigDecimal) contexto.get("totalDespesas")).doubleValue()));
                }
                
                if (contexto.get("capacidadeEconomia") != null) {
                    promptCompleto.append(String.format("- Capacidade de Economia: R$ %.2f\n", 
                        ((BigDecimal) contexto.get("capacidadeEconomia")).doubleValue()));
                }
                
                if (contexto.get("totalMovimentacoes") != null) {
                    promptCompleto.append(String.format("- Total de Movimentações: %d\n", 
                        contexto.get("totalMovimentacoes")));
                }
                
                if (contexto.get("totalMetas") != null && ((Integer) contexto.get("totalMetas")) > 0) {
                    promptCompleto.append(String.format("- Metas Ativas: %d\n", contexto.get("totalMetas")));
                    if (contexto.get("valorTotalMetas") != null) {
                        promptCompleto.append(String.format("- Valor Total das Metas: R$ %.2f\n", 
                            ((BigDecimal) contexto.get("valorTotalMetas")).doubleValue()));
                    }
                    if (contexto.get("valorAtualMetas") != null) {
                        promptCompleto.append(String.format("- Valor Atual das Metas: R$ %.2f\n", 
                            ((BigDecimal) contexto.get("valorAtualMetas")).doubleValue()));
                    }
                    if (contexto.get("detalhesMetas") != null) {
                        promptCompleto.append(String.format("- Detalhes das Metas: %s\n", 
                            contexto.get("detalhesMetas")));
                    }
                }
                
                if (contexto.get("topCategoriasDespesas") != null && 
                    !contexto.get("topCategoriasDespesas").toString().isEmpty()) {
                    promptCompleto.append(String.format("- Top Categorias de Despesas: %s\n", 
                        contexto.get("topCategoriasDespesas")));
                }
                
                promptCompleto.append("\n");
            }
            
            promptCompleto.append("## Pergunta do Usuário:\n");
            promptCompleto.append(prompt);
            promptCompleto.append("\n\n");
            promptCompleto.append("## Instruções:\n");
            promptCompleto.append("- Analise os dados financeiros fornecidos\n");
            promptCompleto.append("- Faça cálculos quando necessário (ex: percentuais, projeções, recomendações)\n");
            promptCompleto.append("- Elabore estratégias personalizadas baseadas nos dados reais\n");
            promptCompleto.append("- Seja específico e use os valores reais nos cálculos\n");
            promptCompleto.append("- Responda de forma clara e objetiva em português brasileiro\n");
            promptCompleto.append("- Se não houver dados suficientes, indique isso e sugira como obter mais informações\n");
            
            logger.debug("Enviando prompt para IA: {}", promptCompleto.toString().substring(0, Math.min(500, promptCompleto.length())));
            
            String resposta = chatClient.prompt()
                .user(promptCompleto.toString())
                .call()
                .content();
            
            logger.info("Resposta da IA gerada com sucesso");
            return resposta != null ? resposta : "Desculpe, não consegui gerar uma resposta. Tente novamente.";
            
        } catch (Exception e) {
            logger.error("Erro ao gerar resposta com IA: {}", e.getMessage(), e);
            return String.format(
                "Desculpe, ocorreu um erro ao processar sua solicitação com a IA. " +
                "Tente novamente em alguns instantes. Erro: %s", 
                e.getMessage()
            );
        }
    }

}
