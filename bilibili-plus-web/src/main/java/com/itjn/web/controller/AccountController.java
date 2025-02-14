package com.itjn.web.controller;

import com.itjn.component.RedisComponent;
import com.itjn.entity.constants.Constants;
import com.itjn.entity.dto.TokenUserInfoDto;
import com.itjn.entity.dto.UserCountInfoDto;
import com.itjn.entity.po.UserInfo;
import com.itjn.entity.query.UserInfoQuery;
import com.itjn.entity.vo.PaginationResultVO;
import com.itjn.entity.vo.ResponseVO;
import com.itjn.exception.BusinessException;
import com.itjn.service.UserInfoService;
import com.itjn.utils.StringTools;
import com.itjn.web.annotation.GlobalInterceptor;
import com.wf.captcha.ArithmeticCaptcha;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

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
    @GlobalInterceptor
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
    @GlobalInterceptor
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
    @GlobalInterceptor
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
            return getSuccessResponseVO(tokenUserInfoDto);
        } finally {
            //用过一次的验证码就要删除
            redisComponent.cleanCheckCode(checkCodeKey);
            //请求登录接口过来时，必定会响应一个新的token给前端存到cookie里，如果之前前端浏览器就登录过的话，
            //那么之前的cookie里的token就失效了，会被新的替代。
            //这样的话这个老的token就可以去redis里删除了(其实设置了过期时间我们可以不用手动删除的)
            //其实这步有点不太能起到作用，因为在上一步的退出登录时我们就已经清理了redis和cookie里的token。

            //方法一：是在“前端代码中没有手动塞token到请求头的情况下”获取请求头里的Cookie中的token数据的方法。
            //浏览器发请求时请求头中自带的cookie中的数据的格式：
            //[Cookie:  token=2537aba2-c49c-49d9-b3a4-bc9f3af4ab38; ....; ....]//....代表Cookie里的其它数据
            Cookie[] cookies = request.getCookies();//String cookie = request.getHeader("Cookie");
            if (cookies != null) {
                String token = null;
                for (Cookie cookie : cookies) {
                    System.out.println("cookie:" + cookie.getName() + "=" + cookie.getValue());
                    if (Constants.TOKEN_WEB.equals(cookie.getName())) {
                        token = cookie.getValue();
                    }
                }
                if (!StringTools.isEmpty(token)) {
                    //清除redis里的上一个登录的token
                    redisComponent.cleanToken(token);
                }
            }
            //方法二：前端代码里往请求头里手动塞了token，不用再从cookie里拿了，直接从请求头里拿。
            /*String token = request.getHeader(Constants.TOKEN_WEB);
            if (!StringTools.isEmpty(token)) {
                //清除redis里的上一个登录的token
                redisComponent.cleanToken(token);
            }*/

        }

    }


    /**
     * 自动登录
     *
     * @param response
     * @return
     */
    @RequestMapping(value = "/autoLogin")
    @GlobalInterceptor
    public ResponseVO autoLogin(HttpServletResponse response) {
        //从redis中获取用户信息
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        //用户长时间没有登录，用户token过期了，也就是存在浏览器cookie里的token过期了，这时返回null前端就会提示用户重新登录。
        if (tokenUserInfoDto == null) {
            return getSuccessResponseVO(null);
        }
        //如果用户登录网站时，token还没过期，但是token快要到期了(token还有不到1天过期)，我们要给用户做续期。
        if (tokenUserInfoDto.getExpireAt() - System.currentTimeMillis() < Constants.REDIS_KEY_EXPIRES_DAY) {
            //重新设置一个token去给用户做自动登录续期
            redisComponent.saveTokenInfo(tokenUserInfoDto);
            //保存新的token到cookie中
            saveToken2Cookie(response, tokenUserInfoDto.getToken());
        }
        return getSuccessResponseVO(tokenUserInfoDto);
    }

    //退出登录
    @RequestMapping(value = "/logout")
    @GlobalInterceptor
    public ResponseVO logout(HttpServletResponse response) {
        //清除cookie:清理服务端redis里的token,同时清除浏览器端的cookie中的token
        cleanCookie(response);
        return getSuccessResponseVO(null);
    }

    /**
     * 获取用户数量信息：粉丝数，关注数，硬币数
     * @return
     */
    @RequestMapping(value = "/getUserCountInfo")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO getUserCountInfo() {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        UserCountInfoDto userCountInfoDto = userInfoService.getUserCountInfo(tokenUserInfoDto.getUserId());
        return getSuccessResponseVO(userCountInfoDto);
    }


}
