package com.sandbox.services.db.mysql.enumeration;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * 数据源枚举类
 *
 * <p>该枚举定义了系统中所有可用的数据源标识，包括主库和从库。
 * 主要用于 ShardingSphere 读写分离配置中的数据源命名，以及在代码中
 * 统一引用数据源名称，避免硬编码字符串导致的错误。
 *
 * <p><b>数据源结构：</b>
 * <ul>
 *   <li><b>DS0 / DS1：</b>主数据源（写库），负责处理写入操作</li>
 *   <li><b>DS0SLAVE0 / DS1SLAVE0：</b>从数据源（读库），负责处理读取操作，分担主库压力</li>
 * </ul>
 *
 * <p><b>使用场景：</b>
 * <ul>
 *   <li><b>动态数据源选择：</b>可用于手动指定使用哪个数据源（如强制读主库）</li>
 *   <li><b>日志监控：</b>记录操作发生在哪个数据源上，便于问题排查</li>
 * </ul>
 *
 * <p><b>设计要点：</b>
 * <ul>
 *   <li><b>命名规范：</b>主库使用 "ds0"、"ds1" 格式，从库使用 "ds0slave0"、"ds1slave0" 格式，
 *       清晰表达数据源之间的关系</li>
 *   <li><b>类型安全：</b>使用枚举替代字符串常量，避免拼写错误，提高代码可维护性</li>
 *   <li><b>扩展性：</b>预留了添加更多数据源的空间，如 DS2、DS2SLAVE0 等</li>
 *   <li><b>描述信息：</b>每个枚举项附带描述，便于日志输出和运维理解</li>
 * </ul>
 *
 * @author 0101
 * @since 2026-03-13
 */
@Getter
public enum DataSourceEnum {

    /**
     * 主数据源 0
     *
     * <p>第一个主库，负责处理写入操作。
     * 对应配置中的 ds0 数据源。
     */
    DS0("ds0", "数据源0"),

    /**
     * 主数据源 1
     *
     * <p>第二个主库，负责处理写入操作。
     * 对应配置中的 ds1 数据源。
     */
    DS1("ds1", "数据源1"),

    /**
     * 主数据源 0 的从库
     *
     * <p>ds0 的从库，负责处理读取操作，分担主库的读压力。
     * 对应配置中的 ds0slave0 数据源。
     */
    DS0SLAVE0("ds0slave0", "数据源0的备用0"),

    /**
     * 主数据源 1 的从库
     *
     * <p>ds1 的从库，负责处理读取操作，分担主库的读压力。
     * 对应配置中的 ds1slave0 数据源。
     */
    DS1SLAVE0("ds1slave0", "数据源1的备用0");

    /**
     * 数据源标识值
     *
     * <p>用于在配置文件和 ShardingSphere 规则中引用数据源。
     * 例如：{@code "ds0"}、{@code "ds1slave0"} 等。
     */
    private final String value;

    /**
     * 数据源描述
     *
     * <p>对人类友好的描述信息，用于日志输出、监控展示等场景。
     */
    private final String description;

    /**
     * 构造方法
     *
     * @param value       数据源标识值
     * @param description 数据源描述
     */
    DataSourceEnum(String value, String description) {
        this.value = value;
        this.description = description;
    }

    /**
     * 根据数据源标识值获取对应的描述信息
     *
     * <p><b>使用场景：</b>
     * <ul>
     *   <li>日志记录时输出人类可读的数据源描述</li>
     *   <li>监控系统中展示数据源信息</li>
     * </ul>
     *
     * @param value 数据源标识值（如 "ds0"、"ds1slave0"）
     * @return 对应的描述信息
     * @throws NoSuchElementException 如果找不到对应的枚举项
     */
    public static String getDescriptionByValue(String value) {
        return Arrays.stream(values())
                .filter(x -> x.getValue().equals(value))
                .findFirst()
                .map(DataSourceEnum::getDescription)
                .orElseThrow(() -> new NoSuchElementException("没有找到对应的枚举！"));
    }

    /**
     * 获取所有数据源枚举的列表
     *
     * <p><b>使用场景：</b>
     * <ul>
     *   <li>遍历所有数据源进行初始化操作</li>
     *   <li>生成数据源配置文档</li>
     *   <li>批量操作（如检查所有数据源连接状态）</li>
     * </ul>
     *
     * @return 包含所有数据源枚举的列表
     */
    public static List<DataSourceEnum> getList() {
        return Arrays.stream(DataSourceEnum.values()).collect(Collectors.toList());
    }
}