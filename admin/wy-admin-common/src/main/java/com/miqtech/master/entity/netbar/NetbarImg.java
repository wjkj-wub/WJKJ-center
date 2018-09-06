package com.miqtech.master.entity.netbar;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "netbar_r_imgs")
public class NetbarImg extends IdEntity {
	private static final long serialVersionUID = 8146537034851032879L;
	private Long netbarId;// 网吧ID
	private Long tmpNetbarId;// 临时网吧ID
	private String url;// 图片url
	private String urlMedia;// 图片（中）
	private String urlThumb;// 图片（小）
	private Integer verified;//是否验证通过

	@Column(name = "netbar_id")
	public Long getNetbarId() {
		return netbarId;
	}

	public void setNetbarId(Long netbarId) {
		this.netbarId = netbarId;
	}

	@Column(name = "url")
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Column(name = "url_media")
	public String getUrlMedia() {
		return urlMedia;
	}

	public void setUrlMedia(String urlMedia) {
		this.urlMedia = urlMedia;
	}

	@Column(name = "url_thumb")
	public String getUrlThumb() {
		return urlThumb;
	}

	public void setUrlThumb(String urlThumb) {
		this.urlThumb = urlThumb;
	}

	@Column(name = "tmp_netbar_id")
	public Long getTmpNetbarId() {
		return tmpNetbarId;
	}

	public void setTmpNetbarId(Long tmpNetbarId) {
		this.tmpNetbarId = tmpNetbarId;
	}

	@Column(name = "verified")
	public Integer getVerified() {
		return verified;
	}

	public void setVerified(Integer verified) {
		this.verified = verified;
	}

}