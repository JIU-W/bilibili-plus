package com.itjn.entity.vo;

import com.itjn.entity.po.UserVideoSeries;
import com.itjn.entity.po.UserVideoSeriesVideo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserVideoSeriesDetailVO {

    //UserVideoSeries合集对象
    private UserVideoSeries videoSeries;

    //UserVideoSeriesVideo集合数据(带有视频信息)
    private List<UserVideoSeriesVideo> seriesVideoList;

}
