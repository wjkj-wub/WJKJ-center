package com.miqtech.master.entity.lottery;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

/**
 * 抽奖奖项设置
 */
@Entity
@Table(name = "lottery_t_award")
public class LotteryAward extends IdEntity {

	private static final long serialVersionUID = 5607319832363676308L;

	private Long lotteryId;// 所属活动
	private Long prizeId;// 奖品
	private String name;// 奖项名
	private Integer inventory;// 对外显示的数量
	private Integer realInventory;// 真实的数量:-1表示无限量
	private Double probablity;// 概率（按权重的算法）
	private Integer virtualWinners;// 虚假的中奖人数，显示的人数为真实人数加虚假人数
	private Integer order;// 排序

	@Column(name = "lottery_id")
	public Long getLotteryId() {
		return lotteryId;
	}

	public void setLotteryId(Long lotteryId) {
		this.lotteryId = lotteryId;
	}

	@Column(name = "prize_id")
	public Long getPrizeId() {
		return prizeId;
	}

	public void setPrizeId(Long prizeId) {
		this.prizeId = prizeId;
	}

	@Column(name = "name")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "inventory")
	public Integer getInventory() {
		return inventory;
	}

	public void setInventory(Integer inventory) {
		this.inventory = inventory;
	}

	@Column(name = "real_inventory")
	public Integer getRealInventory() {
		return realInventory;
	}

	public void setRealInventory(Integer realInventory) {
		this.realInventory = realInventory;
	}

	@Column(name = "probablity")
	public Double getProbablity() {
		return probablity;
	}

	public void setProbablity(Double probablity) {
		this.probablity = probablity;
	}

	@Column(name = "virtual_winners")
	public Integer getVirtualWinners() {
		return virtualWinners;
	}

	public void setVirtualWinners(Integer virtualWinners) {
		this.virtualWinners = virtualWinners;
	}

	@Column(name = "`order`")
	public Integer getOrder() {
		return order;
	}

	public void setOrder(Integer order) {
		this.order = order;
	}

}
