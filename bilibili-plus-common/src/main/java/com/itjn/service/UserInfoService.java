package com.itjn.service;

import com.itjn.entity.dto.TokenUserInfoDto;
import com.itjn.entity.dto.UserCountInfoDto;
import com.itjn.entity.po.UserInfo;
import com.itjn.entity.query.UserInfoQuery;
import com.itjn.entity.vo.PaginationResultVO;

import java.util.List;


/**
 * 用户信息 业务接口
 */
public interface UserInfoService {

    /**
     * 根据条件查询列表
     */
    List<UserInfo> findListByParam(UserInfoQuery param);

    /**
     * 根据条件查询列表
     */
    Integer findCountByParam(UserInfoQuery param);

    /**
     * 分页查询
     */
    PaginationResultVO<UserInfo> findListByPage(UserInfoQuery param);

    /**
     * 新增
     */
    Integer add(UserInfo bean);

    /**
     * 批量新增
     */
    Integer addBatch(List<UserInfo> listBean);

    /**
     * 批量新增/修改
     */
    Integer addOrUpdateBatch(List<UserInfo> listBean);

    /**
     * 多条件更新
     */
    Integer updateByParam(UserInfo bean, UserInfoQuery param);

    /**
     * 多条件删除
     */
    Integer deleteByParam(UserInfoQuery param);

    /**
     * 根据UserId查询对象
     */
    UserInfo getUserInfoByUserId(String userId);


    /**
     * 根据UserId修改
     */
    Integer updateUserInfoByUserId(UserInfo bean, String userId);


    /**
     * 根据UserId删除
     */
    Integer deleteUserInfoByUserId(String userId);


    /**
     * 根据Email查询对象
     */
    UserInfo getUserInfoByEmail(String email);


    /**
     * 根据Email修改
     */
    Integer updateUserInfoByEmail(UserInfo bean, String email);


    /**
     * 根据Email删除
     */
    Integer deleteUserInfoByEmail(String email);


    /**
     * 根据NickName查询对象
     */
    UserInfo getUserInfoByNickName(String nickName);


    /**
     * 根据NickName修改
     */
    Integer updateUserInfoByNickName(UserInfo bean, String nickName);


    /**
     * 根据NickName删除
     */
    Integer deleteUserInfoByNickName(String nickName);

    TokenUserInfoDto login(String email, String password, String ip);

    void register(String email, String nickName, String password);


    void updateUserInfo(UserInfo userInfo, TokenUserInfoDto tokenUserInfoDto);

    UserInfo getUserDetailInfo(String currentUserId, String userId);

    UserCountInfoDto getUserCountInfo(String userId);

    Integer updateCoinCountInfo(String userId, Integer changeCount);
}
