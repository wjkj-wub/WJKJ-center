package com.miqtech.master.entity.common;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "sys_r_notice_read")
public class NoticeRead extends IdEntity {
	private static final long serialVersionUID = 6848144236819767499L;
	private Long noticeId;//公告ID
	private Long merchantId;//商户ID

	@Column(name = "notice_id")
	public Long getNoticeId() {
		return noticeId;
	}

	public void setNoticeId(Long noticeId) {
		this.noticeId = noticeId;
	}

	@Column(name = "merchant_id")
	public Long getMerchantId() {
		return merchantId;
	}

	public void setMerchantId(Long merchantId) {
		this.merchantId = merchantId;
	}
}
