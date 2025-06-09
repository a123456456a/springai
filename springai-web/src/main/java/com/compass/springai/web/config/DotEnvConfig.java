package com.compass.springai.web.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * DotEnv 配置类
 * 用于加载 .env 文件中的环境变量到 Spring 环境中
 */
@Configuration
public class DotEnvConfig {

    private final ConfigurableEnvironment environment;

    public DotEnvConfig(ConfigurableEnvironment environment) {
        this.environment = environment;
    }

    /**
     * 在应用启动时加载 .env 文件
     */
    @PostConstruct
    public void loadDotEnv() {
        try {
            // 加载 .env 文件
            Dotenv dotenv = Dotenv.configure()
                    .directory("./")  // .env 文件位于项目根目录
                    .ignoreIfMalformed()
                    .ignoreIfMissing()
                    .load();

            // 将 .env 中的变量添加到 Spring 环境中
            Map<String, Object> dotenvProperties = new HashMap<>();
            dotenv.entries().forEach(entry -> {
                dotenvProperties.put(entry.getKey(), entry.getValue());
            });

            // 添加到 Spring 环境的属性源中，优先级较高
            environment.getPropertySources().addFirst(
                    new MapPropertySource("dotenv", dotenvProperties)
            );

            System.out.println("✅ .env 文件加载成功");
        } catch (Exception e) {
            System.err.println("⚠️  .env 文件加载失败: " + e.getMessage());
        }
    }
} 