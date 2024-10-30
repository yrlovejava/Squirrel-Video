package com.squirrel.clients;

import com.squirrel.model.response.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 视频服务的远程接口
 */
@FeignClient("squirrel-video")
public interface IVideoClient {

    /**
     * 得到用户被赞数
     * @param userId 用户id
     * @return ResponseResult<Integer> 被赞数
     */
    @GetMapping("/azaz/video/getUserLikes")
    ResponseResult<Integer> getUserLikes(@RequestParam("userId") Long userId);

    /**
     * 得到用户作评总数
     * @param userId 用户id
     * @return 作品总数
     */
    @GetMapping("/azaz/video/getUserWorks")
    ResponseResult<Integer> getUserWorks(@RequestParam("userId") Long userId);
}
