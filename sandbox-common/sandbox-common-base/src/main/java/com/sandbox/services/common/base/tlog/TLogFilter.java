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
 * @description: TLog链路追踪的Servlet过滤器，实现jakarta.servlet.Filter接口，
 * 负责在HTTP请求层面处理链路追踪信息的传递和生成。
 * 核心功能：
 * 1. 在请求进入时调用TLogWebCommon.preHandle()方法，从请求头中提取TraceId、
 * SpanId等链路信息，如果不存在则生成新的TraceId
 * 2. 将当前请求的TraceId添加到HTTP响应头中，方便前端或其他服务获取
 * 3. 执行过滤器链继续处理请求
 * 4. 在请求处理完成后（finally块中）调用TLogWebCommon.afterCompletion()
 * 清理线程上下文中的链路信息，防止内存泄漏
 * 该过滤器确保在同一个请求链路中的所有日志都能关联到同一个TraceId
 * @author: 0101
 * @create: 2026/3/12
 */
public class TLogFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (servletRequest instanceof HttpServletRequest && servletResponse instanceof HttpServletResponse) {
            try {
                TLogWebCommon.loadInstance().preHandle((HttpServletRequest) servletRequest);
                String traceId = TLogContext.getTraceId();
                //把traceId放入response的header，为了方便有些人有这样的需求，从前端拿整条链路的traceId
                ((HttpServletResponse) servletResponse).addHeader(TLogConstants.TLOG_TRACE_KEY, traceId);
                filterChain.doFilter(servletRequest, servletResponse);
                return;
            } finally {
                TLogWebCommon.loadInstance().afterCompletion();
            }
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }
}
