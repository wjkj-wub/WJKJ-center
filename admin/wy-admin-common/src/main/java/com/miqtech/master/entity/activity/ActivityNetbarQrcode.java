package com.miqtech.master.entity.activity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "activity_netbar_qrcode")
public class ActivityNetbarQrcode extends IdEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Long activityId; //赛事ID
	private int round;//赛事场次
	private Long netbarId;//网吧ID
	private String qrcode;//二维码url

	@Column(name = "activity_id")
	public Long getActivityId() {
		return activityId;
	}

	public void setActivityId(Long activityId) {
		this.activityId = activityId;
	}

	@Column(name = "round")
	public int getRound() {
		return round;
	}

	public void setRound(int round) {
		this.round = round;
	}

	@Column(name = "netbar_id ")
	public Long getNetbarId() {
		return netbarId;
	}

	public void setNetbarId(Long netbarId) {
		this.netbarId = netbarId;
	}

	@Column(name = "qrcode")
	public String getQrcode() {
		return qrcode;
	}

	public void setQrcode(String qrcode) {
		this.qrcode = qrcode;
	}

}
