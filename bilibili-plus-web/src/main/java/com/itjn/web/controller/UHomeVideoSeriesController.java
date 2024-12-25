package com.itjn.web.controller;

import com.itjn.entity.dto.TokenUserInfoDto;
import com.itjn.entity.enums.ResponseCodeEnum;
import com.itjn.entity.po.UserVideoSeries;
import com.itjn.entity.po.UserVideoSeriesVideo;
import com.itjn.entity.po.VideoInfo;
import com.itjn.entity.query.UserVideoSeriesQuery;
import com.itjn.entity.query.UserVideoSeriesVideoQuery;
import com.itjn.entity.query.VideoInfoQuery;
import com.itjn.entity.vo.ResponseVO;
import com.itjn.entity.vo.UserVideoSeriesDetailVO;
import com.itjn.exception.BusinessException;
import com.itjn.service.UserVideoSeriesService;
import com.itjn.service.UserVideoSeriesVideoService;
import com.itjn.service.VideoInfoService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.stream.Collectors;


@RestController
@Validated
@RequestMapping("/uhome/series")
public class UHomeVideoSeriesController extends ABaseController {

    @Resource
    private VideoInfoService videoInfoService;

    @Resource
    private UserVideoSeriesService userVideoSeriesService;

    @Resource
    private UserVideoSeriesVideoService userVideoSeriesVideoService;

    /**
     * 查询用户视频系列归档(视频合集)(给视频分类的集合)
     * @param userId
     * @return
     */
    @RequestMapping("/loadVideoSeries")
    //@GlobalInterceptor
    public ResponseVO loadVideoSeries(@NotEmpty String userId) {
        //不分页查询，因为每个用户的视频分类合集不会太多
        List<UserVideoSeries> videoSeries = userVideoSeriesService.getUserAllSeries(userId);
        return getSuccessResponseVO(videoSeries);
    }

    /**
     * 保存用户视频的集合：   1.创建(没有传seriesId)  2.修改(传了seriesId)
     * @param seriesId
     * @param seriesName
     * @param seriesDescription
     * @param videoIds 其中创建的同时必须添加视频(videoIds)到合集
     * @return
     */
    @RequestMapping("/saveVideoSeries")
    //@GlobalInterceptor(checkLogin = true)
    public ResponseVO saveVideoSeries(Integer seriesId, @NotEmpty @Size(max = 100) String seriesName,
                                      @Size(max = 200) String seriesDescription, String videoIds) {
        //获取当前登录用户信息
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        UserVideoSeries videoSeries = new UserVideoSeries();
        videoSeries.setUserId(tokenUserInfoDto.getUserId());
        videoSeries.setSeriesId(seriesId);
        videoSeries.setSeriesName(seriesName);
        videoSeries.setSeriesDescription(seriesDescription);
        userVideoSeriesService.saveUserVideoSeries(videoSeries, videoIds);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/loadAllVideo")
    //@GlobalInterceptor(checkLogin = true)
    public ResponseVO loadAllVideo(Integer seriesId) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        VideoInfoQuery infoQuery = new VideoInfoQuery();
        if (seriesId != null) {
            UserVideoSeriesVideoQuery videoSeriesVideoQuery = new UserVideoSeriesVideoQuery();
            videoSeriesVideoQuery.setSeriesId(seriesId);
            videoSeriesVideoQuery.setUserId(tokenUserInfoDto.getUserId());
            List<UserVideoSeriesVideo> seriesVideoList = userVideoSeriesVideoService.findListByParam(videoSeriesVideoQuery);
            List<String> videoList = seriesVideoList.stream().map(item -> item.getVideoId()).collect(Collectors.toList());
            infoQuery.setExcludeVideoIdArray(videoList.toArray(new String[videoList.size()]));
        }
        infoQuery.setUserId(tokenUserInfoDto.getUserId());
        List<VideoInfo> videoInfoList = videoInfoService.findListByParam(infoQuery);
        return getSuccessResponseVO(videoInfoList);
    }

    @RequestMapping("/getVideoSeriesDetail")
    //@GlobalInterceptor
    public ResponseVO getVideoSeriesDetail(@NotNull Integer seriesId) {
        UserVideoSeries videoSeries = userVideoSeriesService.getUserVideoSeriesBySeriesId(seriesId);
        if (videoSeries == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_404);
        }

        UserVideoSeriesVideoQuery videoSeriesVideoQuery = new UserVideoSeriesVideoQuery();
        videoSeriesVideoQuery.setOrderBy("sort asc");
        videoSeriesVideoQuery.setQueryVideoInfo(true);
        videoSeriesVideoQuery.setSeriesId(seriesId);
        List<UserVideoSeriesVideo> seriesVideoList = userVideoSeriesVideoService.findListByParam(videoSeriesVideoQuery);
        return getSuccessResponseVO(new UserVideoSeriesDetailVO(videoSeries, seriesVideoList));
    }

    /**
     * 保存系列视频
     *
     * @param seriesId
     * @param videoIds
     * @return
     */
    @RequestMapping("/saveSeriesVideo")
    //@GlobalInterceptor(checkLogin = true)
    public ResponseVO saveSeriesVideo(@NotNull Integer seriesId, @NotEmpty String videoIds) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        userVideoSeriesService.saveSeriesVideo(tokenUserInfoDto.getUserId(), seriesId, videoIds);
        return getSuccessResponseVO(null);
    }

    /**
     * 删除视频
     *
     * @param seriesId
     * @param videoId
     * @return
     */
    @RequestMapping("/delSeriesVideo")
    //@GlobalInterceptor(checkLogin = true)
    public ResponseVO delSeriesVideo(@NotNull Integer seriesId, @NotEmpty String videoId) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        userVideoSeriesService.delSeriesVideo(tokenUserInfoDto.getUserId(), seriesId, videoId);
        return getSuccessResponseVO(null);
    }

    /**
     * 删除系列
     *
     * @param seriesId
     * @return
     */
    @RequestMapping("/delVideoSeries")
    //@GlobalInterceptor(checkLogin = true)
    public ResponseVO delVideoSeries(@NotNull Integer seriesId) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        userVideoSeriesService.delVideoSeries(tokenUserInfoDto.getUserId(), seriesId);
        return getSuccessResponseVO(null);
    }


    /**
     * 系列排序
     *
     * @param seriesIds
     * @return
     */
    @RequestMapping("/changeVideoSeriesSort")
    //@GlobalInterceptor(checkLogin = true)
    public ResponseVO changeVideoSeriesSort(@NotEmpty String seriesIds) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        userVideoSeriesService.changeVideoSeriesSort(tokenUserInfoDto.getUserId(), seriesIds);
        return getSuccessResponseVO(null);
    }


    @RequestMapping("/loadVideoSeriesWithVideo")
    //@GlobalInterceptor
    public ResponseVO loadVideoSeriesWithVideo(@NotEmpty String userId) {
        UserVideoSeriesQuery seriesQuery = new UserVideoSeriesQuery();
        seriesQuery.setUserId(userId);
        seriesQuery.setOrderBy("sort asc");
        List<UserVideoSeries> videoSeries = userVideoSeriesService.findListWithVideoList(seriesQuery);
        return getSuccessResponseVO(videoSeries);
    }

}
