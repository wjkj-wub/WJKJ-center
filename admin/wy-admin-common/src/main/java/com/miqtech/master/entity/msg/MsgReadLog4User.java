package com.miqtech.master.entity.msg;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "msg_r_user_read_log")
public class MsgReadLog4User extends IdEntity {
	private static final long serialVersionUID = -6148966600299134959L;
	private Long userId;// 用户ID
	private Long msgId;// 消息ID

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

}
