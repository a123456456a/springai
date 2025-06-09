package com.compass.springai.web.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

/**
 * DotEnv 初始化器
 * 在应用上下文初始化时加载 .env 文件
 * 这种方式比 @PostConstruct 更早执行
 */
public class DotEnvInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
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
            applicationContext.getEnvironment().getPropertySources().addFirst(
                    new MapPropertySource("dotenv", dotenvProperties)
            );

            System.out.println("✅ .env 文件通过初始化器加载成功");
        } catch (Exception e) {
            System.err.println("⚠️  .env 文件加载失败: " + e.getMessage());
        }
    }
} 