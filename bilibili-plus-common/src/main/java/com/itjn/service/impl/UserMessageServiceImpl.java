package com.itjn.service.impl;

import com.itjn.entity.dto.UserMessageCountDto;
import com.itjn.entity.dto.UserMessageExtendDto;
import com.itjn.entity.enums.MessageReadTypeEnum;
import com.itjn.entity.enums.MessageTypeEnum;
import com.itjn.entity.enums.PageSize;
import com.itjn.entity.po.UserMessage;
import com.itjn.entity.po.VideoComment;
import com.itjn.entity.po.VideoInfo;
import com.itjn.entity.po.VideoInfoPost;
import com.itjn.entity.query.SimplePage;
import com.itjn.entity.query.UserMessageQuery;
import com.itjn.entity.query.VideoCommentQuery;
import com.itjn.entity.query.VideoInfoPostQuery;
import com.itjn.entity.vo.PaginationResultVO;
import com.itjn.mappers.UserMessageMapper;
import com.itjn.mappers.VideoCommentMapper;
import com.itjn.mappers.VideoInfoPostMapper;
import com.itjn.service.UserMessageService;
import com.itjn.utils.JsonUtils;
import com.itjn.utils.StringTools;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;


/**
 * 用户消息表 业务接口实现
 */
@Service("userMessageService")
public class UserMessageServiceImpl implements UserMessageService {

    @Resource
    private UserMessageMapper<UserMessage, UserMessageQuery> userMessageMapper;

    @Resource
    private VideoInfoPostMapper<VideoInfoPost, VideoInfoPostQuery> videoInfoPostMapper;

    @Resource
    private VideoCommentMapper<VideoComment, VideoCommentQuery> videoCommentMapper;

    /**
     * 根据条件查询列表
     */
    @Override
    public List<UserMessage> findListByParam(UserMessageQuery param) {
        return this.userMessageMapper.selectList(param);
    }

    /**
     * 根据条件查询列表
     */
    @Override
    public Integer findCountByParam(UserMessageQuery param) {
        return this.userMessageMapper.selectCount(param);
    }

    /**
     * 分页查询方法
     */
    @Override
    public PaginationResultVO<UserMessage> findListByPage(UserMessageQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<UserMessage> list = this.findListByParam(param);
        PaginationResultVO<UserMessage> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    /**
     * 新增
     */
    @Override
    public Integer add(UserMessage bean) {
        return this.userMessageMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<UserMessage> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.userMessageMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或者修改
     */
    @Override
    public Integer addOrUpdateBatch(List<UserMessage> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.userMessageMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 多条件更新
     */
    @Override
    public Integer updateByParam(UserMessage bean, UserMessageQuery param) {
        StringTools.checkParam(param);
        return this.userMessageMapper.updateByParam(bean, param);
    }

    /**
     * 多条件删除
     */
    @Override
    public Integer deleteByParam(UserMessageQuery param) {
        StringTools.checkParam(param);
        return this.userMessageMapper.deleteByParam(param);
    }

    /**
     * 根据MessageId获取对象
     */
    @Override
    public UserMessage getUserMessageByMessageId(Integer messageId) {
        return this.userMessageMapper.selectByMessageId(messageId);
    }

    /**
     * 根据MessageId修改
     */
    @Override
    public Integer updateUserMessageByMessageId(UserMessage bean, Integer messageId) {
        return this.userMessageMapper.updateByMessageId(bean, messageId);
    }

    /**
     * 根据MessageId删除
     */
    @Override
    public Integer deleteUserMessageByMessageId(Integer messageId) {
        return this.userMessageMapper.deleteByMessageId(messageId);
    }

    @Async  //记录消息的这个方法是异步执行:与点赞、收藏、评论、审核那些会产生消息的行为分离开，记录消息不会影响那些行为。
    public void saveUserMessage(String videoId, String sendUserId, MessageTypeEnum messageTypeEnum, String content, Integer replyCommentId) {
        VideoInfo videoInfo = this.videoInfoPostMapper.selectByVideoId(videoId);
        if (videoInfo == null) {
            //视频不存在，只是不记录消息，不抛出异常。因为点赞或者收藏或者评论等等这些行为是成功的，只是消息发送不成功，没有大的影响，
            //故不抛异常到前端
            return;
        }

        UserMessageExtendDto extendDto = new UserMessageExtendDto();
        extendDto.setMessageContent(content);

        //收到消息的用户
        String userId = videoInfo.getUserId();

        //对于"收藏"和"点赞"类型的消息：已经记录过一次的话，就不再记录了。
        //这里设置的本意是点赞和收藏的人只有"在第一次收藏和点赞时"才会发消息给"消息接收人"，
        //后面如果取消点赞然后再重新点赞的话是不会再次发消息的。
        //TODO 还有一个小bug，就是"给一个视频下的评论点赞"时会被当成"给视频点赞"然后发消息给视频发布者，
        //也就是说我们这里没有特殊处理"点赞评论"这种具体的情况。
        if (ArrayUtils.contains(new Integer[]{MessageTypeEnum.LIKE.getType(), MessageTypeEnum.COLLECTION.getType()},
                messageTypeEnum.getType())) {
            UserMessageQuery userMessageQuery = new UserMessageQuery();
            userMessageQuery.setUserId(userId);//接收消息用户
            userMessageQuery.setVideoId(videoId);//消息有关视频
            userMessageQuery.setMessageType(messageTypeEnum.getType());//消息类型
            Integer count = userMessageMapper.selectCount(userMessageQuery);
            if (count > 0) {
                return;
            }
        }
        UserMessage userMessage = new UserMessage();
        userMessage.setUserId(userId);
        userMessage.setVideoId(videoId);
        userMessage.setReadType(MessageReadTypeEnum.NO_READ.getType());
        userMessage.setCreateTime(new Date());
        userMessage.setMessageType(messageTypeEnum.getType());
        userMessage.setSendUserId(sendUserId);

        //"评论消息"的特殊处理
        if (replyCommentId != null) {
            VideoComment commentInfo = videoCommentMapper.selectByCommentId(replyCommentId);
            if (null != commentInfo) {
                //如果发布的评论是用于回复其它的评论(即是二级评论不是一级评论)，那么消息的接收人应该被回复的评论的发送人人。
                userId = commentInfo.getUserId();
                extendDto.setMessageContentReply(commentInfo.getContent());
            }
        }
        //自己回复自己的行为不用发送消息
        if (userId.equals(sendUserId)) {
            return;
        }

        //"系统消息"特殊处理
        if (MessageTypeEnum.SYS == messageTypeEnum) {
            VideoInfoPost videoInfoPost = videoInfoPostMapper.selectByVideoId(videoId);
            extendDto.setAuditStatus(videoInfoPost.getStatus());
        }
        userMessage.setUserId(userId);
        //转换为json
        String extendJson = JsonUtils.convertObj2Json(extendDto);
        userMessage.setExtendJson(extendJson);
        //记录消息
        this.userMessageMapper.insert(userMessage);
    }


    public List<UserMessageCountDto> getMessageTypeNoReadCount(String userId) {
        return this.userMessageMapper.getMessageTypeNoReadCount(userId);
    }

}
