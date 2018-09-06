package com.miqtech.master.entity.bounty;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "bounty_prize")
public class BountyPrize extends IdEntity {

	private static final long serialVersionUID = -8988984728450180910L;

	private Long bountyId;// 悬赏令ID
	private Integer awardType;// 奖品类别:1-自有商品,2-充值,3-实物
	private Integer awardSubType;// 奖品小类别:0-实物,1-自有红包,2-自有金币,3-充值话费,4-充值流量,5-充值Q币
	private Integer awardNum;// 奖励数量
	private String awardName;// 奖品名称
	private String rankName;// 排行榜名称
	private Integer maxNum;// 最大奖励数量

	@Column(name = "bounty_id")
	public Long getBountyId() {
		return bountyId;
	}

	public void setBountyId(Long bountyId) {
		this.bountyId = bountyId;
	}

	@Column(name = "award_type")
	public Integer getAwardType() {
		return awardType;
	}

	public void setAwardType(Integer awardType) {
		this.awardType = awardType;
	}

	@Column(name = "award_sub_type")
	public Integer getAwardSubType() {
		return awardSubType;
	}

	public void setAwardSubType(Integer awardSubType) {
		this.awardSubType = awardSubType;
	}

	@Column(name = "award_num")
	public Integer getAwardNum() {
		return awardNum;
	}

	public void setAwardNum(Integer awardNum) {
		this.awardNum = awardNum;
	}

	@Column(name = "award_name")
	public String getAwardName() {
		return awardName;
	}

	public void setAwardName(String awardName) {
		this.awardName = awardName;
	}

	@Column(name = "rank_name")
	public String getRankName() {
		return rankName;
	}

	public void setRankName(String rankName) {
		this.rankName = rankName;
	}

	@Column(name = "max_num")
	public Integer getMaxNum() {
		return maxNum;
	}

	public void setMaxNum(Integer maxNum) {
		this.maxNum = maxNum;
	}

}
