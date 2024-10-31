package com.squirrel.service;

import com.squirrel.model.response.ResponseResult;
import com.squirrel.model.user.vos.UserListVO;
import com.squirrel.model.video.vos.VideoListVO;

/**
 * 搜索服务接口
 */
public interface SearchService {

    /**
     * 搜索用户
     * @param keyword 关键字
     * @param page 页码
     * @param pageSize 页大小
     * @return 用户列表
     */
    ResponseResult<UserListVO> searchUser(String keyword,Integer page,Integer pageSize);

    /**
     * 搜索视频
     * @param keyword 关键字
     * @param page 页码
     * @param pageSize 页大小
     * @return 视频列表
     */
    ResponseResult<VideoListVO> searchVideo(String keyword, Integer page, Integer pageSize);
}
