package com.miqtech.master.entity.matches;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "matches_organiser")
public class MatchesOrganiser extends IdEntity {

	private static final long serialVersionUID = 312558822352304212L;

	@Column(name = "name")
	private String name;

	@Column(name = "logo")
	private String logo;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLogo() {
		return logo;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}

}
