package com.miqtech.master.entity.netbar;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "netbar_t_recharge_activity_prize")
public class NetbarRechargeActivityPrize extends IdEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8989895396791410743L;

	private Integer activityId;//充值活动id
	private String prizeName;//奖品名称
	private Integer fullParam;//根据活动类型，1表示满额条件，2表示满额抽奖概率
	private Integer prizeCount;//奖品数量:-1表示没有数量限制
	private Integer receiveCount;

	@Transient
	public Integer getReceiveCount() {
		return receiveCount;
	}

	public void setReceiveCount(Integer receiveCount) {
		this.receiveCount = receiveCount;
	}

	@Column(name = "activity_id")
	public Integer getActivityId() {
		return activityId;
	}

	public void setActivityId(Integer activityId) {
		this.activityId = activityId;
	}

	@Column(name = "prize_name")
	public String getPrizeName() {
		return prizeName;
	}

	public void setPrizeName(String prizeName) {
		this.prizeName = prizeName;
	}

	@Column(name = "full_param")
	public Integer getFullParam() {
		return fullParam;
	}

	public void setFullParam(Integer fullParam) {
		this.fullParam = fullParam;
	}

	@Column(name = "prize_count")
	public Integer getPrizeCount() {
		return prizeCount;
	}

	public void setPrizeCount(Integer prizeCount) {
		this.prizeCount = prizeCount;
	}

}
