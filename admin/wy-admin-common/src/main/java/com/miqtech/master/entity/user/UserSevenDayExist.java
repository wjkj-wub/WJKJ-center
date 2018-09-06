package com.miqtech.master.entity.user;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "user_seven_day_exist")
public class UserSevenDayExist extends IdEntity {
	private static final long serialVersionUID = 5383828683602544699L;
	private Long userId;
	private String inviteCode;
	private Date inviteDate;

	@Column(name = "user_id")
	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	@Column(name = "invite_code")
	public String getInviteCode() {
		return inviteCode;
	}

	public void setInviteCode(String inviteCode) {
		this.inviteCode = inviteCode;
	}

	@Column(name = "invite_date")
	public Date getInviteDate() {
		return inviteDate;
	}

	public void setInviteDate(Date inviteDate) {
		this.inviteDate = inviteDate;
	}

}
