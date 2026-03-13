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
 * @description: MybatisPlus配置类
 * @author: 0101
 * @create: 2026/3/13
 */
@Slf4j
@Configuration
public class MybatisPlusConfig {

    @Value("${mybatis-plus.datacenter-id}")
    private long datacenterId;

    @Bean
    public IdentifierGenerator idGenerator() {
        long workerId = getWorkerIdByIp();
        log.info("idGenerator workerId:{},datacenterId:{}", workerId, datacenterId);
        // 使用默认生成器并指定 workerId 和 datacenterId
        return new DefaultIdentifierGenerator(workerId, datacenterId);
    }

    /**
     * 获取机器编码 截取ip最后一位作为机器编码，前提：业务部署机器在同一网段 0-31之间
     *
     * @return WorkerId
     */
    public static long getWorkerIdByIp() {
        try {
            String localhost = InetAddress.getLocalHost().getHostAddress();
            List<String> hostSplit = Arrays.asList(localhost.split("\\."));
            long aLong = Long.parseLong(hostSplit.get(hostSplit.size() - 1));
            long workerId = Math.abs(aLong % (32));
            log.info("workerId :{}", workerId);
            return workerId;
        } catch (Exception e) {
            long num = ThreadLocalRandom.current().nextLong(0, 31);
            long workerId = Math.abs(num % (32));
            log.error("Failed to generate WorkerId from IP workerId:{}", workerId, e);
            return workerId;
        }
    }
}
