package com.sandbox.services.living.ai.annotations;

import com.sandbox.services.living.enumeration.ai.AiTypeEnum;
import org.springframework.beans.factory.annotation.Qualifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @description:
 * @author: xp
 * @create: 2025/6/1
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Qualifier
public @interface AiChatMessageService {
    AiTypeEnum value();
}