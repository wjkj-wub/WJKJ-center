package com.miqtech.master.entity.amuse;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "amuse_reward_progress")
public class AmuseRewardProgress extends IdEntity implements Cloneable {
	private static final long serialVersionUID = 520951024900341185L;
	private Long targetId;//目标ID
	private Integer type;//1审核2申诉
	private Integer state;//0提交成功1待审核2审核通过3审核不通过4已发放5申诉待处理6申诉已处理7申诉驳回8结束9待发放
	private String remark;//备注
	private Long activityId;
	private Long userId;
	private Long sysUserId;// 操作的系统用户ID

	@Column(name = "target_id")
	public Long getTargetId() {
		return targetId;
	}

	public void setTargetId(Long targetId) {
		this.targetId = targetId;
	}

	@Column(name = "type")
	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	@Column(name = "state")
	public Integer getState() {
		return state;
	}

	public void setState(Integer state) {
		this.state = state;
	}

	@Column(name = "remark")
	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	@Column(name = "activity_id")
	public Long getActivityId() {
		return activityId;
	}

	public void setActivityId(Long activityId) {
		this.activityId = activityId;
	}

	@Column(name = "user_id")
	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	@Column(name = "sys_user_id")
	public Long getSysUserId() {
		return sysUserId;
	}

	public void setSysUserId(Long sysUserId) {
		this.sysUserId = sysUserId;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return (AmuseRewardProgress) super.clone();
	}

}
