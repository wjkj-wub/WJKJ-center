package com.miqtech.master.entity.activity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "activity_group")
public class ActivityGroup extends IdEntity {

	private static final long serialVersionUID = 9117456265426715361L;
	
	private Integer activityId;
	private Integer netbarId;
	private Integer targetId;//个人或战队ID,取决于is_team字段
	private String targetName;//个人或战队ID,取决于is_team字段
	private Integer round;//场次号
	private Integer groupNumber;//分组编号
	private Integer seatNumber;//小组编号
	private Integer seat;//所在分组位置:1-主场,2-客场
	private Integer rank ;// 名次
	private Integer isTeam;//是否是战队:0-false,1-true
	
	@Column(name = "activity_id")
	public Integer getActivityId() {
		return activityId;
	}
	public void setActivityId(Integer activityId) {
		this.activityId = activityId;
	}
	@Column(name = "netbar_id")
	public Integer getNetbarId() {
		return netbarId;
	}
	public void setNetbarId(Integer netbarId) {
		this.netbarId = netbarId;
	}
	@Column(name = "target_id")
	public Integer getTargetId() {
		return targetId;
	}
	public void setTargetId(Integer targetId) {
		this.targetId = targetId;
	}
	
	@Transient
	public String getTargetName() {
		return targetName;
	}
	public void setTargetName(String targetName) {
		this.targetName = targetName;
	}
	public Integer getRound() {
		return round;
	}
	public void setRound(Integer round) {
		this.round = round;
	}
	@Column(name = "group_number")
	public Integer getGroupNumber() {
		return groupNumber;
	}
	public void setGroupNumber(Integer groupNumber) {
		this.groupNumber = groupNumber;
	}
	@Column(name = "seat_number")
	public Integer getSeatNumber() {
		return seatNumber;
	}
	public void setSeatNumber(Integer seatNumber) {
		this.seatNumber = seatNumber;
	}
	public Integer getSeat() {
		return seat;
	}
	public void setSeat(Integer seat) {
		this.seat = seat;
	}
	public Integer getRank() {
		return rank;
	}
	public void setRank(Integer rank) {
		this.rank = rank;
	}
	@Column(name = "is_team")
	public Integer getIsTeam() {
		return isTeam;
	}
	public void setIsTeam(Integer isTeam) {
		this.isTeam = isTeam;
	}
	
	
	
}
