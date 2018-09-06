package com.miqtech.master.entity.pc.user;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "pc_user_retention")
public class PcUserRetention implements Serializable {

	private static final long serialVersionUID = 2393349274438357907L;

	private Long id;
	private Date registDate;// 用户注册时间
	private Integer registUserCount;// （当天即registDate）注册用户数量
	private Double retentionRateOne;// 1天留存率
	private Double retentionRateTwo;// 2天留存率
	private Double retentionRateSeven;// 7天留存率
	private Double retentionRateFourteen;// 14天留存率
	private Double retentionRateThirty;// 30天留存率
	private Double retentionRateAfterThirty;// 30天后留存率
	private Date createDate;

	@Id
	@Column(name = "id", nullable = false, unique = true)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name = "regist_date")
	public Date getRegistDate() {
		return registDate;
	}

	public void setRegistDate(Date registDate) {
		this.registDate = registDate;
	}

	@Column(name = "regist_user_count")
	public Integer getRegistUserCount() {
		return registUserCount;
	}

	public void setRegistUserCount(Integer registUserCount) {
		this.registUserCount = registUserCount;
	}

	@Column(name = "retention_rate_one")
	public Double getRetentionRateOne() {
		return retentionRateOne;
	}

	public void setRetentionRateOne(Double retentionRateOne) {
		this.retentionRateOne = retentionRateOne;
	}

	@Column(name = "retention_rate_two")
	public Double getRetentionRateTwo() {
		return retentionRateTwo;
	}

	public void setRetentionRateTwo(Double retentionRateTwo) {
		this.retentionRateTwo = retentionRateTwo;
	}

	@Column(name = "retention_rate_seven")
	public Double getRetentionRateSeven() {
		return retentionRateSeven;
	}

	public void setRetentionRateSeven(Double retentionRateSeven) {
		this.retentionRateSeven = retentionRateSeven;
	}

	@Column(name = "retention_rate_fourteen")
	public Double getRetentionRateFourteen() {
		return retentionRateFourteen;
	}

	public void setRetentionRateFourteen(Double retentionRateFourteen) {
		this.retentionRateFourteen = retentionRateFourteen;
	}

	@Column(name = "retention_rate_thirty")
	public Double getRetentionRateThirty() {
		return retentionRateThirty;
	}

	public void setRetentionRateThirty(Double retentionRateThirty) {
		this.retentionRateThirty = retentionRateThirty;
	}

	@Column(name = "retention_rate_after_thirty")
	public Double getRetentionRateAfterThirty() {
		return retentionRateAfterThirty;
	}

	public void setRetentionRateAfterThirty(Double retentionRateAfterThirty) {
		this.retentionRateAfterThirty = retentionRateAfterThirty;
	}

	@Column(name = "create_date")
	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

}
