package com.miqtech.master.entity.finance;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

/**
 * 结算周期设置
 */
@Entity
@Table(name = "finance_t_period")
public class Period extends IdEntity {
	private static final long serialVersionUID = 5858058518438197394L;

	private String areaCode;//地区
	private Integer period;//结算周期

	@Column(name = "area_code")
	public String getAreaCode() {
		return areaCode;
	}

	public void setAreaCode(String areaCode) {
		this.areaCode = areaCode;
	}

	@Column(name = "period")
	public Integer getPeriod() {
		return period;
	}

	public void setPeriod(Integer period) {
		this.period = period;
	}
}
