package com.financeiro.assitentefinanceiro.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    @Bean
    public Counter loginAttemptCounter(MeterRegistry meterRegistry) {
        return Counter.builder("assitentefinanceiro_login_attempts_total")
                .description("Total de tentativas de login")
                .tag("application", "assitentefinanceiro")
                .register(meterRegistry);
    }

    @Bean
    public Counter loginSuccessCounter(MeterRegistry meterRegistry) {
        return Counter.builder("assitentefinanceiro_login_success_total")
                .description("Total de logins bem-sucedidos")
                .tag("application", "assitentefinanceiro")
                .register(meterRegistry);
    }

    @Bean
    public Counter loginFailureCounter(MeterRegistry meterRegistry) {
        return Counter.builder("assitentefinanceiro_login_failures_total")
                .description("Total de falhas de login")
                .tag("application", "assitentefinanceiro")
                .register(meterRegistry);
    }

    @Bean
    public Counter userRegistrationCounter(MeterRegistry meterRegistry) {
        return Counter.builder("assitentefinanceiro_user_registrations_total")
                .description("Total de registros de usuários")
                .tag("application", "assitentefinanceiro")
                .register(meterRegistry);
    }

    @Bean
    public Counter aiRequestCounter(MeterRegistry meterRegistry) {
        return Counter.builder("assitentefinanceiro_ai_requests_total")
                .description("Total de requisições para IA")
                .tag("application", "assitentefinanceiro")
                .register(meterRegistry);
    }

    @Bean
    public Timer aiResponseTimer(MeterRegistry meterRegistry) {
        return Timer.builder("assitentefinanceiro_ai_response_time")
                .description("Tempo de resposta das requisições de IA")
                .tag("application", "assitentefinanceiro")
                .register(meterRegistry);
    }

    @Bean
    public Timer databaseOperationTimer(MeterRegistry meterRegistry) {
        return Timer.builder("assitentefinanceiro_database_operation_time")
                .description("Tempo de operações no banco de dados")
                .tag("application", "assitentefinanceiro")
                .register(meterRegistry);
    }
}
