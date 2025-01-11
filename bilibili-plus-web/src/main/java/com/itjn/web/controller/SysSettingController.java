package com.itjn.web.controller;

import com.itjn.component.RedisComponent;
import com.itjn.entity.vo.ResponseVO;
import com.itjn.web.annotation.GlobalInterceptor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController("sysSettingController")
@RequestMapping("/sysSetting")
@Validated
public class SysSettingController extends ABaseController {

    @Resource
    private RedisComponent redisComponent;

    @RequestMapping(value = "/getSetting")
    @GlobalInterceptor
    public ResponseVO getSetting() {
        return getSuccessResponseVO(redisComponent.getSysSettingDto());
    }

}
