package com.sandbox.services.living.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.sandbox.services.db.mysql.model.BaseModel;
import java.io.Serial;

/**
 * @description: 用户表(User)表数据库实体
 * @author: 0101
 * @create: 2026-03-14 20:01:44
 */
@Data
@TableName("t_user")
@EqualsAndHashCode(callSuper = true)
public class UserDO extends BaseModel<UserDO> {

    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 昵称
     */
    private String nickName;
     
    /**
     * 真实姓名
     */
    private String realName;
     
    /**
     * 手机号
     */
    private String phone;
     
    /**
     * 密码
     */
    private String password;
     
    /**
     * 性别 1：男 2：女
     */
    private Integer gender;
     
    /**
     * 身份证号码
     */
    private String idNumber;
     
    /**
     * 邮箱
     */
    private String email;
     
    /**
     * 头像链接
     */
    private String headImg;
     
    /**
     * 个性签名
     */
    private String sign;
     

}
