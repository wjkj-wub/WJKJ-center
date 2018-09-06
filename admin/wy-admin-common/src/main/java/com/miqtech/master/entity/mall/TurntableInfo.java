package com.miqtech.master.entity.mall;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "mall_t_turntable")
public class TurntableInfo extends IdEntity {
	private static final long serialVersionUID = 31313255345361L;

	private String moduleName;//模块名称
	private Double probability;//概率
	private Integer prizeCount;//奖品数量
	private Integer canDrawCount;//可用奖品数量
	private List<TurntableInfo> counts;//可用奖品数量

	@Transient
	public List<TurntableInfo> getCounts() {
		return counts;
	}

	public void setCounts(List<TurntableInfo> counts) {
		this.counts = counts;
	}

	@Column(name = "module_name")
	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	@Column(name = "probability")
	public Double getProbability() {
		return probability;
	}

	public void setProbability(Double probability) {
		this.probability = probability;
	}

	@Column(name = "prize_count")
	public Integer getPrizeCount() {
		return prizeCount;
	}

	public void setPrizeCount(Integer prizeCount) {
		this.prizeCount = prizeCount;
	}

	@Transient
	public Integer getCanDrawCount() {
		return canDrawCount;
	}

	public void setCanDrawCount(Integer canDrawCount) {
		this.canDrawCount = canDrawCount;
	}

}
