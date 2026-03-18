package com.sandbox.services.common.base.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 主键 ID 响应值对象
 *
 * <p>该类用于封装新增操作后返回的主键 ID，通常在插入数据成功后，
 * 将生成的 ID 返回给前端。统一使用该对象可以规范 ID 返回格式，
 * 便于前端统一处理。
 *
 * <p><b>使用场景：</b>
 * <ul>
 *   <li><b>新增数据：</b>当插入一条新记录后，需要将生成的主键 ID 返回给调用方</li>
 *   <li><b>批量新增：</b>批量插入后返回多个 ID（可通过 List&lt;IdVO&gt; 实现）</li>
 *   <li><b>导入操作：</b>数据导入成功后返回导入记录的 ID 列表</li>
 * </ul>
 *
 * <p><b>响应示例：</b>
 * <pre>
 * // 单个 ID 返回
 * {
 *   "id": "1234567890123456789"
 * }
 *
 * // 在统一响应对象 R 中
 * {
 *   "code": 0,
 *   "msg": "success",
 *   "data": {
 *     "id": "1234567890123456789"
 *   },
 *   "traceId": "1a2b3c4d5e6f7g8h"
 * }
 * </pre>
 *
 * <p><b>设计要点：</b>
 * <ul>
 *   <li><b>序列化支持：</b>实现 {@link Serializable} 接口，支持跨网络传输和缓存序列化</li>
 *   <li><b>统一版本：</b>通过 {@code @Serial} 注解和 serialVersionUID 确保序列化版本一致性</li>
 *   <li><b>静态工厂方法：</b>提供 {@link #setResultId(String)} 静态方法简化对象创建</li>
 *   <li><b>字符串类型：</b>ID 使用 String 类型，兼容雪花算法生成的长整型数字（防止前端精度丢失）</li>
 * </ul>
 *
 * <p><b>为什么 ID 使用 String 类型？</b>
 * <ul>
 *   <li>雪花算法生成的 ID 为 64 位长整型，JavaScript 的 Number 类型无法精确表示超过 2^53-1 的数字</li>
 *   <li>使用字符串传递可以避免前端精度丢失问题</li>
 * </ul>
 *
 * @author 0101
 * @see com.sandbox.services.common.base.vo.R
 * @since 2026-03-18
 */
@Data
public class IdVO implements Serializable {

    /**
     * 序列化版本号
     *
     * <p>确保在不同版本的 JVM 中序列化和反序列化的兼容性。
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID
     *
     * <p>使用 String 类型存储 ID，兼容雪花算法生成的 19 位数字，
     * 避免前端 JavaScript 处理大整数时的精度丢失问题。
     */
    private String id;

    /**
     * 创建包含指定 ID 的 IdVO 对象
     *
     * <p>静态工厂方法，用于快速创建 IdVO 实例。
     *
     * @param id 主键 ID
     * @return 包含指定 ID 的 IdVO 对象
     */
    public static IdVO setResultId(String id) {
        IdVO idResponse = new IdVO();
        idResponse.setId(id);
        return idResponse;
    }
}