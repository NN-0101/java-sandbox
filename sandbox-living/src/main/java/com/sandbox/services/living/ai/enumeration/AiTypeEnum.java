package com.sandbox.services.living.ai.enumeration;

import lombok.Getter;

import java.util.Arrays;
import java.util.NoSuchElementException;

/**
 * AI 类型枚举，用于区分不同的 AI 服务场景
 *
 * <p>该枚举定义了系统中所有支持的 AI 聊天服务类型，每种类型对应一种特定的业务场景。
 * 通过枚举值，系统可以动态选择对应的 {@link com.sandbox.services.living.ai.chat.BaseChatMessage} 实现类。
 *
 * <p><b>枚举项说明：</b>
 * <ul>
 *   <li>{@link #DB} - 数据库查询场景：AI 可生成 SQL 并查询学生成绩等信息</li>
 *   <li>{@link #MCP} - MCP 工具调用场景：预留用于调用外部 MCP 工具</li>
 *   <li>{@link #TALK} - 通用闲聊场景：仅进行普通对话，不附加任何工具</li>
 * </ul>
 *
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>在 {@link com.sandbox.services.living.ai.annotations.AiChatMessageService} 注解中标记服务实现类</li>
 *   <li>在 {@link com.sandbox.services.living.ai.config.AiConfig#chatMessageStrategyMap} 中构建策略映射</li>
 *   <li>在前端请求中传递类型参数，决定使用哪种 AI 能力</li>
 *   <li>在 {@link com.sandbox.services.living.config.BusinessConfig} 中作为提示词配置的 key</li>
 * </ul>
 *
 * <p><b>设计特点：</b>
 * <ul>
 *   <li>使用 {@link Getter} 注解自动生成 getter 方法，简化代码</li>
 *   <li>提供静态方法 {@link #getDescriptionByValue(String)} 和 {@link #getAiTypeEnum(String)}，
 *       方便根据 value 值查找对应的枚举项或描述信息</li>
 *   <li>当查找不到对应枚举时抛出 {@link NoSuchElementException}，避免返回 null 导致后续空指针异常</li>
 * </ul>
 *
 * @author xp
 * @see com.sandbox.services.living.ai.chat.BaseChatMessage
 * @see com.sandbox.services.living.ai.annotations.AiChatMessageService
 * @see com.sandbox.services.living.ai.config.AiConfig
 * @since 2025-03-07
 */
@Getter
public enum AiTypeEnum {

    /**
     * 数据库查询类型
     *
     * <p>对应 {@link com.sandbox.services.living.ai.chat.DBChatMessage} 实现类。
     * AI 具备数据库查询能力，可根据用户问题生成 SQL 并执行查询，返回结果给用户。
     */
    DB("db", "数据库查询"),

    /**
     * MCP 工具调用类型
     *
     * <p>预留类型，用于调用外部 MCP（Model Context Protocol）工具。
     * 目前尚未实现具体逻辑，供未来扩展使用。
     */
    MCP("mcp", "mcp工具调用"),

    /**
     * 通用闲聊类型
     *
     * <p>对应 {@link com.sandbox.services.living.ai.chat.TalkChatMessage} 实现类。
     * 仅进行普通对话，不附加任何工具或特定提示词，适用于日常交流场景。
     */
    TALK("talk", "聊天"),
    ;

    /**
     * 枚举值，用于在配置和请求中标识类型
     *
     * <p>例如：
     * <ul>
     *   <li>"db" - 数据库查询</li>
     *   <li>"mcp" - MCP 工具调用</li>
     *   <li>"talk" - 通用闲聊</li>
     * </ul>
     */
    private final String value;

    /**
     * 类型描述，用于日志输出和前端展示
     */
    private final String description;

    /**
     * 构造方法
     *
     * @param value       枚举值
     * @param description 描述信息
     */
    AiTypeEnum(String value, String description) {
        this.value = value;
        this.description = description;
    }

    /**
     * 根据枚举值获取对应的描述信息
     *
     * <p><b>使用场景：</b>
     * <ul>
     *   <li>日志记录时输出人类可读的描述</li>
     *   <li>前端展示类型名称</li>
     * </ul>
     *
     * @param value 枚举值（如 "db"、"talk" 等）
     * @return 对应的描述信息
     * @throws NoSuchElementException 如果找不到对应的枚举项
     */
    public static String getDescriptionByValue(String value) {
        return Arrays.stream(values())
                .filter(x -> x.getValue().equals(value))
                .findFirst()
                .map(AiTypeEnum::getDescription)
                .orElseThrow(() -> new NoSuchElementException("没有相关业务！"));
    }

    /**
     * 根据枚举值获取对应的枚举对象
     *
     * <p><b>使用场景：</b>
     * <ul>
     *   <li>前端传递类型参数时，转换为枚举进行后续处理</li>
     *   <li>从配置文件中读取类型字符串，转换为枚举使用</li>
     * </ul>
     *
     * @param value 枚举值（如 "db"、"talk" 等）
     * @return 对应的枚举对象
     * @throws NoSuchElementException 如果找不到对应的枚举项
     */
    public static AiTypeEnum getAiTypeEnum(String value) {
        return Arrays.stream(values())
                .filter(x -> x.getValue().equals(value))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("没有相关业务！"));
    }
}