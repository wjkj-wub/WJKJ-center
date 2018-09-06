package com.miqtech.master.entity.thirdparty;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "third_party_cdkey")
public class ThirdPartyCdkey extends IdEntity {

	private static final long serialVersionUID = -5482476738248411535L;

	private Long categoryId;
	private String cdkey;
	private Integer isUsed;

	@Column(name = "category_id")
	public Long getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(Long categoryId) {
		this.categoryId = categoryId;
	}

	@Column(name = "cdkey")
	public String getCdkey() {
		return cdkey;
	}

	public void setCdkey(String cdkey) {
		this.cdkey = cdkey;
	}

	@Column(name = "is_used")
	public Integer getIsUsed() {
		return isUsed;
	}

	public void setIsUsed(Integer isUsed) {
		this.isUsed = isUsed;
	}
}
