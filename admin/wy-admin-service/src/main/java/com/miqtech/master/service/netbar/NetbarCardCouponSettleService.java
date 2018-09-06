package com.miqtech.master.service.netbar;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.miqtech.master.consts.NetbarConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.netbar.NetbarCardCouponSettleDao;
import com.miqtech.master.entity.netbar.NetbarCardCoupon;
import com.miqtech.master.entity.netbar.NetbarCardCouponSettle;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.utils.StringUtils;
import com.miqtech.master.vo.PageVO;

@Component
public class NetbarCardCouponSettleService {

	@Autowired
	private QueryDao queryDao;
	@Autowired
	private NetbarCardCouponService netbarCardCouponService;
	@Autowired
	private NetbarCardCouponSettleDao netbarCardCouponSettleDao;

	public List<NetbarCardCouponSettle> findByIds(List<Long> ids) {
		if (CollectionUtils.isNotEmpty(ids)) {
			return netbarCardCouponSettleDao.findByIdIn(ids);
		}
		return null;
	}

	public NetbarCardCouponSettle save(NetbarCardCouponSettle settle) {
		return netbarCardCouponSettleDao.save(settle);

	}

	public List<NetbarCardCouponSettle> save(List<NetbarCardCouponSettle> settles) {
		return (List<NetbarCardCouponSettle>) netbarCardCouponSettleDao.save(settles);
	}

	/**
	 * 分页
	 */
	public PageVO page(int page, Map<String, Object> searchParams) {
		String netbarCondition = " nccs.netbar_id = n.id";
		String netbarTotalCondition = netbarCondition;
		String sqlCondition = " nccs.is_valid = 1";
		String sqlTotalCondition = sqlCondition;
		Map<String, Object> params = Maps.newHashMap();

		if (MapUtils.isNotEmpty(searchParams)) {
			String netbarName = MapUtils.getString(searchParams, "netbarName");
			if (StringUtils.isNotBlank(netbarName)) {
				String likeNetbarName = "%" + netbarName + "%";
				params.put("netbarName", likeNetbarName);
				netbarCondition = SqlJoiner.join(netbarCondition, " AND n.name LIKE :netbarName");
				netbarTotalCondition = SqlJoiner.join(netbarTotalCondition, " AND n.name LIKE '", likeNetbarName, "'");
			}
			String areaCode = MapUtils.getString(searchParams, "areaCode");
			if (StringUtils.isNotBlank(areaCode)) {
				params.put("areaCode", areaCode);
				netbarCondition = SqlJoiner.join(netbarCondition, " AND LEFT(n.area_code, 2) = LEFT(:areaCode, 2)");
				netbarTotalCondition = SqlJoiner.join(netbarTotalCondition, " AND LEFT(n.area_code, 2) = LEFT('",
						areaCode, "', 2)");
			}
			String beginDate = MapUtils.getString(searchParams, "beginDate");
			if (StringUtils.isNotBlank(beginDate)) {
				params.put("beginDate", beginDate);
				sqlCondition = SqlJoiner.join(sqlCondition, " AND nccs.create_date > :beginDate");
				sqlTotalCondition = SqlJoiner.join(sqlTotalCondition, " AND nccs.create_date > '", beginDate, "'");
			}
			String endDate = MapUtils.getString(searchParams, "endDate");
			if (StringUtils.isNotBlank(endDate)) {
				params.put("endDate", endDate);
				sqlCondition = SqlJoiner.join(sqlCondition,
						" AND nccs.create_date <= ADDDATE(:endDate, INTERVAL 1 DAY)");
				sqlTotalCondition = SqlJoiner.join(sqlTotalCondition, " AND nccs.create_date <= ADDDATE('", endDate,
						"', INTERVAL 1 DAY)");
			}
			String status = MapUtils.getString(searchParams, "status");
			if (NumberUtils.isNumber(status)) {
				sqlCondition = SqlJoiner.join(sqlCondition, " AND nccs.status = ", status);
				sqlTotalCondition = SqlJoiner.join(sqlTotalCondition, " AND nccs.status = ", status);
			}
		}

		PageVO vo = new PageVO();
		vo.setCurrentPage(page);

		String totalSql = SqlJoiner.join("SELECT COUNT(1) FROM netbar_card_coupon_settle nccs JOIN netbar_t_info n ON",
				netbarTotalCondition, " WHERE", sqlTotalCondition);
		Number total = queryDao.query(totalSql);
		if (total == null) {
			total = 0;
		}
		vo.setTotal(total.longValue());

		if (total.longValue() > 0) {
			String limit = PageUtils.getLimitSql(page);
			String sql = SqlJoiner
					.join("SELECT nccs.id, nccs.netbar_id netbarId, n.name netbarName, nccs.amount, nccs.num, nccs.create_date createDate,",
							" nccs.status FROM netbar_card_coupon_settle nccs JOIN netbar_t_info n ON",
							netbarCondition, " WHERE", sqlCondition, limit);
			List<Map<String, Object>> list = queryDao.queryMap(sql, params);
			vo.setList(list);
		}
		vo.setIsLast(PageUtils.isBottom(page, total.longValue()));

		return vo;
	}

	/**
	 * 统计分页页面的总数与总金额
	 */
	public Map<String, Object> statisPage(Map<String, Object> searchParams) {
		String netbarCondition = " nccs.netbar_id = n.id";
		String sqlCondition = " nccs.is_valid = 1";
		Map<String, Object> params = Maps.newHashMap();

		if (MapUtils.isNotEmpty(searchParams)) {
			String netbarName = MapUtils.getString(searchParams, "netbarName");
			if (StringUtils.isNotBlank(netbarName)) {
				String likeNetbarName = "%" + netbarName + "%";
				params.put("netbarName", likeNetbarName);
				netbarCondition = SqlJoiner.join(netbarCondition, " AND n.name LIKE :netbarName");
			}
			String areaCode = MapUtils.getString(searchParams, "areaCode");
			if (StringUtils.isNotBlank(areaCode)) {
				params.put("areaCode", areaCode);
				netbarCondition = SqlJoiner.join(netbarCondition, " AND LEFT(n.area_code, 2) = LEFT(:areaCode, 2)");
			}
			String beginDate = MapUtils.getString(searchParams, "beginDate");
			if (StringUtils.isNotBlank(beginDate)) {
				params.put("beginDate", beginDate);
				sqlCondition = SqlJoiner.join(sqlCondition, " AND nccs.create_date > :beginDate");
			}
			String endDate = MapUtils.getString(searchParams, "endDate");
			if (StringUtils.isNotBlank(endDate)) {
				params.put("endDate", endDate);
				sqlCondition = SqlJoiner.join(sqlCondition,
						" AND nccs.create_date <= ADDDATE(:endDate, INTERVAL 1 DAY)");
			}
			String status = MapUtils.getString(searchParams, "status");
			if (NumberUtils.isNumber(status)) {
				sqlCondition = SqlJoiner.join(sqlCondition, " AND nccs.status = ", status);
			}
		}

		String sql = SqlJoiner
				.join("SELECT count(1) count, sum(amount) amount, sum(num) num FROM netbar_card_coupon_settle nccs JOIN netbar_t_info n ON",
						netbarCondition, " WHERE", sqlCondition);
		return queryDao.querySingleMap(sql, params);
	}

	/**
	 * 更改订单状态为结款
	 */
	public void settle(String ids) {
		if (StringUtils.isNotBlank(ids)) {
			// 解析ID字符串
			String[] idsSplit = ids.split(",");
			List<Long> idsLong = Lists.newArrayList();
			for (String idStr : idsSplit) {
				if (NumberUtils.isNumber(idStr)) {
					idsLong.add(NumberUtils.toLong(idStr));
				}
			}

			// 查询结款申请单
			List<NetbarCardCouponSettle> settles = findByIds(idsLong);
			if (CollectionUtils.isEmpty(settles)) {
				return;
			}
			List<NetbarCardCouponSettle> operateSettles = Lists.newArrayList();
			List<Long> settleIds = Lists.newArrayList();
			for (NetbarCardCouponSettle s : settles) {
				if (NetbarConstant.NETBAR_CARD_COUPON_SETTLE_STATUS_UNSETTLE.equals(s.getStatus())) {
					s.setStatus(NetbarConstant.NETBAR_CARD_COUPON_SETTLE_STATUS_SETTLED);
					operateSettles.add(s);
					settleIds.add(s.getId());
				}
			}

			// 更改子订单状态
			List<NetbarCardCoupon> cards = netbarCardCouponService.findByCouponSettleIds(settleIds);
			if (CollectionUtils.isNotEmpty(cards)) {
				for (NetbarCardCoupon c : cards) {
					c.setStatus(NetbarConstant.NETBAR_CARD_COUPON_STATUS_SETTLED);
				}
				netbarCardCouponService.save(cards);
			}

			// 修改申请单状态
			save(operateSettles);
		}
	}

}
