package com.miqtech.master.entity;

import com.miqtech.master.utils.PageUtils;

public class Pager {
	public Integer page;
	public Integer pageSize;
	public Integer start;
	public Integer total;

	public Pager(Integer page, Integer pageSize) {
		if (page == null || page <= 0) {
			this.page = 1;
		} else {
			this.page = page;
		}
		if (pageSize == null || pageSize <= 0) {
			this.pageSize = PageUtils.API_DEFAULT_PAGE_SIZE;
		} else {
			this.pageSize = pageSize;
		}
		this.start = (this.page - 1) * this.pageSize;
		this.total = this.page * this.pageSize;
	}

}
