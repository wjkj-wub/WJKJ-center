package com.miqtech.master.entity.audition;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 海选赛-直接发放金币数量配置
 * 
 * @author zhangyuqi 2017年06月14日
 */
@Entity
@Table(name = "audition_match_detail_lol")
public class AuditionMatchDetailLol implements Serializable {

	private static final long serialVersionUID = -4229532287504428187L;

	private Long id;// AUTOINCREMENT,
	private Long auditionId;// 赛事ID',
	private Long seatId;// 对阵位ID',
	private Long userId;// id)',
	private Long playerId;// uwanUserId)',
	private Long barId;// 网吧ID',
	private Long netbarId;// NULL,
	private Long matchId;// 比赛ID',
	private Integer matchType;// 场次类型:1-吧内赛,2-网吧大战,3-预热赛,4-海选赛
	private Integer accountType;// 使用自己账号',
	private String qq;// 参加比赛的qq账号',
	private String ip;// 客户端IP地址',
	private String mac;// 客户端MAC地址',
	private Integer victory;// 2：失败)',
	private Integer smallDragon;// 小龙',
	private Integer bigDragon;// 大龙',
	private Integer redBlue;// 红蓝队，1：红队；2：蓝队',
	private Integer pushTower;// 推塔',
	private Integer gameTime;// 游戏时长',
	private Integer playerCount;// 该队玩家人数',
	private Integer grade;// 等级',
	private Long heroId;// 英雄ID',
	private Integer kills;// 杀人数',
	private Integer deaths;// 死亡数',
	private Integer assists;// 助攻数',
	private Long skill1;// 技能',
	private Long skill2;// 技能',
	private Long equip1;// 装备',
	private Long equip2;// NULL,
	private Long equip3;// NULL,
	private Long equip4;// NULL,
	private Long equip5;// NULL,
	private Long equip6;// NULL,
	private Long ornaments;// 饰品',
	private Integer money;// 金币',
	private Integer creeps;// 补兵',
	private Integer mvp;// 是否MVP，1：是；2：否',
	private Integer isKill3;// 是否3杀',
	private Integer isKill4;// 是否4杀',
	private Integer isKill5;// 是否5杀',
	private Integer totalHurt;// 对英雄的总伤害',
	private Double score;// 本局比赛的评分',
	private Double kda;// 本局比赛的评分',
	private String playerName;// 玩家名称',
	private String matchTime;// 比赛时间',
	private String roleName;// 召唤师名称',

	@Id
	@Column(name = "id", nullable = false, unique = true)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name = "audition_id")
	public Long getAuditionId() {
		return auditionId;
	}

	public void setAuditionId(Long auditionId) {
		this.auditionId = auditionId;
	}

	@Column(name = "seat_id")
	public Long getSeatId() {
		return seatId;
	}

	public void setSeatId(Long seatId) {
		this.seatId = seatId;
	}

	@Column(name = "user_id")
	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	@Column(name = "player_id")
	public Long getPlayerId() {
		return playerId;
	}

	public void setPlayerId(Long playerId) {
		this.playerId = playerId;
	}

	@Column(name = "bar_id")
	public Long getBarId() {
		return barId;
	}

	public void setBarId(Long barId) {
		this.barId = barId;
	}

	@Column(name = "netbar_id")
	public Long getNetbarId() {
		return netbarId;
	}

	public void setNetbarId(Long netbarId) {
		this.netbarId = netbarId;
	}

	@Column(name = "match_id")
	public Long getMatchId() {
		return matchId;
	}

	public void setMatchId(Long matchId) {
		this.matchId = matchId;
	}

	@Column(name = "account_type")
	public Integer getAccountType() {
		return accountType;
	}

	public void setAccountType(Integer accountType) {
		this.accountType = accountType;
	}

	@Column(name = "qq")
	public String getQq() {
		return qq;
	}

	public void setQq(String qq) {
		this.qq = qq;
	}

	@Column(name = "ip")
	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	@Column(name = "mac")
	public String getMac() {
		return mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

	@Column(name = "victory")
	public Integer getVictory() {
		return victory;
	}

	public void setVictory(Integer victory) {
		this.victory = victory;
	}

	@Column(name = "small_dragon")
	public Integer getSmallDragon() {
		return smallDragon;
	}

	public void setSmallDragon(Integer smallDragon) {
		this.smallDragon = smallDragon;
	}

	@Column(name = "big_dragon")
	public Integer getBigDragon() {
		return bigDragon;
	}

	public void setBigDragon(Integer bigDragon) {
		this.bigDragon = bigDragon;
	}

	@Column(name = "red_blue")
	public Integer getRedBlue() {
		return redBlue;
	}

	public void setRedBlue(Integer redBlue) {
		this.redBlue = redBlue;
	}

	@Column(name = "push_tower")
	public Integer getPushTower() {
		return pushTower;
	}

	public void setPushTower(Integer pushTower) {
		this.pushTower = pushTower;
	}

	@Column(name = "game_time")
	public Integer getGameTime() {
		return gameTime;
	}

	public void setGameTime(Integer gameTime) {
		this.gameTime = gameTime;
	}

	@Column(name = "player_count")
	public Integer getPlayerCount() {
		return playerCount;
	}

	public void setPlayerCount(Integer playerCount) {
		this.playerCount = playerCount;
	}

	@Column(name = "grade")
	public Integer getGrade() {
		return grade;
	}

	public void setGrade(Integer grade) {
		this.grade = grade;
	}

	@Column(name = "hero_id")
	public Long getHeroId() {
		return heroId;
	}

	public void setHeroId(Long heroId) {
		this.heroId = heroId;
	}

	@Column(name = "kills")
	public Integer getKills() {
		return kills;
	}

	public void setKills(Integer kills) {
		this.kills = kills;
	}

	@Column(name = "deaths")
	public Integer getDeaths() {
		return deaths;
	}

	public void setDeaths(Integer deaths) {
		this.deaths = deaths;
	}

	@Column(name = "assists")
	public Integer getAssists() {
		return assists;
	}

	public void setAssists(Integer assists) {
		this.assists = assists;
	}

	@Column(name = "skill1")
	public Long getSkill1() {
		return skill1;
	}

	public void setSkill1(Long skill1) {
		this.skill1 = skill1;
	}

	@Column(name = "skill2")
	public Long getSkill2() {
		return skill2;
	}

	public void setSkill2(Long skill2) {
		this.skill2 = skill2;
	}

	@Column(name = "equip1")
	public Long getEquip1() {
		return equip1;
	}

	public void setEquip1(Long equip1) {
		this.equip1 = equip1;
	}

	@Column(name = "equip2")
	public Long getEquip2() {
		return equip2;
	}

	public void setEquip2(Long equip2) {
		this.equip2 = equip2;
	}

	@Column(name = "equip3")
	public Long getEquip3() {
		return equip3;
	}

	public void setEquip3(Long equip3) {
		this.equip3 = equip3;
	}

	@Column(name = "equip4")
	public Long getEquip4() {
		return equip4;
	}

	public void setEquip4(Long equip4) {
		this.equip4 = equip4;
	}

	@Column(name = "equip5")
	public Long getEquip5() {
		return equip5;
	}

	public void setEquip5(Long equip5) {
		this.equip5 = equip5;
	}

	@Column(name = "equip6")
	public Long getEquip6() {
		return equip6;
	}

	public void setEquip6(Long equip6) {
		this.equip6 = equip6;
	}

	@Column(name = "ornaments")
	public Long getOrnaments() {
		return ornaments;
	}

	public void setOrnaments(Long ornaments) {
		this.ornaments = ornaments;
	}

	@Column(name = "money")
	public Integer getMoney() {
		return money;
	}

	public void setMoney(Integer money) {
		this.money = money;
	}

	@Column(name = "creeps")
	public Integer getCreeps() {
		return creeps;
	}

	public void setCreeps(Integer creeps) {
		this.creeps = creeps;
	}

	@Column(name = "mvp")
	public Integer getMvp() {
		return mvp;
	}

	public void setMvp(Integer mvp) {
		this.mvp = mvp;
	}

	@Column(name = "is_kill3")
	public Integer getIsKill3() {
		return isKill3;
	}

	public void setIsKill3(Integer isKill3) {
		this.isKill3 = isKill3;
	}

	@Column(name = "is_kill4")
	public Integer getIsKill4() {
		return isKill4;
	}

	public void setIsKill4(Integer isKill4) {
		this.isKill4 = isKill4;
	}

	@Column(name = "is_kill5")
	public Integer getIsKill5() {
		return isKill5;
	}

	public void setIsKill5(Integer isKill5) {
		this.isKill5 = isKill5;
	}

	@Column(name = "total_hurt")
	public Integer getTotalHurt() {
		return totalHurt;
	}

	public void setTotalHurt(Integer totalHurt) {
		this.totalHurt = totalHurt;
	}

	@Column(name = "score")
	public Double getScore() {
		return score;
	}

	public void setScore(Double score) {
		this.score = score;
	}

	@Column(name = "kda")
	public Double getKda() {
		return kda;
	}

	public void setKda(Double kda) {
		this.kda = kda;
	}

	@Column(name = "player_name")
	public String getPlayerName() {
		return playerName;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

	@Column(name = "match_time")
	public String getMatchTime() {
		return matchTime;
	}

	public void setMatchTime(String matchTime) {
		this.matchTime = matchTime;
	}

	@Column(name = "role_name")
	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	@Column(name = "match_type")
	public Integer getMatchType() {
		return matchType;
	}

	public void setMatchType(Integer matchType) {
		this.matchType = matchType;
	}

}
