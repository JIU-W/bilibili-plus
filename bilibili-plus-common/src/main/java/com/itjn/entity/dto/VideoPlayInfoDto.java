package com.itjn.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * @description 视频播放信息
 * @author JIU-W
 * @date 2025-01-09
 * @version 1.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class VideoPlayInfoDto implements Serializable {
    private String videoId;
    private String userId;
    private Integer fileIndex;

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Integer getFileIndex() {
        return fileIndex;
    }

    public void setFileIndex(Integer fileIndex) {
        this.fileIndex = fileIndex;
    }
}
