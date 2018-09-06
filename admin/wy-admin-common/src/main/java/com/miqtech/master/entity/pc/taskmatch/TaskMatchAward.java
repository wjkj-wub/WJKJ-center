package com.miqtech.master.entity.pc.taskmatch;

import javax.persistence.*;
import java.util.Date;

/**
 * 任务赛奖励发放信息表
 *
 * @author gaohanlin
 * @create 2017年08月30日
 */
@Entity
@Table(name = "pc_task_match_award")
public class TaskMatchAward {
	@Id
	@Column(name = "id", nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(name = "user_id")
	private Long userId; //用户id
	@Column(name = "task_id")
	private Long taskId; //任务id
	@Column(name = "task_type")
	private Byte taskType; //任务赛类型：1-排行榜，2-主题
	@Column(name = "phone")
	private String phone; //用户手机号码
	@Column(name = "alipay")
	private String alipay; //支付宝账号
	@Column(name = "fee_type")
	private Byte feeType; // 费用类型：1-积分，2-娱币，3-人民币
	@Column(name = "fee_amount")
	private Integer feeAmount; // 费用数量
	@Column(name = "trade_no")
	private String tradeNo; //订单号
	@Column(name = "status")
	private Byte status; //发放状态：0-未发放，1-发放中，2-已发放
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

	public Byte getTaskType() {
		return taskType;
	}

	public void setTaskType(Byte taskType) {
		this.taskType = taskType;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getAlipay() {
		return alipay;
	}

	public void setAlipay(String alipay) {
		this.alipay = alipay;
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

	public Byte getStatus() {
		return status;
	}

	public void setStatus(Byte status) {
		this.status = status;
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
