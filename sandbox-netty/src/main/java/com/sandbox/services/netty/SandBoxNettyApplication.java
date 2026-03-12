package com.sandbox.services.netty;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @description: 启动类
 * @author: xp
 * @create: 2026/3/12
 */
@SpringBootApplication(scanBasePackages = {"com.sandbox.services"})
public class SandBoxNettyApplication {

    public static void main(String[] args) {
        SpringApplication.run(SandBoxNettyApplication.class, args);
    }
}
