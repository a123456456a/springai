package com.compass.springai.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 聊天请求DTO
 */
@Data
@Schema(description = "聊天请求")
public class ChatRequest {
    
    @NotBlank(message = "消息内容不能为空")
    @Size(max = 2000, message = "消息内容不能超过2000个字符")
    @Schema(description = "用户消息", example = "你好，请介绍一下Spring Boot")
    private String message;
} 