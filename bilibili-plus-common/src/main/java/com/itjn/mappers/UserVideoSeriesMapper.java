package com.itjn.mappers;

import com.itjn.entity.po.UserVideoSeries;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户视频序列归档 数据库操作接口
 */
public interface UserVideoSeriesMapper<T, P> extends BaseMapper<T, P> {

    /**
     * 根据SeriesId更新
     */
    Integer updateBySeriesId(@Param("bean") T t, @Param("seriesId") Integer seriesId);


    /**
     * 根据SeriesId删除
     */
    Integer deleteBySeriesId(@Param("seriesId") Integer seriesId);


    /**
     * 根据SeriesId获取对象
     */
    T selectBySeriesId(@Param("seriesId") Integer seriesId);

    /**
     * 根据userId获取最大排序
     */
    Integer selectMaxSort(@Param("userId") String userId);

    /**
     * 更新排序
     */
    void changeSort(@Param("videoSeriesList") List<UserVideoSeries> videoSeriesList);

    List<T> selectUserAllSeries(@Param("userId") String userId);

    /**
     * 获取用户的"视频合集"列表(每个合集都附带查5条视频)
     */
    List<T> selectListWithVideoList(@Param("query") P p);

}
