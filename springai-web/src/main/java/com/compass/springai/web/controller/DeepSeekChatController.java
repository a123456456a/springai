package com.compass.springai.web.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * DeepSeek Chat 对话控制器 直接调用DeepSeek API实现智能对话功能
 * 
 * 支持功能: - 标准文本对话 - 流式文本对话 (SSE) - 多种DeepSeek模型 - 自定义参数配置
 */
@Slf4j
@RestController
@RequestMapping("/api/deepseek/chat")
@Tag(name = "DeepSeek对话", description = "基于DeepSeek API的智能对话接口")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
@RequiredArgsConstructor
public class DeepSeekChatController {
    @Autowired
    private ChatClient chatClient;

    @GetMapping("/simple")
    public Map<String, String> completion(
            @RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        return Map.of("completion", chatClient.prompt().user(message).call().content());
    }

    @PostMapping("/stream")
    public Flux<String> stream(@RequestBody String message) {
        return chatClient.prompt().user(message).stream().content();
    }

}
