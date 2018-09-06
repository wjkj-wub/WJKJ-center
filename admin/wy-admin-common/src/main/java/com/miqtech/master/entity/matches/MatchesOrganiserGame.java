package com.miqtech.master.entity.matches;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "matches_organiser_game")
public class MatchesOrganiserGame extends IdEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8917087365357428497L;

	@Column(name = "organiser_id")
	private Long organiserId;

	@Column(name = "items_id")
	private Long itemsId;

	public Long getOrganiserId() {
		return organiserId;
	}

	public void setOrganiserId(Long organiserId) {
		this.organiserId = organiserId;
	}

	public Long getItemsId() {
		return itemsId;
	}

	public void setItemsId(Long itemsId) {
		this.itemsId = itemsId;
	}

}
