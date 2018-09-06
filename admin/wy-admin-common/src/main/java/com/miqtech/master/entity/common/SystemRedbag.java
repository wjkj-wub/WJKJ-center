package com.miqtech.master.entity.common;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "sys_t_redbag")
public class SystemRedbag extends IdEntity {
	private static final long serialVersionUID = 1026677183037731661L;
	private Integer money;// 红包金额(单位元)
	private Integer day;// 有效期天数
	private Integer type;// 0 首次登陆 1 注册绑定 2预约支付3周红包4分享红包5商品红包 6cdkey兑换红包 7自有商品类型的奖品发放
	private String explain;

	private Integer restrict;//红包使用是否有限制 0无限制 1有限制
	private Date beginTime;//开始时间
	private Date endTime;//结束时间
	private Integer maxMoney;//最大金额
	private Long totalmoney;//总金额

	@Column(name = "money")
	public Integer getMoney() {
		return money;
	}

	public void setMoney(Integer money) {
		this.money = money;
	}

	@Column(name = "day")
	public Integer getDay() {
		return day;
	}

	public void setDay(Integer day) {
		this.day = day;
	}

	@Column(name = "type")
	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	@Column(name = "`restrict`")
	public Integer getRestrict() {
		return restrict;
	}

	public void setRestrict(Integer restrict) {
		this.restrict = restrict;
	}

	@Column(name = "begin_time")
	public Date getBeginTime() {
		return beginTime;
	}

	public void setBeginTime(Date beginTime) {
		this.beginTime = beginTime;
	}

	@Column(name = "end_time")
	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	@Column(name = "max_money")
	public Integer getMaxMoney() {
		return maxMoney;
	}

	public void setMaxMoney(Integer maxMoney) {
		this.maxMoney = maxMoney;
	}

	@Column(name = "total_money")
	public Long getTotalmoney() {
		return totalmoney;
	}

	public void setTotalmoney(Long totalmoney) {
		this.totalmoney = totalmoney;
	}

	@Column(name = "`explain`")
	public String getExplain() {
		return explain;
	}

	public void setExplain(String explain) {
		this.explain = explain;
	}

}
