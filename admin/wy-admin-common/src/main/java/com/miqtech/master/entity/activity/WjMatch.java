package com.miqtech.master.entity.activity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "wj_match")
public class WjMatch extends IdEntity {
	private static final long serialVersionUID = -8066718447057151266L;
	private Long itemId;// 比赛项目id
	private String matchName;// 比赛名称
	private String organizer;// 承办机构
	private String time;// 比赛时间
	private String address;// 比赛地点
	private Integer need;// 赛点需求量
	private String remark;// 比赛介绍

	@Column(name = "item_id")
	public Long getItemId() {
		return itemId;
	}

	public void setItemId(Long itemId) {
		this.itemId = itemId;
	}

	@Column(name = "match_name")
	public String getMatchName() {
		return matchName;
	}

	public void setMatchName(String matchName) {
		this.matchName = matchName;
	}

	@Column(name = "organizer")
	public String getOrganizer() {
		return organizer;
	}

	public void setOrganizer(String organizer) {
		this.organizer = organizer;
	}

	@Column(name = "time")
	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	@Column(name = "address")
	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	@Column(name = "need")
	public Integer getNeed() {
		return need;
	}

	public void setNeed(Integer need) {
		this.need = need;
	}

	@Column(name = "remark")
	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

}
