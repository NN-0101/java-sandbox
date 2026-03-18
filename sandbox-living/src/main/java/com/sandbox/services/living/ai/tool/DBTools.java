package com.sandbox.services.living.ai.tool;

import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: xp
 * @create: 2025/6/3
 */
@Slf4j
public class DBTools {

    private final JdbcTemplate jdbcTemplate;

    public DBTools(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    @Tool(description = "执行SQL查询并返回结果，用于查询学生成绩信息。只能执行SELECT查询。")
    public String queryDatabase(String sql) {
        log.info("准备执行sql语句：{}", sql);
        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);
        log.info("执行结果：{}", JSONObject.toJSONString(result));
        return result.isEmpty() ? "未找到匹配的记录" : result.toString();
    }
}
