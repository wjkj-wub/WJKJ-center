package com.miqtech.master.entity.user;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "user_t_black")
public class UserBlack extends IdEntity {
	private static final long serialVersionUID = 5675415629542427991L;
	private Long userId;//用户id
	private String account;//获利帐号
	private Integer channel;//渠道：1-娱乐赛，2-商城
	private Integer isWhite;//是否白名单  0 否 1 是（黑变白）

	@Column(name = "user_id")
	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	@Column(name = "account")
	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	@Column(name = "channel")
	public Integer getChannel() {
		return channel;
	}

	public void setChannel(Integer channel) {
		this.channel = channel;
	}

	@Column(name = "is_white")
	public Integer getIsWhite() {
		return isWhite;
	}

	public void setIsWhite(Integer isWhite) {
		this.isWhite = isWhite;
	}

}
