package com.miqtech.master.entity.cohere;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "cohere_debris_history")
public class CohereDebrisHistory extends IdEntity {

	private static final long serialVersionUID = 7277146985286591073L;
	@Column(name = "user_id")
	private Long userId;
	@Column(name = "draw_id")
	private Long drawId;
	@Column(name = "in_type")
	private Integer inType;
	@Column(name = "out_type")
	private Integer outType;
	@Column(name = "in_id")
	private Long inId;
	@Column(name = "out_id")
	private Long outId;
	@Column(name = "is_used")
	private Integer isUsed;
	@Column(name = "url_code")
	private String urlCode;
	@Column(name="debris_id")
	private Long debrisId;

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getDrawId() {
		return drawId;
	}

	public void setDrawId(Long drawId) {
		this.drawId = drawId;
	}

	public Integer getInType() {
		return inType;
	}

	public void setInType(Integer inType) {
		this.inType = inType;
	}

	public Integer getOutType() {
		return outType;
	}

	public void setOutType(Integer outType) {
		this.outType = outType;
	}
	
	public Long getInId() {
		return inId;
	}

	public void setInId(Long inId) {
		this.inId = inId;
	}

	public Long getOutId() {
		return outId;
	}

	public void setOutId(Long outId) {
		this.outId = outId;
	}

	public Integer getIsUsed() {
		return isUsed;
	}

	public void setIsUsed(Integer isUsed) {
		this.isUsed = isUsed;
	}

	public String getUrlCode() {
		return urlCode;
	}

	public void setUrlCode(String urlCode) {
		this.urlCode = urlCode;
	}

	public Long getDebrisId() {
		return debrisId;
	}

	public void setDebrisId(Long debrisId) {
		this.debrisId = debrisId;
	}

}
