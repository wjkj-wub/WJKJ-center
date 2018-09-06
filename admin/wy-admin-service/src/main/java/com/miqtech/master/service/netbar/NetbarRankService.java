package com.miqtech.master.service.netbar;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.consts.uwan.UwanNetbarConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.netbar.NetbarRankDao;
import com.miqtech.master.entity.netbar.NetbarRank;
import com.miqtech.master.service.common.StringRedisOperateService;
import com.miqtech.master.utils.SqlJoiner;

@Component
public class NetbarRankService {

	private static final Integer MAX_BATCH_UPDATE_COUNT = 500;

	private static final Integer COLUMN_TYPE_HIGHEST_KDA = 1;
	private static final Integer COLUMN_TYPE_HIGHEST_LOSE_KDA = 2;
	private static final Integer COLUMN_TYPE_MOST_KILL = 3;
	private static final Integer COLUMN_TYPE_MOST_ASSIST = 4;

	@Autowired
	private StringRedisOperateService stringRedisOperateService;
	@Autowired
	private QueryDao queryDao;
	@Autowired
	private NetbarRankDao netbarRankDao;

	/**
	 * 批量保存排行信息
	 */
	public void batchSave(List<NetbarRank> ranks) {
		if (CollectionUtils.isEmpty(ranks)) {
			return;
		}

		netbarRankDao.save(ranks);
	}

	/**
	 * 获取上一次week值
	 */
	public Integer getLastWeek() {
		String sql = SqlJoiner.join("SELECT DISTINCT `week` FROM `netbar_rank` ORDER BY WEEK DESC LIMIT 1");
		Map<String, Object> weekMap = queryDao.querySingleMap(sql);
		Integer week = MapUtils.getInteger(weekMap, "week");
		if (week == null) {
			week = 0;
		}
		return week;
	}

	/**
	 * 统计网吧排名
	 */
	public List<Map<String, Object>> statisticNetbarRank() {
		String sql = SqlJoiner.join(
				"SELECT t.*, ( matchCount + memberCount * 0.2 + barMatchCount * 0.1 + winBarMatchCount * 0.2 ) score",
				" FROM ( SELECT n.id netbarId, n.`name`, count(DISTINCT amdl.match_id) matchCount, count(DISTINCT amdl.match_id) barMatchCount,",
				" count(DISTINCT amdl.player_id) memberCount, count( DISTINCT IF ( amdl.victory = 1, amdl.match_id, NULL ) ) winBarMatchCount",
				" FROM uwan_netbar un JOIN netbar_t_info n ON un.netbar_id = n.id AND un.netbar_type = 1 AND n.is_valid = 1",
				" LEFT JOIN audition_match_detail_lol amdl ON un.netbar_id = amdl.netbar_id",
				" AND YEARWEEK( ADDDATE( date_format( amdl.match_time, '%Y-%m-%d %H:%i:%s' ), INTERVAL -1 DAY ) ) = YEARWEEK( ADDDATE(now(), INTERVAL -1 DAY) ) -1",
				" WHERE un.source = 4 GROUP BY n.id ) t ORDER BY score DESC, matchCount DESC, barMatchCount DESC, memberCount DESC, winBarMatchCount DESC, netbarId");
		return queryDao.queryMap(sql);
	}

	/**
	 * 统计网吧最高场均评分
	 */
	public List<Map<String, Object>> statisticNetbarHighestAvgKda() {
		return statisticNetbarHighestColumn(COLUMN_TYPE_HIGHEST_KDA);
	}

	/**
	 * 统计网吧最高败方场均评分
	 */
	public List<Map<String, Object>> statisticNetbarHighestAvgLoseKda() {
		return statisticNetbarHighestColumn(COLUMN_TYPE_HIGHEST_LOSE_KDA);
	}

	/**
	 * 统计网吧最高场均击杀数
	 */
	public List<Map<String, Object>> statisticNetbarHighestAvgKills() {
		return statisticNetbarHighestColumn(COLUMN_TYPE_MOST_KILL);
	}

	/**
	 * 统计网吧场均最高助攻数
	 */
	public List<Map<String, Object>> statisticNetbarHighestAvgAssist() {
		return statisticNetbarHighestColumn(COLUMN_TYPE_MOST_ASSIST);
	}

	/**
	 * 根据类型统计相应字段
	 */
	private List<Map<String, Object>> statisticNetbarHighestColumn(Integer columnType) {
		String column = null;
		String condition = "";
		if (COLUMN_TYPE_HIGHEST_KDA.equals(columnType)) {
			column = "kda";
		} else if (COLUMN_TYPE_HIGHEST_LOSE_KDA.equals(columnType)) {
			column = "kda";
			condition = SqlJoiner.join(condition, " AND amdl.victory != 1");
		} else if (COLUMN_TYPE_MOST_KILL.equals(columnType)) {
			column = "kills";
		} else if (COLUMN_TYPE_MOST_ASSIST.equals(columnType)) {
			column = "assists";
		} else {
			column = "kda";
		}

		String sql = SqlJoiner.join(
				"SELECT t.* FROM ( SELECT t.* FROM ( SELECT un.netbar_id netbarId, amdl.player_id playerId, ROUND(AVG(amdl.",
				column, "), 2) ", column, "",
				" FROM uwan_netbar un JOIN audition_match_detail_lol amdl ON un.netbar_id = amdl.netbar_id",
				" WHERE un.netbar_type = 1 AND un.source = 4 AND amdl.player_id IS NOT NULL", condition,
				" AND YEARWEEK( ADDDATE( date_format( amdl.match_time, '%Y-%m-%d %H:%i:%s' ), INTERVAL -1 DAY ) ) = YEARWEEK( ADDDATE(now(), INTERVAL -1 DAY) ) -1",
				" GROUP BY amdl.netbar_id, amdl.player_id ) t", " ORDER BY t.netbarId, t.", column,
				" DESC ) t GROUP BY netbarId ORDER BY netbarId");
		return queryDao.queryMap(sql);
	}

	public void generateNetbarRank() {
		// 检查比赛数据是否在同步中,须等待同步完成再进行计算
		String syncKey = UwanNetbarConstant.REDIS_KEY_UWAN_NETBAR_MATCH_SYNCING;
		String syncing = stringRedisOperateService.getData(syncKey);
		int waitMills = 5 * 60 * 1000;// 同步等待频次(5分钟)
		while (CommonConstant.INT_BOOLEAN_TRUE.toString().equals(syncing)) {
			try {
				Thread.sleep(waitMills);
			} catch (Exception e) {
			}
			syncing = stringRedisOperateService.getData(syncKey);
		}

		// 统计积分并进行排行
		List<Map<String, Object>> ranks = this.statisticNetbarRank();
		if (CollectionUtils.isEmpty(ranks)) {
			return;
		}

		// 统计各项指标
		List<Map<String, Object>> kdas = this.statisticNetbarHighestAvgKda();
		List<Map<String, Object>> loseKdas = this.statisticNetbarHighestAvgLoseKda();
		List<Map<String, Object>> kills = this.statisticNetbarHighestAvgKills();
		List<Map<String, Object>> assists = this.statisticNetbarHighestAvgAssist();

		// 计算当前周数
		Integer lastWeek = this.getLastWeek();
		Integer week = lastWeek + 1;

		Date now = new Date();
		List<NetbarRank> updateRanks = Lists.newArrayList();
		for (int i = 0; i < ranks.size(); i++) {
			Map<String, Object> rankMap = ranks.get(i);

			NetbarRank updateRank = new NetbarRank();
			updateRank.setWeek(week);
			Integer rank = i + 1;
			updateRank.setRank(rank);

			// 设置网吧统计字段
			Long netbarId = MapUtils.getLong(rankMap, "netbarId");
			updateRank.setNetbarId(netbarId);
			Double score = MapUtils.getDouble(rankMap, "score");
			updateRank.setScore(score);
			Integer matchCount = MapUtils.getInteger(rankMap, "matchCount");
			updateRank.setMatchCount(matchCount);
			Integer barMatchCount = MapUtils.getInteger(rankMap, "barMatchCount");
			updateRank.setBarMatchCount(barMatchCount);
			Integer winBarMatchCount = MapUtils.getInteger(rankMap, "winBarMatchCount");
			updateRank.setWinBarMatchCount(winBarMatchCount);
			Integer memberCount = MapUtils.getInteger(rankMap, "memberCount");
			updateRank.setMemberCount(memberCount);

			// 设置最强用户
			Map<String, Object> kda = popNetbarMap(kdas, netbarId);
			Double mvpKda = MapUtils.getDouble(kda, "kda");
			Long mvpUserId = MapUtils.getLong(kda, "playerId");
			updateRank.setMvpKda(mvpKda);
			updateRank.setMvpUwanUserId(mvpUserId);

			Map<String, Object> loseKda = popNetbarMap(loseKdas, netbarId);
			Double loseMvpKda = MapUtils.getDouble(loseKda, "kda");
			Long loseMvpUserId = MapUtils.getLong(loseKda, "playerId");
			updateRank.setLoseMvpKda(loseMvpKda);
			updateRank.setLoseMvpUwanUserId(loseMvpUserId);

			Map<String, Object> kill = popNetbarMap(kills, netbarId);
			Integer mostKillsCount = MapUtils.getInteger(kill, "kills");
			Long mostKillsUserId = MapUtils.getLong(kill, "playerId");
			updateRank.setMostKillCount(mostKillsCount);
			updateRank.setMostKillUwanUserId(mostKillsUserId);

			Map<String, Object> assist = popNetbarMap(assists, netbarId);
			Integer mostAssistCount = MapUtils.getInteger(assist, "assists");
			Long mostAssistUserId = MapUtils.getLong(assist, "playerId");
			updateRank.setMostAssistCount(mostAssistCount);
			updateRank.setMostAssistUwanUserId(mostAssistUserId);
			updateRanks.add(updateRank);

			// 设置基础值
			updateRank.setValid(CommonConstant.INT_BOOLEAN_TRUE);
			updateRank.setUpdateDate(now);
			updateRank.setCreateDate(now);

			// 分批保存
			if (updateRanks.size() % MAX_BATCH_UPDATE_COUNT == 0) {
				this.batchSave(updateRanks);
				updateRanks = Lists.newArrayList();
			}
		}

		// 保存剩余的排行信息
		this.batchSave(updateRanks);
	}

	/**
	 * 从列表中移出一条当前网吧相关数据
	 */
	private Map<String, Object> popNetbarMap(List<Map<String, Object>> maps, Long netbarId) {
		if (netbarId == null || CollectionUtils.isEmpty(maps)) {
			return null;
		}

		for (Iterator<Map<String, Object>> mapsIt = maps.iterator(); mapsIt.hasNext();) {
			Map<String, Object> map = mapsIt.next();
			Long mapNetbarId = MapUtils.getLong(map, "netbarId");
			if (netbarId.equals(mapNetbarId)) {
				mapsIt.remove();
				return map;
			}
		}

		return null;
	}
}
