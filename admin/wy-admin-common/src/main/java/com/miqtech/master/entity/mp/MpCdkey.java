package com.miqtech.master.entity.mp;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.entity.IdEntity;

/**
 * 微信账户cdkey
 */
@Entity
@Table(name = "mp_cdkey")
public class MpCdkey extends IdEntity {

	private static final long serialVersionUID = 2847993060039548434L;

	public MpCdkey() {
		super();
	}

	public MpCdkey(String openId, Long categoryId, String cdkey) {
		setCategoryId(categoryId);
		setOpenId(openId);
		setCdkey(cdkey);
		setValid(CommonConstant.INT_BOOLEAN_TRUE);
		Date now = new Date();
		setUpdateDate(now);
		setCreateDate(now);
	}

	private Long categoryId;// cdkey类型:0-网娱cdkey,非0-指向third_party_cdkey_category.id
	private String openId;
	private String cdkey;

	@Column(name = "open_id")
	public String getOpenId() {
		return openId;
	}

	public void setOpenId(String openId) {
		this.openId = openId;
	}

	@Column(name = "cdkey")
	public String getCdkey() {
		return cdkey;
	}

	public void setCdkey(String cdkey) {
		this.cdkey = cdkey;
	}

	@Column(name = "category_id")
	public Long getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(Long categoryId) {
		this.categoryId = categoryId;
	}

}
