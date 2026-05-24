package com.bytebridges.anytop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient(EloadConfig config) {
        return WebClient.builder()
        		.baseUrl(config.getBaseUrl())
                .build();
    }
    
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
