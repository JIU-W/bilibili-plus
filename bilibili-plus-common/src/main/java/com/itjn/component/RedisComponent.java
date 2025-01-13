package com.itjn.component;

import com.itjn.entity.config.AppConfig;
import com.itjn.entity.constants.Constants;
import com.itjn.entity.dto.SysSettingDto;
import com.itjn.entity.dto.TokenUserInfoDto;
import com.itjn.entity.dto.UploadingFileDto;
import com.itjn.entity.dto.VideoPlayInfoDto;
import com.itjn.entity.enums.DateTimePatternEnum;
import com.itjn.entity.po.CategoryInfo;
import com.itjn.entity.po.VideoInfoFilePost;
import com.itjn.redis.RedisUtils;
import com.itjn.utils.DateUtil;
import com.itjn.utils.StringTools;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.util.*;

@Component
public class RedisComponent {
    @Resource
    private RedisUtils redisUtils;

    @Resource
    private AppConfig appConfig;

    public String saveCheckCode(String code) {
        //生成每个图片验证码对应的唯一的key
        String checkCodeKey = UUID.randomUUID().toString();
        redisUtils.setex(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey, code,
                Constants.REDIS_KEY_EXPIRES_ONE_MIN * 10);//过期时间给10分钟
        return checkCodeKey;
    }

    public void cleanCheckCode(String checkCodeKey) {
        redisUtils.delete(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey);
    }

    public String getCheckCode(String checkCodeKey) {
        return (String) redisUtils.get(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey);
    }

    /**
     * 将登录用户的用户信息以及token信息保存到redis中
     * @param tokenUserInfoDto
     */
    public void saveTokenInfo(TokenUserInfoDto tokenUserInfoDto) {
        String token = UUID.randomUUID().toString();                //token过期时间设置为7天
        tokenUserInfoDto.setExpireAt(System.currentTimeMillis() + Constants.REDIS_KEY_EXPIRES_DAY * 7);
        tokenUserInfoDto.setToken(token);
        redisUtils.setex(Constants.REDIS_KEY_TOKEN_WEB + token, tokenUserInfoDto, Constants.REDIS_KEY_EXPIRES_DAY * 7);
    }

    public void cleanToken(String token) {
        redisUtils.delete(Constants.REDIS_KEY_TOKEN_WEB + token);
    }

    public TokenUserInfoDto getTokenInfo(String token) {
        return (TokenUserInfoDto) redisUtils.get(Constants.REDIS_KEY_TOKEN_WEB + token);
    }

    public void updateTokenInfo(TokenUserInfoDto tokenUserInfoDto) {
        redisUtils.setex(Constants.REDIS_KEY_TOKEN_WEB + tokenUserInfoDto.getToken(), tokenUserInfoDto, Constants.REDIS_KEY_EXPIRES_DAY * 7);
    }

    public String saveTokenInfo4Admin(String account) {
        String token = UUID.randomUUID().toString();
        redisUtils.setex(Constants.REDIS_KEY_TOKEN_ADMIN + token, account, Constants.REDIS_KEY_EXPIRES_DAY);
        return token;
    }

    public void cleanToken4Admin(String token) {
        redisUtils.delete(Constants.REDIS_KEY_TOKEN_ADMIN + token);
    }

    public String getLoginInfo4Admin(String token) {
        return (String) redisUtils.get(Constants.REDIS_KEY_TOKEN_ADMIN + token);
    }

    public void cleanWebTokenInfo(TokenUserInfoDto tokenUserInfoDto) {
        if (tokenUserInfoDto != null) {
            redisUtils.delete(Constants.REDIS_KEY_TOKEN_ADMIN + tokenUserInfoDto.getToken());
        }
    }

    public void cleanAdminTokenInfo(String token) {
        if (!StringTools.isEmpty(token)) {
            redisUtils.delete(Constants.REDIS_KEY_TOKEN_ADMIN + token);
        }
    }

    /**
     * 保存上传文件时的临时信息到redis
     * @param fileName
     * @param chunks
     * @return
     */
    public String savePreVideoFileInfo(String userId, String fileName, Integer chunks) {
        //生成每个上传文件的唯一标识
        String uploadId = StringTools.getRandomString(Constants.LENGTH_15);
        //保存上传文件信息
        UploadingFileDto fileDto = new UploadingFileDto();
        fileDto.setChunks(chunks);//分片总数
        fileDto.setFileName(fileName);//文件名
        fileDto.setUploadId(uploadId);//上传文件标识
        fileDto.setChunkIndex(0);//当前上传分片

        String day = DateUtil.format(new Date(), DateTimePatternEnum.YYYYMMDD.getPattern());
        String filePath = day + "/" + userId + uploadId;
        //上传文件到的临时目录
        String folder = appConfig.getProjectFolder() + Constants.FILE_FOLDER + Constants.FILE_FOLDER_TEMP + filePath;
        File folderFile = new File(folder);
        if (!folderFile.exists()) {
            folderFile.mkdirs();
        }
        fileDto.setFilePath(filePath);//文件路径
        //保存上传文件时的临时信息到redis
        redisUtils.setex(Constants.REDIS_KEY_UPLOADING_FILE + userId + uploadId, fileDto,
                Constants.REDIS_KEY_EXPIRES_DAY);
        return uploadId;
    }

    public void updateVideoFileInfo(String userId, UploadingFileDto fileDto) {
        redisUtils.setex(Constants.REDIS_KEY_UPLOADING_FILE + userId + fileDto.getUploadId(), fileDto,
                Constants.REDIS_KEY_EXPIRES_DAY);
    }

    //获取上传文件时的临时信息
    public UploadingFileDto getUploadingVideoFile(String userId, String uploadId) {
        return (UploadingFileDto) redisUtils.get(Constants.REDIS_KEY_UPLOADING_FILE + userId + uploadId);
    }

    public void delVideoFileInfo(String userId, String uploadId) {
        redisUtils.delete(Constants.REDIS_KEY_UPLOADING_FILE + userId + uploadId);
    }

    /**
     * 获取系统设置
     * @return
     */
    public SysSettingDto getSysSettingDto() {
        SysSettingDto sysSettingDto = (SysSettingDto) redisUtils.get(Constants.REDIS_KEY_SYS_SETTING);
        if (sysSettingDto == null) {
            //取默认值
            sysSettingDto = new SysSettingDto();
        }
        return sysSettingDto;
    }

    /**
     * 保存系统设置
     * @param sysSettingDto
     */
    public void saveSettingDto(SysSettingDto sysSettingDto) {
        redisUtils.set(Constants.REDIS_KEY_SYS_SETTING, sysSettingDto);
    }

    /**
     * 添加待转码文件到消息队列
     * @param fileList
     */
    public void addFile2TransferQueue(List<VideoInfoFilePost> fileList) {
        redisUtils.lpushAll(Constants.REDIS_KEY_QUEUE_TRANSFER, fileList, 0);
    }

    /**
     * 添加待删除文件到消息队列
     * @param videoId
     * @param filePathList
     */
    public void addFile2DelQueue(String videoId, List<String> filePathList) {
        redisUtils.lpushAll(Constants.REDIS_KEY_FILE_DEL + videoId, filePathList,
                Constants.REDIS_KEY_EXPIRES_DAY * 7);
    }

    /**
     * 从消息队列中获取待删除文件列表
     * @param videoId
     * @return
     */
    public List<String> getDelFileList(String videoId) {
        List<String> filePathList = redisUtils.getQueueList(Constants.REDIS_KEY_FILE_DEL + videoId);
        return filePathList;
    }

    public void cleanDelFileList(String videoId) {
        redisUtils.delete(Constants.REDIS_KEY_FILE_DEL + videoId);
    }

    public void saveCategoryList(List<CategoryInfo> categoryInfoList) {
        redisUtils.set(Constants.REDIS_KEY_CATEGORY_LIST, categoryInfoList);
    }

    public List<CategoryInfo> getCategoryList() {
        List<CategoryInfo> categoryInfoList = (List<CategoryInfo>) redisUtils.get(Constants.REDIS_KEY_CATEGORY_LIST);
        return categoryInfoList == null ? new ArrayList<>() : categoryInfoList;
    }

    /**
     * 统计在线人数
     * @param fileId
     * @param deviceId
     * @return
     */
    public Integer reportVideoPlayOnline(String fileId, String deviceId) {
        //播放用户标识
        String userPlayOnlineKey = String.format(Constants.REDIS_KEY_VIDEO_PLAY_COUNT_USER, fileId, deviceId);
        //在线人数标识
        String playOnlineCountKey = String.format(Constants.REDIS_KEY_VIDEO_PLAY_COUNT_ONLINE, fileId);

        //判断是否已经播放
        if (!redisUtils.keyExists(userPlayOnlineKey)) {
            //这个userPlayOnlineKey在redis中不存在，说明是第一次播放。
            //设置失效时间为8秒
            redisUtils.setex(userPlayOnlineKey, fileId, Constants.REDIS_KEY_EXPIRES_ONE_SECONDS * 8);
            //在线人数+1，并且设置过期时间为10秒
            return redisUtils.incrementex(playOnlineCountKey, Constants.REDIS_KEY_EXPIRES_ONE_SECONDS * 10).intValue();
        }
        //正在播放，不是第一次播放
        //给视频在线总数量续期
        redisUtils.expire(playOnlineCountKey, Constants.REDIS_KEY_EXPIRES_ONE_SECONDS * 10);
        //给播放用户续期，续上8秒
        redisUtils.expire(userPlayOnlineKey, Constants.REDIS_KEY_EXPIRES_ONE_SECONDS * 8);
        Integer count = (Integer) redisUtils.get(playOnlineCountKey);
        return count == null ? 1 : count;
    }

    /**
     * 减少数量
     * @param key
     */
    public void decrementPlayOnlineCount(String key) {
        redisUtils.decrement(key);
    }

    /**
     * 往Redis消息队列中添加视频播放信息
     * @param videoPlayInfoDto
     */
    public void addVideoPlay(VideoPlayInfoDto videoPlayInfoDto) {
        redisUtils.lpush(Constants.REDIS_KEY_QUEUE_VIDEO_PLAY, videoPlayInfoDto, null);
    }

    /**
     * 按天统计视频播放次数
     * @param videoId
     */
    public void recordVideoPlayCount(String videoId) {
        //统计日期
        String date = DateUtil.format(new Date(), DateTimePatternEnum.YYYY_MM_DD.getPattern());
        redisUtils.incrementex(Constants.REDIS_KEY_VIDEO_PLAY_COUNT + date + ":" + videoId,
                Constants.REDIS_KEY_EXPIRES_DAY * 2L);//失效时间为2天
    }

    /**
     * 批量获取视频播放量
     * @param date
     * @return
     */
    public Map<String, Integer> getVideoPlayCount(String date) {
        Map<String, Integer> videoPlayMap = redisUtils.getBatch(Constants.REDIS_KEY_VIDEO_PLAY_COUNT + date);
        return videoPlayMap;
    }

    public void delVideoPlayCount(List<String> keys) {
        redisUtils.delete(keys.toArray(new String[keys.size()]));
    }

    /**
     * 给搜索关键字(搜索热词)的得分加一
     * @param keyword
     */
    public void addKeywordCount(String keyword) {
        redisUtils.zaddCount(Constants.REDIS_KEY_VIDEO_SEARCH_COUNT, keyword);
    }

    /**
     * 获取搜索热词
     * @param top
     * @return
     */
    public List<String> getKeywordTop(Integer top) {
        return redisUtils.getZSetList(Constants.REDIS_KEY_VIDEO_SEARCH_COUNT, top - 1);
    }

}
