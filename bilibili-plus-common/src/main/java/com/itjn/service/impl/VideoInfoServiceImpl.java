package com.itjn.service.impl;

import com.itjn.component.EsSearchComponent;
import com.itjn.component.RedisComponent;
import com.itjn.entity.config.AppConfig;
import com.itjn.entity.dto.SysSettingDto;
import com.itjn.entity.enums.PageSize;
import com.itjn.entity.enums.ResponseCodeEnum;
import com.itjn.entity.enums.UserActionTypeEnum;
import com.itjn.entity.po.*;
import com.itjn.entity.query.*;
import com.itjn.entity.vo.PaginationResultVO;
import com.itjn.exception.BusinessException;
import com.itjn.mappers.*;
import com.itjn.service.UserInfoService;
import com.itjn.service.VideoInfoService;
import com.itjn.utils.StringTools;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * 视频信息 业务接口实现
 */
@Service("videoInfoService")
@Slf4j
public class VideoInfoServiceImpl implements VideoInfoService {

    private static ExecutorService executorService = Executors.newFixedThreadPool(10);

    @Resource
    private AppConfig appConfig;

    @Resource
    private VideoInfoMapper<VideoInfo, VideoInfoQuery> videoInfoMapper;

    @Resource
    private VideoInfoPostMapper<VideoInfoPost, VideoInfoPostQuery> videoInfoPostMapper;

    @Resource
    private EsSearchComponent esSearchComponent;

    @Resource
    private VideoInfoFileMapper<VideoInfoFile, VideoInfoFileQuery> videoInfoFileMapper;

    @Resource
    private VideoInfoFilePostMapper<VideoInfoFilePost, VideoInfoFilePostQuery> videoInfoFilePostMapper;

    @Resource
    private VideoDanmuMapper<VideoDanmu, VideoDanmuQuery> videoDanmuMapper;

    @Resource
    private VideoCommentMapper<VideoComment, VideoCommentQuery> videoCommentMapper;

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private RedisComponent redisComponent;

    /**
     * 根据条件查询列表
     */
    @Override
    public List<VideoInfo> findListByParam(VideoInfoQuery param) {
        return this.videoInfoMapper.selectList(param);
    }

    /**
     * 根据条件查询列表
     */
    @Override
    public Integer findCountByParam(VideoInfoQuery param) {
        return this.videoInfoMapper.selectCount(param);
    }

    /**
     * 分页查询方法
     */
    @Override
    public PaginationResultVO<VideoInfo> findListByPage(VideoInfoQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<VideoInfo> list = this.findListByParam(param);
        PaginationResultVO<VideoInfo> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    /**
     * 新增
     */
    @Override
    public Integer add(VideoInfo bean) {
        return this.videoInfoMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<VideoInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.videoInfoMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或者修改
     */
    @Override
    public Integer addOrUpdateBatch(List<VideoInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.videoInfoMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 多条件更新
     */
    @Override
    public Integer updateByParam(VideoInfo bean, VideoInfoQuery param) {
        StringTools.checkParam(param);
        return this.videoInfoMapper.updateByParam(bean, param);
    }

    /**
     * 多条件删除
     */
    @Override
    public Integer deleteByParam(VideoInfoQuery param) {
        StringTools.checkParam(param);
        return this.videoInfoMapper.deleteByParam(param);
    }

    /**
     * 根据VideoId获取对象
     */
    @Override
    public VideoInfo getVideoInfoByVideoId(String videoId) {
        return this.videoInfoMapper.selectByVideoId(videoId);
    }

    /**
     * 根据VideoId修改
     */
    @Override
    public Integer updateVideoInfoByVideoId(VideoInfo bean, String videoId) {
        return this.videoInfoMapper.updateByVideoId(bean, videoId);
    }

    /**
     * 根据VideoId删除
     */
    @Override
    public Integer deleteVideoInfoByVideoId(String videoId) {
        return this.videoInfoMapper.deleteByVideoId(videoId);
    }

    @Override
    public void addReadCount(String videoId) {
        this.videoInfoMapper.updateCountInfo(videoId, UserActionTypeEnum.VIDEO_PLAY.getField(), 1);
    }


    @Transactional(rollbackFor = Exception.class)
    public void changeInteraction(String videoId, String userId, String interaction) {
        VideoInfo videoInfo = new VideoInfo();
        videoInfo.setInteraction(interaction);
        VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
        videoInfoQuery.setVideoId(videoId);
        videoInfoQuery.setUserId(userId);
        //修改投稿信息(正式表)
        videoInfoMapper.updateByParam(videoInfo, videoInfoQuery);

        VideoInfoPost videoInfoPost = new VideoInfoPost();
        videoInfoPost.setInteraction(interaction);
        VideoInfoPostQuery videoInfoPostQuery = new VideoInfoPostQuery();
        videoInfoPostQuery.setVideoId(videoId);
        videoInfoPostQuery.setUserId(userId);
        //更新(发布时的投稿信息)
        videoInfoPostMapper.updateByParam(videoInfoPost, videoInfoPostQuery);
    }


    @Transactional(rollbackFor = Exception.class)
    public void deleteVideo(String videoId, String userId) {
        VideoInfoPost videoInfoPost = this.videoInfoPostMapper.selectByVideoId(videoId);
        if (videoInfoPost == null || (userId != null && !userId.equals(videoInfoPost.getUserId()))) {
            throw new BusinessException(ResponseCodeEnum.CODE_404);
        }
        //删除投稿信息(正式表)
        this.videoInfoMapper.deleteByVideoId(videoId);
        //删除投稿信息(发布时的投稿信息)
        this.videoInfoPostMapper.deleteByVideoId(videoId);
        /**
         * 删除用户硬币
         */
        SysSettingDto sysSettingDto = redisComponent.getSysSettingDto();
        userInfoService.updateCoinCountInfo(videoInfoPost.getUserId(), -sysSettingDto.getPostVideoCoinCount());

        /**
         * 删除es信息：删除一条文档数据
         */
        esSearchComponent.delDoc(videoId);

        //开启异步线程池去删除文件
        executorService.execute(() -> {
            VideoInfoFileQuery videoInfoFileQuery = new VideoInfoFileQuery();
            videoInfoFileQuery.setVideoId(videoId);
            //查询分P
            List<VideoInfoFile> videoInfoFileList = this.videoInfoFileMapper.selectList(videoInfoFileQuery);

            //删除分P视频信息
            videoInfoFileMapper.deleteByParam(videoInfoFileQuery);
            //删除发布时的分P视频信息
            VideoInfoFilePostQuery videoInfoFilePostQuery = new VideoInfoFilePostQuery();
            videoInfoFilePostQuery.setVideoId(videoId);
            videoInfoFilePostMapper.deleteByParam(videoInfoFilePostQuery);

            //删除弹幕
            VideoDanmuQuery videoDanmuQuery = new VideoDanmuQuery();
            videoDanmuQuery.setVideoId(videoId);
            videoDanmuMapper.deleteByParam(videoDanmuQuery);

            //删除评论
            VideoCommentQuery videoCommentQuery = new VideoCommentQuery();
            videoCommentQuery.setVideoId(videoId);
            videoCommentMapper.deleteByParam(videoCommentQuery);

            //删除文件
            for (VideoInfoFile item : videoInfoFileList) {
                try {
                    FileUtils.deleteDirectory(new File(appConfig.getProjectFolder() + item.getFilePath()));
                } catch (IOException e) {
                    log.error("删除文件失败，文件路径:{}", item.getFilePath());
                }
            }

        });

    }
}
