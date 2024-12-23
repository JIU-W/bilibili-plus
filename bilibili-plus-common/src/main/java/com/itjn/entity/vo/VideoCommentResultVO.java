package com.itjn.entity.vo;

import com.itjn.entity.po.UserAction;
import com.itjn.entity.po.VideoComment;

import java.util.List;

/**
 *  加载评论时返回的VO封装
 */
public class VideoCommentResultVO {

    //评论数据
    private PaginationResultVO<VideoComment> commentData;

    //当前用户的用户行为：包括  1.评论点赞  2.评论讨厌
    private List<UserAction> userActionList;

    public PaginationResultVO<VideoComment> getCommentData() {
        return commentData;
    }

    public void setCommentData(PaginationResultVO<VideoComment> commentData) {
        this.commentData = commentData;
    }

    public List<UserAction> getUserActionList() {
        return userActionList;
    }

    public void setUserActionList(List<UserAction> userActionList) {
        this.userActionList = userActionList;
    }
}
