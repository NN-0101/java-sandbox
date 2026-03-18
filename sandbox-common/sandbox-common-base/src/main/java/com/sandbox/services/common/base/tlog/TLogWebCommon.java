package com.sandbox.services.common.base.tlog;

import com.yomahub.tlog.constant.TLogConstants;
import com.yomahub.tlog.core.rpc.TLogLabelBean;
import com.yomahub.tlog.core.rpc.TLogRPCHandler;
import jakarta.servlet.http.HttpServletRequest;

/**
 * TLog 框架的 Web 端核心处理类
 *
 * <p>该类继承自 {@link TLogRPCHandler}，采用单例模式实现，是 TLog 分布式链路追踪框架
 * 在 Web 环境中的核心适配器。它负责在 HTTP 请求层面处理链路追踪信息的提取、绑定和清理，
 * 连接了 HTTP 协议和日志追踪框架，确保分布式请求的日志能够正确串联。
 *
 * <p><b>核心功能：</b>
 * <ul>
 *   <li><b>信息提取：</b>从 HTTP 请求头中提取上游服务传递的 TraceId、SpanId、调用方应用等信息</li>
 *   <li><b>信息绑定：</b>将提取到的链路信息封装为 {@link TLogLabelBean} 并绑定到当前线程的上下文中</li>
 *   <li><b>上下文清理：</b>请求处理完成后清理线程的 ThreadLocal 变量，防止内存泄漏和线程复用问题</li>
 *   <li><b>单例管理：</b>通过双重检查锁机制提供线程安全的单例实例</li>
 * </ul>
 *
 * <p><b>工作流程：</b>
 * <ol>
 *   <li><b>请求前置处理 ({@link #preHandle(HttpServletRequest)})：</b>
 *     <ul>
 *       <li>从 HTTP 请求头中提取链路追踪信息：TraceId、SpanId、调用方应用、调用方主机、调用方 IP</li>
 *       <li>将提取的信息封装为 {@link TLogLabelBean} 对象</li>
 *       <li>调用父类的 {@link #processProviderSide(TLogLabelBean)} 方法将信息绑定到当前线程</li>
 *     </ul>
 *   </li>
 *   <li><b>请求后置处理 ({@link #afterCompletion()})：</b>
 *     <ul>
 *       <li>调用 {@link #cleanThreadLocal()} 清理当前线程的 ThreadLocal 变量</li>
 *       <li>确保下一个请求不会错误地复用上一个请求的链路信息</li>
 *     </ul>
 *   </li>
 * </ol>
 *
 * <p><b>HTTP 请求头说明：</b>
 * <table border="1">
 *   <tr>
 *     <th>常量名</th>
 *     <th>请求头名称</th>
 *     <th>说明</th>
 *   </tr>
 *   <tr>
 *     <td>{@link TLogConstants#TLOG_TRACE_KEY}</td>
 *     <td>tlogTraceId</td>
 *     <td>全局唯一的追踪 ID</td>
 *   </tr>
 *   <tr>
 *     <td>{@link TLogConstants#TLOG_SPANID_KEY}</td>
 *     <td>tlogSpanId</td>
 *     <td>当前节点在调用链中的位置</td>
 *   </tr>
 *   <tr>
 *     <td>{@link TLogConstants#PRE_IVK_APP_KEY}</td>
 *     <td>preIvkApp</td>
 *     <td>调用方应用名称</td>
 *   </tr>
 *   <tr>
 *     <td>{@link TLogConstants#PRE_IVK_APP_HOST}</td>
 *     <td>preIvkHost</td>
 *     <td>调用方主机名</td>
 *   </tr>
 *   <tr>
 *     <td>{@link TLogConstants#PRE_IP_KEY}</td>
 *     <td>preIp</td>
 *     <td>调用方 IP 地址</td>
 *   </tr>
 * </table>
 *
 * <p><b>使用场景：</b>
 * <ul>
 *   <li><b>HTTP 入口：</b>在 {@link TLogFilter} 中调用此类处理 HTTP 请求的链路信息</li>
 *   <li><b>微服务调用：</b>服务提供方需要从请求头中提取调用方信息，用于日志记录和问题排查</li>
 *   <li><b>链路追踪初始化：</b>作为分布式链路追踪的入口点，初始化当前节点的追踪信息</li>
 * </ul>
 *
 * <p><b>设计要点：</b>
 * <ul>
 *   <li><b>单例模式：</b>使用双重检查锁 (DCL) 实现线程安全的懒加载单例，避免频繁创建对象</li>
 *   <li><b>继承设计：</b>继承 {@link TLogRPCHandler} 复用其线程上下文管理能力</li>
 *   <li><b>无状态：</b>实例本身无状态，所有状态都存储在 ThreadLocal 中，适合单例</li>
 *   <li><b>防御性编程：</b>如果请求头中的某些字段为空，TLog 框架会自动生成默认值</li>
 * </ul>
 *
 * <p><b>注意事项：</b>
 * <ul>
 *   <li>{@link #preHandle(HttpServletRequest)} 和 {@link #afterCompletion()} 必须成对调用</li>
 *   <li>必须在请求完成后调用 {@link #afterCompletion()}，否则会导致线程复用时的信息错乱</li>
 *   <li>如果请求头中没有 TraceId，TLog 框架会自动生成新的 TraceId</li>
 *   <li>在多线程环境下，子线程需要手动传递 TraceId（TLog 提供了相应的工具类）</li>
 * </ul>
 *
 * @author 0101
 * @see TLogRPCHandler
 * @see TLogLabelBean
 * @see TLogConstants
 * @see TLogFilter
 * @since 2026-03-12
 */
public class TLogWebCommon extends TLogRPCHandler {

    /**
     * 单例实例，使用 volatile 保证可见性
     */
    private static volatile TLogWebCommon instance;

    /**
     * 私有构造方法，防止外部直接实例化
     */
    private TLogWebCommon() {
        // 私有构造方法，符合单例模式
    }

    /**
     * 获取单例实例（双重检查锁）
     *
     * <p>使用双重检查锁机制保证线程安全的同时，也保证了延迟加载的性能。
     *
     * @return TLogWebCommon 单例实例
     */
    public static TLogWebCommon loadInstance() {
        if (instance == null) {
            synchronized (TLogWebCommon.class) {
                if (instance == null) {
                    instance = new TLogWebCommon();
                }
            }
        }
        return instance;
    }

    /**
     * 请求前置处理
     *
     * <p>在 HTTP 请求处理前调用，从请求头中提取链路追踪信息并绑定到当前线程。
     *
     * <p><b>处理步骤：</b>
     * <ol>
     *   <li>从 {@link HttpServletRequest} 的请求头中获取 TraceId、SpanId、调用方应用等信息</li>
     *   <li>将获取到的信息封装为 {@link TLogLabelBean} 对象</li>
     *   <li>调用父类的 {@link #processProviderSide(TLogLabelBean)} 方法，
     *       将链路信息绑定到当前线程的 ThreadLocal 中</li>
     * </ol>
     *
     * <p><b>请求头缺失处理：</b>
     * 如果某个请求头缺失（如调用方信息），对应的字段会为 null，TLog 框架会进行相应处理：
     * <ul>
     *   <li>TraceId 缺失：自动生成新的 TraceId</li>
     *   <li>SpanId 缺失：自动生成根节点的 SpanId</li>
     *   <li>调用方信息缺失：记录为 null 或空字符串</li>
     * </ul>
     *
     * @param request HTTP 请求对象，包含需要提取的请求头信息
     */
    public void preHandle(HttpServletRequest request) {
        // 从请求头中提取链路追踪信息
        String traceId = request.getHeader(TLogConstants.TLOG_TRACE_KEY);
        String spanId = request.getHeader(TLogConstants.TLOG_SPANID_KEY);
        String preIvkApp = request.getHeader(TLogConstants.PRE_IVK_APP_KEY);
        String preIvkHost = request.getHeader(TLogConstants.PRE_IVK_APP_HOST);
        String preIp = request.getHeader(TLogConstants.PRE_IP_KEY);

        // 封装为 TLogLabelBean
        TLogLabelBean labelBean = new TLogLabelBean(preIvkApp, preIvkHost, preIp, traceId, spanId);

        // 调用父类方法，将链路信息绑定到当前线程
        processProviderSide(labelBean);
    }

    /**
     * 请求后置处理
     *
     * <p>在 HTTP 请求处理完成后调用，清理当前线程的 ThreadLocal 变量。
     * 这是防止线程复用导致链路信息错乱的关键步骤。
     *
     * <p><b>清理内容：</b>
     * <ul>
     *   <li>当前线程的 TraceId</li>
     *   <li>当前线程的 SpanId</li>
     *   <li>当前线程的调用方信息</li>
     *   <li>其他 TLog 相关的 ThreadLocal 变量</li>
     * </ul>
     */
    public void afterCompletion() {
        // 调用父类方法清理 ThreadLocal
        cleanThreadLocal();
    }
}