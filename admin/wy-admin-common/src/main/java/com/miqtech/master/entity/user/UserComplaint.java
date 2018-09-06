package com.miqtech.master.entity.user;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

/**
 * 用户举报
 *
 */
@Entity
@Table(name = "user_r_complaint")
public class UserComplaint extends IdEntity {

	private static final long serialVersionUID = 109766993531201399L;
	private Long userId;// 用户ID
	private Long subId;// 举报项目id
	private Integer type;// 举报类别:1.用户2.评论3.约战4网吧评论5官方赛/娱乐赛评论
	private Integer category;
	private String remark;//说明

	@Column(name = "user_id")
	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	@Column(name = "sub_id")
	public Long getSubId() {
		return subId;
	}

	public void setSubId(Long subId) {
		this.subId = subId;
	}

	@Column(name = "type")
	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	@Column(name = "remark")
	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	@Column(name = "category")
	public Integer getCategory() {
		return category;
	}

	public void setCategory(Integer category) {
		this.category = category;
	}

}
