package com.sandbox.services.db.mysql.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * MyBatis-Plus 基础实体模型类
 *
 * <p>该类是所有数据库实体类的基类，提供了通用的字段定义和 MyBatis-Plus 的 ActiveRecord 支持。
 * 通过继承该类，子实体类自动获得 ID、创建时间、更新时间、删除标记等通用字段，
 * 以及 MyBatis-Plus 提供的 ActiveRecord 模式下的数据库操作方法。
 *
 * <p><b>核心功能：</b>
 * <ul>
 *   <li><b>通用字段：</b>提供所有表都需要的标准字段（id、createDate、updateDate、delFlag）</li>
 *   <li><b>主键策略：</b>使用 {@link IdType#ASSIGN_ID} 策略，自动生成分布式唯一 ID（雪花算法）</li>
 *   <li><b>ActiveRecord 支持：</b>继承 {@link Model} 类，使实体类具备直接操作数据库的能力
 *       （如 insert、update、delete、select 等方法）</li>
 *   <li><b>代码复用：</b>避免在每个实体类中重复定义通用字段和方法</li>
 * </ul>
 *
 * <p><b>字段说明：</b>
 * <ul>
 *   <li><b>id：</b>主键 ID，使用 ASSIGN_ID 策略（雪花算法），确保分布式环境下的唯一性</li>
 *   <li><b>createDate：</b>创建时间，记录数据插入的时间</li>
 *   <li><b>updateDate：</b>更新时间，记录数据最后修改的时间，每次更新自动维护</li>
 *   <li><b>delFlag：</b>逻辑删除标记，用于软删除（0-正常，1-已删除）</li>
 * </ul>
 *
 * <p><b>使用方式：</b>
 * <pre>
 * &#64;TableName("user")
 * public class User extends BaseModel&lt;User&gt; {
 *     private String name;
 *     private Integer age;
 *
 *     // 子类特有的字段和方法
 * }
 *
 * // 使用示例（ActiveRecord 模式）
 * User user = new User();
 * user.setName("张三");
 * user.insert();  // 直接插入数据库，自动生成 ID 和 createDate
 *
 * User found = new User().selectById("123");  // 根据 ID 查询
 * </pre>
 *
 * <p><b>注意事项：</b>
 * <ul>
 *   <li>子类必须指定泛型为自身类型（如 {@code class User extends BaseModel<User>}），
 *       以满足 {@link Model} 的泛型要求</li>
 *   <li>使用 {@code ASSIGN_ID} 主键策略需要 MyBatis-Plus 配置雪花算法 ID 生成器</li>
 *   <li>逻辑删除字段 {@code delFlag} 需要在 application.yml 中配置全局逻辑删除规则</li>
 *   <li>创建时间和更新时间通常由数据库自动维护，也可通过 MyBatis-Plus 的自动填充功能实现</li>
 * </ul>
 *
 * @param <T> 子实体类型，必须继承自 {@link Model}，用于 ActiveRecord 链式调用
 * @author 0101
 * @see Model
 * @see TableId
 * @see IdType#ASSIGN_ID
 * @since 2026-03-18
 */
@Setter
@Getter
public class BaseModel<T extends Model<?>> extends Model<T> {

    /**
     * 主键 ID
     *
     * <p>使用 {@link IdType#ASSIGN_ID} 策略，采用雪花算法生成分布式唯一 ID。
     * 适用于分布式系统，保证 ID 的全局唯一性和趋势递增。
     *
     * <p>字段对应数据库表的主键列，通常命名为 "id"。
     */
    @TableId(type = IdType.ASSIGN_ID)
    protected String id;

    /**
     * 创建时间
     *
     * <p>记录数据的创建时间，通常由数据库自动生成（如 MySQL 的 CURRENT_TIMESTAMP）。
     * 也可通过 MyBatis-Plus 的自动填充功能在插入时自动设置。
     *
     * <p>对应数据库表的 "create_date" 或 "create_time" 列。
     */
    protected Date createDate;

    /**
     * 更新时间
     *
     * <p>记录数据的最后修改时间，通常由数据库在更新时自动更新（如 MySQL 的 ON UPDATE CURRENT_TIMESTAMP）。
     * 也可通过 MyBatis-Plus 的自动填充功能在更新时自动设置。
     *
     * <p>对应数据库表的 "update_date" 或 "update_time" 列。
     */
    protected Date updateDate;

    /**
     * 逻辑删除标记
     *
     * <p>用于实现软删除功能，而不是物理删除数据。
     * 常见约定：
     * <ul>
     *   <li>"0" - 正常（未删除）</li>
     *   <li>"1" - 已删除</li>
     * </ul>
     *
     * <p>需要在 application.yml 中配置 MyBatis-Plus 的全局逻辑删除规则：
     * <pre>
     * mybatis-plus:
     *   global-config:
     *     db-config:
     *       logic-delete-field: delFlag
     *       logic-delete-value: 1
     *       logic-not-delete-value: 0
     * </pre>
     *
     * <p>对应数据库表的 "del_flag" 或 "deleted" 列。
     */
    protected String delFlag;
}