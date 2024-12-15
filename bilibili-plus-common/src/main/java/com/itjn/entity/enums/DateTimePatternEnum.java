package com.itjn.entity.enums;


/**
 * 时间格式枚举
 */
public enum DateTimePatternEnum {

    YYYY_MM_DD_HH_MM_SS("yyyy-MM-dd HH:mm:ss"),
    YYYY_MM_DD("yyyy-MM-dd"),
    YYYYMM("yyyyMM"),
    YYYYMMDD("yyyyMMdd");

    private String pattern;

    DateTimePatternEnum(String pattern) {
        this.pattern = pattern;
    }

    public String getPattern() {
        return pattern;
    }

}
