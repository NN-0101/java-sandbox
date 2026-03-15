package com.sandbox.services.living.security.handler;

import com.sandbox.services.common.base.vo.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Controller方法层面的权限异常处理器
 *
 * <p>处理在 Controller 方法执行过程中发生的权限拒绝异常，主要场景包括：
 * <ul>
 *   <li>通过 {@code @PreAuthorize} 注解进行的方法级别权限控制：
 *       <pre>{@code
 *       @PreAuthorize("hasRole('ADMIN')")
 *       @GetMapping("/admin/data")
 *       public Data getAdminData() { ... }
 *
 *       @PreAuthorize("@securityService.checkPermission(#id)")
 *       @DeleteMapping("/user/{id}")
 *       public void deleteUser(@PathVariable Long id) { ... }
 *       }</pre>
 *   </li>
 *   <li>通过 {@code @PostAuthorize}、{@code @PreFilter}、{@code @PostFilter} 等注解进行的权限控制</li>
 *   <li>使用 {@code @Secured} 或 {@code @RolesAllowed} 注解的权限控制</li>
 * </ul>
 *
 * <p><b>触发时机：</b>请求已到达 Controller 方法，但在执行前/后权限校验失败时<br>
 * <b>处理位置：</b>在 AOP 切面中通过 {@code @RestControllerAdvice} 统一捕获处理
 *
 * @author 0101
 * @see org.springframework.security.access.prepost.PreAuthorize
 * @see org.springframework.security.authorization.method.AuthorizationManagerBeforeMethodInterceptor
 * @since 2026-03-15
 */
@Slf4j
@RestControllerAdvice
public class SecurityExceptionHandler {

    /**
     * 处理方法级别的权限拒绝异常
     *
     * <p>主要处理通过 {@code @PreAuthorize} 等注解触发的权限不足情况，
     * 兼容 Spring Security 6.0+ 的 {@link AuthorizationDeniedException} 和旧版本的 {@link AccessDeniedException}
     *
     * @param e 权限拒绝异常（可以是 AccessDeniedException 或其子类 AuthorizationDeniedException）
     * @return 统一的 403 权限不足响应
     */
    @ExceptionHandler({AccessDeniedException.class, AuthorizationDeniedException.class})
    public ResponseEntity<R<Void>> handleMethodLevelAccessDenied(Exception e) {
        log.error("【方法层面】权限不足 - 类型: {}, 错误信息: {}", e.getClass().getSimpleName(), e.getMessage());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                R.fail(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase(), null));
    }
}