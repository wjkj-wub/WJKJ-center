package com.miqtech.master.entity.award;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

/**
 * 库存商品
 */
@Entity
@Table(name = "award_t_inventory")
public class AwardInventory extends IdEntity {

	private static final long serialVersionUID = -6905351797426794499L;

	private Long awardTypeId;// 商品类型ID,关联到mall_t_commodity_category.id
	private String name;// 商品名
	private Date startTime;// 生效时间
	private Date endTime;// 过期时间
	private Date importTime;// 导入时间

	@Column(name = "award_type_id")
	public Long getAwardTypeId() {
		return awardTypeId;
	}

	public void setAwardTypeId(Long awardTypeId) {
		this.awardTypeId = awardTypeId;
	}

	@Column(name = "name")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	@Column(name = "import_time")
	public Date getImportTime() {
		return importTime;
	}

	public void setImportTime(Date importTime) {
		this.importTime = importTime;
	}

}
