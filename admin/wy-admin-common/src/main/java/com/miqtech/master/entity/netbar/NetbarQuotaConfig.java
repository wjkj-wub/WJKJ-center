package com.miqtech.master.entity.netbar;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 周期奖金结算峰值
 */
@Entity
@Table(name = "netbar_quota_config")
public class NetbarQuotaConfig implements Serializable {

	private static final long serialVersionUID = -738951942044722684L;

	private Long id;
	private Long netbarId;// 网吧ID
	private Double amount;// 结算额峰值
	private Date monthly;// 所在月份

	@Id
	@Column(name = "id", nullable = false, unique = true)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

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

	@Column(name = "monthly")
	public Date getMonthly() {
		return monthly;
	}

	public void setMonthly(Date monthly) {
		this.monthly = monthly;
	}

}
