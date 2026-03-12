package com.sandbox.services.common.base.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @description: 自增主键id返回
 * @author: xp
 * @create: 2023/9/18
 */
@Data
public class IdVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String id;

    /**
     * setResultId
     *
     * @param id id
     * @return IdResponse
     */
    public static IdVO setResultId(String id) {
        IdVO idResponse = new IdVO();
        idResponse.setId(id);
        return idResponse;
    }
}
