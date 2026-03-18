package com.sandbox.services.living.ai.annotations;

import com.sandbox.services.living.enumeration.ai.AiTypeEnum;
import org.springframework.beans.factory.annotation.Qualifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解，用于标记不同类型的 AI 聊天消息服务实现类
 *
 * <p>该注解结合 {@link Qualifier} 使用，支持通过 {@link AiTypeEnum} 枚举值来区分不同的 AI 服务类型，
 * 便于在 Spring 容器中通过类型注入对应的实现。
 *
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>标注在 {@link com.sandbox.services.living.ai.chat.BaseChatMessage} 的实现类上</li>
 *   <li>用于 {@link com.sandbox.services.living.ai.config.AiConfig#chatMessageStrategyMap} 的策略映射构建</li>
 * </ul>
 *
 * @author 0101
 * @see AiTypeEnum
 * @see Qualifier
 * @since 2026/03/18
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Qualifier
public @interface AiChatMessageService {
    AiTypeEnum value();
}