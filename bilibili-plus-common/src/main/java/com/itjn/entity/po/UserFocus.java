package com.itjn.entity.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.itjn.entity.enums.DateTimePatternEnum;
import com.itjn.utils.DateUtil;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;


/**
 * 粉丝，关注表
 */
@Data
public class UserFocus implements Serializable {


    /**
     * 用户ID
     */
    private String userId;

    /**
     * 关注用户ID
     */
    private String focusUserId;

    /**
     * 关注时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date focusTime;

    /**
     * 以下都是返回给前端的信息
     */
    //关注的人或者粉丝的昵称
    private String otherNickName;

    //关注的人或者粉丝的ID
    private String otherUserId;

    //关注的人或者粉丝的的个人介绍
    private String otherPersonIntroduction;

    //关注的人或者粉丝的头像
    private String otherAvatar;

    //查询是否互相关注(是否互粉)
    //0：未互关 1：已互关
    private Integer focusType;

    @Override
    public String toString() {
        return "用户ID:" + (userId == null ? "空" : userId) + "，关注用户ID:" + (focusUserId == null ? "空" : focusUserId) + "，关注时间:" + (focusTime == null ? "空" : DateUtil.format(focusTime,
                DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()));
    }


}
