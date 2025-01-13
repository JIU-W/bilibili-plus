package com.itjn.entity.enums;

/**
 * @description 数据统计类型枚举
 * @author JIU-W
 * @date 2025-01-13
 * @version 1.0
 */
public enum StatisticsTypeEnum {

    PLAY(0, "播放量"),
    FANS(1, "粉丝"),
    LIKE(2, "点赞"),
    COLLECTION(3, "收藏"),
    COIN(4, "投币"),
    COMMENT(5, "评论"),
    DANMU(6, "弹幕");

    private Integer type;
    private String desc;

    StatisticsTypeEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public static StatisticsTypeEnum getByType(Integer type) {
        for (StatisticsTypeEnum item : StatisticsTypeEnum.values()) {
            if (item.getType().equals(type)) {
                return item;
            }
        }
        return null;
    }

    public Integer getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
