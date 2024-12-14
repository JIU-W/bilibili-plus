package com.itjn.entity.query;

import lombok.Data;

import java.util.List;


/**
 * 用户信息参数
 */
@Data
public class UserInfoQuery extends BaseParam {


    /**
     * 用户ID
     */
    private String userId;

    //用于模糊查询
    private String userIdFuzzy;

    /**
     * 昵称
     */
    private String nickName;

    //用于模糊查询
    private String nickNameFuzzy;

    /**
     * 头像
     */
    private String avatar;

    //用于模糊查询
    private String avatarFuzzy;

    /**
     * 邮箱
     */
    private String email;

    private String emailFuzzy;

    /**
     * 密码
     */
    private String password;

    private String passwordFuzzy;

    /**
     * 0:女 1:男 2:保密
     */
    private Integer sex;

    /**
     * 出生日期
     */
    private String birthday;

    private String birthdayFuzzy;

    /**
     * 学校
     */
    private String school;

    private String schoolFuzzy;

    /**
     * 个人简介
     */
    private String personIntroduction;

    private String personIntroductionFuzzy;

    /**
     * 加入时间
     */
    private String joinTime;

    private String joinTimeStart;

    private String joinTimeEnd;

    /**
     * 最后登录时间
     */
    private String lastLoginTime;

    private String lastLoginTimeStart;

    private String lastLoginTimeEnd;

    /**
     * 最后登录IP
     */
    private String lastLoginIp;

    private String lastLoginIpFuzzy;

    /**
     * 0:禁用 1:正常
     */
    private Integer status;

    /**
     * 空间公告
     */
    private String noticeInfo;

    private String noticeInfoFuzzy;

    /**
     * 硬币总数量
     */
    private Integer totalCoinCount;

    /**
     * 当前硬币数
     */
    private Integer currentCoinCount;

    /**
     * 主题
     */
    private Integer theme;

    private List<String> userIdList;

}
