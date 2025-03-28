package com.itjn.utils;

import com.itjn.entity.config.AppConfig;
import com.itjn.entity.constants.Constants;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.math.BigDecimal;

@Component
public class FFmpegUtils {

    @Resource
    private AppConfig appConfig;


    /**
     * 生成图片缩略图
     * @param filePath
     * @return
     */
    public void createImageThumbnail(String filePath) {
        final String CMD_CREATE_IMAGE_THUMBNAIL = "ffmpeg -i \"%s\" -vf scale=200:-1 \"%s\"";
        String cmd = String.format(CMD_CREATE_IMAGE_THUMBNAIL, filePath, filePath + Constants.IMAGE_THUMBNAIL_SUFFIX);
        //执行命令
        ProcessUtils.executeCommand(cmd, appConfig.getShowFFmpegLog());
    }


    /**
     * 获取视频编码，视频编码为h264才说明是mp4格式，视频编码为其他类型的要将视频转成mp4格式。
     * @param videoFilePath
     * @return
     */
    public String getVideoCodec(String videoFilePath) {
        final String CMD_GET_CODE = "ffprobe -v error -select_streams v:0 -show_entries stream=codec_name \"%s\"";
        //执行命令
        String cmd = String.format(CMD_GET_CODE, videoFilePath);
        String result = ProcessUtils.executeCommand(cmd, appConfig.getShowFFmpegLog());
        //解析结果
        //结果的形状为：
        // [STREAM]
        // codec_name=mpeg4
        // [/STREAM]
        result = result.replace("\n", "");
        result = result.substring(result.indexOf("=") + 1);
        //截取最后要的结果
        String codec = result.substring(0, result.indexOf("["));
        return codec;
    }

    /**
     * 转换视频编码为h264
     * @param newFileName
     * @param videoFilePath
     */
    public void convertHevc2Mp4(String newFileName, String videoFilePath) {
        String CMD_HEVC_264 = "ffmpeg -i %s -c:v libx264 -crf 20 %s";
        String cmd = String.format(CMD_HEVC_264, newFileName, videoFilePath);
        ProcessUtils.executeCommand(cmd, appConfig.getShowFFmpegLog());
    }

    /**
     * 将视频文件转换为ts文件
     * @param tsFolder
     * @param videoFilePath
     */
    public void convertVideo2Ts(File tsFolder, String videoFilePath) {
        //生成ts文件的指令
        final String CMD_TRANSFER_2TS = "ffmpeg -y -i \"%s\"  -vcodec copy -acodec copy -vbsf h264_mp4toannexb \"%s\"";
        //切成多个小ts切片 以及 索引文件.m3u8文件 的指令
        final String CMD_CUT_TS = "ffmpeg -i \"%s\" -c copy -map 0 -f segment -segment_list \"%s\" -segment_time 10 %s/%%4d.ts";
        String tsPath = tsFolder + "/" + Constants.TS_NAME;
        //生成.ts
        String cmd = String.format(CMD_TRANSFER_2TS, videoFilePath, tsPath);
        ProcessUtils.executeCommand(cmd, appConfig.getShowFFmpegLog());
        //生成索引文件.m3u8 和切片.ts
        cmd = String.format(CMD_CUT_TS, tsPath, tsFolder.getPath() + "/" + Constants.M3U8_NAME, tsFolder.getPath());
        ProcessUtils.executeCommand(cmd, appConfig.getShowFFmpegLog());
        //删除index.ts
        new File(tsPath).delete();
    }

    /**
     * 获取视频时长
     * @param completeVideo
     * @return
     */
    public Integer getVideoInfoDuration(String completeVideo) {
        final String CMD_GET_CODE = "ffprobe -v error -show_entries format=duration -of default=noprint_wrappers=1:nokey=1 \"%s\"";
        String cmd = String.format(CMD_GET_CODE, completeVideo);
        String result = ProcessUtils.executeCommand(cmd, appConfig.getShowFFmpegLog());
        if (StringTools.isEmpty(result)) {
            return 0;
        }
        result = result.replace("\n", "");
        return new BigDecimal(result).intValue();
    }

}
