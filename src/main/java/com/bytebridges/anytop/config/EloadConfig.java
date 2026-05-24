package com.bytebridges.anytop.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "eload")
@Getter
@Setter
public class EloadConfig {
    private String baseUrl;
    private Endpoints endpoints;
    private String username;
    private String password;

    @Getter @Setter
    public static class Endpoints {
        private String ussdGateway;
        private String getStatus;
        private String sendCmd;
        private String postSms;
    }
}