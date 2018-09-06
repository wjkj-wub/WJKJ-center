package com.miqtech.master.entity.finance;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;
import com.miqtech.master.utils.EncodeUtils;

/**
 * 财务用户
 */
@Entity
@Table(name = "finance_t_user")
public class FinanceUser extends IdEntity {

	private static final long serialVersionUID = -7261842862633197334L;

	private String username;
	private String password;
	private String nickname;
	private String telephone;
	private String areaCode;

	@Column(name = "username")
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@Column(name = "password")
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setEncryptPassword(String password) {
		setPassword(EncodeUtils.base64Md5(password));
	}

	@Column(name = "nickname")
	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	@Column(name = "telephone")
	public String getTelephone() {
		return telephone;
	}

	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}

	@Column(name = "area_code")
	public String getAreaCode() {
		return areaCode;
	}

	public void setAreaCode(String areaCode) {
		this.areaCode = areaCode;
	}

}
