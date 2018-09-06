package com.miqtech.master.entity.activity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "activity_t_member")
public class ActivityMember extends IdEntity {
	private static final long serialVersionUID = -3206186518783563768L;
	private Long activityId;// 所属赛事ID
	private Long userId;// 用户ID
	private Long cardId;// 参赛卡ID
	private Long teamId;// 战队ID：0时为无战队；否则为战队ID
	private String name;// 成员姓名
	private String idCard;// 成员身份证号码
	private String telephone;// 手机号码
	private String qq;// qq
	private String server;// 大区
	private String labor;// 擅长位置
	private Integer isMonitor;// 是否为队长：0-否；1-是；
	private Integer isEnter;// 是否加入战队：0-否；1-是；
	private Integer inRecord;// 是否已录入：0-否；1-是；
	private Integer round;//场次
	private Integer isAccept;//队长是否接受0待处理1已接受2已拒绝
	private Integer signed;// 是否已签到:0-否,1-是

	@Column(name = "team_id")
	public Long getTeamId() {
		return teamId;
	}

	public void setTeamId(Long teamId) {
		this.teamId = teamId;
	}

	@Column(name = "name")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "id_card")
	public String getIdCard() {
		return idCard;
	}

	public void setIdCard(String idCard) {
		this.idCard = idCard;
	}

	@Column(name = "telephone")
	public String getTelephone() {
		return telephone;
	}

	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}

	@Column(name = "qq")
	public String getQq() {
		return qq;
	}

	public void setQq(String qq) {
		this.qq = qq;
	}

	@Column(name = "labor")
	public String getLabor() {
		return labor;
	}

	public void setLabor(String labor) {
		this.labor = labor;
	}

	@Column(name = "is_monitor")
	public Integer getIsMonitor() {
		return isMonitor;
	}

	public void setIsMonitor(Integer isMonitor) {
		this.isMonitor = isMonitor;
	}

	@Column(name = "is_enter")
	public Integer getIsEnter() {
		return isEnter;
	}

	public void setIsEnter(Integer isEnter) {
		this.isEnter = isEnter;
	}

	@Column(name = "user_id")
	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	@Column(name = "card_id")
	public Long getCardId() {
		return cardId;
	}

	public void setCardId(Long cardId) {
		this.cardId = cardId;
	}

	@Column(name = "activity_id")
	public Long getActivityId() {
		return activityId;
	}

	public void setActivityId(Long activityId) {
		this.activityId = activityId;
	}

	@Column(name = "server")
	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	@Column(name = "in_record")
	public Integer getInRecord() {
		return inRecord;
	}

	public void setInRecord(Integer inRecord) {
		this.inRecord = inRecord;
	}

	@Column(name = "round")
	public Integer getRound() {
		return round;
	}

	public void setRound(Integer round) {
		this.round = round;
	}

	@Column(name = "is_accept")
	public Integer getIsAccept() {
		return isAccept;
	}

	public void setIsAccept(Integer isAccept) {
		this.isAccept = isAccept;
	}

	@Column(name = "signed")
	public Integer getSigned() {
		return signed;
	}

	public void setSigned(Integer signed) {
		this.signed = signed;
	}

}
