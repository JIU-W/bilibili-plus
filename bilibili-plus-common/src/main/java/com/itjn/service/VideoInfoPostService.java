package com.itjn.service;

import com.itjn.entity.po.VideoInfoFilePost;
import com.itjn.entity.po.VideoInfoPost;
import com.itjn.entity.query.VideoInfoPostQuery;
import com.itjn.entity.vo.PaginationResultVO;

import java.io.IOException;
import java.util.List;


/**
 * 视频信息 业务接口
 */
public interface VideoInfoPostService {

    /**
     * 根据条件查询列表
     */
    List<VideoInfoPost> findListByParam(VideoInfoPostQuery param);

    /**
     * 根据条件查询列表
     */
    Integer findCountByParam(VideoInfoPostQuery param);

    /**
     * 分页查询投稿视频列表
     */
    PaginationResultVO<VideoInfoPost> findListByPage(VideoInfoPostQuery param);

    /**
     * 新增
     */
    Integer add(VideoInfoPost bean);

    /**
     * 批量新增
     */
    Integer addBatch(List<VideoInfoPost> listBean);

    /**
     * 批量新增/修改
     */
    Integer addOrUpdateBatch(List<VideoInfoPost> listBean);

    /**
     * 多条件更新
     */
    Integer updateByParam(VideoInfoPost bean, VideoInfoPostQuery param);

    /**
     * 多条件删除
     */
    Integer deleteByParam(VideoInfoPostQuery param);

    /**
     * 根据VideoId查询对象
     */
    VideoInfoPost getVideoInfoPostByVideoId(String videoId);


    /**
     * 根据VideoId修改
     */
    Integer updateVideoInfoPostByVideoId(VideoInfoPost bean, String videoId);


    /**
     * 根据VideoId删除
     */
    Integer deleteVideoInfoPostByVideoId(String videoId);

    /**
     * 发布投稿、修改已投稿的信息
     */
    void saveVideoInfo(VideoInfoPost videoInfoPost, List<VideoInfoFilePost> uploadFileList);

    /**
     * 文件转码
     * @param videoInfoFilePost
     */
    void transferVideoFile(VideoInfoFilePost videoInfoFilePost) throws IOException;

    /**
     * 审核投稿(审核视频)
     * @param videoId
     * @param status
     * @param reason
     */
    void auditVideo(String videoId, Integer status, String reason);

}
