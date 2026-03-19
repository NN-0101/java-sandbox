package com.sandbox.services.living.controller.ai;

import cn.hutool.core.bean.BeanUtil;
import com.sandbox.services.living.ai.model.ChatMessageBO;
import com.sandbox.services.living.model.dto.ai.ChatMessageRequest;
import com.sandbox.services.living.service.ai.ChatMessageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;


/**
 * @description:
 * @author: xp
 * @create: 2025/6/1
 */
@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private ChatMessageService chatMessageService;

    @PostMapping("/message/{business}")
    public Flux<String> message(@PathVariable(value = "business") String business,
                                @RequestBody @Valid ChatMessageRequest param) {
        ChatMessageBO chatMessageBO = BeanUtil.copyProperties(param, ChatMessageBO.class);
        chatMessageBO.setBusiness(business);
        return chatMessageService.message(chatMessageBO);
    }
}
