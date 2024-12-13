package com.itjn.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * @description 登录返回给前端的用户信息
 * @author JIU-W
 * @date 2024-12-13
 * @version 1.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)//忽略json字符串中不识别的属性
public class TokenUserInfoDto implements Serializable {
    private static final long serialVersionUID = -6910208948981307451L;
    private String userId;
    private String nickName;
    private String avatar;

    //token过期时间
    private Long expireAt;
    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getExpireAt() {
        return expireAt;
    }

    public void setExpireAt(Long expireAt) {
        this.expireAt = expireAt;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

}
