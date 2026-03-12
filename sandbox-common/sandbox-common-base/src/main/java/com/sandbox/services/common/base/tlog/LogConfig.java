package com.sandbox.services.common.base.tlog;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * @description: TLog链路追踪框架的配置类，负责初始化TLog相关的组件和配置。
 * 主要功能包括：
 * 1. 通过@ComponentScan扫描并加载TLog框架的核心组件
 * 2. 注册自定义的TLogFilter过滤器到Spring容器
 * 3. 配置过滤器的拦截规则为所有请求路径("/*")
 * 4. 设置过滤器为最高优先级(Ordered.HIGHEST_PRECEDENCE)，确保在请求处理链路的最开始就生成或传递TraceId
 * 这样配置可以保证在请求进入业务处理前就完成链路追踪信息的初始化，实现全链路的日志追踪能力
 * @author: 0101
 * @create: 2026/3/12
 */
@Configuration
@ComponentScan(value = "com.yomahub.tlog")
public class LogConfig {

    @Bean
    public FilterRegistrationBean<TLogFilter> loggingFilter() {
        FilterRegistrationBean<TLogFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new TLogFilter());
        registrationBean.addUrlPatterns("/*"); // 拦截所有请求路径
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registrationBean;
    }
}
