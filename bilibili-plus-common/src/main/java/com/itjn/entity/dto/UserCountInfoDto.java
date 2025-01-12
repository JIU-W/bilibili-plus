package com.itjn.entity.dto;

/**
 * @description 用户数量信息统计
 * @author JIU-W
 * @date 2025-01-12
 * @version 1.0
 */
public class UserCountInfoDto {

    //粉丝数量
    private Integer fansCount;

    //当前硬币数
    private Integer currentCoinCount;

    //关注数
    private Integer focusCount;

    public Integer getFansCount() {
        return fansCount;
    }

    public void setFansCount(Integer fansCount) {
        this.fansCount = fansCount;
    }

    public Integer getCurrentCoinCount() {
        return currentCoinCount;
    }

    public void setCurrentCoinCount(Integer currentCoinCount) {
        this.currentCoinCount = currentCoinCount;
    }

    public Integer getFocusCount() {
        return focusCount;
    }

    public void setFocusCount(Integer focusCount) {
        this.focusCount = focusCount;
    }
}
