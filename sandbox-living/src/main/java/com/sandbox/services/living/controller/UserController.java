package com.sandbox.services.living.controller;

import com.sandbox.services.living.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @description: 用户表(User)表控制层
 * @author: 0101
 * @create: 2026-03-13 22:04:03
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController{

    @Resource
    private UserService userService;

}

