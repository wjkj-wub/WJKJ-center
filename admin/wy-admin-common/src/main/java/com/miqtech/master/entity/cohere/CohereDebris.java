package com.miqtech.master.entity.cohere;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "cohere_debris")
public class CohereDebris extends IdEntity {
	private static final long serialVersionUID = 2219460145775975921L;

	@Column(name = "title")
	private String title;

	@Column(name = "activity_id")
	private Long activityId;

	@Column(name = "url")
	private String url;

	@Column(name = "remark")
	private String remark;

	@Column(name = "counts")
	private Integer counts;

	@Column(name = "num")
	private Integer num;

	@Column(name = "probability")
	private Integer probability;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Long getActivityId() {
		return activityId;
	}

	public void setActivityId(Long activityId) {
		this.activityId = activityId;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public Integer getCounts() {
		return counts;
	}

	public void setCounts(Integer counts) {
		this.counts = counts;
	}

	public Integer getNum() {
		return num;
	}

	public void setNum(Integer num) {
		this.num = num;
	}

	public Integer getProbability() {
		return probability;
	}

	public void setProbability(Integer probability) {
		this.probability = probability;
	}

}
