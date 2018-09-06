package com.miqtech.master.entity.website;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

/*
 * 官网动态新闻、精彩活动、轮播
 */
@Entity
@Table(name = "official_website_t_dynamic")
public class OfficialWebsiteDynamic extends IdEntity {
	private static final long serialVersionUID = 6848144236819767499L;
	private Integer type;//类型：0-外部链接的Banner，1-Banner，2-最新赛事，3-精彩活动，4-行业动态，5-资讯新闻，6-往期赛事
	private String title;//标题
	private String summary;//摘要
	private String content;//内容
	private String icon;//图片地址
	private Integer count;//阅读次数统计

	@Column(name = "count")
	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}
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

	@Column(name = "summary")
	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	@Column(name = "content")
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

}
