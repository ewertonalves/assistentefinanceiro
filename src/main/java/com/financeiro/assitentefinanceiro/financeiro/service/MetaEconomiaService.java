package com.financeiro.assitentefinanceiro.financeiro.service;

import com.financeiro.assitentefinanceiro.cadastro.domain.DadosConta;
import com.financeiro.assitentefinanceiro.cadastro.service.CadastroContaService;
import com.financeiro.assitentefinanceiro.financeiro.domain.MetaEconomia;
import com.financeiro.assitentefinanceiro.financeiro.domain.dto.MetaEconomiaDTO;
import com.financeiro.assitentefinanceiro.financeiro.enums.StatusMeta;
import com.financeiro.assitentefinanceiro.financeiro.enums.TipoMeta;
import com.financeiro.assitentefinanceiro.financeiro.repository.MetaEconomiaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Supplier;

@Service
public class MetaEconomiaService {

    private static final Logger logger = LoggerFactory.getLogger(MetaEconomiaService.class);
    private final MetaEconomiaRepository repository;
    private final CadastroContaService contaService;

    public MetaEconomiaService(MetaEconomiaRepository repository, 
                              CadastroContaService contaService) {
        this.repository = repository;
        this.contaService = contaService;
    }

    @Transactional
    public MetaEconomia criarMeta(MetaEconomiaDTO metaDTO) {
        return executarComTratamentoErro(() -> {
            validarMetaDTO(metaDTO);

            logger.info("Iniciando criacao de meta de economia. Nome: {}, Valor: {}, Conta: {}", 
                metaDTO.nome(), metaDTO.valorMeta(), metaDTO.contaId());

            DadosConta conta = contaService.buscarContaPorId(metaDTO.contaId());
            
            MetaEconomia meta = MetaEconomia.fromDTO(metaDTO, conta);
            MetaEconomia metaSalva = repository.save(meta);

            logger.info("Meta criada com sucesso. ID: {}, Nome: {}, Valor: {}, Status: {}", 
                metaSalva.getId(), metaSalva.getNome(), metaSalva.getValorMeta(), metaSalva.getStatus());

            return metaSalva;
        }, "criar meta de economia");
    }

    public List<MetaEconomia> listarMetas() {
        return executarComTratamentoErro(() -> {
            logger.info("Buscando lista de todas as metas de economia");
            List<MetaEconomia> metas = repository.findAll();
            logger.info("Total de metas encontradas: {}", metas.size());
            return metas;
        }, "listar metas");
    }

    public MetaEconomia buscarMetaPorId(Long id) {
        return executarComTratamentoErro(() -> {
            validarId(id);

            logger.info("Buscando meta por ID: {}", id);
            MetaEconomia meta = repository.findById(id).orElseThrow(() -> {
                logger.error("Meta nao encontrada com ID: {}", id);
                return new IllegalArgumentException("Meta nao encontrada com ID: " + id);
            });
            logger.info("Meta encontrada com sucesso. ID: {}, Nome: {}, Progresso: {}%", 
                meta.getId(), meta.getNome(), meta.getPercentualConcluido());
            return meta;
        }, "buscar meta por ID");
    }

    public List<MetaEconomia> buscarMetasPorConta(Long contaId) {
        return executarComTratamentoErro(() -> {
            validarId(contaId);
            contaService.buscarContaPorId(contaId);

            logger.info("Buscando metas da conta ID: {}", contaId);
            List<MetaEconomia> metas = repository.findByContaId(contaId);
            logger.info("Total de metas encontradas para conta {}: {}", contaId, metas.size());
            return metas;
        }, "buscar metas por conta");
    }

    public List<MetaEconomia> buscarMetasAtivasPorConta(Long contaId) {
        return executarComTratamentoErro(() -> {
            validarId(contaId);
            contaService.buscarContaPorId(contaId);

            logger.info("Buscando metas ativas da conta ID: {}", contaId);
            List<MetaEconomia> metas = repository.findMetasAtivasByContaId(contaId);
            logger.info("Total de metas ativas encontradas para conta {}: {}", contaId, metas.size());
            return metas;
        }, "buscar metas ativas por conta");
    }

    public List<MetaEconomia> buscarMetasVencidasPorConta(Long contaId) {
        return executarComTratamentoErro(() -> {
            validarId(contaId);
            contaService.buscarContaPorId(contaId);

            logger.info("Buscando metas vencidas da conta ID: {}", contaId);
            List<MetaEconomia> metas = repository.findMetasVencidasByContaId(contaId, LocalDate.now());
            logger.info("Total de metas vencidas encontradas para conta {}: {}", contaId, metas.size());
            return metas;
        }, "buscar metas vencidas por conta");
    }

    public List<MetaEconomia> buscarMetasPorTipo(Long contaId, TipoMeta tipoMeta) {
        return executarComTratamentoErro(() -> {
            validarId(contaId);
            validarTipoMeta(tipoMeta);
            contaService.buscarContaPorId(contaId);

            logger.info("Buscando metas do tipo {} para conta {}", tipoMeta, contaId);
            List<MetaEconomia> metas = repository.findByContaIdAndTipoMeta(contaId, tipoMeta);
            logger.info("Total de metas do tipo {} encontradas: {}", tipoMeta, metas.size());
            return metas;
        }, "buscar metas por tipo");
    }

    @Transactional
    public MetaEconomia atualizarMeta(Long id, MetaEconomiaDTO metaDTO) {
        return executarComTratamentoErro(() -> {
            validarMetaDTO(metaDTO);
            validarId(id);

            logger.info("Iniciando atualizacao da meta ID: {}", id);

            MetaEconomia metaExistente = buscarMetaPorId(id);

            metaExistente.atualizarDados(
                metaDTO.nome(),
                metaDTO.descricao(),
                metaDTO.tipoMeta(),
                metaDTO.valorMeta(),
                metaDTO.dataInicio(),
                metaDTO.dataFim(),
                metaDTO.observacoes()
            );

            MetaEconomia metaAtualizada = repository.save(metaExistente);
            logger.info("Meta atualizada com sucesso. ID: {}, Nome: {}, Progresso: {}%", 
                metaAtualizada.getId(), metaAtualizada.getNome(), metaAtualizada.getPercentualConcluido());

            return metaAtualizada;
        }, "atualizar meta");
    }

    @Transactional
    public MetaEconomia atualizarProgressoMeta(Long id, BigDecimal valorAdicionado) {
        return executarComTratamentoErro(() -> {
            validarId(id);
            validarValorProgresso(valorAdicionado);

            logger.info("Atualizando progresso da meta ID: {} com valor: {}", id, valorAdicionado);

            MetaEconomia meta = buscarMetaPorId(id);
            
            if (meta.isConcluida()) {
                logger.warn("Meta ja esta concluida. ID: {}", id);
                throw new IllegalArgumentException("Meta ja esta concluida");
            }

            BigDecimal progressoAnterior = meta.getPercentualConcluido();
            meta.atualizarProgresso(valorAdicionado);
            
            MetaEconomia metaAtualizada = repository.save(meta);
            
            logger.info("Progresso da meta atualizado com sucesso. ID: {}, Progresso: {}% -> {}%", 
                id, progressoAnterior, metaAtualizada.getPercentualConcluido());

            return metaAtualizada;
        }, "atualizar progresso da meta");
    }

    @Transactional
    public void excluirMeta(Long id) {
        executarComTratamentoErro(() -> {
            validarId(id);

            logger.info("Iniciando exclusao da meta ID: {}", id);

            if (!repository.existsById(id)) {
                logger.error("Tentativa de exclusao de meta inexistente. ID: {}", id);
                throw new IllegalArgumentException("Meta nao encontrada com ID: " + id);
            }

            repository.deleteById(id);
            logger.info("Meta excluida com sucesso. ID: {}", id);
            return null;
        }, "excluir meta");
    }

    @Transactional
    public MetaEconomia pausarMeta(Long id) {
        return executarComTratamentoErro(() -> {
            validarId(id);

            logger.info("Pausando meta ID: {}", id);

            MetaEconomia meta = buscarMetaPorId(id);
            
            if (StatusMeta.PAUSADA.equals(meta.getStatus())) {
                logger.warn("Meta ja esta pausada. ID: {}", id);
                throw new IllegalArgumentException("Meta ja esta pausada");
            }

            if (meta.isConcluida()) {
                logger.warn("Nao e possivel pausar meta concluida. ID: {}", id);
                throw new IllegalArgumentException("Nao e possivel pausar meta concluida");
            }

            meta.pausar();
            MetaEconomia metaPausada = repository.save(meta);

            logger.info("Meta pausada com sucesso. ID: {}", id);
            return metaPausada;
        }, "pausar meta");
    }

    @Transactional
    public MetaEconomia reativarMeta(Long id) {
        return executarComTratamentoErro(() -> {
            validarId(id);

            logger.info("Reativando meta ID: {}", id);

            MetaEconomia meta = buscarMetaPorId(id);
            
            logger.info("Status atual da meta ID {}: {}", id, meta.getStatus());
            
            if (!StatusMeta.PAUSADA.equals(meta.getStatus())) {
                logger.warn("Meta nao esta pausada. Status atual: {}. Apenas metas pausadas podem ser reativadas.", meta.getStatus());
                throw new IllegalArgumentException("Meta nao esta pausada. Status atual: " + meta.getStatus() + ". Apenas metas pausadas podem ser reativadas.");
            }

            if (meta.isVencida()) {
                logger.warn("Meta vencida detectada. ID: {}, Data fim: {}. Permitindo reativacao para permitir ajustes.", id, meta.getDataFim());
            }

            meta.reativar();
            MetaEconomia metaReativada = repository.save(meta);

            logger.info("Meta reativada com sucesso. ID: {}, Novo status: {}", id, metaReativada.getStatus());
            return metaReativada;
        }, "reativar meta");
    }

    public MetaEconomiaDTO converterEntidadeParaDTO(MetaEconomia meta) {
        return MetaEconomiaDTO.fromMetaEconomia(meta);
    }

    public int verificarMetasVencidas() {
        return executarComTratamentoErro(() -> {
            logger.info("Verificando metas vencidas");
            
            List<MetaEconomia> todasMetas = repository.findAll();
            int metasVencidas = 0;
            
            for (MetaEconomia meta : todasMetas) {
                if (meta.isVencida() && !StatusMeta.VENCIDA.equals(meta.getStatus())) {
                    meta.marcarComoVencida();
                    repository.save(meta);
                    metasVencidas++;
                    logger.info("Meta marcada como vencida. ID: {}, Nome: {}", meta.getId(), meta.getNome());
                }
            }
            
            logger.info("Verificacao de metas vencidas concluida. {} metas marcadas como vencidas", metasVencidas);
            return metasVencidas;
        }, "verificar metas vencidas");
    }




    private void validarMetaDTO(MetaEconomiaDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Dados da meta sao obrigatorios");
        }

        validarCampoObrigatorio(dto.nome(), "nome");
        validarId(dto.contaId());
        validarTipoMeta(dto.tipoMeta());

        String erro = 
            (dto.valorMeta() == null || dto.valorMeta().compareTo(BigDecimal.ZERO) <= 0) ? "Valor da meta deve ser maior que zero" :
            (dto.dataInicio() == null || dto.dataFim() == null) ? "Data de inicio e fim sao obrigatorias" :
            (dto.dataInicio().isAfter(dto.dataFim())) ? "Data de inicio deve ser anterior a data de fim" :
            (dto.dataInicio().isAfter(LocalDate.now())) ? "Data de inicio nao pode ser futura" :
            null;

        if (erro != null) {
            throw new IllegalArgumentException(erro);
        }

        logger.debug("Validacao de dados da meta realizada com sucesso");
    }

    private void validarId(Long id) {
        if (id == null || id <= 0) {
            logger.error("ID inválido recebido: {}", id);
            throw new IllegalArgumentException("ID inválido");
        }
        logger.debug("ID validado com sucesso: {}", id);
    }

    private void validarTipoMeta(TipoMeta tipoMeta) {
        if (tipoMeta == null) {
            throw new IllegalArgumentException("Tipo de meta e obrigatorio");
        }
    }

    private void validarValorProgresso(BigDecimal valor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor do progresso deve ser maior que zero");
        }
    }

    private void validarCampoObrigatorio(String valor, String nomeCampo) {
        if (valor == null || valor.trim().isEmpty()) {
            logger.error("Campo {} está vazio", nomeCampo);
            throw new IllegalArgumentException(nomeCampo.substring(0, 1).toUpperCase() + nomeCampo.substring(1) + " e obrigatorio");
        }
    }

    private <T> T executarComTratamentoErro(Supplier<T> operacao, String nomeOperacao) {
        try {
            return operacao.get();
        } catch (IllegalArgumentException e) {
            logger.error("Erro de validacao em {}: {}", nomeOperacao, e.getMessage());
            throw e;
        } catch (DataIntegrityViolationException e) {
            logger.error("Violacao de integridade em {}: {}", nomeOperacao, e.getMessage());
            throw new IllegalArgumentException("Dados inválidos para " + nomeOperacao);
        } catch (Exception e) {
            logger.error("Erro inesperado em {}: {}", nomeOperacao, e.getMessage());
            throw new RuntimeException("Erro interno ao executar " + nomeOperacao + ": " + e.getMessage());
        }
    }
}
