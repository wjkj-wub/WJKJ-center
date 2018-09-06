package com.miqtech.master.entity.user;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "user_r_redbag")
public class UserRedbag extends IdEntity {

	private static final long serialVersionUID = -1119211289874486787L;
	private Long userId;//用户id
	private Long redbagId;//红包id
	private int netbarType;//网吧类型 1 全部网吧 2 指定网吧
	private Long netbarId;//网吧id
	private int usable;//当前红包是否可用0不可用 1可用,valid表示是否已经使用过 1未使用 0使用过
	private Integer amount;//金额
	private Integer limitMinMoney;//最低可用消费金额
	private Long delUserId;
	private String tradeNo;

	@Column(name = "user_id")
	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	@Column(name = "redbag_id")
	public Long getRedbagId() {
		return redbagId;
	}

	public void setRedbagId(Long redbagId) {
		this.redbagId = redbagId;
	}

	@Column(name = "netbar_type")
	public int getNetbarType() {
		return netbarType;
	}

	public void setNetbarType(int netbarType) {
		this.netbarType = netbarType;
	}

	@Column(name = "netbar_id")
	public Long getNetbarId() {
		return netbarId;
	}

	public void setNetbarId(Long netbarId) {
		this.netbarId = netbarId;
	}

	@Column(name = "usable")
	public int getUsable() {
		return usable;
	}

	public void setUsable(int usable) {
		this.usable = usable;
	}

	@Column(name = "amount")
	public Integer getAmount() {
		return amount;
	}

	public void setAmount(Integer amount) {
		this.amount = amount;
	}

	@Column(name = "limit_min_money")
	public Integer getLimitMinMoney() {
		return limitMinMoney;
	}

	public void setLimitMinMoney(Integer limitMinMoney) {
		this.limitMinMoney = limitMinMoney;
	}

	@Column(name = "del_user_id")
	public Long getDelUserId() {
		return delUserId;
	}

	public void setDelUserId(Long delUserId) {
		this.delUserId = delUserId;
	}

	@Column(name = "trade_no")
	public String getTradeNo() {
		return tradeNo;
	}

	public void setTradeNo(String tradeNo) {
		this.tradeNo = tradeNo;
	}

}