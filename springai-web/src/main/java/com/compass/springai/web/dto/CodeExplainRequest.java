package com.compass.springai.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 代码解释请求DTO
 */
@Data
@Schema(description = "代码解释请求")
public class CodeExplainRequest {
    
    @NotBlank(message = "代码内容不能为空")
    @Size(max = 5000, message = "代码内容不能超过5000个字符")
    @Schema(description = "代码内容", example = "public class HelloWorld { public static void main(String[] args) { System.out.println(\"Hello World\"); } }")
    private String code;
    
    @NotBlank(message = "编程语言不能为空")
    @Schema(description = "编程语言", example = "Java")
    private String language;
} 