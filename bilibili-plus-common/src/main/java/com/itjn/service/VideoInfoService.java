package com.itjn.service;

import com.itjn.entity.po.VideoInfo;
import com.itjn.entity.query.VideoInfoQuery;
import com.itjn.entity.vo.PaginationResultVO;

import java.util.List;


/**
 * 视频信息 业务接口
 */
public interface VideoInfoService {

    /**
     * 根据条件查询列表
     */
    List<VideoInfo> findListByParam(VideoInfoQuery param);

    /**
     * 根据条件查询列表
     */
    Integer findCountByParam(VideoInfoQuery param);

    /**
     * 分页查询
     */
    PaginationResultVO<VideoInfo> findListByPage(VideoInfoQuery param);

    /**
     * 新增
     */
    Integer add(VideoInfo bean);

    /**
     * 批量新增
     */
    Integer addBatch(List<VideoInfo> listBean);

    /**
     * 批量新增/修改
     */
    Integer addOrUpdateBatch(List<VideoInfo> listBean);

    /**
     * 多条件更新
     */
    Integer updateByParam(VideoInfo bean, VideoInfoQuery param);

    /**
     * 多条件删除
     */
    Integer deleteByParam(VideoInfoQuery param);

    /**
     * 根据VideoId查询对象
     */
    VideoInfo getVideoInfoByVideoId(String videoId);


    /**
     * 根据VideoId修改
     */
    Integer updateVideoInfoByVideoId(VideoInfo bean, String videoId);


    /**
     * 根据VideoId删除
     */
    Integer deleteVideoInfoByVideoId(String videoId);


    /**
     * 增加投稿作品播放量
     */
    void addReadCount(String videoId);

    /**
     * 修改投稿的互动信息
     */
    void changeInteraction(String videoId, String userId, String interaction);

    /**
     * 删除投稿
     * @param videoId
     * @param userId
     */
    void deleteVideo(String videoId, String userId);

    /**
     * 推荐、取消推荐视频
     * @param videoId
     */
    void recommendVideo(String videoId);

}
