package com.compass.springai.web.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.deepseek.DeepSeekChatOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ChatClient 配置类
 * 配置不同类型的AI聊天客户端
 */
@Configuration
public class ChatClientConfig {

    /**
     * 默认系统提示词
     */
    private static final String DEFAULT_SYSTEM_PROMPT = "I'm TonyQ, 我精通WEB开发, 你有什么问题都可以问我.";
    
    /**
     * DeepSeek模型的温度参数，控制回答的创造性
     */
    private static final double DEEPSEEK_TEMPERATURE = 1.3;

    /**
     * 默认的ChatClient Bean
     * 使用标准配置
     */
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem(DEFAULT_SYSTEM_PROMPT)
                .build();
    }

    /**
     * DeepSeek专用的ChatClient Bean
     * 配置了更高的温度参数以获得更有创造性的回答
     */
    @Bean
    public ChatClient deepSeekChatClient(ChatClient.Builder builder) {
        DeepSeekChatOptions deepSeekChatOptions = DeepSeekChatOptions.builder()
                .temperature(DEEPSEEK_TEMPERATURE)
                .build();
        
        return builder
                .defaultSystem(DEFAULT_SYSTEM_PROMPT)
                .defaultOptions(deepSeekChatOptions)
                .build();
    }
}
