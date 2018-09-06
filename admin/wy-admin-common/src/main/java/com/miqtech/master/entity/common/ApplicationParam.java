package com.miqtech.master.entity.common;

import javax.persistence.Entity;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.miqtech.master.entity.IdEntity;

/**
 * APP启动配置数据
 */

@Entity
@Table(name = "application_t_params")
@JsonIgnoreProperties({ "id", "createDate", "createUserId", "updateDate", "updateUserId", "valid" })
public class ApplicationParam extends IdEntity {
	private static final long serialVersionUID = -4816760816526007763L;
	private String key;
	private String value;
	private Integer type;//1app2后台

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

}
