package com.miqtech.master.entity.common;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.miqtech.master.entity.IdEntity;

/**
 * app首页动态入口
 *
 */
@Entity
@Table(name = "app_dynamic_entry")
@JsonIgnoreProperties({ "createDate", "createUserId", "updateDate", "updateUserId", "valid", "isShow" })
public class AppDynamicEntry extends IdEntity {
	private static final long serialVersionUID = -1063823396131037557L;
	private String title;//标题
	private String icon;//图标
	private Integer type;//1附近网吧2热点资讯3金币商城4我的红包
	private Integer isShow;//1显示0不显示
	private Integer sort;//排序(数字越大,显示越靠前)

	@Column(name = "title")
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Column(name = "icon")
	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	@Column(name = "type")
	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	@Column(name = "is_show")
	public Integer getIsShow() {
		return isShow;
	}

	public void setIsShow(Integer isShow) {
		this.isShow = isShow;
	}

	@Column(name = "sort")
	public Integer getSort() {
		return sort;
	}

	public void setSort(Integer sort) {
		this.sort = sort;
	}

}
