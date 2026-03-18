package com.sandbox.services.common.base.tlog;

import com.yomahub.tlog.constant.TLogConstants;
import com.yomahub.tlog.context.TLogContext;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * TLog 链路追踪 Servlet 过滤器
 *
 * <p>该类实现了 Jakarta Servlet 的 {@link Filter} 接口，是 TLog 分布式链路追踪框架
 * 在 HTTP 请求层面的核心组件。它负责在每个 HTTP 请求的入口和出口处处理链路追踪信息的
 * 传递、生成和清理，确保一次请求在整个调用链中的所有日志都能关联到同一个 TraceId。
 *
 * <p><b>核心功能：</b>
 * <ul>
 *   <li><b>链路信息提取：</b>从 HTTP 请求头中提取上游服务传递的 TraceId、SpanId 等信息</li>
 *   <li><b>链路信息生成：</b>如果请求头中没有 TraceId，则生成新的全局唯一 TraceId</li>
 *   <li><b>响应头传递：</b>将当前 TraceId 添加到 HTTP 响应头中，便于前端或其他服务获取</li>
 *   <li><b>上下文清理：</b>请求处理完成后清理线程上下文中的链路信息，防止内存泄漏</li>
 *   <li><b>无缝集成：</b>与 TLog 框架的 {@link TLogWebCommon} 组件配合，完成具体操作</li>
 * </ul>
 *
 * <p><b>工作流程：</b>
 * <ol>
 *   <li><b>前置处理：</b>调用 {@link TLogWebCommon#preHandle(HttpServletRequest)}，
 *       从请求头中提取 TraceId 并设置到当前线程的 TLog 上下文中</li>
 *   <li><b>获取 TraceId：</b>从 {@link TLogContext} 中获取当前请求的 TraceId</li>
 *   <li><b>响应头设置：</b>将 TraceId 添加到 HTTP 响应头中（键为 {@link TLogConstants#TLOG_TRACE_KEY}）</li>
 *   <li><b>继续请求：</b>执行过滤器链，进入后续的业务处理</li>
 *   <li><b>后置清理：</b>在 finally 块中调用 {@link TLogWebCommon#afterCompletion()}，
 *       清理线程上下文中的链路信息，避免线程复用导致的问题</li>
 * </ol>
 *
 * <p><b>HTTP 头说明：</b>
 * <ul>
 *   <li><b>请求头：</b>上游服务应在请求头中传递 TraceId，默认头名为 "tlogTraceId"</li>
 *   <li><b>响应头：</b>本服务会将 TraceId 写入响应头，方便调用方获取整条链路的 TraceId</li>
 * </ul>
 *
 * <p><b>使用场景：</b>
 * <ul>
 *   <li><b>HTTP 入口：</b>所有 HTTP 请求的入口处都需要此过滤器</li>
 *   <li><b>微服务调用：</b>服务 A 调用服务 B 时，服务 A 需要在 HTTP 头中传递 TraceId</li>
 *   <li><b>前端调试：</b>前端可以从响应头中获取 TraceId，在向后端反馈问题时提供</li>
 *   <li><b>日志串联：</b>通过 TraceId 将一次请求经过的所有服务日志串联起来</li>
 * </ul>
 *
 * <p><b>设计要点：</b>
 * <ul>
 *   <li><b>类型检查：</b>先检查 ServletRequest 和 ServletResponse 是否为 HTTP 类型，
 *       避免在非 HTTP 环境下出错</li>
 *   <li><b>异常安全：</b>使用 try-finally 确保即使在业务处理抛出异常时，也能执行上下文清理</li>
 *   <li><b>响应头传递：</b>将 TraceId 放入响应头，方便下游服务或前端获取</li>
 *   <li><b>与 TLog 框架集成：</b>使用 {@link TLogWebCommon#loadInstance()} 获取 TLog 的 Web 组件实例</li>
 * </ul>
 *
 * <p><b>注意事项：</b>
 * <ul>
 *   <li>该过滤器需要在 {@link LogConfig} 中注册，并设置为最高优先级</li>
 *   <li>如果使用异步 Servlet，需要在异步线程中手动传递 TraceId</li>
 *   <li>响应头中的 TraceId 可能会暴露内部信息，生产环境可根据需要决定是否添加</li>
 *   <li>清理操作必须在 finally 块中执行，确保线程复用不会导致 TraceId 错乱</li>
 *   <li>如果请求不是 HTTP 请求（如 WebSocket），此过滤器会直接放行</li>
 * </ul>
 *
 * @author 0101
 * @see TLogWebCommon
 * @see TLogContext
 * @see TLogConstants
 * @see LogConfig
 * @since 2026-03-12
 */
public class TLogFilter implements Filter {

    /**
     * 执行过滤操作
     *
     * <p>处理 HTTP 请求的链路追踪信息，包含以下步骤：
     * <ul>
     *   <li>提取或生成 TraceId</li>
     *   <li>将 TraceId 放入响应头</li>
     *   <li>执行后续过滤器链</li>
     *   <li>清理线程上下文</li>
     * </ul>
     *
     * @param servletRequest  Servlet 请求对象
     * @param servletResponse Servlet 响应对象
     * @param filterChain     过滤器链
     * @throws IOException      I/O 异常
     * @throws ServletException Servlet 异常
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {

        // 检查是否为 HTTP 请求和响应
        if (servletRequest instanceof HttpServletRequest && servletResponse instanceof HttpServletResponse) {
            HttpServletRequest request = (HttpServletRequest) servletRequest;
            HttpServletResponse response = (HttpServletResponse) servletResponse;

            try {
                // 1. 前置处理：从请求头中提取 TraceId 并设置到上下文
                //    如果请求头中没有 TraceId，TLog 会自动生成新的
                TLogWebCommon.loadInstance().preHandle(request);

                // 2. 获取当前线程上下文中的 TraceId
                String traceId = TLogContext.getTraceId();

                // 3. 将 TraceId 添加到响应头中，方便调用方获取整条链路的 TraceId
                //    头名称使用 TLogConstants.TLOG_TRACE_KEY，默认为 "tlogTraceId"
                response.addHeader(TLogConstants.TLOG_TRACE_KEY, traceId);

                // 4. 继续执行过滤器链，进入后续业务处理
                filterChain.doFilter(servletRequest, servletResponse);

                // 提前返回，避免重复执行 filterChain
                return;
            } finally {
                // 5. 后置清理：无论业务处理是否成功，都清理线程上下文中的链路信息
                //    这是防止线程复用导致 TraceId 错乱的关键步骤
                TLogWebCommon.loadInstance().afterCompletion();
            }
        }

        // 非 HTTP 请求（如 WebSocket），直接放行
        filterChain.doFilter(servletRequest, servletResponse);
    }
}