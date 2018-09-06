package com.miqtech.master.entity.common;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "sys_r_user_netbar")
public class SysUserNetbar extends IdEntity {
	private static final long serialVersionUID = 2991513154095059495L;
	private Long sysUserId;//系统用户id
	private Long netbarId;//网吧id
	private String firstName;//一级管理员
	private String secondName;//二级管理员
	private String thirdName;//三级管理员
	private String fourthName;//四级管理员

	@Column(name = "sys_user_id")
	public Long getSysUserId() {
		return sysUserId;
	}

	public void setSysUserId(Long sysUserId) {
		this.sysUserId = sysUserId;
	}

	@Column(name = "netbar_id")
	public Long getNetbarId() {
		return netbarId;
	}

	public void setNetbarId(Long netbarId) {
		this.netbarId = netbarId;
	}

	@Column(name = "first_name")
	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	@Column(name = "second_name")
	public String getSecondName() {
		return secondName;
	}

	public void setSecondName(String secondName) {
		this.secondName = secondName;
	}

	@Column(name = "third_name")
	public String getThirdName() {
		return thirdName;
	}

	public void setThirdName(String thirdName) {
		this.thirdName = thirdName;
	}

	@Column(name = "fourth_name")
	public String getFourthName() {
		return fourthName;
	}

	public void setFourthName(String fourthName) {
		this.fourthName = fourthName;
	}

}
