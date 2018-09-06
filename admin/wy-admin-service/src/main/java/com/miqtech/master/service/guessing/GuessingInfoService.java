package com.miqtech.master.service.guessing;

import com.miqtech.master.consts.CacheKeyConstant;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.consts.mall.CoinConstant;
import com.miqtech.master.consts.official.GuessingConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.guessing.GuessingInfoDao;
import com.miqtech.master.dao.guessing.GuessingInfoItemDao;
import com.miqtech.master.dao.mall.CoinHistoryDao;
import com.miqtech.master.dao.user.UserInfoDao;
import com.miqtech.master.entity.guessing.GuessingInfo;
import com.miqtech.master.entity.guessing.GuessingInfoItem;
import com.miqtech.master.entity.mall.CoinHistory;
import com.miqtech.master.entity.user.UserInfo;
import com.miqtech.master.service.common.StringRedisOperateService;
import com.miqtech.master.utils.DateUtils;
import com.miqtech.master.utils.ExcelUtils;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.vo.PageVO;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.support.atomic.RedisAtomicInteger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * 竞猜  Service
 * @author zhangyuqi
 * 2017年6月1日
 */
@Service
public class GuessingInfoService {

	private static final Logger LOGGER = LoggerFactory.getLogger(GuessingInfoService.class);

	private static final String GUESSING_ITEM_ID = "itemId";// 查询结果竞猜对象ID key
	private static final String GUESSING_ITEM_NAME = "itemName";// 查询结果竞猜对象名称key
	private static final String GUESSING_ITEM_LOGO_URL = "logoUrl";// 查询结果竞猜对象logo url key
	private static final String GUESSING_ITEM_POSITION = "position";// 查询结果竞猜对象位置key
	private static final String GUESSING_ITEM_IS_WINNER = "isWinner";// 查询结果竞猜对象输赢标识key
	private static final String GUESSING_ITEM_SCORE = "score";// 查询结果竞猜对象得分key

	// 竞猜列表对应导出文件标题栏
	private static final String[] EXPORT_FILE_COLUMN_NAME = { "标题", "竞猜对象1", "竞猜对象2", "猜中人数", "参与人数", "押中金币数量",
			"金币押注总数量", "竞猜状态" };

	@Resource
	private QueryDao queryDao;
	@Resource
	private GuessingInfoDao guessingInfoDao;
	@Resource
	private GuessingInfoItemDao guessingInfoItemDao;
	@Resource
	private CoinHistoryDao coinHistoryDao;
	@Resource
	private UserInfoDao userInfoDao;
	@Resource
	protected StringRedisOperateService redisOperateService;

	/**
	 * 查询所有竞猜列表
	 * @param page		当前页数
	 * @param pageSize	单页显示数量
	 * @param keyTitle	竞猜标题关键字(搜索条件)
	 * @param keyStatus	竞猜状态关键字(搜索条件)
	 */
	public PageVO findGuessingList(int page, Integer pageSize, String keyTitle, Integer keyStatus) {
		List<Map<String, Object>> guessingInfoList = null;
		Number total;

		try {
			String condition = StringUtils.EMPTY;
			String limit = PageUtils.getLimitSql(page, pageSize);

			if (StringUtils.isNotBlank(keyTitle)) {
				condition = SqlJoiner.joinWithoutSpace(condition, " AND title LIKE '%", keyTitle, "%' ");
			}

			if (keyStatus != null) {
				condition = SqlJoiner.joinWithoutSpace(condition, " AND `status` = ", keyStatus, " ");
			}

			String totalSql = "SELECT count(id) count FROM guessing_info WHERE is_valid = 1" + condition;
			total = queryDao.query(totalSql);

			if (total != null && total.intValue() != 0) {
				guessingInfoList = getGuessingInfoList(condition, limit);
			}
		} catch (Exception e) {
			LOGGER.error("获取竞猜列表失败：{}", e);
			throw e;
		}
		return new PageVO(page, guessingInfoList, total, pageSize);
	}

	/**
	 * 新增或更新竞猜信息
	 * @param guessingInfo	竞猜信息
	 */
	public void saveOrUpdate(GuessingInfo guessingInfo) {
		try {
			guessingInfoDao.save(guessingInfo);
		} catch (Exception e) {
			if (guessingInfo.getId() != null) {
				LOGGER.error("更新Id {} 的竞猜信息失败：{}", guessingInfo.getId(), e);
			} else {
				LOGGER.error("新增标题为 {} 的竞猜信息失败：{}", guessingInfo.getTitle(), e);
			}
			throw e;
		}
	}

	/**
	 * 保存竞猜和竞猜对象关系信息
	 */
	@Transactional
	public void saveOrUpdateWithItem(GuessingInfo guessingInfo, GuessingInfoItem leftInfoItem,
			GuessingInfoItem rightInfoItem) {
		try {
			Long guessingInfoId = guessingInfoDao.save(guessingInfo).getId();
			leftInfoItem.setGuessingInfoId(guessingInfoId);
			rightInfoItem.setGuessingInfoId(guessingInfoId);
			guessingInfoItemDao.save(leftInfoItem);
			guessingInfoItemDao.save(rightInfoItem);
		} catch (Exception e) {
			if (guessingInfo.getId() != null) {
				LOGGER.error("更新Id {} 的竞猜信息失败：{}", guessingInfo.getId(), e);
			} else {
				LOGGER.error("新增标题为 {} 的竞猜信息失败：{}", guessingInfo.getTitle(), e);
			}
			throw e;
		}
	}

	/**
	 * 比赛结束，保存录入结果
	 */
	@Transactional
	public void saveResult(Long userId, GuessingInfo guessingInfo, GuessingInfoItem leftInfoItem,
			GuessingInfoItem rightInfoItem) {
		try {
			guessingInfoItemDao.save(leftInfoItem);
			guessingInfoItemDao.save(rightInfoItem);

			Long winnerItemId = leftInfoItem.getIsWinner().equals(CommonConstant.INT_BOOLEAN_TRUE)
					? leftInfoItem.getGuessingItemId()
					: rightInfoItem.getGuessingItemId();
			Long loserItemId = leftInfoItem.getIsWinner().equals(CommonConstant.INT_BOOLEAN_FALSE)
					? leftInfoItem.getGuessingItemId()
					: rightInfoItem.getGuessingItemId();

			// 录入结果操作,比赛结束录入竞猜结果时，计算比赛双方的押注用户金币收支情况
			guessingInfo.setStatus(GuessingConstant.GUESSING_STATUS_END);
			guessingInfo.setUpdateDate(new Date());
			guessingInfo.setUpdateUserId(userId);
			this.countRevenueOfGuessingUser(userId, guessingInfo, winnerItemId, loserItemId);
			guessingInfoDao.save(guessingInfo);
		} catch (Exception e) {
			LOGGER.error("保存竞猜ID为 {} 的比赛结果信息失败：{}", leftInfoItem.getGuessingInfoId(), e);
			throw e;
		}
	}

	/**
	 * 根据ID获取单个竞猜信息
	 */
	public GuessingInfo findSingleGuessingInfo(Long guessingId) {
		try {
			return guessingInfoDao.findByIdAndValid(guessingId, CommonConstant.INT_BOOLEAN_TRUE);
		} catch (Exception e) {
			LOGGER.error("根据Id {} 获取单个竞猜信息失败：{}", guessingId, e);
			throw e;
		}
	}

	/**
	 * 根据ID获取单个比赛结束的竞猜信息所有内容
	 * 包括：竞猜对象，竞猜结果等信息
	 */
	public Map<String, Object> findSingleGuessing(Long guessingId) {
		Map<String, Object> guessingInfo;
		try {
			String sql = "SELECT o.id guessingId, t1.itemId itemId, t1.itemName itemName, t1.logoUrl logoUrl,"
					+ " t1.score score, t1.isWinner isWinner, o.title title, o.end_date endDate, o.release_date releaseDate"
					+ " FROM guessing_info o          "
					+ " LEFT JOIN guessing_info_item om ON om.guessing_info_id = o.id"
					+ " LEFT JOIN                         "
					+ "		(SELECT om1.guessing_info_id guessingInfoId, group_concat(m.id) itemId,"
					+ " 	group_concat(m.`name`) itemName, group_concat(m.logo_url) logoUrl,"
					+ "     group_concat(om1.score) score, group_concat(om1.is_winner) isWinner"
					+ " 	FROM guessing_info_item om1                "
					+ " 	LEFT JOIN guessing_item m ON m.id = om1.guessing_item_id"
					+ "		GROUP BY om1.guessing_info_id ) t1 ON t1.guessingInfoId = o.id               "
					+ " WHERE o.id = " + guessingId + " AND o.is_valid = 1 AND om.is_valid = 1 GROUP BY o.id";
			guessingInfo = queryDao.querySingleMap(sql);

			if (!CollectionUtils.isEmpty(guessingInfo)) {
				Map<String, Object> leftItem = new LinkedHashMap<>();
				Map<String, Object> rightItem = new LinkedHashMap<>();
				for (Map.Entry<String, Object> entry : guessingInfo.entrySet()) {
					switch (entry.getKey()) {
					case GUESSING_ITEM_ID:
						convertObjectToItem(leftItem, rightItem, (String) entry.getValue(), GUESSING_ITEM_ID);
						break;
					case GUESSING_ITEM_NAME:
						convertObjectToItem(leftItem, rightItem, (String) entry.getValue(), GUESSING_ITEM_NAME);
						break;
					case GUESSING_ITEM_LOGO_URL:
						convertObjectToItem(leftItem, rightItem, (String) entry.getValue(), GUESSING_ITEM_LOGO_URL);
						break;
					case GUESSING_ITEM_IS_WINNER:
						convertObjectToItem(leftItem, rightItem, (String) entry.getValue(), GUESSING_ITEM_IS_WINNER);
						break;
					case GUESSING_ITEM_SCORE:
						convertObjectToItem(leftItem, rightItem, (String) entry.getValue(), GUESSING_ITEM_SCORE);
						break;
					}
				}
				// 删除冗余信息
				guessingInfo.remove(GUESSING_ITEM_ID);
				guessingInfo.remove(GUESSING_ITEM_NAME);
				guessingInfo.remove(GUESSING_ITEM_LOGO_URL);
				guessingInfo.remove(GUESSING_ITEM_IS_WINNER);
				guessingInfo.remove(GUESSING_ITEM_SCORE);
				// 添加(单个)竞猜对象信息
				guessingInfo.put("leftItem", leftItem);
				guessingInfo.put("rightItem", rightItem);
			}
		} catch (Exception e) {
			LOGGER.error("根据Id {} 获取单个竞猜信息失败：{}", guessingId, e);
			throw e;
		}
		return guessingInfo;
	}

	/**
	 * 设置当前竞猜置顶
	 */
	public void setFirst(Long guessingId) {
		try {
			// 复位已置顶竞猜
			String sql = "UPDATE guessing_info SET is_top = null WHERE is_top = 1";
			queryDao.update(sql);

			// 设置新的竞猜信息置顶
			String newSql = "UPDATE guessing_info SET is_top = 1 WHERE id = " + guessingId;
			queryDao.update(newSql);
		} catch (Exception e) {
			LOGGER.error("根据Id {} 置顶竞猜信息失败：{}", guessingId, e);
			throw e;
		}
	}

	/**
	 * 取消当前置顶竞猜
	 */
	public void cancelFirst(Long guessingId) {
		try {
			// 取消当前置顶
			String newSql = "UPDATE guessing_info SET is_top = null WHERE id = " + guessingId;
			queryDao.update(newSql);
		} catch (Exception e) {
			LOGGER.error("根据Id {} 取消置顶竞猜失败：{}", guessingId, e);
			throw e;
		}
	}

	/**
	 * 删除单个竞猜信息
	 */
	public void delete(Long guessingId) {
		try {
			guessingInfoDao.delete(guessingId);
		} catch (Exception e) {
			LOGGER.error("根据Id {} 删除竞猜信息失败：{}", guessingId, e);
			throw e;
		}
	}

	/**
	 * 导出竞猜列表信息
	 */
	public void export(HttpServletResponse res) throws Exception {
		try {
			List<Map<String, Object>> guessingList = this.getGuessingInfoList(null, null);
			String[][] contents = new String[guessingList.size() + 1][];
			// 设置标题行
			contents[0] = EXPORT_FILE_COLUMN_NAME;

			// 设置内容
			if (!CollectionUtils.isEmpty(guessingList)) {
				for (int i = 0; i < guessingList.size(); i++) {
					Map<String, Object> obj = guessingList.get(i);
					String[] row = new String[EXPORT_FILE_COLUMN_NAME.length];
					row[0] = MapUtils.getString(obj, "title");
					row[1] = MapUtils.getString(obj, "leftItem");
					row[2] = MapUtils.getString(obj, "rightItem");
					row[3] = MapUtils.getString(obj, "winnerCount");
					row[4] = MapUtils.getString(obj, "totalUserCount");
					row[5] = MapUtils.getString(obj, "winnerCoinCount");
					row[6] = MapUtils.getString(obj, "totalStakeCoin");
					row[7] = MapUtils.getString(obj, "guessingStatus");
					contents[i + 1] = row;
				}
			}

			ExcelUtils.exportExcel("竞猜列表", contents, false, res);
		} catch (Exception e) {
			LOGGER.error("导出竞猜列表信息失败：{}", e);
			throw e;
		}
	}

	/**
	 * 根据竞猜发布日期，竞猜截止日期更新竞猜状态
	 */
	public void updateGuessingStatus() {
		String now = DateUtils.dateToString(new Date(), null);

		try {
			String sql = "SELECT * FROM guessing_info o WHERE o.is_valid = 1 AND ((o.release_date <= '" + now
					+ "' AND o.`status` = 0) OR ( o.`status` = 1 AND o.end_date <= '" + now + "'))";
			List<GuessingInfo> guessingInfoList = queryDao.queryObject(sql, GuessingInfo.class);

			if (!CollectionUtils.isEmpty(guessingInfoList)) {
				for (GuessingInfo guessingInfo : guessingInfoList) {
					if (GuessingConstant.GUESSING_STATUS_NOT_START.equals(guessingInfo.getStatus())) {
						guessingInfo.setStatus(GuessingConstant.GUESSING_STATUS_UNDER_WAY);
					} else if (GuessingConstant.GUESSING_STATUS_UNDER_WAY.equals(guessingInfo.getStatus())) {
						guessingInfo.setStatus(GuessingConstant.GUESSING_STATUS_WAITING);
						guessingInfo.setTop(null);// 取消置顶
					}
				}
				guessingInfoDao.save(guessingInfoList);
			}
		} catch (Exception e) {
			LOGGER.error("定时任务修改竞猜状态失败：{}", e);
		}
	}

	/**
	 * 根据竞猜ID和竞猜对象位置获取单个竞猜与竞猜对象关系信息
	 */
	public GuessingInfoItem findSingleGuessingInfoItem(Long guessingId, Integer position) {
		try {
			return guessingInfoItemDao.findByGuessingInfoIdAndPositionAndValid(guessingId, position,
					CommonConstant.INT_BOOLEAN_TRUE);
		} catch (Exception e) {
			LOGGER.error("根据竞猜Id {} 和竞猜对象位置 {} 获取单个竞猜与竞猜对象关系信息失败：{}", guessingId, position, e);
			throw e;
		}
	}

	/**
	 * 根据查询条件和 limit 查询竞猜列表结果集
	 */
	private List<Map<String, Object>> getGuessingInfoList(String condition, String limit) {

		condition = condition == null ? StringUtils.EMPTY : condition;
		limit = limit == null ? StringUtils.EMPTY : limit;

		String sql = "SELECT t1.itemName itemName, t1.position position, o.title title, o.`status` guessingStatus,"
				+ " o.is_top AS top, o.id guessingId, o.end_date endDate, o.release_date releaseDate "
				+ " FROM guessing_info o                               "
				+ " LEFT JOIN                                          "
				+ "		(SELECT om1.guessing_info_id guessingInfoId, group_concat(m.`name`) itemName,"
				+ "		group_concat(om1.position) position         "
				+ " 	FROM guessing_info_item om1                 "
				+ "		LEFT JOIN guessing_item m ON m.id = om1.guessing_item_id"
				+ " 	GROUP BY om1.guessing_info_id ) t1 ON t1.guessingInfoId = o.id            "
				+ " WHERE o.is_valid = 1 " + condition + " GROUP BY o.id ORDER BY o.is_top DESC, o.`status`, o.id DESC"
				+ limit;

		List<Map<String, Object>> guessingInfoList = queryDao.queryMap(sql);

		if (!CollectionUtils.isEmpty(guessingInfoList)) {
			for (Map<String, Object> map : guessingInfoList) {
				if (map.get(GUESSING_ITEM_POSITION) == null || map.get(GUESSING_ITEM_NAME) == null) {
					continue;
				}

				// 添加当竞猜的总参与人数和总押注金币数
				Map<String, Object> totalUserAndStakeCoinMap = this
						.getTotalUserAndStakeCoin(map.get("guessingId").toString());
				if (!CollectionUtils.isEmpty(totalUserAndStakeCoinMap)) {
					map.putAll(totalUserAndStakeCoinMap);
				}

				if (GuessingConstant.GUESSING_STATUS_END
						.equals(Integer.valueOf(map.get("guessingStatus").toString()))) {
					Map<String, Object> winnerMap = this.getWinnerUserAndStakeCoin(map.get("guessingId").toString());
					if (!CollectionUtils.isEmpty(winnerMap)) {
						map.putAll(winnerMap);
					}
					map.putIfAbsent("winnerCount", 0);
					map.putIfAbsent("winnerCoinCount", 0);
					map.putIfAbsent("totalStakeCoin", 0);
				}

				// 拆分竞猜对象名称，position=0的为左边对象，position=1的为右边对象
				String[] positions = ((String) map.get(GUESSING_ITEM_POSITION)).split(",");
				String[] itemNames = ((String) map.get(GUESSING_ITEM_NAME)).split(",");
				if (Integer.valueOf(positions[0]) == 0) {
					map.put("leftItem", itemNames[0]);
					map.put("rightItem", itemNames[1]);
				} else {
					map.put("leftItem", itemNames[1]);
					map.put("rightItem", itemNames[0]);
				}
				map.remove(GUESSING_ITEM_NAME);
				map.remove(GUESSING_ITEM_POSITION);
			}
		}
		return guessingInfoList;
	}

	/**
	 * 根据竞猜ID获取当前竞猜的参人数和押注总金币数
	 */
	private Map<String, Object> getTotalUserAndStakeCoin(String guessingId) {
		if (StringUtils.isBlank(guessingId)) {
			return null;
		}
		String sql = "SELECT SUM(IF(r.is_adding_coin = 0, 1, 0)) totalUserCount, SUM(r.coin_count) totalStakeCoin"
				+ " FROM guessing_record r WHERE r.guessing_info_id = " + guessingId;
		return queryDao.querySingleMap(sql);
	}

	/**
	 * 根据竞猜ID获取当前竞猜获胜总人数和押中总金币数
	 */
	private Map<String, Object> getWinnerUserAndStakeCoin(String guessingId) {
		if (StringUtils.isBlank(guessingId)) {
			return null;
		}
		String sql = "SELECT SUM(IF(r.is_adding_coin = 0, 1, 0)) winnerCount, SUM(r.coin_count) winnerCoinCount"
				+ " FROM guessing_record AS r"
				+ " LEFT JOIN guessing_info_item AS om ON r.guessing_info_id = om.guessing_info_id"
				+ " AND r.guessing_item_id = om.guessing_item_id  "
				+ " WHERE om.is_winner = 1 AND r.guessing_info_id = " + guessingId;
		return queryDao.querySingleMap(sql);
	}

	/**
	 * 解析竞猜对象双方信息并封装到对应单个竞猜对象
	 * @param leftItem		左边竞猜对象
	 * @param rightItem		右边竞猜对象
	 */
	private void convertObjectToItem(Map<String, Object> leftItem, Map<String, Object> rightItem, String value,
			String key) {
		if (StringUtils.isNotBlank(value)) {
			String[] values = value.split(",");
			leftItem.put(key, values[0]);
			rightItem.put(key, values[1]);
		}
	}

	/**
	 * 比赛结束，计算比赛双方的押注用户金币收支情况
	 * @param adminId					当前操作员
	 * @param guessingInfo				竞猜信息
	 * @param winnerItemId				获胜竞猜对象ID
	 */
	private void countRevenueOfGuessingUser(Long adminId, GuessingInfo guessingInfo, Long winnerItemId,
			Long loserItemId) {
		// 查询当前竞猜胜负双方押注的总金币数
		String guessingInfoSql = "SELECT SUM(IF(om.guessing_item_id = " + winnerItemId
				+ ", r.coin_count, 0)) winnerCoinCount, SUM(IF(om.guessing_item_id = " + loserItemId
				+ ", r.coin_count, 0)) loserCoinCount               "
				+ " FROM guessing_info o                            "
				+ " LEFT JOIN guessing_record r ON o.id = r.guessing_info_id"
				+ " LEFT JOIN guessing_info_item om ON r.guessing_info_id = om.guessing_info_id"
				+ " AND r.guessing_item_id = om.guessing_item_id                                      "
				+ " WHERE r.is_valid = 1 AND o.id = " + guessingInfo.getId() + " GROUP BY o.id";
		Map<String, Object> guessingInfoMap = queryDao.querySingleMap(guessingInfoSql);

		int totalWinnerCoinCount = 0;
		int totalLoserCoinCount = 0;
		if (!CollectionUtils.isEmpty(guessingInfoMap)) {
			totalWinnerCoinCount = NumberUtils.toInt(guessingInfoMap.get("winnerCoinCount").toString());// 胜方押注总和
			totalLoserCoinCount = NumberUtils.toInt(guessingInfoMap.get("loserCoinCount").toString());// 负方押注总和
		}
		totalWinnerCoinCount = totalWinnerCoinCount < 0 ? 0 : totalWinnerCoinCount;
		totalLoserCoinCount = totalLoserCoinCount < 0 ? 0 : totalLoserCoinCount;

		// 查询所有押注当前竞猜的用户
		String recordSql = "SELECT r.guessing_info_id guessingInfoId, r.user_id userId,"
				+ " SUM(r.coin_count) coinCount, om.guessing_item_id itemId           "
				+ " FROM guessing_record r                  "
				+ " LEFT JOIN guessing_info o ON o.id = r.guessing_info_id"
				+ " LEFT JOIN guessing_info_item om ON r.guessing_info_id = om.guessing_info_id"
				+ " AND r.guessing_item_id = om.guessing_item_id"
				+ " WHERE o.is_valid = 1 AND r.is_valid = 1 AND om.is_valid = 1 AND o.id = " + guessingInfo.getId()
				+ " GROUP BY r.user_id ";
		List<Map<String, Object>> guessingRecordList = queryDao.queryMap(recordSql);

		// 获取上一次竞猜结束时平台的余量额
		RedisAtomicInteger redisAtomicInteger = new RedisAtomicInteger(CacheKeyConstant.GUESSING_SYSTEM_COIN_COUNT,
				redisOperateService.getRedisTemplate().getConnectionFactory());
		int currentProfit = 0;// 当前余量额 = 本场正余量-本场负余量+上次平台余量额
		int ownerCoin;// 用户押注金币
		int winnerCoin;// 用户获胜金币 = 大于1金币的向下取整，大于0小于1金币的，向上取整
		int winnerTotalCoin = 0;// 用户获胜金币总额，sum(winnerCoin)
		int coinHistory;// 收支记录金币量，胜方=押注本金+盈利
		int positiveRemainder = 0;// 本场正余量 = 负方押注总金额-用户获胜金币总额
		int negativeRemainder = 0;// 本场负余量 = count(winnerCoin=0的用户)
		int totalExpenditure = 0;// 本场总支出 = sum(每一个胜方押注本金+盈利)
		long userId;// 押注用户ID
		UserInfo userInfo;// 押注用户信息

		if (totalWinnerCoinCount > 0) {
			List<CoinHistory> histories = new ArrayList<>();
			List<UserInfo> userInfoList = new ArrayList<>();
			for (Map<String, Object> record : guessingRecordList) {
				if (String.valueOf(record.get("itemId")).equals(winnerItemId.toString())) {
					// 当前用户总押注金币
					ownerCoin = record.get("coinCount") == null ? 0
							: NumberUtils.toInt(record.get("coinCount").toString());
					userId = record.get("userId") == null ? 0 : NumberUtils.toLong(record.get("userId").toString());

					if (ownerCoin == 0 || userId == 0) {
						continue;
					}

					winnerCoin = ownerCoin * totalLoserCoinCount / totalWinnerCoinCount;
					if (winnerCoin == 0) {// 失败方没有押注金额，故获胜方每人补贴1金币
						negativeRemainder++;
						winnerCoin++;
						coinHistory = ownerCoin + 1;
					} else {
						coinHistory = ownerCoin + winnerCoin;
					}

					winnerTotalCoin += winnerCoin;
					totalExpenditure += coinHistory;

					userInfo = userInfoDao.findByIdAndValid(userId, CommonConstant.INT_BOOLEAN_TRUE);
					if (userInfo != null) {
						int userCoin = userInfo.getCoin() == null ? 0 : userInfo.getCoin();
						userInfo.setCoin(userCoin + coinHistory);
						userInfoList.add(userInfo);
					} else {
						continue;
					}

					CoinHistory h = new CoinHistory();
					Date now = new Date();
					h.setUserId(userId);
					h.setType(CoinConstant.HISTORY_TYPE_GUESSING);
					h.setTargetId(NumberUtils.toLong(record.get("guessingInfoId").toString()));
					h.setValid(CommonConstant.INT_BOOLEAN_TRUE);
					h.setCreateUserId(adminId);
					h.setCreateDate(now);
					h.setDirection(CoinConstant.HISTORY_DIRECTION_INCOME);
					h.setCoin(coinHistory);
					histories.add(h);
				}
			}

			if (!CollectionUtils.isEmpty(histories)) {
				coinHistoryDao.save(histories);
			}

			if (!CollectionUtils.isEmpty(userInfoList)) {
				userInfoDao.save(userInfoList);
			}

			positiveRemainder = totalLoserCoinCount - winnerTotalCoin;
			positiveRemainder = positiveRemainder < 0 ? 0 : positiveRemainder;
			currentProfit = positiveRemainder - negativeRemainder;
		}
		guessingInfo.setNegativeRemainder(negativeRemainder);
		guessingInfo.setPositiveRemainder(positiveRemainder);
		guessingInfo.setCurrentRemainder(currentProfit + redisAtomicInteger.intValue());
		guessingInfo.setTotalIncome(totalWinnerCoinCount + totalLoserCoinCount);// 本场总收入=胜方押注金币总量+负方金币押注总量
		guessingInfo.setTotalExpenditure(totalExpenditure);
		redisAtomicInteger.addAndGet(currentProfit);
	}
}
