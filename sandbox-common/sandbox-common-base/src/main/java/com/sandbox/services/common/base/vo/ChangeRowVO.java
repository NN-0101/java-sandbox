package com.sandbox.services.common.base.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 受影响行数响应值对象
 *
 * <p>该类用于封装更新、删除等操作后返回的受影响行数，通常在数据库写操作
 * （如 UPDATE、DELETE、批量 INSERT）成功后，将影响的行数返回给调用方。
 * 统一使用该对象可以规范受影响行数的返回格式，便于前端或调用方判断操作结果。
 *
 * <p><b>使用场景：</b>
 * <ul>
 *   <li><b>更新操作：</b>修改数据后返回被更新的记录数</li>
 *   <li><b>删除操作：</b>删除数据后返回被删除的记录数</li>
 *   <li><b>批量插入：</b>批量插入数据后返回实际插入的记录数</li>
 *   <li><b>状态变更：</b>如批量审核、批量启用/禁用等操作</li>
 * </ul>
 *
 * <p><b>响应示例：</b>
 * <pre>
 * // 单条更新
 * {
 *   "changeRow": 1
 * }
 *
 * // 批量删除
 * {
 *   "changeRow": 5
 * }
 *
 * // 在统一响应对象 R 中
 * {
 *   "code": 0,
 *   "msg": "success",
 *   "data": {
 *     "changeRow": 3
 *   },
 *   "traceId": "1a2b3c4d5e6f7g8h"
 * }
 * </pre>
 *
 * <p><b>设计要点：</b>
 * <ul>
 *   <li><b>序列化支持：</b>实现 {@link Serializable} 接口，支持跨网络传输和缓存序列化</li>
 *   <li><b>统一版本：</b>通过 {@code @Serial} 注解和 serialVersionUID 确保序列化版本一致性</li>
 *   <li><b>静态工厂方法：</b>提供 {@link #changeRow(int)} 静态方法简化对象创建</li>
 *   <li><b>整数类型：</b>使用 Integer 类型，足够表示受影响行数，且避免不必要的装箱开销</li>
 * </ul>
 *
 * <p><b>使用示例：</b>
 * <pre>
 * // Controller 层
 * &#64;PutMapping("/users/{id}")
 * public R&lt;ChangeRowVO&gt; updateUser(@PathVariable String id, @RequestBody UserDTO dto) {
 *     int count = userService.updateUser(id, dto);
 *     return R.success(ChangeRowVO.changeRow(count));
 * }
 *
 * &#64;DeleteMapping("/users/batch")
 * public R&lt;ChangeRowVO&gt; batchDeleteUsers(@RequestBody List&lt;String&gt; ids) {
 *     int count = userService.batchDelete(ids);
 *     return R.success(ChangeRowVO.changeRow(count));
 * }
 * </pre>
 *
 * <p><b>注意事项：</b>
 * <ul>
 *   <li>changeRow 为 0 表示没有数据被影响，可能是条件不匹配或数据不存在</li>
 *   <li>对于更新操作，changeRow 可能小于传入的 ID 数量（部分更新成功）</li>
 *   <li>前端可以根据 changeRow 的值给出相应的提示，如“成功更新 3 条数据”</li>
 *   <li>对于预期应该影响数据但结果为 0 的情况，可能需要抛出业务异常</li>
 * </ul>
 *
 * @author 0101
 * @see com.sandbox.services.common.base.vo.R
 * @since 2026-03-18
 */
@Data
public class ChangeRowVO implements Serializable {

    /**
     * 序列化版本号
     *
     * <p>确保在不同版本的 JVM 中序列化和反序列化的兼容性。
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 受影响行数
     *
     * <p>表示数据库操作（UPDATE、DELETE、INSERT）实际影响的数据行数。
     * <ul>
     *   <li>大于 0：操作成功，影响了对应行数的数据</li>
     *   <li>等于 0：没有数据被影响（可能条件不匹配）</li>
     *   <li>不会为负数</li>
     * </ul>
     */
    private Integer changeRow;

    /**
     * 创建包含指定受影响行数的 ChangeRowVO 对象
     *
     * <p>静态工厂方法，用于快速创建 ChangeRowVO 实例。
     *
     * @param changeRow 受影响行数
     * @return 包含指定行数的 ChangeRowVO 对象
     */
    public static ChangeRowVO changeRow(int changeRow) {
        ChangeRowVO changeRowResponse = new ChangeRowVO();
        changeRowResponse.setChangeRow(changeRow);
        return changeRowResponse;
    }
}