package com.miqtech.master.entity.pc.detail;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "pc_coin_detail")
public class CoinDetail {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/**
	 * 用户ID
	 */
	@Column(name = "user_id")
	private Long userId;

	/**
	 * 收入或消耗类型 
	         1 积分兑换娱币  2比赛消耗 3 兑换商品 4 抽奖 5 缺陷反馈  
	 */
	private Byte type;

	/**
	 * 兑换、抽奖编号或比赛id
	 */
	@Column(name = "trade_no")
	private String tradeNo;

	/**
	 * 消耗金额
	 */
	private Integer amount;

	/**
	 * -1 支出 1 收入
	 */
	private Byte direction;

	/**
	 * 状态
	 */
	private Byte state;

	@Column(name = "is_valid")
	private Byte isValid;

	@Column(name = "create_date")
	private Date createDate;

	@Column(name = "update_date")
	private Date updateDate;

	public CoinDetail() {

	}

	public CoinDetail(Long userId, Byte type, String tradeNo, Integer amount, Byte direction, Byte state, Byte isValid,
			Date createDate) {
		this.userId = userId;
		this.type = type;
		this.tradeNo = tradeNo;
		this.amount = amount;
		this.direction = direction;
		this.state = state;
		this.isValid = isValid;
		this.createDate = createDate;
	}

	/**
	 * @return id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * 获取用户ID
	 *
	 * @return user_id - 用户ID
	 */
	public Long getUserId() {
		return userId;
	}

	/**
	 * 设置用户ID
	 *
	 * @param userId 用户ID
	 */
	public void setUserId(Long userId) {
		this.userId = userId;
	}

	/**
	 * 获取收入或消耗类型 
	         1 积分兑换娱币  2比赛消耗 3 兑换商品 4 抽奖 5 缺陷反馈  
	 *
	 * @return type - 收入或消耗类型 
	         1 积分兑换娱币  2比赛消耗 3 兑换商品 4 抽奖 5 缺陷反馈  
	 */
	public Byte getType() {
		return type;
	}

	/**
	 * 设置收入或消耗类型 
	         1 积分兑换娱币  2比赛消耗 3 兑换商品 4 抽奖 5 缺陷反馈  
	 *
	 * @param type 收入或消耗类型 
	         1 积分兑换娱币  2比赛消耗 3 兑换商品 4 抽奖 5 缺陷反馈  
	 */
	public void setType(Byte type) {
		this.type = type;
	}

	/**
	 * 获取兑换、抽奖编号或比赛id
	 *
	 * @return trade_no - 兑换、抽奖编号或比赛id
	 */
	public String getTradeNo() {
		return tradeNo;
	}

	/**
	 * 设置兑换、抽奖编号或比赛id
	 *
	 * @param tradeNo 兑换、抽奖编号或比赛id
	 */
	public void setTradeNo(String tradeNo) {
		this.tradeNo = tradeNo;
	}

	/**
	 * 获取消耗金额
	 *
	 * @return amount - 消耗金额
	 */
	public Integer getAmount() {
		return amount;
	}

	/**
	 * 设置消耗金额
	 *
	 * @param amount 消耗金额
	 */
	public void setAmount(Integer amount) {
		this.amount = amount;
	}

	/**
	 * 获取-1 支出 1 收入
	 *
	 * @return direction - -1 支出 1 收入
	 */
	public Byte getDirection() {
		return direction;
	}

	/**
	 * 设置-1 支出 1 收入
	 *
	 * @param direction -1 支出 1 收入
	 */
	public void setDirection(Byte direction) {
		this.direction = direction;
	}

	/**
	 * 获取状态
	 *
	 * @return state - 状态
	 */
	public Byte getState() {
		return state;
	}

	/**
	 * 设置状态
	 *
	 * @param state 状态
	 */
	public void setState(Byte state) {
		this.state = state;
	}

	/**
	 * @return is_valid
	 */
	public Byte getIsValid() {
		return isValid;
	}

	/**
	 * @param isValid
	 */
	public void setIsValid(Byte isValid) {
		this.isValid = isValid;
	}

	/**
	 * @return create_date
	 */
	public Date getCreateDate() {
		return createDate;
	}

	/**
	 * @param createDate
	 */
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	/**
	 * @return update_date
	 */
	public Date getUpdateDate() {
		return updateDate;
	}

	/**
	 * @param updateDate
	 */
	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}
}