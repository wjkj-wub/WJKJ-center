package com.miqtech.master.entity.netbar;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

/**
 * 雇员交接班
 */
@Entity
@Table(name = "netbar_t_staff_batch")
public class NetbarStaffBatch extends IdEntity {

	private static final long serialVersionUID = 4771026024162891353L;

	private Long netbarId;
	private Double totalAmount;// 总金额
	private Double amount;// 支付金额
	private Double rebateAmount;// 折扣金额
	private Double redbagAmount;// 红包金额
	private Double scoreAmount;// 积分金额
	private Double valueAddedAmount;//增值券金额
	private Integer status;// 状态：1 未申请，2 申请
	private Integer orderNum;// 订单数量
	private Date earliestDate;// 首笔订单的时间
	private Date latestDate;// 末笔订单的时间

	@Column(name = "netbar_id")
	public Long getNetbarId() {
		return netbarId;
	}

	public void setNetbarId(Long netbarId) {
		this.netbarId = netbarId;
	}

	@Column(name = "total_amount")
	public Double getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(Double totalAmount) {
		this.totalAmount = totalAmount;
	}

	@Column(name = "amount")
	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	@Column(name = "rebate_amount")
	public Double getRebateAmount() {
		return rebateAmount;
	}

	public void setRebateAmount(Double rebateAmount) {
		this.rebateAmount = rebateAmount;
	}

	@Column(name = "redbag_amount")
	public Double getRedbagAmount() {
		return redbagAmount;
	}

	public void setRedbagAmount(Double redbagAmount) {
		this.redbagAmount = redbagAmount;
	}

	@Column(name = "score_amount")
	public Double getScoreAmount() {
		return scoreAmount;
	}

	public void setScoreAmount(Double scoreAmount) {
		this.scoreAmount = scoreAmount;
	}

	@Column(name = "status")
	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	@Column(name = "order_num")
	public Integer getOrderNum() {
		return orderNum;
	}

	public void setOrderNum(Integer orderNum) {
		this.orderNum = orderNum;
	}

	@Column(name = "earliest_date")
	public Date getEarliestDate() {
		return earliestDate;
	}

	public void setEarliestDate(Date earliestDate) {
		this.earliestDate = earliestDate;
	}

	@Column(name = "latest_date")
	public Date getLatestDate() {
		return latestDate;
	}

	public void setLatestDate(Date latestDate) {
		this.latestDate = latestDate;
	}

	@Column(name = "value_added_amount")
	public Double getValueAddedAmount() {
		return valueAddedAmount;
	}

	public void setValueAddedAmount(Double valueAddedAmount) {
		this.valueAddedAmount = valueAddedAmount;
	}
}
