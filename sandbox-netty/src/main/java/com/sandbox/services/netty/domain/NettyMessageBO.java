package com.sandbox.services.netty.domain;

import lombok.Data;

/**
 * @description: 接受消息
 * @author: xp
 * @create: 2025/5/1
 */
@Data
public class NettyMessageBO {

    private int type;

    private String uid;

    private String clientId;
}
