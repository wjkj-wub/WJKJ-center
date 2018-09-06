package com.miqtech.master.entity.netbar;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "netbar_r_evaluation_imgs")
@JsonIgnoreProperties({ "createDate", "createUserId", "updateDate", "updateUserId", "valid" })
public class NetbarEvaluationImg extends IdEntity {

	private static final long serialVersionUID = 8385317154703046706L;

	private Long evaId;
	private String url;

	@Column(name = "eva_id")
	public Long getEvaId() {
		return evaId;
	}

	public void setEvaId(Long evaId) {
		this.evaId = evaId;
	}

	@Column(name = "url")
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
