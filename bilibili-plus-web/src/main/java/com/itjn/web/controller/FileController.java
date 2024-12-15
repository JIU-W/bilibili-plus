package com.itjn.web.controller;

import com.itjn.component.RedisComponent;
import com.itjn.entity.config.AppConfig;
import com.itjn.entity.constants.Constants;
import com.itjn.entity.dto.SysSettingDto;
import com.itjn.entity.dto.TokenUserInfoDto;
import com.itjn.entity.dto.UploadingFileDto;
import com.itjn.entity.dto.VideoPlayInfoDto;
import com.itjn.entity.enums.DateTimePatternEnum;
import com.itjn.entity.enums.FileTypeEnum;
import com.itjn.entity.enums.ResponseCodeEnum;
import com.itjn.entity.po.VideoInfoFile;
import com.itjn.entity.vo.ResponseVO;
import com.itjn.exception.BusinessException;
import com.itjn.service.VideoInfoFileService;
import com.itjn.utils.DateUtil;
import com.itjn.utils.FFmpegUtils;
import com.itjn.utils.StringTools;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;


@Validated
@Slf4j
@RestController
@RequestMapping("/file")
public class FileController extends ABaseController {

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private AppConfig appConfig;

    @Resource
    private VideoInfoFileService videoInfoFileService;

    @Resource
    private FFmpegUtils fFmpegUtils;


    @RequestMapping("/getResource")
    //@GlobalInterceptor
    public void getResource(HttpServletResponse response, @NotEmpty String sourceName) {
        if (!StringTools.pathIsOk(sourceName)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        String suffix = StringTools.getFileSuffix(sourceName);
        FileTypeEnum fileTypeEnum = FileTypeEnum.getBySuffix(suffix);
        if (null == fileTypeEnum) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        switch (fileTypeEnum) {
            case IMAGE:
                //缓存30天
                response.setHeader("Cache-Control", "max-age=" + 30 * 24 * 60 * 60);
                response.setContentType("image/" + suffix.replace(".", ""));
                break;
        }
        readFile(response, sourceName);
    }

    protected void readFile(HttpServletResponse response, String filePath) {
        File file = new File(appConfig.getProjectFolder() + Constants.FILE_FOLDER + filePath);
        if (!file.exists()) {
            return;
        }
        try (OutputStream out = response.getOutputStream(); FileInputStream in = new FileInputStream(file)) {
            byte[] byteData = new byte[1024];
            int len = 0;
            while ((len = in.read(byteData)) != -1) {
                out.write(byteData, 0, len);
            }
            out.flush();
        } catch (Exception e) {
            log.error("读取文件异常", e);
        }
    }


}
