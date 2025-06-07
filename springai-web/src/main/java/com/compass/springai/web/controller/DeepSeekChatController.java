package com.compass.springai.web.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * DeepSeek Chat 对话控制器
 * 直接调用DeepSeek API实现智能对话功能
 * 
 * 支持功能:
 * - 标准文本对话
 * - 流式文本对话 (SSE)
 * - 多种DeepSeek模型
 * - 自定义参数配置
 */
@Slf4j
@RestController
@RequestMapping("/api/deepseek/chat")
@Tag(name = "DeepSeek对话", description = "基于DeepSeek API的智能对话接口")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
@RequiredArgsConstructor
public class DeepSeekChatController {
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${spring.ai.deepseek.api-key:your-deepseek-api-key}")
    private String apiKey;
    
    @Value("${spring.ai.deepseek.base-url:https://api.deepseek.com}")
    private String baseUrl;
    
    @Value("${spring.ai.deepseek.chat.options.model:deepseek-chat}")
    private String defaultModel;
    
    @Value("${spring.ai.deepseek.chat.options.temperature:0.7}")
    private Double defaultTemperature;
    
    @Value("${spring.ai.deepseek.chat.options.max-tokens:4000}")
    private Integer defaultMaxTokens;

    /**
     * 标准DeepSeek对话接口
     */
    @PostMapping("/ask")
    @Operation(summary = "DeepSeek对话", description = "使用DeepSeek模型进行标准对话，返回完整响应")
    public ResponseEntity<Map<String, Object>> ask(@RequestBody @Valid ChatRequest request) {
        log.info("收到DeepSeek对话请求: {}", request.getMessage());
        
        try {
            // 构建请求体
            Map<String, Object> requestBody = Map.of(
                "model", defaultModel,
                "messages", List.of(
                    Map.of("role", "user", "content", request.getMessage())
                ),
                "temperature", defaultTemperature,
                "max_tokens", defaultMaxTokens,
                "stream", false
            );
            
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            // 调用DeepSeek API
            ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/chat/completions", 
                entity, 
                String.class
            );
            
            // 解析响应
            JsonNode responseJson = objectMapper.readTree(response.getBody());
            String content = responseJson.path("choices").get(0).path("message").path("content").asText();
            JsonNode usage = responseJson.path("usage");
            
            Map<String, Object> result = Map.of(
                "success", true,
                "message", "对话成功",
                "data", Map.of(
                    "response", content,
                    "model", responseJson.path("model").asText(),
                    "tokens", Map.of(
                        "prompt", usage.path("prompt_tokens").asInt(),
                        "completion", usage.path("completion_tokens").asInt(),
                        "total", usage.path("total_tokens").asInt()
                    )
                )
            );
            
            log.info("DeepSeek对话完成，响应长度: {}", content.length());
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("DeepSeek对话失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "对话失败: " + e.getMessage(),
                "error", e.getClass().getSimpleName()
            ));
        }
    }

    /**
     * 流式DeepSeek对话接口
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "DeepSeek流式对话", description = "使用DeepSeek模型进行流式对话，返回SSE格式的实时响应")
    public SseEmitter streamChat(@RequestBody @Valid ChatRequest request) {
        log.info("收到DeepSeek流式对话请求: {}", request.getMessage());
        
        SseEmitter emitter = new SseEmitter(60000L); // 60秒超时
        
        CompletableFuture.runAsync(() -> {
            try {
                // 构建流式请求体
                Map<String, Object> requestBody = Map.of(
                    "model", defaultModel,
                    "messages", List.of(
                        Map.of("role", "user", "content", request.getMessage())
                    ),
                    "temperature", defaultTemperature,
                    "max_tokens", defaultMaxTokens,
                    "stream", true
                );
                
                // 设置请求头
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setBearerAuth(apiKey);
                
                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
                
                // 模拟流式响应（实际项目中需要使用WebClient或其他支持流式的HTTP客户端）
                ResponseEntity<String> response = restTemplate.postForEntity(
                    baseUrl + "/chat/completions", 
                    entity, 
                    String.class
                );
                
                JsonNode responseJson = objectMapper.readTree(response.getBody());
                String content = responseJson.path("choices").get(0).path("message").path("content").asText();
                
                // 模拟流式输出，将完整响应分块发送
                String[] words = content.split(" ");
                for (String word : words) {
                    emitter.send(SseEmitter.event()
                        .name("data")
                        .data(word + " "));
                    Thread.sleep(50); // 模拟流式延迟
                }
                
                emitter.send(SseEmitter.event()
                    .name("end")
                    .data("对话结束"));
                emitter.complete();
                
            } catch (Exception e) {
                log.error("DeepSeek流式对话失败", e);
                try {
                    emitter.send(SseEmitter.event()
                        .name("error")
                        .data("对话服务出错: " + e.getMessage()));
                } catch (IOException ex) {
                    log.error("发送错误消息失败", ex);
                }
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    /**
     * 自定义参数对话接口
     */
    @PostMapping("/custom")
    @Operation(summary = "自定义参数对话", description = "使用自定义参数进行DeepSeek对话")
    public ResponseEntity<Map<String, Object>> customChat(@RequestBody @Valid CustomChatRequest request) {
        log.info("收到自定义DeepSeek对话请求: {}, 温度: {}, 最大token: {}", 
                request.getMessage(), request.getTemperature(), request.getMaxTokens());
        
        try {
            // 构建自定义请求体
            Map<String, Object> requestBody = Map.of(
                "model", request.getModel() != null ? request.getModel() : defaultModel,
                "messages", List.of(
                    Map.of("role", "user", "content", request.getMessage())
                ),
                "temperature", request.getTemperature() != null ? request.getTemperature() : defaultTemperature,
                "max_tokens", request.getMaxTokens() != null ? request.getMaxTokens() : defaultMaxTokens,
                "top_p", request.getTopP() != null ? request.getTopP() : 1.0,
                "stream", false
            );
            
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            // 调用DeepSeek API
            ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/chat/completions", 
                entity, 
                String.class
            );
            
            // 解析响应
            JsonNode responseJson = objectMapper.readTree(response.getBody());
            String content = responseJson.path("choices").get(0).path("message").path("content").asText();
            JsonNode usage = responseJson.path("usage");
            
            Map<String, Object> result = Map.of(
                "success", true,
                "message", "自定义对话成功",
                "data", Map.of(
                    "response", content,
                    "model", responseJson.path("model").asText(),
                    "options", Map.of(
                        "temperature", request.getTemperature(),
                        "maxTokens", request.getMaxTokens(),
                        "topP", request.getTopP(),
                        "model", request.getModel()
                    ),
                    "tokens", Map.of(
                        "prompt", usage.path("prompt_tokens").asInt(),
                        "completion", usage.path("completion_tokens").asInt(),
                        "total", usage.path("total_tokens").asInt()
                    )
                )
            );
            
            log.info("自定义DeepSeek对话完成，响应长度: {}", content.length());
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("自定义DeepSeek对话失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "自定义对话失败: " + e.getMessage(),
                "error", e.getClass().getSimpleName()
            ));
        }
    }

    /**
     * DeepSeek服务健康检查
     */
    @GetMapping("/health")
    @Operation(summary = "DeepSeek服务健康检查", description = "检查DeepSeek Chat服务状态")
    public ResponseEntity<Map<String, Object>> health() {
        try {
            // 发送简单测试请求
            Map<String, Object> requestBody = Map.of(
                "model", defaultModel,
                "messages", List.of(
                    Map.of("role", "user", "content", "Hello")
                ),
                "max_tokens", 10
            );
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/chat/completions", 
                entity, 
                String.class
            );
            
            JsonNode responseJson = objectMapper.readTree(response.getBody());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "DeepSeek Chat服务正常",
                "status", "healthy",
                "model", responseJson.path("model").asText(),
                "timestamp", System.currentTimeMillis()
            ));
            
        } catch (Exception e) {
            log.error("DeepSeek健康检查失败", e);
            return ResponseEntity.status(503).body(Map.of(
                "success", false,
                "message", "DeepSeek Chat服务异常",
                "status", "unhealthy",
                "error", e.getMessage(),
                "timestamp", System.currentTimeMillis()
            ));
        }
    }

    /**
     * 获取可用模型列表
     */
    @GetMapping("/models")
    @Operation(summary = "获取DeepSeek模型列表", description = "获取可用的DeepSeek模型列表")
    public ResponseEntity<Map<String, Object>> getModels() {
        List<Map<String, Object>> models = List.of(
            Map.of(
                "id", "deepseek-chat",
                "name", "DeepSeek Chat",
                "description", "通用对话模型，适合日常对话和问答"
            ),
            Map.of(
                "id", "deepseek-coder",
                "name", "DeepSeek Coder",
                "description", "专业代码生成模型，适合编程相关任务"
            ),
            Map.of(
                "id", "deepseek-reasoner",
                "name", "DeepSeek Reasoner",
                "description", "推理模型，适合复杂逻辑推理任务"
            )
        );
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "获取模型列表成功",
            "data", Map.of(
                "models", models,
                "default", defaultModel
            )
        ));
    }

    /**
     * 标准聊天请求
     */
    public static class ChatRequest {
        @NotBlank(message = "消息内容不能为空")
        private String message;

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    /**
     * 自定义参数聊天请求
     */
    public static class CustomChatRequest extends ChatRequest {
        private String model;
        private Double temperature;
        private Integer maxTokens;
        private Double topP;

        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
        public Double getTemperature() { return temperature; }
        public void setTemperature(Double temperature) { this.temperature = temperature; }
        public Integer getMaxTokens() { return maxTokens; }
        public void setMaxTokens(Integer maxTokens) { this.maxTokens = maxTokens; }
        public Double getTopP() { return topP; }
        public void setTopP(Double topP) { this.topP = topP; }
    }
} 