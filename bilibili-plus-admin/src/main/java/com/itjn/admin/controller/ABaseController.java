package com.itjn.admin.controller;

import com.itjn.entity.config.AppConfig;
import com.itjn.entity.constants.Constants;
import com.itjn.entity.enums.ResponseCodeEnum;
import com.itjn.entity.vo.ResponseVO;
import com.itjn.exception.BusinessException;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;


public class ABaseController {

    @Resource
    private AppConfig appConfig;

    protected static final String STATUC_SUCCESS = "success";

    protected static final String STATUC_ERROR = "error";

    protected <T> ResponseVO getSuccessResponseVO(T t) {
        ResponseVO<T> responseVO = new ResponseVO<>();
        responseVO.setStatus(STATUC_SUCCESS);
        responseVO.setCode(ResponseCodeEnum.CODE_200.getCode());
        responseVO.setInfo(ResponseCodeEnum.CODE_200.getMsg());
        responseVO.setData(t);
        return responseVO;
    }

    protected <T> ResponseVO getBusinessErrorResponseVO(BusinessException e, T t) {
        ResponseVO vo = new ResponseVO();
        vo.setStatus(STATUC_ERROR);
        if (e.getCode() == null) {
            vo.setCode(ResponseCodeEnum.CODE_600.getCode());
        } else {
            vo.setCode(e.getCode());
        }
        vo.setInfo(e.getMessage());
        vo.setData(t);
        return vo;
    }

    protected <T> ResponseVO getServerErrorResponseVO(T t) {
        ResponseVO vo = new ResponseVO();
        vo.setStatus(STATUC_ERROR);
        vo.setCode(ResponseCodeEnum.CODE_500.getCode());
        vo.setInfo(ResponseCodeEnum.CODE_500.getMsg());
        vo.setData(t);
        return vo;
    }

    public void saveToken2Cookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie(Constants.TOKEN_ADMIN, token);
        //设置Cookie的最大存活时间
        //当参数为-1时，表示Cookie是会话Cookie，即Cookie只在当前会话期间有效，浏览器关闭后Cookie将被自动删除。
        cookie.setMaxAge(-1);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

}
