package com.sandbox.services.living.model.dto.ai;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @description:
 * @author: xp
 * @create: 2025/6/1
 */
@Data
public class ChatMessageRequest {

    private String conversationId;

    @NotBlank(message = "用户不能为空")
    private String user;

    @NotBlank(message = "询问不能为空")
    private String query;

}
