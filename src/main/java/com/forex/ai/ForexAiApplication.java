package com.forex.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ForexAiApplication {
    public static void main(String[] args) {
        SpringApplication.run(ForexAiApplication.class, args);
        System.out.println("✅ Forex AI Bot işə düşdü!");
        System.out.println("📊 API: http://localhost:8080");
        System.out.println("📖 Swagger: http://localhost:8080/api/info");
    }
}
