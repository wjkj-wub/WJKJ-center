package com.miqtech.master.entity.amuse;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "amuse_r_activity_comment")
public class AmuseActivityComment extends IdEntity {
	private static final long serialVersionUID = 8267513150549872553L;

	private Long amuseId; //娱乐赛ID
	private Long userId; //用户ID
	private String content; //评论内容
	private Integer type; //评论类型：1-娱乐赛评论；2-官方赛事评论 3-资讯评论
	private Long parentId; //一级评论ID
	private Long replyId; //楼中楼，回复对象的评论ID
	private Integer likeCount; //点赞次数
	private Integer virtualLikeCount; //虚拟点赞次数
	private Integer bountyHunterFlag;

	@Column(name = "virtual_like_count")
	public Integer getVirtualLikeCount() {
		return virtualLikeCount;
	}

	public void setVirtualLikeCount(Integer virtualLikeCount) {
		this.virtualLikeCount = virtualLikeCount;
	}

	private String img;
	private Long shortcutId;

	@Column(name = "like_count")
	public Integer getLikeCount() {
		return likeCount;
	}

	public void setLikeCount(Integer likeCount) {
		this.likeCount = likeCount;
	}

	@Column(name = "type")
	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	@Column(name = "parent_id")
	public Long getParentId() {
		return parentId;
	}

	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}

	@Column(name = "reply_id")
	public Long getReplyId() {
		return replyId;
	}

	public void setReplyId(Long replyId) {
		this.replyId = replyId;
	}

	@Column(name = "amuse_id")
	public Long getAmuseId() {
		return amuseId;
	}

	public void setAmuseId(Long amuseId) {
		this.amuseId = amuseId;
	}

	@Column(name = "user_id")
	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	@Column(name = "content")
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	@Column(name = "img")
	public String getImg() {
		return img;
	}

	public void setImg(String img) {
		this.img = img;
	}

	@Column(name = "shortcut_id")
	public Long getShortcutId() {
		return shortcutId;
	}

	public void setShortcutId(Long shortcutId) {
		this.shortcutId = shortcutId;
	}

	@Column(name = "bounty_hunter_flag")
	public Integer getBountyHunterFlag() {
		return bountyHunterFlag;
	}

	public void setBountyHunterFlag(Integer bountyHunterFlag) {
		this.bountyHunterFlag = bountyHunterFlag;
	}

}
