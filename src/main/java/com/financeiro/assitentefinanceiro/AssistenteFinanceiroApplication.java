package com.financeiro.assitentefinanceiro;

import com.financeiro.assitentefinanceiro.config.JwtProperties;
import com.financeiro.assitentefinanceiro.config.OllamaProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableConfigurationProperties({JwtProperties.class, OllamaProperties.class})
@EnableCaching
public class AssistenteFinanceiroApplication {

	public static void main(String[] args) {
		SpringApplication.run(AssistenteFinanceiroApplication.class, args);
	}

}
