package com.miqtech.master.entity.netbar.resource;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "netbar_commodity_category")
public class NetbarCommodityCategory extends IdEntity {
	private static final long serialVersionUID = 6848144236819767499L;
	private String name;//类目名称（自有或者第三方：红包或活动）
	private Long pid;
	private Integer isShowApp;//是否app展示,0-否，1-是

	@Column(name = "name")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "pid")
	public Long getPid() {
		return pid;
	}

	public void setPid(Long pid) {
		this.pid = pid;
	}

	@Column(name = "is_show_app")
	public Integer getIsShowApp() {
		return isShowApp;
	}

	public void setIsShowApp(Integer isShowApp) {
		this.isShowApp = isShowApp;
	}

}
