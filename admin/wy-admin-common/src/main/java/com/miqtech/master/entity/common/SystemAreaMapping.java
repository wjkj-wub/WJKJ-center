package com.miqtech.master.entity.common;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "sys_t_area_code_map")
@JsonIgnoreProperties({ "updateUserId", "updateDate", "createUserId", "createDate", "valid" })
public class SystemAreaMapping extends IdEntity {
	private static final long serialVersionUID = -3676224767971316083L;
	private Long sId;//'原始id',
	private String sName;// '原始城市名称',
	private String sCode;// '原始城市code',
	private Long tId;// '目标城市id',
	private String tCode;// '目标城市code',
	private String tName;// '目标城市名称',

	@Column(name = "s_name")
	public String getsName() {
		return sName;
	}

	public void setsName(String sName) {
		this.sName = sName;
	}

	@Column(name = "s_code")
	public String getsCode() {
		return sCode;
	}

	public void setsCode(String sCode) {
		this.sCode = sCode;
	}

	@Column(name = "s_id")
	public Long getsId() {
		return sId;
	}

	public void setsId(Long sId) {
		this.sId = sId;
	}

	@Column(name = "t_id")
	public Long gettId() {
		return tId;
	}

	public void settId(Long tId) {
		this.tId = tId;
	}

	@Column(name = "t_code")
	public String gettCode() {
		return tCode;
	}

	public void settCode(String tCode) {
		this.tCode = tCode;
	}

	@Column(name = "t_name")
	public String gettName() {
		return tName;
	}

	public void settName(String tName) {
		this.tName = tName;
	}

}
