package com.miqtech.master.entity.code;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

/**
 * 邀请记录
 *
 */
@Entity
@Table(name = "invitecode_record")
public class InviteRecord extends IdEntity {
	private static final long serialVersionUID = -7405698680214092845L;
	private Long codeId;//邀请码id
	private String code;//邀请码code

	private Long userId;//用户id

	@Column(name = "code_id")
	public Long getCodeId() {
		return codeId;
	}

	public void setCodeId(Long codeId) {
		this.codeId = codeId;
	}

	@Column(name = "user_id")
	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	@Column(name = "code")
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

}