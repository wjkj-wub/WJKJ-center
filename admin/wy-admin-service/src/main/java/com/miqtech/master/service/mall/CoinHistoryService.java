package com.miqtech.master.service.mall;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.consts.mall.CoinConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.mall.CoinHistoryDao;
import com.miqtech.master.entity.mall.CoinHistory;
import com.miqtech.master.entity.user.UserInfo;
import com.miqtech.master.service.user.UserInfoService;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.utils.StringUtils;
import com.miqtech.master.vo.PageVO;

/**
 * 金币收入支出历史操作service
 */
@Component
public class CoinHistoryService {
	@Autowired
	private CoinHistoryDao goldHistoryDao;
	@Autowired
	private UserInfoService userInfoService;
	@Autowired
	private QueryDao queryDao;

	public List<CoinHistory> findValidByUserIdIn(List<Long> userIds) {
		if (CollectionUtils.isNotEmpty(userIds)) {
			return goldHistoryDao.findByUserIdInAndValid(userIds, CommonConstant.INT_BOOLEAN_TRUE);
		}
		return null;
	}

	/**
	 * 保存金币收入支出历史
	 */
	public void save(CoinHistory goldHistory) {
		goldHistoryDao.save(goldHistory);
	}

	/**
	 * 保存金币收支历史(批量)
	 */
	public List<CoinHistory> save(List<CoinHistory> hs) {
		return (List<CoinHistory>) goldHistoryDao.save(hs);
	}

	/**
	 * 增加金币收支历史（公用方法）
	 */
	public int addGoldHistoryPub(Long userId, long targetId, int type, int coin, int direction) {
		// 1.增加/扣除（依据收支方向direction）用户金币
		if (null != userId) {
			UserInfo userInfo = userInfoService.findById(userId);
			if (null != userInfo) {
				int oldCoin = null == userInfo.getCoin() ? 0 : userInfo.getCoin();
				if (oldCoin + coin * direction < 0) { //金币不足
					return -1;
				}
				userInfo.setCoin(oldCoin + coin * direction);
				userInfoService.save(userInfo);
			}
		}

		// 2.增加金币历史
		CoinHistory coinHistory = new CoinHistory();
		coinHistory.setUserId(userId);
		coinHistory.setType(type); //收支类型：1-积分任务，2-邀请得积分，3-商品兑换
		coinHistory.setTargetId(targetId); //根据type来
		coinHistory.setCoin(coin); //金币数
		coinHistory.setDirection(direction); //收支方向：-1-支出，1-收入
		coinHistory.setValid(CommonConstant.INT_BOOLEAN_TRUE);
		coinHistory.setCreateUserId(userId);
		coinHistory.setCreateDate(new Date());
		save(coinHistory);

		return 1;
	}

	/**
	 * 统计用户金币收支历史（type=1：IOS，type=2：android），分页
	 */
	public Map<String, Object> coinListByUserId(long userId, int type, int page, int rows) {
		if (page <= 0) {
			page = 1;
		}
		if (rows <= 0) {
			rows = PageUtils.API_DEFAULT_PAGE_SIZE;//api分页大小默认10
		}
		Map<String, Object> params = new HashMap<>();
		params.put("pageStart", (page - 1) * rows);
		params.put("rows", rows);
		params.put("userId", userId);
		String sqlQuery = SqlJoiner.join(
				"select h.type, h.coin, h.direction, h.create_date date, h.target_id taskIdentify, t.ios_icon taskIcon_IOS, t.android_icon taskIcon_Android, t.name taskName, i.invited_telephone invitedTelephone, ic.icon commodityIcon, if(c.name is null,d.prize_name,c.name) commodityName from mall_r_coin_history h",
				" left join mall_t_task t on t.id=h.target_id and h.type=1",
				" left join mall_t_invite i on i.id=h.target_id and h.type=2",
				" left join mall_t_commodity c on c.id=h.target_id and (h.type=3 or h.type=6)",
				" left join mall_t_turntable_prize d on d.id=h.target_id  and h.type=8",
				" left join mall_r_commodity_icon ic on ic.commodity_id=h.target_id and ic.is_main=1 and h.type=3",
				" where h.create_date is not null and h.is_valid=1 and h.user_id=:userId",
				" order by h.create_date desc", " limit :pageStart, :rows");
		String sqlTotal = "select count(1) from mall_r_coin_history h  where h.create_date is not null and h.is_valid=1 and h.user_id="
				+ userId;
		List<Map<String, Object>> queyList = queryDao.queryMap(sqlQuery, params);
		//将属性名统一，再返给前端
		List<Map<String, Object>> newList = new ArrayList<>();
		for (Map<String, Object> map : queyList) {
			if (CoinConstant.HISTORY_TYPE_TASK.equals(map.get("type"))) { //任务
				Map<String, Object> tempMap = new HashMap<>();
				if (1 == type) { //IOS调用
					tempMap.put("icon", map.get("taskIcon_IOS"));
				} else if (2 == type) { //Android调用
					tempMap.put("icon", map.get("taskIcon_Android"));
				}

				tempMap.put("name", map.get("taskName"));
				tempMap.put("coin", map.get("coin"));
				tempMap.put("type", map.get("type"));
				tempMap.put("direction", map.get("direction"));
				tempMap.put("date", map.get("date"));
				newList.add(tempMap);
			} else if (CoinConstant.HISTORY_TYPE_INVITATION.equals(map.get("type"))) { //邀请
				Map<String, Object> tempMap = new HashMap<>();
				tempMap.put("icon", "null");
				tempMap.put("name", "被邀请人手机号：" + map.get("invitedTelephone"));
				tempMap.put("coin", map.get("coin"));
				tempMap.put("type", map.get("type"));
				tempMap.put("direction", map.get("direction"));
				tempMap.put("date", map.get("date"));
				newList.add(tempMap);
			} else if (CoinConstant.HISTORY_TYPE_COMMODITY.equals(map.get("type"))) { //商品
				Map<String, Object> tempMap = new HashMap<>();
				tempMap.put("icon", map.get("commodityIcon"));
				tempMap.put("name", map.get("commodityName"));
				tempMap.put("coin", map.get("coin"));
				tempMap.put("type", map.get("type"));
				tempMap.put("direction", map.get("direction"));
				tempMap.put("date", map.get("date"));
				newList.add(tempMap);
			} else if (CoinConstant.HISTORY_TYPE_CDKEY.equals(map.get("type"))) {// CDKEY 兑换
				Map<String, Object> tempMap = new HashMap<>();
				tempMap.put("icon", "null");
				tempMap.put("name", "CDKEY兑换");
				tempMap.put("coin", map.get("coin"));
				tempMap.put("type", map.get("type"));
				tempMap.put("direction", map.get("direction"));
				tempMap.put("date", map.get("date"));
				newList.add(tempMap);
			} else if (CoinConstant.HISTORY_TYPE_AWARD.equals(map.get("type"))) {// 娱乐赛或金币商城 奖品发放
				Map<String, Object> tempMap = new HashMap<>();
				tempMap.put("icon", "null");
				tempMap.put("name", "奖品发放");
				tempMap.put("coin", map.get("coin"));
				tempMap.put("type", map.get("type"));
				tempMap.put("direction", map.get("direction"));
				tempMap.put("date", map.get("date"));
				newList.add(tempMap);
			} else if (CoinConstant.HISTORY_TYPE_ROBTREASURE.equals(map.get("type"))) {//众筹夺宝
				Map<String, Object> tempMap = new HashMap<>();
				tempMap.put("icon", map.get("commodityIcon"));
				tempMap.put("name", "众筹夺宝-" + map.get("commodityName"));
				tempMap.put("coin", map.get("coin"));
				tempMap.put("type", map.get("type"));
				tempMap.put("direction", map.get("direction"));
				tempMap.put("date", map.get("date"));
				newList.add(tempMap);
			} else if (CoinConstant.HISTORY_TYPE_WHEEL_LOTTERY.equals(map.get("type"))) {//大转盘
				Map<String, Object> tempMap = new HashMap<>();
				tempMap.put("icon", map.get("commodityIcon"));
				tempMap.put("name", "欢乐大转盘-" + map.get("commodityName"));
				tempMap.put("coin", map.get("coin"));
				tempMap.put("type", map.get("type"));
				tempMap.put("direction", map.get("direction"));
				tempMap.put("date", map.get("date"));
				newList.add(tempMap);
			} else if (CoinConstant.HISTORY_TYPE_H5GAMEAWARD.equals(map.get("type"))) { //h5游戏
				Map<String, Object> tempMap = new HashMap<>();
				tempMap.put("icon", "null");
				tempMap.put("name", "游戏大厅");
				tempMap.put("coin", map.get("coin"));
				tempMap.put("type", map.get("type"));
				tempMap.put("direction", map.get("direction"));
				tempMap.put("date", map.get("date"));
				newList.add(tempMap);
			}
		}

		Map<String, Object> map = new HashMap<>();
		map.put("list", newList);
		Number total = queryDao.query(sqlTotal);
		if (page * rows >= total.intValue()) {
			map.put("isLast", 1); //是最后一页
		} else {
			map.put("isLast", 0); //不是最后一页
		}

		return map;
	}

	/**
	 * 获取用户某任务的完成情况
	 */
	public List<Map<String, Object>> getUserTaskHistory(Long taskId, Long userId, boolean onlyToday) {
		if (taskId == null || userId == null) {
			return null;
		}

		String dateCondition = onlyToday ? " AND DATE(h.create_date) = DATE(now())" : "";

		String sql = SqlJoiner.join("SELECT h.* FROM mall_r_coin_history h", " WHERE h.user_id = ", userId.toString(),
				" AND h.type = 1 AND h.target_id = ", taskId.toString(), " AND h.is_valid = 1", dateCondition, ";");

		return queryDao.queryMap(sql);
	}

	/**
	 * 分类统计用户获取的金币
	 */
	public List<Map<String, Object>> getCoinGroupByType(Long userId) {
		String sqlQuery = SqlJoiner.join("SELECT sum(coin) sum, type FROM mall_r_coin_history",
				" WHERE is_valid = 1 AND user_id = ", userId.toString(), " GROUP BY type");
		return queryDao.queryMap(sqlQuery);
	}

	/**
	 * 获取用户当天的某项任务完成情况
	 */
	public List<Map<String, Object>> getUserTodayTaskHistory(Long taskId, Long userId) {
		return getUserTaskHistory(taskId, userId, true);
	}

	/**
	 * 获取用户某任务的全部完成情况
	 */
	public List<Map<String, Object>> getUserAllTaskHistory(Long taskId, Long userId) {
		return getUserTaskHistory(taskId, userId, false);
	}

	/**
	 * 统计用户积分账面情况
	 */
	public List<Map<String, Object>> statisUserCoinHistory(String beginDate, String endDate, Integer threshold,
			String type, Integer limit) {
		if (StringUtils.isBlank(type)) {
			type = "1";
		}

		if (limit < 1) {
			limit = 10;
		} else if (limit > 30) {
			limit = 30;
		}

		// 产生查询条件
		String conditions = " WHERE 1 ";
		if (StringUtils.isNotBlank(beginDate)) {
			conditions = SqlJoiner.join(conditions, " AND h.create_date > ':beginDate'").replaceAll(":beginDate",
					beginDate);
		}
		if (StringUtils.isNotBlank(endDate)) {
			conditions = SqlJoiner.join(conditions, " AND h.create_date < ADDDATE(':endDate',INTERVAL 1 DAY)")
					.replaceAll(":endDate", endDate);
		}

		String orderField = null;
		if (type.equals("3")) {
			orderField = "sum";
		} else if (type.equals("2")) {
			orderField = "expendSum";
		} else {
			orderField = "incomeSum";// 默认查询收入
		}

		// 过滤条件
		String havings = "";
		if (threshold != null) {
			havings = " HAVING :havingField > :threshold".replaceAll(":havingField", orderField)
					.replaceAll(":threshold", threshold.toString());
		}

		String sql = SqlJoiner
				.join("SELECT user_id, SUM(h.coin * h.direction) sum, SUM( IF (h.direction = - 1, 0, h.coin) ) incomeSum, SUM(IF(h.direction = 1, 0, h.coin)) expendSum,",
						" u.username, u.nickname, h.create_date createDate",
						" FROM mall_r_coin_history h LEFT JOIN user_t_info u ON h.user_id = u.id :conditions",
						" GROUP BY user_id :havings ORDER BY :orderField DESC, sum DESC LIMIT 0, :limit")
				.replaceAll(":conditions", conditions).replaceAll(":havings", havings)
				.replaceAll(":orderField", orderField).replaceAll(":limit", limit.toString());

		return queryDao.queryMap(sql);
	}

	/**
	 * 获取积分排行
	 */
	public PageVO rankCoin(int page, Integer pageRows, String beginDate, String endDate, Integer expend) {
		String limit = "";
		if (page > 0) {
			Integer startRow = (page - 1) * pageRows;
			limit = SqlJoiner.join(" LIMIT ", startRow.toString(), ", ", pageRows.toString());
		}

		String sql = SqlJoiner.join("SELECT u.username, u.id userId, sum",
				" FROM ( SELECT user_id, sum(coin) sum, create_date, direction FROM mall_r_coin_history mch",
				buildRankConditions(beginDate, endDate, expend), " GROUP BY user_id ORDER BY sum DESC ", limit,
				" ) mch", " LEFT JOIN user_t_info u ON mch.user_id = u.id");

		String countSql = SqlJoiner.join("SELECT COUNT(1) FROM ( SELECT 1 FROM mall_r_coin_history mch ",
				buildRankConditions(beginDate, endDate, expend), " GROUP BY user_id ) mch");

		List<Map<String, Object>> ranks = queryDao.queryMap(sql);
		Number count = (Number) queryDao.query(countSql);
		if (count == null) {
			count = 0;
		}

		PageVO vo = new PageVO();
		vo.setList(ranks);
		vo.setTotal(count.longValue());
		vo.setIsLast(page * PageUtils.ADMIN_DEFAULT_PAGE_SIZE >= count.intValue() ? 1 : 0);
		return vo;
	}

	/**
	 * 产生排名的查询条件
	 */
	private String buildRankConditions(String beginDate, String endDate, Integer expend) {
		String condition = " WHERE mch.is_valid = 1";

		if (StringUtils.isNotBlank(beginDate)) {
			condition = SqlJoiner.join(condition, " AND mch.create_date >= ':beginDate' ").replaceAll(":beginDate",
					beginDate);
		}
		if (StringUtils.isNotBlank(endDate)) {
			condition = SqlJoiner.join(condition, " AND mch.create_date < ADDDATE(':endDate', INTERVAL 1 DAY)")
					.replaceAll(":endDate", endDate);
		}
		Integer direction = null;
		if ("1".equals(expend.toString())) {
			direction = -1;
		} else {
			direction = 1;
		}
		condition = SqlJoiner.join(condition, " AND mch.direction = :direction").replaceAll(":direction",
				direction.toString());

		return condition;
	}

	/**
	 * 查询用户金币历史
	 */
	public PageVO userHistory(int page, String username, Long userId, String beginDate, String endDate,
			Integer direction, String orderColumn, String orderType) {
		String conditon = buildHistoryCondition(username, userId, beginDate, endDate, direction);

		if (StringUtils.isBlank(orderColumn)) {
			orderColumn = "mch.create_date";
		}
		if (StringUtils.isBlank(orderType)) {
			orderType = "DESC";
		}

		// 分页查询
		Integer pageRows = PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		Integer startRow = (page - 1) * pageRows;
		String pageCondition = " LIMIT :startRow, :pageRow".replaceAll(":startRow", startRow.toString())
				.replaceAll(":pageRow", pageRows.toString());
		String sql = SqlJoiner
				.join("SELECT mch.id, mch.user_id userId, mch.type, mch.target_id targetId, mch.coin, mch.direction, mch.is_valid isValid, mch.create_date createDate, ",
						" IF ( mch.type = ", CoinConstant.HISTORY_TYPE_TASK.toString(), ", t.name, IF ( mch.type = ",
						CoinConstant.HISTORY_TYPE_INVITATION.toString(), ", '邀请', IF (mch.type = ",
						CoinConstant.HISTORY_TYPE_COMMODITY.toString(), ", c. NAME, IF (mch.type = ",
						CoinConstant.HISTORY_TYPE_CDKEY.toString(), ", 'CDKEY兑换', IF (mch.type = ",
						CoinConstant.HISTORY_TYPE_AWARD.toString(), ", '奖品发放', IF (mch.type = ",
						CoinConstant.HISTORY_TYPE_H5GAMEAWARD.toString(), ", '游戏排名奖励' ,IF (mch.type = ",
						CoinConstant.HISTORY_TYPE_ROBTREASURE.toString(), ", '众筹夺宝奖励',IF (mch.type = ",
						CoinConstant.HISTORY_TYPE_WHEEL_LOTTERY.toString(), ", '大转盘抽奖奖励' ,mch.type))))))", " ) ) typeName",
						" FROM mall_r_coin_history mch LEFT JOIN user_t_info u ON mch.user_id = u.id",
						" LEFT JOIN mall_t_task t ON mch.target_id = t.id AND mch.type = 1",
						" LEFT JOIN mall_t_commodity c ON mch.target_id = c.id AND mch.type = 3", conditon,
						" ORDER BY :orderColumn :orderType", pageCondition)
				.replaceAll(":orderColumn", orderColumn).replaceAll(":orderType", orderType);
		List<Map<String, Object>> list = queryDao.queryMap(sql);

		// 查询总数
		String countSql = SqlJoiner.join(
				"SELECT COUNT(1) FROM mall_r_coin_history mch left join user_t_info u on mch.user_id = u.id", conditon);
		Number count = queryDao.query(countSql);
		if (count == null) {
			count = 0;
		}

		PageVO vo = new PageVO();
		vo.setList(list);
		vo.setTotal(count.longValue());
		vo.setIsLast(page * PageUtils.ADMIN_DEFAULT_PAGE_SIZE >= count.intValue() ? 1 : 0);
		return vo;
	}

	/**
	 * 产生查询条件
	 */
	private String buildHistoryCondition(String username, Long userId, String beginDate, String endDate,
			Integer direction) {
		// 产生查询条件
		String condition = " WHERE mch.is_valid = 1";
		if (userId != null) {
			condition = SqlJoiner.join(condition, " AND mch.user_id = :userId").replaceAll(":userId",
					userId.toString());
		}
		if (StringUtils.isNotBlank(username)) {
			condition = SqlJoiner.join(condition, " AND u.username = ':username'").replaceAll(":username", username);
		}
		if (direction != null) {
			condition = SqlJoiner.join(condition, " AND mch.direction = :direction").replaceAll(":direction",
					direction.toString());
		}
		if (StringUtils.isNotBlank(beginDate)) {
			condition = SqlJoiner.join(condition, " AND mch.create_date >= ':beginDate'").replaceAll(":beginDate",
					beginDate);
		}
		if (StringUtils.isNotBlank(endDate)) {
			condition = SqlJoiner.join(condition, " AND mch.create_date < ADDDATE(':endDate', INTERVAL 1 DAY)")
					.replaceAll(":endDate", endDate);
		}
		return condition;
	}

	/**
	 * 统计用户的积分情况
	 */
	public Map<String, Object> userCoinReport(String username, Long userId, String beginDate, String endDate,
			Integer direction) {

		String conditon = buildHistoryCondition(username, userId, beginDate, endDate, direction);

		String userCondition = " ON mch.user_id = u.id";
		if (userId != null) {
			userCondition = SqlJoiner.join(userCondition, " AND mch.user_id = ", userId.toString());
		}
		if (StringUtils.isNotBlank(username)) {
			userCondition = SqlJoiner.join(userCondition, " AND u.username = '", username, "'");
		}

		// 分页查询
		String sql = SqlJoiner.join("SELECT t.*, IF ( ISNULL(b.is_valid), 0, b.is_valid ) userValid FROM",
				" (SELECT sum(mch.coin) sum, u.id userId, u.username, u.coin",
				" FROM mall_r_coin_history mch JOIN user_t_info u", userCondition,
				" LEFT JOIN mall_t_task t ON mch.target_id = t.id AND mch.type = 1",
				" LEFT JOIN mall_t_commodity c ON mch.target_id = c.id AND mch.type = 3", conditon, ") t",
				" LEFT JOIN user_t_black b ON b.user_id = t.userId AND b.is_valid = 1 LIMIT 1");

		return queryDao.querySingleMap(sql);
	}
}
