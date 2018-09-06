package com.miqtech.master.entity.amuse;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

/**
 * 娱乐赛反馈消息模版
 */
@Entity
@Table(name = "amuse_msg_feedback")
public class AmuseMsgFeedback extends IdEntity {

	private static final long serialVersionUID = 3132670320639311008L;

	private Integer type;// 通知类型:1-审核,2-发放,3-申诉,4-常见申诉理由
	private String content;// 内容

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

}
