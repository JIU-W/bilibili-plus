package com.itjn.entity.enums;

/**
 * 分页大小，每页显示条数也就是每页记录数
 */
public enum PageSize {
	SIZE10(10),SIZE15(15), SIZE20(20), SIZE30(30), SIZE40(40), SIZE50(50);
	int size;

	private PageSize(int size) {
		this.size = size;
	}

	public int getSize() {
		return this.size;
	}
}
