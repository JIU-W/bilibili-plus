package com.itjn.web.controller;

import com.itjn.component.RedisComponent;
import com.itjn.entity.constants.Constants;
import com.itjn.entity.dto.TokenUserInfoDto;
import com.itjn.entity.po.UserInfo;
import com.itjn.entity.query.UserInfoQuery;
import com.itjn.entity.vo.PaginationResultVO;
import com.itjn.entity.vo.ResponseVO;
import com.itjn.exception.BusinessException;
import com.itjn.service.UserInfoService;
import com.itjn.utils.StringTools;
import com.wf.captcha.ArithmeticCaptcha;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.HashMap;
import java.util.Map;

/**
 * @description 用户账号管理
 * @author JIU-W
 * @date 2024-12-12
 * @version 1.0
 */
@RestController("accountController")
@RequestMapping("/account")
@Validated
public class AccountController extends ABaseController {

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private RedisComponent redisComponent;

    //分页查询用户信息
    @RequestMapping("/loadDataList")
    public ResponseVO loadDataList(UserInfoQuery userInfoQuery) {
        PaginationResultVO<UserInfo> listByPage = userInfoService.findListByPage(userInfoQuery);
        ResponseVO successResponseVO = getSuccessResponseVO(listByPage);
        return successResponseVO;
    }

    //生成验证码
    @RequestMapping(value = "/checkCode")
    //@GlobalInterceptor
    public ResponseVO checkCode() {
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(100, 42);

        String code = captcha.text();
        String checkCodeKey = redisComponent.saveCheckCode(code);

        Map<String, String> result = new HashMap<>();
        String checkCodeBase64 = captcha.toBase64();
        result.put("checkCode", checkCodeBase64);
        result.put("checkCodeKey", checkCodeKey);
        return getSuccessResponseVO(result);
    }

    //注册
    @RequestMapping(value = "/register")
    //@GlobalInterceptor
    public ResponseVO register(@NotEmpty @Email @Size(max = 150) String email,
                               @NotEmpty @Size(max = 20) String nickName,
                               @NotEmpty @Pattern(regexp = Constants.REGEX_PASSWORD) String registerPassword,
                               @NotEmpty String checkCodeKey,
                               @NotEmpty String checkCode) {
        try {
            if (!checkCode.equalsIgnoreCase(redisComponent.getCheckCode(checkCodeKey))) {
                throw new BusinessException("图片验证码不正确");
            }
            userInfoService.register(email, nickName, registerPassword);
            return getSuccessResponseVO(null);
        } finally {
            redisComponent.cleanCheckCode(checkCodeKey);
        }
    }

    //登录
    @RequestMapping(value = "/login")
    //@GlobalInterceptor
    public ResponseVO login(HttpServletRequest request, HttpServletResponse response,
                            @NotEmpty @Email String email, @NotEmpty String password,
                            @NotEmpty String checkCodeKey, @NotEmpty String checkCode) {
        try {
            if (!checkCode.equalsIgnoreCase(redisComponent.getCheckCode(checkCodeKey))) {
                throw new BusinessException("图片验证码不正确");
            }
            //记录最后一次登录时的ip
            String ip = getIpAddr();
            TokenUserInfoDto tokenUserInfoDto = userInfoService.login(email, password, ip);
            //保存token到cookie中
            saveToken2Cookie(response, tokenUserInfoDto.getToken());
            //TODO 设置粉丝数，关注数，硬币数
            return getSuccessResponseVO(tokenUserInfoDto);
        } finally {
            //清理reids里面的验证码
            redisComponent.cleanCheckCode(checkCodeKey);
            //清理token
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                String token = null;
                for (Cookie cookie : cookies) {
                    if (Constants.TOKEN_WEB.equals(cookie.getName())) {
                        token = cookie.getValue();
                    }
                }
                if (!StringTools.isEmpty(token)) {
                    redisComponent.cleanToken(token);
                }
            }
        }
    }
/*
    @RequestMapping(value = "/autoLogin")
    //@GlobalInterceptor
    public ResponseVO autoLogin(HttpServletResponse response) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        if (tokenUserInfoDto == null) {
            return getSuccessResponseVO(null);
        }
        if (tokenUserInfoDto.getExpireAt() - System.currentTimeMillis() < Constants.REDIS_KEY_EXPIRES_DAY) {
            redisComponent.saveTokenInfo(tokenUserInfoDto);
            saveToken2Cookie(response, tokenUserInfoDto.getToken());
        }
        return getSuccessResponseVO(tokenUserInfoDto);
    }

    @RequestMapping(value = "/logout")
    //@GlobalInterceptor
    public ResponseVO logout(HttpServletResponse response) {
        cleanCookie(response);
        return getSuccessResponseVO(null);
    }

    @RequestMapping(value = "/getUserCountInfo")
    //@GlobalInterceptor(checkLogin = true)
    public ResponseVO getUserCountInfo() {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        UserCountInfoDto userCountInfoDto = userInfoService.getUserCountInfo(tokenUserInfoDto.getUserId());
        return getSuccessResponseVO(userCountInfoDto);
    }
    */

}
