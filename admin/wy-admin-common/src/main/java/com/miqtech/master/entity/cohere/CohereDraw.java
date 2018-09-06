package com.miqtech.master.entity.cohere;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "cohere_draw")
public class CohereDraw extends IdEntity {
	private static final long serialVersionUID = 1824334028023731930L;

	@Column(name = "user_id")
	private Long userId;
	@Column(name = "debris_id")
	private Long debrisId;
	@Column(name = "type")
	private Integer type;

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getDebrisId() {
		return debrisId;
	}

	public void setDebrisId(Long debrisId) {
		this.debrisId = debrisId;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

}
