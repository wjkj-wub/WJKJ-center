package com.miqtech.master.service.netbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.support.atomic.RedisAtomicInteger;
import org.springframework.stereotype.Component;

import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.netbar.NetbarOrderDao;
import com.miqtech.master.dao.netbar.NetbarRechargeActivityDao;
import com.miqtech.master.dao.netbar.NetbarRechargeActivityPrizeDao;
import com.miqtech.master.dao.user.UserRechargePrizeDao;
import com.miqtech.master.entity.netbar.NetbarOrder;
import com.miqtech.master.entity.netbar.NetbarRechargeActivity;
import com.miqtech.master.entity.netbar.NetbarRechargeActivityPrize;
import com.miqtech.master.entity.user.UserRechargePrize;
import com.miqtech.master.utils.DateUtils;

@Component
public class NetbarRechargeActivityService {
	@Autowired
	private QueryDao queryDao;
	@Autowired
	private NetbarRechargeActivityDao netbarRechargeActivityDao;

	@Autowired
	private NetbarRechargeActivityPrizeDao netbarRechargeActivityPrizeDao;
	@Autowired
	private JedisConnectionFactory redisConnectionFactory;
	@Autowired
	UserRechargePrizeDao userRechargePrizeDao;
	@Autowired
	NetbarOrderDao netbarOrderDao;
	@Autowired
	NetbarOrderService netbarOrderService;

	public List<NetbarRechargeActivity> list(Long netbarId) {

		List<NetbarRechargeActivity> activitys = netbarRechargeActivityDao.findByNetbarIdAndValidOrderByIdDesc(
				netbarId.intValue(), 1);
		if (activitys != null) {
			for (NetbarRechargeActivity activity : activitys) {
				int id = activity.getId().intValue();
				activity.setPrizes(setReceiveCount(netbarRechargeActivityPrizeDao.findByActivityIdAndValid(id, 1)));
			}
		}
		return activitys;
	}

	/**
	 * 查找领取数量
	 * @param prizes
	 * @return
	 */
	public List<NetbarRechargeActivityPrize> setReceiveCount(List<NetbarRechargeActivityPrize> prizes) {
		if (prizes != null) {
			for (NetbarRechargeActivityPrize prize : prizes) {
				String sql = "select count(recharge_prize_id) receiveCount from user_recharge_prize where recharge_prize_id="
						+ prize.getId() + " and is_valid in(0,1) group by recharge_prize_id";
				Number count = (Number) queryDao.query(sql);
				prize.setReceiveCount(count == null ? 0 : count.intValue());
			}
		}
		return prizes;
	}

	/**
	 * 检查剩余库存
	 * @param prizes
	 * @return
	 */
	public int getSupReceiveCount(List<NetbarRechargeActivityPrize> prizes) {
		int countsum = 0;
		if (prizes != null) {
			for (NetbarRechargeActivityPrize prize : prizes) {
				if (prize.getPrizeCount() == -1) {
					countsum = 1;
					break;
				}
				String sql = "select count(recharge_prize_id) receiveCount from user_recharge_prize where recharge_prize_id="
						+ prize.getId() + " and is_valid=1 group by recharge_prize_id";
				Number count = (Number) queryDao.query(sql);
				countsum += (prize.getPrizeCount() - count.intValue());
			}
		}
		return countsum;
	}

	/**
	* 删除充值活动
	* @param id
	*/
	public void deleteAcivity(long id) {
		netbarRechargeActivityDao.delete(id);
		String sql = "update netbar_t_recharge_activity_prize set is_valid=0 where activity_id=" + id;
		queryDao.update(sql);
	}

	/**
	* 删除充值活动
	* @param id
	*/
	public void deleteAcivitys(String ids) {
		String sql = "update netbar_t_recharge_activity a set a.is_valid=0  where  find_in_set(a.id,'" + ids + "')";
		queryDao.update(sql);
	}

	/**
	* 统计参与网吧数量
	* @param id
	*/
	public int querynetbars() {
		String sql = "select count(distinct netbar_id) from netbar_t_recharge_activity where is_valid=1";
		Number count = (Number) queryDao.query(sql);
		return count.intValue();
	}

	public NetbarRechargeActivity findbyId(long id) {
		NetbarRechargeActivity activity = netbarRechargeActivityDao.findOne(id);
		activity.setPrizes(netbarRechargeActivityPrizeDao.findByActivityIdAndValid((int) id, 1));
		return activity;
	}

	/**
	* 删除充值活动
	* @param id
	*/
	public List<Map<String, Object>> selectMerchants() {

		String sql = "select id netbarId from  netbar_t_merchant  where  is_valid=1";
		return queryDao.queryMap(sql);
	}

	/**
	 * 查找开启的活动
	 * @return
	 */
	public NetbarRechargeActivity findStartActivity(int netbarId) {
		List<NetbarRechargeActivity> activitys = netbarRechargeActivityDao.findByValidAndStartStatusAndNetbarId(1, 1,
				netbarId);
		if (activitys != null && activitys.size() > 0) {
			NetbarRechargeActivity activity = activitys.get(0);
			activity.setPrizes(netbarRechargeActivityPrizeDao.findByActivityIdAndValid(activity.getId().intValue(), 1));
			return activity;
		} else {
			return null;
		}
	}

	/**
	 * 查找开启的活动
	 * @return
	 */
	public NetbarRechargeActivity findStartActivityOnly(int netbarId) {
		List<NetbarRechargeActivity> activitys = netbarRechargeActivityDao.findByValidAndStartStatusAndNetbarId(1, 1,
				netbarId);
		if (activitys != null && activitys.size() > 0) {
			NetbarRechargeActivity activity = activitys.get(0);
			return activity;
		} else {
			return null;
		}
	}

	/**
	* 关闭充值活动
	* @param id
	*/
	public void closeAcivity(long netbarid) {
		String sql = "update netbar_t_recharge_activity set start_status=2 where is_valid=1 and start_status=1 and netbar_id="
				+ netbarid;
		queryDao.update(sql);
	}

	/**
	* 检查起始时间
	* @param id
	*/
	public Map<String, Object> checkAcivityDate(String startDate, int netbarId, Integer id) {
		//	String datestr = new SimpleDateFormat("yyyy-MM-dd").format(startDate);
		String sql = "select start_date,name,id  from netbar_t_recharge_activity  where is_valid=1  "
				+ (id != null ? " and id!=" + id : "") + " and  DATE_FORMAT(start_date,'%Y-%m-%d')='" + startDate
				+ "' and netbar_id=" + netbarId + " limit 1";
		Map<String, Object> obj = queryDao.querySingleMap(sql);
		return obj;
	}

	/**
	* 检查起始时间
	* @param id
	*/
	public Map<String, Object> checkAcivityTimedDate(Date startDate, int netbarId) {
		String datestr = new SimpleDateFormat("yyyy-MM-dd").format(startDate);
		String sql = "select start_date,name,id  from netbar_t_recharge_activity  where is_valid=1  and start_status=0 and  DATE_FORMAT(start_date,'%Y-%m-%d')='"
				+ datestr + "' and netbar_id=" + netbarId + " limit 1";
		Map<String, Object> obj = queryDao.querySingleMap(sql);
		return obj;
	}

	/**
	* 保存充值活动
	* @param id
	*/
	public void saveActivity(NetbarRechargeActivity activity) {
		boolean isnew = false;
		if (activity.getId() == null) {
			isnew = true;
		}
		netbarRechargeActivityDao.save(activity);
		List<NetbarRechargeActivityPrize> activityPrizes = activity.getPrizes();
		List<NetbarRechargeActivityPrize> prizes = new ArrayList<NetbarRechargeActivityPrize>(10);
		if (activityPrizes != null) {
			for (NetbarRechargeActivityPrize prize : activityPrizes) {
				if (prize.getPrizeName() != null) {
					if (isnew) {
						prize.setId(null);
					}
					boolean hasid = false;
					if (prize.getId() == null) {
						hasid = true;
					}
					prize.setActivityId(activity.getId().intValue());
					prize.setValid(1);
					prize.setCreateDate(new Date());
					netbarRechargeActivityPrizeDao.save(prize);
					if (hasid) {
						prizes.add(prize);
					}
				}
			}
		}
		if (activity.getType() == 2 && activity.getStartStatus() == 1) {
			NetbarRechargeActivityPrize prize = addThanks(activity.getId().intValue());
			if (prize != null) {
				prizes.add(prize);
			}
		}
		if (activity.getStartStatus() == 1) {
			if (CollectionUtils.isNotEmpty(prizes)) {
				for (NetbarRechargeActivityPrize prize : prizes) {
					RedisAtomicInteger leftNum = new RedisAtomicInteger(CommonConstant.NETBAR_RECHARGE_PRIZE_LEFT_NUM
							+ prize.getId(), redisConnectionFactory);
					leftNum.set(prize.getPrizeCount());
				}
			}
		}
		if (activity.getDeleteId() != null && !"".equals(activity.getDeleteId())) {
			deleteAcivityPrizes(activity.getDeleteId());
		}

	}

	/**
	* 查找奖品名
	* @param id
	*/
	public List<Map<String, Object>> searchName(int type, long netbatId) {
		String sql = "select distinct prize_name label ,0 id from netbar_t_recharge_activity_prize  a,netbar_t_recharge_activity b where a.activity_id=b.id and b.netbar_id="
				+ netbatId + " and a.prize_name<>'谢谢惠顾' order by a.create_date desc limit 10 ";
		return queryDao.queryMap(sql);
	}

	/**
	* 删除奖品
	* @param id
	*/
	public void deleteAcivityPrizes(String ids) {
		String sql = "update netbar_t_recharge_activity_prize set is_valid=0 where find_in_set(id,'" + ids + "')";
		queryDao.update(sql);
	}

	/**
	* 改变活动状态
	* @param id
	*/
	public void changeAcivity(int id, Integer status) {
		if (status == 1) {
			//添加谢谢惠顾概率
			addThanks(id);
			//add redis
			List<NetbarRechargeActivityPrize> prizes = netbarRechargeActivityPrizeDao.findByActivityIdAndValid(id, 1);
			if (CollectionUtils.isNotEmpty(prizes)) {
				for (NetbarRechargeActivityPrize prize : prizes) {
					RedisAtomicInteger leftNum = new RedisAtomicInteger(CommonConstant.NETBAR_RECHARGE_PRIZE_LEFT_NUM
							+ prize.getId(), redisConnectionFactory);
					leftNum.set(prize.getPrizeCount());
				}
			}
		}
		String sql = "update netbar_t_recharge_activity set start_status=" + status + " where is_valid=1  and id=" + id;
		queryDao.update(sql);
	}

	//添加谢谢惠顾概率
	public NetbarRechargeActivityPrize addThanks(int id) {
		String sqls = "select 100-sum(full_param) surplus  from netbar_t_recharge_activity_prize a,netbar_t_recharge_activity b "
				+ "	where a.activity_id=b.id and b.type=2 and b.id=" + id + " and a.is_valid=1 group by b.id";
		Number surplus = (Number) queryDao.query(sqls);
		if (surplus != null && surplus.intValue() > 0) {
			NetbarRechargeActivityPrize prize = new NetbarRechargeActivityPrize();
			prize.setActivityId(id);
			prize.setPrizeName("谢谢惠顾");
			prize.setFullParam(surplus.intValue());
			prize.setPrizeCount(-1);
			prize.setValid(1);
			netbarRechargeActivityPrizeDao.save(prize);
			return prize;
		}
		return null;
	}

	/**用户领取兑换券
	 * @param netbarId
	 * @param userId
	 * @param amount
	 * @return
	 */
	public Map<String, Object> userGetRechargePrize(Long netbarId, Long userId, Double amount) {
		Map<String, Object> prize = new HashMap<String, Object>();
		Map<String, Object> type = queryDao
				.querySingleMap("select a.id,a.type from netbar_t_recharge_activity a where  a.start_status=1 and a.is_valid=1 and a.netbar_id="
						+ netbarId);
		if (type == null) {
			return prize;
		} else {
			Number n = queryDao
					.query("select count(1) from user_recharge_prize a where (a.is_valid=1 or a.is_valid=0) and a.netbar_id="
							+ netbarId
							+ " and a.user_id="
							+ userId
							+ " and date_format(a.create_date,'%Y-%m-%d')='"
							+ DateUtils.dateToString(new Date(), DateUtils.YYYY_MM_DD) + "'");
			if (n.intValue() > 0) {
				return prize;
			} else {
				if (((Number) type.get("type")).intValue() == 1) {//满送
					List<Map<String, Object>> prizes = queryDao
							.queryMap("select b.id, b.prize_name name,b.activity_id,c.name netbar_name from netbar_t_recharge_activity a left join netbar_t_recharge_activity_prize b on a.id = b.activity_id left join netbar_t_info c on a.netbar_id=c.id where  a.start_status = 1 and a.is_valid = 1 and b.is_valid = 1 and a.type = 1 and netbar_id="
									+ netbarId
									+ " and b.full_param <="
									+ amount
									+ " order by abs("
									+ amount
									+ " - b.full_param)");
					if (CollectionUtils.isEmpty(prizes)) {
						return prize;
					} else {
						for (Map<String, Object> map : prizes) {
							RedisAtomicInteger leftNum = new RedisAtomicInteger(
									CommonConstant.NETBAR_RECHARGE_PRIZE_LEFT_NUM + map.get("id"),
									redisConnectionFactory);//库存不足取下一个商品
							if (leftNum.get() > 0 || leftNum.get() == -1) {
								prize = map;
								if (leftNum.get() > 0) {
									leftNum.decrementAndGet();
								}
								saveRecord(netbarId, userId, ((Number) map.get("id")).longValue(),
										((Number) map.get("activity_id")).longValue(), prize);
								break;
							}
						}
						if (prize.isEmpty()) {
							queryDao.update("update netbar_t_recharge_activity set start_status=2 where id="
									+ type.get("id"));
						}
						return prize;
					}
				} else {//满抽
					List<Map<String, Object>> prizes = queryDao
							.queryMap("select b.id,b.activity_id,b.prize_name, b.full_param from netbar_t_recharge_activity a left join netbar_t_recharge_activity_prize b on a.id = b.activity_id where a.netbar_id="
									+ netbarId
									+ " and a.start_status = 1 and a.is_valid = 1 and b.is_valid = 1 and a.type = 2 and a.full_amount <= "
									+ amount);
					if (CollectionUtils.isNotEmpty(prizes)) {
						prize.put("lottery", 1);
					}
					return prize;
				}
			}
		}
	}

	public Map<String, Object> lottery(Long netbarId, Long userId, Long orderId) {
		Map<String, Object> result = new HashMap<String, Object>();
		Map<String, Object> type = queryDao
				.querySingleMap("select a.id,a.type from netbar_t_recharge_activity a where  a.start_status=1 and a.is_valid=1 and a.netbar_id="
						+ netbarId);
		NetbarOrder netbarOrder = netbarOrderDao.findOne(orderId);
		Number n = queryDao
				.query("select count(1) from user_recharge_prize a where (a.is_valid=1 or a.is_valid=0) and a.netbar_id="
						+ netbarId
						+ " and a.user_id="
						+ userId
						+ " and date_format(a.create_date,'%Y-%m-%d')='"
						+ DateUtils.dateToString(new Date(), DateUtils.YYYY_MM_DD) + "'");
		if (n.intValue() == 0 && netbarOrderService.isTodayPayedOrder(orderId) && type != null
				&& ((Number) type.get("type")).intValue() == 2) {
			List<Map<String, Object>> prizes = queryDao
					.queryMap("select b.id,b.activity_id,b.prize_name, b.full_param from netbar_t_recharge_activity a left join netbar_t_recharge_activity_prize b on a.id = b.activity_id where  a.netbar_id="
							+ netbarId
							+ " and a.start_status = 1 and a.is_valid = 1 and b.is_valid = 1 and a.type = 2 and a.full_amount <= "
							+ netbarOrder.getAmount());
			if (CollectionUtils.isNotEmpty(prizes)) {
				List<Map<String, Object>> effectivePrizes = new ArrayList<Map<String, Object>>();
				int probability = 0;
				int disabledProbability = 0;
				for (Map<String, Object> map : prizes) {
					RedisAtomicInteger leftNum = new RedisAtomicInteger(CommonConstant.NETBAR_RECHARGE_PRIZE_LEFT_NUM
							+ map.get("id"), redisConnectionFactory);
					if (leftNum.get() == 0) {
						disabledProbability += ((Number) map.get("full_param")).intValue();
					} else {
						effectivePrizes.add(map);
					}
					probability += ((Number) map.get("full_param")).intValue();
				}
				if (effectivePrizes.size() == 0) {
					queryDao.update("update netbar_t_recharge_activity set start_status=2 where id=" + type.get("id"));
					return result;
				}
				if (probability != 100) {
					return result;
				}
				if (disabledProbability == 0) {
					effectivePrizes = prizes;
				} else {
					//库存不足的商品概率平均分配给其他商品
					int averageProbability = disabledProbability / effectivePrizes.size();
					int totalProbability = 0;
					for (int i = 0; i < effectivePrizes.size(); i++) {
						Map<String, Object> tmp = effectivePrizes.get(i);
						if (i < effectivePrizes.size() - 1) {
							tmp.put("full_param", ((Number) tmp.get("full_param")).intValue() + averageProbability);
							totalProbability += ((Number) tmp.get("full_param")).intValue();
						} else {
							tmp.put("full_param", 100 - totalProbability);
						}
					}
				}
				int tmp = 0;
				List<Map<String, Object>> lotteryList = new ArrayList<Map<String, Object>>();
				for (Map<String, Object> obj : effectivePrizes) {
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("id", obj.get("id"));
					map.put("activity_id", obj.get("activity_id"));
					map.put("name", obj.get("prize_name"));
					map.put("min", tmp);
					tmp += ((Number) obj.get("full_param")).intValue();
					map.put("max", tmp);
					lotteryList.add(map);
				}
				Random random = new Random();
				int lotteryRandom = random.nextInt(100);
				for (Map<String, Object> obj : lotteryList) {
					if (lotteryRandom >= (int) obj.get("min") && lotteryRandom < (int) obj.get("max")) {
						result.put("name", obj.get("name"));
						saveRecord(netbarId, userId, ((Number) obj.get("id")).longValue(),
								((Number) obj.get("activity_id")).longValue(), null);
						break;
					}
				}

			}
		}
		return result;
	}

	public void saveRecord(Long netbarId, Long userId, Long prizeId, Long activityId, Map<String, Object> prize) {
		if (prize == null) {
			RedisAtomicInteger leftNum = new RedisAtomicInteger(
					CommonConstant.NETBAR_RECHARGE_PRIZE_LEFT_NUM + prizeId, redisConnectionFactory);
			if (leftNum.get() > 0) {
				leftNum.decrementAndGet();
			}
		}
		UserRechargePrize userRechargePrize = new UserRechargePrize();
		userRechargePrize.setRechargePrizeId(prizeId);
		userRechargePrize.setNetbarId(netbarId);
		userRechargePrize.setUserId(userId);
		NetbarRechargeActivity netbarRechargeActivity = netbarRechargeActivityDao.findOne(activityId);
		userRechargePrize.setExpireDate(DateUtils.addDays(new Date(), netbarRechargeActivity.getEffectDays()));
		userRechargePrize.setValid(1);
		userRechargePrize.setCreateDate(new Date());
		userRechargePrizeDao.save(userRechargePrize);
		if (prize != null) {
			prize.put("start_date", new Date());
			prize.put("end_date", userRechargePrize.getExpireDate());
			prize.put("status", 1);
			prize.put("id", userRechargePrize.getId());
		}
	}
}
