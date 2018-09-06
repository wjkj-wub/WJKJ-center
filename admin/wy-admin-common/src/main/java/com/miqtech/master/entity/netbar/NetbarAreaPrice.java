package com.miqtech.master.entity.netbar;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "netbar_area_price")
public class NetbarAreaPrice extends IdEntity {

	private static final long serialVersionUID = -2093091011154387500L;

	private Long netbarId;
	private String areaName;//消费区域名称
	private Float price;
	private Float rebatePrice;

	@Column(name = "netbar_id")
	public Long getNetbarId() {
		return netbarId;
	}

	public void setNetbarId(Long netbarId) {
		this.netbarId = netbarId;
	}

	@Column(name = "area_name")
	public String getAreaName() {
		return areaName;
	}

	public void setAreaName(String areaName) {
		this.areaName = areaName;
	}

	@Column(name = "price")
	public Float getPrice() {
		return price;
	}

	public void setPrice(Float price) {
		this.price = price;
	}

	@Column(name = "rebate_price")
	public Float getRebatePrice() {
		return rebatePrice;
	}

	public void setRebatePrice(Float rebatePrice) {
		this.rebatePrice = rebatePrice;
	}


}
