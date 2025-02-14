package com.itjn.web.task;

import com.itjn.entity.config.AppConfig;
import com.itjn.entity.constants.Constants;
import com.itjn.entity.enums.DateTimePatternEnum;
import com.itjn.service.StatisticsInfoService;
import com.itjn.utils.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;

@Component
@Slf4j
public class SysTask {

    @Resource
    private StatisticsInfoService statisticsInfoService;

    @Resource
    private AppConfig appConfig;

    /**
     * 每天0点执行一次：统计数据(...)  TODO 有点小问题，到凌晨0点这个方法没有执行，数据没有统计
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void statisticsData() {
        log.info("开始统计数据");
        statisticsInfoService.statisticsData();
        log.info("统计数据结束");
    }

    @Scheduled(cron = "0 */1 * * * ?")//0 0 3 * * ?
    public void delTempFile() {
        String tempFolderName = appConfig.getProjectFolder() + Constants.FILE_FOLDER + Constants.FILE_FOLDER_TEMP;
        File folder = new File(tempFolderName);
        File[] listFile = folder.listFiles();
        if (listFile == null) {
            return;
        }
        String twodaysAgo = DateUtil.format(DateUtil.getDayAgo(2), DateTimePatternEnum.YYYYMMDD.getPattern()).toLowerCase();
        Integer dayInt = Integer.parseInt(twodaysAgo);
        for (File file : listFile) {
            Integer fileDate = Integer.parseInt(file.getName());
            if (fileDate <= dayInt) {
                try {
                    FileUtils.deleteDirectory(file);
                } catch (IOException e) {
                    log.info("删除临时文件失败", e);
                }
            }
        }
    }
}
