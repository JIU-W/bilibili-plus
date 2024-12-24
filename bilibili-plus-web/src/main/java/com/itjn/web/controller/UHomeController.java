package com.itjn.web.controller;

import com.itjn.entity.constants.Constants;
import com.itjn.entity.dto.TokenUserInfoDto;
import com.itjn.entity.enums.PageSize;
import com.itjn.entity.enums.UserActionTypeEnum;
import com.itjn.entity.enums.VideoOrderTypeEnum;
import com.itjn.entity.po.UserInfo;
import com.itjn.entity.query.UserActionQuery;
import com.itjn.entity.query.UserFocusQuery;
import com.itjn.entity.query.VideoInfoQuery;
import com.itjn.entity.vo.PaginationResultVO;
import com.itjn.entity.vo.ResponseVO;
import com.itjn.entity.vo.UserInfoVO;
import com.itjn.service.UserActionService;
import com.itjn.service.UserFocusService;
import com.itjn.service.UserInfoService;
import com.itjn.service.VideoInfoService;
import com.itjn.utils.CopyTools;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.*;

@RestController
@Validated
@RequestMapping("/uhome")
public class UHomeController extends ABaseController {

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private VideoInfoService videoInfoService;

    @Resource
    private UserFocusService userFocusService;

    @Resource
    private UserActionService userActionService;

    /**
     * 获取用户主页信息： 1.查看个人主页  2.查看他人主页
     * @param userId
     * @return
     */
    @RequestMapping("/getUserInfo")
    //@GlobalInterceptor
    public ResponseVO getUserInfo(@NotEmpty String userId) {
        //当前登录用户
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        //获取用户详情
        UserInfo userInfo = userInfoService
                .getUserDetailInfo(null == tokenUserInfoDto ? null : tokenUserInfoDto.getUserId(), userId);
        UserInfoVO userInfoVO = CopyTools.copy(userInfo, UserInfoVO.class);
        return getSuccessResponseVO(userInfoVO);
    }

    /**
     * 修改用户信息
     */
    @RequestMapping("/updateUserInfo")
    //@GlobalInterceptor(checkLogin = true)
    public ResponseVO updateUserInfo(@NotEmpty @Size(max = 20) String nickName, @NotEmpty @Size(max = 100) String avatar,
                                     @NotNull Integer sex, String birthday, @Size(max = 150) String school,
                                     @Size(max = 80) String personIntroduction, @Size(max = 300) String noticeInfo) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(tokenUserInfoDto.getUserId());
        userInfo.setNickName(nickName);
        userInfo.setAvatar(avatar);
        userInfo.setSex(sex);
        userInfo.setBirthday(birthday);
        userInfo.setSchool(school);
        userInfo.setPersonIntroduction(personIntroduction);
        userInfo.setNoticeInfo(noticeInfo);
        userInfoService.updateUserInfo(userInfo, tokenUserInfoDto);
        return getSuccessResponseVO(null);
    }

    /**
     * 保存主题
     * @param theme
     * @return
     */
    @RequestMapping("/saveTheme")
    //@GlobalInterceptor
    public ResponseVO saveTheme(@Min(1) @Max(10) @NotNull Integer theme) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        UserInfo userInfo = new UserInfo();
        userInfo.setTheme(theme);
        userInfoService.updateUserInfoByUserId(userInfo, tokenUserInfoDto.getUserId());
        return getSuccessResponseVO(null);
    }

    /**
     * 关注
     * @param focusUserId 被关注用户id
     * @return
     */
    @RequestMapping("/focus")
    //@GlobalInterceptor(checkLogin = true)
    public ResponseVO focus(@NotEmpty String focusUserId) {
        userFocusService.focusUser(getTokenUserInfoDto().getUserId(), focusUserId);
        return getSuccessResponseVO(null);
    }

    /**
     * 取消关注
     * @param focusUserId
     * @return
     */
    @RequestMapping("/cancelFocus")
    //@GlobalInterceptor(checkLogin = true)
    public ResponseVO cancelFocus(@NotEmpty String focusUserId) {
        userFocusService.cancelFocus(getTokenUserInfoDto().getUserId(), focusUserId);
        return getSuccessResponseVO(null);
    }


    /**
     * 加载我的关注列表
     */
    @RequestMapping("/loadFocusList")
    //@GlobalInterceptor(checkLogin = true)
    public ResponseVO loadFocusList(Integer pageNo) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        UserFocusQuery focusQuery = new UserFocusQuery();
        //设置user_id
        focusQuery.setUserId(tokenUserInfoDto.getUserId());
        //设置查询类型为“查关注列表”
        focusQuery.setQueryType(Constants.ZERO);
        focusQuery.setPageNo(pageNo);
        focusQuery.setOrderBy("focus_time desc");
        //分页查询关注列表
        PaginationResultVO resultVO = userFocusService.findListByPage(focusQuery);
        return getSuccessResponseVO(resultVO);
    }

    /**
     * 加载我的粉丝列表
     */
    @RequestMapping("/loadFansList")
    //@GlobalInterceptor(checkLogin = true)
    public ResponseVO loadFansList(Integer pageNo) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        UserFocusQuery focusQuery = new UserFocusQuery();
        //设置focus_user_id
        focusQuery.setFocusUserId(tokenUserInfoDto.getUserId());
        //设置查询类型为“查粉丝列表”
        focusQuery.setQueryType(Constants.ONE);
        focusQuery.setPageNo(pageNo);
        focusQuery.setOrderBy("focus_time desc");
        //分页查询粉丝列表
        PaginationResultVO resultVO = userFocusService.findListByPage(focusQuery);
        return getSuccessResponseVO(resultVO);
    }


    /*
    @RequestMapping("/loadVideoList")
    //@GlobalInterceptor
    public ResponseVO loadVideoList(@NotEmpty String userId, Integer type, Integer pageNo, String videoName, Integer orderType) {
        VideoInfoQuery infoQuery = new VideoInfoQuery();
        if (type != null) {
            infoQuery.setPageSize(PageSize.SIZE10.getSize());
        }
        VideoOrderTypeEnum videoOrderTypeEnum = VideoOrderTypeEnum.getByType(orderType);
        if (videoOrderTypeEnum == null) {
            videoOrderTypeEnum = VideoOrderTypeEnum.CREATE_TIME;
        }
        infoQuery.setOrderBy(videoOrderTypeEnum.getField() + " desc");
        infoQuery.setVideoNameFuzzy(videoName);
        infoQuery.setPageNo(pageNo);
        infoQuery.setUserId(userId);
        PaginationResultVO resultVO = videoInfoService.findListByPage(infoQuery);
        return getSuccessResponseVO(resultVO);
    }


    @RequestMapping("/loadUserCollection")
    //@GlobalInterceptor
    public ResponseVO loadUserCollection(@NotEmpty String userId, Integer pageNo) {
        UserActionQuery actionQuery = new UserActionQuery();
        actionQuery.setActionType(UserActionTypeEnum.VIDEO_COLLECT.getType());
        actionQuery.setUserId(userId);
        actionQuery.setPageNo(pageNo);
        actionQuery.setQueryVideoInfo(true);
        actionQuery.setOrderBy("action_time desc");
        PaginationResultVO resultVO = userActionService.findListByPage(actionQuery);
        return getSuccessResponseVO(resultVO);
    }*/

}
