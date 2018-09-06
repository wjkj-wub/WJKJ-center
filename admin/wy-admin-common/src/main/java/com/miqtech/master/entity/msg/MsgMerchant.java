package com.miqtech.master.entity.msg;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "msg_t_merchant")
public class MsgMerchant extends IdEntity implements Comparable<MsgMerchant> {
	private static final long serialVersionUID = -8503110525241344122L;
	private Long merchantId;
	private Integer type;// 消息类别
	private String content;// 消息内容
	private Integer isRead;// 是否已读:0-false 1-true

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

	@Column(name = "is_read")
	public Integer getIsRead() {
		return isRead;
	}

	public void setIsRead(Integer isRead) {
		this.isRead = isRead;
	}

	@Column(name = "merchant_id")
	public Long getMerchantId() {
		return merchantId;
	}

	public void setMerchantId(Long merchantId) {
		this.merchantId = merchantId;
	}

	@Override
	public int compareTo(MsgMerchant o) {
		Date createDate1 = this.getCreateDate();
		Date createDate2 = o.getCreateDate();
		if (createDate1.before(createDate2)) {
			return -1;
		} else {
			return 1;
		}
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}

}
