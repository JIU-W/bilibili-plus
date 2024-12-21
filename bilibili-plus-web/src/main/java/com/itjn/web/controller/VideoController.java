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
        //直接查询而不需要分页(因为推荐的投稿一般不会太多，就十来条)
        List<VideoInfo> recommendVideoList = videoInfoService.findListByParam(videoInfoQuery);
        return getSuccessResponseVO(recommendVideoList);
    }

    /*@RequestMapping("/loadVideo")
    //@GlobalInterceptor
    public ResponseVO postVideo(Integer pCategoryId, Integer categoryId, Integer pageNo) {
        VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
        videoInfoQuery.setCategoryId(categoryId);
        videoInfoQuery.setpCategoryId(pCategoryId);
        videoInfoQuery.setPageNo(pageNo);
        videoInfoQuery.setQueryUserInfo(true);
        videoInfoQuery.setOrderBy("create_time desc");
        videoInfoQuery.setRecommendType(VideoRecommendTypeEnum.NO_RECOMMEND.getType());
        PaginationResultVO resultVO = videoInfoService.findListByPage(videoInfoQuery);
        return getSuccessResponseVO(resultVO);
    }

    @RequestMapping("/loadVideoPList")
    //@GlobalInterceptor
    public ResponseVO loadVideoPList(@NotEmpty String videoId) {
        VideoInfoFileQuery videoInfoQuery = new VideoInfoFileQuery();
        videoInfoQuery.setVideoId(videoId);
        videoInfoQuery.setOrderBy("file_index asc");
        List<VideoInfoFile> fileList = videoInfoFileService.findListByParam(videoInfoQuery);
        return getSuccessResponseVO(fileList);
    }

    @RequestMapping("/getVideoInfo")
    //@GlobalInterceptor
    public ResponseVO getVideoInfo(@NotEmpty String videoId) {
        VideoInfo videoInfo = videoInfoService.getVideoInfoByVideoId(videoId);
        if (null == videoInfo) {
            throw new BusinessException(ResponseCodeEnum.CODE_404);
        }
        TokenUserInfoDto userInfoDto = getTokenUserInfoDto();

        List<UserAction> userActionList = new ArrayList<>();
        if (userInfoDto != null) {
            UserActionQuery actionQuery = new UserActionQuery();
            actionQuery.setVideoId(videoId);
            actionQuery.setUserId(userInfoDto.getUserId());
            actionQuery.setActionTypeArray(new Integer[]{UserActionTypeEnum.VIDEO_LIKE.getType(), UserActionTypeEnum.VIDEO_COLLECT.getType(),
                    UserActionTypeEnum.VIDEO_COIN.getType(),});
            userActionList = userActionService.findListByParam(actionQuery);
        }
        VideoInfoResultVo resultVo = new VideoInfoResultVo();
        resultVo.setVideoInfo(CopyTools.copy(videoInfo, VideoInfoVo.class));
        resultVo.setUserActionList(userActionList);
        return getSuccessResponseVO(resultVo);
    }*/


}
