package com.itjn.service;

import com.itjn.entity.po.StatisticsInfo;
import com.itjn.entity.query.StatisticsInfoQuery;
import com.itjn.entity.vo.PaginationResultVO;

import java.util.List;
import java.util.Map;


/**
 * 数据统计 业务接口
 */
public interface StatisticsInfoService {

    /**
     * 根据条件查询列表
     */
    List<StatisticsInfo> findListByParam(StatisticsInfoQuery param);

    /**
     * 根据条件查询列表
     */
    Integer findCountByParam(StatisticsInfoQuery param);

    /**
     * 分页查询
     */
    PaginationResultVO<StatisticsInfo> findListByPage(StatisticsInfoQuery param);

    /**
     * 新增
     */
    Integer add(StatisticsInfo bean);

    /**
     * 批量新增
     */
    Integer addBatch(List<StatisticsInfo> listBean);

    /**
     * 批量新增/修改
     */
    Integer addOrUpdateBatch(List<StatisticsInfo> listBean);

    /**
     * 多条件更新
     */
    Integer updateByParam(StatisticsInfo bean, StatisticsInfoQuery param);

    /**
     * 多条件删除
     */
    Integer deleteByParam(StatisticsInfoQuery param);

    /**
     * 根据StatisticsDateAndUserIdAndDataType查询对象
     */
    StatisticsInfo getStatisticsInfoByStatisticsDateAndUserIdAndDataType(String statisticsDate, String userId, Integer dataType);


    /**
     * 根据StatisticsDateAndUserIdAndDataType修改
     */
    Integer updateStatisticsInfoByStatisticsDateAndUserIdAndDataType(StatisticsInfo bean, String statisticsDate, String userId, Integer dataType);


    /**
     * 根据StatisticsDateAndUserIdAndDataType删除
     */
    Integer deleteStatisticsInfoByStatisticsDateAndUserIdAndDataType(String statisticsDate, String userId, Integer dataType);

    /**
     * 统计数据
     */
    void statisticsData();

    /**
     * 1.userId不为null：获取当前用户所有的数据
     * 2.userId为null：获取所有用户所有的数据
     */
    Map<String, Integer> getStatisticsInfoActualTime(String userId);

    /**
     * 分组查询"所有用户""前一天"统计数据的总和(根据统计数据类型分组)
     * @param param
     * @return
     */
    List<StatisticsInfo> findPreDayListTotalInfoByParam(StatisticsInfoQuery param);

    /**
     * 分组查询"所有用户""前一周每一天"统计数据的总和(根据统计数据类型和日期分组)(指定了条件：统计数据类型)
     * @param param
     * @return
     */
    List<StatisticsInfo> findWeekListTotalInfoByParam(StatisticsInfoQuery param);

    /**
     * "按加入时间分组"查询新增的用户数量
     */
    List<StatisticsInfo> findUserCountTotalInfoByParam(StatisticsInfoQuery param);

}
