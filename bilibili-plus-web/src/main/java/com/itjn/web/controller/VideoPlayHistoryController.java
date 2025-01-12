package com.itjn.web.controller;

import com.itjn.entity.dto.TokenUserInfoDto;
import com.itjn.entity.query.VideoPlayHistoryQuery;
import com.itjn.entity.vo.ResponseVO;
import com.itjn.service.VideoPlayHistoryService;
import com.itjn.web.annotation.GlobalInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;

@RestController
@Validated
@RequestMapping("/history")
@Slf4j
public class VideoPlayHistoryController extends ABaseController {

    @Resource
    private VideoPlayHistoryService videoPlayHistoryService;

    /*@RequestMapping("/loadHistory")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO loadHistory(Integer pageNo) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        VideoPlayHistoryQuery historyQuery = new VideoPlayHistoryQuery();
        historyQuery.setUserId(tokenUserInfoDto.getUserId());
        historyQuery.setOrderBy("last_update_time desc");
        historyQuery.setPageNo(pageNo);
        historyQuery.setQueryVideoDetail(true);
        return getSuccessResponseVO(videoPlayHistoryService.findListByPage(historyQuery));
    }

    @RequestMapping("/cleanHistory")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO cleanHistory() {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        VideoPlayHistoryQuery historyQuery = new VideoPlayHistoryQuery();
        historyQuery.setUserId(tokenUserInfoDto.getUserId());
        videoPlayHistoryService.deleteByParam(historyQuery);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/delHistory")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO delHistory(@NotEmpty String videoId) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        videoPlayHistoryService.deleteVideoPlayHistoryByUserIdAndVideoId(tokenUserInfoDto.getUserId(), videoId);
        return getSuccessResponseVO(null);
    }*/

}
