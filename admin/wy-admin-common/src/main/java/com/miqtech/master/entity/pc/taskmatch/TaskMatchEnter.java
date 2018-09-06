package com.miqtech.master.entity.pc.taskmatch;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * 任务赛报名表
 *
 * @author gaohanlin
 * @create 2017年08月30日
 */
@Entity
@Table(name = "pc_task_match_enter")
public class TaskMatchEnter {
	@Id
	@Column(name = "id", nullable = false)
	private Long id;
	@Column(name = "user_id")
	private Long userId; //用户id
	@Column(name = "task_id")
	private Long taskId; //任务id
	@Column(name = "task_type")
	private Byte taskType; //任务赛类型：1-排行榜，2-主题
	@Column(name = "user_qq")
	private String userQq; //玩家QQ号
	@Column(name = "game_server_tcls")
	private Integer gameServerTcls; //游戏区服tcls，对应pc_lol_game_server
	@Column(name = "game_nickname")
	private String gameNickname; //游戏昵称
	@Column(name = "status")
	private Byte status; //任务状态：0-无效，1-进行中，2-已完成
	@Column(name = "play_times")
	private Integer playTimes; //比赛场次
	@Column(name = "is_valid")
	private Integer isValid;
	@Column(name = "create_date") //创建时间
	private Date createDate;
	@Column(name = "update_date") //更新时间
	private Date updateDate;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getTaskId() {
		return taskId;
	}

	public void setTaskId(Long taskId) {
		this.taskId = taskId;
	}

	public Byte getTaskType() {
		return taskType;
	}

	public void setTaskType(Byte taskType) {
		this.taskType = taskType;
	}

	public String getUserQq() {
		return userQq;
	}

	public void setUserQq(String userQq) {
		this.userQq = userQq;
	}

	public Integer getGameServerTcls() {
		return gameServerTcls;
	}

	public void setGameServerTcls(Integer gameServerTcls) {
		this.gameServerTcls = gameServerTcls;
	}

	public String getGameNickname() {
		return gameNickname;
	}

	public void setGameNickname(String gameNickname) {
		this.gameNickname = gameNickname;
	}

	public Byte getStatus() {
		return status;
	}

	public void setStatus(Byte status) {
		this.status = status;
	}

	public Integer getPlayTimes() {
		return playTimes;
	}

	public void setPlayTimes(Integer playTimes) {
		this.playTimes = playTimes;
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
}
