package com.miqtech.master.entity.activity;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.google.common.collect.Lists;
import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "activity_over_activity_module")
public class ActivityOverActivityModule extends IdEntity {

	private static final long serialVersionUID = -3147399690166450909L;

	private Long pid;// 0为一级,非0为上级的ID
	private Integer type;// 模块类型:1-资讯,2-视频
	private String img;
	private String name;
	private Integer orderNum;// 排序序号,小的在前

	private List<ActivityOverActivityModule> children;// 子模块

	@Column(name = "pid")
	public Long getPid() {
		return pid;
	}

	public void setPid(Long pid) {
		this.pid = pid;
	}

	@Column(name = "type")
	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	@Column(name = "img")
	public String getImg() {
		return img;
	}

	public void setImg(String img) {
		this.img = img;
	}

	@Column(name = "name")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "order_num")
	public Integer getOrderNum() {
		return orderNum;
	}

	public void setOrderNum(Integer orderNum) {
		this.orderNum = orderNum;
	}

	@Transient
	public List<ActivityOverActivityModule> getChildren() {
		return children;
	}

	public void setChildren(List<ActivityOverActivityModule> children) {
		this.children = children;
	}

	public void addChildren(ActivityOverActivityModule m) {
		List<ActivityOverActivityModule> c = getChildren();
		if (c == null) {
			c = Lists.newArrayList();
			setChildren(c);
		}
		c.add(m);
	}
}
