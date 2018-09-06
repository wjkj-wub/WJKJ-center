package com.miqtech.master.entity.user;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "user_value_added_card")
public class UserValueAddedCard extends IdEntity {
	private static final long serialVersionUID = 7910977933621418355L;

	private Long userId;// 用户id                                                         
	private Long valueAddCardId;//增值券id                             
	private Integer usable;//当前增值券是否可用0不可用 1可用                       
	private Integer amount;// 金额                                                           
	private Long netbarId;// 网吧id                                                         
	private String tradeNo;//资源商城增值券：对应网吧购买增值券商品订单号  
	private Date expireDate;// 过期时间 
	private Integer limitMinMoney;//满多少可用

	@Column(name = "user_id")
	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	@Column(name = "value_add_card_id")
	public Long getValueAddCardId() {
		return valueAddCardId;
	}

	public void setValueAddCardId(Long valueAddCardId) {
		this.valueAddCardId = valueAddCardId;
	}

	@Column(name = "usable")
	public Integer getUsable() {
		return usable;
	}

	public void setUsable(Integer usable) {
		this.usable = usable;
	}

	@Column(name = "amount")
	public Integer getAmount() {
		return amount;
	}

	public void setAmount(Integer amount) {
		this.amount = amount;
	}

	@Column(name = "netbar_id")
	public Long getNetbarId() {
		return netbarId;
	}

	public void setNetbarId(Long netbarId) {
		this.netbarId = netbarId;
	}

	@Column(name = "trade_no")
	public String getTradeNo() {
		return tradeNo;
	}

	public void setTradeNo(String tradeNo) {
		this.tradeNo = tradeNo;
	}

	@Column(name = "expire_date")
	public Date getExpireDate() {
		return expireDate;
	}

	public void setExpireDate(Date expireDate) {
		this.expireDate = expireDate;
	}

	@Column(name = "limit_min_money")
	public Integer getLimitMinMoney() {
		return limitMinMoney;
	}

	public void setLimitMinMoney(Integer limitMinMoney) {
		this.limitMinMoney = limitMinMoney;
	}

}
