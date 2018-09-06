package com.miqtech.master.service.netbar;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.consts.NetbarConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.netbar.NetbarFundInfoDao;
import com.miqtech.master.entity.netbar.NetbarFundDetail;
import com.miqtech.master.entity.netbar.NetbarFundInfo;
import com.miqtech.master.entity.netbar.NetbarInfo;
import com.miqtech.master.entity.netbar.NetbarMerchant;
import com.miqtech.master.service.common.StringRedisOperateService;
import com.miqtech.master.utils.ArithUtil;
import com.miqtech.master.utils.JsonUtils;
import com.miqtech.master.utils.StringUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 网吧资金信息Service
 */
@Component
public class NetbarFundInfoService {

	public static final Logger LOGGER = LoggerFactory.getLogger(NetbarFundInfoService.class);
	public static final String REDIS_NETBAR_FUND_WITHDRAW_PREFIX = "netbar_fund_withdraw_";

	@Autowired
	NetbarFundInfoDao netbarFundInfoDao;
	@Autowired
	NetbarFundDetailService netbarFundDetailService;
	@Autowired
	StringRedisOperateService redisOperateService;

	@Autowired
	QueryDao queryDao;

	/**
	 * 查询网吧ID对应的资金信息
	 */
	public NetbarFundInfo findByNetbarId(Long netbarId) {
		return netbarFundInfoDao.findByNetbarIdAndValid(netbarId, CommonConstant.INT_BOOLEAN_TRUE);
	}

	/**
	 * 单个保存/更新
	 */
	public NetbarFundInfo save(NetbarFundInfo fund) {
		return netbarFundInfoDao.save(fund);
	}

	/**
	 * 查询多个网吧ID对应的资金信息
	 */
	public List<NetbarFundInfo> findByNetbarIdIn(Collection<Long> netbarIds) {
		return netbarFundInfoDao.findByNetbarIdIn(netbarIds);
	}

	/**
	 * 批量保存
	 */
	public List<NetbarFundInfo> save(List<NetbarFundInfo> funds) {
		return (List<NetbarFundInfo>) netbarFundInfoDao.save(funds);
	}

	/**
	 * 批量增加网吧资金
	 *
	 * @param netbarAmounts
	 *            以网吧ID为key,value须为一个包含amount(金额)、type(收支类型)、serNumbers(订单号)[settlAccounts(当期核销金额)、quotaRatio(配额奖金比例%)]键的Map
	 * @return 操作成功的数量
	 */
	public synchronized int addAccountsBatch(Map<Long, Map<String, Object>> netbarAmounts) {
		if (MapUtils.isEmpty(netbarAmounts)) {
			return 0;
		}

		// 更新网吧资金信息
		Set<Long> keys = netbarAmounts.keySet();
		if (CollectionUtils.isNotEmpty(keys)) {
			// 根据网吧ID查询网吧资金列表
			List<NetbarFundInfo> fundInfoes = findByNetbarIdIn(keys);
			List<NetbarFundDetail> fundDetails = Lists.newArrayList();
			Date now = new Date();
			for (Long netbarId : keys) {
				// 获取金额及订单号
				Map<String, Object> config = netbarAmounts.get(netbarId);
				Double amount = MapUtils.getDouble(config, "amount");
				Double quota = MapUtils.getDouble(config, "quota");
				String serNumbers = MapUtils.getString(config, "serNumbers");
				Integer type = MapUtils.getInteger(config, "type");
				Integer direction = MapUtils.getInteger(config, "direction");
				// 未传入direction时,根据类型匹配收支方向
				if (direction == null) {
					LOGGER.error("记录网吧资金明细异常 - direction参数错误: netbarId " + netbarId + JsonUtils.objectToString(config));
					continue;
				}
				Double settlAccounts = MapUtils.getDouble(config, "settlAccounts");
				Double quotaRatio = MapUtils.getDouble(config, "quotaRatio");
				if (amount == null) {
					amount = NumberUtils.DOUBLE_ZERO;
				}
				if (quota == null) {
					quota = NumberUtils.DOUBLE_ZERO;
				}
				if ((NumberUtils.DOUBLE_ZERO.equals(amount) && NumberUtils.DOUBLE_ZERO.equals(quota))
						|| StringUtils.isBlank(serNumbers) || type == null) {
					continue;
				}

				// 找到网吧资金信息,并更新
				int radix = direction;
				if (NetbarConstant.NETBAR_FUND_DETAIL_DIRECTION_REFUND.equals(radix)) {
					radix = NetbarConstant.NETBAR_FUND_DETAIL_DIRECTION_INCOME;
				}
				NetbarFundInfo currFund = null;
				if (CollectionUtils.isNotEmpty(fundInfoes)) {
					for (NetbarFundInfo fi : fundInfoes) {
						if (netbarId.equals(fi.getNetbarId())) {
							// 更新网吧资金
							fi.addAccounts(amount * radix);
							fi.addUsableQuota(quota * radix);
							if (settlAccounts != null) {
								fi.setSettlAccounts(settlAccounts);
							}
							if (NetbarConstant.NETBAR_FUND_DETAIL_DIRECTION_INCOME.equals(radix)
									&& (!NetbarConstant.NETBAR_FUND_DETAIL_DIRECTION_REFUND.equals(direction))) {// 如果不是退款累计配额奖金
								fi.addQuota(quota);
							}
							fi.setUpdateDate(now);
							currFund = fi;
							break;
						}
					}
				}

				// 不存在网吧资金,初始化网吧资金
				if (currFund == null) {
					NetbarFundInfo fi = new NetbarFundInfo(netbarId);
					fi.setAccounts(amount * radix);
					fi.setUsableQuota(quota * radix);
					if (settlAccounts != null) {
						fi.setSettlAccounts(settlAccounts);
					}
					if (NetbarConstant.NETBAR_FUND_DETAIL_DIRECTION_INCOME.equals(direction)) {
						fi.addQuota(quota);
					}
					currFund = fi;
					fundInfoes.add(fi);
				}

				// 记录网吧资金收支明细
				if (amount > 0) {
					NetbarFundDetail fd = new NetbarFundDetail(netbarId, type, direction, amount, serNumbers);
					fd.setResidual(currFund.getAccounts());
					if (settlAccounts != null) {
						fd.setSettlAccounts(settlAccounts);
					}
					fd.setQuotaRatio(quotaRatio);
					fundDetails.add(fd);
				}
				if (quota > 0) {
					NetbarFundDetail fd = new NetbarFundDetail(netbarId, NetbarConstant.NETBAR_FUND_DETAIL_TYPE_QUOTA,
							direction, quota, serNumbers);
					fd.setResidual(currFund.getAccounts());
					if (settlAccounts != null) {
						fd.setSettlAccounts(settlAccounts);
					}
					fd.setQuotaRatio(quotaRatio);
					fundDetails.add(fd);
				}
			}

			// 保存网吧资金及明细
			fundInfoes = save(fundInfoes);
			fundDetails = netbarFundDetailService.save(fundDetails);

			// 检查执行状态并返回结果
			int operateNum = netbarAmounts.size();
			int addedNum = fundInfoes.size();
			if (addedNum < operateNum) {
				LOGGER.error("增加网吧资金异常:应操作 " + operateNum + " 条记录,实际操作 " + addedNum + " 条记录\n操作记录:"
						+ JSONObject.toJSONString(netbarAmounts) + "\n成功记录:" + JSONObject.toJSONString(fundInfoes));
			}
			return addedNum;
		}
		return 0;
	}

	/**
	 * 按订单退款
	 */
	public boolean refund(String tradeNo) {
		// 查询有效订单明细
		List<NetbarFundDetail> details = netbarFundDetailService.findRefundableBySerNumbers(tradeNo);
		if (CollectionUtils.isNotEmpty(details)) {
			Long netbarId = details.get(0).getNetbarId();

			// 统计须退款的资金数及配额奖金数
			Double refundAmount = NumberUtils.DOUBLE_ZERO;
			Double refundQuota = NumberUtils.DOUBLE_ZERO;
			for (NetbarFundDetail d : details) {
				if (NetbarConstant.NETBAR_FUND_DETAIL_TYPE_QUOTA.equals(d.getType())) {
					refundQuota = ArithUtil.add(refundQuota, d.getAmount());
				} else {
					refundAmount = ArithUtil.add(refundAmount, d.getAmount());
				}
			}

			// 增加退款金额
			Map<String, Object> config = Maps.newHashMap();
			config.put("amount", refundAmount);
			config.put("quota", refundQuota);
			config.put("serNumbers", tradeNo);
			config.put("type", NetbarConstant.NETBAR_FUND_DETAIL_TYPE_REFUND);
			config.put("direction", NetbarConstant.NETBAR_FUND_DETAIL_DIRECTION_REFUND);
			Map<Long, Map<String, Object>> netbarAmounts = Maps.newHashMap();
			netbarAmounts.put(netbarId, config);
			addAccountsBatch(netbarAmounts);

			// 停止100ms以确保数据库主从同步完毕
			try {
				Thread.sleep(100);
			} catch (Exception expect) {
			}

			return true;
		}
		return false;
	}

	public boolean canWithDraw(Long netbarId) {
		//		String key = REDIS_NETBAR_FUND_WITHDRAW_PREFIX + netbarId;
		//		String data = redisOperateService.getData(key);
		//		if (StringUtils.isNotBlank(data)) {
		//			return false;
		//		}
		//		return true;
		List<NetbarFundDetail> details = netbarFundDetailService
				.findByNetbarIdAndValidAndStatusAndDirectionAndType(netbarId, 1, 1, -1, 2);
		return CollectionUtils.isEmpty(details);

	}

	/**
	 * 自动支付-取现操作
	 */
	@Transactional
	public NetbarFundDetail withdrawV2(NetbarMerchant merchant, Double withdrawMoney) {
		Long netbarId = merchant.getNetbarId();
		NetbarFundInfo netbarFundInfo = findByNetbarId(netbarId);
		double payMoney = netbarFundInfo.getAccounts();
		if (payMoney < withdrawMoney) {
			return null;
		}
		netbarFundInfo.setAccounts(payMoney - withdrawMoney);
		this.save(netbarFundInfo);

		NetbarFundDetail fd = new NetbarFundDetail(netbarId, NetbarConstant.NETBAR_FUND_DETAIL_TYPE_WITHDRAW,
				NetbarConstant.NETBAR_FUND_DETAIL_DIRECTION_EXPAND, withdrawMoney, null);
		fd.setResidual(netbarFundInfo.getAccounts());
		fd = netbarFundDetailService.save(fd);
		return fd;
	}

	/**
	 * 取现操作
	 */
	@Transactional
	public int withdraw(NetbarMerchant merchant, Double withdrawMoney) {
		Long netbarId = merchant.getNetbarId();
		NetbarFundInfo netbarFundInfo = findByNetbarId(netbarId);
		double payMoney = netbarFundInfo.getAccounts();
		if (payMoney < withdrawMoney) {
			return -3;
		}
		netbarFundInfo.setAccounts(payMoney - withdrawMoney);
		this.save(netbarFundInfo);

		NetbarFundDetail fd = new NetbarFundDetail(netbarId, NetbarConstant.NETBAR_FUND_DETAIL_TYPE_WITHDRAW,
				NetbarConstant.NETBAR_FUND_DETAIL_DIRECTION_EXPAND, withdrawMoney, null);
		fd.setResidual(netbarFundInfo.getAccounts());
		fd = netbarFundDetailService.save(fd);
		//		//设置已经取现标识
		//		String key = REDIS_NETBAR_FUND_WITHDRAW_PREFIX + netbarId;
		//		redisOperateService.setData(key, "1", 7, TimeUnit.DAYS);
		return 0;
	}

	public void updateRatio() {
		try {
			String sql = "select area_code ,gold_ratio,vip_ratio,jewel_ratio from netbar_resource_area_quotta_raito where is_valid =1 ";
			List<Map<String, Object>> queryMap = queryDao.queryMap(sql);
			if (CollectionUtils.isNotEmpty(queryMap)) {
				for (Map<String, Object> map : queryMap) {
					String areaCode = MapUtils.getString(map, "area_code");
					String goldRatio = MapUtils.getString(map, "gold_ratio");
					String vipRatio = MapUtils.getString(map, "vip_ratio");
					areaCode = org.apache.commons.lang3.StringUtils.substring(areaCode, 0, 2);
					String netbarIdsSql = "select         id     from        netbar_t_info "
							+ "  where area_code like '" + areaCode
							+ "%'         and is_valid = 1         and id in         (select             netbar_id         from            netbar_fund_info         where is_valid = 1)         and levels =2";
					List<Map<String, Object>> ids = queryDao.queryMap(netbarIdsSql);
					if (CollectionUtils.isNotEmpty(ids)) {
						Joiner join = Joiner.on(",");
						String idsString = StringUtils.EMPTY;
						for (Map<String, Object> id : ids) {
							Number object = (Number) id.get("id");
							idsString = join.join(idsString, object.longValue());
						}
						idsString = org.apache.commons.lang3.StringUtils.removeStart(idsString, ",");
						if (StringUtils.isNotBlank(idsString)) {
							//更新黄金网吧配额比例
							String updateSql = "update     netbar_fund_info set    quota_ratio = " + goldRatio
									+ " where netbar_id in     (" + idsString + ")";
							queryDao.update(updateSql);
						}
					}

					netbarIdsSql = "select         id     from        netbar_t_info " + "  where area_code like '"
							+ areaCode
							+ "%'         and is_valid = 1         and id in         (select             netbar_id         from            netbar_fund_info         where is_valid = 1)         and levels =1";
					ids = queryDao.queryMap(netbarIdsSql);
					if (CollectionUtils.isNotEmpty(ids)) {
						Joiner join = Joiner.on(",");
						String idsString = StringUtils.EMPTY;
						for (Map<String, Object> id : ids) {
							Number object = (Number) id.get("id");
							idsString = join.join(idsString, object.longValue());
						}
						idsString = org.apache.commons.lang3.StringUtils.removeStart(idsString, ",");
						if (StringUtils.isNotBlank(idsString)) {
							//更新黄金网吧配额比例
							String updateSql = "update     netbar_fund_info set    quota_ratio = " + vipRatio
									+ " where netbar_id in     (" + idsString + ")";
							queryDao.update(updateSql);
						}
					}

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public double statisticsTotalQuota(Long netbarId) {
		if (null == netbarId || 0L == netbarId.longValue()) {
			return 0;
		}

		String sql = "select sum(amount) from netbar_fund_detail  where type=5 and direction =1 and netbar_id ="
				+ netbarId.longValue();
		Number quota = queryDao.query(sql);
		if (quota != null) {
			return quota.doubleValue();
		}
		return 0;
	}

	@Autowired
	private NetbarInfoService netbarInfoService;

	public NetbarFundInfo initInfo(Long netbarId) {
		NetbarFundInfo info = new NetbarFundInfo(netbarId);
		NetbarInfo netbar = netbarInfoService.findById(netbarId);
		double ratio = 0.00;

		int levels = netbar.getLevels() == null ? 0 : netbar.getLevels().intValue();
		String areaCode = StringUtils.substring(netbar.getAreaCode(), 0, 2) + "0000";
		String sql = "";
		if (levels == 1) {
			sql = "select vip_ratio from netbar_resource_area_quotta_raito where area_code ='" + areaCode
					+ "' and is_valid =1 ";
		} else if (levels == 2) {
			sql = "select gold_ratio from netbar_resource_area_quotta_raito where area_code ='" + areaCode
					+ "' and is_valid =1  ";
		}

		Number areaRatio = queryDao.query(sql);
		if (null != areaRatio) {
			ratio = areaRatio.doubleValue();
		}
		info.setQuotaRatio(ratio);
		return this.save(info);

	}

	public NetbarFundInfo initOrUpdateFundInfoRatio(Long netbarId) {
		NetbarFundInfo info = findByNetbarId(netbarId);
		if (info == null) {
			info = new NetbarFundInfo(netbarId);
		}
		NetbarInfo netbar = netbarInfoService.findById(netbarId);
		double ratio = 0.00;

		int levels = netbar.getLevels() == null ? 0 : netbar.getLevels().intValue();
		String areaCode = StringUtils.substring(netbar.getAreaCode(), 0, 2) + "0000";
		String sql = "";
		if (levels == 1) {
			sql = "select vip_ratio from netbar_resource_area_quotta_raito where area_code ='" + areaCode
					+ "' and is_valid =1 ";
		} else if (levels == 2) {
			sql = "select gold_ratio from netbar_resource_area_quotta_raito where area_code ='" + areaCode
					+ "' and is_valid =1  ";
		}

		Number areaRatio = queryDao.query(sql);
		if (null != areaRatio) {
			ratio = areaRatio.doubleValue();
		}
		info.setQuotaRatio(ratio);
		return this.save(info);

	}
}