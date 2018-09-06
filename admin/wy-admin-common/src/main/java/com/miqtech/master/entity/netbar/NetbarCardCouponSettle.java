package com.miqtech.master.entity.netbar;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "netbar_card_coupon_settle")
public class NetbarCardCouponSettle extends IdEntity {

	private static final long serialVersionUID = -3783338256313621277L;

	private Long netbarId;
	private Double amount;
	private Integer num;
	private Integer status;

	@Column(name = "netbar_id")
	public Long getNetbarId() {
		return netbarId;
	}

	public void setNetbarId(Long netbarId) {
		this.netbarId = netbarId;
	}

	@Column(name = "amount")
	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	@Column(name = "num")
	public Integer getNum() {
		return num;
	}

	public void setNum(Integer num) {
		this.num = num;
	}

	@Column(name = "status")
	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

}
