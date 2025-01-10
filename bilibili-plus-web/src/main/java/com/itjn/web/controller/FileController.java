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
import com.itjn.entity.po.VideoInfoFilePost;
import com.itjn.entity.vo.ResponseVO;
import com.itjn.exception.BusinessException;
import com.itjn.service.VideoInfoFilePostService;
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
    private VideoInfoFilePostService videoInfoFilePostService;

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
     * 上传视频文件：上传分片
     * @param chunkFile
     * @param chunkIndex
     * @param uploadId
     * @return
     * @throws IOException
     */
    @RequestMapping("/uploadVideo")
    //@GlobalInterceptor(checkLogin = true)
    public ResponseVO uploadVideo(@NotNull MultipartFile chunkFile, @NotNull Integer chunkIndex,
                                  @NotEmpty String uploadId) throws IOException {
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
        //判断分片：
        //1.判断上传的分片是不是按照顺序一块一块来的：0->1->2->3->...
        //2.分片序号不能大于 分片总数 - 1
        if ((chunkIndex - 1) > fileDto.getChunkIndex() || chunkIndex > fileDto.getChunks() - 1) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        //保存分片文件到本地临时目录
        String folder = appConfig.getProjectFolder() + Constants.FILE_FOLDER + Constants.FILE_FOLDER_TEMP + fileDto.getFilePath();
        //将前端上传过来的临时文件(分片文件) 转存到 目标文件(本地临时文件)
        chunkFile.transferTo(new File(folder + "/" + chunkIndex));

        //记录文件上传的分片数
        fileDto.setChunkIndex(chunkIndex);
        //更新文件上传的大小
        fileDto.setFileSize(fileDto.getFileSize() + chunkFile.getSize());
        //更新redis中的上传文件信息
        redisComponent.updateVideoFileInfo(tokenUserInfoDto.getUserId(), fileDto);
        return getSuccessResponseVO(null);
    }

    /**
     * 删除上传的视频文件(此时视频作品还未发布，只是在服务端的临时文件目录处删除)
     * @param uploadId
     * @return
     * @throws IOException
     */
    @RequestMapping("/delUploadVideo")
    public ResponseVO delUploadVideo(@NotEmpty String uploadId) throws IOException {
        //获取当前用户信息
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        UploadingFileDto fileDto = redisComponent.getUploadingVideoFile(tokenUserInfoDto.getUserId(), uploadId);
        if (fileDto == null) {
            throw new BusinessException("文件不存在请重新上传");
        }
        //删除redis中的上传文件信息
        redisComponent.delVideoFileInfo(tokenUserInfoDto.getUserId(), uploadId);
        //删除本地临时文件
        FileUtils.deleteDirectory(new File(appConfig.getProjectFolder() + Constants.FILE_FOLDER +
                Constants.FILE_FOLDER_TEMP + fileDto.getFilePath()));
        return getSuccessResponseVO(uploadId);
    }

    /**
     * 上传图片(上传视频作品的封面)
     * @param file
     * @param createThumbnail
     * @return
     * @throws IOException
     */
    @RequestMapping("/uploadImage")
    //@GlobalInterceptor(checkLogin = true)
    public ResponseVO uploadCover(@NotNull MultipartFile file, @NotNull Boolean createThumbnail) throws IOException {
        String day = DateUtil.format(new Date(), DateTimePatternEnum.YYYYMMDD.getPattern());
        String folder = appConfig.getProjectFolder() + Constants.FILE_FOLDER + Constants.FILE_COVER + day;
        File folderFile = new File(folder);
        if (!folderFile.exists()) {
            folderFile.mkdirs();
        }
        String fileName = file.getOriginalFilename();
        String fileSuffix = fileName.substring(fileName.lastIndexOf("."));
        String realFileName = StringTools.getRandomString(Constants.LENGTH_30) + fileSuffix;
        String filePath = folder + "/" + realFileName;
        file.transferTo(new File(filePath));
        if (createThumbnail) {
            //生成缩略图
            fFmpegUtils.createImageThumbnail(filePath);
        }
        return getSuccessResponseVO(Constants.FILE_COVER + day + "/" + realFileName);
    }

    /**
     * 获取视频文件资源(以流的形式将视频文件的.m3u8索引文件返回)
     * @param response
     * @param fileId
     */
    @RequestMapping("/videoResource/{fileId}")
    public void getVideoResource(HttpServletResponse response, @PathVariable @NotEmpty String fileId) {
        VideoInfoFile videoInfoFile = videoInfoFileService.getVideoInfoFileByFileId(fileId);
        String filePath = videoInfoFile.getFilePath();
        //读取视频文件的.m3u8索引文件
        readFile(response, filePath + "/" + Constants.M3U8_NAME);

        //更新视频的阅读记录  播放次数
        VideoPlayInfoDto videoPlayInfoDto = new VideoPlayInfoDto();
        videoPlayInfoDto.setVideoId(videoInfoFile.getVideoId());
        videoPlayInfoDto.setFileIndex(videoInfoFile.getFileIndex());

        //根据cookie获取用户token信息
        //因为这个播放视频接口前端是"播放器"发起的请求，我们无法自己在请求头设置token，只能从cookie里拿token
        TokenUserInfoDto tokenUserInfoDto = getTokenInfoFromCookie();
        if (tokenUserInfoDto != null) {
            videoPlayInfoDto.setUserId(tokenUserInfoDto.getUserId());
        }

        //添加分p视频的播放数据到redis消息队列
        redisComponent.addVideoPlay(videoPlayInfoDto);
    }

    /**
     * 获取视频文件资源(以流的形式将ts切片文件返回)
     */
    @RequestMapping("/videoResource/{fileId}/{ts}")
    public void getVideoResourceTs(HttpServletResponse response, @PathVariable @NotEmpty String fileId,
                                   @PathVariable @NotNull String ts) {
        VideoInfoFile videoInfoFile = videoInfoFileService.getVideoInfoFileByFileId(fileId);
        String filePath = videoInfoFile.getFilePath();
        readFile(response, filePath + "/" + ts);
    }


}
