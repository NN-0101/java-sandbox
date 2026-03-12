package com.sandbox.services.common.base.tlog;

import com.yomahub.tlog.constant.TLogConstants;
import com.yomahub.tlog.core.rpc.TLogLabelBean;
import com.yomahub.tlog.core.rpc.TLogRPCHandler;
import jakarta.servlet.http.HttpServletRequest;

/**
 * @description: TLog框架的Web端核心处理类，继承自TLogRPCHandler，
 * 采用单例模式实现，负责HTTP请求维度的链路追踪信息处理。
 * 主要方法：
 * 1. loadInstance()：双重检查锁实现的线程安全单例获取方法
 * 2. preHandle(HttpServletRequest request)：在请求处理前执行，
 * 从HTTP请求头中提取TLog相关的追踪信息（TraceId、SpanId、调用方应用、主机、IP等），
 * 封装成TLogLabelBean并调用父类的processProviderSide()方法将信息绑定到当前线程
 * 3. afterCompletion()：在请求完成后执行，调用cleanThreadLocal()
 * 清理当前线程的ThreadLocal变量，防止线程复用导致的信息错乱
 * 该类是TLog在Web环境中的核心适配器，连接了HTTP协议和日志追踪框架
 * @author: 0101
 * @create: 2026/3/12
 */
public class TLogWebCommon extends TLogRPCHandler {
    private static volatile TLogWebCommon tLogWebCommon;

    public static TLogWebCommon loadInstance() {
        if (tLogWebCommon == null) {
            synchronized (TLogWebCommon.class) {
                if (tLogWebCommon == null) {
                    tLogWebCommon = new TLogWebCommon();
                }
            }
        }
        return tLogWebCommon;
    }

    public void preHandle(HttpServletRequest request) {
        String traceId = request.getHeader(TLogConstants.TLOG_TRACE_KEY);
        String spanId = request.getHeader(TLogConstants.TLOG_SPANID_KEY);
        String preIvkApp = request.getHeader(TLogConstants.PRE_IVK_APP_KEY);
        String preIvkHost = request.getHeader(TLogConstants.PRE_IVK_APP_HOST);
        String preIp = request.getHeader(TLogConstants.PRE_IP_KEY);

        TLogLabelBean labelBean = new TLogLabelBean(preIvkApp, preIvkHost, preIp, traceId, spanId);

        processProviderSide(labelBean);
    }

    public void afterCompletion() {
        cleanThreadLocal();
    }
}