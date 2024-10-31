package com.squirrel.service.impl;

import com.alibaba.fastjson2.JSON;
import com.squirrel.clients.IVideoClient;
import com.squirrel.exception.NullParamException;
import com.squirrel.exception.SearchException;
import com.squirrel.model.response.ResponseResult;
import com.squirrel.model.user.pojos.User;
import com.squirrel.model.user.vos.UserListVO;
import com.squirrel.model.user.vos.UserPersonInfoVO;
import com.squirrel.model.video.pojos.Video;
import com.squirrel.model.video.vos.VideoDetail;
import com.squirrel.model.video.vos.VideoListVO;
import com.squirrel.service.SearchService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.xml.builders.BooleanQueryBuilder;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 搜索服务接口实现类
 */
@Slf4j
@Service
public class SearchServiceImpl implements SearchService {

    @Resource
    private RestHighLevelClient restHighLevelClient;

    @Resource
    private IVideoClient videoClient;

    /**
     * 搜索用户
     *
     * @param keyword  关键字
     * @param page     页码
     * @param pageSize 页大小
     * @return 用户列表
     */
    @Override
    public ResponseResult<UserListVO> searchUser(String keyword, Integer page, Integer pageSize) {
        // 1.参数校验
        if (StringUtils.isBlank(keyword) || page == null || pageSize == null) {
            throw new NullParamException();
        }
        log.info("搜索用户，关键字: {},页码: {}，页大小: {}", keyword, page, pageSize);

        // 2.设置查询条件
        SearchRequest searchRequest = new SearchRequest("tb_user");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 2.1布尔查询
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        // 2.2设置查询关键词
        QueryStringQueryBuilder queryStringQueryBuilder = new QueryStringQueryBuilder(keyword);
        boolQueryBuilder.must(queryStringQueryBuilder);
        // 2.3设置分页
        // 页码从1开始
        searchSourceBuilder.from((page - 1) * pageSize);
        // 页大小
        searchSourceBuilder.size(pageSize);
        // 设置排序
        searchSourceBuilder.sort("id", SortOrder.DESC);
        // 设置查询条件
        searchSourceBuilder.query(boolQueryBuilder);
        // 设置查询源
        searchRequest.source(searchSourceBuilder);

        // 3.执行查询
        List< UserPersonInfoVO> usrInfoList = new ArrayList<>();
        try {
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            // 返回结果
            SearchHit[] hits = searchResponse.getHits().getHits();
            for (SearchHit hit : hits) {
                String json = hit.getSourceAsString();
                User user = JSON.parseObject(json, User.class);
                log.info("搜索结果: {}",user);
                UserPersonInfoVO vo = new UserPersonInfoVO();
                BeanUtils.copyProperties(user,vo);
                vo.setId(user.getId().toString());
                usrInfoList.add(vo);
            }
        }catch (Exception e){
            log.error("搜索用户失败: {}",e.toString());
            throw new SearchException("搜索用户失败");
        }

        // 4.封装返回vo
        UserListVO userListVO = UserListVO.builder()
                .total(usrInfoList.size())
                .list(usrInfoList)
                .build();

        // 5.返回结果
        return ResponseResult.successResult(userListVO);
    }

    /**
     * 搜索视频
     *
     * @param keyword  关键字
     * @param page     页码
     * @param pageSize 页大小
     * @return 视频列表
     */
    @Override
    public ResponseResult<VideoListVO> searchVideo(String keyword, Integer page, Integer pageSize) {
        // 1.参数校验
        if (StringUtils.isBlank(keyword) || page == null || pageSize == null) {
            throw new NullParamException();
        }
        log.info("搜索视频，关键字: {},页码: {}，页大小: {}", keyword, page, pageSize);

        // 2.设置查询条件
        SearchRequest searchRequest = new SearchRequest("tb_video");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 2.1布尔查询
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        // 2.2设置查询关键词
        QueryStringQueryBuilder queryStringQueryBuilder = new QueryStringQueryBuilder(keyword);
        boolQueryBuilder.must(queryStringQueryBuilder);
        // 2.3设置分页
        // 页码从1开始
        searchSourceBuilder.from((page - 1) * pageSize);
        // 页大小
        searchSourceBuilder.size(pageSize);
        // 设置排序
        searchSourceBuilder.sort("id", SortOrder.DESC);
        // 设置查询条件
        searchSourceBuilder.query(boolQueryBuilder);
        // 设置查询源
        searchRequest.source(searchSourceBuilder);

        // 3.执行查询
        List<VideoDetail> videoDetailList = new ArrayList<>();
        try {
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            // 返回结果
            SearchHit[] hits = searchResponse.getHits().getHits();
            for (SearchHit hit : hits) {
                String json = hit.getSourceAsString();
                Video video = JSON.parseObject(json, Video.class);
                log.info("搜索结果: {}",video);
                VideoDetail videoDetail = videoClient.getVideoDetailInfo(video.getId()).getData();
                videoDetailList.add(videoDetail);
            }
        }catch (Exception e){
            log.error("搜索用户失败: {}",e.toString());
            throw new SearchException("搜索用户失败");
        }

        // 4.封装返回vo
        VideoListVO userListVO = VideoListVO.builder()
                .total(videoDetailList.size())
                .videoList(videoDetailList)
                .build();

        // 5.返回结果
        return ResponseResult.successResult(userListVO);
    }
}
