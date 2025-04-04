package com.itjn.web.controller;

import com.itjn.component.RedisComponent;
import com.itjn.entity.constants.Constants;
import com.itjn.entity.dto.TokenUserInfoDto;
import com.itjn.entity.enums.DateTimePatternEnum;
import com.itjn.entity.enums.ResponseCodeEnum;
import com.itjn.entity.vo.ResponseVO;
import com.itjn.exception.BusinessException;
import com.itjn.utils.DateUtil;
import org.apache.commons.io.FileUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.Date;


public class ABaseController {

    //@Resource
    //private AppConfig appConfig;

    @Resource
    private RedisComponent redisComponent;

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
/*

    public String getRealFilePath(String filePath) throws IOException {
        if (!filePath.contains(Constants.FILE_FOLDER_TEMP)) {
            return filePath;
        }
        File file = new File(appConfig.getProjectFolder() + filePath);
        if (!file.exists()) {
            return filePath;
        }
        String month = DateUtil.format(new Date(), DateTimePatternEnum.YYYYMM.getPattern());
        String folderName = appConfig.getProjectFolder() + month;
        File folder = new File(folderName);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File targetFile = new File(folder, file.getName());
        FileUtils.copyFile(file, targetFile);
        FileUtils.forceDelete(file);
        return month + "/" + file.getName();
    }

*/


    /**
     * 保存token到cookie中
     * @param response
     * @param token
     */
    public void saveToken2Cookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie(Constants.TOKEN_WEB, token);
        //设置Cookie的最大存活时间   -1会话级别 单位：秒
        cookie.setMaxAge(Constants.TIME_SECONDS_DAY * 7);//7天
        //设置Cookie的路径.setPath("/")：表示这个Cookie在整个应用的根路径下都有效，即所有路径都可以访问这个Cookie。
        cookie.setPath("/");
        //往HTTP响应里加上Cookie对象，当响应发送给客户端时，浏览器会保存这个Cookie。
        response.addCookie(cookie);
    }


    /**
     * 从redis中获取用户信息
     * @return
     */
    public TokenUserInfoDto getTokenUserInfoDto() {
        //从当前的请求上下文中获取 HttpServletRequest 对象
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String token = request.getHeader(Constants.TOKEN_WEB);
        return redisComponent.getTokenInfo(token);
    }

    /**
     * 从cookie中获取token从而通过token从redis获取用户信息
     * @return
     */
    public TokenUserInfoDto getTokenInfoFromCookie() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        //从cookie中获取token
        String token = getTokenFromCookie(request);
        if (token == null) {
            return null;
        }
        //从redis中获取用户信息
        return redisComponent.getTokenInfo(token);
    }

    /**
     * 从cookie中获取token
     * @param request
     * @return
     */
    private String getTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (cookie.getName().equalsIgnoreCase(Constants.TOKEN_WEB)) {
                return cookie.getValue();
            }
        }
        return null;
    }


    /**
     * 清理服务端redis里的token
     * 同时 清除浏览器端的cookie中的token
     * @param response
     */
    public void cleanCookie(HttpServletResponse response) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return;
        }
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(Constants.TOKEN_WEB)) {
                //清理redis中的token
                redisComponent.cleanToken(cookie.getValue());
                //当参数为0时，表示立即删除该Cookie。
                cookie.setMaxAge(0);
                cookie.setPath("/");
                response.addCookie(cookie);
                break;
            }
        }

    }


    /**
     * 获取用户ip地址
     * @return
     */
    protected String getIpAddr() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String ip = request.getHeader("x-forwarded-for");
        if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
            // 多次反向代理后会有多个ip值，第一个ip才是真实ip
            if (ip.indexOf(",") != -1) {
                ip = ip.split(",")[0];
            }
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

}
