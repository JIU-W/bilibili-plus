package com.itjn.web.task;

import com.itjn.component.EsSearchComponent;
import com.itjn.component.RedisComponent;
import com.itjn.entity.constants.Constants;
import com.itjn.entity.dto.VideoPlayInfoDto;
import com.itjn.entity.enums.SearchOrderTypeEnum;
import com.itjn.entity.po.VideoInfoFilePost;
import com.itjn.redis.RedisUtils;
import com.itjn.service.VideoInfoPostService;
import com.itjn.service.VideoInfoService;
import com.itjn.service.VideoPlayHistoryService;
import com.itjn.utils.StringTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@Slf4j
public class ExecuteQueueTask {

    //线程池
    private ExecutorService executorService = Executors.newFixedThreadPool(Constants.LENGTH_10);

    @Resource
    private VideoInfoPostService videoInfoPostService;

    @Resource
    private RedisUtils redisUtils;


    @Resource
    private VideoInfoService videoInfoService;

    @Resource
    private VideoPlayHistoryService videoPlayHistoryService;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private EsSearchComponent esSearchComponent;

    /**
     * 监听转码文件队列(因为redis本身并没有MQ那种自带的监听机制，所以这里只能模仿实现Mq的监听机制)
     * 还有一个就是redis没有MQ那些高可用方案，所以这只是个简易版的实现。
     */
    @PostConstruct       //项目启动时执行该方法
    public void consumeTransferFileQueue() {
        //单独开启一个线程以保证不会阻塞启动项目的主线程，即保证异步执行。
        executorService.execute(() -> {
            while (true) {
                try {
                    VideoInfoFilePost videoInfoFile =
                            (VideoInfoFilePost) redisUtils.rpop(Constants.REDIS_KEY_QUEUE_TRANSFER);
                    if (videoInfoFile == null) {
                        Thread.sleep(1500);
                        continue;
                    }
                    //转码视频文件
                    videoInfoPostService.transferVideoFile(videoInfoFile);
                } catch (Exception e) {
                    log.error("获取转码文件队列信息失败", e);
                }
            }
        });
    }

    /**
     * 监听要删除的视频文件队列,要删除的视频文件都是编辑之前发布过投稿时删除的一些分p视频，
     * 这些视频最后要在视频存储的最终目录被删除。但是删除这些视频有一个前提条件：就是用户编辑后发布的投稿要被再次审核通过才行，
     * 因为如果用户编辑后重新投稿的审核没有通过的话，我们要保证用户这个作品之前的原状的，
     * 不能又不让他审核通过 又把他之前发布成功的视频删除掉。
     */
    /**
     * 所以最好的做法就是再次编辑投稿发布且审核通过的，我们要把这些作品对应的删除了的分p视频文件信息放到另一个消息队列里去，
     * 那样的话那个消息队列的数据就可以无所顾忌的被监听然后删除了。
     *
     * 不过这个项目本身并没有再添加到一个新队列去异步，而是审核通过后直接去同步删去，同步去删除可能会导致请求时间过长导致超时。
     * 不过我们限制了分p视频数量就还好，就采用同步删除了。
     */
    /*@PostConstruct
    public void consumeVideoPlayQueue() {
        executorService.execute(() -> {
            while (true) {
                try {
                    VideoPlayInfoDto videoPlayInfoDto = (VideoPlayInfoDto) redisUtils.rpop(Constants.REDIS_KEY_QUEUE_VIDEO_PLAY);
                    if (videoPlayInfoDto == null) {
                        Thread.sleep(1500);
                        continue;
                    }
                    //更新播放数
                    videoInfoService.addReadCount(videoPlayInfoDto.getVideoId());
                    if (!StringTools.isEmpty(videoPlayInfoDto.getUserId())) {
                        //记录历史
                        videoPlayHistoryService.saveHistory(videoPlayInfoDto.getUserId(), videoPlayInfoDto.getVideoId(), videoPlayInfoDto.getFileIndex());
                    }
                    //按天记录播放数
                    redisComponent.recordVideoPlayCount(videoPlayInfoDto.getVideoId());


                    //更新es播放数量
                    //esSearchComponent.updateDocCount(videoPlayInfoDto.getVideoId(), SearchOrderTypeEnum.VIDEO_PLAY.getField(), 1);

                } catch (Exception e) {
                    log.error("获取视频播放文件队列信息失败", e);
                }
            }
        });
    }*/

}
