package com.miqtech.master.entity.award;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

/**
 * 库存商品物品
 */
@Entity
@Table(name = "award_t_commodity")
public class AwardCommodity extends IdEntity {

	private static final long serialVersionUID = -3266101999145864861L;

	private Long inventoryId;// 所属库存的ID,关联到award_t_inventory.id
	private Long awardRecordId;// 奖品发放记录ID
	private String cdkey;
	private Date startTime;// 生效时间
	private Date endTime;// 过期时间
	private Integer isUsed;// 是否已使用:0-未使用,1-已使用
	private Date usedTime;// 使用时间

	@Column(name = "inventory_id")
	public Long getInventoryId() {
		return inventoryId;
	}

	public void setInventoryId(Long inventoryId) {
		this.inventoryId = inventoryId;
	}

	@Column(name = "award_record_id")
	public Long getAwardRecordId() {
		return awardRecordId;
	}

	public void setAwardRecordId(Long awardRecordId) {
		this.awardRecordId = awardRecordId;
	}

	@Column(name = "cdkey")
	public String getCdkey() {
		return cdkey;
	}

	public void setCdkey(String cdkey) {
		this.cdkey = cdkey;
	}

	@Column(name = "start_time")
	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	@Column(name = "end_time")
	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	@Column(name = "is_used")
	public Integer getIsUsed() {
		return isUsed;
	}

	public void setIsUsed(Integer isUsed) {
		this.isUsed = isUsed;
	}

	@Column(name = "used_time")
	public Date getUsedTime() {
		return usedTime;
	}

	public void setUsedTime(Date usedTime) {
		this.usedTime = usedTime;
	}

}
