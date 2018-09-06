package com.miqtech.master.entity.bounty;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

/**
 * 悬赏令审核成绩物品
 */
@Entity
@Table(name = "bounty_grade")
public class BountyGrade extends IdEntity {

	private static final long serialVersionUID = -4953892895172195002L;
	private Long bountyId;// 悬赏令id
	private Long userId;//领奖用户id
	private String img;// 成绩图片
	private String remark;//成绩说明
	private Integer grade;// 审核后的分数
	private Integer state;// 审核状态:1-待审核,2-审核拒绝,3-审核通过,4-审核通过但成绩弃用
	private String serialNo;//序列化

	@Column(name = "bounty_id")
	public Long getBountyId() {
		return bountyId;
	}

	public void setBountyId(Long bountyId) {
		this.bountyId = bountyId;
	}

	@Column(name = "img")
	public String getImg() {
		return img;
	}

	public void setImg(String img) {
		this.img = img;
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
	@Column(name = "grade")
	public Integer getGrade() {
		return grade;
	}

	public void setGrade(Integer grade) {
		this.grade = grade;
	}

	public void setState(Integer state) {
		this.state = state;
	}

	@Column(name = "serial_no")
	public String getSerialNo() {
		return serialNo;
	}

	public void setSerialNo(String serialNo) {
		this.serialNo = serialNo;
	}

	@Column(name = "user_id")
	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

}
