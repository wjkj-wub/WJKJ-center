package com.miqtech.master.entity.cohere;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "cohere_activity")
public class CohereActivity extends IdEntity {

	private static final long serialVersionUID = -1400375306621643060L;
	@Column(name = "title")
	private String title;
	@Column(name = "award_description")
	private String awardDescription;
	@Column(name = "rule")
	private String rule;
	@Column(name = "state")
	private Integer state;
	@Column(name = "begin_time")
	private Date beginTime;
	@Column(name = "end_time")
	private Date endTime;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAwardDescription() {
		return awardDescription;
	}

	public void setAwardDescription(String awardDescription) {
		this.awardDescription = awardDescription;
	}

	public String getRule() {
		return rule;
	}

	public void setRule(String rule) {
		this.rule = rule;
	}

	public Integer getState() {
		return state;
	}

	public void setState(Integer state) {
		this.state = state;
	}

	public Date getBeginTime() {
		return beginTime;
	}

	public void setBeginTime(Date beginTime) {
		this.beginTime = beginTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

}
