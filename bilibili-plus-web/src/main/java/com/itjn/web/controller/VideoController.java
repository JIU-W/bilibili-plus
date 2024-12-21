package com.itjn.web.controller;

import com.itjn.component.EsSearchComponent;
import com.itjn.component.RedisComponent;
import com.itjn.entity.constants.Constants;
import com.itjn.entity.dto.TokenUserInfoDto;
import com.itjn.entity.enums.*;
import com.itjn.entity.po.UserAction;
import com.itjn.entity.po.VideoInfo;
import com.itjn.entity.po.VideoInfoFile;
import com.itjn.entity.query.UserActionQuery;
import com.itjn.entity.query.VideoInfoFileQuery;
import com.itjn.entity.query.VideoInfoQuery;
import com.itjn.entity.vo.PaginationResultVO;
import com.itjn.entity.vo.ResponseVO;
import com.itjn.entity.vo.VideoInfoResultVo;
import com.itjn.entity.vo.VideoInfoVo;
import com.itjn.exception.BusinessException;
import com.itjn.service.UserActionService;
import com.itjn.service.VideoInfoFileService;
import com.itjn.service.VideoInfoService;
import com.itjn.utils.CopyTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Validated
@RequestMapping("/video")
@Slf4j
public class VideoController extends ABaseController {

    @Resource
    private VideoInfoService videoInfoService;

    @Resource
    private VideoInfoFileService videoInfoFileService;

    @Resource
    private UserActionService userActionService;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private EsSearchComponent esSearchComponent;

    /**
     * 获取推荐视频列表
     * @return
     */
    @RequestMapping("/loadRecommendVideo")
    //@GlobalInterceptor
    public ResponseVO loadRecommendVideo() {
        VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
        videoInfoQuery.setQueryUserInfo(true);//同时要关联查询出对应的用户信息(用户昵称和用户头像)
        videoInfoQuery.setOrderBy("create_time desc");
        videoInfoQuery.setRecommendType(VideoRecommendTypeEnum.RECOMMEND.getType());
        //直接查询而不需要分页(因为推荐的投稿一般不会太多，不会超过5 + 6条)
        List<VideoInfo> recommendVideoList = videoInfoService.findListByParam(videoInfoQuery);
        return getSuccessResponseVO(recommendVideoList);
    }

    /**
     * 获取未推荐视频列表
     * @param pCategoryId
     * @param categoryId
     * @param pageNo
     * @return
     */
    @RequestMapping("/loadVideo")
    //@GlobalInterceptor
    public ResponseVO postVideo(Integer pCategoryId, Integer categoryId, Integer pageNo) {
        VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
        videoInfoQuery.setCategoryId(categoryId);
        videoInfoQuery.setpCategoryId(pCategoryId);
        videoInfoQuery.setPageNo(pageNo);//要分页查询
        videoInfoQuery.setQueryUserInfo(true);//同时要关联查询出对应的用户信息(用户昵称和用户头像)
        videoInfoQuery.setOrderBy("create_time desc");
        videoInfoQuery.setRecommendType(VideoRecommendTypeEnum.NO_RECOMMEND.getType());
        //分页查询未推荐投稿视频列表
        PaginationResultVO resultVO = videoInfoService.findListByPage(videoInfoQuery);
        return getSuccessResponseVO(resultVO);
    }


    /**
     * 获取分p视频播放列表
     * @param videoId
     * @return
     */
    @RequestMapping("/loadVideoPList")
    //@GlobalInterceptor
    public ResponseVO loadVideoPList(@NotEmpty String videoId) {
        VideoInfoFileQuery videoInfoQuery = new VideoInfoFileQuery();
        videoInfoQuery.setVideoId(videoId);
        videoInfoQuery.setOrderBy("file_index asc");
        //查询分p视频列表(不分页，因为数量不多)
        List<VideoInfoFile> fileList = videoInfoFileService.findListByParam(videoInfoQuery);
        return getSuccessResponseVO(fileList);
    }


    /**
     * 获取视频详情信息
     * @param videoId
     * @return
     */
    @RequestMapping("/getVideoInfo")
    //@GlobalInterceptor
    public ResponseVO getVideoInfo(@NotEmpty String videoId) {
        //查询视频信息
        VideoInfo videoInfo = videoInfoService.getVideoInfoByVideoId(videoId);
        if (null == videoInfo) {
            throw new BusinessException(ResponseCodeEnum.CODE_404);
        }
        //查询用户是否登录
        TokenUserInfoDto userInfoDto = getTokenUserInfoDto();

        //TODO 获取用户行为列表
        /*List<UserAction> userActionList = new ArrayList<>();
        if (userInfoDto != null) {
            UserActionQuery actionQuery = new UserActionQuery();
            actionQuery.setVideoId(videoId);
            actionQuery.setUserId(userInfoDto.getUserId());
            actionQuery.setActionTypeArray(new Integer[]{UserActionTypeEnum.VIDEO_LIKE.getType(), UserActionTypeEnum.VIDEO_COLLECT.getType(),
                    UserActionTypeEnum.VIDEO_COIN.getType(),});
            userActionList = userActionService.findListByParam(actionQuery);
        }*/

        VideoInfoResultVo resultVo = new VideoInfoResultVo();
        resultVo.setVideoInfo(CopyTools.copy(videoInfo, VideoInfoVo.class));
        //resultVo.setUserActionList(userActionList);
        return getSuccessResponseVO(resultVo);
    }



}
