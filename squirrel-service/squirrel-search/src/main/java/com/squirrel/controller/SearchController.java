package com.squirrel.controller;

import com.squirrel.model.response.ResponseResult;
import com.squirrel.model.user.vos.UserListVO;
import com.squirrel.model.video.vos.VideoListVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 搜索服务控制器
 */
@RestController
@RequestMapping("/azaz/search")
public class SearchController {

    /**
     * 搜索用户
     * @param keyword 关键字
     * @param page 页码
     * @param pageSize 页大小
     * @return 用户列表
     */
    @GetMapping("/user")
    public ResponseResult<UserListVO> searchUser(String keyword,Integer page,Integer pageSize) {

    }

    /**
     * 搜索视频
     * @param keyword 关键字
     * @param page 页码
     * @param pageSize 页大小
     * @return 视频列表
     */
    @GetMapping("/video")
    public ResponseResult<VideoListVO> searchVideo(String keyword, Integer page, Integer pageSize) {

    }

}
