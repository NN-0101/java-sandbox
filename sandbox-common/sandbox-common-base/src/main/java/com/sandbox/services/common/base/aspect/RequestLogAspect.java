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
 * @description: 请求日志切面 打印请求参数
 * @author: 0101
 * @create: 2026/3/12
 */
@Aspect
@Order(1)
@Component
public class RequestLogAspect {

    private static final Logger log = LoggerFactory.getLogger(RequestLogAspect.class);

    private final ThreadLocal<Long> requestCostThreadLocal = new ThreadLocal<>();

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    private void controllerAspect() {
    }

    /**
     * methodBefore
     *
     * @param joinPoint joinPoint
     */
    @Before(value = "controllerAspect()")
    public void methodBefore(JoinPoint joinPoint) {
        try {
            requestCostThreadLocal.set(System.currentTimeMillis());

            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();
            assert requestAttributes != null;
            HttpServletRequest request = requestAttributes.getRequest();
            String uri = request.getRequestURI();
            String method = request.getMethod();
            //打印请求头
            Enumeration<String> headerNames = request.getHeaderNames();
            Map<String, String> headerMap = new HashMap<>(16);
            //判断是否还有下一个元素
            while (headerNames.hasMoreElements()) {
                //获取headerNames集合中的请求头
                String nextElement = headerNames.nextElement();
                //通过请求头得到请求内容
                String value = request.getHeader(nextElement);
                headerMap.put(nextElement, value);
            }
            StringBuilder builder = new StringBuilder();
            for (Object arg : joinPoint.getArgs()) {
                if (arg instanceof HttpServletResponse) {
                    continue;
                }
                if (arg instanceof HttpServletRequest) {
                    continue;
                }
                if (arg instanceof MultipartFile) {
                    builder.append(arg);
                } else {
                    builder.append(JSONObject.toJSONString(arg));
                    builder.append(",");
                }
            }
            //如果接口没有入参 添加一个逗号 防止下标越界
            if (builder.length() <= 0) {
                builder.append(",");
            }
            log.info("uri: {}  method:{}  params: {}  body:{}  headers: {}", uri, method, JSON.toJSONString(request.getParameterMap()), builder.substring(0, builder.length() - 1), JSONObject.toJSONString(headerMap));

        } catch (Exception e) {
            log.error("###RequestLogAspect.class methodBefore() ### ERROR:", e);
        }
    }

    /**
     * methodAfterReturning
     *
     * @param o 参数
     */
    @AfterReturning(returning = "o", pointcut = "controllerAspect()")
    public void methodAfterReturning(Object o) {
        try {
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();
            assert requestAttributes != null;
            HttpServletRequest request = requestAttributes.getRequest();
            String uri = request.getRequestURI();

            String pretty = JSON.toJSONString(JSONObject.parseObject(JSON.toJSONString(o)), JSONWriter.Feature.PrettyFormat);

            log.info("uri: {}  result: \n{}", request.getRequestURI(), pretty);
            log.info("uri: {}  请求耗时: {}", uri, System.currentTimeMillis() - requestCostThreadLocal.get());
            requestCostThreadLocal.remove();
        } catch (Exception e) {
            log.error("###RequestLogAspect.class methodAfterReturning() ### ERROR:", e);
        }
    }
}

