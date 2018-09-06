package com.miqtech.master.entity.pc.taskmatch;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * 任务赛-LOL英雄关联表
 *
 * @author gaohanlin
 * @create 2017年08月30日
 */
@Entity
@Table(name = "pc_task_match_limit_hero")
public class TaskMatchLimitHero implements Serializable {
	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(name = "task_id")
	private Long taskId;
	@Column(name = "task_type")
	private Integer taskType;
	@Column(name = "lol_hero_id")
	private Long lolHeroId;
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

	public Long getTaskId() {
		return taskId;
	}

	public void setTaskId(Long taskId) {
		this.taskId = taskId;
	}

	public Integer getTaskType() {
		return taskType;
	}

	public void setTaskType(Integer taskType) {
		this.taskType = taskType;
	}

	public Long getLolHeroId() {
		return lolHeroId;
	}

	public void setLolHeroId(Long lolHeroId) {
		this.lolHeroId = lolHeroId;
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

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}

	public TaskMatchLimitHero(Long taskId, Integer taskType, Long lolHeroId, Integer isValid, Date createDate) {
		this.taskId = taskId;
		this.taskType = taskType;
		this.lolHeroId = lolHeroId;
		this.isValid = isValid;
		this.createDate = createDate;
	}

	public TaskMatchLimitHero() {

	}

}
