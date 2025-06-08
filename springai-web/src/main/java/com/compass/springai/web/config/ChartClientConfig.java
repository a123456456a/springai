package com.compass.springai.web.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChartClientConfig {

    @Bean
    ChatClient chatClient(ChatClient.Builder builder) {
        String defaultSystem = "I'm TonyQ, 我精通WEB开发, 你有什么问题都可以问我.";
        return builder.defaultSystem(defaultSystem).build();
    }
}
