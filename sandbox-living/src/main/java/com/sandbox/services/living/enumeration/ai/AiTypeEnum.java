package com.sandbox.services.living.enumeration.ai;

import lombok.Getter;

import java.util.Arrays;
import java.util.NoSuchElementException;

/**
 * @description:
 * @author: xp
 * @create: 2025/3/7
 */
@Getter
public enum AiTypeEnum {


    DB("db", "数据库查询"),

    MCP("mcp", "mcp工具调用"),

    TALK("talk", "聊天"),

    ;

    private final String value;
    private final String description;

    private AiTypeEnum(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public static String getDescriptionByValue(String value) {
        return Arrays.stream(values()).filter(x -> x.getValue().equals(value)).findFirst()
                .map(AiTypeEnum::getDescription).orElseThrow(() -> new NoSuchElementException("没有相关业务！"));
    }

    public static AiTypeEnum getAiTypeEnum(String value) {
        return Arrays.stream(values()).filter(x -> x.getValue().equals(value)).findFirst()
                .orElseThrow(() -> new NoSuchElementException("没有相关业务！"));
    }

}
