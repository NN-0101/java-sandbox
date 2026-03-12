package com.sandbox.services.common.base.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @description:
 * @author: xp
 * @create: 2023/10/23
 */
@Data
public class ChangeRowVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Integer changeRow;

    /**
     * changeRow
     *
     * @param changeRow changeRow
     * @return ChangeRowResponse
     */
    public static ChangeRowVO changeRow(int changeRow) {
        ChangeRowVO changeRowResponse = new ChangeRowVO();
        changeRowResponse.setChangeRow(changeRow);
        return changeRowResponse;
    }
}
