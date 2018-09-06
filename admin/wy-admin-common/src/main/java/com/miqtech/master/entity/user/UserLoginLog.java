package com.miqtech.master.entity.user;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "user_r_login_log")
public class UserLoginLog extends IdEntity {

	private static final long serialVersionUID = -4632349648401414586L;
	private Long userId;// 用户ID
	private Long combo;// 连续登陆的次数
	private Date lasDate;// 最后一次登陆的时间
	private Integer isSendRedbag;//是否已经发送过登陆红包：0-false,1-true

	@Column(name = "user_id")
	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	@Column(name = "combo")
	public Long getCombo() {
		return combo;
	}

	public void setCombo(Long combo) {
		this.combo = combo;
	}

	@Column(name = "last_time")
	public Date getLasDate() {
		return lasDate;
	}

	public void setLasDate(Date lasDate) {
		this.lasDate = lasDate;
	}

	@Column(name = "is_send_redbag")
	public Integer getIsSendRedbag() {
		return isSendRedbag;
	}

	public void setIsSendRedbag(Integer isSendRedbag) {
		this.isSendRedbag = isSendRedbag;
	}
}
