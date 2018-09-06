package com.miqtech.master.entity.pc.detail;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "pc_chip_detail")
public class ChipDetail {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/**
	 * 用户ID
	 */
	@Column(name = "user_id")
	private Long userId;

	/**
	 * 收入或消耗类型 收入:充值 积分兑换 赛事奖励, 消耗:打比赛 兑换商品 
	        1 充值 2 积分兑换 3 比赛消耗 4 兑换商品
	 */
	private Byte type;

	@Column(name = "trade_no")
	private String tradeNo;

	private Integer amount;

	/**
	 * -1 支出 1 收入
	 */
	private Byte direction;

	private Byte state;

	@Column(name = "is_valid")
	private Byte isValid;

	@Column(name = "create_date")
	private Date createDate;

	@Column(name = "update_date")
	private Date updateDate;

	public ChipDetail(Long userId, Byte type, String tradeNo, Integer amount, Byte direction, Byte state, Byte isValid,
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

	public ChipDetail() {

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
	 * 获取收入或消耗类型 收入:充值 积分兑换 赛事奖励, 消耗:打比赛 兑换商品 
	        1 充值 2 积分兑换 3 比赛消耗 4 兑换商品
	 *
	 * @return type - 收入或消耗类型 收入:充值 积分兑换 赛事奖励, 消耗:打比赛 兑换商品 
	        1 充值 2 积分兑换 3 比赛消耗 4 兑换商品
	 */
	public Byte getType() {
		return type;
	}

	/**
	 * 设置收入或消耗类型 收入:充值 积分兑换 赛事奖励, 消耗:打比赛 兑换商品 
	        1 充值 2 积分兑换 3 比赛消耗 4 兑换商品
	 *
	 * @param type 收入或消耗类型 收入:充值 积分兑换 赛事奖励, 消耗:打比赛 兑换商品 
	        1 充值 2 积分兑换 3 比赛消耗 4 兑换商品
	 */
	public void setType(Byte type) {
		this.type = type;
	}

	/**
	 * @return trade_no
	 */
	public String getTradeNo() {
		return tradeNo;
	}

	/**
	 * @param tradeNo
	 */
	public void setTradeNo(String tradeNo) {
		this.tradeNo = tradeNo;
	}

	/**
	 * @return amount
	 */
	public Integer getAmount() {
		return amount;
	}

	/**
	 * @param amount
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
	 * @return state
	 */
	public Byte getState() {
		return state;
	}

	/**
	 * @param state
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