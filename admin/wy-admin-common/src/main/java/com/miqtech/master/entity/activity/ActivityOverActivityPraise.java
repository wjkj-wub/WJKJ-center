package com.miqtech.master.entity.activity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "activity_over_activity_praise")
public class ActivityOverActivityPraise extends IdEntity {

	private static final long serialVersionUID = 3062176080645826723L;
	private Long activityOverActivityId;// 资讯ID
	private Long userId;//点赞用户id

	@Column(name = "activity_over_activity_id")
	public Long getActivityOverActivityId() {
		return activityOverActivityId;
	}

	public void setActivityOverActivityId(Long activityOverActivityId) {
		this.activityOverActivityId = activityOverActivityId;
	}

	@Column(name = "user_id")
	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}
}
