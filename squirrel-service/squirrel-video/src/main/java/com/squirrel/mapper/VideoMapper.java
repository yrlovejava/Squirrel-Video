package com.squirrel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.squirrel.model.video.pojos.Video;
import org.apache.ibatis.annotations.Mapper;

/**
 * video mapper
 */
@Mapper
public interface VideoMapper extends BaseMapper<Video> {
}
