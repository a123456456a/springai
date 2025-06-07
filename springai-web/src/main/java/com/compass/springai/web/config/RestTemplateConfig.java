package com.compass.springai.web.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate 配置
 * 用于HTTP请求，包括调用DeepSeek API
 */
@Slf4j
@Configuration
public class RestTemplateConfig {

    /**
     * 创建RestTemplate Bean
     */
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000); // 连接超时10秒
        factory.setReadTimeout(60000);    // 读取超时60秒
        
        RestTemplate restTemplate = new RestTemplate(factory);
        
        log.info("RestTemplate配置完成，连接超时: 10秒, 读取超时: 60秒");
        return restTemplate;
    }
} 