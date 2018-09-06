package com.miqtech.master.entity.event;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "oet_event_member")
public class EventMember extends IdEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1531839720944755306L;
	@Column(name = "user_id")
	private Long userId;
	@Column(name = "round_id")
	private Long roundId;
	@Column(name = "team_id")
	private Long teamId;
	@Column(name = "name")
	private String name;
	@Column(name = "telephone")
	private String telephone;
	@Column(name = "qq")
	private String qq;
	@Column(name = "idcard")
	private String idcard;
	@Column(name = "labor")
	private String labor;
	@Column(name = "signed")
	private Integer signed;
	@Column(name = "is_monitor")
	private Byte isMonitor;
	@Column(name = "is_enter")
	private Byte isEnter;
	@Column(name = "is_accept")
	private Byte isAccept;
	@Column(name = "is_read")
	private Byte isRead;

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getRoundId() {
		return roundId;
	}

	public void setRoundId(Long roundId) {
		this.roundId = roundId;
	}

	public Long getTeamId() {
		return teamId;
	}

	public void setTeamId(Long teamId) {
		this.teamId = teamId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTelephone() {
		return telephone;
	}

	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}

	public String getQq() {
		return qq;
	}

	public void setQq(String qq) {
		this.qq = qq;
	}

	public String getIdcard() {
		return idcard;
	}

	public void setIdcard(String idcard) {
		this.idcard = idcard;
	}

	public String getLabor() {
		return labor;
	}

	public void setLabor(String labor) {
		this.labor = labor;
	}

	public Integer getSigned() {
		return signed;
	}

	public void setSigned(Integer signed) {
		this.signed = signed;
	}

	public Byte getIsMonitor() {
		return isMonitor;
	}

	public void setIsMonitor(Byte isMonitor) {
		this.isMonitor = isMonitor;
	}

	public Byte getIsEnter() {
		return isEnter;
	}

	public void setIsEnter(Byte isEnter) {
		this.isEnter = isEnter;
	}

	public Byte getIsAccept() {
		return isAccept;
	}

	public void setIsAccept(Byte isAccept) {
	}

	public Byte getIsRead() {
		return isRead;
	}

	public void setIsRead(Byte isRead) {
		this.isRead = isRead;
	}

}
