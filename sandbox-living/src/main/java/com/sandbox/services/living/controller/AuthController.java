package com.sandbox.services.living.controller;

import com.sandbox.services.common.base.vo.R;
import com.sandbox.services.living.model.dto.auth.LoginDTO;
import com.sandbox.services.living.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description: 登录控制器
 * @author: 0101
 * @create: 2026/03/14
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public R<?> login(@Valid @RequestBody LoginDTO loginDTO) {
        return R.success(authService.passwordLogin( loginDTO.getPhone(),loginDTO.getPassword()));
    }
}
