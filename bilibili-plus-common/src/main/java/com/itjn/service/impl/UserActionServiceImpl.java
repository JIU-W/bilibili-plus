package com.itjn.service.impl;

import com.itjn.component.EsSearchComponent;
import com.itjn.entity.enums.PageSize;
import com.itjn.entity.enums.ResponseCodeEnum;
import com.itjn.entity.enums.SearchOrderTypeEnum;
import com.itjn.entity.enums.UserActionTypeEnum;
import com.itjn.entity.po.UserAction;
import com.itjn.entity.po.VideoComment;
import com.itjn.entity.po.VideoInfo;
import com.itjn.entity.query.SimplePage;
import com.itjn.entity.query.UserActionQuery;
import com.itjn.entity.query.VideoCommentQuery;
import com.itjn.entity.query.VideoInfoQuery;
import com.itjn.entity.vo.PaginationResultVO;
import com.itjn.exception.BusinessException;
import com.itjn.mappers.UserActionMapper;
import com.itjn.mappers.UserInfoMapper;
import com.itjn.mappers.VideoCommentMapper;
import com.itjn.mappers.VideoInfoMapper;
import com.itjn.service.UserActionService;
import com.itjn.service.UserMessageService;
import com.itjn.utils.StringTools;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;


/**
 * 用户行为 点赞、评论 业务接口实现
 */
@Service("userActionService")
public class UserActionServiceImpl implements UserActionService {

    @Resource
    private UserActionMapper<UserAction, UserActionQuery> userActionMapper;

    @Resource
    private VideoInfoMapper<VideoInfo, VideoInfoQuery> videoInfoMapper;

    @Resource
    private VideoCommentMapper<VideoComment, VideoCommentQuery> videoCommentMapper;

    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private UserMessageService userMessageService;

    @Resource
    private EsSearchComponent esSearchComponent;

    /**
     * 根据条件查询列表
     */
    @Override
    public List<UserAction> findListByParam(UserActionQuery param) {
        return this.userActionMapper.selectList(param);
    }

    /**
     * 根据条件查询列表
     */
    @Override
    public Integer findCountByParam(UserActionQuery param) {
        return this.userActionMapper.selectCount(param);
    }

    /**
     * 分页查询方法
     */
    @Override
    public PaginationResultVO<UserAction> findListByPage(UserActionQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<UserAction> list = this.findListByParam(param);
        PaginationResultVO<UserAction> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    /**
     * 新增
     */
    @Override
    public Integer add(UserAction bean) {
        return this.userActionMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<UserAction> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.userActionMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或者修改
     */
    @Override
    public Integer addOrUpdateBatch(List<UserAction> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.userActionMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 多条件更新
     */
    @Override
    public Integer updateByParam(UserAction bean, UserActionQuery param) {
        StringTools.checkParam(param);
        return this.userActionMapper.updateByParam(bean, param);
    }

    /**
     * 多条件删除
     */
    @Override
    public Integer deleteByParam(UserActionQuery param) {
        StringTools.checkParam(param);
        return this.userActionMapper.deleteByParam(param);
    }

    /**
     * 根据ActionId获取对象
     */
    @Override
    public UserAction getUserActionByActionId(Integer actionId) {
        return this.userActionMapper.selectByActionId(actionId);
    }

    /**
     * 根据ActionId修改
     */
    @Override
    public Integer updateUserActionByActionId(UserAction bean, Integer actionId) {
        return this.userActionMapper.updateByActionId(bean, actionId);
    }

    /**
     * 根据ActionId删除
     */
    @Override
    public Integer deleteUserActionByActionId(Integer actionId) {
        return this.userActionMapper.deleteByActionId(actionId);
    }

    /**
     * 根据VideoIdAndCommentIdAndActionTypeAndUserId获取对象
     */
    @Override
    public UserAction getUserActionByVideoIdAndCommentIdAndActionTypeAndUserId(String videoId, Integer commentId, Integer actionType, String userId) {
        return this.userActionMapper.selectByVideoIdAndCommentIdAndActionTypeAndUserId(videoId, commentId, actionType, userId);
    }

    /**
     * 根据VideoIdAndCommentIdAndActionTypeAndUserId修改
     */
    @Override
    public Integer updateUserActionByVideoIdAndCommentIdAndActionTypeAndUserId(UserAction bean, String videoId, Integer commentId, Integer actionType, String userId) {
        return this.userActionMapper.updateByVideoIdAndCommentIdAndActionTypeAndUserId(bean, videoId, commentId, actionType, userId);
    }

    /**
     * 根据VideoIdAndCommentIdAndActionTypeAndUserId删除
     */
    @Override
    public Integer deleteUserActionByVideoIdAndCommentIdAndActionTypeAndUserId(String videoId, Integer commentId, Integer actionType, String userId) {
        return this.userActionMapper.deleteByVideoIdAndCommentIdAndActionTypeAndUserId(videoId, commentId, actionType, userId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveAction(UserAction bean) {
        VideoInfo videoInfo = videoInfoMapper.selectByVideoId(bean.getVideoId());
        if (videoInfo == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        bean.setVideoUserId(videoInfo.getUserId());
        //检查用户行为类型是不是允许的
        UserActionTypeEnum actionTypeEnum = UserActionTypeEnum.getByType(bean.getActionType());
        if (actionTypeEnum == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        UserAction dbAction = userActionMapper.selectByVideoIdAndCommentIdAndActionTypeAndUserId(
                bean.getVideoId(), bean.getCommentId(), bean.getActionType(), bean.getUserId());
        bean.setActionTime(new Date());
        //分情况处理
        switch (actionTypeEnum) {
            //点赞和收藏逻辑一样
            case VIDEO_LIKE:
            case VIDEO_COLLECT:
                if (dbAction != null) {
                    //点击的时候都是：有则删(取消点赞或者收藏)
                    userActionMapper.deleteByActionId(dbAction.getActionId());
                } else {
                    //没有则加(点赞或者收藏)
                    userActionMapper.insert(bean);
                }
                //更新投稿(视频)的点赞和收藏数量
                Integer changeCount = dbAction == null ? 1 : -1;
                videoInfoMapper.updateCountInfo(bean.getVideoId(), actionTypeEnum.getField(), changeCount);

                if (actionTypeEnum == UserActionTypeEnum.VIDEO_COLLECT) {
                    //TODO 更新es收藏数量
                    //esSearchComponent.updateDocCount(videoInfo.getVideoId(), SearchOrderTypeEnum.VIDEO_COLLECT.getField(), changeCount);
                }
                break;
            //投币的逻辑
            case VIDEO_COIN:
                if (videoInfo.getUserId().equals(bean.getUserId())) {
                    throw new BusinessException("UP主不能给自己投币");
                }
                if (dbAction != null) {
                    throw new BusinessException("对本稿件的投币枚数已用完");
                }
                //减少自己的硬币
                Integer updateCount = userInfoMapper.updateCoinCountInfo(bean.getUserId(), -bean.getActionCount());
                if (updateCount == 0) {
                    throw new BusinessException("币不够");
                }
                //增加该投稿作品UP主的硬币
                updateCount = userInfoMapper.updateCoinCountInfo(videoInfo.getUserId(), bean.getActionCount());
                if (updateCount == 0) {
                    throw new BusinessException("投币失败");
                }
                userActionMapper.insert(bean);
                //更新投稿作品硬币数量
                videoInfoMapper.updateCountInfo(bean.getVideoId(), actionTypeEnum.getField(), bean.getActionCount());
                break;
            //评论
            /*case COMMENT_LIKE:
            case COMMENT_HATE:
                UserActionTypeEnum opposeTypeEnum = UserActionTypeEnum.COMMENT_LIKE == actionTypeEnum ? UserActionTypeEnum.COMMENT_HATE : UserActionTypeEnum.COMMENT_LIKE;
                UserAction opposeAction = userActionMapper.selectByVideoIdAndCommentIdAndActionTypeAndUserId(bean.getVideoId(), bean.getCommentId(),
                        opposeTypeEnum.getType(), bean.getUserId());
                if (opposeAction != null) {
                    userActionMapper.deleteByActionId(opposeAction.getActionId());
                }

                if (dbAction != null) {
                    userActionMapper.deleteByActionId(dbAction.getActionId());
                } else {
                    userActionMapper.insert(bean);
                }
                changeCount = dbAction == null ? 1 : -1;
                Integer opposeChangeCount = changeCount * -1;
                videoCommentMapper.updateCountInfo(bean.getCommentId(),
                        actionTypeEnum.getField(),
                        changeCount,
                        opposeAction == null ? null : opposeTypeEnum.getField(),
                        opposeChangeCount);
                break;*/
        }

    }

}
