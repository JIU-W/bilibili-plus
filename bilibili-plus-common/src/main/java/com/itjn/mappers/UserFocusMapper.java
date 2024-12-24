package com.itjn.mappers;

import org.apache.ibatis.annotations.Param;

/**
 * 粉丝，关注表 数据库操作接口
 */
public interface UserFocusMapper<T, P> extends BaseMapper<T, P> {

    /**
     * 根据UserIdAndFocusUserId更新
     */
    Integer updateByUserIdAndFocusUserId(@Param("bean") T t, @Param("userId") String userId, @Param("focusUserId") String focusUserId);


    /**
     * 根据UserIdAndFocusUserId删除
     */
    Integer deleteByUserIdAndFocusUserId(@Param("userId") String userId, @Param("focusUserId") String focusUserId);


    /**
     * 根据UserIdAndFocusUserId获取对象
     */
    T selectByUserIdAndFocusUserId(@Param("userId") String userId, @Param("focusUserId") String focusUserId);


    /**
     * 根据UserId查询关注数
     * @param userId
     * @return
     */
    Integer selectFocusCount(@Param("userId") String userId);

    /**
     * 根据UserId查询粉丝数
     * @param userId
     * @return
     */
    Integer selectFansCount(@Param("userId") String userId);

}
