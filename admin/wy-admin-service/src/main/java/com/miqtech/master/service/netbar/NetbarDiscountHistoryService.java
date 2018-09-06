package com.miqtech.master.service.netbar;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.netbar.NetbarDiscountHistoryDao;
import com.miqtech.master.entity.netbar.NetbarDiscountHistory;
import com.miqtech.master.entity.netbar.NetbarMerchant;
import com.miqtech.master.utils.SqlJoiner;

/**
 * 网吧优惠历史service
 */
@Component
public class NetbarDiscountHistoryService {

	@Autowired
	private QueryDao queryDao;
	@Autowired
	private NetbarDiscountHistoryDao netbarDiscountHistoryDao;

	public NetbarDiscountHistory save(NetbarDiscountHistory netbarMsgHistory) {
		return netbarDiscountHistoryDao.save(netbarMsgHistory);
	}

	/**
	 * 查询商户最后一条发布信息
	 */
	public String findLastContent(NetbarMerchant merchant, Integer type) {
		String sql = SqlJoiner.join("SELECT content FROM netbar_t_discount_history WHERE  netbar_id=", merchant
				.getNetbarId().toString(), " AND merchant_id=", merchant.getId().toString(), " AND type=", type
				.toString(), " AND IS_VALID = 1 ORDER BY CREATE_DATE DESC LIMIT 0,1");
		return queryDao.query(sql);
	}

	/**
	 * 分页查询优惠信息历史
	 */
	public List<Map<String, Object>> findMerchantDiscountHistoriesPage(int page, Integer type, Long netbarId) {
		StringBuffer condition = new StringBuffer();
		condition.append(" WHERE h.is_valid = 1 ");
		if (type != null) {
			condition.append(" AND h.type = ").append(type);
		}
		if (netbarId != null) {
			condition.append(" AND h.netbar_id = ").append(netbarId);
		}
		condition.append(" order by h.id desc ").append(" LIMIT ").append((page - 1) * 5).append(" , ").append(5);

		String sql = "SELECT h.content, h.type, h.create_date, m.admin_name FROM netbar_t_discount_history h "
				+ " LEFT JOIN netbar_t_merchant m ON h.merchant_id = m.id AND m.is_valid = 1 " + condition;
		return queryDao.queryMap(sql);
	}

	/**
	 * 优惠信息总数
	 */
	public long findMerchantDiscountHistoriesCount(Integer type, Long netbarId) {
		StringBuffer condition = new StringBuffer();
		condition.append(" WHERE is_valid = 1 ");
		if (type != null) {
			condition.append(" AND type = ").append(type);
		}
		if (netbarId != null) {
			condition.append(" AND netbar_id = ").append(netbarId);
		}

		String sql = "SELECT COUNT(1) FROM netbar_t_discount_history " + condition;
		return ((Number) queryDao.query(sql)).intValue();
	}
}
