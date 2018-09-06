package com.miqtech.master.entity.activity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "activity_r_invite")
public class ActivityInvite extends IdEntity {

	private static final long serialVersionUID = 6358928740619022150L;

	private Long inviteUserId;// 邀请人ID
	private String invitedTelephone;// 被邀请人手机号
	private Integer type;// 邀请类型：1-约战；2-战队
	private Long targetId;// 约战或战队ID
	private Integer status;// 邀请状态：-1无效0-初始状态；1-拒绝；2-接受；

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

	@Column(name = "type")
	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	@Column(name = "target_id")
	public Long getTargetId() {
		return targetId;
	}

	public void setTargetId(Long targetId) {
		this.targetId = targetId;
	}

	@Column(name = "status")
	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

}
