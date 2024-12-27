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
        videoInfo.setPCategoryId(pCategoryId);
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


    /**
     * 加载投稿列表(这个接口前端是会不断地轮询地去查的，几秒钟查一次)
     * @param status 前端传的投稿状态   -1：显示“进行中”的投稿信息  3：显示“已通过”的投稿信息  4：显示“未通过”的投稿信息
     * @param pageNo 分页页码
     * @param videoNameFuzzy 根据视频名称(投稿名称)进行模糊查询
     * @return
     */
    @RequestMapping("/loadVideoList")
    //@GlobalInterceptor(checkLogin = true)
    public ResponseVO loadVideoList(Integer status, Integer pageNo, String videoNameFuzzy) {
        //获取当前登录用户信息
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        VideoInfoPostQuery videoInfoQuery = new VideoInfoPostQuery();
        videoInfoQuery.setUserId(tokenUserInfoDto.getUserId());
        videoInfoQuery.setOrderBy("v.create_time desc");
        videoInfoQuery.setPageNo(pageNo);
        if (status != null) {
            if (status == -1) {
                //显示“进行中”的投稿信息(除了审核通过和审核失败的)
                videoInfoQuery.setExcludeStatusArray(new Integer[]{VideoStatusEnum.STATUS3.getStatus(),
                        VideoStatusEnum.STATUS4.getStatus()});
            } else {
                //设置投稿状态：审核通过或者审核失败的状态
                videoInfoQuery.setStatus(status);
            }
        }
        videoInfoQuery.setVideoNameFuzzy(videoNameFuzzy);//投稿名称模糊查询
        //查询投稿总数
        videoInfoQuery.setQueryCountInfo(true);
        //查询投稿列表
        PaginationResultVO resultVO = videoInfoPostService.findListByPage(videoInfoQuery);
        return getSuccessResponseVO(resultVO);
    }

    /**
     * 获取三种类型投稿的总数(进行中，已通过，未通过)
     *             (这个接口前端也是会不断地轮询地去查的，几秒钟查一次)
     * @return
     */
    @RequestMapping("/getVideoCountInfo")
    //@GlobalInterceptor(checkLogin = true)
    public ResponseVO getVideoCountInfo() {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();

        VideoInfoPostQuery videoInfoQuery = new VideoInfoPostQuery();
        videoInfoQuery.setUserId(tokenUserInfoDto.getUserId());
        //查询“审核已通过”状态的投稿总数
        videoInfoQuery.setStatus(VideoStatusEnum.STATUS3.getStatus());
        Integer auditPassCount = videoInfoPostService.findCountByParam(videoInfoQuery);

        //查询“审核未通过”状态的投稿总数
        videoInfoQuery.setStatus(VideoStatusEnum.STATUS4.getStatus());
        Integer auditFailCount = videoInfoPostService.findCountByParam(videoInfoQuery);

        //查询“进行中”状态的投稿总数
        videoInfoQuery.setStatus(null);
        videoInfoQuery.setExcludeStatusArray(new Integer[]{VideoStatusEnum.STATUS3.getStatus(),
                VideoStatusEnum.STATUS4.getStatus()});
        Integer inProgress = videoInfoPostService.findCountByParam(videoInfoQuery);

        VideoStatusCountInfoVO countInfo = new VideoStatusCountInfoVO();
        countInfo.setAuditPassCount(auditPassCount);
        countInfo.setAuditFailCount(auditFailCount);
        countInfo.setInProgress(inProgress);
        return getSuccessResponseVO(countInfo);
    }


    /**
     * 获取投稿详情信息(用户编辑自己的投稿信息时之前要查出之前的投稿信息)：
     *                  1、获取投稿信息   2、获取投稿时的分p视频文件信息
     * @param videoId
     * @return
     */
    @RequestMapping("/getVideoByVideoId")
    //@GlobalInterceptor(checkLogin = true)
    public ResponseVO getVideoByVideoId(@NotEmpty String videoId) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        //查询投稿信息
        VideoInfoPost videoInfoPost = this.videoInfoPostService.getVideoInfoPostByVideoId(videoId);
        //校验该投稿是否属于当前登录用户
        if (videoInfoPost == null || !videoInfoPost.getUserId().equals(tokenUserInfoDto.getUserId())) {
            throw new BusinessException(ResponseCodeEnum.CODE_404);
        }
        //查询投稿时的分p视频文件信息
        VideoInfoFilePostQuery videoInfoFilePostQuery = new VideoInfoFilePostQuery();
        videoInfoFilePostQuery.setVideoId(videoId);
        videoInfoFilePostQuery.setOrderBy("file_index asc");
        List<VideoInfoFilePost> videoInfoFilePostList = this.videoInfoFilePostService.findListByParam(videoInfoFilePostQuery);
        //封装VO
        VideoPostEditInfoVo vo = new VideoPostEditInfoVo();
        vo.setVideoInfo(videoInfoPost);
        vo.setVideoInfoFileList(videoInfoFilePostList);
        return getSuccessResponseVO(vo);
    }

    /**
     * 保存投稿的互动信息
     *              互动信息设置： null：既没关闭评论也没关闭弹幕  包含0：关闭评论  包含1：关闭弹幕
     * @param videoId
     * @param interaction
     * @return
     */
    @RequestMapping("/saveVideoInteraction")
    //@GlobalInterceptor(checkLogin = true)
    public ResponseVO saveVideoInteraction(@NotEmpty String videoId, String interaction) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        //更新视频的互动信息
        videoInfoService.changeInteraction(videoId, tokenUserInfoDto.getUserId(), interaction);
        return getSuccessResponseVO(null);
    }

    /**
     * 删除投稿
     * @param videoId
     * @return
     */
    @RequestMapping("/deleteVideo")
    //@GlobalInterceptor(checkLogin = true)
    public ResponseVO deleteVideo(@NotEmpty String videoId) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        videoInfoService.deleteVideo(videoId, tokenUserInfoDto.getUserId());
        return getSuccessResponseVO(null);
    }

}
