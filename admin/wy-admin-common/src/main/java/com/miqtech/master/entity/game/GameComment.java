package com.miqtech.master.entity.game;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "game_r_comment")
public class GameComment extends IdEntity {
	private static final long serialVersionUID = 2800220288015421149L;
	private Long gameId;// 游戏ID
	private Long userId;// 评论用户ID
	private Integer score;// 评分
	private String comment;// 介绍

	@Column(name = "game_id")
	public Long getGameId() {
		return gameId;
	}

	public void setGameId(Long gameId) {
		this.gameId = gameId;
	}

	@Column(name = "user_id")
	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	@Column(name = "score")
	public Integer getScore() {
		return score;
	}

	public void setScore(Integer score) {
		this.score = score;
	}

	@Column(name = "comment")
	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

}
