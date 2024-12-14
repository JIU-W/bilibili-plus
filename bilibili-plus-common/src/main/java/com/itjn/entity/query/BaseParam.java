package com.itjn.entity.query;

//分页基础参数
public class BaseParam {

	//分页对象(后端创建)
	private SimplePage simplePage;

	//当前页码(前端传的)
	private Integer pageNo;

	//每页记录数(前端传的)
	private Integer pageSize;

	//排序字段(前端传的)
	private String orderBy;

	public SimplePage getSimplePage() {
		return simplePage;
	}

	public void setSimplePage(SimplePage simplePage) {
		this.simplePage = simplePage;
	}

	public Integer getPageNo() {
		return pageNo;
	}

	public void setPageNo(Integer pageNo) {
		this.pageNo = pageNo;
	}

	public Integer getPageSize() {
		return pageSize;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}

	public void setOrderBy(String orderBy){
		this.orderBy = orderBy;
	}

	public String getOrderBy(){
		return this.orderBy;
	}
}
