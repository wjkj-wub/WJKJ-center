package com.miqtech.master.entity.activity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "activity_over_activity_module_info")
public class ActivityOverActivityModuleInfo implements Serializable {

	private static final long serialVersionUID = -8131430898317847878L;

	private Long id;
	private Long moduleId;
	private Long overActivityId;

	@Id
	@Column(name = "id", nullable = false, unique = true)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name = "module_id")
	public Long getModuleId() {
		return moduleId;
	}

	public void setModuleId(Long moduleId) {
		this.moduleId = moduleId;
	}

	@Column(name = "over_activity_id")
	public Long getOverActivityId() {
		return overActivityId;
	}

	public void setOverActivityId(Long overActivityId) {
		this.overActivityId = overActivityId;
	}

}
