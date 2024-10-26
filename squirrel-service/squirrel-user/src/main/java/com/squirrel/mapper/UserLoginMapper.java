package com.squirrel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.squirrel.model.user.pojos.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户登录 mapper
 */
@Mapper
public interface UserLoginMapper extends BaseMapper<User> {
}
