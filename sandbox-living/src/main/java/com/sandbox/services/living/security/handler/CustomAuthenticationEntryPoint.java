package com.sandbox.services.living.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sandbox.services.common.base.vo.R;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @description: 自定义认证入口点
 * @author: 0101
 * @create: 2026/03/14
 */
@Slf4j
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        log.error("未认证访问: {}, message: {}", request.getRequestURI(), authException.getMessage());

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401

        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(),
                R.fail("401", "未认证，请先登录",null));
    }
}
