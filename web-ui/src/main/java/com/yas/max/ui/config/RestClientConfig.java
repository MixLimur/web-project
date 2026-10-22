package com.yas.max.ui.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Value("${app.rest-api.url}")
    private String restApiUrl;

    @Bean
    public RestClient restClient() {
        return RestClient.builder()
                .baseUrl(restApiUrl)
                .build();
    }
}
