package com.miqtech.master.entity.common;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "sys_t_area")
@JsonIgnoreProperties({ "pid", "level", "orderId", "updateUserId", "updateDate", "createUserId", "createDate", "valid" })
public class SystemArea extends IdEntity {
	private static final long serialVersionUID = -6631376791474032618L;
	private String name;// 地区名称
	private Long pid;// 上级ID
	private Integer orderId;// 排序
	private String areaCode;
	private Integer level;// 级别
	private String pinyin;// 地区名称对应的拼音

	private List<SystemArea> children;

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

	@Column(name = "order_id")
	public Integer getOrderId() {
		return orderId;
	}

	public void setOrderId(Integer orderId) {
		this.orderId = orderId;
	}

	@Column(name = "area_code")
	public String getAreaCode() {
		return areaCode;
	}

	public void setAreaCode(String areaCode) {
		this.areaCode = areaCode;
	}

	@Column(name = "level")
	public Integer getLevel() {
		return level;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}

	@Transient
	public List<SystemArea> getChildren() {
		return children;
	}

	public void setChildren(List<SystemArea> children) {
		this.children = children;
	}

	public void addChild(SystemArea area) {
		if (this.children == null) {
			this.children = new ArrayList<SystemArea>();
		}

		this.children.add(area);
	}

	@Column(name = "pinyin")
	public String getPinyin() {
		return pinyin;
	}

	public void setPinyin(String pinyin) {
		this.pinyin = pinyin;
	}

}
