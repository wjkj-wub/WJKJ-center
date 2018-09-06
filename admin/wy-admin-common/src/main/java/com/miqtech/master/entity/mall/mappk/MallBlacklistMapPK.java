package com.miqtech.master.entity.mall.mappk;

import java.io.Serializable;
import java.util.Date;

public class MallBlacklistMapPK implements Serializable {

	private static final long serialVersionUID = -3191702162348206737L;

	private Long userId;
	private Date createDate;

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
}
