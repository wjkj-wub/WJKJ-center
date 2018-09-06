package com.miqtech.master.entity.log;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

/**
 * 商户端用户操作日志
 *
 */
@Entity
@Table(name = "merchant_t_operate_log")
public class MerchantUserLog extends IdEntity {
	private static final long serialVersionUID = 4532451126019418901L;
	private Long netbarId;//网吧id
	private Integer userType;//用户类型1业主2雇员
	private String msg;//操作
	private String info;//操作详情

	@Column(name = "netbar_id")
	public Long getNetbarId() {
		return netbarId;
	}

	public void setNetbarId(Long netbarId) {
		this.netbarId = netbarId;
	}

	@Column(name = "user_type")
	public Integer getUserType() {
		return userType;
	}

	public void setUserType(Integer userType) {
		this.userType = userType;
	}

	@Column(name = "msg")
	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	@Column(name = "info")
	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

}
