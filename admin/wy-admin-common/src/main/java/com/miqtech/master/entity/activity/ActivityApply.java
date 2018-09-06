package com.miqtech.master.entity.activity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "activity_r_apply")
public class ActivityApply extends IdEntity {
	private static final long serialVersionUID = 6588650830718079882L;
	private Long activityId;// 赛事ID
	private Long targetId;// 战队或个人ID
	private Integer type;// 类型：1-个人；2-战队
	private Long netbarId;// 报名网吧的ID
	private Long favor;// 点赞数量
	private Integer round;//场次

	@Column(name = "activity_id")
	public Long getActivityId() {
		return activityId;
	}

	public void setActivityId(Long activityId) {
		this.activityId = activityId;
	}

	@Column(name = "target_id")
	public Long getTargetId() {
		return targetId;
	}

	public void setTargetId(Long targetId) {
		this.targetId = targetId;
	}

	@Column(name = "type")
	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	@Column(name = "netbar_id")
	public Long getNetbarId() {
		return netbarId;
	}

	public void setNetbarId(Long netbarId) {
		this.netbarId = netbarId;
	}

	@Column(name = "favor")
	public Long getFavor() {
		return favor;
	}

	public void setFavor(Long favor) {
		this.favor = favor;
	}

	@Column(name = "round")
	public Integer getRound() {
		return round;
	}

	public void setRound(Integer round) {
		this.round = round;
	}

}