package com.itjn.entity.vo;

/**
 * @description 对三种类型投稿的总数进行封装
 * @author JIU-W
 * @date 2024-12-20
 * @version 1.0
 */
public class VideoStatusCountInfoVO {
    private Integer auditPassCount;//审核通过
    private Integer auditFailCount;//审核不通过
    private Integer inProgress;//进行中

    public Integer getAuditPassCount() {
        return auditPassCount;
    }

    public void setAuditPassCount(Integer auditPassCount) {
        this.auditPassCount = auditPassCount;
    }

    public Integer getAuditFailCount() {
        return auditFailCount;
    }

    public void setAuditFailCount(Integer auditFailCount) {
        this.auditFailCount = auditFailCount;
    }

    public Integer getInProgress() {
        return inProgress;
    }

    public void setInProgress(Integer inProgress) {
        this.inProgress = inProgress;
    }
}
