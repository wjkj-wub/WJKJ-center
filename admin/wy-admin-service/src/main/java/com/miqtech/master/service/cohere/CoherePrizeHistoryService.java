package com.miqtech.master.service.cohere;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.support.atomic.RedisAtomicInteger;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.cohere.CoherePrizeDao;
import com.miqtech.master.dao.cohere.CoherePrizeHistoryDao;
import com.miqtech.master.entity.cohere.CoherePrize;
import com.miqtech.master.entity.cohere.CoherePrizeHistory;
import com.miqtech.master.service.common.ObjectRedisOperateService;
import com.miqtech.master.thirdparty.service.juhe.JuheGameCharge;
import com.miqtech.master.thirdparty.service.juhe.JuheTencentCharge;
import com.miqtech.master.thirdparty.util.SMSMessageUtil;
import com.miqtech.master.utils.DateUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.vo.PageVO;

@Service
public class CoherePrizeHistoryService {

	@Autowired
	private CoherePrizeHistoryDao coherePrizeHistoryDao;
	@Autowired
	private CoherePrizeDao coherePrizeDao;
	@Autowired
	private QueryDao queryDao;
	@Autowired
	private ObjectRedisOperateService objectRedisOperateService;

	public static final String COHERE_PRIZE_SEND_LIMIT_PREFIX = "cohere_prize_send_limit_prefix";

	public CoherePrizeHistory saveOrUpdate(CoherePrizeHistory coherePrizeHistory) {
		return coherePrizeHistoryDao.save(coherePrizeHistory);
	}

	public List<CoherePrizeHistory> findByUserIdAndStateAndValid(Long userId, Integer state) {
		return coherePrizeHistoryDao.findByUserIdAndStateAndValid(userId, state, 1);
	}

	public CoherePrizeHistory findOne(Long id) {
		return coherePrizeHistoryDao.findOne(id);
	}

	public PageVO getPrizeHistory(Long activityId, Integer page, Integer pageSize) {
		String totalSql = "SELECT count(a.id) FROM cohere_prize_history a "
				+ "LEFT JOIN cohere_prize b ON a.prize_id = b.id "
				+ "LEFT JOIN cohere_activity c ON b.activity_id = c.id "
				+ "WHERE a.is_valid = 1 AND b.is_valid = 1 AND c.is_valid = 1 AND a.is_get=1 AND c.id = " + activityId;
		Number totalCount = queryDao.query(totalSql);
		if (null != totalCount && totalCount.intValue() > 0) {
			int start = (page - 1) * pageSize;
			String sql = "SELECT d.nickname,a.create_date,d.icon,b.name,if(b.num>1,2,b.num) as num "
					+ "FROM cohere_prize_history a " + "LEFT JOIN cohere_prize b ON a.prize_id = b.id "
					+ "LEFT JOIN cohere_activity c ON b.activity_id = c.id left join user_t_info d on a.user_id=d.id "
					+ "WHERE a.is_valid = 1 AND b.is_valid = 1 AND c.is_valid = 1  AND a.is_get=1 AND c.id = "
					+ activityId + " and d.is_valid=1 " + "order by a.create_date desc limit " + start + " ,"
					+ pageSize;
			PageVO vo = new PageVO(queryDao.queryMap(sql));
			if (page * pageSize >= totalCount.intValue()) {
				vo.setIsLast(1);
			}
			vo.setTotal(totalCount.longValue());
			return vo;
		} else {
			PageVO vo = new PageVO(new ArrayList<Map<String, Object>>());
			return vo;
		}
	}

	public Map<String, Object> findPrizeInfo(Long prizeId) {
		String sql = "select   cp.name prizeName,  ui.nickname from  cohere_prize_history cph "
				+ " left join cohere_prize cp     on cp.id = cph.prize_id   left join user_t_info ui     on ui.id = cph.user_id "
				+ " where cph.id = " + prizeId + "  and cph.is_valid = 1 and ui.is_valid = 1  ";
		return queryDao.querySingleMap(sql);
	}

	/*
	 * Q币充值
	 */
	public int chargeQB(CoherePrizeHistory coherePrizeHistory, Integer quota) {
		String account = coherePrizeHistory.getAccount();
		String result = null;
		if (quota == 0) {
			return 0;
		}
		if (quota > 5) {
			if (StringUtils.isNotBlank(account)) {
				Map<String, String> params = Maps.newHashMap();
				params.put("cardid", "220612");
				params.put("cardnum", quota.toString());
				params.put("orderid", coherePrizeHistory.getTranNo());
				params.put("game_userid", account);
				result = JuheGameCharge.order(params);

				String juHeAwardRecordResult = juHeAwardRecord(result);
				if ("0".equals(juHeAwardRecordResult)) {
					return 0;
				} else if ("1".equals(juHeAwardRecordResult)) {//充值中
					return 1;
				} else if ("208810".equals(juHeAwardRecordResult)) {//账户余额不足
					return -2;
				} else {
					return -1;
				}
			} else {
				return -1;
			}
		} else {
			if (StringUtils.isNotBlank(account)) {
				Map<String, String> params = Maps.newHashMap();
				params.put("nums", quota.toString());
				params.put("uorderid", coherePrizeHistory.getTranNo());
				params.put("game_userid", account);
				result = JuheTencentCharge.orderQ(params);

				String juHeAwardRecordResult = juHeAwardRecord(result);
				if ("0".equals(juHeAwardRecordResult)) {
					return 0;
				} else if ("1".equals(juHeAwardRecordResult)) {//充值中
					return 1;
				} else if ("210808".equals(juHeAwardRecordResult)) {//账户余额不足
					return -2;
				} else {
					return -1;
				}
			} else {
				return -1;
			}
		}
	}

	private String juHeAwardRecord(String result) {
		Gson gson = new Gson();
		JSONObject object = gson.fromJson(result, JSONObject.class);
		if (object.getInteger("error_code") == 0) {
			String chargeStatus = object.getJSONObject("result").getString("game_state");
			if (chargeStatus.equals("1")) {
				return "0"; //充值成功
			} else {
				return "1"; //充值中
			}
		} else {
			return object.getString("reason");
		}
	}

	/**
	 * @param result
	 * @return 1充值成功  0充值中 9充值失败
	 */
	private String juHeAwardStatus(String result) {
		Gson gson = new Gson();
		JSONObject object = gson.fromJson(result, JSONObject.class);
		if (object.getInteger("error_code") == 0) {
			JSONObject jsonResult = gson.fromJson(object.getString("result"), JSONObject.class);
			return jsonResult.getString("game_state");
		} else {
			return object.getString("reason");
		}
	}

	/**
	 * 生成订单编号
	 */
	public String genSerial(int awardType) {
		// 以时间+类型作为订单前部分
		String tradeNo = DateUtils.dateToString(new Date(), "yyyyMMddHHmmssSSS") + "-0-" + awardType;
		// 累加递增部分
		Joiner joiner = Joiner.on("_");
		String redisKey = joiner.join("wy_cohere_prize_order_repeat", tradeNo);
		RedisConnectionFactory factory = objectRedisOperateService.getRedisTemplate().getConnectionFactory();
		RedisAtomicInteger redisRepeat = new RedisAtomicInteger(redisKey, factory);
		int repeat = redisRepeat.incrementAndGet();
		if (repeat <= 1) {
			redisRepeat.expire(1, TimeUnit.MINUTES);
		}
		String repeatStr = repeat < 10 ? "00" + repeat : repeat < 100 ? "0" + repeat : String.valueOf(repeat);
		return tradeNo.substring(3, tradeNo.length()) + repeatStr;
	}

	/*
	 * 发送短信
	 */
	public void sendMsg(String[] phoneNum, String templateId, String... params) {
		SMSMessageUtil.sendTemplateMessage(phoneNum, templateId, params);
	}

	/**
	 * 批量发放
	 */
	public List<Map<String, Object>> getPrizeInfo(Map<String, Object> params, String activityId, String prizeHistoryIds,
			Integer type) {
		String searchSql = "";
		if (StringUtils.isNotBlank(params.get("prizeType").toString())) {
			searchSql = SqlJoiner.join(searchSql, " and cp.num=" + params.get("prizeType").toString());
		}
		if (StringUtils.isNotBlank(params.get("prizeState").toString())) {
			if (params.get("prizeState").toString().equals("1")) {
				searchSql = SqlJoiner.join(searchSql, " and cph.state !=3");
			} else if (params.get("prizeState").toString().equals("2")) {
				searchSql = SqlJoiner.join(searchSql, " and cph.state=3");
			} else if (params.get("prizeState").toString().equals("3")) {
				searchSql = SqlJoiner.join(searchSql, " and cph.question=1");
			}
		}
		String sql = "SELECT cph.id,cph.state,cp.type,cp.id prizeId, ca.id activityId FROM cohere_prize_history cph LEFT JOIN cohere_prize cp ON cph.prize_id = cp.id "
				+ "LEFT JOIN cohere_activity ca ON cp.activity_id = ca.id "
				+ "WHERE cph.is_valid = 1 AND cp.is_valid = 1 and cp.num not in (1) AND ca.is_valid = 1  AND cph.is_get=1 and cph.state=1 AND ca.id ="
				+ activityId + searchSql;
		if (type != 1) {
			if (StringUtils.isNotBlank(prizeHistoryIds)) {
				sql = SqlJoiner.join(sql, " and cph.id in (", prizeHistoryIds, ")");
			}
		}
		return queryDao.queryMap(sql);
	}

	/**
	 * 充值记录状态查询
	 * @
	 */
	public int updateStatus(Long id) {
		CoherePrizeHistory coherePrizeHistory = coherePrizeHistoryDao.findOne(id);
		String orderId = coherePrizeHistory.getTranNo(); // 用户自定义订单号
		CoherePrize coherePrize = coherePrizeDao.findOne(coherePrizeHistory.getPrizeId());
		Double value = coherePrize.getValue();
		String status = "0";
		if (value > 5) {//游戏直冲
			status = juHeAwardStatus(JuheGameCharge.checkChargeStatus(orderId));
			if (status.equals("9")) {
				coherePrizeHistory.setState(2);//发放失败
				saveOrUpdate(coherePrizeHistory);
				return 9;
			} else if (status.equals("0")) {
				coherePrizeHistory.setState(4);//正在发放
				saveOrUpdate(coherePrizeHistory);
				return 0;
			} else {
				coherePrizeHistory.setState(3);//发放成功
				saveOrUpdate(coherePrizeHistory);
				return 1;
			}
		} else { //q币直冲
			status = juHeAwardStatus(JuheTencentCharge.checkChargeStatus(orderId));
			if (status.equals("9")) {
				coherePrizeHistory.setState(2);//发放失败
				saveOrUpdate(coherePrizeHistory);
				return 9;
			} else if (status.equals("0")) {
				coherePrizeHistory.setState(4);//正在发放
				saveOrUpdate(coherePrizeHistory);
				return 0;
			} else {
				coherePrizeHistory.setState(3);//发放成功
				saveOrUpdate(coherePrizeHistory);
				return 1;
			}
		}
	}

	public List<Map<String, Object>> findUnFinishedPrize() {
		String sql = "select id from cohere_prize_history where state =4";
		return queryDao.queryMap(sql);
	}

	public boolean canSendPrize(Long id) {
		RedisAtomicInteger flag = new RedisAtomicInteger(COHERE_PRIZE_SEND_LIMIT_PREFIX + id.toString(),
				objectRedisOperateService.getRedisTemplate().getConnectionFactory());
		if (flag.intValue() > 0) {
			return false;
		}
		return true;
	}

	public void setSended(Long id) {
		RedisAtomicInteger flag = new RedisAtomicInteger(COHERE_PRIZE_SEND_LIMIT_PREFIX + id.toString(),
				objectRedisOperateService.getRedisTemplate().getConnectionFactory());
		flag.incrementAndGet();
	}

}
