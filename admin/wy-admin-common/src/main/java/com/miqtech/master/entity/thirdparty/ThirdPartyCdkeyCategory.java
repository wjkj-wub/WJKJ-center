package com.miqtech.master.entity.thirdparty;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "third_party_cdkey_category")
public class ThirdPartyCdkeyCategory extends IdEntity {

	private static final long serialVersionUID = 5379129695496654849L;

	private String name;

	@Column(name = "name")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
