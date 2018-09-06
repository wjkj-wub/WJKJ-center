package com.miqtech.master.entity.amuse;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.miqtech.master.entity.IdEntity;
import com.miqtech.master.entity.user.UserInfo;

/**
 * 娱乐赛审核
 */
@Entity
@Table(name = "amuse_t_verify")
public class AmuseVerify extends IdEntity {

	private static final long serialVersionUID = -8278692341011610119L;

	private Long activityId;// 娱乐赛ID
	private Long userId;// 提交审核申请的用户ID
	private String serial;// 编号
	private String describes;// 审核内容
	private String remark;// 发放备注信息
	private Integer state;// 审核状态：0-待审核,1-审核中,2-审核拒绝,3-审核通过,4-已发放(新:1-待审核,2-审核拒绝,3-审核通过,4-已发放5-结束6-拒绝后申诉通过)
	private Long claimUserId;// 认领人ID，为0表示系统自动发放
	private Date finishDate;
	private Integer isSpecial;// 是否异常,默认0,充值失败或加入黑名单是标记为1

	private List<AmuseVerifyImg> imgs;// 审核图片
	private AmuseActivityInfo activityInfo;// 赛事信息
	private UserInfo userInfo;
	private AmuseActivityRecord activityRecord;// 报名信息

	@Column(name = "activity_id")
	public Long getActivityId() {
		return activityId;
	}

	public void setActivityId(Long activityId) {
		this.activityId = activityId;
	}

	@Column(name = "claim_user_id")
	public Long getClaimUserId() {
		return claimUserId;
	}

	public void setClaimUserId(Long claimUserId) {
		this.claimUserId = claimUserId;
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

	@Column(name = "remark")
	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	@Column(name = "state")
	public Integer getState() {
		return state;
	}

	public void setState(Integer state) {
		this.state = state;
	}

	@Column(name = "finish_date")
	public Date getFinishDate() {
		return finishDate;
	}

	public void setFinishDate(Date finishDate) {
		this.finishDate = finishDate;
	}

	@Column(name = "is_special")
	public Integer getIsSpecial() {
		return isSpecial;
	}

	public void setIsSpecial(Integer isSpecial) {
		this.isSpecial = isSpecial;
	}

	@Transient
	public List<AmuseVerifyImg> getImgs() {
		return imgs;
	}

	public void setImgs(List<AmuseVerifyImg> imgs) {
		this.imgs = imgs;
	}

	@Transient
	public AmuseActivityInfo getActivityInfo() {
		return activityInfo;
	}

	public void setActivityInfo(AmuseActivityInfo activityInfo) {
		this.activityInfo = activityInfo;
	}

	@Transient
	public UserInfo getUserInfo() {
		return userInfo;
	}

	public void setUserInfo(UserInfo userInfo) {
		this.userInfo = userInfo;
	}

	@Transient
	public AmuseActivityRecord getActivityRecord() {
		return activityRecord;
	}

	public void setActivityRecord(AmuseActivityRecord activityRecord) {
		this.activityRecord = activityRecord;
	}

}
