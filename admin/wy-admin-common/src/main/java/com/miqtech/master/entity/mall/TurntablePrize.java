package com.miqtech.master.entity.mall;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "mall_t_turntable_prize")
public class TurntablePrize extends IdEntity {

	private static final long serialVersionUID = -4326709418436806330L;

	private Integer moduleId; //模块id
	private String prizeName;//奖品名称
	private Integer categoryId;//类型
	private String prizeImg; //图片url
	private Integer prizeCount; //奖品数量
	private Integer enableStatus;//启用状态
	private Integer superType;//大类型
	private String informationDefualt;//默认信息
	private Integer prizeQuota;//奖品数额

	@Column(name = "prize_quota")
	public Integer getPrizeQuota() {
		return prizeQuota;
	}

	public void setPrizeQuota(Integer prizeQuota) {
		this.prizeQuota = prizeQuota;
	}

	@Column(name = "information_defualt")
	public String getInformationDefualt() {
		return informationDefualt;
	}

	public void setInformationDefualt(String informationDefualt) {
		this.informationDefualt = informationDefualt;
	}

	@Transient
	public Integer getSuperType() {
		return superType;
	}

	public void setSuperType(Integer superType) {
		this.superType = superType;
	}

	@Column(name = "module_id")
	public Integer getModuleId() {
		return moduleId;
	}

	public void setModuleId(Integer moduleId) {
		this.moduleId = moduleId;
	}

	@Column(name = "prize_name")
	public String getPrizeName() {
		return prizeName;
	}

	public void setPrizeName(String prizeName) {
		this.prizeName = prizeName;
	}

	@Column(name = "category_id")
	public Integer getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(Integer categoryId) {
		this.categoryId = categoryId;
	}

	@Column(name = "prize_img")
	public String getPrizeImg() {
		return prizeImg;
	}

	public void setPrizeImg(String prizeImg) {
		this.prizeImg = prizeImg;
	}

	@Column(name = "prize_count")
	public Integer getPrizeCount() {
		return prizeCount;
	}

	public void setPrizeCount(Integer prizeCount) {
		this.prizeCount = prizeCount;
	}

	@Column(name = "enable_status")
	public Integer getEnableStatus() {
		return enableStatus;
	}

	public void setEnableStatus(Integer enableStatus) {
		this.enableStatus = enableStatus;
	}

}
