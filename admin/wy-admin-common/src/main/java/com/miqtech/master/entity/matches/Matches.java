package com.miqtech.master.entity.matches;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "matches")
public class Matches extends IdEntity {

	private static final long serialVersionUID = -5801359168984845248L;

	@Column(name = "title")
	private String title;

	@Column(name = "items_id")
	private Long itemsId;

	@Column(name = "organiser_id")
	private Long organiserId;

	@Column(name = "league_id")
	private Long leagueId;

	@Column(name = "prize")
	private String prize;

	@Column(name = "summary")
	private String summary;

	@Column(name = "rule")
	private String rule;

	@Column(name = "is_draft")
	private Integer isDraft;

	@Column(name = "reward")
	private String reward;

	@Column(name = "state")
	private Integer state;

	@Column(name = "icon")
	private String icon;

	@Column(name = "video")
	private String video;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

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

	public Long getLeagueId() {
		return leagueId;
	}

	public void setLeagueId(Long leagueId) {
		this.leagueId = leagueId;
	}

	public String getPrize() {
		return prize;
	}

	public void setPrize(String prize) {
		this.prize = prize;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getRule() {
		return rule;
	}

	public void setRule(String rule) {
		this.rule = rule;
	}

	public String getReward() {
		return reward;
	}

	public void setReward(String reward) {
		this.reward = reward;
	}

	public Integer getState() {
		return state;
	}

	public void setState(Integer state) {
		this.state = state;
	}

	public Integer getIsDraft() {
		return isDraft;
	}

	public void setIsDraft(Integer isDraft) {
		this.isDraft = isDraft;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getVideo() {
		return video;
	}

	public void setVideo(String video) {
		this.video = video;
	}

}
