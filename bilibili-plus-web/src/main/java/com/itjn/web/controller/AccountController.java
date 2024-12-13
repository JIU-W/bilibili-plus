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
 * @author JIU-W
 * @version 1.0
 * @description 用户账号管理
 * @date 2024-12-12
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
            //用过一次的验证码就要删除
            redisComponent.cleanCheckCode(checkCodeKey);
            //请求登录接口过来时，必定会响应一个新的token给前端存到cookie里，如果之前前端浏览器就登录过的话，
            //那么之前的cookie里的token就失效了，会被新的替代。
            //这样的话这个老的token就可以去redis里删除了(其实设置了过期时间我们可以不用手动删除的)
            //因为在cookie里，所以每次请求的请求头里会有token，所以就能拿到去redis删掉。

            /*Cookie[] cookies = request.getCookies();
            if (cookies != null) {//请求体里的cookies也可能为null，也就是之前没有登录过，或者是之前的过期了。
                String token = null;
                for (Cookie cookie : cookies) {
                    if (Constants.TOKEN_WEB.equals(cookie.getName())) {
                        token = cookie.getValue();
                    }
                }
                if (!StringTools.isEmpty(token)) {
                    //清除redis里的上一个登录的token
                    redisComponent.cleanToken(token);
                }
            }*/

            //TODO 方法二：不用去遍历请求头里的cookie了，直接从请求头里拿，效率更高。
            //存在浏览器cookie的token的格式：token=2537aba2-c49c-49d9-b3a4-bc9f3af4ab38
            String cookie = request.getHeader("Cookie");
            if (!StringTools.isEmpty(cookie)) {
                String token = null;
                String[] split = cookie.split("=");
                if (Constants.TOKEN_WEB.equals(split[0])) {
                    token = split[1];
                }
                if (!StringTools.isEmpty(token)) {
                    redisComponent.cleanToken(token);
                }
            }

        }

    }

    //自动登录
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



/*

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
