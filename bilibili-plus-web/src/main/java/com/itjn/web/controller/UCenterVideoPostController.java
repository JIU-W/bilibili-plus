package com.itjn.web.controller;

import com.itjn.entity.dto.TokenUserInfoDto;
import com.itjn.entity.enums.ResponseCodeEnum;
import com.itjn.entity.enums.VideoStatusEnum;
import com.itjn.entity.po.VideoInfoFilePost;
import com.itjn.entity.po.VideoInfoPost;
import com.itjn.entity.query.VideoInfoFilePostQuery;
import com.itjn.entity.query.VideoInfoPostQuery;
import com.itjn.entity.vo.PaginationResultVO;
import com.itjn.entity.vo.ResponseVO;
import com.itjn.entity.vo.VideoPostEditInfoVo;
import com.itjn.entity.vo.VideoStatusCountInfoVO;
import com.itjn.exception.BusinessException;
import com.itjn.service.VideoInfoFilePostService;
import com.itjn.service.VideoInfoPostService;
import com.itjn.service.VideoInfoService;
import com.itjn.utils.JsonUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@RestController
@Validated
@RequestMapping("/ucenter")
public class UCenterVideoPostController extends ABaseController {

    @Resource
    private VideoInfoPostService videoInfoPostService;

    @Resource
    private VideoInfoFilePostService videoInfoFilePostService;

    @Resource
    private VideoInfoService videoInfoService;


    /**
     * 发布投稿、修改已投稿的信息
     * @param videoId 视频id 前端没有传视频唯一标识则说明是新增投稿，否则是修改投稿。
     * @param videoCover 视频封面文件的存储路径
     * @param videoName  视频名称 (标题)
     * @param pCategoryId 父级分类id
     * @param categoryId 分类id
     * @param postType 发布类型
     * @param tags     标签
     * @param introduction 简介
     * @param interaction  互动设置
     * @param uploadFileList 上传文件的集合列表(集合内容包括uploadId fileName)(前端传的是JSON字符串类型)
     * @return
     */
    @RequestMapping("/postVideo")
    //@GlobalInterceptor(checkLogin = true)
    public ResponseVO postVideo(String videoId, @NotEmpty String videoCover,
                                @NotEmpty @Size(max = 100) String videoName, @NotNull Integer pCategoryId,
                                Integer categoryId, @NotNull Integer postType,
                                @NotEmpty @Size(max = 300) String tags, @Size(max = 2000) String introduction,
                                @Size(max = 3) String interaction, @NotEmpty String uploadFileList) {
        //获取当前登录用户信息
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        //获取上传文件的集合
        List<VideoInfoFilePost> fileInfoList = JsonUtils.convertJsonArray2List(uploadFileList, VideoInfoFilePost.class);

        //封装(视频信息---发布表)
        VideoInfoPost videoInfo = new VideoInfoPost();
        videoInfo.setVideoId(videoId);
        videoInfo.setVideoName(videoName);
        videoInfo.setVideoCover(videoCover);
        videoInfo.setpCategoryId(pCategoryId);
        videoInfo.setCategoryId(categoryId);
        videoInfo.setPostType(postType);
        videoInfo.setTags(tags);
        videoInfo.setIntroduction(introduction);
        videoInfo.setInteraction(interaction);
        videoInfo.setUserId(tokenUserInfoDto.getUserId());

        //发布投稿、修改已投稿的信息
        videoInfoPostService.saveVideoInfo(videoInfo, fileInfoList);
        return getSuccessResponseVO(null);
    }


    @RequestMapping("/loadVideoList")
    //@GlobalInterceptor(checkLogin = true)
    public ResponseVO loadVideoList(Integer status, Integer pageNo, String videoNameFuzzy) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        VideoInfoPostQuery videoInfoQuery = new VideoInfoPostQuery();
        videoInfoQuery.setUserId(tokenUserInfoDto.getUserId());
        videoInfoQuery.setOrderBy("v.create_time desc");
        videoInfoQuery.setPageNo(pageNo);
        if (status != null) {
            if (status == -1) {
                videoInfoQuery.setExcludeStatusArray(new Integer[]{VideoStatusEnum.STATUS3.getStatus(), VideoStatusEnum.STATUS4.getStatus()});
            } else {
                videoInfoQuery.setStatus(status);
            }
        }
        videoInfoQuery.setVideoNameFuzzy(videoNameFuzzy);
        videoInfoQuery.setQueryCountInfo(true);
        PaginationResultVO resultVO = videoInfoPostService.findListByPage(videoInfoQuery);
        return getSuccessResponseVO(resultVO);
    }

    @RequestMapping("/getVideoCountInfo")
    //@GlobalInterceptor(checkLogin = true)
    public ResponseVO getVideoCountInfo() {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        VideoInfoPostQuery videoInfoQuery = new VideoInfoPostQuery();
        videoInfoQuery.setUserId(tokenUserInfoDto.getUserId());
        videoInfoQuery.setStatus(VideoStatusEnum.STATUS3.getStatus());

        Integer auditPassCount = videoInfoPostService.findCountByParam(videoInfoQuery);
        videoInfoQuery.setStatus(VideoStatusEnum.STATUS4.getStatus());
        Integer auditFailCount = videoInfoPostService.findCountByParam(videoInfoQuery);

        videoInfoQuery.setStatus(null);
        videoInfoQuery.setExcludeStatusArray(new Integer[]{VideoStatusEnum.STATUS3.getStatus(), VideoStatusEnum.STATUS4.getStatus()});
        Integer inProgress = videoInfoPostService.findCountByParam(videoInfoQuery);

        VideoStatusCountInfoVO countInfo = new VideoStatusCountInfoVO();
        countInfo.setAuditPassCount(auditPassCount);
        countInfo.setAuditFailCount(auditFailCount);
        countInfo.setInProgress(inProgress);
        return getSuccessResponseVO(countInfo);
    }


    @RequestMapping("/getVideoByVideoId")
    //@GlobalInterceptor(checkLogin = true)
    public ResponseVO getVideoByVideoId(@NotEmpty String videoId) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        VideoInfoPost videoInfoPost = this.videoInfoPostService.getVideoInfoPostByVideoId(videoId);
        if (videoInfoPost == null || !videoInfoPost.getUserId().equals(tokenUserInfoDto.getUserId())) {
            throw new BusinessException(ResponseCodeEnum.CODE_404);
        }
        VideoInfoFilePostQuery videoInfoFilePostQuery = new VideoInfoFilePostQuery();
        videoInfoFilePostQuery.setVideoId(videoId);
        videoInfoFilePostQuery.setOrderBy("file_index asc");
        List<VideoInfoFilePost> videoInfoFilePostList = this.videoInfoFilePostService.findListByParam(videoInfoFilePostQuery);
        VideoPostEditInfoVo vo = new VideoPostEditInfoVo();
        vo.setVideoInfo(videoInfoPost);
        vo.setVideoInfoFileList(videoInfoFilePostList);
        return getSuccessResponseVO(vo);
    }

    @RequestMapping("/saveVideoInteraction")
    //@GlobalInterceptor(checkLogin = true)
    public ResponseVO saveVideoInteraction(@NotEmpty String videoId, String interaction) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        videoInfoService.changeInteraction(videoId, tokenUserInfoDto.getUserId(), interaction);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/deleteVideo")
    //@GlobalInterceptor(checkLogin = true)
    public ResponseVO deleteVideo(@NotEmpty String videoId) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        videoInfoService.deleteVideo(videoId, tokenUserInfoDto.getUserId());
        return getSuccessResponseVO(null);
    }

}
