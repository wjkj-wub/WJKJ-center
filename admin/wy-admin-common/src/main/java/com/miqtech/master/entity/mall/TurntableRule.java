package com.miqtech.master.entity.mall;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "mall_t_turntable_start")
public class TurntableRule extends IdEntity  {

	private static final long serialVersionUID = 7082218518842543371L;
	
	private String rule ;//规则设置
	
	@Column(name = "rule")
	public String getRule() {
		return rule;
	}

	public void setRule(String rule) {
		this.rule = rule;
	}
	
	
	
	
}
