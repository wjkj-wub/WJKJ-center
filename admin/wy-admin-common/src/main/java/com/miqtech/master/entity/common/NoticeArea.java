package com.miqtech.master.entity.common;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "sys_r_notice_area")
public class NoticeArea extends IdEntity {
	private static final long serialVersionUID = 6848144236819767499L;
	private Long noticeId;//公告ID
	private String areaCode;//地区code

	@Column(name = "notice_id")
	public Long getNoticeId() {
		return noticeId;
	}

	public void setNoticeId(Long noticeId) {
		this.noticeId = noticeId;
	}

	@Column(name = "area_code")
	public String getAreaCode() {
		return areaCode;
	}

	public void setAreaCode(String areaCode) {
		this.areaCode = areaCode;
	}

}
