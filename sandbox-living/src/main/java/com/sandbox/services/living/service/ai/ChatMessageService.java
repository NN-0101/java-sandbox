package com.sandbox.services.living.service.ai;

import com.sandbox.services.living.model.bo.ai.ChatMessageBO;
import reactor.core.publisher.Flux;

/**
 * @description:
 * @author: 0101
 * @create: 2026/3/18
 */
public interface ChatMessageService {

    /**
     * 对话
     *
     * @param chatMessageBO 参数
     * @return 结果
     */
    Flux<String> message(ChatMessageBO chatMessageBO);
}
