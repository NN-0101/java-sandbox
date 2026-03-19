package com.sandbox.services.living.service.ai.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sandbox.services.living.mapper.custom.ConversationMemoryRepository;
import com.sandbox.services.living.entity.ConversationMemoryDO;
import com.sandbox.services.living.ai.model.ChatMemoryBO;
import com.sandbox.services.living.service.ai.ConversationMemoryService;
import com.sandbox.services.living.service.ai.UserConversationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @description: AI聊天会话记忆表(ConversationMemory)表服务实现类
 * @author: 0101
 * @create: 2026-03-18 14:27:11
 */
@Slf4j
@Service("conversationMemoryService")
public class ConversationMemoryServiceImpl extends ServiceImpl<ConversationMemoryRepository, ConversationMemoryDO> implements ConversationMemoryService {

    @Autowired
    private UserConversationService userConversationService;

    @Override
    public void add(String conversationId, List<Message> messages) {
        List<ChatMemoryBO> entities = messages.stream().map(m -> new ChatMemoryBO(conversationId, m)).toList();
        if (CollectionUtils.isNotEmpty(entities)) {
            List<ConversationMemoryDO> collect = entities.stream().map(m -> BeanUtil.copyProperties(m, ConversationMemoryDO.class)).toList();
            getBaseMapper().insertBatch(collect);
        }
    }

    @Override
    public List<Message> get(String conversationId) {
        LambdaQueryWrapper<ConversationMemoryDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ConversationMemoryDO::getConversationId, conversationId).orderByAsc(ConversationMemoryDO::getCreateDate);

        Page<ConversationMemoryDO> page = new Page<>(0, Integer.MAX_VALUE);
        Page<ConversationMemoryDO> pageList = getBaseMapper().selectPage(page, queryWrapper);

        List<ConversationMemoryDO> records = pageList.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return new ArrayList<>();
        }
        // 转换为Message列表
        List<Message> messageList = new ArrayList<>();
        for (ConversationMemoryDO record : records) {
            //消息类型
            MessageType messageType = MessageType.valueOf(record.getMessageType().toUpperCase());
            // 创建空元数据（因ChatMemoryDO未存储metadata）
            Map metadata = JSONObject.parseObject(record.getMetadata(), Map.class);
            // 根据类型创建具体Message实现类
            Message message;
            switch (messageType) {
                case USER:
                    message = new UserMessage(record.getContent());
                    break;
                case ASSISTANT:
                    message = new AssistantMessage(record.getContent());
                    break;
                case SYSTEM:
                    message = new SystemMessage(record.getContent());
                    break;
                /*case TOOL:
                    message = new ToolResponseMessage(record.getContent(), metadata);
                    break;*/
                default:
                    log.warn("Unsupported message type: {}, treated as USER", messageType);
                    continue;

            }
            messageList.add(message);
        }
        return messageList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clear(String conversationId) {
        //删除用户会话
        userConversationService.getBaseMapper().deleteById(conversationId);

        LambdaQueryWrapper<ConversationMemoryDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ConversationMemoryDO::getConversationId, conversationId);
        getBaseMapper().delete(queryWrapper);
    }
}

