package com.miqtech.master.entity.lottery;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

/**
 * 抽奖奖项位置设置
 */
@Entity
@Table(name = "lottery_t_award_seat")
public class LotteryAwardSeat extends IdEntity {

	private static final long serialVersionUID = 3411261866895149363L;

	private Long lotteryId;
	private Long awardId;
	private Integer seat;// 转盘的位置设置（序号0-8）

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

	@Column(name = "seat")
	public Integer getSeat() {
		return seat;
	}

	public void setSeat(Integer seat) {
		this.seat = seat;
	}

}
