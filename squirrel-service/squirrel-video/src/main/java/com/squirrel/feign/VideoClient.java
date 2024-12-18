package com.squirrel.feign;

import com.squirrel.clients.IVideoClient;
import com.squirrel.model.response.ResponseResult;
import com.squirrel.model.video.vos.VideoDetail;
import com.squirrel.model.video.vos.VideoInfo;
import com.squirrel.service.VideoDoLikeService;
import com.squirrel.service.VideoUploadService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 视频远程接口的实现
 */
@RestController
public class VideoClient implements IVideoClient {

    @Resource
    private VideoDoLikeService videoDoLikeService;

    @Resource
    private VideoUploadService videoUploadService;

    /**
     * 得到用户被赞数
     *
     * @param userId 用户id
     * @return ResponseResult<Integer> 用户被赞数
     */
    @GetMapping("/azaz/video/getUserLikes")
    @Override
    public ResponseResult<Integer> getUserLikes(@RequestParam("userId") Long userId) {
        return videoDoLikeService.getUserLikes(userId);
    }

    /**
     * 得到用户作评总数
     *
     * @param userId 用户id
     * @return 作品总数
     */
    @GetMapping("/azaz/video/getUserWorks")
    @Override
    public ResponseResult<Integer> getUserWorks(Long userId) {
        return videoDoLikeService.getUserWorks(userId);
    }

    /**
     * 得到用户被收藏数
     *
     * @param userId 用户id
     * @return ResponseResult<Integer> 用户被收藏数
     */
    @GetMapping("/azaz/video/getUserCollects")
    public ResponseResult<Integer> getUserCollects(@RequestParam("userId") Long userId) {
        return videoDoLikeService.getUserCollects(userId);
    }

    /**
     * 得到视频简略信息
     *
     * @param videoId 视频id
     * @return 视频简略信息
     */
    @GetMapping("/azaz/video/info")
    @Override
    public ResponseResult<VideoInfo> getVideoInfo(Long videoId) {
        return videoUploadService.getVideoInfo(videoId);
    }

    /**
     * 得到视频详细信息
     *
     * @param videoId 视频id
     * @return 视频详细信息
     */
    @GetMapping("/azaz/video/detailInfo")
    @Override
    public ResponseResult<VideoDetail> getVideoDetailInfo(Long videoId) {
        return videoUploadService.getVideoDetailInfo(videoId);
    }
}
