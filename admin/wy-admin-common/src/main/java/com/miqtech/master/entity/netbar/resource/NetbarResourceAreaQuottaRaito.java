package com.miqtech.master.entity.netbar.resource;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "netbar_resource_area_quotta_raito")
public class NetbarResourceAreaQuottaRaito extends IdEntity {
	private static final long serialVersionUID = 6848144236819767499L;
	private String areaCode;//区域code
	private Float vipRatio;
	private Float goldRatio;
	private Float jewelRatio;

	@Column(name = "area_code")
	public String getAreaCode() {
		return areaCode;
	}

	public void setAreaCode(String areaCode) {
		this.areaCode = areaCode;
	}

	@Column(name = "vip_ratio")
	public Float getVipRatio() {
		return vipRatio;
	}

	public void setVipRatio(Float vipRatio) {
		this.vipRatio = vipRatio;
	}

	@Column(name = "gold_ratio")
	public Float getGoldRatio() {
		return goldRatio;
	}

	public void setGoldRatio(Float goldRatio) {
		this.goldRatio = goldRatio;
	}

	@Column(name = "jewel_ratio")
	public Float getJewelRatio() {
		return jewelRatio;
	}

	public void setJewelRatio(Float jewelRatio) {
		this.jewelRatio = jewelRatio;
	}

}
