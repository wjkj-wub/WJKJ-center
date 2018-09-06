package com.miqtech.master.entity.common;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

/**
 * 首页热门
 *
 */
@Entity
@Table(name = "index_hot")
public class IndexHot extends IdEntity {
	private static final long serialVersionUID = -749143360062367063L;
	private Integer type;//1热门赛事2热门娱乐赛3热门约战4竞技大厅卡片转动区官方赛5竞技大厅卡片转动区娱乐赛6官方活动推荐7娱乐比赛推荐
	private Long targetId;//赛事或娱乐赛或约战id
	private Integer sort;//排序(数字越大,显示越靠前)
	private String areaCode;//区域code

	@Column(name = "type")
	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	@Column(name = "target_id")
	public Long getTargetId() {
		return targetId;
	}

	public void setTargetId(Long targetId) {
		this.targetId = targetId;
	}

	@Column(name = "sort")
	public Integer getSort() {
		return sort;
	}

	public void setSort(Integer sort) {
		this.sort = sort;
	}

	@Column(name = "area_code")
	public String getAreaCode() {
		return areaCode;
	}

	public void setAreaCode(String areaCode) {
		this.areaCode = areaCode;
	}

}
