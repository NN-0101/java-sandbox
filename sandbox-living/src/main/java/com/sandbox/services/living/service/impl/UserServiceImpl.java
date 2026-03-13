package com.sandbox.services.living.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sandbox.services.living.mapper.custom.UserRepository;
import com.sandbox.services.living.model.UserDO;
import com.sandbox.services.living.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @description: 用户表(User)表服务实现类
 * @author: 0101
 * @create: 2026-03-13 22:04:03
 */
@Slf4j
@Service("userService")
public class UserServiceImpl extends ServiceImpl<UserRepository, UserDO> implements UserService {

}

