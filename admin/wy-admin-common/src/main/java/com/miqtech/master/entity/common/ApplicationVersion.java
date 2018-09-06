package com.miqtech.master.entity.common;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.miqtech.master.entity.IdEntity;

/**
 * APP版本-用于手机端应用更新检测
 */

@Entity
@Table(name = "application_t_version")
@JsonIgnoreProperties({ "createDate", "createUserId", "updateDate", "updateUserId", "valid" })
public class ApplicationVersion extends IdEntity {
	private static final long serialVersionUID = -4969961128263546027L;
	private int versionCode;//仅用于android进行版本校验
	private String version;// 版本号
	private String url;// 下载链接
	private Integer isCoercive;// 是否强制：1-是；0-否；
	private Integer type;// 类型：(客户端)1-安卓；2-IOS；(商户端)3-安卓；4-IOS；
	private int hiddenElement;// 仅用于ios的功能隐藏,是否有要隐藏的功能0:无1:有
	private String patchCode;
	private String patchUrl;
	private Integer systemType;

	@Column(name = "version")
	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	@Column(name = "is_coercive")
	public Integer getIsCoercive() {
		return isCoercive;
	}

	public void setIsCoercive(Integer isCoercive) {
		this.isCoercive = isCoercive;
	}

	@Column(name = "type")
	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	@Column(name = "url")
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Column(name = "version_code")
	public int getVersionCode() {
		return versionCode;
	}

	public void setVersionCode(int versionCode) {
		this.versionCode = versionCode;
	}

	@Column(name = "hidden_element")
	public int getHiddenElement() {
		return hiddenElement;
	}

	public void setHiddenElement(int hiddenElement) {
		this.hiddenElement = hiddenElement;
	}

	@Column(name = "patch_code")
	public String getPatchCode() {
		return patchCode;
	}

	public void setPatchCode(String patchCode) {
		this.patchCode = patchCode;
	}

	@Column(name = "patch_url")
	public String getPatchUrl() {
		return patchUrl;
	}

	public void setPatchUrl(String patchUrl) {
		this.patchUrl = patchUrl;
	}

	@Column(name = "system_type")
	public Integer getSystemType() {
		return systemType;
	}

	public void setSystemType(Integer systemType) {
		this.systemType = systemType;
	}

}
