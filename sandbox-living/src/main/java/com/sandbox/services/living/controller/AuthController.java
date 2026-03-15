package com.sandbox.services.living.controller;

import com.sandbox.services.common.base.vo.R;
import com.sandbox.services.living.model.dto.auth.LoginDTO;
import com.sandbox.services.living.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
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


    private final AuthenticationManager authenticationManager;

    @PostMapping("/login")
    public R<?> login(@RequestBody LoginDTO loginDTO) {
        return R.success(authService.passwordLogin( loginDTO.getPhone(),loginDTO.getPassword()));
    }

//    @PostMapping("/logout")
//    public R<?> logout(@RequestHeader("Authorization") String token) {
//        if (token != null && token.startsWith("Bearer ")) {
//            String tokenValue = token.substring(7);
//            tokenManager.removeToken(tokenValue);
//        }
//        return R.success();
//    }
//
//    @PostMapping("/refresh")
//    public R<?> refresh(@RequestHeader("Authorization") String token) {
//        if (token != null && token.startsWith("Bearer ")) {
//            String tokenValue = token.substring(7);
//            AuthorizationTokenBO newToken = tokenManager.refreshToken(tokenValue);
//
//            return R.success(new LoginVO(
//                    newToken.getTokenValue(),
//                    newToken.getUsername(),
//                    newToken.getExpireTime()
//            ));
//        }
//        return R.success();
//    }
//
//    @PostMapping("/captcha/login")
//    public R<?> captchaLogin(@RequestBody CaptchaLoginDTO loginDTO) {
//            // 1. 先验证验证码
//
//            // 2. 创建验证码认证Token
//            CaptchaAuthenticationToken authToken = new CaptchaAuthenticationToken(
//                    loginDTO.getAccount(),
//                    loginDTO.getCaptcha()
//            );
//
//            // 3. 认证（会调用CaptchaAuthenticationProvider）
//            Authentication authentication = authenticationManager.authenticate(authToken);
//
//            // 4. 获取用户详情
//            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
//
//            // 5. 创建令牌
//            AuthorizationTokenBO token = tokenManager.createToken(userDetails.getUserId(), userDetails.getUsername());
//
//            // 6. 返回登录结果
//            return R.success(new LoginVO(
//                    token.getTokenValue(),
//                    userDetails.getUsername(),
//                    token.getExpireTime()
//            ));
//
//    }
}
