package com.itjn.entity.query;

import lombok.Data;

//分页基础参数
@Data
public class BaseParam {

	//分页对象(后端创建)
	private SimplePage simplePage;

	//当前页码(前端传的)
	private Integer pageNo;

	//每页记录数(前端传的)
	private Integer pageSize;

	//排序字段(后端设置或者前端传)
	private String orderBy;

}
