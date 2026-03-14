package com.sandbox.services.living.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sandbox.services.common.base.vo.R;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @description: 自定义访问拒绝处理器
 * @author: 0101
 * @create: 2026/03/14
 */
@Slf4j
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {

        log.error("权限不足: {}, message: {}", request.getRequestURI(), accessDeniedException.getMessage());

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403

        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(),
                R.fail("403", "权限不足，无法访问",null));
    }
}