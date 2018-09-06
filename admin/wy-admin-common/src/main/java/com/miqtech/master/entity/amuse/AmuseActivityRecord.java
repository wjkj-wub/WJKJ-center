package com.miqtech.master.entity.amuse;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "amuse_r_activity_record")
public class AmuseActivityRecord extends IdEntity {
	private static final long serialVersionUID = -8803463715704981619L;

	private Long activityId; //（娱乐赛）活动ID
	private Long userId; //用户ID
	private String telephone; //手机号
	private String name; //用户（真实）姓名
	private String gameAccount; //游戏账号（昵称）
	private String server; //比赛区服
	private String qq; //QQ号
	private String idCard; //身份证号
	private String teamName; //团队名称
	private Integer state; //状态:-1-报名后放弃比赛；0-报名后未提交审核；1-已提交审核
	private Date verifyDate; //提交认证时间

	@Column(name = "verify_date")
	public Date getVerifyDate() {
		return verifyDate;
	}

	public void setVerifyDate(Date verifyDate) {
		this.verifyDate = verifyDate;
	}

	@Column(name = "telephone")
	public String getTelephone() {
		return telephone;
	}

	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}

	@Column(name = "name")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "game_account")
	public String getGameAccount() {
		return gameAccount;
	}

	public void setGameAccount(String gameAccount) {
		this.gameAccount = gameAccount;
	}

	@Column(name = "server")
	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	@Column(name = "qq")
	public String getQq() {
		return qq;
	}

	public void setQq(String qq) {
		this.qq = qq;
	}

	@Column(name = "id_card")
	public String getIdCard() {
		return idCard;
	}

	public void setIdCard(String idCard) {
		this.idCard = idCard;
	}

	@Column(name = "team_name")
	public String getTeamName() {
		return teamName;
	}

	public void setTeamName(String teamName) {
		this.teamName = teamName;
	}

	@Column(name = "state")
	public Integer getState() {
		return state;
	}

	public void setState(Integer state) {
		this.state = state;
	}

	@Column(name = "activity_id")
	public Long getActivityId() {
		return activityId;
	}

	public void setActivityId(Long activityId) {
		this.activityId = activityId;
	}

	@Column(name = "user_id")
	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}
}
