package com.miqtech.master.entity.mall;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

/**
 *兑换码
 *
 */
@Entity
@Table(name = "mall_t_cdkey")
public class MallCdkey extends IdEntity {
	private static final long serialVersionUID = 8836183948664312122L;
	private Long historyId;//兑换记录id
	private Long itemId;
	private String cdkey;
	private Integer isUse;//是否已使用0否1是
	private Long stockId;//库id
	private Date beginTime;//开始时间
	private Date endTime;//结束时间

	@Column(name = "history_id")
	public Long getHistoryId() {
		return historyId;
	}

	public void setHistoryId(Long historyId) {
		this.historyId = historyId;
	}

	@Column(name = "item_id")
	public Long getItemId() {
		return itemId;
	}

	public void setItemId(Long itemId) {
		this.itemId = itemId;
	}

	@Column(name = "cdkey")
	public String getCdkey() {
		return cdkey;
	}

	public void setCdkey(String cdkey) {
		this.cdkey = cdkey;
	}

	@Column(name = "is_use")
	public Integer getIsUse() {
		return isUse;
	}

	public void setIsUse(Integer isUse) {
		this.isUse = isUse;
	}

	@Column(name = "stock_id")
	public Long getStockId() {
		return stockId;
	}

	public void setStockId(Long stockId) {
		this.stockId = stockId;
	}

	@Column(name = "begin_time")
	public Date getBeginTime() {
		return beginTime;
	}

	public void setBeginTime(Date beginTime) {
		this.beginTime = beginTime;
	}

	@Column(name = "end_time")
	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

}
