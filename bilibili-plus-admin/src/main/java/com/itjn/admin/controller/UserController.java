package com.itjn.admin.controller;


import com.itjn.entity.po.UserInfo;
import com.itjn.entity.query.UserInfoQuery;
import com.itjn.entity.vo.PaginationResultVO;
import com.itjn.entity.vo.ResponseVO;
import com.itjn.service.UserInfoService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/user")
@Validated
public class UserController extends ABaseController {
    @Resource
    private UserInfoService userInfoService;

    /**
     * 分页查询用户信息
     */
    @RequestMapping("/loadUser")
    public ResponseVO loadUser(UserInfoQuery userInfoQuery) {
        userInfoQuery.setOrderBy("join_time desc");
        PaginationResultVO resultVO = userInfoService.findListByPage(userInfoQuery);
        return getSuccessResponseVO(resultVO);
    }

    /**
     * 修改用户状态
     */
    @RequestMapping("/changeStatus")
    public ResponseVO changeStatus(String userId, Integer status) {
        UserInfo userInfo = new UserInfo();
        userInfo.setStatus(status);
        userInfoService.updateUserInfoByUserId(userInfo, userId);
        return getSuccessResponseVO(null);
    }

}
