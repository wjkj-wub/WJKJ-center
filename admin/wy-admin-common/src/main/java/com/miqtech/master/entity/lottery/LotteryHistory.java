package com.miqtech.master.entity.lottery;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

/**
 * 抽奖历史记录
 */
@Entity
@Table(name = "lottery_t_history")
public class LotteryHistory extends IdEntity {

	private static final long serialVersionUID = -7442388187051345617L;

	private Long lotteryId;
	private Long userId;
	private Long awardId;
	private Long prizeId;
	private Integer isWin;// 是否
	private Integer hasGet;// 是否已领取:0-否,1-是

	@Column(name = "user_id")
	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	@Column(name = "lottery_id")
	public Long getLotteryId() {
		return lotteryId;
	}

	public void setLotteryId(Long lotteryId) {
		this.lotteryId = lotteryId;
	}

	@Column(name = "award_id")
	public Long getAwardId() {
		return awardId;
	}

	public void setAwardId(Long awardId) {
		this.awardId = awardId;
	}

	@Column(name = "prize_id")
	public Long getPrizeId() {
		return prizeId;
	}

	public void setPrizeId(Long prizeId) {
		this.prizeId = prizeId;
	}

	@Column(name = "is_win")
	public Integer getIsWin() {
		return isWin;
	}

	public void setIsWin(Integer isWin) {
		this.isWin = isWin;
	}

	@Column(name = "has_get")
	public Integer getHasGet() {
		return hasGet;
	}

	public void setHasGet(Integer hasGet) {
		this.hasGet = hasGet;
	}

}
