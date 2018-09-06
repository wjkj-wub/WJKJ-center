package com.miqtech.master.entity.user;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "user_r_favor")
public class UserFavor extends IdEntity {

	private static final long serialVersionUID = 109766993531201399L;
	private Long userId;// 用户ID
	private Long subId;// 收藏项目的ID
	private Integer type;// 收藏的类别:1-网吧;2-游戏;3-官方赛事;4-赛事资讯;5娱乐赛事

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

}
