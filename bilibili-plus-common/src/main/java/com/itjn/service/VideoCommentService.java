package com.itjn.service;

import com.itjn.entity.po.VideoComment;
import com.itjn.entity.query.VideoCommentQuery;
import com.itjn.entity.vo.PaginationResultVO;

import java.util.List;


/**
 * 评论 业务接口
 */
public interface VideoCommentService {

    /**
     * 根据条件查询列表
     */
    List<VideoComment> findListByParam(VideoCommentQuery param);

    /**
     * 根据条件查询列表
     */
    Integer findCountByParam(VideoCommentQuery param);

    /**
     * 分页查询
     */
    PaginationResultVO<VideoComment> findListByPage(VideoCommentQuery param);

    /**
     * 新增
     */
    Integer add(VideoComment bean);

    /**
     * 批量新增
     */
    Integer addBatch(List<VideoComment> listBean);

    /**
     * 批量新增/修改
     */
    Integer addOrUpdateBatch(List<VideoComment> listBean);

    /**
     * 多条件更新
     */
    Integer updateByParam(VideoComment bean, VideoCommentQuery param);

    /**
     * 多条件删除
     */
    Integer deleteByParam(VideoCommentQuery param);

    /**
     * 根据CommentId查询对象
     */
    VideoComment getVideoCommentByCommentId(Integer commentId);


    /**
     * 根据CommentId修改
     */
    Integer updateVideoCommentByCommentId(VideoComment bean, Integer commentId);


    /**
     * 根据CommentId删除
     */
    Integer deleteVideoCommentByCommentId(Integer commentId);

    /**
     * 发布评论
     */
    void postComment(VideoComment comment, Integer replyCommentId);

    /**
     * 删除评论
     */
    void deleteComment(Integer commentId, String userId);

    /**
     * 置顶评论
     */
    void topComment(Integer commentId, String userId);

    /**
     * 取消置顶评论
     */
    void cancelTopComment(Integer commentId, String userId);

}
