package com.financeiro.assitentefinanceiro.monitoring.controller;

import com.financeiro.assitentefinanceiro.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.boot.actuate.metrics.MetricsEndpoint;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/monitoring")
@CrossOrigin
@Tag(name = "Monitoramento", description = "Endpoints para monitoramento e métricas da aplicação")
public class MonitoringController {

    private final MetricsEndpoint metricsEndpoint;

    public MonitoringController(MetricsEndpoint metricsEndpoint) {
        this.metricsEndpoint = metricsEndpoint;
    }

    @Operation(summary = "Dashboard de métricas", description = "Retorna métricas principais da aplicação")
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboard() {
        try {
            Map<String, Object> dashboard = new HashMap<>();
            dashboard.put("status", "UP");            
            Map<String, Object> customMetrics = new HashMap<>();
            
            try {
                var loginAttempts = metricsEndpoint.metric("assitentefinanceiro_login_attempts_total", null);
                customMetrics.put("login_attempts", loginAttempts != null ? 
                    loginAttempts.getMeasurements().get(0).getValue() : 0);
            } catch (Exception e) {
                customMetrics.put("login_attempts", 0);
            }
            
            try {
                var loginSuccess = metricsEndpoint.metric("assitentefinanceiro_login_success_total", null);
                customMetrics.put("login_success", loginSuccess != null ? 
                    loginSuccess.getMeasurements().get(0).getValue() : 0);
            } catch (Exception e) {
                customMetrics.put("login_success", 0);
            }
            
            try {
                var userRegistrations = metricsEndpoint.metric("assitentefinanceiro_user_registrations_total", null);
                customMetrics.put("user_registrations", userRegistrations != null ? 
                    userRegistrations.getMeasurements().get(0).getValue() : 0);
            } catch (Exception e) {
                customMetrics.put("user_registrations", 0);
            }
            
            dashboard.put("custom_metrics", customMetrics);
            
            Map<String, Object> systemMetrics = new HashMap<>();
            
            try {
                var jvmMemory = metricsEndpoint.metric("jvm.memory.used", null);
                systemMetrics.put("memory_used_bytes", jvmMemory != null ? 
                    jvmMemory.getMeasurements().get(0).getValue() : 0);
            } catch (Exception e) {
                systemMetrics.put("memory_used_bytes", 0);
            }
            
            try {
                var httpRequests = metricsEndpoint.metric("http.server.requests", null);
                systemMetrics.put("http_requests_total", httpRequests != null ? 
                    httpRequests.getMeasurements().get(0).getValue() : 0);
            } catch (Exception e) {
                systemMetrics.put("http_requests_total", 0);
            }
            
            dashboard.put("system_metrics", systemMetrics);
            dashboard.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(
                ApiResponse.sucesso("Dashboard de métricas carregado com sucesso", dashboard)
            );
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(ApiResponse.erro("Erro ao carregar dashboard", e.getMessage()));
        }
    }

    @Operation(summary = "Status da aplicação", description = "Retorna status de saúde da aplicação")
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getHealth() {
        try {
            Map<String, Object> healthData = new HashMap<>();
            healthData.put("status", "UP");
            healthData.put("application", "assitentefinanceiro");
            healthData.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(
                ApiResponse.sucesso("Status da aplicação", healthData)
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(ApiResponse.erro("Erro ao verificar status", e.getMessage()));
        }
    }
}
