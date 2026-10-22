
package com.hiasoon.sdk.api;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(HiasoonApiConfig.class)
public class HiasoonApiAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public HiasoonApiClient hiasoonApiClient(HiasoonApiConfig config) {
        return new HiasoonApiClient(config);
    }
}
