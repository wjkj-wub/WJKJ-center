package com.miqtech.master.entity.cohere;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "cohere_prize")
public class CoherePrize extends IdEntity {

	private static final long serialVersionUID = 4873308618685394761L;

	@Column(name="activity_id")
	private Long activityId;

	@Column(name = "name")
	private String name;

	@Column(name = "description")
	private String description;

	@Column(name = "num")
	private Integer num;

	@Column(name = "probability")
	private Byte probability;

	@Column(name = "value")
	private Double value;

	@Column(name = "type")
	private Byte type;
	
	@Column(name="url_crosswise")
	private String urlCrosswise;
	
	@Column(name="url_vertical")
	private String urlVertical;
	
	@Column(name="counts")
	private Integer counts;
	

	public Long getActivityId() {
		return activityId;
	}

	public void setActivityId(Long activityId) {
		this.activityId = activityId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getNum() {
		return num;
	}

	public void setNum(Integer num) {
		this.num = num;
	}

	public Byte getProbability() {
		return probability;
	}

	public void setProbability(Byte probability) {
		this.probability = probability;
	}

	public Double getValue() {
		return value;
	}

	public void setValue(Double value) {
		this.value = value;
	}

	public Byte getType() {
		return type;
	}

	public void setType(Byte type) {
		this.type = type;
	}

	public String getUrlCrosswise() {
		return urlCrosswise;
	}

	public void setUrlCrosswise(String urlCrosswise) {
		this.urlCrosswise = urlCrosswise;
	}

	public String getUrlVertical() {
		return urlVertical;
	}

	public void setUrlVertical(String urlVertical) {
		this.urlVertical = urlVertical;
	}

	public Integer getCounts() {
		return counts;
	}

	public void setCounts(Integer counts) {
		this.counts = counts;
	}
	
}
