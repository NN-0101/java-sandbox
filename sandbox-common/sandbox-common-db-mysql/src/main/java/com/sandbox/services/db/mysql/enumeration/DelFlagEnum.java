package com.sandbox.services.db.mysql.enumeration;

import lombok.Getter;

/**
 * 逻辑删除标记枚举
 *
 * <p>该枚举定义了数据逻辑删除的状态，用于替代数据库表中的硬删除操作。
 * 通过逻辑删除，数据不会被真正从数据库中移除，而是通过标记字段标识其删除状态，
 * 便于数据恢复和审计追踪。
 *
 * <p><b>核心功能：</b>
 * <ul>
 *   <li><b>删除状态标识：</b>统一管理数据是否被删除的两种状态</li>
 *   <li><b>与 MyBatis-Plus 集成：</b>可与 MyBatis-Plus 的逻辑删除功能配合使用，
 *       在查询时自动过滤已删除的数据</li>
 *   <li><b>代码语义化：</b>使用枚举代替魔法数字，提高代码可读性和可维护性</li>
 * </ul>
 *
 * <p><b>枚举项说明：</b>
 * <ul>
 *   <li><b>NO：</b>未删除状态（正常数据），对应数据库中的 0</li>
 *   <li><b>YES：</b>已删除状态（逻辑删除），对应数据库中的 1</li>
 * </ul>
 *
 * <p><b>使用场景：</b>
 * <ul>
 *   <li><b>实体类字段：</b>在 {@link com.sandbox.services.db.mysql.model.BaseModel} 中作为 delFlag 字段的值</li>
 *   <li><b>查询条件：</b>在业务代码中构建查询条件时，使用 DelFlagEnum.NO.getCode() 过滤未删除数据</li>
 *   <li><b>删除操作：</b>执行删除操作时，将 delFlag 字段更新为 DelFlagEnum.YES.getCode()</li>
 *   <li><b>数据恢复：</b>恢复已删除数据时，将 delFlag 字段更新为 DelFlagEnum.NO.getCode()</li>
 * </ul>
 *
 * <p><b>MyBatis-Plus 配置示例：</b>
 * <pre>
 * mybatis-plus:
 *   global-config:
 *     db-config:
 *       logic-delete-field: delFlag
 *       logic-delete-value: 1           # 对应 DelFlagEnum.YES.getCode()
 *       logic-not-delete-value: 0       # 对应 DelFlagEnum.NO.getCode()
 * </pre>
 *
 * <p><b>使用示例：</b>
 * <pre>
 * // 查询未删除的用户
 * LambdaQueryWrapper&lt;User&gt; wrapper = new LambdaQueryWrapper&lt;&gt;()
 *     .eq(User::getDelFlag, DelFlagEnum.NO.getCode());
 *
 * // 逻辑删除用户
 * User user = new User();
 * user.setId("123");
 * user.setDelFlag(String.valueOf(DelFlagEnum.YES.getCode()));
 * user.updateById();
 * </pre>
 *
 * <p><b>设计要点：</b>
 * <ul>
 *   <li><b>数值类型：</b>使用 int 类型作为 code，与数据库中的数字类型（如 tinyint）对应</li>
 *   <li><b>语义清晰：</b>NO 表示未删除，YES 表示已删除，符合英语习惯</li>
 *   <li><b>扩展性：</b>如果未来需要更多删除状态（如待删除、永久删除等），可在此枚举中扩展</li>
 *   <li><b>类型安全：</b>使用枚举替代整数常量，避免误传错误的值</li>
 * </ul>
 *
 * @author 0101
 * @see com.sandbox.services.db.mysql.model.BaseModel
 * @see com.baomidou.mybatisplus.annotation.TableLogic
 * @since 2026-03-18
 */
@Getter
public enum DelFlagEnum {

    /**
     * 未删除（正常数据）
     *
     * <p>表示数据处于正常状态，未被逻辑删除。
     * 对应数据库中的 0，是大多数查询的默认条件。
     */
    NO(0, "未删除"),

    /**
     * 已删除（逻辑删除）
     *
     * <p>表示数据已被逻辑删除，但在数据库中仍然存在。
     * 对应数据库中的 1，通常不会出现在常规查询结果中。
     */
    YES(1, "已删除");

    /**
     * 删除状态代码
     *
     * <p>存储在数据库中的实际值，与数据库字段类型匹配。
     * 通常使用 tinyint 类型存储，占用空间小，查询效率高。
     * -- GETTER --
     * 获取删除状态代码
     */
    private final int code;

    /**
     * 状态描述
     *
     * <p>描述信息，用于日志输出、前端展示等场景。
     * -- GETTER --
     * 获取状态描述
     */
    private final String description;

    /**
     * 构造方法
     *
     * @param code        删除状态代码
     * @param description 状态描述
     */
    DelFlagEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

}