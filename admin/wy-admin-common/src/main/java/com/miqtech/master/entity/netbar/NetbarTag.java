package com.miqtech.master.entity.netbar;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

@Table(name = "netbar_tag")
@Entity
public class NetbarTag extends IdEntity {

	private static final long serialVersionUID = 8657893465417539885L;

	@Column(name = "name")
	private String name;

	@Column(name = "level")
	private Integer level;

	@Column(name = "is_publish")
	private Integer isPublish;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getLevel() {
		return level;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}

	public Integer getIsPublish() {
		return isPublish;
	}

	public void setIsPublish(Integer isPublish) {
		this.isPublish = isPublish;
	}

}
