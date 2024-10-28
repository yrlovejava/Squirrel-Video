package com.squirrel.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.squirrel.exception.DbOperationException;
import com.squirrel.exception.NullParamException;
import com.squirrel.mapper.FollowMapper;
import com.squirrel.model.follow.dtos.FollowEachOtherDTO;
import com.squirrel.model.follow.pojos.Follow;
import com.squirrel.model.response.ResponseResult;
import com.squirrel.service.FollowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 关注接口的实现类
 */
@Slf4j
@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements FollowService {

    /**
     * 是否相关关注
     * @param dto 是否相互关注的 dto
     * @return ResponseResult 是否相互关注
     */
    @Override
    public ResponseResult<Boolean> isFollowEachOther(FollowEachOtherDTO dto) {
        // 1.参数校验
        if (dto == null || dto.getFirstUserId() == null || dto.getSecondUserId() == null) {
            throw new NullParamException();
        }

        // 2.查询数据库
        boolean isFollowEachOther = false;
        try {
            Long count1 = getBaseMapper().selectCount(Wrappers.<Follow>lambdaQuery()
                    .eq(Follow::getFollowId, dto.getFirstUserId()) // 第一个用户被关注
                    .eq(Follow::getUserId, dto.getSecondUserId()) // 第二个用户关注
            );
            Long count2 = getBaseMapper().selectCount(Wrappers.<Follow>lambdaQuery()
                    .eq(Follow::getFollowId, dto.getSecondUserId()) // 第二个用户被关注
                    .eq(Follow::getUserId, dto.getFirstUserId()) // 第一个用户关注
            );
            if (count1 == 1 && count2 == 1){
                isFollowEachOther = true;
            }
        }catch (Exception ex){
            log.error("查询数据库出错: {}",ex.toString());
            throw new DbOperationException("查询数据库出错");
        }

        // 3.返回结果
        return ResponseResult.successResult(isFollowEachOther);
    }


}
