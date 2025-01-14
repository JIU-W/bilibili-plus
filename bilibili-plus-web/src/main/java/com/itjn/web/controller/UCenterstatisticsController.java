package com.itjn.web.controller;

import com.itjn.entity.dto.TokenUserInfoDto;
import com.itjn.entity.po.StatisticsInfo;
import com.itjn.entity.query.StatisticsInfoQuery;
import com.itjn.entity.vo.ResponseVO;
import com.itjn.service.StatisticsInfoService;
import com.itjn.utils.DateUtil;
import com.itjn.web.annotation.GlobalInterceptor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@Validated
@RequestMapping("/ucenter")
public class UCenterstatisticsController extends ABaseController {

    @Resource
    private StatisticsInfoService statisticsInfoService;

    /**
     * 获取当前用户前一天的统计数据 以及 获取当前用户所有的数据信息
     * @return
     */
    @RequestMapping("/getActualTimeStatisticsInfo")
    @GlobalInterceptor
    public ResponseVO getActualTimeStatisticsInfo() {
        //获取前一天的日期时间
        String preDate = DateUtil.getBeforeDayDate(1);
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();

        StatisticsInfoQuery param = new StatisticsInfoQuery();
        param.setStatisticsDate(preDate);
        param.setUserId(tokenUserInfoDto.getUserId());

        //获取当前用户前一天的统计数据
        List<StatisticsInfo> preDayData = statisticsInfoService.findListByParam(param);
        Map<Integer, Integer> preDayDataMap = preDayData.stream().collect(
                Collectors.toMap(StatisticsInfo::getDataType, StatisticsInfo::getStatisticsCount,
                        (item1, item2) -> item2));
        //(item1, item2) -> item2)作用：键(key)冲突时，选择使用当前值(item2)覆盖已存在的值(item1)

        //获取当前用户所有的数据信息：播放量、点赞量、收藏量、评论量、投币量、弹幕数、粉丝数
        Map<String, Integer> totalCountInfo = statisticsInfoService.getStatisticsInfoActualTime(tokenUserInfoDto.getUserId());
        //封装返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("preDayData", preDayDataMap);
        result.put("totalCountInfo", totalCountInfo);
        return getSuccessResponseVO(result);
    }

    /**
     * 根据"数据统计类型"获取用户一周的统计数据
     * @param dataType
     * @return
     */
    @RequestMapping("/getWeekStatisticsInfo")
    @GlobalInterceptor
    public ResponseVO getWeekStatisticsInfo(Integer dataType) {
        //获取一周之内的日期时间集合
        List<String> dateList = DateUtil.getBeforeDates(7);
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();

        StatisticsInfoQuery param = new StatisticsInfoQuery();
        param.setDataType(dataType);//统计数据类型
        param.setUserId(tokenUserInfoDto.getUserId());//用户id
        param.setStatisticsDateStart(dateList.get(0));//统计开始时间
        param.setStatisticsDateEnd(dateList.get(dateList.size() - 1));//统计结束时间
        param.setOrderBy("statistics_date asc");
        //获取一周之内的统计数据
        List<StatisticsInfo> statisticsInfoList = statisticsInfoService.findListByParam(param);
        //转成map  (格式：key = 日期，value = 统计数据)
        Map<String, StatisticsInfo> dataMap = statisticsInfoList.stream().collect(
                Collectors.toMap(item -> item.getStatisticsDate(), Function.identity(), (data1, data2) -> data2));

        List<StatisticsInfo> resultDataList = new ArrayList<>();
        for (String date : dateList) {
            StatisticsInfo dataItem = dataMap.get(date);
            //如果没有数据，则设置默认值
            if (dataItem == null) {
                dataItem = new StatisticsInfo();
                dataItem.setStatisticsCount(0);
                dataItem.setStatisticsDate(date);
                dataItem.setDataType(dataType);
                dataItem.setUserId(tokenUserInfoDto.getUserId());
            }
            resultDataList.add(dataItem);
        }
        return getSuccessResponseVO(resultDataList);
    }

    /**
     * 测试时使用：用于测试定时任务的业务逻辑
     */
    @RequestMapping("/testTask")
    public void testTask(){
        statisticsInfoService.statisticsData();
    }


}
