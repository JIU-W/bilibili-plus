package com.itjn.mappers;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 评论 数据库操作接口
 */
public interface VideoCommentMapper<T, P> extends BaseMapper<T, P> {

    /**
     * 根据CommentId更新
     */
    Integer updateByCommentId(@Param("bean") T t, @Param("commentId") Integer commentId);


    /**
     * 根据CommentId删除
     */
    Integer deleteByCommentId(@Param("commentId") Integer commentId);


    /**
     * 根据CommentId获取对象
     */
    T selectByCommentId(@Param("commentId") Integer commentId);

    /**
     * 查询一级评论，同时查询其的所有子评论
     */
    List<T> selectListWithChildren(@Param("query") P p);

    /**
     * 更新评论的点赞数和讨厌数
     */
    void updateCountInfo(@Param("commentId") Integer commentId,
                         @Param("field") String field, @Param("changeCount") Integer changeCount,
                         @Param("opposeField") String opposeField, @Param("opposeChangeCount") Integer opposeChangeCount);

}
