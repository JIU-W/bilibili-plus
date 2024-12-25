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
        //同时还需要把"合集"里的"第一条视频数据的封面"查出来作为"这个合集的封面"！！！
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

    /**
     * 查询出当前用户的视频供给选择(往合集里加视频时就需要先查出来再选择)
     *                              前提：要过滤掉已经存在合集里的视频
     * @param seriesId
     * @return
     */
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
            //取出集合seriesVideoList里的视频id
            List<String> videoList = seriesVideoList.stream()
                    .map(item -> item.getVideoId()).collect(Collectors.toList());
            //排除掉已经存在的视频
            infoQuery.setExcludeVideoIdArray(videoList.toArray(new String[videoList.size()]));
        }
        infoQuery.setUserId(tokenUserInfoDto.getUserId());
        List<VideoInfo> videoInfoList = videoInfoService.findListByParam(infoQuery);
        return getSuccessResponseVO(videoInfoList);
    }

    /**
     * 获取某个视频合集里的详情信息
     */
    @RequestMapping("/getVideoSeriesDetail")
    //@GlobalInterceptor
    public ResponseVO getVideoSeriesDetail(@NotNull Integer seriesId) {
        UserVideoSeries videoSeries = userVideoSeriesService.getUserVideoSeriesBySeriesId(seriesId);
        if (videoSeries == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_404);
        }
        UserVideoSeriesVideoQuery videoSeriesVideoQuery = new UserVideoSeriesVideoQuery();
        videoSeriesVideoQuery.setOrderBy("sort asc");
        //设置要"关联视频表"以"查询视频信息"
        videoSeriesVideoQuery.setQueryVideoInfo(true);
        videoSeriesVideoQuery.setSeriesId(seriesId);
        //查询出视频合集里的视频信息(不分页)
        List<UserVideoSeriesVideo> seriesVideoList = userVideoSeriesVideoService.findListByParam(videoSeriesVideoQuery);
        //封装VO
        UserVideoSeriesDetailVO userVideoSeriesDetailVO = new UserVideoSeriesDetailVO(videoSeries, seriesVideoList);
        return getSuccessResponseVO(userVideoSeriesDetailVO);
    }


    /**
     * 1、批量保存用户视频到集合  2、更改合集里的视频的"排序"
     * @param seriesId
     * @param videoIds
     * @return
     */
    @RequestMapping("/saveSeriesVideo")
    //@GlobalInterceptor(checkLogin = true)
    public ResponseVO saveSeriesVideo(@NotNull Integer seriesId, @NotEmpty String videoIds) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        //批量保存视频到合集 或者是 更改合集里的视频的"排序"
        userVideoSeriesService.saveSeriesVideo(tokenUserInfoDto.getUserId(), seriesId, videoIds);
        return getSuccessResponseVO(null);
    }

    /**
     * 删除合集里的视频
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
     * 删除视频合集
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
     * 更改合集的排序
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

/*
    @RequestMapping("/loadVideoSeriesWithVideo")
    //@GlobalInterceptor
    public ResponseVO loadVideoSeriesWithVideo(@NotEmpty String userId) {
        UserVideoSeriesQuery seriesQuery = new UserVideoSeriesQuery();
        seriesQuery.setUserId(userId);
        seriesQuery.setOrderBy("sort asc");
        List<UserVideoSeries> videoSeries = userVideoSeriesService.findListWithVideoList(seriesQuery);
        return getSuccessResponseVO(videoSeries);
    }*/

}
