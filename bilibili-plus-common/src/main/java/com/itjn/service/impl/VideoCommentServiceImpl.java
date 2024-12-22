package com.itjn.service.impl;

import com.itjn.component.EsSearchComponent;
import com.itjn.entity.constants.Constants;
import com.itjn.entity.enums.CommentTopTypeEnum;
import com.itjn.entity.enums.PageSize;
import com.itjn.entity.enums.ResponseCodeEnum;
import com.itjn.entity.enums.UserActionTypeEnum;
import com.itjn.entity.po.UserInfo;
import com.itjn.entity.po.VideoComment;
import com.itjn.entity.po.VideoInfo;
import com.itjn.entity.query.SimplePage;
import com.itjn.entity.query.UserInfoQuery;
import com.itjn.entity.query.VideoCommentQuery;
import com.itjn.entity.query.VideoInfoQuery;
import com.itjn.entity.vo.PaginationResultVO;
import com.itjn.exception.BusinessException;
import com.itjn.mappers.UserInfoMapper;
import com.itjn.mappers.VideoCommentMapper;
import com.itjn.mappers.VideoInfoMapper;
import com.itjn.service.UserMessageService;
import com.itjn.service.VideoCommentService;
import com.itjn.utils.StringTools;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;


/**
 * 评论 业务接口实现
 */
@Service("videoCommentService")
public class VideoCommentServiceImpl implements VideoCommentService {

    @Resource
    private VideoCommentMapper<VideoComment, VideoCommentQuery> videoCommentMapper;

    @Resource
    private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

    @Resource
    private VideoInfoMapper<VideoInfo, VideoInfoQuery> videoInfoMapper;

    @Resource
    private UserMessageService userMessageService;

    @Resource
    private EsSearchComponent esSearchComponent;

    /**
     * 根据条件查询列表
     */
    @Override
    public List<VideoComment> findListByParam(VideoCommentQuery param) {
        if (param.getLoadChildren() != null && param.getLoadChildren()) {
            return this.videoCommentMapper.selectListWithChildren(param);
        }
        return this.videoCommentMapper.selectList(param);
    }

    /**
     * 根据条件查询列表
     */
    @Override
    public Integer findCountByParam(VideoCommentQuery param) {
        return this.videoCommentMapper.selectCount(param);
    }

    /**
     * 分页查询方法
     */
    @Override
    public PaginationResultVO<VideoComment> findListByPage(VideoCommentQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<VideoComment> list = this.findListByParam(param);
        PaginationResultVO<VideoComment> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    /**
     * 新增
     */
    @Override
    public Integer add(VideoComment bean) {
        return this.videoCommentMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<VideoComment> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.videoCommentMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或者修改
     */
    @Override
    public Integer addOrUpdateBatch(List<VideoComment> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.videoCommentMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 多条件更新
     */
    @Override
    public Integer updateByParam(VideoComment bean, VideoCommentQuery param) {
        StringTools.checkParam(param);
        return this.videoCommentMapper.updateByParam(bean, param);
    }

    /**
     * 多条件删除
     */
    @Override
    public Integer deleteByParam(VideoCommentQuery param) {
        StringTools.checkParam(param);
        return this.videoCommentMapper.deleteByParam(param);
    }

    /**
     * 根据CommentId获取对象
     */
    @Override
    public VideoComment getVideoCommentByCommentId(Integer commentId) {
        return this.videoCommentMapper.selectByCommentId(commentId);
    }

    /**
     * 根据CommentId修改
     */
    @Override
    public Integer updateVideoCommentByCommentId(VideoComment bean, Integer commentId) {
        return this.videoCommentMapper.updateByCommentId(bean, commentId);
    }

    /**
     * 根据CommentId删除
     */
    @Override
    public Integer deleteVideoCommentByCommentId(Integer commentId) {
        return this.videoCommentMapper.deleteByCommentId(commentId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void postComment(VideoComment comment, Integer replyCommentId) {
        //判断投稿作品是否存在
        VideoInfo videoInfo = videoInfoMapper.selectByVideoId(comment.getVideoId());
        if (videoInfo == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        //是否关闭评论
        if (videoInfo.getInteraction() != null && videoInfo.getInteraction().contains(Constants.ZERO.toString())) {
            throw new BusinessException("UP主已关闭评论区");
        }
        //判断是一级评论二级评论
        if (replyCommentId != null) {
            //
            VideoComment replyComment = getVideoCommentByCommentId(replyCommentId);
            if (replyComment == null || !replyComment.getVideoId().equals(comment.getVideoId())) {
                throw new BusinessException(ResponseCodeEnum.CODE_600);
            }
            if (replyComment.getpCommentId() == 0) {
                comment.setpCommentId(replyComment.getCommentId());
            } else {
                comment.setpCommentId(replyComment.getpCommentId());
                comment.setReplyUserId(replyComment.getUserId());
            }
            UserInfo userInfo = userInfoMapper.selectByUserId(replyComment.getUserId());
            comment.setReplyNickName(userInfo.getNickName());
            comment.setReplyAvatar(userInfo.getAvatar());
        } else {
            //一级评论
            comment.setpCommentId(0);
        }
        comment.setPostTime(new Date());
        comment.setVideoUserId(videoInfo.getUserId());
        this.videoCommentMapper.insert(comment);
        //增加评论数
        if (comment.getpCommentId() == 0) {
            this.videoInfoMapper.updateCountInfo(comment.getVideoId(), UserActionTypeEnum.VIDEO_COMMENT.getField(), 1);
        }
    }

    @Override
    public void deleteComment(Integer commentId, String userId) {
        VideoComment comment = videoCommentMapper.selectByCommentId(commentId);
        if (null == comment) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        VideoInfo videoInfo = videoInfoMapper.selectByVideoId(comment.getVideoId());
        if (null == videoInfo) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        if (userId != null && !videoInfo.getUserId().equals(userId) && !comment.getUserId().equals(userId)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        videoCommentMapper.deleteByCommentId(commentId);
        if (comment.getpCommentId() == 0) {
            videoInfoMapper.updateCountInfo(videoInfo.getVideoId(), UserActionTypeEnum.VIDEO_COMMENT.getField(), -1);
            //删除二级评论
            VideoCommentQuery videoCommentQuery = new VideoCommentQuery();
            videoCommentQuery.setpCommentId(commentId);
            videoCommentMapper.deleteByParam(videoCommentQuery);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void topComment(Integer commentId, String userId) {
        this.cancelTopComment(commentId, userId);
        VideoComment videoComment = new VideoComment();
        videoComment.setTopType(CommentTopTypeEnum.TOP.getType());
        videoCommentMapper.updateByCommentId(videoComment, commentId);
    }

    @Override
    public void cancelTopComment(Integer commentId, String userId) {
//现清除之前的指定
        VideoComment dbVideoComment = videoCommentMapper.selectByCommentId(commentId);
        if (dbVideoComment == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        VideoInfo videoInfo = videoInfoMapper.selectByVideoId(dbVideoComment.getVideoId());
        if (videoInfo == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        if (!videoInfo.getUserId().equals(userId)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        VideoComment videoComment = new VideoComment();
        videoComment.setTopType(CommentTopTypeEnum.NO_TOP.getType());

        VideoCommentQuery videoCommentQuery = new VideoCommentQuery();
        videoCommentQuery.setVideoId(dbVideoComment.getVideoId());
        videoCommentQuery.setTopType(CommentTopTypeEnum.TOP.getType());
        videoCommentMapper.updateByParam(videoComment, videoCommentQuery);
    }
}
