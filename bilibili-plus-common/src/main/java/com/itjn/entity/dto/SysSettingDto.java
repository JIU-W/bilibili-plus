package com.itjn.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;

/**
 * @description 系统设置
 * @author JIU-W
 * @date 2024-12-16
 * @version 1.0
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SysSettingDto implements Serializable {

    private Integer registerCoinCount = 10;//注册送的积分数
    private Integer postVideoCoinCount = 5;//发布视频送的积分数
    private Integer videoSize = 5;//视频文件大小限制
    private Integer videoPCount = 10;//视频文件P数：发布一次作品的视频数量
    private Integer videoCount = 10;//视频文件数量限制
    private Integer commentCount = 20;//评论数
    private Integer danmuCount = 20;//弹幕数

}
