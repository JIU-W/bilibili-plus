package com.itjn.web.controller;

import com.itjn.entity.dto.TokenUserInfoDto;
import com.itjn.entity.dto.UserMessageCountDto;
import com.itjn.entity.enums.MessageReadTypeEnum;
import com.itjn.entity.po.UserMessage;
import com.itjn.entity.query.UserMessageQuery;
import com.itjn.entity.vo.PaginationResultVO;
import com.itjn.entity.vo.ResponseVO;
import com.itjn.service.UserMessageService;
import com.itjn.web.annotation.GlobalInterceptor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@Validated
@RequestMapping("/message")
public class UserMessageContrller extends ABaseController {

    @Resource
    private UserMessageService userMessageService;

    /**
     * 获取未读消息数量
     * @return
     */
    @RequestMapping("/getNoReadCount")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO getNoReadCount() {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        if (tokenUserInfoDto == null) {
            return getSuccessResponseVO(0);
        }
        UserMessageQuery messageQuery = new UserMessageQuery();
        messageQuery.setUserId(tokenUserInfoDto.getUserId());
        messageQuery.setReadType(MessageReadTypeEnum.NO_READ.getType());
        Integer count = userMessageService.findCountByParam(messageQuery);
        return getSuccessResponseVO(count);
    }

    /**
     *
     * @return
     */
    @RequestMapping("/getNoReadCountGroup")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO getNoReadCountGroup() {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        List<UserMessageCountDto> dataList = userMessageService.getMessageTypeNoReadCount(tokenUserInfoDto.getUserId());
        return getSuccessResponseVO(dataList);
    }

    /*
    @RequestMapping("/readAll")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO readAll(Integer messageType) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();

        UserMessageQuery userMessageQuery = new UserMessageQuery();
        userMessageQuery.setUserId(tokenUserInfoDto.getUserId());
        userMessageQuery.setMessageType(messageType);

        UserMessage userMessage = new UserMessage();
        userMessage.setReadType(MessageReadTypeEnum.READ.getType());
        userMessageService.updateByParam(userMessage, userMessageQuery);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/loadMessage")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO loadMessage(@NotNull Integer messageType, Integer pageNo) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        UserMessageQuery messageQuery = new UserMessageQuery();
        messageQuery.setMessageType(messageType);
        messageQuery.setPageNo(pageNo);
        messageQuery.setUserId(tokenUserInfoDto.getUserId());
        messageQuery.setOrderBy("message_id desc");
        PaginationResultVO resultVO = userMessageService.findListByPage(messageQuery);
        return getSuccessResponseVO(resultVO);
    }

    @RequestMapping("/delMessage")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO delMessage(@NotNull Integer messageId) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        UserMessageQuery messageQuery = new UserMessageQuery();
        messageQuery.setUserId(tokenUserInfoDto.getUserId());
        messageQuery.setMessageId(messageId);
        userMessageService.deleteByParam(messageQuery);
        return getSuccessResponseVO(null);
    }*/

}
