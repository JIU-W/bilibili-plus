package com.itjn.web.controller;

import com.itjn.entity.dto.TokenUserInfoDto;
import com.itjn.entity.po.VideoInfo;
import com.itjn.entity.query.VideoCommentQuery;
import com.itjn.entity.query.VideoDanmuQuery;
import com.itjn.entity.query.VideoInfoQuery;
import com.itjn.entity.vo.PaginationResultVO;
import com.itjn.entity.vo.ResponseVO;
import com.itjn.service.VideoCommentService;
import com.itjn.service.VideoDanmuService;
import com.itjn.service.VideoInfoService;
import com.itjn.web.annotation.GlobalInterceptor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@Validated
@RequestMapping("/ucenter")
public class UCenterInteractController extends ABaseController {

    @Resource
    private VideoCommentService videoCommentService;

    @Resource
    private VideoDanmuService videoDanmuService;

    @Resource
    private VideoInfoService videoInfoService;

    /**
     * 加载当前用户的所有投稿(已发布的投稿)
     * @return
     */
    @RequestMapping("/loadAllVideo")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO loadAllVideo() {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
        videoInfoQuery.setUserId(tokenUserInfoDto.getUserId());
        videoInfoQuery.setOrderBy("create_time desc");
        List<VideoInfo> videoInfoList = videoInfoService.findListByParam(videoInfoQuery);
        return getSuccessResponseVO(videoInfoList);
    }


    /**
     * 查询当前用户发布的视频下的所有评论(查询出的评论不用层级展示，线形展示就可以了)
     * @param pageNo 前端没有传的话，SimplePage()构造方法里会设置默认的pageNo，设置为1。
     * @param pageSize
     * @param videoId 可传可不传：前端在搜索框选择了视频就传，否则不传。
     * @return
     */
    @RequestMapping("/loadComment")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO loadComment(Integer pageNo, Integer pageSize, String videoId) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        VideoCommentQuery commentQuery = new VideoCommentQuery();
        commentQuery.setVideoUserId(tokenUserInfoDto.getUserId());
        commentQuery.setVideoId(videoId);
        commentQuery.setOrderBy("comment_id desc");
        commentQuery.setPageNo(pageNo);
        commentQuery.setPageSize(pageSize);
        //设置同时要查询评论对应的"视频信息"(名称，封面)以及评论的"发布人信息"(昵称，头像)
                                                   //以及评论的"回复人信息"(昵称)
        commentQuery.setQueryVideoInfo(true);
        //分页查询评论列表
        PaginationResultVO resultVO = videoCommentService.findListByPage(commentQuery);
        return getSuccessResponseVO(resultVO);
    }

    /**
     * 删除评论
     * @param commentId
     * @return
     */
    @RequestMapping("/delComment")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO delComment(@NotNull Integer commentId) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        videoCommentService.deleteComment(commentId, tokenUserInfoDto.getUserId());
        return getSuccessResponseVO(null);
    }

    /**
     * 查询当前用户发布的视频下的所有弹幕
     * @param pageNo 前端没有传的话，SimplePage()构造方法里会设置默认的pageNo，设置为1。
     * @param pageSize
     * @param videoId 可传可不传：前端在搜索框选择了视频就传，否则不传。
     * @return
     */
    @RequestMapping("/loadDanmu")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO loadDanmu(Integer pageNo, Integer pageSize, String videoId) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        //封装查询条件
        VideoDanmuQuery danmuQuery = new VideoDanmuQuery();
        //弹幕属于的投稿视频的用户id //重点：videoUserId这个字段在弹幕数据库表里并没有做冗余，
                    //但是现在要根据这个字段来查弹幕数据，那就只能在SQL的query_condition里加特殊条件了。
        danmuQuery.setVideoUserId(tokenUserInfoDto.getUserId());
        //弹幕属于的视频id
        danmuQuery.setVideoId(videoId);
        danmuQuery.setOrderBy("danmu_id desc");
        danmuQuery.setPageNo(pageNo);
        danmuQuery.setPageSize(pageSize);
        //设置同时要查询弹幕对应的"视频信息"(名称，封面)和弹幕发布人信息(昵称)
        danmuQuery.setQueryVideoInfo(true);
        PaginationResultVO resultVO = videoDanmuService.findListByPage(danmuQuery);
        return getSuccessResponseVO(resultVO);
    }

    /**
     * 删除弹幕
     * @param danmuId
     * @return
     */
    @RequestMapping("/delDanmu")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO delDanmu(@NotNull Integer danmuId) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        videoDanmuService.deleteDanmu(tokenUserInfoDto.getUserId(), danmuId);
        return getSuccessResponseVO(null);
    }

}
