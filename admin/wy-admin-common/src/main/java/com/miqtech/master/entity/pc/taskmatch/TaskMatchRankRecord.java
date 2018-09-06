package com.miqtech.master.entity.pc.taskmatch;

import javax.persistence.*;
import java.util.Date;

/**
 * 赛事排行榜记录表
 *
 * @author gaohanlin
 * @create 2017年08月30日
 */
@Entity
@Table(name = "pc_task_match_rank_record")
public class TaskMatchRankRecord {

	@Id
	@Column(name = "id", nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(name = "user_id")
	private Long userId; //用户id
	@Column(name = "task_id")
	private Long taskId; //任务id
	@Column(name = "game_nickname")
	private String gameNickname; // 
	@Column(name = "play_times")
	private Integer playTimes; //
	@Column(name = "cumulation_count")
	private Integer cumulationCount; //
	@Column(name = "fee_type")
	private Byte feeType; // 费用类型：1-积分，2-娱币，3-人民币
	@Column(name = "fee_amount")
	private Integer feeAmount; // 费用数量
	@Column(name = "is_valid")
	private Byte isValid;
	@Column(name = "create_date") //创建时间
	private Date createDate;
	@Column(name = "create_user_id")
	private Long createUserId;
	@Column(name = "update_date") //更新时间
	private Date updateDate;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getTaskId() {
		return taskId;
	}

	public void setTaskId(Long taskId) {
		this.taskId = taskId;
	}

	public String getGameNickname() {
		return gameNickname;
	}

	public void setGameNickname(String gameNickname) {
		this.gameNickname = gameNickname;
	}

	public Integer getPlayTimes() {
		return playTimes;
	}

	public void setPlayTimes(Integer playTimes) {
		this.playTimes = playTimes;
	}

	public Integer getCumulationCount() {
		return cumulationCount;
	}

	public void setCumulationCount(Integer cumulationCount) {
		this.cumulationCount = cumulationCount;
	}

	public Byte getFeeType() {
		return feeType;
	}

	public void setFeeType(Byte feeType) {
		this.feeType = feeType;
	}

	public Integer getFeeAmount() {
		return feeAmount;
	}

	public void setFeeAmount(Integer feeAmount) {
		this.feeAmount = feeAmount;
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

	public Long getCreateUserId() {
		return createUserId;
	}

	public void setCreateUserId(Long createUserId) {
		this.createUserId = createUserId;
	}

	public Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}

}
