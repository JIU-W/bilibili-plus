package com.itjn.admin.controller;

import com.itjn.annotation.RecordUserMessage;
import com.itjn.entity.enums.MessageTypeEnum;
import com.itjn.entity.po.VideoInfoFilePost;
import com.itjn.entity.query.VideoInfoFilePostQuery;
import com.itjn.entity.query.VideoInfoPostQuery;
import com.itjn.entity.vo.PaginationResultVO;
import com.itjn.entity.vo.ResponseVO;
import com.itjn.service.VideoInfoFilePostService;
import com.itjn.service.VideoInfoPostService;
import com.itjn.service.VideoInfoService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@Validated
@RequestMapping("/videoInfo")
public class VideoInfoController extends ABaseController {

    @Resource
    private VideoInfoPostService videoInfoPostService;

    @Resource
    private VideoInfoFilePostService videoInfoFilePostService;

    @Resource
    private VideoInfoService videoInfoService;

    /**
     * 加载所有用户的投稿列表
     * @return
     */
    @RequestMapping("/loadVideoList")
    public ResponseVO loadVideoList(VideoInfoPostQuery videoInfoPostQuery) {
        videoInfoPostQuery.setOrderBy("last_update_time desc");
        //设置要查询数量相关信息
        videoInfoPostQuery.setQueryCountInfo(true);
        //设置要查询投稿相关用户信息
        videoInfoPostQuery.setQueryUserInfo(true);
        PaginationResultVO resultVO = videoInfoPostService.findListByPage(videoInfoPostQuery);
        return getSuccessResponseVO(resultVO);
    }

    /**
     * 审核投稿
     * @param videoId
     * @param status 审核结果
     * @param reason 审核不通过的原因
     * @return
     */
    @RequestMapping("/auditVideo")
    @RecordUserMessage(messageType = MessageTypeEnum.SYS)
    public ResponseVO auditVideo(@NotEmpty String videoId, @NotNull Integer status, String reason) {
        videoInfoPostService.auditVideo(videoId, status, reason);
        return getSuccessResponseVO(null);
    }

    /**
     * 推荐、取消推荐视频
     * @param videoId
     * @return
     */
    @RequestMapping("/recommendVideo")
    public ResponseVO recommendVideo(@NotEmpty String videoId) {
        videoInfoService.recommendVideo(videoId);
        return getSuccessResponseVO(null);
    }

    /**
     * 删除投稿视频
     * @param videoId
     * @return
     */
    @RequestMapping("/deleteVideo")
    public ResponseVO deleteVideo(@NotEmpty String videoId) {
        videoInfoService.deleteVideo(videoId, null);
        return getSuccessResponseVO(null);
    }

    /*


    @RequestMapping("/loadVideoPList")
    public ResponseVO loadVideoPList(@NotEmpty String videoId) {
        VideoInfoFilePostQuery postQuery = new VideoInfoFilePostQuery();
        postQuery.setOrderBy("file_index asc");
        postQuery.setVideoId(videoId);
        List<VideoInfoFilePost> videoInfoFilePostsList = videoInfoFilePostService.findListByParam(postQuery);
        return getSuccessResponseVO(videoInfoFilePostsList);
    }*/


}
