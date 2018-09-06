package com.miqtech.master.entity.mall;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

/**
 *邀请好友
 *
 */
@Entity
@Table(name = "mall_t_invite")
public class MallInvite extends IdEntity {
	private static final long serialVersionUID = -2357333732994272267L;
	private Long inviteUserId;
	private String invitedTelephone;
	private Integer isRegister;

	@Column(name = "invite_user_id")
	public Long getInviteUserId() {
		return inviteUserId;
	}

	public void setInviteUserId(Long inviteUserId) {
		this.inviteUserId = inviteUserId;
	}

	@Column(name = "invited_telephone")
	public String getInvitedTelephone() {
		return invitedTelephone;
	}

	public void setInvitedTelephone(String invitedTelephone) {
		this.invitedTelephone = invitedTelephone;
	}

	@Column(name = "is_register")
	public Integer getIsRegister() {
		return isRegister;
	}

	public void setIsRegister(Integer isRegister) {
		this.isRegister = isRegister;
	}

}
