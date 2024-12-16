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
        //判断文件名是否合法(路径中包含 .. 的话则会访问到上一层目录的内容导致越权)
        if (!StringTools.pathIsOk(sourceName)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        //获取文件后缀
        String suffix = StringTools.getFileSuffix(sourceName);
        //根据文件后缀判断文件类型
        FileTypeEnum fileTypeEnum = FileTypeEnum.getBySuffix(suffix);
        //不支持的文件类型
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
        //读取文件
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

    /**
     * 上传视频文件之前的接口
     * @param fileName 文件名
     * @param chunks 文件分片总数
     * @return
     */
    @RequestMapping("/preUploadVideo")
    //@GlobalInterceptor(checkLogin = true)
    public ResponseVO preUploadVideo(@NotEmpty String fileName, @NotNull Integer chunks) {
        //获取当前用户信息
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        //保存上传文件信息到redis
        String uploadId = redisComponent.savePreVideoFileInfo(tokenUserInfoDto.getUserId(), fileName, chunks);
        //返回上传文件标识
        return getSuccessResponseVO(uploadId);
    }

    /**
     * 上传视频文件：分片上传
     * @param chunkFile
     * @param chunkIndex
     * @param uploadId
     * @return
     * @throws IOException
     */
    // 1.上传文件分片 2.合并文件分片 3.生成视频文件 4.生成视频缩略图
    // 5.生成视频封面 6.生成视频信息 7.生成视频播放地址 8.生成视频播放记录
    @RequestMapping("/uploadVideo")
    //@GlobalInterceptor(checkLogin = true)
    public ResponseVO uploadVideo(@NotNull MultipartFile chunkFile, @NotNull Integer chunkIndex, @NotEmpty String uploadId) throws IOException {
        //获取当前用户信息
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        //从redis中获取上传文件时的临时信息
        UploadingFileDto fileDto = redisComponent.getUploadingVideoFile(tokenUserInfoDto.getUserId(), uploadId);
        if (fileDto == null) {
            throw new BusinessException("文件不存在请重新上传");
        }
        //获取系统设置
        SysSettingDto sysSettingDto = redisComponent.getSysSettingDto();
        //判断文件大小
        if (fileDto.getFileSize() > sysSettingDto.getVideoSize() * Constants.MB_SIZE) {
            throw new BusinessException("文件超过最大文件限制");
        }
        //判断分片
        if ((chunkIndex - 1) > fileDto.getChunkIndex() || chunkIndex > fileDto.getChunks() - 1) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        String folder = appConfig.getProjectFolder() + Constants.FILE_FOLDER + Constants.FILE_FOLDER_TEMP + fileDto.getFilePath();
        File targetFile = new File(folder + "/" + chunkIndex);
        chunkFile.transferTo(targetFile);
        //记录文件上传的分片数
        fileDto.setChunkIndex(chunkIndex);
        fileDto.setFileSize(fileDto.getFileSize() + chunkFile.getSize());
        redisComponent.updateVideoFileInfo(tokenUserInfoDto.getUserId(), fileDto);
        return getSuccessResponseVO(null);
    }




}
