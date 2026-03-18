package com.sandbox.services.common.base.aspect;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * 请求日志切面 - 打印请求参数和响应结果
 *
 * <p>该类通过 AOP 切面技术，对所有 Controller 层的请求进行拦截，自动打印请求信息
 * （包括请求头、请求参数、请求体）和响应结果，并统计请求耗时。这大大简化了手动
 * 记录日志的工作，同时提供了统一的日志格式，便于问题排查和性能监控。
 *
 * <p><b>核心功能：</b>
 * <ul>
 *   <li><b>请求日志：</b>在请求处理前打印请求方法、URI、请求头、Query 参数和 Body 参数</li>
 *   <li><b>响应日志：</b>在请求处理后打印响应结果（格式化 JSON 输出）</li>
 *   <li><b>耗时统计：</b>计算并打印整个请求的处理耗时</li>
 *   <li><b>自动清理：</b>使用 ThreadLocal 存储开始时间，请求结束后自动清理，防止内存泄漏</li>
 *   <li><b>异常安全：</b>切面中的日志打印异常不会影响主业务流程</li>
 * </ul>
 *
 * <p><b>拦截范围：</b>
 * 所有被 {@code @RestController} 注解标记的类中的方法都会被拦截。
 *
 * <p><b>日志示例：</b>
 * <pre>
 * INFO  - uri: /api/users  method:POST  params: {"page":"1","size":"10"}  body: {"name":"张三","age":18}  headers: {"user-agent":"Mozilla/5.0","content-type":"application/json"}
 * INFO  - uri: /api/users  result:
 * {
 *   "code": 0,
 *   "msg": "success",
 *   "data": [...],
 *   "traceId": "1a2b3c4d5e6f7g8h"
 * }
 * INFO  - uri: /api/users  请求耗时: 156
 * </pre>
 *
 * <p><b>设计要点：</b>
 * <ul>
 *   <li><b>执行顺序：</b>使用 {@code @Order(1)} 设置切面优先级，确保在业务逻辑之前执行</li>
 *   <li><b>线程安全：</b>使用 {@link ThreadLocal} 存储每个请求的开始时间，避免并发问题</li>
 *   <li><b>参数过滤：</b>自动过滤 {@link HttpServletRequest}、{@link HttpServletResponse} 等非业务参数，
 *       避免日志过大或序列化异常</li>
 *   <li><b>文件上传支持：</b>对 {@link MultipartFile} 类型参数特殊处理，只打印文件信息而非内容</li>
 *   <li><b>响应格式化：</b>使用 JSON PrettyFormat 格式化输出，提高可读性</li>
 *   <li><b>异常隔离：</b>切面中的异常被捕获并记录，不会传播到业务代码中</li>
 * </ul>
 *
 * <p><b>注意事项：</b>
 * <ul>
 *   <li>生产环境中，如果请求量巨大，建议考虑关闭此切面或调整日志级别，避免日志过多影响性能</li>
 *   <li>敏感信息（如密码、token）会明文打印在日志中，需要注意日志脱敏处理</li>
 *   <li>大文件上传时，打印完整请求体会导致日志过大，建议对文件类型参数进行裁剪</li>
 *   <li>确保 {@code ThreadLocal} 在请求结束后被正确清理，防止内存泄漏</li>
 * </ul>
 *
 * @author 0101
 * @see org.springframework.web.bind.annotation.RestController
 * @see org.aspectj.lang.annotation.Aspect
 * @since 2026-03-12
 */
@Aspect
@Order(1)
@Component
public class RequestLogAspect {

    private static final Logger log = LoggerFactory.getLogger(RequestLogAspect.class);

    /**
     * 请求开始时间 ThreadLocal 存储
     *
     * <p>每个请求的开始时间存储在当前线程的 ThreadLocal 中，
     * 确保在并发环境下各请求的耗时统计互不干扰。
     */
    private final ThreadLocal<Long> requestCostThreadLocal = new ThreadLocal<>();

    /**
     * 切点定义
     *
     * <p>拦截所有被 {@code @RestController} 注解标记的类中的方法。
     */
    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    private void controllerAspect() {
        // 切点定义，无需方法体
    }

    /**
     * 请求前置处理
     *
     * <p>在 Controller 方法执行前调用，记录请求开始时间并打印请求信息。
     *
     * <p><b>记录内容：</b>
     * <ul>
     *   <li>请求开始时间（用于后续计算耗时）</li>
     *   <li>请求 URI 和 HTTP 方法</li>
     *   <li>请求头信息（所有请求头）</li>
     *   <li>Query 参数（URL 中的参数）</li>
     *   <li>请求体参数（JSON 格式）</li>
     * </ul>
     *
     * @param joinPoint 连接点，包含方法参数等信息
     */
    @Before(value = "controllerAspect()")
    public void methodBefore(JoinPoint joinPoint) {
        try {
            // 记录请求开始时间
            requestCostThreadLocal.set(System.currentTimeMillis());

            // 获取当前请求的 HttpServletRequest 对象
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();
            assert requestAttributes != null;
            HttpServletRequest request = requestAttributes.getRequest();

            // 获取请求 URI 和 HTTP 方法
            String uri = request.getRequestURI();
            String method = request.getMethod();

            // ========== 收集请求头信息 ==========
            Map<String, String> headerMap = new HashMap<>(16);
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                String headerValue = request.getHeader(headerName);
                headerMap.put(headerName, headerValue);
            }

            // ========== 收集请求体参数 ==========
            StringBuilder bodyBuilder = new StringBuilder();
            for (Object arg : joinPoint.getArgs()) {
                // 过滤掉非业务参数（如 HttpServletRequest、HttpServletResponse）
                if (arg instanceof HttpServletResponse || arg instanceof HttpServletRequest) {
                    continue;
                }
                // 对 MultipartFile 特殊处理，只打印文件信息而不是内容
                if (arg instanceof MultipartFile) {
                    bodyBuilder.append(arg);
                } else {
                    bodyBuilder.append(JSONObject.toJSONString(arg));
                    bodyBuilder.append(",");
                }
            }
            // 如果接口没有入参，添加一个占位符，避免后续截取时下标越界
            if (bodyBuilder.length() <= 0) {
                bodyBuilder.append(",");
            }

            // ========== 打印请求日志 ==========
            log.info("uri: {}  method:{}  params: {}  body: {}  headers: {}",
                    uri,
                    method,
                    JSON.toJSONString(request.getParameterMap()),
                    bodyBuilder.substring(0, bodyBuilder.length() - 1),
                    JSONObject.toJSONString(headerMap));

        } catch (Exception e) {
            // 确保日志切面的异常不会影响主业务流程
            log.error("请求日志切面前置处理异常:", e);
        }
    }

    /**
     * 请求后置返回处理
     *
     * <p>在 Controller 方法执行成功后调用，打印响应结果和请求耗时。
     *
     * <p><b>记录内容：</b>
     * <ul>
     *   <li>响应结果（格式化 JSON）</li>
     *   <li>请求总耗时（毫秒）</li>
     * </ul>
     *
     * @param o 控制器方法的返回值
     */
    @AfterReturning(returning = "o", pointcut = "controllerAspect()")
    public void methodAfterReturning(Object o) {
        try {
            // 获取当前请求的 HttpServletRequest 对象
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();
            assert requestAttributes != null;
            HttpServletRequest request = requestAttributes.getRequest();

            // 获取请求 URI
            String uri = request.getRequestURI();

            // ========== 格式化响应结果 ==========
            // 使用 PrettyFormat 格式化 JSON，提高日志可读性
            String pretty = JSON.toJSONString(
                    JSONObject.parseObject(JSON.toJSONString(o)),
                    JSONWriter.Feature.PrettyFormat
            );

            // ========== 打印响应日志 ==========
            log.info("uri: {}  result: \n{}", request.getRequestURI(), pretty);

            // ========== 计算并打印请求耗时 ==========
            Long startTime = requestCostThreadLocal.get();
            if (startTime != null) {
                log.info("uri: {}  请求耗时: {} ms", uri, System.currentTimeMillis() - startTime);
            }

        } catch (Exception e) {
            log.error("请求日志切面后置处理异常:", e);
        } finally {
            // 清理 ThreadLocal，防止内存泄漏
            requestCostThreadLocal.remove();
        }
    }
}