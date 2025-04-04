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
import com.itjn.web.annotation.GlobalInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

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
    @GlobalInterceptor
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
    @GlobalInterceptor
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
     * 获取视频详情信息
     * @param videoId
     * @return
     */
    @RequestMapping("/getVideoInfo")
    @GlobalInterceptor
    public ResponseVO getVideoInfo(@NotEmpty String videoId) {
        //查询视频信息
        VideoInfo videoInfo = videoInfoService.getVideoInfoByVideoId(videoId);
        if (null == videoInfo) {
            throw new BusinessException(ResponseCodeEnum.CODE_404);
        }
        //查询用户是否登录
        TokenUserInfoDto userInfoDto = getTokenUserInfoDto();

        //获取用户行为列表：  与视频有关的：(点赞，收藏，投币)
        List<UserAction> userActionList = new ArrayList<>();
        if (userInfoDto != null) {
            UserActionQuery actionQuery = new UserActionQuery();
            actionQuery.setVideoId(videoId);
            actionQuery.setUserId(userInfoDto.getUserId());
            actionQuery.setActionTypeArray(new Integer[]{UserActionTypeEnum.VIDEO_LIKE.getType(),
                    UserActionTypeEnum.VIDEO_COLLECT.getType(),
                    UserActionTypeEnum.VIDEO_COIN.getType(),});
            userActionList = userActionService.findListByParam(actionQuery);
        }

        VideoInfoResultVo resultVo = new VideoInfoResultVo();
        resultVo.setVideoInfo(CopyTools.copy(videoInfo, VideoInfoVo.class));
        resultVo.setUserActionList(userActionList);
        return getSuccessResponseVO(resultVo);
    }

    /**
     * 获取视频播放在线人数
     * @param fileId 某个投稿下的分p视频id
     * @param deviceId 设备id：前端通过一个插件生成的一个唯一标识，用于区分不同的浏览器设备。
     *                 有小概率存在相同值，但是概率极低，可以忽略。
     * @return
     */
    @RequestMapping("/reportVideoPlayOnline")
    public ResponseVO reportVideoPlayOnline(@NotEmpty String fileId, @NotEmpty String deviceId) {
        //统计在线人数
        Integer count = redisComponent.reportVideoPlayOnline(fileId, deviceId);
        return getSuccessResponseVO(count);
    }

    /**
     * 获取分p视频播放列表
     * @param videoId
     * @return
     */
    @RequestMapping("/loadVideoPList")
    @GlobalInterceptor
    public ResponseVO loadVideoPList(@NotEmpty String videoId) {
        VideoInfoFileQuery videoInfoQuery = new VideoInfoFileQuery();
        videoInfoQuery.setVideoId(videoId);
        videoInfoQuery.setOrderBy("file_index asc");
        //查询分p视频列表(不分页，因为数量不多)
        List<VideoInfoFile> fileList = videoInfoFileService.findListByParam(videoInfoQuery);
        return getSuccessResponseVO(fileList);
    }

    /**
     * 获取视频作品推荐列表：在ES中搜索，根据播放量排序后推荐前十个。(展示在视频播放页面，列表的视频都与当前视频相关)
     * @param keyword 搜索关键词：当前视频作品的名称(video_name)
     * @param videoId 当前视频作品id
     * @return
     */
    @RequestMapping("/getVideoRecommend")
    @GlobalInterceptor
    public ResponseVO getVideoRecommend(@NotEmpty String keyword, @NotEmpty String videoId) {
        List<VideoInfo> videoInfoList = esSearchComponent.search(false, keyword,
                SearchOrderTypeEnum.VIDEO_PLAY.getType(), 1, PageSize.SIZE10.getSize()).getList();
        //搜索结果中排除当前视频作品
        videoInfoList = videoInfoList.stream()
                .filter(item -> !item.getVideoId().equals(videoId)).collect(Collectors.toList());
        return getSuccessResponseVO(videoInfoList);
    }

    /**
     * 搜索视频作品
     */
    @RequestMapping("/search")
    @GlobalInterceptor
    public ResponseVO search(@NotEmpty String keyword, Integer orderType, Integer pageNo) {
        //记录搜索热词：给对应热词的得分加1
        redisComponent.addKeywordCount(keyword);
        //搜索视频作品
        PaginationResultVO resultVO = esSearchComponent.search(true, keyword, orderType, pageNo, PageSize.SIZE30.getSize());
        return getSuccessResponseVO(resultVO);
    }

    /**
     * 获取搜索热词
     * @return
     */
    @RequestMapping("/getSearchKeywordTop")
    @GlobalInterceptor
    public ResponseVO getSearchKeywordTop() {
        List<String> keywordList = redisComponent.getKeywordTop(Constants.LENGTH_10);
        return getSuccessResponseVO(keywordList);
    }

    /**
     * 获取热门视频列表
     * @param pageNo
     * @return
     */
    @RequestMapping("/loadHotVideoList")
    @GlobalInterceptor
    public ResponseVO loadHotVideoList(Integer pageNo) {
        VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
        videoInfoQuery.setPageNo(pageNo);
        videoInfoQuery.setQueryUserInfo(true);
        //设置排序规则：按播放量倒序排序
        videoInfoQuery.setOrderBy("play_count desc");
        //设置查找最近24小时内播放过的作品
        videoInfoQuery.setLastPlayHour(Constants.HOUR_24);
        //查询最近24小时内播放量最多的视频列表
        PaginationResultVO resultVO = videoInfoService.findListByPage(videoInfoQuery);
        return getSuccessResponseVO(resultVO);
    }


}
