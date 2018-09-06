package com.miqtech.master.entity.netbar;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "netbar_t_recharge_activity")
public class NetbarRechargeActivity extends IdEntity {

	private static final long serialVersionUID = 4152563294803644022L;

	private Integer netbarId;// 网吧id
	private String activityName;// 活动名称
	private Integer type;//活动类别:1满额送，2满额抽
	private Integer fullAmount;// 抽奖条件:满多少抽
	private Integer effectDays;// 奖品有效天数',
	private Integer isTimed;//是否定时开启活动:1是，0否
	private Integer startStatus;//开启状态:1是，0否，2已关闭
	private Date startDate;//开始时间
	private Date endDate;// 结束时间
	private String deleteId;// 删除奖品id

	private List<NetbarRechargeActivityPrize> prizes;// 活动奖品

	@Column(name = "start_status")
	public Integer getStartStatus() {
		return startStatus;
	}

	public void setStartStatus(Integer startStatus) {
		this.startStatus = startStatus;
	}

	@Transient
	public String getDeleteId() {
		return deleteId;
	}

	public void setDeleteId(String deleteId) {
		this.deleteId = deleteId;
	}

	@Transient
	public List<NetbarRechargeActivityPrize> getPrizes() {
		return prizes;
	}

	public void setPrizes(List<NetbarRechargeActivityPrize> prizes) {
		this.prizes = prizes;
	}

	@Column(name = "netbar_id")
	public Integer getNetbarId() {
		return netbarId;
	}

	public void setNetbarId(Integer netbarId) {
		this.netbarId = netbarId;
	}

	@Column(name = "name")
	public String getActivityName() {
		return activityName;
	}

	public void setActivityName(String activityName) {
		this.activityName = activityName;
	}

	@Column(name = "type")
	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	@Column(name = "full_amount")
	public Integer getFullAmount() {
		return fullAmount;
	}

	public void setFullAmount(Integer fullAmount) {
		this.fullAmount = fullAmount;
	}

	@Column(name = "effect_days")
	public Integer getEffectDays() {
		return effectDays;
	}

	public void setEffectDays(Integer effectDays) {
		this.effectDays = effectDays;
	}

	@Column(name = "is_timed")
	public Integer getIsTimed() {
		return isTimed;
	}

	public void setIsTimed(Integer isTimed) {
		this.isTimed = isTimed;
	}

	@Column(name = "start_date")
	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	@Column(name = "end_date")
	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

}
