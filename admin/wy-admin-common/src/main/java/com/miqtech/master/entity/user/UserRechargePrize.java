package com.miqtech.master.entity.user;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "user_recharge_prize")
public class UserRechargePrize extends IdEntity {
	private static final long serialVersionUID = -2002085852919958934L;
	@Column(name = "recharge_prize_id")
	private Long rechargePrizeId;
	@Column(name = "netbar_id")
	private Long netbarId;
	@Column(name = "user_id")
	private Long userId;
	@Column(name = "expire_date")
	private Date expireDate;

	public Long getRechargePrizeId() {
		return rechargePrizeId;
	}

	public void setRechargePrizeId(Long rechargePrizeId) {
		this.rechargePrizeId = rechargePrizeId;
	}

	public Long getNetbarId() {
		return netbarId;
	}

	public void setNetbarId(Long netbarId) {
		this.netbarId = netbarId;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Date getExpireDate() {
		return expireDate;
	}

	public void setExpireDate(Date expireDate) {
		this.expireDate = expireDate;
	}

}
