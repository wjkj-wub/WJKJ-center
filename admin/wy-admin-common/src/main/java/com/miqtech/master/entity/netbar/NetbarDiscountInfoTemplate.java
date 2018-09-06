package com.miqtech.master.entity.netbar;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "netbar_t_discount_info_template")
public class NetbarDiscountInfoTemplate extends IdEntity {

	private static final long serialVersionUID = 750961930836661259L;
	private String template;// 用户名

	@Column(name = "template")
	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

}
