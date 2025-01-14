package com.itjn.admin.controller;

import com.itjn.component.RedisComponent;
import com.itjn.entity.dto.SysSettingDto;
import com.itjn.entity.vo.ResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Validated
@Slf4j
@RestController
@RequestMapping("/setting")
public class SettingController extends ABaseController {

    @Resource
    private RedisComponent redisComponent;

    /**
     * 获取系统设置
     * @return
     */
    @RequestMapping("/getSetting")
    public ResponseVO getSetting() {
        return getSuccessResponseVO(redisComponent.getSysSettingDto());
    }

    /**
     * 保存系统设置
     */
    @RequestMapping("/saveSetting")
    public ResponseVO saveSetting(SysSettingDto sysSettingDto) {
        redisComponent.saveSettingDto(sysSettingDto);
        return getSuccessResponseVO(null);
    }

}
