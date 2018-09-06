package com.miqtech.master.entity.user;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "user_t_game")
@JsonIgnoreProperties({ "createDate", "createUserId", "updateDate", "updateUserId", "valid" })
public class UserGame extends IdEntity {

	private static final long serialVersionUID = 5574195337183944050L;
	private Long userId;
	private Long gameId;
	private String gameServer;// 游戏服务器
	private String gameNickname;//用户的昵称
	private String gameLevel;//用户在游戏中的等级
	private Integer gameTimes;//游戏次数
	private String winRate;//胜率
	private Date thirdUpdated;//第三方更新时间

	@Column(name = "user_id")
	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	@Column(name = "game_id")
	public Long getGameId() {
		return gameId;
	}

	public void setGameId(Long gameId) {
		this.gameId = gameId;
	}

	@Column(name = "game_server")
	public String getGameServer() {
		return gameServer;
	}

	public void setGameServer(String gameServer) {
		this.gameServer = gameServer;
	}

	@Column(name = "game_nickname")
	public String getGameNickname() {
		return gameNickname;
	}

	public void setGameNickname(String gameNickname) {
		this.gameNickname = gameNickname;
	}

	@Column(name = "game_level")
	public String getGameLevel() {
		return gameLevel;
	}

	public void setGameLevel(String gameLevel) {
		this.gameLevel = gameLevel;
	}

	@Column(name = "game_times")
	public Integer getGameTimes() {
		return gameTimes;
	}

	public void setGameTimes(Integer gameTimes) {
		this.gameTimes = gameTimes;
	}

	@Column(name = "win_rate")
	public String getWinRate() {
		return winRate;
	}

	public void setWinRate(String winRate) {
		this.winRate = winRate;
	}

	@Column(name = "third_updated")
	public Date getThirdUpdated() {
		return thirdUpdated;
	}

	public void setThirdUpdated(Date thirdUpdated) {
		this.thirdUpdated = thirdUpdated;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
