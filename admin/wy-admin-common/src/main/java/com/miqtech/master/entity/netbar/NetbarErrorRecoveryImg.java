package com.miqtech.master.entity.netbar;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "netbar_error_recovery_img")
public class NetbarErrorRecoveryImg extends IdEntity {

	private static final long serialVersionUID = 4786546664423296774L;
	private Long recoveryId;// '纠错信息id',
	private String url;//图片地址,

	@Column(name = "recovery_id")
	public Long getRecoveryId() {
		return recoveryId;
	}

	public void setRecoveryId(Long recoveryId) {
		this.recoveryId = recoveryId;
	}

	@Column(name = "url")
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
