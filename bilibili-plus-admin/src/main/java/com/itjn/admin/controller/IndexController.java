package com.itjn.admin.controller;

import com.itjn.entity.enums.StatisticsTypeEnum;
import com.itjn.entity.po.StatisticsInfo;
import com.itjn.entity.query.StatisticsInfoQuery;
import com.itjn.entity.query.UserInfoQuery;
import com.itjn.entity.vo.ResponseVO;
import com.itjn.service.StatisticsInfoService;
import com.itjn.service.UserInfoService;
import com.itjn.utils.DateUtil;
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
@RequestMapping("/index")
@Validated
public class IndexController extends ABaseController {
    @Resource
    private StatisticsInfoService statisticsInfoService;

    @Resource
    private UserInfoService userInfoService;

    /**
     * 获取所有用户前一天的统计数据的总和 以及 获取所有用户所有的数据信息
     * @return
     */
    @RequestMapping("/getActualTimeStatisticsInfo")
    public ResponseVO getActualTimeStatisticsInfo() {
        //获取前一天的日期时间
        String preDate = DateUtil.getBeforeDayDate(1);

        StatisticsInfoQuery param = new StatisticsInfoQuery();
        //???
        param.setStatisticsDate(preDate);

        //分组查询所有用户统计数据的总和(根据数据类型和日期分组)  ????前一天？？？
        List<StatisticsInfo> preDayData = statisticsInfoService.findListTotalInfoByParam(param);
        //查询"用户总数" ---> 替换掉类型为"粉丝的数量"
        Integer userCount = userInfoService.findCountByParam(new UserInfoQuery());
        preDayData.forEach(item -> {
            if (StatisticsTypeEnum.FANS.getType().equals(item.getDataType())) {
                item.setStatisticsCount(userCount);
            }
        });
        Map<Integer, Integer> preDayDataMap = preDayData.stream().collect(Collectors.toMap(
                StatisticsInfo::getDataType, StatisticsInfo::getStatisticsCount, (item1, item2) -> item2));

        //获取所有用户所有的数据信息
        Map<String, Integer> totalCountInfo = statisticsInfoService.getStatisticsInfoActualTime(null);
        Map<String, Object> result = new HashMap<>();
        result.put("preDayData", preDayDataMap);
        result.put("totalCountInfo", totalCountInfo);
        return getSuccessResponseVO(result);
    }

    @RequestMapping("/getWeekStatisticsInfo")
    public ResponseVO getWeekStatisticsInfo(Integer dataType) {
        List<String> dateList = DateUtil.getBeforeDates(7);

        List<StatisticsInfo> statisticsInfoList = new ArrayList<>();
        StatisticsInfoQuery param = new StatisticsInfoQuery();
        param.setDataType(dataType);
        param.setStatisticsDateStart(dateList.get(0));
        param.setStatisticsDateEnd(dateList.get(dateList.size() - 1));
        param.setOrderBy("statistics_date asc");

        if (!StatisticsTypeEnum.FANS.getType().equals(dataType)) {
            statisticsInfoList = statisticsInfoService.findListTotalInfoByParam(param);
        } else {
            statisticsInfoList = statisticsInfoService.findUserCountTotalInfoByParam(param);
        }

        Map<String, StatisticsInfo> dataMap = statisticsInfoList.stream().collect(Collectors.toMap(item -> item.getStatisticsDate(), Function.identity(), (data1,
                                                                                                                                                           data2) -> data2));
        List<StatisticsInfo> resultDataList = new ArrayList<>();
        for (String date : dateList) {
            StatisticsInfo dataItem = dataMap.get(date);
            if (dataItem == null) {
                dataItem = new StatisticsInfo();
                dataItem.setStatisticsCount(0);
                dataItem.setStatisticsDate(date);
            }
            resultDataList.add(dataItem);
        }
        return getSuccessResponseVO(resultDataList);
    }

}
