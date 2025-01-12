package com.itjn.entity.dto;

/**
 * @descriptio 用户消息的扩展信息DTO，存在数据库的扩展字段extend_json
 * @author JIU-W
 * @date 2025-01-11
 * @version 1.0
 */
public class UserMessageExtendDto {

    //"评论消息"的评论内容
    private String messageContent;

    //"评论消息"的回复内容(二级评论才有)
    private String messageContentReply;

    //"系统消息"的审核状态
    private Integer auditStatus;

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

    public String getMessageContentReply() {
        return messageContentReply;
    }

    public void setMessageContentReply(String messageContentReply) {
        this.messageContentReply = messageContentReply;
    }

    public Integer getAuditStatus() {
        return auditStatus;
    }

    public void setAuditStatus(Integer auditStatus) {
        this.auditStatus = auditStatus;
    }
}
