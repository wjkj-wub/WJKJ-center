package com.miqtech.master.service.bounty;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.support.atomic.RedisAtomicInteger;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.consts.mall.CommodityConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.award.AwardRecordDao;
import com.miqtech.master.dao.bounty.BountyDao;
import com.miqtech.master.dao.bounty.BountyGradeDao;
import com.miqtech.master.dao.user.UserInfoDao;
import com.miqtech.master.entity.award.AwardRecord;
import com.miqtech.master.entity.bounty.Bounty;
import com.miqtech.master.entity.bounty.BountyGrade;
import com.miqtech.master.entity.bounty.BountyPrize;
import com.miqtech.master.entity.mall.CoinHistory;
import com.miqtech.master.entity.mall.CommodityHistory;
import com.miqtech.master.entity.user.UserInfo;
import com.miqtech.master.service.common.ObjectRedisOperateService;
import com.miqtech.master.service.mall.CoinHistoryService;
import com.miqtech.master.service.mall.CommodityHistoryService;
import com.miqtech.master.utils.BeanUtils;
import com.miqtech.master.utils.DateUtils;
import com.miqtech.master.utils.StringUtils;
import com.miqtech.master.vo.PageVO;

@Component
public class BountyGradeService {
	@Autowired
	private QueryDao queryDao;
	@Autowired
	private BountyGradeDao bountyGradeDao;
	@Autowired
	private UserInfoDao userInfoDao;
	@Autowired
	private CoinHistoryService coinHistoryService;
	@Autowired
	private BountyPrizeService bountyPrizeService;
	@Autowired
	private AwardRecordDao awardRecordDao;
	@Autowired
	private CommodityHistoryService commodityHistoryService;
	@Autowired
	private BountyService bountyService;
	@Autowired
	private ObjectRedisOperateService objectRedisOperateService;
	@Autowired
	private BountyDao bountyDao;

	/**
	 * 获取悬赏令审核列表
	 * @throws ParseException
	 */
	public PageVO getItems(String bountyId, String submitDateStart, String submitDateEnd, String telephone,
			Integer page, Integer rows, Integer isMarked) throws ParseException {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("pageStart", (page - 1) * rows);
		params.put("pageNum", rows);
		StringBuilder querySql = new StringBuilder(
				" select a.id,a.img,a.create_date,a.grade,b.nickname,a.user_id userId,b.telephone from bounty_grade a "
						+ "left join user_t_info b on a.user_id=b.id and b.is_valid=1 where a.bounty_id="
						+ NumberUtils.toLong(bountyId) + " and a.is_valid=1 and a.create_user_id<>-1 ");
		String countSql = "select count(1) from bounty_grade a left join user_t_info b on a.user_id=b.id where a.bounty_id="
				+ NumberUtils.toLong(bountyId) + " and a.is_valid=1  and a.create_user_id<>-1";
		StringBuilder addSql = new StringBuilder("");
		if (StringUtils.isNotBlank(telephone)) {
			addSql.append(" and b.telephone like '%" + telephone + "%'");
		}
		if (StringUtils.isNotBlank(submitDateStart)) {
			addSql.append(" and DATE_FORMAT(a.create_date,'%Y-%m-%d')>='" + submitDateStart + "'");
		}
		if (StringUtils.isNotBlank(submitDateEnd)) {
			addSql.append(" and DATE_FORMAT(a.create_date,'%Y-%m-%d')<='" + submitDateEnd + "'");
		}
		if (isMarked != null) {
			if (isMarked == 1) {
				addSql.append(" and a.state !=1 ");
			} else if (isMarked == 2) {
				addSql.append(" and a.state =1 ");
			}
		}
		querySql.append(addSql);
		countSql += addSql;
		querySql.append(" order by a.create_date asc limit :pageStart, :pageNum");
		List<Map<String, Object>> dataList = queryDao.queryMap(querySql.toString(), params);
		Number count = queryDao.query(countSql);
		PageVO vo = new PageVO();
		vo.setList(dataList);
		vo.setTotal(count.longValue());
		vo.setCurrentPage(page);
		return vo;
	}

	public BountyGrade saveOrUpdate(BountyGrade bountyGrade) {
		if (bountyGrade != null) {
			Date now = new Date();
			bountyGrade.setUpdateDate(now);
			if (bountyGrade.getId() != null) {
				BountyGrade old = findById(bountyGrade.getId());
				bountyGrade = BeanUtils.updateBean(old, bountyGrade);
			} else {
				bountyGrade.setValid(CommonConstant.INT_BOOLEAN_TRUE);
				bountyGrade.setCreateDate(now);
			}
			return bountyGradeDao.save(bountyGrade);
		}
		return null;
	}

	/**
	 * 批量保存
	 */
	public void save(List<BountyGrade> vs) {
		if (CollectionUtils.isNotEmpty(vs)) {
			bountyGradeDao.save(vs);
		}
	}

	public BountyGrade findById(Long id) {
		return bountyGradeDao.findOne(id);
	}

	/**
	 *
	 * @param bountyId
	 * @param valid
	 * @return
	 */
	public List<BountyGrade> findByBountyIdAndUserId(Long bountyId, Long userId) {
		return bountyGradeDao.findByBountyIdAndValidAndUserIdOrderByCreateDateDesc(bountyId, 1, userId);
	}

	public List<Map<String, Object>> getBountyGradeByBountyId(Long bountyId, Integer valid) {
		String querySql = "select a.* from bounty_grade a where a.bounty_id=" + bountyId
				+ " and is_valid=1 and state!=4 and state!=3";
		Bounty bounty = bountyService.findById(bountyId);
		if (bounty.getOrderType() != null) {
			if (bounty.getOrderType() == 1) {
				querySql += " order by a.state asc,a.grade asc,a.create_date desc";
			} else {
				querySql += " order by a.state asc,a.grade desc,a.create_date desc";
			}
		} else {
			querySql += " order by a.create_date desc";
		}
		List<Map<String, Object>> dataList = queryDao.queryMap(querySql);
		return dataList;
	}

	public List<Map<String, Object>> getAllBountyGradeByBountyId(Long bountyId, Integer valid) {
		String querySql = "select a.* from bounty_grade a where a.bounty_id=" + bountyId + " and is_valid=1";
		List<Map<String, Object>> dataList = queryDao.queryMap(querySql);
		return dataList;
	}

	public List<Map<String, Object>> getBountyGradeByBountyIdAndGrade(Long bountyId) {
		String querySql = "select a.*,b.nickname,b.username from bounty_grade a "
				+ " left join user_t_info b on a.user_id=b.id and b.is_valid=1" + " where a.bounty_id=" + bountyId
				+ " and a.is_valid=1 and a.grade is not null and a.state!=4";
		Bounty bounty = bountyService.findById(bountyId);
		if (bounty.getType() != null && bounty.getType() != 1) {
			if (bounty.getOrderType() != null) {
				if (bounty.getOrderType() == 1) {
					querySql += " order by a.state asc,a.grade asc,a.create_date asc";
				} else {
					querySql += " order by a.state asc,a.grade desc,a.create_date asc";
				}
			} else {
				querySql += " order by a.create_date asc";
			}
		} else {
			querySql += " order by a.create_date asc";
		}
		List<Map<String, Object>> dataList = queryDao.queryMap(querySql);
		return dataList;
	}

	public List<Map<String, Object>> getBountyPrizeByBountyId(Long bountyId) {
		String querySql = "select a.id,a.award_type awardType,a.award_num awardNum,a.max_num maxNum,a.award_sub_type awardSubType from bounty_prize a where a.bounty_id="
				+ bountyId + " and a.is_valid=1 order by a.create_date,a.id";
		List<Map<String, Object>> dataList = queryDao.queryMap(querySql);
		return dataList;
	}

	/**
	 * 获奖奖品为网娱自有时直接发放给用户
	 * 0-发放成功1-发放失败
	 */
	public int grantByTypePub(int subType, Double quota, CommodityHistory commodityHistory, Long bountyId) {
		if (subType == 6) {//流量
			//			if (systemConfig.getEnvironment().equals("online")) {
			//				if (chargeFlow(commodityHistory, quota) == 0) { //充值成功
			//					newAwardRecord(commodityHistory, 2, 4, commodityHistory.getUserId() + "", 3);
			//				} else {//充值失败
			//					newAwardRecord(commodityHistory, 2, 4, commodityHistory.getUserId() + "", 1);
			//				}
			//			} else {
			//				newAwardRecord(commodityHistory, 2, 4, commodityHistory.getUserId() + "", 3);
			//			}
		} else if (subType == 5) {//金币
			UserInfo user = userInfoDao.findOne(commodityHistory.getUserId());
			user.setCoin(user.getCoin() + quota.intValue());
			userInfoDao.save(user);
			CoinHistory coinHistory = new CoinHistory();
			coinHistory.setUserId(user.getId());
			coinHistory.setType(5);
			coinHistory.setCoin(quota.intValue());
			coinHistory.setDirection(1);
			coinHistory.setValid(CommonConstant.INT_BOOLEAN_TRUE);
			coinHistory.setCreateDate(new Date());
			coinHistoryService.save(coinHistory);
			newAwardRecord(commodityHistory, 1, 2, coinHistory.getId().toString(), 3);
			return changeStatusAndRemark(commodityHistory, "0", bountyId);
		}
		return 0;
	}

	/**
	 * @param telephone
	 * @param quota
	 * @return 1充值失败 0充值成功
	 */
	public int chargeFlow(CommodityHistory commodityHistory, Double quota) {
		String serial = getSerial(4);
		Map<String, String> params = Maps.newHashMap();
		params.put("phone", commodityHistory.getAccount());
		params.put("quota", quota.toString());
		params.put("orderid", serial);
		String result = com.miqtech.master.thirdparty.service.juhe.JuheFlow.flowCharge(params);

		// 更新充值状态
		JSONObject object = new Gson().fromJson(result, JSONObject.class);
		if (object != null) {
			// 记录请求结果
			Integer code = object.getInteger("error_code");
			if (!code.equals(0)) {
				return 1;
			}
		} else {
			return 1;
		}
		return 0;
	}

	/**
	 * 生成订单编号
	 */
	public String getSerial(int awardType) {
		// 以时间+类型作为订单前部分
		String tradeNo = DateUtils.dateToString(new Date(), "yyyyMMddHHmmssSSS") + "-0-" + awardType;
		// 累加递增部分
		Joiner joiner = Joiner.on("_");
		String redisKey = joiner.join("wy_bounty_prize_order_repeat", tradeNo);
		RedisConnectionFactory factory = objectRedisOperateService.getRedisTemplate().getConnectionFactory();
		RedisAtomicInteger redisRepeat = new RedisAtomicInteger(redisKey, factory);
		int repeat = redisRepeat.incrementAndGet();
		if (repeat <= 1) {
			redisRepeat.expire(1, TimeUnit.MINUTES);
		}
		String repeatStr = repeat < 10 ? "00" + repeat : repeat < 100 ? "0" + repeat : String.valueOf(repeat);
		return tradeNo.substring(3, tradeNo.length()) + repeatStr;
	}

	/**
	 * 记录财务信息
	 * @param subType 发放小类别:0-库存(指向amuse_r_award_type.id),1-自有红包,2-自有金币,3-充值话费,4-充值流量,5-充值Q币
	 */
	private long newAwardRecord(CommodityHistory commodityHistory, int type, int subType, String targetId, int status) {
		BountyPrize bountyPrize = bountyPrizeService.findById(commodityHistory.getCommodityId());
		AwardRecord awardRecord = new AwardRecord();
		awardRecord.setUserId(commodityHistory.getUserId());
		awardRecord.setType(type);
		awardRecord.setSubType(subType);
		if (StringUtils.isNotBlank(targetId)) {
			awardRecord.setTargetId(NumberUtils.toLong(targetId));
		}
		awardRecord.setAmount(NumberUtils.toDouble(bountyPrize.getAwardNum().toString()));
		awardRecord.setStatus(status);
		awardRecord.setChecked(0);
		awardRecord.setSourceType(2);
		awardRecord.setSourceTargetId(commodityHistory.getCommodityId());
		awardRecord.setValid(CommodityConstant.INT_STATUS_TRUE);
		awardRecord.setCreateDate(new Date());

		awardRecordDao.save(awardRecord);

		commodityHistory.setStatus(1);
		commodityHistoryService.save(commodityHistory);
		return awardRecord.getId();
	}

	/*
	 * 处理订单状态和备注
	 */
	private int changeStatusAndRemark(CommodityHistory commodityHistory, String result, Long bountyId) {
		Integer oldStatus = commodityHistory.getStatus();
		if (result.equals("0")) { //发放成功
			if (CommodityConstant.VERIFY_STATUS_PASS.equals(oldStatus)
					|| CommodityConstant.VERIFY_STATUS_FAIL.equals(oldStatus)) {
				commodityHistory.setStatus(CommodityConstant.VERIFY_STATUS_FINISH);
			} else if (CommodityConstant.VERIFY_STATUS_APPEALPASS.equals(oldStatus)
					|| CommodityConstant.VERIFY_STATUS_APPEALFAIL.equals(oldStatus)) {
				commodityHistory.setStatus(CommodityConstant.VERIFY_STATUS_APPEALFINISH);
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

	/**
	 * 判断用户是否已有标记信息
	 */
	public List<Map<String, Object>> isMarked(String userId, String bountyId) {
		String querySql = "select * from bounty_grade where user_id=" + NumberUtils.toLong(userId)
				+ " and is_valid=1 and grade is not null and bounty_id=" + bountyId;
		List<Map<String, Object>> list = queryDao.queryMap(querySql);
		return list;
	}

	public BountyGrade saveGrade(BountyGrade bountyGrade) {
		Bounty bounty = bountyDao.findOne(bountyGrade.getBountyId());
		Date date = bounty.getEndTime();
		if (date.after(new Date())) {
			return bountyGradeDao.save(bountyGrade);
		} else {
			return null;
		}
	}

	/**
	 * 得到全部获奖用户
	 * @param bountyId
	 * @return
	 */
	public List<Map<String, Object>> getWinUser(Long bountyId) {
		String sql = "select * from bounty_grade where bounty_id=" + bountyId + " and grade>0 and is_valid=1";
		return queryDao.queryMap(sql);
	}
}
