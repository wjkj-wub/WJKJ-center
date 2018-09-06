package com.miqtech.master.entity.matches;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "matches_league")
public class MatchesLeague extends IdEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6466080397376589450L;

	@Column(name = "items_id")
	private Long itemsId;

	@Column(name = "organiser_id")
	private Long organiserId;

	@Column(name = "name")
	private String name;

	@Column(name = "logo")
	private String logo;

	public Long getItemsId() {
		return itemsId;
	}

	public void setItemsId(Long itemsId) {
		this.itemsId = itemsId;
	}

	public Long getOrganiserId() {
		return organiserId;
	}

	public void setOrganiserId(Long organiserId) {
		this.organiserId = organiserId;
	}

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
