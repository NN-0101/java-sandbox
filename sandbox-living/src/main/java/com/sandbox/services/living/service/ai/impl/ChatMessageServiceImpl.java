package com.sandbox.services.living.service.ai.impl;

import com.sandbox.services.living.ai.chat.BaseChatMessage;
import com.sandbox.services.living.entity.UserConversationDO;
import com.sandbox.services.living.enumeration.LivingResponseCodeEnum;
import com.sandbox.services.living.enumeration.ai.AiTypeEnum;
import com.sandbox.services.living.exception.LivingBusinessException;
import com.sandbox.services.living.model.bo.ai.ChatMessageBO;
import com.sandbox.services.living.service.ai.ChatMessageService;
import com.sandbox.services.living.service.ai.UserConversationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.Objects;

/**
 * @description:
 * @author: xp
 * @create: 2025/6/1
 */
@Slf4j
@Service
public class ChatMessageServiceImpl implements ChatMessageService {

    @Autowired
    public Map<AiTypeEnum, BaseChatMessage> chatMessageStrategyMap;

    @Autowired
    private UserConversationService userConversationService;

    @Override
    public Flux<String> message(ChatMessageBO chatMessageBO) {
        String business = chatMessageBO.getBusiness();
        String query = chatMessageBO.getQuery();
        String user = chatMessageBO.getUser();
        AiTypeEnum aiType = AiTypeEnum.getAiTypeEnum(chatMessageBO.getBusiness());

        String conversationId = chatMessageBO.getConversationId();
        UserConversationDO userChatDO = userConversationService.getBaseMapper().selectById(conversationId);
        if (userChatDO == null) {
            //保存用户会话
            UserConversationDO entity = new UserConversationDO();
            entity.setName(query);
            entity.setBusinessType(business);
            entity.setUser(user);
            userConversationService.getBaseMapper().insert(entity);
            conversationId = entity.getId();
        } else {
            if (!Objects.equals(userChatDO.getUser(), user)) {
                throw new LivingBusinessException(LivingResponseCodeEnum.AI_CONVERSATION_NOT_EXITS);
            }
        }
        //调用大模型
        BaseChatMessage processor = chatMessageStrategyMap.get(aiType);
        return processor.sendMessage(user, conversationId, query);
    }
}
