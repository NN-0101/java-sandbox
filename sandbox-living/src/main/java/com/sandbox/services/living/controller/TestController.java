package com.sandbox.services.living.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description:
 * @author: 0101
 * @create: 2026/3/13
 */
@Slf4j
@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("")
    public String test(){
        return "test";
    }
}
