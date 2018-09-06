package com.miqtech.master.entity.amuse;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.miqtech.master.entity.IdEntity;

/**
 * 娱乐赛申诉
 */
@Entity
@Table(name = "amuse_t_appeal")
public class AmuseAppeal extends IdEntity {

	private static final long serialVersionUID = -5100893270251997468L;

	private Long activityId;// 娱乐赛ID
	private Long userId;// 提交审核申请的用户ID
	private String serial;// 订单编号
	private String describes;// 审核内容
	private Integer state;// 审核状态：0-审核中,1-审核拒绝,2-审核通过(新:0待审核1-审核拒绝,2-审核通过)
	private String remark;
	private Long categoryId;//申诉类目id(amuse_msg_feedback的id)

	private List<AmuseAppealImg> imgs;// 图片信息

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

	@Column(name = "serial")
	public String getSerial() {
		return serial;
	}

	public void setSerial(String serial) {
		this.serial = serial;
	}

	@Column(name = "describes")
	public String getDescribes() {
		return describes;
	}

	public void setDescribes(String describes) {
		this.describes = describes;
	}

	@Column(name = "state")
	public Integer getState() {
		return state;
	}

	public void setState(Integer state) {
		this.state = state;
	}

	@Transient
	public List<AmuseAppealImg> getImgs() {
		return imgs;
	}

	public void setImgs(List<AmuseAppealImg> imgs) {
		this.imgs = imgs;
	}

	@Column(name = "remark")
	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	@Column(name = "category_id")
	public Long getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(Long categoryId) {
		this.categoryId = categoryId;
	}

}
