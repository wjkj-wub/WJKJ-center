package com.miqtech.master.entity.activity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "activity_r_rounds")
public class ActivityRound extends IdEntity {
	private static final long serialVersionUID = -2405200064725176894L;

	private Long activityId;// 赛事ID

	private Date beginTime;// 报名开始时间

	private Date overTime;// 报名结束时间(比赛开始时间)

	private Date endTime;// 比赛结束时间

	private String netbars;// 网吧列表

	private String areas;// 地区列表

	private Integer round;// 场次

	private String remark;// 场次说明

	@Column(name = "activity_id")
	public Long getActivityId() {
		return activityId;
	}

	public void setActivityId(Long activityId) {
		this.activityId = activityId;
	}

	@Column(name = "begin_time")
	public Date getBeginTime() {
		return beginTime;
	}

	public void setBeginTime(Date beginTime) {
		this.beginTime = beginTime;
	}

	@Column(name = "over_time")
	public Date getOverTime() {
		return overTime;
	}

	public void setOverTime(Date overTime) {
		this.overTime = overTime;
	}

	@Column(name = "end_time")
	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	@Column(name = "netbars")
	public String getNetbars() {
		return netbars;
	}

	public void setNetbars(String netbars) {
		this.netbars = netbars;
	}

	@Column(name = "areas")
	public String getAreas() {
		return areas;
	}

	public void setAreas(String areas) {
		this.areas = areas;
	}

	@Column(name = "round")
	public Integer getRound() {
		return round;
	}

	public void setRound(Integer round) {
		this.round = round;
	}

	@Column(name = "remark")
	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

}
