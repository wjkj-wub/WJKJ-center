package com.miqtech.master.entity.mall;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "mall_commodity_history_cdkey")
public class CommodityHistoryCdkey extends IdEntity {
	private static final long serialVersionUID = -1518007116535457467L;
	private Long historyId;
	private String cdkey;
	private Integer isSelected;

	@Column(name = "history_id")
	public Long getHistoryId() {
		return historyId;
	}

	public void setHistoryId(Long historyId) {
		this.historyId = historyId;
	}

	@Column(name = "cdkey")
	public String getCdkey() {
		return cdkey;
	}

	public void setCdkey(String cdkey) {
		this.cdkey = cdkey;
	}

	@Column(name = "is_selected")
	public Integer getIsSelected() {
		return isSelected;
	}

	public void setIsSelected(Integer isSelected) {
		this.isSelected = isSelected;
	}

}
