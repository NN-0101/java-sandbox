package com.sandbox.services.db.mysql.config;

import com.baomidou.mybatisplus.core.incrementer.DefaultIdentifierGenerator;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * MyBatis-Plus 配置类
 *
 * <p>该类主要负责 MyBatis-Plus 的全局配置，当前主要配置了雪花算法 ID 生成器。
 * 通过自定义 {@link IdentifierGenerator}，为分布式系统提供全局唯一、趋势递增的主键 ID。
 *
 * <p><b>核心功能：</b>
 * <ul>
 *   <li><b>ID 生成器配置：</b>配置雪花算法 ID 生成器，为实体类主键提供分布式唯一 ID</li>
 *   <li><b>WorkerId 自动生成：</b>根据服务器 IP 地址自动生成机器码（workerId），确保不同节点的 workerId 不冲突</li>
 *   <li><b>容错机制：</b>当无法通过 IP 获取 workerId 时，使用随机数作为降级方案</li>
 * </ul>
 *
 * <p><b>雪花算法原理：</b>
 * 雪花算法生成的 ID 由以下几部分组成：
 * <ul>
 *   <li>1 位符号位（固定为 0）</li>
 *   <li>41 位时间戳（毫秒级，可以使用 69 年）</li>
 *   <li>10 位机器码（5 位 datacenterId + 5 位 workerId，支持 1024 个节点）</li>
 *   <li>12 位序列号（每毫秒可生成 4096 个 ID）</li>
 * </ul>
 *
 * <p><b>配置项：</b>
 * <ul>
 *   <li>{@code mybatis-plus.datacenter-id}：数据中心 ID，需要在配置文件中指定（0-31）</li>
 * </ul>
 *
 * <p><b>WorkerId 生成策略：</b>
 * <ul>
 *   <li><b>优先策略：</b>从服务器 IP 地址的最后一段（0-255）取模 32，得到 0-31 范围内的 workerId</li>
 *   <li><b>前提条件：</b>业务部署的服务器需要在同一网段，确保 IP 最后一段唯一或冲突概率低</li>
 *   <li><b>降级策略：</b>如果获取 IP 失败，使用 {@link ThreadLocalRandom} 生成 0-31 的随机数作为 workerId</li>
 * </ul>
 *
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>与 {@link com.sandbox.services.db.mysql.model.BaseModel} 中的 {@code @TableId(type = IdType.ASSIGN_ID)} 配合使用</li>
 *   <li>在分布式系统中生成全局唯一的主键 ID，避免数据库自增主键在分库分表时的冲突问题</li>
 * </ul>
 *
 * <p><b>注意事项：</b>
 * <ul>
 *   <li>datacenterId 需要在配置文件中显式指定，不同数据中心使用不同的 ID</li>
 *   <li>workerId 自动生成策略要求服务器 IP 最后一段不能重复，否则可能导致 ID 冲突</li>
 *   <li>如果部署在容器或虚拟化环境中，IP 可能相同，建议手动配置 workerId 或使用其他唯一标识</li>
 *   <li>生成的 ID 是 Long 类型，数据库主键字段需要对应使用 BIGINT 类型</li>
 * </ul>
 *
 * @author 0101
 * @see IdentifierGenerator
 * @see DefaultIdentifierGenerator
 * @see com.sandbox.services.db.mysql.model.BaseModel
 * @since 2026-03-13
 */
@Slf4j
@Configuration
public class MybatisPlusConfig {

    /**
     * 数据中心 ID
     *
     * <p>从配置文件读取，用于标识不同的数据中心（机房）。
     * 取值范围：0-31，共 5 位，支持最多 32 个数据中心。
     */
    @Value("${mybatis-plus.datacenter-id}")
    private long datacenterId;

    /**
     * 配置 ID 生成器
     *
     * <p>创建并配置雪花算法 ID 生成器实例，用于全局主键生成。
     *
     * <p><b>生成策略：</b>
     * <ol>
     *   <li>通过 {@link #getWorkerIdByIp()} 方法获取机器码 workerId（0-31）</li>
     *   <li>从配置文件读取数据中心 ID datacenterId（0-31）</li>
     *   <li>使用 {@link DefaultIdentifierGenerator} 组合两者创建 ID 生成器</li>
     * </ol>
     *
     * @return 配置好的 ID 生成器实例
     */
    @Bean
    public IdentifierGenerator idGenerator() {
        long workerId = getWorkerIdByIp();
        log.info("idGenerator workerId:{}, datacenterId:{}", workerId, datacenterId);
        // 使用默认生成器并指定 workerId 和 datacenterId
        return new DefaultIdentifierGenerator(workerId, datacenterId);
    }

    /**
     * 根据服务器 IP 地址获取机器码（workerId）
     *
     * <p>从本地 IP 地址的最后一段计算 workerId，确保同一数据中心内不同节点的 workerId 尽量不冲突。
     *
     * <p><b>计算规则：</b>
     * <ol>
     *   <li>获取本机 IP 地址（如 192.168.1.100）</li>
     *   <li>提取 IP 的最后一段（100）</li>
     *   <li>对 32 取模，得到 0-31 范围内的 workerId</li>
     * </ol>
     *
     * <p><b>前提条件：</b>
     * 业务部署的服务器在同一网段，IP 最后一段具有较好的唯一性。
     * 例如：192.168.1.100、192.168.1.101、192.168.1.102 等。
     *
     * <p><b>容错处理：</b>
     * 如果获取 IP 地址失败（如网络异常、UnknownHostException），
     * 则使用 {@link ThreadLocalRandom} 生成 0-31 的随机数作为 workerId，
     * 并记录错误日志。
     *
     * @return 0-31 范围内的机器码
     */
    public static long getWorkerIdByIp() {
        try {
            // 获取本机 IP 地址
            String localhost = InetAddress.getLocalHost().getHostAddress();
            // 按点分割 IP 地址
            List<String> hostSplit = Arrays.asList(localhost.split("\\."));
            // 获取 IP 最后一段
            long lastSegment = Long.parseLong(hostSplit.get(hostSplit.size() - 1));
            // 对 32 取模，确保结果在 0-31 范围内
            long workerId = Math.abs(lastSegment % 32);
            log.info("根据 IP 生成 workerId 成功: IP={}, lastSegment={}, workerId={}",
                    localhost, lastSegment, workerId);
            return workerId;
        } catch (Exception e) {
            // 降级策略：生成随机 workerId
            long num = ThreadLocalRandom.current().nextLong(0, 31);
            long workerId = Math.abs(num % 32);
            log.error("根据 IP 生成 workerId 失败，使用随机值 workerId:{}", workerId, e);
            return workerId;
        }
    }
}