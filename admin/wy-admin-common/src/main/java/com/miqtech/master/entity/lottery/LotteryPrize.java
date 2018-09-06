package com.miqtech.master.entity.lottery;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

/**
 * 抽奖奖品设置
 */
@Entity
@Table(name = "lottery_t_prize")
public class LotteryPrize extends IdEntity {

	private static final long serialVersionUID = -725682248533158837L;

	private String icon;// 奖品图标
	private String name;// 奖品名
	private String price;// 价值描述

	@Column(name = "icon")
	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	@Column(name = "name")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "price")
	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

}
