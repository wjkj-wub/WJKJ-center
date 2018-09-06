package com.miqtech.master.entity.common;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "sys_value_added_card")
public class SysValueAddedCard extends IdEntity {
	private static final long serialVersionUID = -1661871911295009157L;
	private Integer amount;//int(4) NOT NULL COMMENT '金额',
	private Integer day;//'有效期天数',
	private String explain;//'描述',
	private Integer restrict;//'是否有限制条件0无 1有',
	private Integer limitMinMoney; //'最低使用金额',

	@Column(name = "amount")
	public Integer getAmount() {
		return amount;
	}

	public void setAmount(Integer amount) {
		this.amount = amount;
	}

	@Column(name = "day")
	public Integer getDay() {
		return day;
	}

	public void setDay(Integer day) {
		this.day = day;
	}

	@Column(name = "`explain`")
	public String getExplain() {
		return explain;
	}

	public void setExplain(String explain) {
		this.explain = explain;
	}

	@Column(name = "`restrict`")
	public Integer getRestrict() {
		return restrict;
	}

	public void setRestrict(Integer restrict) {
		this.restrict = restrict;
	}

	@Column(name = "limit_min_money")
	public Integer getLimitMinMoney() {
		return limitMinMoney;
	}

	public void setLimitMinMoney(Integer limitMinMoney) {
		this.limitMinMoney = limitMinMoney;
	}

}
