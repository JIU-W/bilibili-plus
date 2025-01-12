package com.itjn.annotation;

import com.itjn.entity.enums.MessageTypeEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @description 自定义注解，用于记录用户消息
 * @author JIU-W
 * @date 2025-01-11
 * @version 1.0
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RecordUserMessage {

    //消息类型：点赞类型、收藏类型、评论类型、系统消息
    MessageTypeEnum messageType();

}
