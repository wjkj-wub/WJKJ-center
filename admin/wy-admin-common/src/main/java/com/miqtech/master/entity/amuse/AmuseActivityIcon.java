package com.miqtech.master.entity.amuse;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "amuse_r_activity_icon")
public class AmuseActivityIcon extends IdEntity {
	private static final long serialVersionUID = 8267513150549872553L;

	private Long activityId; //（娱乐赛）活动ID
	private String icon; //活动icon地址
	private Integer isMain; //是否为主图（列表显示）：0-否，1-是

	@Column(name = "activity_id")
	public Long getActivityId() {
		return activityId;
	}
	public void setActivityId(Long activityId) {
		this.activityId = activityId;
	}

	@Column(name = "icon")
	public String getIcon() {
		return icon;
	}
	public void setIcon(String icon) {
		this.icon = icon;
	}

	@Column(name = "is_main")
	public Integer getIsMain() {
		return isMain;
	}
	public void setIsMain(Integer isMain) {
		this.isMain = isMain;
	}
}
