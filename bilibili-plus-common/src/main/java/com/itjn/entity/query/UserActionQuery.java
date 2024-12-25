package com.itjn.entity.query;

import lombok.Data;

/**
 * 用户行为 点赞、评论参数
 */
@Data
public class UserActionQuery extends BaseParam {


    /**
     * 自增ID
     */
    private Integer actionId;

    /**
     * 视频ID
     */
    private String videoId;

    private String videoIdFuzzy;

    /**
     * 视频用户ID
     */
    private String videoUserId;

    private String videoUserIdFuzzy;

    /**
     * 评论ID
     */
    private Integer commentId;

    /**
     * 0:评论喜欢点赞 1:讨厌评论 2:视频点赞 3:视频收藏 4:视频投币
     */
    private Integer actionType;

    /**
     * 数量
     */
    private Integer actionCount;

    /**
     * 用户ID
     */
    private String userId;

    private String userIdFuzzy;

    /**
     * 操作时间
     */
    private String actionTime;

    private String actionTimeStart;

    private String actionTimeEnd;

    private Integer[] actionTypeArray;

    private Boolean queryVideoInfo;

}
