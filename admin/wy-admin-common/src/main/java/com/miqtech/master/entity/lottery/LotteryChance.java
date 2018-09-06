package com.miqtech.master.entity.lottery;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

/**
 * 用户抽奖机会
 */
@Entity
@Table(name = "lottery_t_chance")
public class LotteryChance extends IdEntity {

	private static final long serialVersionUID = 8520805664289177774L;

	private Long lotteryId;
	private Long userId;
	private Integer chance;

	@Column(name = "lottery_id")
	public Long getLotteryId() {
		return lotteryId;
	}

	public void setLotteryId(Long lotteryId) {
		this.lotteryId = lotteryId;
	}

	@Column(name = "user_id")
	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	@Column(name = "chance")
	public Integer getChance() {
		return chance;
	}

	public void setChance(Integer chance) {
		this.chance = chance;
	}

}
