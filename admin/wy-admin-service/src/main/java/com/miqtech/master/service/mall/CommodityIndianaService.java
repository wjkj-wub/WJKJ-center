package com.miqtech.master.service.mall;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.consts.Msg4UserType;
import com.miqtech.master.consts.MsgConstant;
import com.miqtech.master.consts.RedbagConstant;
import com.miqtech.master.consts.SystemUserConstant;
import com.miqtech.master.consts.mall.CommodityConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.award.AwardRecordDao;
import com.miqtech.master.dao.mall.CommodityHistoryCdkeyDao;
import com.miqtech.master.dao.mall.CommodityInfoDao;
import com.miqtech.master.dao.user.UserInfoDao;
import com.miqtech.master.dao.user.UserRedbagDao;
import com.miqtech.master.entity.Pager;
import com.miqtech.master.entity.award.AwardRecord;
import com.miqtech.master.entity.mall.CoinHistory;
import com.miqtech.master.entity.mall.CommodityHistory;
import com.miqtech.master.entity.mall.CommodityHistoryCdkey;
import com.miqtech.master.entity.mall.CommodityInfo;
import com.miqtech.master.entity.user.UserInfo;
import com.miqtech.master.entity.user.UserRedbag;
import com.miqtech.master.service.system.SystemRedbagService;
import com.miqtech.master.thirdparty.service.JpushService;
import com.miqtech.master.thirdparty.util.SMSMessageUtil;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.utils.StringUtils;
import com.miqtech.master.vo.PageVO;

/**
 * （金币）众筹商品操作service
 */
@Component
public class CommodityIndianaService {
	@Autowired
	private CommodityInfoDao commodityDao;
	@Autowired
	private QueryDao queryDao;
	@Autowired
	private UserRedbagDao userRedbagDao;
	@Autowired
	private SystemRedbagService systemRedbagService;
	@Autowired
	CommodityHistoryCdkeyDao commodityHistoryCdkeyDao;
	@Autowired
	CommodityHistoryService commodityHistoryService;
	@Autowired
	private CommodityService commodityService;
	@Autowired
	private AwardRecordDao awardRecordDao;
	@Autowired
	private JpushService msgOperateService;
	@Autowired
	private CoinHistoryService coinHistoryService;
	@Autowired
	private UserInfoDao userInfoDao;

	public Map<String, Object> mallCommodityList() {
		Map<String, Object> result = new HashMap<String, Object>();
		//众筹夺宝
		String sql = "select c.id, ifnull(c.sort_no, 99999999) sortNo, c. name commodityName, i.icon mainIcon, c.area_id areaId, a.area_name areaName, c.category_id categoryId, t. name categoryName, c.price, cast((c.coins - d.coins) / price AS signed ) left_num, cast( d.coins / c.coins * 100 AS signed ) progress, c.crowdfund_status, if ( f.id is null, e.virtual_phone, f.username ) prize_phone from mall_t_commodity c left join mall_t_commodity_area a on a.id = 3 left join mall_t_commodity_category t on t.id = c.category_id left join mall_r_commodity_icon i on i.commodity_id = c.id and i.is_main = 1 left join ( select commodity_id, sum(coin) coins from mall_r_commodity_history where status = 0 and is_valid = 1 and commodity_source = 1 group by commodity_id ) d on d.commodity_id = c.id left join mall_r_commodity_history e on e.commodity_id = c.id and e.commodity_source = 1 and e.is_get = 1 and e.status=0 and e.is_valid=1 left join user_t_info f on e.user_id = f.id where c.is_valid = 1 and c. status = 1 and c.area_id = 3 order by sortNo asc limit 3";
		result.put("grobTreasure", queryDao.queryMap(sql));
		//兑奖专区
		sql = "select c.id, ifnull(c.sort_no, 99999999) sortNo, c. name commodityName, i.icon mainIcon, c.area_id areaId, a.area_name areaName, c.category_id categoryId, t. name categoryName, c.price, c.discount_price discountPrice from mall_t_commodity c left join mall_t_commodity_area a on a.id = 1 left join mall_t_commodity_category t on t.id = c.category_id left join mall_r_commodity_icon i on i.commodity_id = c.id and i.is_main = 1 where c.is_valid = 1 and c. status = 1 and c.area_id = 1 order by sortNo asc limit 4";
		result.put("prizeArea", queryDao.queryMap(sql));
		return result;
	}

	/**
	 * 根据id查商品
	 */
	public CommodityInfo getCommodityById(long commodityId) {
		return commodityDao.findOne(commodityId);
	}

	/**
	 * 查目前商品最大排序号
	 */
	public int getLargestSortNo() {
		String sqlQuery = "select sort_no from mall_t_commodity where sort_no is not null and area_id=3 order by sort_no desc limit 1";
		Number sortNo = queryDao.query(sqlQuery);
		if (null != sortNo) {
			return sortNo.intValue();
		}
		return 0;
	}

	/**
	 * 查目前商品最小排序号
	 */
	public int getLeastSortNo() {
		String sqlQuery = "select sort_no from mall_t_commodity where sort_no is not null and area_id=3 order by sort_no asc limit 1";
		Number sortNo = queryDao.query(sqlQuery);
		if (null != sortNo) {
			return sortNo.intValue();
		}
		return 0;
	}

	/**
	 * 判断某排序号是否已存在（有效商品里）
	 */
	public boolean existSortNoValid(int sortNo) {
		String sqlCount = "select count(1) from mall_t_commodity where is_valid and area_id=3 and sort_no=" + sortNo;
		Number count = queryDao.query(sqlCount);
		if (null != count && count.intValue() > 0) {
			return true;
		}
		return false;
	}

	/**
	 * 排序号介于(oriSorno,newSortNo]所有商品ID
	 */
	public List<Map<String, Object>> getFrontIds(int newSortNo, int oriSorno, long oriId) {
		String sqlQuery = "select id from mall_t_commodity where id <> " + oriId + "  and area_id=3  and sort_no <= "
				+ newSortNo + " and sort_no > " + oriSorno;
		return queryDao.queryMap(sqlQuery);
	}

	/**
	 * 排序号介于[newSortNo,oriSorno)所有商品ID
	 */
	public List<Map<String, Object>> getBehindIds(int newSortNo, int oriSorno, long oriId) {
		if (oriSorno == 0) {
			oriSorno = getLargestSortNo();
		}
		String sqlQuery = "select id from mall_t_commodity where id <> " + oriId + "  and area_id=3  and sort_no >= "
				+ newSortNo + " and sort_no < " + oriSorno;
		return queryDao.queryMap(sqlQuery);
	}

	/**
	 * 查出有重复sorNo的ID组(以","隔开)
	 */
	public List<Map<String, Object>> getIdsGoupBySortNo(Long id) {
		String sqlFilter = StringUtils.EMPTY;
		if (null != id) {
			sqlFilter = " and id <> " + id;
		}
		String sqlQuery = SqlJoiner.join("SELECT GROUP_CONCAT(id) ids, sort_no FROM mall_t_commodity  where area_id=3 ",
				sqlFilter, " GROUP BY sort_no HAVING count(id)>=2");
		return queryDao.queryMap(sqlQuery);
	}

	/**
	 * 根据ID修改排序号
	 */
	public void updateSortNoById(Long id, int sortNo) {
		String sqlUpdate = "UPDATE mall_t_commodity SET sort_no = " + sortNo + " WHERE  area_id=3 and id = " + id;
		queryDao.update(sqlUpdate);
	}

	/**
	* ##后台管理##:查询商品列表，分页
	*/
	public PageVO listIndianaPage(int page, int rows, Map<String, Object> params) {
		String sqlQuery = SqlJoiner.join(
				"select c.id,ifnull(c.sort_no,99999999) sortNo,c.shelve_record shelveRecord,c.crowdfund_status crowdfundStatus, c.item_no itemNo, c.name commodityName, c.status,c.auto_drawn autoDrawn, i.icon mainIcon, c.area_id areaId, ",
				" c.super_type superType, c.category_id categoryId, c.third_type thirdType, IFNULL(t.name,c.category_id) categoryName, c.price, c.coins, ",
				" c.create_date createDate,  c.introduce, c.rule, c.information_defualt informationDefualt , ifnull(d.personCount,0) personCount, ifnull(d1.tPersonCount,0) tPersonCount, ",
				"(ifnull(d.buyCount,0)*c.price)/(if(c.coins=0,1,c.coins))*100 percent  ",
				" from mall_t_commodity c left join mall_t_commodity_area a on a.id=c.area_id ",
				" left join mall_t_commodity_category t on t.id = c.category_id ",
				" left join mall_r_commodity_icon i on i.commodity_id = c.id ",
				" left join (select count(1) personCount ,sum(num) buyCount ,commodity_id from mall_r_commodity_history where  commodity_source=1 and is_valid=1 and create_date <now() group  by commodity_id)  d on d.commodity_id = c.id  ",
				" left join (select count(1) tPersonCount ,commodity_id from mall_r_commodity_history where  commodity_source = 1 and user_id is not null and  is_valid=1 and create_date <now() group  by commodity_id)  d1 on d1.commodity_id = c.id  ",
				"where (i.is_main = 1 or i.is_main is null) and c.is_valid = 1  and c.area_id=3  ");

		String sqlCount = SqlJoiner.join("select count(1) from mall_t_commodity c",
				" where c.is_valid=1 and c.area_id=3 ");
		if (null != params.get("status")) { //上下架状态
			sqlQuery = SqlJoiner.join(sqlQuery, " and c.status=:status");
			sqlCount = SqlJoiner.join(sqlCount, " and c.status=" + params.get("status"));
		}
		if (null != params.get("name")) { //根据商品名查询
			sqlQuery = SqlJoiner.join(sqlQuery, " and c.name like concat('%', :name, '%')");
			sqlCount = SqlJoiner.join(sqlCount, " and c.name like '%" + params.get("name") + "%'");
		}
		if (null != params.get("crowdfundStatus")) { //分类查询
			sqlQuery = SqlJoiner.join(sqlQuery, " and c.crowdfund_status=:crowdfundStatus");
			sqlCount = SqlJoiner.join(sqlCount,
					" and c.crowdfund_status like '%" + params.get("crowdfundStatus") + "%'");
		}
		if (page < 1) {
			page = 1;
		}
		params.put("page", (page - 1) * rows);
		params.put("rows", rows);
		sqlQuery = SqlJoiner.join(sqlQuery, " order by sortNo desc, c.create_date desc limit :page, :rows");
		PageVO pageVO = new PageVO();
		pageVO.setList(queryDao.queryMap(sqlQuery, params));
		Number total = queryDao.query(sqlCount);
		pageVO.setTotal(total.intValue());

		return pageVO;
	}

	/**
	 * 查商品历史表中某商品被购买的记录
	 */
	public PageVO listPersonPage(int page, int rows, Map<String, Object> params) {
		String sqlQuery = "select h.create_date createDate,b.username telephone,h.num from mall_r_commodity_history h left join user_t_info b on h.user_id=b.id where h.commodity_source=1 and h.is_valid=1 and h.user_id is not null and h.commodity_id=:commodityId and  h.create_date<now()";
		String sqlCount = SqlJoiner.join("select count(1) from mall_r_commodity_history c ",
				"where c.is_valid=1 and c.commodity_source=1 and c.commodity_id=", params.get("commodityId") + "",
				" and c.user_id is not null and c.create_date<now()");
		if (page < 1) {
			page = 1;
		}
		params.put("page", (page - 1) * rows);
		params.put("rows", rows);
		PageVO pageVO = new PageVO();
		sqlQuery = SqlJoiner.join(sqlQuery, " order by h.create_date desc limit :page, :rows");
		pageVO.setList(queryDao.queryMap(sqlQuery, params));
		Number total = queryDao.query(sqlCount);
		pageVO.setTotal(total.intValue());
		return pageVO;
	}

	/**
	 * 查询真实、虚拟数据
	 */
	public List<Map<String, Object>> commodityCdkeyList(Boolean flag, int commodityId) {
		String sqlQuery = "";
		if (flag) {
			sqlQuery = "select c.cdkey,b.username telephone ,c.id ,a.id historyId from mall_r_commodity_history a "
					+ " left join user_t_info b on a.user_id=b.id "
					+ " LEFT JOIN mall_commodity_history_cdkey c on a.id=c.history_id where a.is_valid=1 and a.user_id is not null and a.commodity_id="
					+ commodityId;
		} else {
			sqlQuery = "select c.cdkey,a.virtual_phone telephone,c.id ,a.id historyId from mall_r_commodity_history a "
					+ " LEFT JOIN mall_commodity_history_cdkey c on a.id=c.history_id where a.is_valid=1 and a.commodity_source=1 and a.virtual_phone is not null and a.commodity_id="
					+ commodityId;
		}
		return queryDao.queryMap(sqlQuery);
	}

	/**
	 * 查出所有商品（有效）
	 */
	public List<Map<String, Object>> commodityList() {
		String sqlQuery = "select c.id, c.name from mall_t_commodity c where c.is_valid=1";
		return queryDao.queryMap(sqlQuery);
	}

	/**
	 * 更新中奖纪录
	 */
	public void updateWinPrize(long id) {
		CommodityHistoryCdkey chcdky = findCdkey(id);
		chcdky.setIsSelected(1);
		saveCdkey(chcdky);
		CommodityHistory commoditHistory = commodityHistoryService.findById(chcdky.getHistoryId());
		commoditHistory.setIsGet(1);
		CommodityInfo commodityInfo = commodityService.getCommodityById(commoditHistory.getCommodityId());
		if (null != commoditHistory.getUserId() && commodityInfo.getSuperType() != 1) {//真实数据分配审核人员金币红包除外
			commoditHistory.setCreateUserId(getSysUserIdOfLeastOrder(1));
		}
		commodityHistoryService.save(commoditHistory);
		if (commoditHistory != null) {
			long commodityId = commoditHistory.getCommodityId();
			String sqlciupdate = "update mall_t_commodity set crowdfund_status=2 ,lottery_time =now() where id="
					+ commodityId;
			queryDao.update(sqlciupdate);

		}
	}

	/*
	 * 查出订单量最少的商城（1审核2发放）用户id
	 */
	public long getSysUserIdOfLeastOrder(int type) {
		long mallUserId = 0;
		String whichId = "h.create_user_id"; //审核人员id
		String userType = SystemUserConstant.TYPE_MALL_VERIFY.toString();
		if (type == 2) {
			whichId = "h.update_user_id"; //发放人员id
			userType = SystemUserConstant.TYPE_MALL_GRANT.toString();
		}
		String sql = SqlJoiner.join("SELECT COUNT(", whichId, ") count, u.id FROM sys_t_user u",
				" LEFT JOIN mall_r_commodity_history h ON u.id = ", whichId, " AND h.is_valid = 1",
				" WHERE u.is_valid = 1 AND h.commodity_source=1 u.user_type = ", userType, " GROUP BY ", whichId,
				" ORDER BY count LIMIT 1");
		Map<String, Object> map = queryDao.querySingleMap(sql);
		if (MapUtils.isNotEmpty(map) && null != map.get("id")) {
			mallUserId = NumberUtils.toLong(map.get("id").toString());
		}
		return mallUserId;
	}

	/**
	 *获取要补全的次数
	 */
	public long querySurplus(long id) {

		//使超时的虚拟数据失效
		//		String deleteVirData = "update  mall_r_commodity_history set is_valid=0 where virtual_phone is not null and create_date>now() and commodity_id="
		//				+ id;
		//		queryDao.update(deleteVirData);
		String sqlCount = "select (b.coins/b.price-IFNULL(a.num,0)) count from mall_t_commodity b LEFT JOIN(select sum(num) num ,commodity_id from  mall_r_commodity_history where  is_valid=1 and commodity_id="
				+ id + ") a on b.id=a.commodity_id " + " where  b.id=" + id;
		Map<String, Object> countMap = queryDao.querySingleMap(sqlCount);
		long count = Math.round(((Number) countMap.get("count")).doubleValue());
		return count;
	}

	/**
	 *获取最近购买的时间
	 */
	public Date getLastDate(long id) {
		String sqlDate = "select max(create_date) createDate from  mall_r_commodity_history where  user_id is not null and is_valid=1 and commodity_id="
				+ id;
		Map<String, Object> countMap = queryDao.querySingleMap(sqlDate);
		Date lastDate = (Date) countMap.get("createDate");
		return lastDate;
	}

	/**
	 *保存cdkey
	 */
	public void saveCdkey(CommodityHistoryCdkey cdkey) {
		commodityHistoryCdkeyDao.save(cdkey);
	}

	/**
	 *保存cdkey
	 */
	public CommodityHistoryCdkey findCdkey(long cdkeyId) {
		return commodityHistoryCdkeyDao.findOne(cdkeyId);
	}

	/**
	 *更新购买记录
	 */
	public void updateShelveRecordByCommodityId(long commodityId) {
		String sqlShelveRecord = "update mall_t_commodity set shelve_record=1 where id=" + commodityId;
		queryDao.update(sqlShelveRecord);
	}

	/**
	 *查询购买记录
	 */
	public int countBuyRecordsByCommodityId(long commodityId) {
		String buyRecordSql = "select count(*) count from mall_r_commodity_history where  is_valid=1 and commodity_id="
				+ commodityId;
		Map<String, Object> countMap = queryDao.querySingleMap(buyRecordSql);
		int count = ((Number) countMap.get("count")).intValue();
		return count;
	}

	/**众筹夺宝商品详情
	 * @param id
	 * @param userId
	 * @param pager
	 * @return
	 */
	public Map<String, Object> commodityDetail(Long id, Long userId, Pager pager) {
		Map<String, Object> result = new HashMap<String, Object>();
		if (userId == null) {
			userId = 0L;
		}
		if (pager.page == 1) {
			result.put("commodity_info",
					queryDao.querySingleMap(
							"select a.id, b.icon mainIcon, a. name commodityName, cast( c.coins / a.coins * 100 AS signed ) progress, a.coins, cast((a.coins - c.coins) / price AS signed ) left_num, d.buy_num, a.introduce, a.rule, a.crowdfund_status, if ( f.id is null, e.virtual_phone, f.username ) prize_phone, g.cdkey,group_concat(b.icon order by b.icon) as imgs from mall_t_commodity a left join mall_r_commodity_icon b on b.commodity_id = a.id and b.is_valid=1 left join ( select commodity_id, sum(coin) coins from mall_r_commodity_history where status = 0 and is_valid = 1 and commodity_source = 1 group by commodity_id ) c on c.commodity_id = a.id left join ( select count(1) buy_num, commodity_id from mall_r_commodity_history where status = 0 and is_valid = 1 and commodity_source = 1 and user_id ="
									+ userId
									+ " group by user_id ) d on d.commodity_id = a.id left join mall_r_commodity_history e on e.commodity_id = a.id and e.commodity_source = 1 and e.is_get = 1 and e. status = 0 and e.is_valid = 1 left join mall_commodity_history_cdkey g on g.history_id=e.id and g.is_valid=1 and g.is_selected=1 left join user_t_info f on e.user_id = f.id where a.id ="
									+ id + " group by a.id"));
			result.put("buy_record",
					queryDao.queryMap(
							"select if ( b.id is null, a.virtual_phone, b.username ) prize_phone, sum(a.coin) pay_coin, a.create_date from mall_r_commodity_history a left join user_t_info b on a.user_id = b.id where a.commodity_id = 154 and a.commodity_source = 1 group by a.tran_no order by a.create_date desc limit 0,"
									+ pager.pageSize));
		} else {
			result.put("buy_record",
					queryDao.queryMap(
							"select if ( b.id is null, a.virtual_phone, b.username ) prize_phone, sum(a.coin) pay_coin, a.create_date from mall_r_commodity_history a left join user_t_info b on a.user_id = b.id where a.commodity_id = 154 and a.commodity_source = 1 group by a.tran_no order by a.create_date desc limit "
									+ pager.start + "," + pager.pageSize));
		}
		return result;
	}

	/**
	 * 红包发放
	 */
	public int redbagGrant(CommodityHistory commodityHistory, int quota) {
		UserRedbag userRedbag = new UserRedbag();
		userRedbag.setRedbagId(systemRedbagService.querySystemRedbagId(RedbagConstant.REDBAG_TYPE_MALL)); //系统红包ID
		userRedbag.setUserId(commodityHistory.getUserId());
		userRedbag.setNetbarType(1);
		userRedbag.setNetbarId((long) 0);
		userRedbag.setAmount(quota);
		userRedbag.setUsable(1);
		userRedbag.setValid(CommonConstant.INT_BOOLEAN_TRUE);
		userRedbag.setCreateDate(new Date());
		userRedbag.setLimitMinMoney(0);
		userRedbagDao.save(userRedbag);
		commodityHistory.setThirdpartyId(userRedbag.getId());
		newAwardRecord(commodityHistory, 1, 1, userRedbag.getId().toString(), 3);
		changeStatusAndRemark(commodityHistory, "0", 2);
		return 0;
	}

	/**
	 * 金币发放
	 */
	public int coinGrant(CommodityHistory commodityHistory, int quota) {
		UserInfo user = userInfoDao.findOne(commodityHistory.getUserId());
		user.setCoin(user.getCoin() + quota);
		userInfoDao.save(user);
		CoinHistory coinHistory = new CoinHistory();
		coinHistory.setUserId(user.getId());
		coinHistory.setType(6);//众筹夺宝

		coinHistory.setCoin(quota);
		coinHistory.setDirection(1);
		coinHistory.setValid(CommonConstant.INT_BOOLEAN_TRUE);
		coinHistory.setCreateDate(new Date());
		coinHistoryService.save(coinHistory);
		long record = newAwardRecord(commodityHistory, 1, 2, coinHistory.getId().toString(), 3);
		coinHistory.setTargetId(record);
		coinHistoryService.save(coinHistory);
		return changeStatusAndRemark(commodityHistory, "0", 3);
	}

	/**
	 * 其他发放
	 */
	public int otherGrant(CommodityHistory commodityHistory) {
		return changeStatusAndRemark(commodityHistory, "0", 6);
	}

	/*
	 * 记录财务信息
	 */
	private long newAwardRecord(CommodityHistory commodityHistory, int type, int subType, String targetId, int status) {
		CommodityInfo commodityInfo = commodityService.getCommodityById(commodityHistory.getCommodityId());
		AwardRecord awardRecord = new AwardRecord();
		awardRecord.setUserId(commodityHistory.getUserId());
		awardRecord.setType(type);
		awardRecord.setSubType(subType);
		if (StringUtils.isNotBlank(targetId)) {
			awardRecord.setTargetId(NumberUtils.toLong(targetId));
		}
		awardRecord.setAmount(NumberUtils.toDouble(commodityInfo.getQuota().toString()));
		awardRecord.setStatus(status);
		awardRecord.setChecked(0);
		awardRecord.setSourceType(2);
		awardRecord.setSourceTargetId(commodityHistory.getCommodityId());
		awardRecord.setValid(CommodityConstant.INT_STATUS_TRUE);
		awardRecord.setCreateDate(new Date());

		awardRecordDao.save(awardRecord);
		return awardRecord.getId();
	}

	/*
	 * 处理订单状态和备注
	 */
	private int changeStatusAndRemark(CommodityHistory commodityHistory, String result, int msgType) {
		Integer oldStatus = commodityHistory.getStatus();
		if (result.equals("0")) { //发放成功
			if (CommodityConstant.VERIFY_STATUS_PASS.equals(oldStatus)
					|| CommodityConstant.VERIFY_STATUS_FAIL.equals(oldStatus)) {
				commodityHistory.setStatus(CommodityConstant.VERIFY_STATUS_FINISH);
			} else if (CommodityConstant.VERIFY_STATUS_APPEALPASS.equals(oldStatus)
					|| CommodityConstant.VERIFY_STATUS_APPEALFAIL.equals(oldStatus)) {
				commodityHistory.setStatus(CommodityConstant.VERIFY_STATUS_APPEALFINISH);
			}
			if (msgType > 0) {
				pushMsg(commodityHistory, msgType);
				sendMsg(commodityHistory, msgType);
			}
			commodityHistory.setIsGet(1);
		} else { //发放失败
			if (CommodityConstant.VERIFY_STATUS_PASS.equals(oldStatus)) {
				commodityHistory.setStatus(CommodityConstant.VERIFY_STATUS_FAIL);
			} else if (CommodityConstant.VERIFY_STATUS_APPEALPASS.equals(oldStatus)) {
				commodityHistory.setStatus(CommodityConstant.VERIFY_STATUS_APPEALFAIL);
			}
			commodityHistory.setRemark(commodityHistory.getRemark() + ",B" + result);
			return -1;
		}

		commodityHistory.setUpdateDate(new Date());
		commodityHistoryService.save(commodityHistory);
		return 0;
	}

	/*
	 * 发送短信
	 */
	private void sendMsg(CommodityHistory commodityHistory, int msgType) {
		UserInfo userInfo = userInfoDao.findOne(commodityHistory.getUserId());
		CommodityInfo commodityInfo = commodityService.getCommodityById(commodityHistory.getCommodityId());
		String[] phoneNum = { userInfo.getTelephone() };
		String[] params = { commodityInfo.getName() };
		SMSMessageUtil.sendTemplateMessage(phoneNum, "6494", params);

	}

	/*
	 * 推送消息
	 */
	private void pushMsg(CommodityHistory commodityHistory, int msgType) {
		CommodityInfo commodityInfo = commodityService.getCommodityById(commodityHistory.getCommodityId());
		String content = StringUtils.EMPTY;
		if (msgType == 2) { //数据库红包
			content = "【网娱大师】尊敬的用户，恭喜您在众筹夺宝中购买的" + commodityInfo.getName() + " 被选为幸运用户了,{ " + commodityInfo.getQuota()
					+ "元网娱红包}已发放成功，请登录APP查询确认；若有异议，可在APP内客服反馈或拨打热线4006902530。";
		} else if (msgType == 3) { //数据库金币
			content = "【网娱大师】尊敬的用户，恭喜您在众筹夺宝中购买的" + commodityInfo.getName() + " 被选为幸运用户了,{" + commodityInfo.getQuota()
					+ "网娱金币}已发放成功，请登录APP查询确认；若有异议，可在APP内客服反馈或拨打热线4006902530。";
		} else {
			content = "【网娱大师】尊敬的用户，恭喜您在众筹夺宝中购买的" + commodityInfo.getName()
					+ " 被选为幸运用户了,请登录APP查询确认；若有异议，可在APP内客服反馈或拨打热线4006902530。";
		}
		msgOperateService.notifyMemberAliasMsg(Msg4UserType.SYS_NOTIFY.ordinal(), commodityHistory.getUserId(),
				MsgConstant.PUSH_MSG_TYPE_COMMODITY, "众筹商品发放提示信息", content, true, commodityHistory.getId());

	}
}
