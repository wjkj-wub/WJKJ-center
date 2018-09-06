package com.miqtech.master.entity.common;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

/**
 * 首页广告的地区
 */
@Entity
@Table(name = "index_r_advertise_area")
public class IndexAdvertiseArea extends IdEntity {
	private static final long serialVersionUID = -1967812325313053366L;

	private Long advertiseId;// 广告ID
	private String areaCode;// 地区code

	@Column(name = "advertise_id")
	public Long getAdvertiseId() {
		return advertiseId;
	}

	public void setAdvertiseId(Long advertiseId) {
		this.advertiseId = advertiseId;
	}

	@Column(name = "area_code")
	public String getAreaCode() {
		return areaCode;
	}

	public void setAreaCode(String areaCode) {
		this.areaCode = areaCode;
	}


}
