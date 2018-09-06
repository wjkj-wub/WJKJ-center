package com.miqtech.master.service.game;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.consts.UserConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.game.GameInfoDao;
import com.miqtech.master.dao.user.UserFavorDao;
import com.miqtech.master.entity.game.GameImg;
import com.miqtech.master.entity.game.GameInfo;
import com.miqtech.master.entity.user.UserFavor;
import com.miqtech.master.utils.BeanUtils;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.vo.PageVO;

@Component
public class GameInfoService {

	private static final int TYPE_USER_FAVOR_TYPE = 2;

	@Autowired
	private QueryDao queryDao;
	@Autowired
	private GameInfoDao gameInfoDao;
	@Autowired
	private UserFavorDao userFavorDao;

	/**
	 * 查所有有手游吧id，name
	 */
	public List<Map<String, Object>> findAllGameIdandName() {
		String sqlQuery = "select id,name gameName from game_t_info where is_valid=1";
		return queryDao.queryMap(sqlQuery);
	}

	/**
	 * 获取 手游 列表
	 */
	public PageVO getGames(int page, int rows, Long userId) {
		// 查询手游列表
		String sqlGames = null;
		if (userId != null) {
			sqlGames = "SELECT g.id, name, version, intro, des, icon icon,android_file_size, ios_file_size, url_android, url_ios, download_count, cover cover, count(uf.id) favor_count, if(count(uuf.id) > 0, 1, 0) has_favor "
					+ "FROM game_t_info g LEFT JOIN user_r_favor uf ON uf.sub_id = g.id AND uf.type = 2 and uf.is_valid = 1 "
					+ "left join user_r_favor uuf on uuf.sub_id = g.id and uuf.type = "
					+ TYPE_USER_FAVOR_TYPE
					+ " and uuf.user_id = "
					+ userId
					+ " and uuf.is_valid = 1 "
					+ "WHERE g.is_valid = 1 GROUP BY g.id limit :pageStart, :pageNum";
		} else {
			sqlGames = "select g.id, name, version, intro, des, icon icon,android_file_size, ios_file_size, url_android, url_ios, download_count, cover cover, count(uf.id) favor_count, -1 has_favor "
					+ "from game_t_info g left join user_r_favor uf on uf.sub_id = g.id and uf.type = "
					+ TYPE_USER_FAVOR_TYPE + " where g.is_valid = 1 group by g.id limit :pageStart, :pageNum";
		}

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("pageStart", (page - 1) * rows);
		params.put("pageNum", rows);

		// 统计分页
		String sqlCount = "select count(1) from game_t_info where is_valid = 1";
		PageVO vo = new PageVO();
		vo.setList(queryDao.queryMap(sqlGames, params));
		BigInteger total = (BigInteger) queryDao.query(sqlCount);
		if (page * rows >= total.intValue()) {
			vo.setIsLast(1);
		}

		return vo;
	}

	/**
	 * 查询手游有效总数
	 * @return
	 */
	public int getTotal() {
		String sqlCount = "select count(1) from game_t_info where is_valid = 1";
		Number total = (Number) queryDao.query(sqlCount);
		return total.intValue();
	}

	/**
	 * 获取手游详情
	 */
	public Map<String, Object> getGame(long id, Long userId) {
		// 查询手游基本信息
		String sqlGame = "select id, name, android_file_size, ios_file_size, url_android, url_ios, download_count, version, intro, des, icon icon, cover cover from game_t_info where is_valid = 1 and id="
				+ id;
		Map<String, Object> game = queryDao.querySingleMap(sqlGame);

		// 成功查询到手游信息，补充更多信息
		if (game != null) {
			// 补充手游图片列表
			String sqlGameImgs = "select url url from game_r_imgs where is_valid = 1 and game_id = " + id;
			game.put("imgs", queryDao.queryMap(sqlGameImgs));

			// 补充收藏数
			String sqlGameFavorCount = "select count(1) from user_r_favor where sub_id = " + id + " and type = "
					+ UserConstant.FAVOR_TYPE_GAME + " and is_valid = 1";
			game.put("favor_count", queryDao.query(sqlGameFavorCount));

			// 补充当前用户是否已收藏
			int hasFavor = -1;// 默认未登陆
			if (userId != null) {
				String sqlUserGameFavorCount = "select count(1) from user_r_favor where sub_id = " + id
						+ " and type = " + UserConstant.FAVOR_TYPE_GAME + " and user_id = " + userId
						+ " and is_valid = 1";
				BigInteger userFavor = (BigInteger) queryDao.query(sqlUserGameFavorCount);

				if (userFavor.intValue() > 0) {
					hasFavor = 1;
				} else {
					hasFavor = 0;
				}
			}
			game.put("has_favor", hasFavor);
		}

		return game;
	}

	/**
	 * 增加下载量，并获得下载地址
	 */
	public Map<String, Object> download(long id) {
		//更新下载量
		String sqlUpdateDownloadCount = "update game_t_info set download_count = download_count + 1 where id = " + id;
		queryDao.update(sqlUpdateDownloadCount);
		//添加下载记录
		String insertDownloadSql = "insert into game_t_download (user_id, game_id, is_valid, create_date) values (0,"
				+ id + ",1,now()) ";
		queryDao.update(insertDownloadSql);

		// 查询 URL 及 最新下载量
		String sqlGetUrls = "select url_android, url_ios, download_count from game_t_info where id = " + id;
		return queryDao.querySingleMap(sqlGetUrls);
	}

	/**
	 * 收藏手游
	 */
	public Map<String, Object> favor(long gameId, long userId) {
		boolean isFavor = false;// 标记是否已关注，最终记录
		UserFavor userFavor = userFavorDao.findByUserIdAndSubIdAndType(userId, gameId, TYPE_USER_FAVOR_TYPE);
		// 根据收藏情况，做相应动作
		if (userFavor != null) {// 存在收藏记录
			if (CommonConstant.INT_BOOLEAN_TRUE.equals(userFavor.getValid())) {
				userFavor.setValid(CommonConstant.INT_BOOLEAN_FALSE);
				isFavor = false;
			} else {
				userFavor.setValid(CommonConstant.INT_BOOLEAN_TRUE);
				isFavor = true;
			}
			userFavorDao.save(userFavor);
		} else {// 不存在收藏记录：添加收藏记录，且为有效
			userFavor = new UserFavor();
			userFavor.setCreateDate(new Date());
			userFavor.setUserId(userId);
			userFavor.setSubId(gameId);
			userFavor.setValid(1);
			userFavor.setType(TYPE_USER_FAVOR_TYPE);
			userFavorDao.save(userFavor);
			isFavor = true;
		}
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("isFavor", isFavor);
		return result;
	}

	/**
	 * 推荐手游列表
	 */
	public List<Map<String, Object>> recommendations(Integer rows) {
		String recommendSql = "select id, if(isnull(icon), '', icon) icon,if(isnull(cover), '', cover) cover from game_t_info where is_valid = 1 and is_recommend = 1";
		if (rows != null) {
			recommendSql += " order by create_date desc  limit 0, " + rows;
		}
		return queryDao.queryMap(recommendSql);
	}

	/**
	 * 查找用户收藏的手游信息
	 */
	public PageVO favedGames(Long userId, int page, int pageSize) {
		List<Map<String, Object>> result = null;
		String sql = "";
		Map<String, Object> total = null;
		int start = (page - 1) * pageSize;
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("userId", userId);
		sql = "select count(*) total from game_t_info a,user_r_favor b where b.type=2 and b.sub_id=a.id and b.is_valid=1 and  b.user_id=:userId order by b.create_date desc";
		total = queryDao.querySingleMap(sql, params);
		params.put("start", start);
		params.put("pageSize", pageSize);
		sql = "select a.id,icon,name name,intro from game_t_info a,user_r_favor b where b.type=2 and b.is_valid=1  and b.sub_id=a.id and b.user_id=:userId order by b.create_date desc limit :start,:pageSize";
		result = queryDao.queryMap(sql, params);
		PageVO vo = new PageVO();
		if (CollectionUtils.isNotEmpty(result)) {
			vo.setList(result);
			BigInteger bi = (BigInteger) total.get("total");
			if (page * pageSize >= bi.intValue()) {
				vo.setIsLast(1);
			}
		} else {
			vo.setIsLast(1);
			vo.setList(new ArrayList<Map<String, Object>>());

		}
		return vo;
	}

	/**
	 * 根据id查找手游信息
	 */
	public GameInfo findById(Long id) {
		return gameInfoDao.findOne(id);
	}

	/**
	 * 查找某个手游的轮播图片
	 */
	public List<GameImg> queryImgByGameId(Long id) {
		String sql = "select * from game_r_imgs where game_id=:id";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("id", id);
		return queryDao.queryObjectList(sql, params, GameImg.class);
	}

	/**
	 * 搜索手游
	 */
	public List<Map<String, Object>> search(String gameName) {
		if (StringUtils.isBlank(gameName)) {
			return null;
		}
		gameName = StringUtils.replace(gameName, "'", "");
		String querySql = " select id,name,version,is_recommend,download_count,if(isnull(icon), '', icon) icon ,cover ,url_android,url_ios,android_file_size ,ios_file_size from game_t_info where name like '%"
				+ gameName + "%'";
		return queryDao.queryMap(querySql);
	}

	/**
	 * 新游排行(最近180天内下载数据最多的手游列表)
	 */
	public PageVO newest(int page, int pageSize) {
		Map<String, Object> params = Maps.newHashMap();
		int start = (page - 1) * pageSize;
		params.put("start", start);
		params.put("pageSize", pageSize);
		String queryDateSql = "select g.id,g.name,g.download_count,g.des,g.icon,g.url_android,g.url_ios,g.android_file_size from game_t_info g left join game_t_download gd on g.id= gd.game_id where date_sub(now(), INTERVAL 180 DAY)<g.create_date and g.is_valid=1 group by g.id order by count(gd.id) desc limit :start,:pageSize ";
		List<Map<String, Object>> dataList = queryDao.queryMap(queryDateSql, params);
		if (CollectionUtils.isEmpty(dataList)) {
			return null;
		}
		String countSql = "select count(1) from game_t_info where date_sub(now(), INTERVAL 180 DAY)<create_date and is_valid=1 ";
		long total = ((Number) queryDao.query(countSql)).longValue();
		PageVO result = new PageVO();
		result.setCurrentPage(page);
		result.setList(dataList);
		result.setTotal(total);
		result.setIsLast(PageUtils.apiIsBottom(page, total));
		return result;
	}

	/**
	 * 周热门手游列表数据
	 */
	public PageVO weeklyHot(int page, int pageSize) {
		Map<String, Object> params = Maps.newHashMap();
		int start = (page - 1) * pageSize;
		params.put("start", start);
		params.put("pageSize", pageSize);
		String queryDateSql = "select g.id,g.name,g.download_count,g.des,g.icon,g.url_android,g.url_ios,g.android_file_size from game_t_info g left join game_t_download gd on g.id= gd.game_id where g.is_valid=1 group by g.id order by count(gd.id) desc limit :start,:pageSize ";
		List<Map<String, Object>> dataList = queryDao.queryMap(queryDateSql, params);
		if (CollectionUtils.isEmpty(dataList)) {
			return null;
		}
		String countSql = "select count(1) from game_t_info where date_sub(now(), INTERVAL 7 DAY)<create_date and  is_valid=1";
		long total = ((Number) queryDao.query(countSql)).longValue();
		PageVO result = new PageVO();
		result.setCurrentPage(page);
		result.setList(dataList);
		result.setTotal(total);
		result.setIsLast(PageUtils.apiIsBottom(page, total));
		return result;
	}

	/**
	 * 热门手游列表数据
	 */
	public PageVO hot(int page, int pageSize) {
		Map<String, Object> params = Maps.newHashMap();
		int start = (page - 1) * pageSize;
		params.put("start", start);
		params.put("pageSize", pageSize);
		String queryDateSql = "select id,name,download_count,des,icon,url_android,url_ios,android_file_size from game_t_info where is_valid=1 order by download_count desc limit :start,:pageSize ";
		List<Map<String, Object>> dataList = queryDao.queryMap(queryDateSql, params);
		if (CollectionUtils.isEmpty(dataList)) {
			return null;
		}
		String countSql = "select count(1) from game_t_info where is_valid=1";
		long total = ((Number) queryDao.query(countSql)).longValue();
		PageVO result = new PageVO();
		result.setCurrentPage(page);
		result.setList(dataList);
		result.setTotal(total);
		result.setIsLast(PageUtils.apiIsBottom(page, total));
		return result;
	}

	/**
	 * 后台分页
	 */
	public PageVO adminPage(int page, Map<String, String> searchParams) {
		String condition = " WHERE is_valid = 1";
		String totalCondition = condition;
		Map<String, Object> params = Maps.newHashMap();

		if (MapUtils.isNotEmpty(searchParams)) {
			String name = MapUtils.getString(searchParams, "name");
			if (StringUtils.isNotBlank(name)) {
				String likeName = "%" + name + "%";
				params.put("name", likeName);
				condition = SqlJoiner.join(condition, " AND name LIKE :name");
				totalCondition = SqlJoiner.join(totalCondition, " AND name LIKE '", likeName, "'");
			}
		}

		String totalSql = SqlJoiner.join("SELECT COUNT(1) FROM game_t_info g", totalCondition);
		Number total = queryDao.query(totalSql);
		List<Map<String, Object>> list = null;
		if (total != null && total.intValue() > 0) {
			String limitSql = PageUtils.getLimitSql(page);
			String sql = SqlJoiner.join(
					"SELECT id, name, des, version, score, intro, download_count downloadCount, url_ios urlIos,",
					" url_android urlAndroid, ios_file_size iosFileSize, android_file_size androidFileSize,",
					" icon, cover, is_valid valid FROM game_t_info", condition, limitSql);
			list = queryDao.queryMap(sql, params);
		}

		return new PageVO(page, list, total);
	}

	public GameInfo save(GameInfo game) {
		return gameInfoDao.save(game);
	}

	public GameInfo saveOrUpdate(GameInfo game) {
		if (game == null) {
			return null;
		}

		Date now = new Date();
		game.setUpdateDate(now);
		if (game.getId() != null) {
			GameInfo old = findById(game.getId());
			game = BeanUtils.updateBean(old, game);
		} else {
			game.setCreateDate(now);
			game.setValid(CommonConstant.INT_BOOLEAN_TRUE);
		}

		return save(game);
	}

	public GameInfo disabled(Long id) {
		if (id == null) {
			return null;
		}

		GameInfo game = new GameInfo();
		game.setId(id);
		game.setValid(CommonConstant.INT_BOOLEAN_FALSE);
		return saveOrUpdate(game);
	}
}