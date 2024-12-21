package com.itjn.entity.vo;

import com.itjn.entity.po.UserAction;

import java.util.List;

public class VideoInfoResultVo {

    //投稿视频信息
    private VideoInfoVo videoInfo;

    //用户行为信息
    private List<UserAction> userActionList;

    public VideoInfoVo getVideoInfo() {
        return videoInfo;
    }

    public void setVideoInfo(VideoInfoVo videoInfo) {
        this.videoInfo = videoInfo;
    }

    public List<UserAction> getUserActionList() {
        return userActionList;
    }

    public void setUserActionList(List<UserAction> userActionList) {
        this.userActionList = userActionList;
    }

}
