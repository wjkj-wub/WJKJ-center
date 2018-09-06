package com.miqtech.master.entity.common;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.miqtech.master.entity.IdEntity;

/**
 * 邀请码广告领取记录
 */
@Entity
@Table(name = "invite_t_advertise")
@JsonIgnoreProperties({ "createDate", "id", "createUserId", "updateDate", "updateUserId", "valid" })
public class InviteAdvertise extends IdEntity {
	private static final long serialVersionUID = -5857422713362177388L;
	private Long adId;// 广告id
	private Long userId;// 用户id
	private String inviteCode;// 邀请码

	@Column(name = "ad_id")
	public Long getAdId() {
		return adId;
	}

	public void setAdId(Long adId) {
		this.adId = adId;
	}

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

}
