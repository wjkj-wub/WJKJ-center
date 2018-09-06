package com.miqtech.master.entity.msg;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "msg_r_sys_read_log")
public class MsgReadLog4Sys extends IdEntity {
	private static final long serialVersionUID = -9030028896549619967L;
	private Long userId;// 用户ID
	private Long msgId;// 消息ID
	private Integer type;// 消息类别

	@Column(name = "user_id")
	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	@Column(name = "msg_id")
	public Long getMsgId() {
		return msgId;
	}

	public void setMsgId(Long msgId) {
		this.msgId = msgId;
	}

	@Column(name = "type")
	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

}
