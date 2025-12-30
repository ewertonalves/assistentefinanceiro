package com.financeiro.assitentefinanceiro.cadastro.controller;

import com.financeiro.assitentefinanceiro.cadastro.domain.DadosConta;
import com.financeiro.assitentefinanceiro.cadastro.domain.dto.DadosContaDTO;
import com.financeiro.assitentefinanceiro.cadastro.service.CadastroContaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Tag(name = "Cadastro de Contas", description = "Operações de gerenciamento de contas bancárias")
@RestController
@RequestMapping("/api/v1/contas")
public class CadastroContaController {

    private final CadastroContaService service;
    private static final Logger logger = LoggerFactory.getLogger(CadastroContaController.class);

    public CadastroContaController(CadastroContaService service) {
        this.service = service;
    }

    @Operation(summary = "Listar todas as contas", description = "Retorna uma lista de todas as contas cadastradas")
    @GetMapping
    public ResponseEntity<List<DadosContaDTO>> listarContas() {
        try {
            logger.info("Solicitação para listar todas as contas");
            List<DadosConta> contas = service.listarContas();
            List<DadosContaDTO> contasDTO = contas.stream().map(service::converterEntidadeParaDTO).toList();
            logger.info("Lista de contas retornada com sucesso. Total: {}", contasDTO.size());
            return ResponseEntity.ok(contasDTO);
        } catch (Exception e) {
            logger.error("Erro ao listar contas", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Buscar conta por ID", description = "Retorna os dados de uma conta específica pelo ID")
    @GetMapping("/{id}")
    public ResponseEntity<DadosContaDTO> buscarContaPorId(@PathVariable Long id) {
        try {
            logger.info("Solicitação para buscar conta por ID: {}", id);
            DadosConta conta = service.buscarContaPorId(id);
            DadosContaDTO contaDTO = service.converterEntidadeParaDTO(conta);
            logger.info("Conta encontrada com sucesso. ID: {}, Responsável: {}", conta.getId(), conta.getResponsavel());
            return ResponseEntity.ok(contaDTO);
        } catch (IllegalArgumentException e) {
            logger.warn("Conta não encontrada: {} - {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            logger.error("Erro ao buscar conta por ID", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Criar nova conta", description = "Cadastra uma nova conta bancária")
    @PostMapping
    public ResponseEntity<DadosContaDTO> criarConta(@RequestBody DadosContaDTO contaDTO) {
        try {
            logger.info("Solicitação para criar nova conta. Responsável: {}, Banco: {}", contaDTO.responsavel(), contaDTO.banco());
            DadosConta novaConta = service.cadastrarConta(contaDTO);
            DadosContaDTO contaCriada = service.converterEntidadeParaDTO(novaConta);
            logger.info("Conta criada com sucesso. ID: {}, Responsável: {}, Banco: {}", contaCriada.id(), contaCriada.responsavel(), contaCriada.banco());
            return ResponseEntity.status(HttpStatus.CREATED).body(contaCriada);
        } catch (IllegalArgumentException e) {
            logger.warn("Erro de validação ao criar conta: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            logger.error("Erro ao criar conta", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Atualizar conta", description = "Atualiza os dados de uma conta existente")
    @PutMapping("/{id}")
    public ResponseEntity<DadosContaDTO> atualizarConta(@PathVariable Long id, @RequestBody DadosContaDTO contaDTO) {
        try {
            logger.info("Solicitação para atualizar conta. ID: {}, Responsável: {}", id, contaDTO.responsavel());
            DadosConta contaAtualizada = service.atualizarConta(id, contaDTO);
            DadosContaDTO contaAtualizadaDTO = service.converterEntidadeParaDTO(contaAtualizada);
            logger.info("Conta atualizada com sucesso. ID: {}, Responsável: {}, Banco: {}", contaAtualizadaDTO.id(), contaAtualizadaDTO.responsavel(), contaAtualizadaDTO.banco());
            return ResponseEntity.ok(contaAtualizadaDTO);
        } catch (IllegalArgumentException e) {
            logger.warn("Erro de validação ao atualizar conta: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            logger.error("Erro ao atualizar conta", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Deletar conta", description = "Remove uma conta pelo ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarConta(@PathVariable Long id) {
        try {
            logger.info("Solicitação para deletar conta. ID: {}", id);
            service.apagarConta(id);
            logger.info("Conta deletada com sucesso. ID: {}", id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            logger.warn("Conta para deletar não encontrada: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            logger.error("Erro ao deletar conta", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
