package com.miqtech.master.entity.activity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

/**
 * 赛事评论点赞
 *
 */
@Entity
@Table(name = "activity_comment_praise")
public class ActivityCommentPraise extends IdEntity {
	private static final long serialVersionUID = 1072437444948009693L;
	private Long userId;
	private Long commentId;

	@Column(name = "user_id")
	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	@Column(name = "comment_id")
	public Long getCommentId() {
		return commentId;
	}

	public void setCommentId(Long commentId) {
		this.commentId = commentId;
	}

}
