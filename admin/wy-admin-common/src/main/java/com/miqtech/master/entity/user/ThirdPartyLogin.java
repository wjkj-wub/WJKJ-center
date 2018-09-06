package com.miqtech.master.entity.user;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "third_party_login")
public class ThirdPartyLogin extends IdEntity {
	private static final long serialVersionUID = 7290631086671384098L;
	private String openId;
	private Integer platform;
	private String thirdPartyUsername;
	private String username;
	private String icon;
	private String nickname;
	private Integer sex;

	@Column(name = "open_id")
	public String getOpenId() {
		return openId;
	}

	public void setOpenId(String openId) {
		this.openId = openId;
	}

	@Column(name = "platform")
	public Integer getPlatform() {
		return platform;
	}

	public void setPlatform(Integer platform) {
		this.platform = platform;
	}

	@Column(name = "third_party_username")
	public String getThirdPartyUsername() {
		return thirdPartyUsername;
	}

	public void setThirdPartyUsername(String thirdPartyUsername) {
		this.thirdPartyUsername = thirdPartyUsername;
	}

	@Column(name = "username")
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@Column(name = "icon")
	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	@Column(name = "nickname")
	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	@Column(name = "sex")
	public Integer getSex() {
		return sex;
	}

	public void setSex(Integer sex) {
		this.sex = sex;
	}

}