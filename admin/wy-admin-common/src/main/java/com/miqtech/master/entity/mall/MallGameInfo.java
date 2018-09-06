package com.miqtech.master.entity.mall;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "mall_game_info")
public class MallGameInfo extends IdEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String name; //游戏名
	private String image;//图片
	private Date publishTime;//发布时间
	private Integer status;//状态
	private Integer topStatus;//置顶状态
	private String gameUrl;//游戏url
	private String score;//积分或关卡
	private String coin;//金币
	private String rule;//规则
	private Integer coinConsume;//单次金币消耗
	private Integer gameType;//游戏类型
	private String briefRule;//简要规则

	@Column(name = "brief_rule")
	public String getBriefRule() {
		return briefRule;
	}

	public void setBriefRule(String briefRule) {
		this.briefRule = briefRule;
	}

	@Column(name = "coin_consume")
	public Integer getCoinConsume() {
		return coinConsume;
	}

	public void setCoinConsume(Integer coinConsume) {
		this.coinConsume = coinConsume;
	}

	@Column(name = "game_type")
	public Integer getGameType() {
		return gameType;
	}

	public void setGameType(Integer gameType) {
		this.gameType = gameType;
	}

	@Column(name = "score")
	public String getScore() {
		return score;
	}

	public void setScore(String score) {
		this.score = score;
	}

	@Column(name = "coin")
	public String getCoin() {
		return coin;
	}

	public void setCoin(String coin) {
		this.coin = coin;
	}

	@Column(name = "rule")
	public String getRule() {
		return rule;
	}

	public void setRule(String rule) {
		this.rule = rule;
	}

	@Column(name = "name")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "image")
	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	@Column(name = "publish_time")
	public Date getPublishTime() {
		return publishTime;
	}

	public void setPublishTime(Date publishTime) {
		this.publishTime = publishTime;
	}

	@Column(name = "status")
	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	@Column(name = "top_status")
	public Integer getTopStatus() {
		return topStatus;
	}

	public void setTopStatus(Integer topStatus) {
		this.topStatus = topStatus;
	}

	@Column(name = "game_url")
	public String getGameUrl() {
		return gameUrl;
	}

	public void setGameUrl(String gameUrl) {
		this.gameUrl = gameUrl;
	}

}
