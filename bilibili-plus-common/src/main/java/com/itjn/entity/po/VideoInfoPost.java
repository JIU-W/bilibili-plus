package com.itjn.entity.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.itjn.entity.enums.DateTimePatternEnum;
import com.itjn.entity.enums.VideoStatusEnum;
import com.itjn.utils.DateUtil;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;


/**
 * 视频信息---发布表(发布时的投稿信息)
 */
@Data
public class VideoInfoPost extends VideoInfo implements Serializable {


    /**
     * 视频ID
     */
    private String videoId;

    /**
     * 视频封面
     */
    private String videoCover;

    /**
     * 视频名称
     */
    private String videoName;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 最后更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastUpdateTime;

    /**
     * 父级分类ID
     */
    @JsonProperty("pCategoryId") //显式指定JSON字段名称
    private Integer pCategoryId;

    /**
     * 分类ID
     */
    private Integer categoryId;

    /**
     * 0:转码中 1转码失败 2:待审核 3:审核成功 4:审核失败
     */
    private Integer status;

    /**
     * 0:自制作  1:转载
     */
    private Integer postType;

    /**
     * 原资源说明
     */
    private String originInfo;

    /**
     * 标签
     */
    private String tags;

    /**
     * 简介
     */
    private String introduction;

    /**
     * 互动设置
     */
    private String interaction;

    /**
     * 持续时间（秒）
     */
    private Integer duration;

    /**
     * 状态名称：便于传给前端显示
     */
    private String statusName;

    //这个get方法一定不能去掉：用来设置状态名称
    public String getStatusName() {
        //获取状态名称
        VideoStatusEnum statusEnum = VideoStatusEnum.getByStatus(status);
        return statusEnum == null ? "未知" : statusEnum.getDesc();
    }

    @Override
    public String toString() {
        return "视频ID:" + (videoId == null ? "空" : videoId) + "，视频封面:" + (videoCover == null ? "空" : videoCover) + "，视频名称:" + (videoName == null ? "空" : videoName) + "，用户ID:" + (userId == null ?
                "空" : userId) + "，创建时间:" + (createTime == null ? "空" : DateUtil.format(
                createTime,
                DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern())) + "，最后更新时间:" + (lastUpdateTime == null ? "空" : DateUtil.format(lastUpdateTime,
                DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern())) + "，父级分类ID:" + (pCategoryId == null ? "空" : pCategoryId) + "，分类ID:" + (categoryId == null ? "空" : categoryId) + "，0:转码中 1转码失败 " +
                "2:待审核 3:审核成功 4:审核失败:" + (status == null ? "空" : status) + "，0:自制作  1:转载:" + (postType == null ? "空" : postType) + "，原资源说明:" + (originInfo == null ? "空" : originInfo) + "，标签:" + (tags == null ? "空" : tags) + "，简介:" + (introduction == null ? "空" : introduction) + "，互动设置:" + (interaction == null ? "空" : interaction) + "，持续时间（秒）:" + (duration == null ? "空" : duration);
    }

}
