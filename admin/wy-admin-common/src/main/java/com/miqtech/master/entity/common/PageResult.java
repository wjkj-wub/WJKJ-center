package com.miqtech.master.entity.common;

import org.springframework.data.domain.Page;

public class PageResult<T> {
	private Page<T> page;
	private int currentPage;

	public Page<T> getPage() {
		return page;
	}

	public void setPage(Page<T> page) {
		this.page = page;
	}

	public int getCurrentPage() {
		return currentPage;
	}

	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}

}
