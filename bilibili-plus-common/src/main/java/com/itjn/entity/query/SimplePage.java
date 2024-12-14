package com.itjn.entity.query;

import com.itjn.entity.enums.PageSize;

/**
 * 分页对象
 */
public class SimplePage {

	//当前页码
	private int pageNo;

	//总记录数
	private int countTotal;

	//每页记录数
	private int pageSize;

	//总页数
	private int pageTotal;

	//指定从结果集的哪一行开始
	private int start;

	//返回多少行数据(每页显示的记录数)(等价于pageSize)
	private int end;

	public SimplePage() {
	}

	public SimplePage(Integer pageNo, int countTotal, int pageSize) {
		if (null == pageNo) {
			pageNo = 0;
		}
		this.pageNo = pageNo;//当前页码
		this.countTotal = countTotal;//总记录数
		this.pageSize = pageSize;//
		//根据当前页码、总记录数、每页记录数计算出了start和end和pageTotal
		//start和end:(这两者用于数据库分页时的SQL语句)
		action();
	}

	public SimplePage(int start, int end) {
		this.start = start;
		this.end = end;
	}

	/**
	 * 根据当前页码、总记录数、每页记录数计算出了start和end和pageTotal
	 * start和end:(这两者用于数据库分页时的SQL语句)
	 */
	public void action() {
		if (this.pageSize <= 0) {
			this.pageSize = PageSize.SIZE20.getSize();
		}
		if (this.countTotal > 0) {
			this.pageTotal = this.countTotal % this.pageSize == 0 ? this.countTotal / this.pageSize
					: this.countTotal / this.pageSize + 1;
		} else {
			pageTotal = 1;
		}

		if (pageNo <= 1) {
			pageNo = 1;
		}
		if (pageNo > pageTotal) {
			pageNo = pageTotal;
		}
		this.start = (pageNo - 1) * pageSize;
		this.end = this.pageSize;
	}

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}

	public int getPageTotal() {
		return pageTotal;
	}

	public int getPageNo() {
		return pageNo;
	}

	public void setPageNo(int pageNo) {
		this.pageNo = pageNo;
	}

	public void setPageTotal(int pageTotal) {
		this.pageTotal = pageTotal;
	}

	public int getCountTotal() {
		return countTotal;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public void setCountTotal(int countTotal) {
		this.countTotal = countTotal;
		this.action();
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

}
