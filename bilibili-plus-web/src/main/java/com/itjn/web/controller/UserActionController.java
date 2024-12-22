package com.itjn.web.controller;

import com.itjn.annotation.RecordUserMessage;
import com.itjn.entity.constants.Constants;
import com.itjn.entity.enums.MessageTypeEnum;
import com.itjn.entity.po.UserAction;
import com.itjn.entity.vo.ResponseVO;
import com.itjn.service.UserActionService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;

/**
 * 用户行为，总共有五种  [ 评论：(点赞，讨厌)      视频：(点赞，收藏，投币) ]
 */
@RestController("userActionController")
@RequestMapping("/userAction")
public class UserActionController extends ABaseController {

    @Resource
    private UserActionService userActionService;

    /**
     * 用户做出某种行为  [ 评论：(点赞，讨厌)      视频：(点赞，收藏，投币) ]
     * @param videoId 关联的投稿(视频)id
     * @param actionType 行为类型： 0:评论喜欢点赞 1:讨厌评论 2:视频点赞 3:视频收藏 4:视频投币
     * @param actionCount 行为数量：   用户的一次操作：“只有投币数可能为2，其它全为1”
     * @param commentId 如果行为类型为 0:评论喜欢点赞 或者 1:讨厌评论 则前端传评论id过来。
     * @return
     */
    @RequestMapping("doAction")
    @RecordUserMessage(messageType = MessageTypeEnum.LIKE)
    //@GlobalInterceptor(checkLogin = true)
    public ResponseVO doAction(@NotEmpty String videoId, @NotEmpty Integer actionType,
                               @Max(2) @Min(1) Integer actionCount, Integer commentId) {
        UserAction userAction = new UserAction();
        userAction.setUserId(getTokenUserInfoDto().getUserId());//当前用户id
        userAction.setVideoId(videoId);
        userAction.setActionType(actionType);
        actionCount = actionCount == null ? Constants.ONE : actionCount;
        userAction.setActionCount(actionCount);
        //评论id：0:表示对视频操作  非0：表示对评论操作
        commentId = commentId == null ? 0 : commentId;
        userAction.setCommentId(commentId);
        userActionService.saveAction(userAction);
        return getSuccessResponseVO(null);
    }


}
