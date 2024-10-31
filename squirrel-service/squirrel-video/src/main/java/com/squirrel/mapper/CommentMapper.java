package com.squirrel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.squirrel.model.comment.Comment;
import org.apache.ibatis.annotations.Mapper;

/**
 * comment mapper
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
}
