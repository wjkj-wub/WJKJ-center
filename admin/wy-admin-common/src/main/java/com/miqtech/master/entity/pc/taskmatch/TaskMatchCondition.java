package com.miqtech.master.entity.pc.taskmatch;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "pc_task_match_condition")
public class TaskMatchCondition implements Serializable {
	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "id", nullable = false, unique = true)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(name = "target_id")
	private Long targetId;
	@Column(name = "module_type")
	private Integer moduleType;
	@Column(name = "symbol")
	private String symbol;
	@Column(name = "param1")
	private Integer param1;
	@Column(name = "param2")
	private Integer param2;
	@Column(name = "result")
	private Integer result;
	@Column(name = "is_valid")
	private Integer isValid;
	@Column(name = "create_date")
	private Date createDate;
	@Column(name = "update_date")
	private Date updateDate;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getTargetId() {
		return targetId;
	}

	public void setTargetId(Long targetId) {
		this.targetId = targetId;
	}

	public Integer getModuleType() {
		return moduleType;
	}

	public void setModuleType(Integer moduleType) {
		this.moduleType = moduleType;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public Integer getParam1() {
		return param1;
	}

	public void setParam1(Integer param1) {
		this.param1 = param1;
	}

	public Integer getParam2() {
		return param2;
	}

	public void setParam2(Integer param2) {
		this.param2 = param2;
	}

	public Integer getResult() {
		return result;
	}

	public void setResult(Integer result) {
		this.result = result;
	}

	public Integer getIsValid() {
		return isValid;
	}

	public void setIsValid(Integer isValid) {
		this.isValid = isValid;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreate_date(Date createDate) {
		this.createDate = createDate;
	}

	public Date getUpdate_date() {
		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}

	public TaskMatchCondition(Long targetId, Integer moduleType, String symbol, Integer param1, Integer param2,
			Integer result, Integer isValid, Date createDate) {
		this.targetId = targetId;
		this.moduleType = moduleType;
		this.symbol = symbol;
		this.param1 = param1;
		this.param2 = param2;
		this.result = result;
		this.isValid = isValid;
		this.createDate = createDate;
	}

	public TaskMatchCondition() {

	}

}
