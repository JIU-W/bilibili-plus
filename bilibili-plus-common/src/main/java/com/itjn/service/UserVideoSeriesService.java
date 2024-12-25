package com.itjn.service;

import com.itjn.entity.po.UserVideoSeries;
import com.itjn.entity.query.UserVideoSeriesQuery;
import com.itjn.entity.vo.PaginationResultVO;

import java.util.List;


/**
 * 用户视频序列归档 业务接口
 */
public interface UserVideoSeriesService {

    /**
     * 根据条件查询列表
     */
    List<UserVideoSeries> findListByParam(UserVideoSeriesQuery param);

    /**
     * 根据条件查询列表
     */
    Integer findCountByParam(UserVideoSeriesQuery param);

    /**
     * 分页查询
     */
    PaginationResultVO<UserVideoSeries> findListByPage(UserVideoSeriesQuery param);

    /**
     * 新增
     */
    Integer add(UserVideoSeries bean);

    /**
     * 批量新增
     */
    Integer addBatch(List<UserVideoSeries> listBean);

    /**
     * 批量新增/修改
     */
    Integer addOrUpdateBatch(List<UserVideoSeries> listBean);

    /**
     * 多条件更新
     */
    Integer updateByParam(UserVideoSeries bean, UserVideoSeriesQuery param);

    /**
     * 多条件删除
     */
    Integer deleteByParam(UserVideoSeriesQuery param);

    /**
     * 根据SeriesId查询对象
     */
    UserVideoSeries getUserVideoSeriesBySeriesId(Integer seriesId);


    /**
     * 根据SeriesId修改
     */
    Integer updateUserVideoSeriesBySeriesId(UserVideoSeries bean, Integer seriesId);


    /**
     * 根据SeriesId删除
     */
    Integer deleteUserVideoSeriesBySeriesId(Integer seriesId);

    /**
     * 保存用户视频系列归档
     */
    void saveUserVideoSeries(UserVideoSeries bean, String videoIds);

    void changeVideoSeriesSort(String userId, String seriesIds);

    /**
     * 保存视频到合集 或者是 更改合集里的视频的"排序"
     */
    void saveSeriesVideo(String userId, Integer seriesId, String videoIds);

    /**
     * 删除视频合集里的视频
     * @param userId
     * @param seriesId
     * @param videoId
     */
    void delSeriesVideo(String userId, Integer seriesId, String videoId);

    /**
     * 根据userId查询用户视频系列归档
     */
    List<UserVideoSeries> getUserAllSeries(String userId);

    /**
     * 删除视频合集
     * @param userId
     * @param seriesId
     */
    void delVideoSeries(String userId, Integer seriesId);

    List<UserVideoSeries> findListWithVideoList(UserVideoSeriesQuery query);

}
