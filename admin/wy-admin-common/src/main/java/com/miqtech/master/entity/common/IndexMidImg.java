package com.miqtech.master.entity.common;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

/**
 * app首页腰图
 *
 */
@Entity
@Table(name = "index_mid_img")
public class IndexMidImg extends IdEntity {
	private static final long serialVersionUID = -7270827279451534728L;
	private Integer type;//1官方赛2娱乐赛3约战4推广5福利
	private Long targetId;//腰图1的赛事或娱乐赛id
	private String img;//图片地址
	private String url;//链接地址
	private Integer sort;//排序(数字越大,显示越靠前)
	private Integer category;//1腰图一2腰图二
	private String areaCode;//地区code

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

	@Column(name = "img")
	public String getImg() {
		return img;
	}

	public void setImg(String img) {
		this.img = img;
	}

	@Column(name = "url")
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Column(name = "sort")
	public Integer getSort() {
		return sort;
	}

	public void setSort(Integer sort) {
		this.sort = sort;
	}

	@Column(name = "category")
	public Integer getCategory() {
		return category;
	}

	public void setCategory(Integer category) {
		this.category = category;
	}

	@Column(name = "area_code")
	public String getAreaCode() {
		return areaCode;
	}

	public void setAreaCode(String areaCode) {
		this.areaCode = areaCode;
	}

}
