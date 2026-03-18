package com.sandbox.services.living.ai.tool;

import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

/**
 * 数据库查询工具类，供 AI 模型调用以执行 SQL 查询
 *
 * <p>该类中的方法通过 {@link Tool} 注解暴露给 AI 模型，使模型能够生成 SQL 并执行查询。
 * 当前仅支持 SELECT 类型的查询，用于查询学生成绩等信息。
 *
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>在 {@link com.sandbox.services.living.ai.chat.DBChatMessage} 中作为工具注入</li>
 *   <li>AI 模型根据用户问题动态生成 SQL 并调用此工具获取结果</li>
 * </ul>
 *
 * @author 0101
 * @see Tool
 * @see JdbcTemplate
 * @since 2026/03/18
 */
@Slf4j
public class DBTools {

    private final JdbcTemplate jdbcTemplate;

    public DBTools(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 执行 SQL 查询并返回结果
     *
     * <p><b>限制：</b>仅支持 SELECT 查询，不允许执行更新或修改操作。
     *
     * @param sql 要执行的 SQL 查询语句
     * @return 查询结果字符串，若无结果则返回“未找到匹配的记录”
     */
    @Tool(description = "执行SQL查询并返回结果，用于查询学生成绩信息。只能执行SELECT查询。")
    public String queryDatabase(String sql) {
        log.info("准备执行sql语句：{}", sql);
        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);
        log.info("执行结果：{}", JSONObject.toJSONString(result));
        return result.isEmpty() ? "未找到匹配的记录" : result.toString();
    }
}