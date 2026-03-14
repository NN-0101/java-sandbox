package com.sandbox.services.living;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @description: 启动类
 * @author: 0101
 * @create: 2026/3/13
 */
@SpringBootApplication(scanBasePackages = {"com.sandbox.services"})
public class SandBoxLivingApplication {

    public static void main(String[] args) {
        SpringApplication.run(SandBoxLivingApplication.class, args);
    }
}
