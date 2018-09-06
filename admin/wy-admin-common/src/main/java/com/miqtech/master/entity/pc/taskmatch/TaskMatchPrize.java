package com.miqtech.master.entity.pc.taskmatch;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * 任务赛奖项表
 *
 * @author gaohanlin
 * @create 2017年08月30日
 */
@Entity
@Table(name = "pc_task_match_prize")
public class TaskMatchPrize implements Serializable {
	private static final long serialVersionUID = -7136178479053060191L;
	@Id
	@Column(name = "id", nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(name = "task_id")
	private Long taskId; //任务id
	@Column(name = "award_level")
	private Byte awardLevel; //奖品档次
	@Column(name = "award_type")
	private Byte awardType; //奖励类型：1-积分，2-娱币，3-人民币
	@Column(name = "award_amount")
	private Integer awardAmount; //奖励金额
	@Column(name = "rank_start")
	private Integer rankStart; //起始名次
	@Column(name = "rank_end")
	private Integer rankEnd; //截至名次
	@Column(name = "is_valid")
	private Byte isValid;
	@Column(name = "create_date") //创建时间
	private Date createDate;
	@Column(name = "update_date") //更新时间
	private Date updateDate;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getTaskId() {
		return taskId;
	}

	public void setTaskId(Long taskId) {
		this.taskId = taskId;
	}

	public Byte getAwardLevel() {
		return awardLevel;
	}

	public void setAwardLevel(Byte awardLevel) {
		this.awardLevel = awardLevel;
	}

	public Byte getAwardType() {
		return awardType;
	}

	public void setAwardType(Byte awardType) {
		this.awardType = awardType;
	}

	public Integer getAwardAmount() {
		return awardAmount;
	}

	public void setAwardAmount(Integer awardAmount) {
		this.awardAmount = awardAmount;
	}

	public Integer getRankStart() {
		return rankStart;
	}

	public void setRankStart(Integer rankStart) {
		this.rankStart = rankStart;
	}

	public Integer getRankEnd() {
		return rankEnd;
	}

	public void setRankEnd(Integer rankEnd) {
		this.rankEnd = rankEnd;
	}

	public Byte getIsValid() {
		return isValid;
	}

	public void setIsValid(Byte isValid) {
		this.isValid = isValid;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}

}
