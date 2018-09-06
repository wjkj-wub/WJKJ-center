package com.miqtech.master.entity.netbar;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "netbar_rank")
public class NetbarRank extends IdEntity {

	private static final long serialVersionUID = -7936507698078574548L;

	private Long netbarId;// 网吧ID
	private Double score;// 积分
	private Integer rank;// 排名
	private Integer week;// 排行榜周次
	private Integer matchCount;// 比赛场数
	private Integer barMatchCount;// 网吧大战场数
	private Integer memberCount;// 参赛人数
	private Integer winBarMatchCount;// 网吧大战胜场数
	private Long mvpUwanUserId;// 场均评分最高用户
	private Double mvpKda;// 场均最高评分
	private Long loseMvpUwanUserId;// 场均败方最高评分用户
	private Double loseMvpKda;// 场均败方最高评分
	private Long mostKillUwanUserId;// 场均杀人最多用户
	private Integer mostKillCount;// 最高杀人数
	private Long mostAssistUwanUserId;// 场均助攻最多用户
	private Integer mostAssistCount;// 最高助攻数

	@Column(name = "netbar_id")
	public Long getNetbarId() {
		return netbarId;
	}

	public void setNetbarId(Long netbarId) {
		this.netbarId = netbarId;
	}

	@Column(name = "score")
	public Double getScore() {
		return score;
	}

	public void setScore(Double score) {
		this.score = score;
	}

	@Column(name = "rank")
	public Integer getRank() {
		return rank;
	}

	public void setRank(Integer rank) {
		this.rank = rank;
	}

	@Column(name = "week")
	public Integer getWeek() {
		return week;
	}

	public void setWeek(Integer week) {
		this.week = week;
	}

	@Column(name = "match_count")
	public Integer getMatchCount() {
		return matchCount;
	}

	public void setMatchCount(Integer matchCount) {
		this.matchCount = matchCount;
	}

	@Column(name = "bar_match_count")
	public Integer getBarMatchCount() {
		return barMatchCount;
	}

	public void setBarMatchCount(Integer barMatchCount) {
		this.barMatchCount = barMatchCount;
	}

	@Column(name = "member_count")
	public Integer getMemberCount() {
		return memberCount;
	}

	public void setMemberCount(Integer memberCount) {
		this.memberCount = memberCount;
	}

	@Column(name = "win_bar_match_count")
	public Integer getWinBarMatchCount() {
		return winBarMatchCount;
	}

	public void setWinBarMatchCount(Integer winBarMatchCount) {
		this.winBarMatchCount = winBarMatchCount;
	}

	@Column(name = "mvp_uwan_user_id")
	public Long getMvpUwanUserId() {
		return mvpUwanUserId;
	}

	public void setMvpUwanUserId(Long mvpUwanUserId) {
		this.mvpUwanUserId = mvpUwanUserId;
	}

	@Column(name = "mvp_kda")
	public Double getMvpKda() {
		return mvpKda;
	}

	public void setMvpKda(Double mvpKda) {
		this.mvpKda = mvpKda;
	}

	@Column(name = "lose_mvp_uwan_user_id")
	public Long getLoseMvpUwanUserId() {
		return loseMvpUwanUserId;
	}

	public void setLoseMvpUwanUserId(Long loseMvpUwanUserId) {
		this.loseMvpUwanUserId = loseMvpUwanUserId;
	}

	@Column(name = "lose_mvp_kda")
	public Double getLoseMvpKda() {
		return loseMvpKda;
	}

	public void setLoseMvpKda(Double loseMvpKda) {
		this.loseMvpKda = loseMvpKda;
	}

	@Column(name = "most_kill_uwan_user_id")
	public Long getMostKillUwanUserId() {
		return mostKillUwanUserId;
	}

	public void setMostKillUwanUserId(Long mostKillUwanUserId) {
		this.mostKillUwanUserId = mostKillUwanUserId;
	}

	@Column(name = "most_kill_count")
	public Integer getMostKillCount() {
		return mostKillCount;
	}

	public void setMostKillCount(Integer mostKillCount) {
		this.mostKillCount = mostKillCount;
	}

	@Column(name = "most_assist_uwan_user_id")
	public Long getMostAssistUwanUserId() {
		return mostAssistUwanUserId;
	}

	public void setMostAssistUwanUserId(Long mostAssistUwanUserId) {
		this.mostAssistUwanUserId = mostAssistUwanUserId;
	}

	@Column(name = "most_assist_count")
	public Integer getMostAssistCount() {
		return mostAssistCount;
	}

	public void setMostAssistCount(Integer mostAssistCount) {
		this.mostAssistCount = mostAssistCount;
	}
}
