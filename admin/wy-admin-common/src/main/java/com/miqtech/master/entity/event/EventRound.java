package com.miqtech.master.entity.event;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "oet_event_round")
public class EventRound extends IdEntity{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1603334826073980512L;
	@Column(name = "event_id")
	private Long eventId;
	@Column(name = "round_num")
	private Integer roundNum;
	@Column(name="status")
	private Byte status;
	@Column(name="type")
	private Byte type;
	@Column(name="regime")
	private Byte regime;
	@Column(name = "allow_apply")
	private Byte allowApply;
	@Column(name = "apply_type")
	private Byte applyType;
	@Column(name = "need_third")
	private Byte needThird;
	@Column(name = "max_num")
	private Integer maxNum;
	@Column(name = "activity_begin")
	private Date activityBegin;
	@Column(name = "apply_begin")
	private Date applyBegin;
	@Column(name = "apply_end")
	private Date applyEnd;
	public Long getEventId() {
		return eventId;
	}
	public void setEventId(Long eventId) {
		this.eventId = eventId;
	}
	public Integer getRoundNum() {
		return roundNum;
	}
	public void setRoundNum(Integer roundNum) {
		this.roundNum = roundNum;
	}
	public Byte getStatus() {
		return status;
	}
	public void setStatus(Byte status) {
		this.status = status;
	}
	public Byte getType() {
		return type;
	}
	public void setType(Byte type) {
		this.type = type;
	}
	public Byte getRegime() {
		return regime;
	}
	public void setRegime(Byte regime) {
		this.regime = regime;
	}
	public Byte getAllowApply() {
		return allowApply;
	}
	public void setAllowApply(Byte allowApply) {
		this.allowApply = allowApply;
	}
	public Byte getApplyType() {
		return applyType;
	}
	public void setApplyType(Byte applyType) {
		this.applyType = applyType;
	}
	public Byte getNeedThird() {
		return needThird;
	}
	public void setNeedThird(Byte needThird) {
		this.needThird = needThird;
	}
	public Integer getMaxNum() {
		return maxNum;
	}
	public void setMaxNum(Integer maxNum) {
		this.maxNum = maxNum;
	}
	public Date getActivityBegin() {
		return activityBegin;
	}
	public void setActivityBegin(Date activityBegin) {
		this.activityBegin = activityBegin;
	}
	public Date getApplyBegin() {
		return applyBegin;
	}
	public void setApplyBegin(Date applyBegin) {
		this.applyBegin = applyBegin;
	}
	public Date getApplyEnd() {
		return applyEnd;
	}
	public void setApplyEnd(Date applyEnd) {
		this.applyEnd = applyEnd;
	}
	
}
