package com.itjn.entity.vo;


import com.itjn.entity.po.VideoInfoFilePost;
import com.itjn.entity.po.VideoInfoPost;
import lombok.Data;

import java.util.List;

/**
 * 返回给前端的：投稿信息编辑页面数据
 */
@Data
public class VideoPostEditInfoVo {

    //视频投稿信息
    private VideoInfoPost videoInfo;

    //视频投稿的分p视频文件信息
    private List<VideoInfoFilePost> videoInfoFileList;


}
