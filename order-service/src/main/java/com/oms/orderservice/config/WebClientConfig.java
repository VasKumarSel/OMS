package com.oms.orderservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${app.market-data-service.url}")
    private String marketDataServiceUrl;

    @Value("${app.market-data-service.timeout:3000}")
    private long timeout;

    @Bean("marketDataWebClient")
    public WebClient marketDataWebClient() {
        return WebClient.builder()
                .baseUrl(marketDataServiceUrl)
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(1024 * 1024)) // 1MB
                .build();
    }
}
