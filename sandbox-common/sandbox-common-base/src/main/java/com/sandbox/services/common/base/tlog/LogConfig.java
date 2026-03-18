package com.sandbox.services.common.base.tlog;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * TLog 链路追踪框架配置类
 *
 * <p>该类负责初始化 TLog 分布式链路追踪框架的相关组件和配置。
 * TLog 是一款轻量级的分布式日志追踪框架，能够自动为每个请求生成全局唯一的 TraceId，
 * 并在整个调用链（包括 HTTP 请求、RPC 调用、消息队列等）中传递，从而实现分布式系统的日志串联。
 *
 * <p><b>核心功能：</b>
 * <ul>
 *   <li><b>组件扫描：</b>通过 {@link ComponentScan} 扫描并加载 TLog 框架的核心组件</li>
 *   <li><b>过滤器注册：</b>注册自定义的 {@link TLogFilter} 到 Spring 容器</li>
 *   <li><b>拦截规则：</b>配置过滤器拦截所有请求路径 ("/*")，确保每个请求都被处理</li>
 *   <li><b>优先级设置：</b>设置过滤器为最高优先级 ({@link Ordered#HIGHEST_PRECEDENCE})，
 *       确保在请求处理链路的最开始就生成或传递 TraceId</li>
 * </ul>
 *
 * <p><b>工作流程：</b>
 * <ol>
 *   <li>HTTP 请求到达应用时，{@link TLogFilter} 以最高优先级拦截请求</li>
 *   <li>过滤器检查请求头中是否包含 TraceId（如果是从上游服务过来的调用）</li>
 *   <li>如果请求头中有 TraceId，则将其设置到当前线程的 TLog 上下文中</li>
 *   <li>如果没有，则生成新的 TraceId 并设置到上下文中</li>
 *   <li>TraceId 在整个请求处理过程中通过 TLog 的 MDC 机制与日志绑定</li>
 *   <li>所有日志输出都会自动包含 TraceId，实现全链路的日志串联</li>
 *   <li>请求结束后，过滤器负责清理上下文，避免内存泄漏</li>
 * </ol>
 *
 * <p><b>使用场景：</b>
 * <ul>
 *   <li><b>分布式系统调试：</b>通过 TraceId 串联一次请求经过的所有服务日志</li>
 *   <li><b>性能分析：</b>追踪请求在各服务间的耗时分布</li>
 *   <li><b>异常排查：</b>快速定位问题发生在调用链的哪个环节</li>
 *   <li><b>全链路监控：</b>与监控系统集成，实时分析系统健康状况</li>
 * </ul>
 *
 * <p><b>日志配置示例（logback-spring.xml）：</b>
 * <pre>
 * &lt;pattern&gt;%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - [%X{traceId}] %msg%n&lt;/pattern&gt;
 * </pre>
 *
 * <p><b>HTTP 头传递示例：</b>
 * <pre>
 * // 上游服务在调用下游服务时，需要在请求头中传递 TraceId
 * headers.put("tlogTraceId", TLogContext.getTraceId());
 * </pre>
 *
 * <p><b>设计要点：</b>
 * <ul>
 *   <li><b>无侵入：</b>TLog 通过过滤器自动管理 TraceId，业务代码无需关心</li>
 *   <li><b>高优先级：</b>确保 TraceId 在请求处理的最早期就被初始化</li>
 *   <li><b>全路径覆盖：</b>拦截所有请求路径 ("/*")，不遗漏任何请求</li>
 *   <li><b>自动清理：</b>过滤器在请求结束后自动清理上下文，防止线程复用导致的内存泄漏</li>
 *   <li><b>与 Spring Boot 集成：</b>通过 {@link FilterRegistrationBean} 注册过滤器，充分利用 Spring Boot 的自动配置能力</li>
 * </ul>
 *
 * <p><b>注意事项：</b>
 * <ul>
 *   <li>过滤器必须设置为最高优先级，否则可能错过在更早阶段生成的 TraceId</li>
 *   <li>如果使用了网关（如 Zuul、Gateway），需要在网关层也配置 TLog 过滤器以传递 TraceId</li>
 *   <li>异步线程中需要手动传递 TraceId（TLog 提供了相应的工具类）</li>
 *   <li>确保日志配置中正确使用了 {@code %X{traceId}} 来输出 TraceId</li>
 * </ul>
 *
 * @author 0101
 * @see TLogFilter
 * @see org.springframework.boot.web.servlet.FilterRegistrationBean
 * @since 2026-03-12
 */
@Configuration
@ComponentScan(value = "com.yomahub.tlog")
public class LogConfig {

    /**
     * 注册 TLog 过滤器
     *
     * <p>创建并配置 {@link FilterRegistrationBean}，将自定义的 {@link TLogFilter}
     * 注册到 Spring 的过滤器链中。
     *
     * <p><b>配置说明：</b>
     * <ul>
     *   <li><b>setFilter：</b>设置要注册的过滤器实例</li>
     *   <li><b>addUrlPatterns：</b>设置拦截的 URL 模式，"/*" 表示拦截所有请求</li>
     *   <li><b>setOrder：</b>设置过滤器执行顺序，{@link Ordered#HIGHEST_PRECEDENCE} 确保最先执行</li>
     * </ul>
     *
     * @return 配置好的过滤器注册 Bean
     */
    @Bean
    public FilterRegistrationBean<TLogFilter> loggingFilter() {
        FilterRegistrationBean<TLogFilter> registrationBean = new FilterRegistrationBean<>();

        // 设置过滤器实例
        registrationBean.setFilter(new TLogFilter());

        // 设置拦截规则：拦截所有请求路径
        registrationBean.addUrlPatterns("/*");

        // 设置最高优先级，确保在请求处理的最开始就生成或传递 TraceId
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);

        return registrationBean;
    }
}