package com.miqtech.master.entity.user;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "user_t_fans")
public class UserFans extends IdEntity {
	private static final long serialVersionUID = 597279183099275769L;
	private Long userId;//用户id
	private Long fansId;//粉丝id（其他用户）

	@Column(name = "user_id")
	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	@Column(name = "fan_id")
	public Long getFansId() {
		return fansId;
	}

	public void setFansId(Long fansId) {
		this.fansId = fansId;
	}

}
