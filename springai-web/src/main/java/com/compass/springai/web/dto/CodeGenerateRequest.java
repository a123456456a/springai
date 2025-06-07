package com.compass.springai.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 代码生成请求DTO
 */
@Data
@Schema(description = "代码生成请求")
public class CodeGenerateRequest {
    
    @NotBlank(message = "需求描述不能为空")
    @Size(max = 1000, message = "需求描述不能超过1000个字符")
    @Schema(description = "需求描述", example = "创建一个计算两个数字相加的方法")
    private String requirement;
    
    @NotBlank(message = "编程语言不能为空")
    @Schema(description = "编程语言", example = "Java")
    private String language;
} 