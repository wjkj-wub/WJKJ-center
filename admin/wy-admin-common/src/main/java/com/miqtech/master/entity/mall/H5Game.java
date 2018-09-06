package com.miqtech.master.entity.mall;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

/**
 * 用户h5游戏详情记录
 */

@Entity
@Table(name = "mall_r_game")
public class H5Game extends IdEntity {

	private static final long serialVersionUID = -5261313584082359920L;
	@Column(name = "user_id", nullable = false)
	private Long userId;
	@Column(name = "playtimes", nullable = false)
	private Integer playtimes;
	@Column(name = "score", nullable = false, columnDefinition = "0")
	private Long score;
	@Column(name = "game_id", nullable = false, columnDefinition = "1")
	private Integer gameId;
	@Column(name = "play_date", nullable = false)
	private Date playDate;
	@Column(name = "score_date", nullable = false)
	private Date scoreDate;

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Integer getPlaytimes() {
		return playtimes;
	}

	public void setPlaytimes(Integer playtimes) {
		this.playtimes = playtimes;
	}

	public Long getScore() {
		return score;
	}

	public void setScore(Long score) {
		this.score = score;
	}

	public Integer getGameId() {
		return gameId;
	}

	public void setGameId(Integer gameId) {
		this.gameId = gameId;
	}

	public Date getPlayDate() {
		return playDate;
	}

	public void setPlayDate(Date playDate) {
		this.playDate = playDate;
	}

	public Date getScoreDate() {
		return scoreDate;
	}

	public void setScoreDate(Date scoreDate) {
		this.scoreDate = scoreDate;
	}
}
