package com.miqtech.master.service.user;

import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.user.UserGameDao;
import com.miqtech.master.entity.user.UserGame;
import com.miqtech.master.utils.DateUtils;
import com.miqtech.master.utils.JsonUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.vo.PageVO;

/**
 * 用户游戏资料service
 */
@Component
public class UserGameService {

	@Autowired
	private QueryDao queryDao;
	@Autowired
	private UserGameDao uerGameDao;

	public UserGame saveOrUpdate(UserGame userGame) {
		return uerGameDao.save(userGame);
	}

	/**
	 * 分页查询用户的游戏资料
	 */
	public PageVO getUserGames(long userId, int page, int pageSize) {
		PageVO result = new PageVO();
		int pageStart = ((page - 1) * pageSize);
		String sql = SqlJoiner
				.join("select ug.id, ug.game_level, ug.game_nickname, ug.game_server, ug.game_id, ai.pic game_pic, ai.name game_name ,ug.win_rate,ug.game_times",
						" from user_t_game ug left join activity_r_items ai on ai.id = ug.game_id ",
						" where ug.is_valid=1 and ug.user_id = :userId limit :pageStart,:pageSize");
		Map<String, Object> params = Maps.newHashMap();
		params.put("userId", userId);
		params.put("pageStart", pageStart);
		params.put("pageSize", pageSize);
		List<Map<String, Object>> games = queryDao.queryMap(sql, params);
		result.setList(games);
		if (CollectionUtils.isNotEmpty(games)) {
			String totalSql = SqlJoiner.join("select count(1) from user_t_game where user_id = ",
					String.valueOf(userId), " and is_valid = 1");
			Number count = queryDao.query(totalSql);
			long totalCount = count.longValue();
			result.setTotal(totalCount);
			if (page * pageSize >= totalCount) {
				result.setIsLast(1);
			}
		} else {
			result.setTotal(0);
		}
		return result;
	}

	public UserGame findByIdAndUserId(long id, long userIdLong) {
		return uerGameDao.findByIdAndUserId(id, userIdLong);
	}

	/**
	 * 抓取lol玩家的数据
	 * @param userGame
	 */
	@SuppressWarnings("unchecked")
	public void loadLolInfo(UserGame userGame) {
		int timeout = 5000;
		String baseInfoUrl = "http://lolbox.duowan.com/playerDetail.php?serverName=" + userGame.getGameServer()
				+ "&playerName=" + userGame.getGameNickname();

		Document doc;
		try {
			doc = Jsoup.connect(baseInfoUrl).timeout(timeout).get();//超时时间5s
			Elements outerDiv = doc.getElementsByAttributeValue("class", "mod-tabs-bd J_content");
			if (outerDiv.size() > 0) {
				Element content1 = outerDiv.get(0);
				Element tbody = content1.child(0).child(0).child(0);
				Element tr = tbody.child(1);
				Element gameTimes = tr.child(1);
				if (gameTimes != null) {
					userGame.setGameTimes(Integer.valueOf(gameTimes.html()));
				}
				Element winRate = tr.child(2);
				if (winRate != null) {
					userGame.setWinRate(winRate.html());
				}
			}
			Element serverName = doc.getElementById("serverName");
			String serverNameStr;
			if (serverName != null) {
				serverNameStr = URLEncoder.encode(URLEncoder.encode(serverName.html(), "UTF-8"), "UTF-8");
			} else {
				serverNameStr = URLEncoder.encode(URLEncoder.encode(userGame.getGameServer(), "UTF-8"), "UTF-8");
			}
			String playerName = URLEncoder.encode(URLEncoder.encode(userGame.getGameNickname(), "UTF-8"), "UTF-8");

			String tierAjaxUrl = "http://lolbox.duowan.com/ajaxGetWarzone.php?serverName=" + serverNameStr
					+ "&playerName=" + playerName;
			doc = Jsoup.connect(tierAjaxUrl).timeout(timeout).get();
			Element body = doc.body();
			String json = body.html();
			Map<String, Object> obj = JsonUtils.stringToObject(json, Map.class);
			String tier = (String) obj.get("tier");
			String rank = (String) obj.get("rank");
			if (tier != null && rank != null) {
				userGame.setGameLevel(tier + "/" + rank);
				userGame.setThirdUpdated(DateUtils.stringToDateYyyyMMddhhmm((String) obj.get("warzone_updated")));
			}
			uerGameDao.save(userGame);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<UserGame> findAllLOLValidData() {
		String sql = SqlJoiner
				.join("select * from user_t_game where game_nickname is not null and game_server is not null and is_valid=1 ",
						"and game_id =(select id from activity_r_items where name='英雄联盟')");
		return queryDao.queryObject(sql, UserGame.class);
	}
}
