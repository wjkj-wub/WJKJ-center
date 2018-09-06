package com.miqtech.master.entity.mall;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "mall_r_commodity_icon")
public class CommodityIcon extends IdEntity {
	private static final long serialVersionUID = 2986121321540342754L;

	private Long commodityId; //商品ID
	private String icon; //商品icon地址
	private Integer isMain; //是否为主图：0-否，1-是

	@Column(name = "commodity_id")
	public Long getCommodityId() {
		return commodityId;
	}

	public void setCommodityId(Long commodityId) {
		this.commodityId = commodityId;
	}

	@Column(name = "icon")
	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	@Column(name = "is_main")
	public Integer getIsMain() {
		return isMain;
	}

	public void setIsMain(Integer isMain) {
		this.isMain = isMain;
	}

}
