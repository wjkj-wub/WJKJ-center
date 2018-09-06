package com.miqtech.master.entity.pc.taskmatch;

import javax.persistence.*;
import java.util.Date;

/**
 * 任务赛费用记录实体
 *
 * @author zhangyuqi
 * @create 2017年08月31日
 */
@Entity
@Table(name = "pc_task_match_fee")
public class TaskMatchFee {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(name = "user_id")
	private Long userId;
	@Column(name = "task_id")
	private Long taskId; // 任务赛ID
	@Column(name = "task_type")
	private Byte taskType; // 任务赛类型：1-排行榜，2-主题
	@Column(name = "record_type")
	private Byte recordType; // 费用记录类型：1-报名，2-奖励，3-退费，4-更新主题任务
	@Column(name = "fee_type")
	private Byte feeType; // 费用类型：1-积分，2-娱币，3-人民币
	@Column(name = "fee_amount")
	private Integer feeAmount; // 费用值
	@Column(name = "trade_no")
	private String tradeNo; // 订单号
	@Column(name = "is_valid")
	private Byte isValid;
	@Column(name = "create_date")
	private Date createDate;

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

	public Byte getTaskType() {
		return taskType;
	}

	public void setTaskType(Byte taskType) {
		this.taskType = taskType;
	}

	public Byte getRecordType() {
		return recordType;
	}

	public void setRecordType(Byte recordType) {
		this.recordType = recordType;
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

	public String getTradeNo() {
		return tradeNo;
	}

	public void setTradeNo(String tradeNo) {
		this.tradeNo = tradeNo;
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

}
