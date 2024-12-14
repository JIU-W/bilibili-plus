package com.itjn.admin.interceptor;

import com.itjn.component.RedisComponent;
import com.itjn.entity.constants.Constants;
import com.itjn.entity.enums.ResponseCodeEnum;
import com.itjn.exception.BusinessException;
import com.itjn.utils.StringTools;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @description 登录认证拦截器
 * @author JIU-W
 * @date 2024-12-14
 * @version 1.0
 */
@Component
public class AppInterceptor implements HandlerInterceptor {

    private final static String URL_ACCOUNT = "/account";
    private final static String URL_FILE = "/file";

    @Resource
    private RedisComponent redisComponent;


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (null == handler) {
            return false;
        }
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        //路径以/account开头则放行：放行验证码和登录接口
        if (request.getRequestURI().contains(URL_ACCOUNT)) {
            return true;
        }
        //从请求头中获取token
        String token = request.getHeader(Constants.TOKEN_ADMIN);
        //特殊情况："获取图片等文件的接口"无法直接从请求头里取到(前端的原因，没有去塞token到header)，
        //只能手动从请求体的cookie中遍历获取token
        if (request.getRequestURI().contains(URL_FILE)) {
            //从cookie中遍历获取token
            token = getTokenFromCookie(request);
        }
        //校验token
        if (StringTools.isEmpty(token)) {
            throw new BusinessException(ResponseCodeEnum.CODE_901);
        }
        //校验Redis中是否有该token
        Object sessionObj = redisComponent.getLoginInfo4Admin(token);
        if (sessionObj == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_901);
        }
        return true;
    }

    /**
     * 从cookie中遍历获取token
     * @param request
     * @return
     */
    private String getTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (cookie.getName().equalsIgnoreCase(Constants.TOKEN_ADMIN)) {
                return cookie.getValue();
            }
        }
        return null;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }

}
