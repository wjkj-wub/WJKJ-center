package com.miqtech.master.entity.mall;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

/**
 *邀请好友
 *
 */
@Entity
@Table(name = "mall_t_msg")
public class MallMsg extends IdEntity {
	private static final long serialVersionUID = 8529340469336932702L;

	private Long userId;//用户id;
	private Integer type;//类型1反馈消息2兑换异议3兑换消息
	private String content;//内容
	private Long targetId;//目标ID

	@Column(name = "user_id")
	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	@Column(name = "type")
	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	@Column(name = "content")
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	@Column(name = "target_id")
	public Long getTargetId() {
		return targetId;
	}

	public void setTargetId(Long targetId) {
		this.targetId = targetId;
	}

}
