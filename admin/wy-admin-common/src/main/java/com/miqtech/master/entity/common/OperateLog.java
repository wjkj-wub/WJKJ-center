package com.miqtech.master.entity.common;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.entity.IdEntity;

/**
 * 网娱后台商城后台用户操作记录
 */
@Entity
@Table(name = "operate_log")
public class OperateLog extends IdEntity {

	private static final long serialVersionUID = 3178126641797191288L;

	public OperateLog() {
		super();
	}

	public OperateLog(int sysType, long sysUserId, long thirdId, int type, String info) {
		setSysType(sysType);
		setSysUserId(sysUserId);
		setType(type);
		setThirdId(thirdId);
		setInfo(info);
		setValid(CommonConstant.INT_BOOLEAN_TRUE);
		setCreateDate(new Date());
	}

	private Integer sysType;//系统类型：1-网娱后台，2-商城后台
	private Long sysUserId;// 系统用户ID
	private Integer type;// 操作类型
	private Long thirdId;// 关联到其他表的ID
	private String info;// 操作详情

	@Column(name = "sys_type")
	public Integer getSysType() {
		return sysType;
	}

	public void setSysType(Integer sysType) {
		this.sysType = sysType;
	}

	@Column(name = "sys_user_id")
	public Long getSysUserId() {
		return sysUserId;
	}

	public void setSysUserId(Long sysUserId) {
		this.sysUserId = sysUserId;
	}

	@Column(name = "type")
	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	@Column(name = "third_id")
	public Long getThirdId() {
		return thirdId;
	}

	public void setThirdId(Long thirdId) {
		this.thirdId = thirdId;
	}

	@Column(name = "info")
	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

}
