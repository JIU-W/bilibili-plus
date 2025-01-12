package com.itjn.entity.dto;

/**
 * @description 分类型未读消息数量
 * @author JIU-W
 * @date 2025-01-12
 * @version 1.0
 */
public class UserMessageCountDto {

    public Integer messageType;
    private Integer messageCount;

    public Integer getMessageType() {
        return messageType;
    }

    public void setMessageType(Integer messageType) {
        this.messageType = messageType;
    }

    public Integer getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(Integer messageCount) {
        this.messageCount = messageCount;
    }
}
