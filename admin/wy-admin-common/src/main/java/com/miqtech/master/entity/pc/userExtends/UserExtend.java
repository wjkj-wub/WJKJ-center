/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.miqtech.master.entity.pc.userExtends;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * 用户扩展信息
 */
@Entity
@Table(name = "pc_user_extend")
public class UserExtend {
	@Id
	@Column(name = "user_id")
	private Long userId; // 用户id
	@Column(name = "coin")
	private Integer coin; //积分
	@Column(name = "chip")
	private Integer chip; //娱币
	@Column(name = "is_valid")
	private Integer valid; //是否有效
	@Column(name = "create_date")
	private Date createDate; //录入时间
	@Column(name = "update_date")
	private Date updateDate; //最新修改时间

	public UserExtend() {

	}

	public UserExtend(Long userId, Integer valid) {
		this.userId = userId;
		this.valid = valid;
	}

	/**
	 * @return the userId
	 */
	public Long getUserId() {
		return userId;
	}

	/**
	 * @param userId the userId to set
	 */
	public void setUserId(Long userId) {
		this.userId = userId;
	}

	/**
	 * @return the coin
	 */
	public Integer getCoin() {
		return coin;
	}

	/**
	 * @param coin the coin to set
	 */
	public void setCoin(Integer coin) {
		this.coin = coin;
	}

	/**
	 * @return the chip
	 */
	public Integer getChip() {
		return chip;
	}

	/**
	 * @param chip the chip to set
	 */
	public void setChip(Integer chip) {
		this.chip = chip;
	}

	public Integer getValid() {
		return valid;
	}

	public void setValid(Integer valid) {
		this.valid = valid;
	}

	/**
	 * @return the createDate
	 */
	public Date getCreateDate() {
		return createDate;
	}

	/**
	 * @param createDate the createDate to set
	 */
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	/**
	 * @return the updateDate
	 */
	public Date getUpdateDate() {
		return updateDate;
	}

	/**
	 * @param updateDate the updateDate to set
	 */
	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}
}
