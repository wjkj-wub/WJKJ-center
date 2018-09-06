package com.miqtech.master.entity.amuse;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "amuse_r_award_type")
public class AmuseAwardType extends IdEntity {
	private static final long serialVersionUID = 8267513150549872553L;

	private Integer type; //类型，常量维护
	private String name; //奖励类型名称

	@Column(name = "type")
	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	@Column(name = "name")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
