package com.itjn.web.controller;

import com.itjn.entity.constants.Constants;
import com.itjn.entity.po.VideoDanmu;
import com.itjn.entity.po.VideoInfo;
import com.itjn.entity.query.VideoDanmuQuery;
import com.itjn.entity.vo.ResponseVO;
import com.itjn.service.VideoDanmuService;
import com.itjn.service.impl.VideoInfoServiceImpl;
import com.itjn.web.annotation.GlobalInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Date;

@RestController
@Validated
@RequestMapping("/danmu")
@Slf4j
public class VideoDanmuController extends ABaseController {

    @Resource
    private VideoDanmuService videoDanmuService;

    @Resource
    private VideoInfoServiceImpl videoInfoService;

    /**
     * 发布弹幕
     * @param videoId 弹幕属于的投稿(视频)
     * @param fileId 弹幕属于这个投稿下的具体的分p视频
     * @param text 内容
     * @param mode 展示位置
     * @param color 颜色
     * @param time 展示时间
     * @return
     */
    @RequestMapping("/postDanmu")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO postDanmu(@NotEmpty String videoId, @NotEmpty String fileId,
                                @NotEmpty @Size(max = 200) String text, @NotNull Integer mode,
                                @NotEmpty String color, @NotNull Integer time) {

        log.info(Thread.currentThread().getName(),"当前线程名称：{}");

        VideoDanmu videoDanmu = new VideoDanmu();
        videoDanmu.setVideoId(videoId);
        videoDanmu.setFileId(fileId);
        videoDanmu.setText(text);
        videoDanmu.setMode(mode);
        videoDanmu.setColor(color);
        videoDanmu.setTime(time);
        videoDanmu.setUserId(getTokenUserInfoDto().getUserId());
        videoDanmu.setPostTime(new Date());//发布时间
        //发布弹幕
        videoDanmuService.saveVideoDanmu(videoDanmu);
        return getSuccessResponseVO(null);
    }


    /**
     * 加载弹幕
     * @param fileId 弹幕属于这个投稿下的具体的分p视频
     * @param videoId 弹幕属于的投稿(视频)
     * @return
     */
    @RequestMapping("/loadDanmu")
    @GlobalInterceptor
    public ResponseVO loadDanmu(@NotEmpty String fileId, @NotEmpty String videoId) {
        VideoInfo videoInfo = videoInfoService.getVideoInfoByVideoId(videoId);
        //投稿是否关闭弹幕这个功能                                                              //ZERO
        if (videoInfo.getInteraction() != null && videoInfo.getInteraction().contains(Constants.ONE.toString())) {
            return getSuccessResponseVO(new ArrayList<>());
        }
        //根据fileId(加了普通索引：用于加快查询)查询弹幕，也就是查询这个分p视频下的所有弹幕。
        VideoDanmuQuery videoDanmuQuery = new VideoDanmuQuery();
        videoDanmuQuery.setFileId(fileId);
        videoDanmuQuery.setOrderBy("danmu_id asc");
        return getSuccessResponseVO(videoDanmuService.findListByParam(videoDanmuQuery));
    }


}
