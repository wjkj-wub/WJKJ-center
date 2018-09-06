package com.miqtech.master.entity.common;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "sys_t_merchant_notice")
public class SystemMerchantNotice extends IdEntity {
	private static final long serialVersionUID = 6848144236819767499L;
	private String title;//通知标题
	private String content;//通知内容

	@Column(name = "title")
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Column(name = "content")
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

}
