package com.miqtech.master.service.netbar;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.netbar.NetbarRechargeOrderDao;
import com.miqtech.master.entity.netbar.NetbarRechargeOrder;
import com.miqtech.master.utils.SqlJoiner;

@Component
public class NetbarRechargeOrderService {

	@Autowired
	private QueryDao queryDao;
	@Autowired
	private NetbarRechargeOrderDao netbarRechargeOrderDao;

	public NetbarRechargeOrder findById(Long id) {
		if (id == null) {
			return null;
		}

		return netbarRechargeOrderDao.findOne(id);
	}

	/**
	 * 根据订单号查询一笔订单
	 */
	public NetbarRechargeOrder findValidByOutTradeNo(String outTradeNo) {
		if (StringUtils.isBlank(outTradeNo)) {
			return null;
		}

		List<NetbarRechargeOrder> orders = netbarRechargeOrderDao.findByOutTradeNoAndValid(outTradeNo,
				CommonConstant.INT_BOOLEAN_TRUE);
		if (CollectionUtils.isEmpty(orders)) {
			return null;
		}

		return orders.get(0);
	}

	public NetbarRechargeOrder save(NetbarRechargeOrder order) {
		return netbarRechargeOrderDao.save(order);
	}

	public List<Map<String, Object>> queryTodayOrdersByNetbarId(Long netbarId) {
		if (netbarId == null) {
			return null;
		}

		String sql = SqlJoiner.join(
				"SELECT id, out_trade_no outTradeNo, create_date createDate FROM netbar_recharge_order",
				" WHERE is_valid = 1 AND netbar_id = ", netbarId.toString(),
				" AND DATE(create_date) = DATE(now()) ORDER BY create_date DESC");
		return queryDao.queryMap(sql);
	}
}