package com.miqtech.master.entity.netbar;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "netbar_order_day_statistics")
public class NetbarOrderDayStatistics extends IdEntity {
	private static final long serialVersionUID = 5663652551744074309L;
	private Long netbarId;// 网吧ID
	private Integer type;// 支付类型:1-支付宝;2-财付通
	private Float allAmount;// 网吧订单总金额:含未计算金额（status!=0）
	private Float totalAmount;// 结算总金额
	private Float amount;// 用户支付金额
	private Float rebateAmount;// 网吧折扣金额
	private Float redbagAmount;// 红包抵扣金额
	private Float scoreAmount;// 积分抵扣金额

	@Column(name = "netbar_id")
	public Long getNetbarId() {
		return netbarId;
	}

	public void setNetbarId(Long netbarId) {
		this.netbarId = netbarId;
	}

	@Column(name = "type")
	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	@Column(name = "all_amount")
	public Float getAllAmount() {
		return allAmount;
	}

	public void setAllAmount(Float allAmount) {
		this.allAmount = allAmount;
	}

	@Column(name = "total_amount")
	public Float getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(Float totalAmount) {
		this.totalAmount = totalAmount;
	}

	@Column(name = "amount")
	public Float getAmount() {
		return amount;
	}

	public void setAmount(Float amount) {
		this.amount = amount;
	}

	@Column(name = "rebate_amount")
	public Float getRebateAmount() {
		return rebateAmount;
	}

	public void setRebateAmount(Float rebateAmount) {
		this.rebateAmount = rebateAmount;
	}

	@Column(name = "redbag_amount")
	public Float getRedbagAmount() {
		return redbagAmount;
	}

	public void setRedbagAmount(Float redbagAmount) {
		this.redbagAmount = redbagAmount;
	}

	@Column(name = "score_amount")
	public Float getScoreAmount() {
		return scoreAmount;
	}

	public void setScoreAmount(Float scoreAmount) {
		this.scoreAmount = scoreAmount;
	}


}
