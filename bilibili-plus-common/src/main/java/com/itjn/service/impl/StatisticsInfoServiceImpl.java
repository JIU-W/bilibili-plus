package com.itjn.service.impl;

import com.itjn.component.RedisComponent;
import com.itjn.entity.constants.Constants;
import com.itjn.entity.enums.PageSize;
import com.itjn.entity.enums.StatisticsTypeEnum;
import com.itjn.entity.enums.UserActionTypeEnum;
import com.itjn.entity.po.StatisticsInfo;
import com.itjn.entity.po.VideoInfo;
import com.itjn.entity.query.SimplePage;
import com.itjn.entity.query.StatisticsInfoQuery;
import com.itjn.entity.query.UserInfoQuery;
import com.itjn.entity.query.VideoInfoQuery;
import com.itjn.entity.vo.PaginationResultVO;
import com.itjn.mappers.StatisticsInfoMapper;
import com.itjn.mappers.UserFocusMapper;
import com.itjn.mappers.UserInfoMapper;
import com.itjn.mappers.VideoInfoMapper;
import com.itjn.service.StatisticsInfoService;
import com.itjn.utils.DateUtil;
import com.itjn.utils.StringTools;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 数据统计 业务接口实现
 */
@Service("statisticsInfoService")
public class StatisticsInfoServiceImpl implements StatisticsInfoService {

    @Resource
    private StatisticsInfoMapper<StatisticsInfo, StatisticsInfoQuery> statisticsInfoMapper;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private VideoInfoMapper videoInfoMapper;

    @Resource
    private UserFocusMapper userFocusMapper;

    @Resource
    private UserInfoMapper userInfoMapper;

    /**
     * 根据条件查询列表
     */
    @Override
    public List<StatisticsInfo> findListByParam(StatisticsInfoQuery param) {
        return this.statisticsInfoMapper.selectList(param);
    }

    /**
     * 根据条件查询列表
     */
    @Override
    public Integer findCountByParam(StatisticsInfoQuery param) {
        return this.statisticsInfoMapper.selectCount(param);
    }

    /**
     * 分页查询方法
     */
    @Override
    public PaginationResultVO<StatisticsInfo> findListByPage(StatisticsInfoQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<StatisticsInfo> list = this.findListByParam(param);
        PaginationResultVO<StatisticsInfo> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    /**
     * 新增
     */
    @Override
    public Integer add(StatisticsInfo bean) {
        return this.statisticsInfoMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<StatisticsInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.statisticsInfoMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或者修改
     */
    @Override
    public Integer addOrUpdateBatch(List<StatisticsInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.statisticsInfoMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 多条件更新
     */
    @Override
    public Integer updateByParam(StatisticsInfo bean, StatisticsInfoQuery param) {
        StringTools.checkParam(param);
        return this.statisticsInfoMapper.updateByParam(bean, param);
    }

    /**
     * 多条件删除
     */
    @Override
    public Integer deleteByParam(StatisticsInfoQuery param) {
        StringTools.checkParam(param);
        return this.statisticsInfoMapper.deleteByParam(param);
    }

    /**
     * 根据StatisticsDateAndUserIdAndDataType获取对象
     */
    @Override
    public StatisticsInfo getStatisticsInfoByStatisticsDateAndUserIdAndDataType(String statisticsDate, String userId, Integer dataType) {
        return this.statisticsInfoMapper.selectByStatisticsDateAndUserIdAndDataType(statisticsDate, userId, dataType);
    }

    /**
     * 根据StatisticsDateAndUserIdAndDataType修改
     */
    @Override
    public Integer updateStatisticsInfoByStatisticsDateAndUserIdAndDataType(StatisticsInfo bean, String statisticsDate, String userId, Integer dataType) {
        return this.statisticsInfoMapper.updateByStatisticsDateAndUserIdAndDataType(bean, statisticsDate, userId, dataType);
    }

    /**
     * 根据StatisticsDateAndUserIdAndDataType删除
     */
    @Override
    public Integer deleteStatisticsInfoByStatisticsDateAndUserIdAndDataType(String statisticsDate, String userId, Integer dataType) {
        return this.statisticsInfoMapper.deleteByStatisticsDateAndUserIdAndDataType(statisticsDate, userId, dataType);
    }

    public void statisticsData() {

        List<StatisticsInfo> statisticsInfoList = new ArrayList<>();

        //获取前一天的日期
        final String statisticsDate = DateUtil.getBeforeDayDate(1);

        //1.统计播放量(只统计前一天的数据)
        //获取redis中的所有的播放量数据
        Map<String, Integer> videoPlayCountMap = redisComponent.getVideoPlayCount(statisticsDate);

        List<String> playVideoKeys = new ArrayList<>(videoPlayCountMap.keySet());
        //从key中截取出videoId         (key的格式：easylive:video:playcount:2025-01-13:A6jOX5lxQG)
        playVideoKeys = playVideoKeys.stream().map(item ->
                item.substring(item.lastIndexOf(":") + 1)).collect(Collectors.toList());
        //查询出videoId集合对应的视频信息
        VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
        videoInfoQuery.setVideoIdArray(playVideoKeys.toArray(new String[playVideoKeys.size()]));
        List<VideoInfo> videoInfoList = videoInfoMapper.selectList(videoInfoQuery);
        //分组统计每个用户的视频播放量的总和。    map格式：<userId, 该用户所有视频播放量的总和>
        Map<String, Integer> videoCountMap = videoInfoList.stream().collect(Collectors.groupingBy
                (VideoInfo::getUserId,
                        Collectors.summingInt(item -> videoPlayCountMap.get(Constants.REDIS_KEY_VIDEO_PLAY_COUNT + statisticsDate
                        + ":" + item.getVideoId()))));

        //往statisticsInfoList中添加数据
        videoCountMap.forEach((k, v) -> {
            StatisticsInfo statisticsInfo = new StatisticsInfo();
            statisticsInfo.setStatisticsDate(statisticsDate);
            statisticsInfo.setUserId(k);
            statisticsInfo.setDataType(StatisticsTypeEnum.PLAY.getType());
            statisticsInfo.setStatisticsCount(v);
            statisticsInfoList.add(statisticsInfo);
        });

        //2.统计前一天增加的粉丝量(只统计前一天的数据)
        List<StatisticsInfo> fansDataList = this.statisticsInfoMapper.selectStatisticsFans(statisticsDate);
        for (StatisticsInfo statisticsInfo : fansDataList) {
            statisticsInfo.setStatisticsDate(statisticsDate);
            statisticsInfo.setDataType(StatisticsTypeEnum.FANS.getType());
        }
        statisticsInfoList.addAll(fansDataList);

        //3.统计用户前一天所有视频作品总共新增的评论数量(只统计前一天的数据)  TODO SQL被改正了
        List<StatisticsInfo> commentDataList = this.statisticsInfoMapper.selectStatisticsComment(statisticsDate);
        for (StatisticsInfo statisticsInfo : commentDataList) {
            statisticsInfo.setStatisticsDate(statisticsDate);
            statisticsInfo.setDataType(StatisticsTypeEnum.COMMENT.getType());
        }
        statisticsInfoList.addAll(commentDataList);

        //4.统计用户前一天所有视频作品总共收到的 "视频点赞数量"、"视频收藏数量"、"投币数量"(只统计前一天的数据)
        List<StatisticsInfo> statisticsInfoOthers = this.statisticsInfoMapper.selectStatisticsInfo(statisticsDate,
                new Integer[]{UserActionTypeEnum.VIDEO_LIKE.getType(), UserActionTypeEnum.VIDEO_COIN.getType(),
                        UserActionTypeEnum.VIDEO_COLLECT.getType()});

        for (StatisticsInfo statisticsInfo : statisticsInfoOthers) {
            statisticsInfo.setStatisticsDate(statisticsDate);
            if (UserActionTypeEnum.VIDEO_LIKE.getType().equals(statisticsInfo.getDataType())) {
                statisticsInfo.setDataType(StatisticsTypeEnum.LIKE.getType());
            } else if (UserActionTypeEnum.VIDEO_COLLECT.getType().equals(statisticsInfo.getDataType())) {
                statisticsInfo.setDataType(StatisticsTypeEnum.COLLECTION.getType());
            } else if (UserActionTypeEnum.VIDEO_COIN.getType().equals(statisticsInfo.getDataType())) {
                statisticsInfo.setDataType(StatisticsTypeEnum.COIN.getType());
            }
        }
        statisticsInfoList.addAll(statisticsInfoOthers);

        //5.统计用户前一天所有视频作品总共新增的弹幕数量(只统计前一天的数据)
        List<StatisticsInfo> danmuDataList = this.statisticsInfoMapper.selectStatisticsDanmu(statisticsDate);
        for (StatisticsInfo statisticsInfo : danmuDataList) {
            statisticsInfo.setStatisticsDate(statisticsDate);
            statisticsInfo.setDataType(StatisticsTypeEnum.DANMU.getType());
        }
        statisticsInfoList.addAll(danmuDataList);

        //正常情况下用insert就行，因为定时任务只会执行一次。但是为了方便我们"测试"时对数据的多次执行，还是使用insertOrUpdate。
        this.statisticsInfoMapper.insertOrUpdateBatch(statisticsInfoList);
    }


    public Map<String, Integer> getStatisticsInfoActualTime(String userId) {
        //查询出用户所有的统计数据(除了粉丝数)
        Map<String, Integer> result = statisticsInfoMapper.selectTotalCountInfo(userId);
        if (!StringTools.isEmpty(userId)) {
            //查询用户的粉丝数
            result.put("fansCount", userFocusMapper.selectFansCount(userId));
        } else {
            //管理后台的某个接口走这种情况：查询系统所有用户数
            result.put("userCount", userInfoMapper.selectCount(new UserInfoQuery()));
        }
        return result;
    }


    public List<StatisticsInfo> findListTotalInfoByParam(StatisticsInfoQuery param) {
        return statisticsInfoMapper.selectListTotalInfoByParam(param);
    }


    public List<StatisticsInfo> findUserCountTotalInfoByParam(StatisticsInfoQuery param) {
        return statisticsInfoMapper.selectUserCountTotalInfoByParam(param);
    }

}
