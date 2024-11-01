package com.squirrel.model.user.vos;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 用户列表 vo
 */
@Data
@Builder
public class UserListVO {

    /**
     * 总数
     */
    private Integer total;

    /**
     * 用户列表
     */
    private List<UserPersonalInfoVO> list;
}
