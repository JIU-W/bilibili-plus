package com.itjn.entity.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.itjn.entity.enums.DateTimePatternEnum;
import com.itjn.utils.DateUtil;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.List;


/**
 * 评论
 */
public class VideoComment implements Serializable {


    /**
     * 评论ID
     */
    private Integer commentId;

    /**
     * 父级评论ID
     */
    private Integer pCommentId;

    /**
     * 视频ID
     */
    private String videoId;

    /**
     * 视频用户ID
     */
    private String videoUserId;

    /**
     * 回复内容
     */
    private String content;

    /**
     * 图片
     */
    private String imgPath;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 回复人ID
     */
    private String replyUserId;

    /**
     * 0:未置顶  1:置顶
     */
    private Integer topType;

    /**
     * 发布时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date postTime;

    /**
     * 喜欢数量
     */
    private Integer likeCount;

    /**
     * 讨厌数量
     */
    private Integer hateCount;

    /**
     * 子评论
     */
    private List<VideoComment> children;


    //返回给前端的：用户昵称
    private String nickName;

    //返回给前端的：用户头像
    private String avatar;


    //发布"回复二级评论的二级评论"时：前端要显示的回复人昵称
    private String replyNickName;

    //回复人头像
    private String replyAvatar;

    //返回给前端的：视频名称
    private String videoName;

    //返回给前端的：视频封面
    private String videoCover;

    public void setCommentId(Integer commentId) {
        this.commentId = commentId;
    }

    public Integer getCommentId() {
        return this.commentId;
    }

    public void setpCommentId(Integer pCommentId) {
        this.pCommentId = pCommentId;
    }

    public Integer getpCommentId() {
        return this.pCommentId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getVideoId() {
        return this.videoId;
    }

    public void setVideoUserId(String videoUserId) {
        this.videoUserId = videoUserId;
    }

    public String getVideoUserId() {
        return this.videoUserId;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return this.content;
    }

    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
    }

    public String getImgPath() {
        return this.imgPath;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setReplyUserId(String replyUserId) {
        this.replyUserId = replyUserId;
    }

    public String getReplyUserId() {
        return this.replyUserId;
    }

    public void setTopType(Integer topType) {
        this.topType = topType;
    }

    public Integer getTopType() {
        return this.topType;
    }

    public void setPostTime(Date postTime) {
        this.postTime = postTime;
    }

    public Date getPostTime() {
        return this.postTime;
    }

    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }

    public Integer getLikeCount() {
        return this.likeCount;
    }

    public void setHateCount(Integer hateCount) {
        this.hateCount = hateCount;
    }

    public Integer getHateCount() {
        return this.hateCount;
    }

    public List<VideoComment> getChildren() {
        return children;
    }

    public void setChildren(List<VideoComment> children) {
        this.children = children;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getReplyNickName() {
        return replyNickName;
    }

    public void setReplyNickName(String replyNickName) {
        this.replyNickName = replyNickName;
    }

    public String getReplyAvatar() {
        return replyAvatar;
    }

    public void setReplyAvatar(String replyAvatar) {
        this.replyAvatar = replyAvatar;
    }

    public String getVideoName() {
        return videoName;
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }

    public String getVideoCover() {
        return videoCover;
    }

    public void setVideoCover(String videoCover) {
        this.videoCover = videoCover;
    }

    @Override
    public String toString() {
        return "评论ID:" + (commentId == null ? "空" : commentId) + "，父级评论ID:" + (pCommentId == null ? "空" : pCommentId) + "，视频ID:" + (videoId == null ? "空" : videoId) +
                "，视频用户ID:" + (videoUserId == null ? "空" : videoUserId) + "，回复内容:" + (content == null ? "空" : content) + "，图片:" + (imgPath == null ? "空" : imgPath) +
                "，用户ID:" + (userId == null ? "空" : userId) + "，回复人ID:" + (replyUserId == null ? "空" : replyUserId) + "，0:未置顶  1:置顶:" + (topType == null ? "空" : topType) + "，发布时间:" + (postTime == null ? "空" : DateUtil.format(
                postTime,
                DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern())) + "，喜欢数量:" + (likeCount == null ? "空" : likeCount) + "，讨厌数量:" + (hateCount == null ? "空" : hateCount);
    }
}
