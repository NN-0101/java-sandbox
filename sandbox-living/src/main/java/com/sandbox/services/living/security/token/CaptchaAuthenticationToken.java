package com.sandbox.services.living.security.token;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityCoreVersion;

import java.io.Serial;
import java.util.Collection;

/**
 * 验证码认证令牌
 *
 * <p>封装基于手机号和验证码的认证信息，是 {@link org.springframework.security.core.Authentication}
 * 接口的自定义实现。该令牌贯穿整个验证码认证流程，用于在认证各组件间传递认证数据。
 *
 * <p><b>两种状态：</b>
 * <ul>
 *   <li><b>未认证状态：</b>使用 {@link #CaptchaAuthenticationToken(Object, Object)} 构造函数创建，
 *       此时仅包含手机号和验证码，权限为空，authenticated = false</li>
 *   <li><b>已认证状态：</b>使用 {@link #CaptchaAuthenticationToken(Object, Object, Collection)}
 *       构造函数创建，包含完整的用户信息和权限，authenticated = true</li>
 * </ul>
 *
 * <p><b>数据存储：</b>
 * <ul>
 *   <li><b>principal：</b>存储手机号（未认证）或 {@link com.sandbox.services.living.security.user.CustomUserDetails}（已认证）</li>
 *   <li><b>credentials：</b>存储验证码（认证完成后会通过 {@link #eraseCredentials()} 擦除）</li>
 *   <li><b>authorities：</b>存储用户权限（仅在已认证状态存在）</li>
 * </ul>
 *
 * <p><b>使用场景：</b>
 * <ol>
 *   <li><b>创建未认证令牌：</b>登录请求到达 {@link com.sandbox.services.living.security.filter.CustomAuthenticationFilter}，
 *       解析请求中的手机号和验证码，创建未认证令牌并传递给 {@link org.springframework.security.authentication.AuthenticationManager}</li>
 *   <li><b>认证处理：</b>{@link com.sandbox.services.living.security.provider.CaptchaAuthenticationProvider}
 *       接收令牌，验证手机号和验证码的有效性，验证成功后返回已认证令牌</li>
 *   <li><b>存储认证结果：</b>已认证令牌被存入 {@link org.springframework.security.core.context.SecurityContextHolder}，
 *       供后续请求使用</li>
 *   <li><b>凭证擦除：</b>认证完成后，调用 {@link #eraseCredentials()} 清除验证码，避免敏感信息在内存中滞留</li>
 * </ol>
 *
 * <p><b>设计要点：</b>
 * <ul>
 *   <li>继承 {@link AbstractAuthenticationToken}，复用 Spring Security 提供的认证令牌基础功能</li>
 *   <li>实现 serialVersionUID，确保在不同 JVM 版本间的序列化兼容性</li>
 *   <li>遵循 Spring Security 的设计规范：未认证时 credentials 存储敏感信息，认证后及时擦除</li>
 *   <li>提供清晰的构造函数区分，避免开发者错误地创建不完整的认证令牌</li>
 * </ul>
 *
 * @author 0101
 * @see AbstractAuthenticationToken
 * @see com.sandbox.services.living.security.provider.CaptchaAuthenticationProvider
 * @see com.sandbox.services.living.security.filter.CustomAuthenticationFilter
 * @see org.springframework.security.core.Authentication
 * @since 2026-03-14
 */
public class CaptchaAuthenticationToken extends AbstractAuthenticationToken {

    /**
     * 序列化版本号，确保序列化兼容性
     */
    @Serial
    private static final long serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID;

    /**
     * 主体信息
     *
     * <p>在认证流程的不同阶段，此字段存储不同的内容：
     * <ul>
     *   <li><b>认证前：</b>手机号（字符串类型）</li>
     *   <li><b>认证后：</b>{@link com.sandbox.services.living.security.user.CustomUserDetails} 对象</li>
     * </ul>
     */
    private final Object principal;

    /**
     * 凭证信息
     *
     * <p>存储用户输入的验证码。认证成功后应调用 {@link #eraseCredentials()} 擦除此字段，
     * 避免敏感信息在内存中长期存在。
     */
    private Object credentials;

    /**
     * 创建未认证的验证码认证令牌
     *
     * <p>用于认证流程的起始阶段，此时仅包含用户提交的手机号和验证码，
     * 尚未进行任何验证。authenticated 状态为 false。
     *
     * @param principal   手机号
     * @param credentials 验证码
     */
    public CaptchaAuthenticationToken(Object principal, Object credentials) {
        super(null); // 未认证状态下权限为空
        this.principal = principal;
        this.credentials = credentials;
        setAuthenticated(false); // 显式设置为未认证状态
    }

    /**
     * 创建已认证的验证码认证令牌
     *
     * <p>由 {@link com.sandbox.services.living.security.provider.CaptchaAuthenticationProvider}
     * 在验证成功后调用，包含完整的用户信息和权限。authenticated 状态为 true。
     *
     * <p><b>注意：</b>虽然 credentials 参数仍然传递，但建议在认证提供者中将其设置为 null，
     * 或在此构造函数之后调用 {@link #eraseCredentials()} 擦除敏感信息。
     *
     * @param principal   用户详情对象 {@link com.sandbox.services.living.security.user.CustomUserDetails}
     * @param credentials 凭证（建议传入 null，或后续擦除）
     * @param authorities 用户权限集合
     */
    public CaptchaAuthenticationToken(Object principal, Object credentials,
                                      Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.credentials = credentials;
        super.setAuthenticated(true); // 使用父类方法设置认证状态
    }

    /**
     * 获取凭证信息（验证码）
     *
     * @return 验证码字符串，如果已擦除则返回 null
     */
    @Override
    public Object getCredentials() {
        return this.credentials;
    }

    /**
     * 获取主体信息
     *
     * @return 认证前返回手机号，认证后返回用户详情对象
     */
    @Override
    public Object getPrincipal() {
        return this.principal;
    }

    /**
     * 擦除凭证信息
     *
     * <p>认证完成后调用此方法，清除内存中的验证码敏感信息。
     * 同时调用父类的 {@link AbstractAuthenticationToken#eraseCredentials()} 方法，
     * 确保权限信息中的敏感数据也被擦除。
     */
    @Override
    public void eraseCredentials() {
        super.eraseCredentials();
        this.credentials = null;
    }
}