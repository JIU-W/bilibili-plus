package com.itjn.aspect;

import com.itjn.annotation.RecordUserMessage;
import com.itjn.entity.constants.Constants;
import com.itjn.entity.dto.TokenUserInfoDto;
import com.itjn.entity.enums.MessageTypeEnum;
import com.itjn.entity.enums.ResponseCodeEnum;
import com.itjn.entity.enums.UserActionTypeEnum;
import com.itjn.entity.vo.ResponseVO;
import com.itjn.exception.BusinessException;
import com.itjn.redis.RedisUtils;
import com.itjn.service.UserMessageService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * @description 自定义用户消息记录切面
 * @author JIU-W
 * @date 2025-01-11
 * @version 1.0
 */
@Component("userMessageOperationAspect")
@Aspect
@Slf4j
public class UserMessageOperationAspect {

    private static final String PARAMETERS_VIDEO_ID = "videoId";

    private static final String PARAMETERS_ACTION_TYPE = "actionType";

    private static final String PARAMETERS_REPLY_COMMENTID = "replyCommentId";

    //系统消息需要的参数：审核不通过原因
    private static final String PARAMETERS_AUDIT_REJECT_REASON = "reason";

    //评论消息需要的参数：评论内容
    private static final String PARAMETERS_CONTENT = "content";

    @Resource
    private RedisUtils redisUtils;

    @Resource
    private UserMessageService userMessageService;


    /**
     *
     * Around：环绕通知：在目标方法执行前后执行，可以修改参数，可以获取到目标方法的返回值，捕获并处理异常。
     * @param point
     * @return
     * @throws Exception
     */
    @Around("@annotation(com.itjn.annotation.RecordUserMessage)")   //切点位置：加了注解@annotation的方法
    public ResponseVO interceptorDo(ProceedingJoinPoint point) throws Exception {
        try {           //ProceedingJoinPoint:目标方法的上下文。可以通过它获取到目标方法的各种信息

            //调用目标方法执行：相当于就是AOP这里帮助调用执行了目标方法，目标方法就不用再执行了。
            ResponseVO result = (ResponseVO) point.proceed();

            //获取目标方法
            Method method = ((MethodSignature) point.getSignature()).getMethod();
            //获取注解
            RecordUserMessage recordUserMessage = method.getAnnotation(RecordUserMessage.class);
            if (recordUserMessage != null) {
                //记录用户的消息(将消息填入数据库的消息表)
                saveUserMessage(recordUserMessage, point.getArgs(), method.getParameters());
            }
            return result;//返回目标方法的返回值
        } catch (BusinessException e) {
            log.error("全局拦截器异常", e);
            throw e;
        } catch (Exception e) {
            log.error("全局拦截器异常", e);
            throw e;
        } catch (Throwable e) {
            log.error("全局拦截器异常", e);
            throw new BusinessException(ResponseCodeEnum.CODE_500);
        }
    }

    /**
     * 记录用户的消息
     *              目标方法是这几个其中之一：doAction()，   postComment()，   auditVideo()
     * 所有的消息类型：
     *              1.系统消息：管理员对我的作品的审核消息
     *              2.点赞：自己作品被点赞/给别人作品点赞
     *              3.收藏：自己作品被收藏/收藏别人作品
     *              4.评论：自己作品被评论/自己发布评论
     * @param recordUserMessage 注解
     * @param arguments 目标方法参数具体的值
     * @param parameters 目标方法的参数
     */
    private void saveUserMessage(RecordUserMessage recordUserMessage, Object[] arguments, Parameter[] parameters) {
        //视频id："所有消息"都需要
        String videoId = null;
        //行为类型："点赞"或者"收藏"的消息需要
        Integer actionType = null;
        //回复评论id："评论消息"需要
        Integer replyCommentId = null;
        //评论内容/审核不通过原因："评论消息"或者"系统消息"需要
        String content = null;
        for (int i = 0; i < parameters.length; i++) {
            if (PARAMETERS_VIDEO_ID.equals(parameters[i].getName())) {
                videoId = (String) arguments[i];
            } else if (PARAMETERS_ACTION_TYPE.equals(parameters[i].getName())) {
                actionType = (Integer) arguments[i];
            } else if (PARAMETERS_REPLY_COMMENTID.equals(parameters[i].getName())) {
                replyCommentId = (Integer) arguments[i];
            } else if (PARAMETERS_AUDIT_REJECT_REASON.equals(parameters[i].getName())) {
                content = (String) arguments[i];
            } else if (PARAMETERS_CONTENT.equals(parameters[i].getName())) {
                content = (String) arguments[i];
            }
        }
        //确定actionType是"点赞"还是"收藏"类型的消息
        MessageTypeEnum messageTypeEnum = recordUserMessage.messageType();
        if (UserActionTypeEnum.VIDEO_COLLECT.getType().equals(actionType)) {
            messageTypeEnum = MessageTypeEnum.COLLECTION;
        }

        //获取消息发送人信息(当前用户信息)
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();

        //保存消息到数据库
        //系统消息不需要消息发送人(管理端获取不到用户信息，系统发送，不需要用户id)
        userMessageService.saveUserMessage(videoId, tokenUserInfoDto == null ? null : tokenUserInfoDto.getUserId(),
                messageTypeEnum, content, replyCommentId);
    }

    private TokenUserInfoDto getTokenUserInfoDto() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String token = request.getHeader(Constants.TOKEN_WEB);
        return (TokenUserInfoDto) redisUtils.get(Constants.REDIS_KEY_TOKEN_WEB + token);
    }

}
