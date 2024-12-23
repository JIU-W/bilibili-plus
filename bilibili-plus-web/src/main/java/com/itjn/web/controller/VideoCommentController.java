package com.itjn.web.controller;

import com.itjn.annotation.RecordUserMessage;
import com.itjn.entity.constants.Constants;
import com.itjn.entity.dto.TokenUserInfoDto;
import com.itjn.entity.enums.CommentTopTypeEnum;
import com.itjn.entity.enums.MessageTypeEnum;
import com.itjn.entity.enums.PageSize;
import com.itjn.entity.enums.UserActionTypeEnum;
import com.itjn.entity.po.UserAction;
import com.itjn.entity.po.VideoComment;
import com.itjn.entity.po.VideoInfo;
import com.itjn.entity.query.UserActionQuery;
import com.itjn.entity.query.VideoCommentQuery;
import com.itjn.entity.vo.PaginationResultVO;
import com.itjn.entity.vo.ResponseVO;
import com.itjn.entity.vo.VideoCommentResultVO;
import com.itjn.service.UserActionService;
import com.itjn.service.VideoCommentService;
import com.itjn.service.impl.VideoInfoServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Validated
@RequestMapping("/comment")
@Slf4j
public class VideoCommentController extends ABaseController {

    @Resource
    private VideoCommentService videoCommentService;

    @Resource
    private UserActionService userActionService;

    @Resource
    private VideoInfoServiceImpl videoInfoService;


    /**
     * 发表评论   评论分为：  1.一级评论(p_comment_id为0，不传replyCommentId)
     * 2.二级评论(传replyCommentId)：  [1.回复一级评论的二级评论  2.回复二级评论的二级评论]
     *
     * @param videoId        投稿(视频)ID
     * @param replyCommentId 是被回复的评论ID，而不是父级评论ID(p_comment_id)
     *                       replyCommentId和p_comment_id的区别：
     *                       1.回复一级评论的二级评论，replyCommentId为那个一级评论的id
     *                       2.回复二级评论的二级评论，replyCommentId为被回复的二级评论的id
     *                       而回复一级评论的二级评论 以及 回复二级评论的二级评论的p_comment_id是一样的，
     *                       也就是说在同一个消息中，它之下的两种类型的回复的p_comment_id都是这个消息的id。
     * @param content
     * @param imgPath
     * @return
     */
    @RequestMapping("/postComment")
    //@GlobalInterceptor(checkLogin = true)
    //@RecordUserMessage(messageType = MessageTypeEnum.COMMENT)
    public ResponseVO postComment(@NotEmpty String videoId, Integer replyCommentId,
                                  @NotEmpty @Size(max = 500) String content, @Size(max = 50) String imgPath) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        VideoComment comment = new VideoComment();
        comment.setUserId(tokenUserInfoDto.getUserId());
        comment.setVideoId(videoId);
        comment.setContent(content);
        comment.setImgPath(imgPath);
        //冗余的字段，用于发布评论后显示，不用再另外关联数据库查询。
        comment.setAvatar(tokenUserInfoDto.getAvatar());
        comment.setNickName(tokenUserInfoDto.getNickName());
        //发布评论
        videoCommentService.postComment(comment, replyCommentId);
        return getSuccessResponseVO(comment);
    }

    /**
     * 查询评论
     *
     * @param videoId   投稿(视频)ID
     * @param pageNo    页码
     * @param orderType 排序类型
     * @return
     */
    @RequestMapping("/loadComment")
    //@GlobalInterceptor
    public ResponseVO loadComment(@NotEmpty String videoId, Integer pageNo, Integer orderType) {

        VideoInfo videoInfo = videoInfoService.getVideoInfoByVideoId(videoId);
        //判断这个投稿作品是否关闭这个功能
        if (videoInfo.getInteraction() != null && videoInfo.getInteraction().contains(Constants.ZERO.toString())) {
            return getSuccessResponseVO(new ArrayList<>());
        }

        VideoCommentQuery commentQuery = new VideoCommentQuery();
        commentQuery.setVideoId(videoId);
        commentQuery.setLoadChildren(true);//要查询一级评论的子评论
        commentQuery.setPageNo(pageNo);
        commentQuery.setPageSize(PageSize.SIZE15.getSize());
        //查询一级评论
        commentQuery.setpCommentId(0);
        //根据热度排序(点赞数量) 或者 根据时间排序
        String orderBy = orderType == null || orderType == 0 ? "like_count desc,comment_id desc" : "comment_id desc";
        commentQuery.setOrderBy(orderBy);
        PaginationResultVO<VideoComment> commentData = videoCommentService.findListByPage(commentQuery);

        //置顶评论     pageNo == null || pageNo == 1：第一页时才查询置顶评论
        if (pageNo == null || pageNo == 1) {
            //查询置顶的评论：一条一级评论以及它的所用子评论
            List<VideoComment> topCommentList = topComment(videoId);
            if (!topCommentList.isEmpty()) {
                //排除之前分页查询到的所有评论数据中的置顶评论
                List<VideoComment> commentList =
                        commentData.getList().stream()
                                .filter(item -> !item.getCommentId().equals(topCommentList.get(0).getCommentId()))
                                .collect(Collectors.toList());
                //将置顶评论放到最前面
                commentList.addAll(0, topCommentList);
                //重新设置分页查询的结果，也就是重新塞值。
                commentData.setList(commentList);
            }
        }

        //封装返回数据
        VideoCommentResultVO resultVO = new VideoCommentResultVO();
        //评论数据
        resultVO.setCommentData(commentData);
        List<UserAction> userActionList = new ArrayList<>();
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        if (tokenUserInfoDto != null) {
            UserActionQuery userActionQuery = new UserActionQuery();
            userActionQuery.setUserId(tokenUserInfoDto.getUserId());
            userActionQuery.setVideoId(videoId);
            userActionQuery.setActionTypeArray(new Integer[]{UserActionTypeEnum.COMMENT_LIKE.getType(),
                    UserActionTypeEnum.COMMENT_HATE.getType()});
            userActionList = userActionService.findListByParam(userActionQuery);
        }
        //当前用户的用户行为：包括  1.评论点赞  2.评论讨厌
        resultVO.setUserActionList(userActionList);
        return getSuccessResponseVO(resultVO);
    }

    //查询置顶的评论：一条一级评论以及它的所用子评论
    private List<VideoComment> topComment(String videoId) {
        VideoCommentQuery commentQuery = new VideoCommentQuery();
        commentQuery.setVideoId(videoId);
        commentQuery.setTopType(CommentTopTypeEnum.TOP.getType());
        commentQuery.setLoadChildren(true);//要查询一级评论的子评论
        //返回的数据虽然是一个List集合，但是结果只有一条数据。
        List<VideoComment> videoCommentList = videoCommentService.findListByParam(commentQuery);
        return videoCommentList;
    }


    /**
     * 置顶评论
     * @param commentId
     * @return
     */
    @RequestMapping("/topComment")
    //@GlobalInterceptor(checkLogin = true)
    public ResponseVO topComment(@NotNull Integer commentId) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        videoCommentService.topComment(commentId, tokenUserInfoDto.getUserId());
        return getSuccessResponseVO(null);
    }


    /**
     * 取消评论置顶
     * @param commentId
     * @return
     */
    @RequestMapping("/cancelTopComment")
    //@GlobalInterceptor(checkLogin = true)
    public ResponseVO cancelTopComment(@NotNull Integer commentId) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        videoCommentService.cancelTopComment(commentId, tokenUserInfoDto.getUserId());
        return getSuccessResponseVO(null);
    }

    /**
     * 删除评论
     * @param commentId
     * @return
     */
    @RequestMapping("/userDelComment")
    //@GlobalInterceptor(checkLogin = true)
    public ResponseVO userDelComment(@NotNull Integer commentId) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        VideoComment comment = new VideoComment();
        videoCommentService.deleteComment(commentId, tokenUserInfoDto.getUserId());
        return getSuccessResponseVO(comment);
    }


}
