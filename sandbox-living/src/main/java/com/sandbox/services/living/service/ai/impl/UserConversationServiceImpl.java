package com.sandbox.services.living.service.ai.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sandbox.services.living.mapper.custom.UserConversationRepository;
import com.sandbox.services.living.entity.UserConversationDO;
import com.sandbox.services.living.model.bo.ai.UserConversationBO;
import com.sandbox.services.living.service.ai.UserConversationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @description: 用户AI聊天会话表(UserConversation)表服务实现类
 * @author: 0101
 * @create: 2026-03-18 14:31:42
 */
@Slf4j
@Service("userConversationService")
public class UserConversationServiceImpl extends ServiceImpl<UserConversationRepository, UserConversationDO> implements UserConversationService {

    @Override
    public List<UserConversationBO> get(String user, String type) {
        LambdaQueryWrapper<UserConversationDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserConversationDO::getUser, user).eq(UserConversationDO::getBusinessType, type);

        List<UserConversationDO> userChatDOS = getBaseMapper().selectList(queryWrapper);

        return userChatDOS.stream().map(m -> BeanUtil.copyProperties(m, UserConversationBO.class)).toList();
    }

    @Override
    public int update(UserConversationBO userConversationBO) {
        UserConversationDO userConversationDO = BeanUtil.copyProperties(userConversationBO, UserConversationDO.class);
        return getBaseMapper().updateById(userConversationDO);
    }
}

