package com.itjn.service;


import com.itjn.entity.po.UserFocus;
import com.itjn.entity.query.UserFocusQuery;
import com.itjn.entity.vo.PaginationResultVO;

import java.util.List;


/**
 * 粉丝，关注表 业务接口
 */
public interface UserFocusService {

    /**
     * 根据条件查询列表
     */
    List<UserFocus> findListByParam(UserFocusQuery param);

    /**
     * 根据条件查询列表
     */
    Integer findCountByParam(UserFocusQuery param);

    /**
     * 分页查询
     */
    PaginationResultVO<UserFocus> findListByPage(UserFocusQuery param);

    /**
     * 新增
     */
    Integer add(UserFocus bean);

    /**
     * 批量新增
     */
    Integer addBatch(List<UserFocus> listBean);

    /**
     * 批量新增/修改
     */
    Integer addOrUpdateBatch(List<UserFocus> listBean);

    /**
     * 多条件更新
     */
    Integer updateByParam(UserFocus bean, UserFocusQuery param);

    /**
     * 多条件删除
     */
    Integer deleteByParam(UserFocusQuery param);

    /**
     * 根据UserIdAndFocusUserId查询对象
     */
    UserFocus getUserFocusByUserIdAndFocusUserId(String userId, String focusUserId);


    /**
     * 根据UserIdAndFocusUserId修改
     */
    Integer updateUserFocusByUserIdAndFocusUserId(UserFocus bean, String userId, String focusUserId);


    /**
     * 根据UserIdAndFocusUserId删除
     */
    Integer deleteUserFocusByUserIdAndFocusUserId(String userId, String focusUserId);

    /**
     * 关注
     */
    /**
     * 关注
     * @param userId 当前用户id
     * @param focusUserId 被关注用户id
     */
    void focusUser(String userId, String focusUserId);

    /**
     * 取消关注
     * @param userId 当前用户id
     * @param focusUserId 被关注用户id
     */
    void cancelFocus(String userId, String focusUserId);

}
