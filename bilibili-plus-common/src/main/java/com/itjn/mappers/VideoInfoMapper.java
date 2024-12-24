package com.itjn.mappers;

import com.itjn.entity.dto.CountInfoDto;
import org.apache.ibatis.annotations.Param;

/**
 * 文件信息 数据库操作接口
 */
public interface VideoInfoMapper<T, P> extends BaseMapper<T, P> {

    /**
     * 根据VideoId更新
     */
    Integer updateByVideoId(@Param("bean") T t, @Param("videoId") String videoId);


    /**
     * 根据VideoId删除
     */
    Integer deleteByVideoId(@Param("videoId") String videoId);


    /**
     * 根据VideoId获取对象
     */
    T selectByVideoId(@Param("videoId") String videoId);

    /**
     * 更新统计信息
     */
    void updateCountInfo(@Param("videoId") String videoId, @Param("field") String field,
                         @Param("changeCount") Integer changeCount);

    /**
     * 获取统计信息
     * @param userId
     * @return
     */
    CountInfoDto selectSumCountInfo(@Param("userId") String userId);

}
