package com.miqtech.master.entity.pc.taskmatch;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

/**
 * 排行榜任务赛-标签关联表
 *
 * @author gaohanlin
 * @create 2017年08月30日
 */
@Entity
@Table(name = "pc_task_match_rank_label")
public class TaskMatchRankLabel implements Serializable {
	private static final long serialVersionUID = -8136178479053060161L;
	@Id
	@Column(name = "id")
	private long id;
	@Column(name = "task_id")
	private long taskId; //任务赛ID
	@Column(name = "label_id")
	private long labelId; //任务赛标签ID
	@Column(name = "is_valid")
	private Byte isValid;
	@Column(name = "create_date")
	private Date createDate;
	@Column(name = "update_date")
	private Date updateDate;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getTaskId() {
		return taskId;
	}

	public void setTaskId(long taskId) {
		this.taskId = taskId;
	}

	public long getLabelId() {
		return labelId;
	}

	public void setLabelId(long labelId) {
		this.labelId = labelId;
	}

	public Byte getIsValid() {
		return isValid;
	}

	public void setIsValid(Byte isValid) {
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

}
