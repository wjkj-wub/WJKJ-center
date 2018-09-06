package com.miqtech.master.entity.website;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

/*
 * 官网视频、游戏链接
 */
@Entity
@Table(name = "official_website_t_link")
public class OfficialWebsiteLink extends IdEntity {
	private static final long serialVersionUID = 6848144236819767499L;
	private Integer type;//类型：1-电竞视频，2-H5游戏
	private String title;//标题
	private String icon;//图片地址
	private String url;//链接地址

	@Column(name = "type")
	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	@Column(name = "icon")
	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	@Column(name = "title")
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Column(name = "url")
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
