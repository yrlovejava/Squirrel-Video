package com.squirrel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.squirrel.model.follow.pojos.Follow;
import org.apache.ibatis.annotations.Mapper;

/**
 * 关注 mapper
 */
@Mapper
public interface UserFollowMapper extends BaseMapper<Follow> {
}
