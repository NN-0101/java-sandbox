package com.sandbox.services.living.utils;

import com.sandbox.services.living.security.user.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全上下文工具类
 *
 * <p>该类提供了对当前登录用户信息的便捷访问方法，封装了从 Spring Security
 * {@link SecurityContextHolder} 中获取认证信息的重复代码。通过该工具类，
 * 业务层和控制器层可以轻松获取当前请求的用户信息，而无需直接操作安全上下文。
 *
 * <p><b>核心功能：</b>
 * <ul>
 *   <li><b>获取用户ID：</b>快速获取当前登录用户的唯一标识</li>
 *   <li><b>获取手机号：</b>快速获取当前登录用户的手机号（用户名）</li>
 *   <li><b>获取完整用户详情：</b>获取 {@link CustomUserDetails} 对象，包含用户所有信息</li>
 *   <li><b>认证状态检查：</b>判断当前请求是否已通过认证</li>
 * </ul>
 *
 * <p><b>使用场景：</b>
 * <ul>
 *   <li><b>审计日志：</b>记录操作日志时需要记录操作人ID</li>
 *   <li><b>数据权限过滤：</b>查询数据时根据用户ID进行过滤</li>
 *   <li><b>业务逻辑判断：</b>根据用户角色或权限执行不同逻辑</li>
 *   <li><b>Controller 层：</b>获取当前用户信息用于响应</li>
 *   <li><b>Service 层：</b>获取当前用户信息作为业务参数</li>
 * </ul>
 *
 * <p><b>设计要点：</b>
 * <ul>
 *   <li><b>空安全：</b>所有方法都进行了空值检查，避免 NullPointerException</li>
 *   <li><b>类型安全：</b>对 {@link SecurityContextHolder} 返回的对象进行类型检查</li>
 *   <li><b>便捷性：</b>封装重复的获取代码，提供语义清晰的方法名</li>
 *   <li><b>单例无状态：</b>工具类设计为静态方法，无需实例化</li>
 * </ul>
 *
 * <p><b>工作原理：</b>
 * <ol>
 *   <li>请求经过 {@link com.sandbox.services.living.security.filter.CustomAuthenticationFilter} 时，
 *       会解析 JWT 并将认证信息存入 {@link SecurityContextHolder}</li>
 *   <li>{@link SecurityContextHolder} 使用 ThreadLocal 存储当前线程的认证信息</li>
 *   <li>在同一个请求的任何地方，都可以通过 {@code SecurityContextHolder.getContext().getAuthentication()}
 *       获取当前用户的认证信息</li>
 *   <li>本工具类在此基础上提供了更高层次的封装</li>
 * </ol>
 *
 * <p><b>注意事项：</b>
 * <ul>
 *   <li>在未认证的请求中调用 {@link #getCurrentUserId()} 等方法会返回 null，调用方需要做好空值处理</li>
 *   <li>不要在异步线程中直接使用此工具类，因为 {@link SecurityContextHolder} 默认不会传播到子线程，
 *       如需使用需手动传递或配置 {@link org.springframework.security.core.context.SecurityContextHolderStrategy}</li>
 *   <li>单元测试时，可以通过 {@link SecurityContextHolder#setContext(SecurityContext)} 设置模拟的认证信息</li>
 * </ul>
 *
 * @author 0101
 * @see SecurityContextHolder
 * @see CustomUserDetails
 * @see com.sandbox.services.living.security.filter.CustomAuthenticationFilter
 * @since 2026-03-14
 */
public class SecurityUtils {

    /**
     * 私有构造方法，防止实例化
     *
     * <p>工具类不应被实例化，所有方法均为静态方法。
     */
    private SecurityUtils() {
        throw new UnsupportedOperationException("工具类不能被实例化");
    }

    /**
     * 获取当前登录用户的ID
     *
     * <p>如果当前请求已认证且 principal 是 {@link CustomUserDetails} 类型，
     * 则返回用户的 userId；否则返回 null。
     *
     * <p><b>使用示例：</b>
     * <pre>
     * String userId = SecurityUtils.getCurrentUserId();
     * if (userId != null) {
     *     // 使用用户ID进行业务处理
     * }
     * </pre>
     *
     * @return 当前用户的ID，如果用户未认证或类型不匹配则返回 null
     */
    public static String getCurrentUserId() {
        CustomUserDetails userDetails = getCurrentUser();
        return userDetails != null ? userDetails.getUserId() : null;
    }

    /**
     * 获取当前登录用户的手机号
     *
     * <p>如果当前请求已认证且 principal 是 {@link CustomUserDetails} 类型，
     * 则返回用户的手机号；否则返回 null。
     *
     * <p>手机号在系统中作为登录用户名（principal），同时也是用户的联系方式。
     *
     * @return 当前用户的手机号，如果用户未认证或类型不匹配则返回 null
     */
    public static String getCurrentPhone() {
        CustomUserDetails userDetails = getCurrentUser();
        return userDetails != null ? userDetails.getPhone() : null;
    }

    /**
     * 获取当前登录用户的完整详情对象
     *
     * <p>从 {@link SecurityContextHolder} 中获取当前认证信息，并尝试转换为
     * {@link CustomUserDetails} 类型。如果认证信息不存在或类型不匹配，返回 null。
     *
     * <p>该方法返回的对象包含了用户的所有信息：
     * <ul>
     *   <li>用户ID、手机号</li>
     *   <li>角色列表、权限列表</li>
     *   <li>账号状态（是否启用、是否锁定等）</li>
     * </ul>
     *
     * @return {@link CustomUserDetails} 对象，如果用户未认证或类型不匹配则返回 null
     */
    public static CustomUserDetails getCurrentUser() {
        // 获取当前认证信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 如果认证信息不存在，返回 null
        if (authentication == null) {
            return null;
        }

        // 获取 principal 对象
        Object principal = authentication.getPrincipal();

        // 检查 principal 是否为 CustomUserDetails 类型
        if (principal instanceof CustomUserDetails) {
            return (CustomUserDetails) principal;
        }

        // 类型不匹配时返回 null（可能是匿名用户或其他认证类型）
        return null;
    }

    /**
     * 判断当前请求是否已通过认证
     *
     * <p>检查 {@link SecurityContextHolder} 中是否存在有效的认证信息，
     * 且认证状态为 true。
     *
     * <p><b>注意：</b>已认证（authenticated）不等于用户已登录。
     * 对于匿名用户，Spring Security 也会创建 Authentication 对象，
     * 但其 authenticated 状态为 true，principal 通常是 "anonymousUser"。
     * 如果需要区分匿名用户和已登录用户，可以结合 {@link #getCurrentUser()} 判断。
     *
     * @return true 表示存在认证信息且已通过认证；false 表示未认证
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }
}