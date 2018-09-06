package com.miqtech.master.service.netbar;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.consts.NetbarConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.netbar.NetbarFundDetailDao;
import com.miqtech.master.entity.netbar.NetbarFundDetail;
import com.miqtech.master.utils.ArithUtil;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.vo.PageVO;

/**
 * 网吧资金明细Service
 */
@Component
public class NetbarFundDetailService {

	@Autowired
	private NetbarFundDetailDao netbarFundDetailDao;
	@Autowired
	private QueryDao queryDao;

	public List<NetbarFundDetail> findRefundableBySerNumbers(String serNumbers) {
		return netbarFundDetailDao.findBySerNumbersAndDirectionAndValid(serNumbers,
				NetbarConstant.NETBAR_FUND_DETAIL_DIRECTION_EXPAND, CommonConstant.INT_BOOLEAN_TRUE);
	}

	public NetbarFundDetail findById(Long id) {
		return netbarFundDetailDao.findOne(id);
	}

	/**
	 * 保存
	 */
	public NetbarFundDetail save(NetbarFundDetail detail) {
		return netbarFundDetailDao.save(detail);
	}

	/**
	 * 批量保存
	 */
	public List<NetbarFundDetail> save(List<NetbarFundDetail> details) {
		return (List<NetbarFundDetail>) netbarFundDetailDao.save(details);
	}

	public PageVO page(Map<String, Object> params) {

		String conditonSql = "";
		if (params.containsKey("type")) {
			conditonSql += " and nfd.type in (" + params.get("type") + ") ";
		}
		if (params.containsKey("netbarId")) {
			conditonSql += " and nfd.netbar_id  = " + params.get("netbarId");
		}
		if (params.containsKey("direction")) {
			conditonSql += " and nfd.direction  = " + params.get("direction");
		}
		if (params.containsKey("time")) {
			conditonSql += " and date_format(nfd.create_date,'%Y-%m') = '" + params.get("time") + "'  ";
		}

		if (params.containsKey("beginTime") && params.containsKey("endTime")) {
			conditonSql += " and nfd.create_date >= '" + params.get("beginTime") + "' and nfd.create_date <= '"
					+ params.get("endTime") + "'";
		}

		// 查询总数
		String totalCountSql = " select    count(1) 	from    netbar_fund_detail nfd  	where    nfd.is_valid = 1 and   nfd.ser_numbers is null "
				+ conditonSql;
		int total = 0;
		Number totalSerNumberNull = queryDao.query(totalCountSql);

		// 查询总数
		totalCountSql = " select count(1) from (select    count(1) 	from    netbar_fund_detail nfd  	where    nfd.is_valid = 1 and   nfd.ser_numbers is not  null "
				+ conditonSql + " group by  nfd.ser_numbers ,nfd.direction,nfd.netbar_id,nfd.type) a";
		Number totalSerNumberNotNull = queryDao.query(totalCountSql);

		if (totalSerNumberNull != null) {
			total = total + totalSerNumberNull.intValue();
		}

		if (totalSerNumberNotNull != null) {
			total = total + totalSerNumberNotNull.intValue();
		}

		if (total <= 0) {
			return new PageVO();
		}
		Integer page = (Integer) params.get("page");

		// 分页
		String limit = "";
		Integer pageSize = PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		if (page <= 0) {
			page = 1;
		}

		Integer startRow = (page - 1) * pageSize;
		limit = SqlJoiner.join(" LIMIT ", startRow.toString(), ", ", pageSize.toString());
		String sql = " select * from ( select "
				+ "	    nfd.type ,nfd.direction,nfd.amount,nfd.ser_numbers,nfd.residual ,nfd.settl_accounts ,nfd.quota_ratio ,nfd.status, sum(nob.amount) thirdAmount, sum(nob.redbag_amount) redbagAmount, sum(nob.value_added_amount) valueAddedAmount,nfd.create_date "
				+ "	from    netbar_fund_detail nfd  left join netbar_t_owner_batch nob "
				+ "	on nob.ser_numbers = nfd.ser_numbers and nob.ser_numbers is not null "
				+ "	where    nfd.is_valid = 1 and nfd.ser_numbers is not null   " + conditonSql
				+ " group by nfd.ser_numbers ,nfd.direction,nfd.netbar_id,nfd.type " + "union "
				+ " select 		  nfd.type ,nfd.direction,nfd.amount,nfd.ser_numbers,nfd.residual ,nfd.settl_accounts ,nfd.quota_ratio ,nfd.status, 0 thirdAmount, 0 redbagAmount,0 valueAddedAmount,nfd.create_date "
				+ " 	from    netbar_fund_detail nfd  where    nfd.is_valid = 1  and nfd.ser_numbers is null"
				+ conditonSql + ") aaa order by create_date desc " + limit;

		List<Map<String, Object>> dataList = queryDao.queryMap(sql);
		PageVO vo = new PageVO();
		vo.setCurrentPage(page);
		vo.setList(dataList);
		vo.setTotal(total);
		int isLast = total > page * pageSize ? 1 : 0;
		vo.setIsLast(isLast);
		return vo;
	}

	/**
	 * 查询交易明细
	 */
	public PageVO repage(Map<String, Object> params) {

		String conditonSql = "";
		String conditonElseSql = "";
		String conditonElseSumSql = "";
		if (params.containsKey("netbarId")) {
			conditonSql += " and nfd.netbar_id  = " + params.get("netbarId");
		}
		if (params.containsKey("direction")) {
			conditonSql += " and nfd.direction  = " + params.get("direction");
		}
		if (params.containsKey("oredrTime")) {
			if (NumberUtils.toInt((String) params.get("oredrTime")) == 1) {
				conditonSql += " and date_format(nfd.create_date,'%Y-%m-%d %H:%i:%s') >=DATE_SUB(NOW(), INTERVAL 3 DAY)";
			} else if (NumberUtils.toInt((String) params.get("oredrTime")) == -1) {
				conditonSql += " and date_format(nfd.create_date,'%Y-%m-%d %H:%i:%s') >=DATE_SUB(NOW(), INTERVAL 7 DAY)";
			} else if (NumberUtils.toInt((String) params.get("oredrTime")) == 0) {
				conditonSql += " and nfd.create_date between date_add(now(),interval -1 month) and now()";
			}
		}
		if (params.containsKey("orderType")) {
			if (NumberUtils.toInt((String) params.get("orderType")) != 8
					&& NumberUtils.toInt((String) params.get("orderType")) != 9
					&& NumberUtils.toInt((String) params.get("orderType")) != 10) {
				conditonElseSql += " and find_in_set(" + params.get("orderType") + ",retype)";
				conditonElseSumSql += " and type=" + params.get("orderType");
			} else if (NumberUtils.toInt((String) params.get("orderType")) == 8) {
				conditonElseSql += " and find_in_set(5,retype) and direction=1";
				conditonElseSumSql += " and type=5 and direction=1";
			} else if (NumberUtils.toInt((String) params.get("orderType")) == 9) {
				conditonElseSql += " and find_in_set(5,retype) and direction=0";
				conditonElseSumSql += " and type=5 and direction=0";
			} else if (NumberUtils.toInt((String) params.get("orderType")) == 10) {
				conditonElseSql += " and find_in_set(5,retype) and direction=-1";
				conditonElseSumSql += " and type=5 and direction=-1";
			}
		}
		if (params.containsKey("startTime")) {
			conditonSql += " and date_format(nfd.create_date,'%Y-%m-%d') >='" + params.get("startTime") + "'";
		}
		if (params.containsKey("endTime")) {
			conditonSql += " and date_format(nfd.create_date,'%Y-%m-%d') <='" + params.get("endTime") + "'";
		}

		// 查询总数
		String totalCountSql = " select    count(1) 	from  netbar_fund_detail nfd  where nfd.is_valid = 1 and  nfd.ser_numbers is null AND nfd.type IN (0, 2, 3, 4, 5, 6, 7) "
				+ conditonSql + conditonElseSumSql;
		int total = 0;
		Number totalSerNumberNull = queryDao.query(totalCountSql);

		// 查询总数
		totalCountSql = " select count(1) from (select    count(1),GROUP_CONCAT(DISTINCT nfd.type order by nfd.type) retype,direction from    netbar_fund_detail nfd  	where    nfd.is_valid = 1 and   nfd.ser_numbers is not  null AND nfd.type IN (0, 2, 3, 4, 5, 6, 7)"
				+ conditonSql + " group by  nfd.ser_numbers ,nfd.direction,nfd.netbar_id,nfd.create_date) a where 1=1 "
				+ conditonElseSql;
		Number totalSerNumberNotNull = queryDao.query(totalCountSql);

		if (totalSerNumberNull != null) {
			total = total + totalSerNumberNull.intValue();
		}

		if (totalSerNumberNotNull != null) {
			total = total + totalSerNumberNotNull.intValue();
		}

		if (total <= 0) {
			return new PageVO();
		}
		Integer page = (Integer) params.get("page");

		// 分页
		String limit = "";
		Integer pageSize = PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		if (page <= 0) {
			page = 1;
		}

		Integer startRow = (page - 1) * pageSize;
		limit = SqlJoiner.join(" LIMIT ", startRow.toString(), ", ", pageSize.toString());
		String sql = "SELECT direction,amount,status,create_date,retype,case when FIND_IN_SET(retype,5) then 0 else 1 end as flag,num,orderId,netbar_id  "
				+ "FROM("
				+ "SELECT nfd.direction, sum(nfd.amount) amount,nfd. STATUS,nfd.create_date,GROUP_CONCAT(DISTINCT nfd.type order by nfd.type) retype,count(DISTINCT nfd.type) num,GROUP_CONCAT(id) orderId,nfd.netbar_id FROM netbar_fund_detail nfd "
				+ "WHERE nfd.is_valid = 1 AND nfd.ser_numbers IS NOT NULL AND nfd.type IN (0, 2, 3, 4, 5, 6, 7) "
				+ conditonSql + "  GROUP BY nfd.ser_numbers,nfd.direction,nfd.netbar_id,nfd.create_date" + " UNION "
				+ "SELECT nfd.direction,nfd.amount,nfd. STATUS,nfd.create_date,nfd.type retype,1 num,id orderId,nfd.netbar_id "
				+ "FROM netbar_fund_detail nfd "
				+ "WHERE nfd.is_valid = 1 AND nfd.ser_numbers IS NULL AND nfd.type IN (0, 2, 3, 4, 5, 6, 7) "
				+ conditonSql + " ) aaa where orderId is not null " + conditonElseSql + " ORDER BY aaa.create_date DESC"
				+ limit;
		List<Map<String, Object>> dataList = queryDao.queryMap(sql);
		PageVO vo = new PageVO();
		vo.setCurrentPage(page);
		vo.setList(dataList);
		vo.setTotal(total);
		int isLast = total > page * pageSize ? 1 : 0;
		vo.setIsLast(isLast);
		return vo;
	}

	/**
	 * 查询交易明细前10
	 */
	public List<Map<String, Object>> repage10(Long netbarId) {
		String sql = "SELECT netbar_id,direction,amount,status,create_date,retype,case when FIND_IN_SET(retype,5) then 0 else 1 end as flag,orderId  "
				+ "FROM("
				+ "SELECT nfd.netbar_id,nfd.direction, sum(nfd.amount) amount,nfd. STATUS,nfd.create_date,GROUP_CONCAT(DISTINCT nfd.type order by nfd.type) retype,GROUP_CONCAT(nfd.id) orderId FROM netbar_fund_detail nfd "
				+ "WHERE nfd.is_valid = 1 AND nfd.ser_numbers IS NOT NULL AND nfd.type IN (0, 2, 3, 4, 5, 6, 7) AND nfd.netbar_id ="
				+ netbarId + "  GROUP BY nfd.ser_numbers,nfd.direction,nfd.netbar_id,nfd.create_date "
				+ "UNION SELECT nfd.netbar_id,nfd.direction,nfd.amount amount,nfd. STATUS,nfd.create_date,nfd.type retype,nfd.id orderId "
				+ "FROM netbar_fund_detail nfd "
				+ "WHERE nfd.is_valid = 1 AND nfd.ser_numbers IS NULL AND nfd.type IN (0, 2, 3, 4, 5, 6, 7) AND nfd.netbar_id = "
				+ netbarId + ") aaa  where orderId is not null ORDER BY create_date DESC limit 0,10";
		List<Map<String, Object>> dataList = queryDao.queryMap(sql);
		return dataList;
	}

	public Map<String, Object> totalFundMap(Map<String, Object> params) {
		Map<String, Object> result = Maps.newHashMap();
		String conditonSql = "";
		if (params.containsKey("type")) {
			conditonSql += " and nfd.type in (" + params.get("type") + ") ";
		}
		if (params.containsKey("netbarId")) {
			conditonSql += " and nfd.netbar_id  = " + params.get("netbarId");
		}
		if (params.containsKey("direction")) {
			conditonSql += " and nfd.direction  = " + params.get("direction");
		}
		if (params.containsKey("time")) {
			conditonSql += " and date_format(nfd.create_date,'%Y-%m') = '" + params.get("time") + "'  ";
		}
		if (params.containsKey("beginTime") && params.containsKey("endTime")) {
			conditonSql += " and nfd.create_date >= '" + params.get("beginTime") + "' and nfd.create_date <= '"
					+ params.get("endTime") + "'";
		}
		// 查询总数
		String totalCountSql = " select    sum(nfd.amount) sumAmount,nfd.direction 	from    netbar_fund_detail nfd  	where    is_valid = 1  "
				+ conditonSql + " group by direction,type,ser_numbers ";
		List<Map<String, Object>> dataList = queryDao.queryMap(totalCountSql);
		double totalIn = 0.0;
		double totalOut = 0.0;
		double totalBack = 0.0;
		for (Map<String, Object> map : dataList) {
			int direction = NumberUtils.toInt(map.get("direction").toString());
			if (direction == 1) {
				totalIn = ArithUtil.add(totalIn,
						org.apache.commons.lang3.math.NumberUtils.toDouble(map.get("sumAmount").toString()));
			} else if (direction == -1) {
				totalOut = ArithUtil.add(totalOut,
						org.apache.commons.lang3.math.NumberUtils.toDouble(map.get("sumAmount").toString()));
			} else if (direction == 0) {
				totalBack = ArithUtil.add(totalBack,
						org.apache.commons.lang3.math.NumberUtils.toDouble(map.get("sumAmount").toString()));
			}
		}
		result.put("totalIn", totalIn);
		result.put("totalOut", totalOut);
		result.put("totalBack", totalBack);

		return result;
	}

	public Map<String, Object> totalFundDetailMap(Map<String, Object> params) {
		Map<String, Object> result = Maps.newHashMap();
		String conditonSql = "";
		String conditonElseSql = "";
		if (params.containsKey("netbarId")) {
			conditonSql += " and nfd.netbar_id  = " + params.get("netbarId");
		}
		if (params.containsKey("direction")) {
			conditonSql += " and nfd.direction  = " + params.get("direction");
		}
		if (params.containsKey("oredrTime")) {
			if (NumberUtils.toInt((String) params.get("oredrTime")) == 1) {
				conditonSql += " and date_format(nfd.create_date,'%Y-%m-%d %H:%i:%s') >=DATE_SUB(NOW(), INTERVAL 3 DAY)";
			} else if (NumberUtils.toInt((String) params.get("oredrTime")) == -1) {
				conditonSql += " and date_format(nfd.create_date,'%Y-%m-%d %H:%i:%s') >=DATE_SUB(NOW(), INTERVAL 7 DAY)";
			} else if (NumberUtils.toInt((String) params.get("oredrTime")) == 0) {
				conditonSql += " and nfd.create_date between date_add(now(),interval -1 month) and now()";
			}
		}
		if (params.containsKey("orderType")) {
			if (NumberUtils.toInt((String) params.get("orderType")) != 8
					&& NumberUtils.toInt((String) params.get("orderType")) != 9
					&& NumberUtils.toInt((String) params.get("orderType")) != 10) {
				conditonElseSql += " and find_in_set(" + params.get("orderType") + ",retype)";
			} else if (NumberUtils.toInt((String) params.get("orderType")) == 8) {
				conditonElseSql += " and find_in_set(5,retype) and direction=1";
			} else if (NumberUtils.toInt((String) params.get("orderType")) == 9) {
				conditonElseSql += " and find_in_set(5,retype) and direction=0";
			} else if (NumberUtils.toInt((String) params.get("orderType")) == 10) {
				conditonElseSql += " and find_in_set(5,retype) and direction=-1";
			}
		}
		if (params.containsKey("startTime")) {
			conditonSql += " and date_format(nfd.create_date,'%Y-%m-%d') >='" + params.get("startTime") + "'";
		}
		if (params.containsKey("endTime")) {
			conditonSql += " and date_format(nfd.create_date,'%Y-%m-%d') <='" + params.get("endTime") + "'";
		}

		// 查询总数
		String totalCountSql = "select  sumAmount,direction, retype from (select    sum(nfd.amount) sumAmount,nfd.direction,GROUP_CONCAT(DISTINCT nfd.type order by nfd.type) retype 	from    netbar_fund_detail nfd  	where    is_valid = 1  "
				+ conditonSql + " group by nfd.ser_numbers,nfd.direction,nfd.netbar_id,nfd.create_date) aaa where 1=1 "
				+ conditonElseSql;
		List<Map<String, Object>> dataList = queryDao.queryMap(totalCountSql);
		double totalIn = 0.0;
		double totalOut = 0.0;
		double totalBack = 0.0;
		for (Map<String, Object> map : dataList) {
			int direction = NumberUtils.toInt(map.get("direction").toString());
			if (direction == 1) {
				totalIn = ArithUtil.add(totalIn,
						org.apache.commons.lang3.math.NumberUtils.toDouble(map.get("sumAmount").toString()));
			} else if (direction == -1) {
				totalOut = ArithUtil.add(totalOut,
						org.apache.commons.lang3.math.NumberUtils.toDouble(map.get("sumAmount").toString()));
			} else if (direction == 0) {
				totalBack = ArithUtil.add(totalBack,
						org.apache.commons.lang3.math.NumberUtils.toDouble(map.get("sumAmount").toString()));
			}
		}
		result.put("totalIn", totalIn);
		result.put("totalOut", totalOut);
		result.put("totalBack", totalBack);

		return result;
	}

	public void changStatus(long id, int status) {
		status = status == 1 ? 1 : 2;
		String sql = "update netbar_fund_detail set update_date=NOW(), status=" + status + " where id=" + id;
		queryDao.update(sql);
	}

	public Double queryExpendTotalAmount(Map<String, Object> params) {
		Double totalAmount = NumberUtils.DOUBLE_ZERO;
		String where = " where f.is_valid=1 and f.type =2";
		String joinSql = StringUtils.EMPTY;
		if (MapUtils.isNotEmpty(params)) {
			String netbarId = MapUtils.getString(params, "netbarId", "");
			if (StringUtils.isNotBlank(netbarId)) {
				where = SqlJoiner.join(where, " and f.netbar_id=", netbarId);
			}
			String beginDate = MapUtils.getString(params, "beginDate", "");
			if (StringUtils.isNotBlank(beginDate)) {
				where = SqlJoiner.join(where, " and DATE(f.create_date) >= DATE('", beginDate, "')");
			}
			String endDate = MapUtils.getString(params, "endDate", "");
			if (StringUtils.isNotBlank(endDate)) {
				where = SqlJoiner.join(where, " and DATE(f.create_date) <= DATE('", endDate, "')");
			}

			String name = MapUtils.getString(params, "name", "");

			if (StringUtils.isNotBlank(name)) {
				joinSql = " right join netbar_t_merchant m on m.netbar_id=f.netbar_id and m.bank_username like '%"
						+ name + "%'";
			}
			String telephone = MapUtils.getString(params, "telephone", "");
			if (StringUtils.isNotBlank(telephone)) {
				if (StringUtils.isNotBlank(name)) {
					joinSql = SqlJoiner.join(joinSql, " and m.owner_telephone like '%", telephone, "%'");
				} else {
					joinSql = SqlJoiner.join(
							" right join netbar_t_merchant m on m.netbar_id=f.netbar_id and m.owner_telephone like '%",
							telephone, "%'");
				}
			}

			String areaCode = MapUtils.getString(params, "areaCode", "");
			if (StringUtils.isNotBlank(areaCode) && !"000000".equals(areaCode)) {
				String leftPading = "6";
				if (StringUtils.endsWith(areaCode, "00")) {
					leftPading = "4";
				}
				if (StringUtils.endsWith(areaCode, "0000")) {
					leftPading = "2";
				}
				joinSql = SqlJoiner.join(" right join netbar_t_info n on n.id=f.netbar_id and LEFT(n.area_code,",
						leftPading, ") = LEFT(", areaCode, "," + leftPading + ")");
			}

			String status = MapUtils.getString(params, "status", "");
			if (StringUtils.isNotBlank(status)) {
				where = SqlJoiner.join(where, " and f.status=", status);
			}

		}
		String sql = SqlJoiner.join("select SUM(amount) from netbar_fund_detail f", joinSql, where);
		Number number = queryDao.query(sql);
		if (null != number) {
			totalAmount = number.doubleValue();
		}

		return totalAmount;
	}

	public int queryExpendTotalNetbar(Map<String, Object> params) {
		int totalNetbar = 0;
		String where = " where f.is_valid=1 and f.type =2";
		String joinSql = StringUtils.EMPTY;
		if (MapUtils.isNotEmpty(params)) {
			String netbarId = MapUtils.getString(params, "netbarId", "");
			if (StringUtils.isNotBlank(netbarId)) {
				where = SqlJoiner.join(where, " and f.netbar_id=", netbarId);
			}
			String beginDate = MapUtils.getString(params, "beginDate", "");
			if (StringUtils.isNotBlank(beginDate)) {
				where = SqlJoiner.join(where, " and DATE(f.create_date) >= DATE('", beginDate, "')");
			}
			String endDate = MapUtils.getString(params, "endDate", "");
			if (StringUtils.isNotBlank(endDate)) {
				where = SqlJoiner.join(where, " and DATE(f.create_date) <= DATE('", endDate, "')");
			}

			String name = MapUtils.getString(params, "name", "");

			if (StringUtils.isNotBlank(name)) {
				joinSql = " right join netbar_t_merchant m on m.netbar_id=f.netbar_id and m.bank_username like '%"
						+ name + "%'";
			}
			String telephone = MapUtils.getString(params, "telephone", "");
			if (StringUtils.isNotBlank(telephone)) {
				if (StringUtils.isNotBlank(name)) {
					joinSql = SqlJoiner.join(joinSql, " and m.owner_telephone like '%", telephone, "%'");
				} else {
					joinSql = SqlJoiner.join(
							" right join netbar_t_merchant m on m.netbar_id=f.netbar_id and m.owner_telephone like '%",
							telephone, "%'");
				}
			}

			String areaCode = MapUtils.getString(params, "areaCode", "");
			if (StringUtils.isNotBlank(areaCode) && !"000000".equals(areaCode)) {
				String leftPading = "6";
				if (StringUtils.endsWith(areaCode, "00")) {
					leftPading = "4";
				}
				if (StringUtils.endsWith(areaCode, "0000")) {
					leftPading = "2";
				}
				joinSql = SqlJoiner.join(" right join netbar_t_info n on n.id=f.netbar_id and LEFT(n.area_code,",
						leftPading, ") = LEFT(", areaCode, "," + leftPading + ")");
			}

			String status = MapUtils.getString(params, "status", "");
			if (StringUtils.isNotBlank(status)) {
				where = SqlJoiner.join(where, " and f.status=", status);
			}

		}
		String sql = SqlJoiner.join("select COUNT(DISTINCT netbar_id) from netbar_fund_detail f ", joinSql, where);
		Number number = queryDao.query(sql);
		if (null != number) {
			totalNetbar = number.intValue();
		}

		return totalNetbar;
	}

	public int queryExpendTotalCount(Map<String, Object> params) {
		int totalNetbar = 0;
		String where = " where is_valid=1 and type =2";
		if (MapUtils.isNotEmpty(params)) {
			String netbarId = MapUtils.getString(params, "netbarId", "");
			if (StringUtils.isNotBlank(netbarId)) {
				where = SqlJoiner.join(where, " and netbar_id=", netbarId);
			}
			String beginDate = MapUtils.getString(params, "beginDate", "");
			if (StringUtils.isNotBlank(beginDate)) {
				where = SqlJoiner.join(where, " and DATE(create_date) >= DATE('", beginDate, "')");
			}
			String endDate = MapUtils.getString(params, "endDate", "");
			if (StringUtils.isNotBlank(endDate)) {
				where = SqlJoiner.join(where, " and DATE(create_date) <= DATE('", endDate, "')");
			}

		}
		String sql = SqlJoiner.join("select COUNT(1) from netbar_fund_detail", where);
		Number number = queryDao.query(sql);
		if (null != number) {
			totalNetbar = number.intValue();
		}

		return totalNetbar;
	}

	/*
	 * 后台列表
	 */
	public PageVO pageList(int page, Map<String, Object> paramsIn) {
		page = page < 1 ? 1 : page;
		String sqlLimit = StringUtils.EMPTY;
		String exportField = StringUtils.EMPTY;
		if (!"1".equals(MapUtils.getString(paramsIn, "noLimit", ""))) {
			page = page < 1 ? 1 : page;
			int size = PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
			int start = (page - 1) * size;
			sqlLimit = " limit " + start + ", " + size;
		} else {
			exportField = "";
		}
		String joinM = " left join netbar_t_merchant m on m.is_valid=1 and m.netbar_id=f.netbar_id";
		String joinMTotal = StringUtils.EMPTY;
		String joinN = SqlJoiner.join(" left join netbar_t_info n on n.is_valid=1 and n.id=f.netbar_id");
		String joinNTotal = StringUtils.EMPTY;
		String where = " where f.is_valid=1 and f.type=2";
		String orderBy = " order by f.create_date desc";
		if (null != paramsIn) {
			String name = MapUtils.getString(paramsIn, "name", "");
			if (StringUtils.isNotBlank(name)) {
				joinN = SqlJoiner.join(" right join netbar_t_info n on n.id=f.netbar_id and n.name like '%", name,
						"%'");
				joinNTotal = joinN;

			}
			String telephone = MapUtils.getString(paramsIn, "telephone", "");
			if (StringUtils.isNotBlank(telephone)) {
				joinM = SqlJoiner.join(
						" right join netbar_t_merchant m on m.netbar_id=f.netbar_id and m.owner_telephone like '%",
						telephone, "%'");
				joinMTotal = joinM;
			}

			String areaCode = MapUtils.getString(paramsIn, "areaCode", "");
			if (StringUtils.isNotBlank(areaCode) && !"000000".equals(areaCode)) {
				String leftPading = "6";

				if (StringUtils.endsWith(areaCode, "00")) {
					leftPading = "4";
				}
				if (StringUtils.endsWith(areaCode, "0000")) {
					leftPading = "2";
				}
				if (StringUtils.isNotBlank(name)) {
					joinN = SqlJoiner.join(joinN, " and LEFT(n.area_code,", leftPading, ") = LEFT(", areaCode,
							"," + leftPading + ")");
				} else {
					joinN = SqlJoiner.join(" right join netbar_t_info n on n.id=f.netbar_id and LEFT(n.area_code,",
							leftPading, ") = LEFT(", areaCode, "," + leftPading + ")");
				}
				joinNTotal = joinN;
			}

			String beginDate = MapUtils.getString(paramsIn, "beginDate", "");
			if (StringUtils.isNotBlank(beginDate)) {
				where = SqlJoiner.join(where, " and DATE(f.create_date) >= DATE('", beginDate, "')");
			}
			String endDate = MapUtils.getString(paramsIn, "endDate", "");
			if (StringUtils.isNotBlank(endDate)) {
				where = SqlJoiner.join(where, " and DATE(f.create_date) <= DATE('", endDate, "')");
			}
			String status = MapUtils.getString(paramsIn, "status", "");
			if (StringUtils.isNotBlank(status)) {
				where = SqlJoiner.join(where, " and f.status=", status);
			}
			String netbarId = MapUtils.getString(paramsIn, "netbarId", "");
			if (StringUtils.isNotBlank(netbarId)) {
				where = SqlJoiner.join(where, " and f.netbar_id=", netbarId);
			}
		}

		String sql = SqlJoiner.join(
				"select f.id, f.netbar_id netbarId, f.status,f.create_date createDate,n.name, n.area_code areaCode, f.update_date updateDate, f.amount, m.bank_username username, m.ali_pay_account aliPay, m.bank_name bankName,m.bank_branch_name branchBankName, m.bank_card_id bankCard, m.owner_telephone telephone",
				exportField, " from netbar_fund_detail f", joinN, joinM, where, orderBy, sqlLimit);
		String sqlTotal = SqlJoiner.join("select count(1) from netbar_fund_detail f", joinMTotal, joinNTotal, where);
		Number totalNum = queryDao.query(sqlTotal);
		int total = null == totalNum ? 0 : totalNum.intValue();
		List<Map<String, Object>> list;
		if (total > 0) {
			list = queryDao.queryMap(sql);
		} else {
			list = Lists.newArrayList();
		}

		PageVO pageVO = new PageVO();
		pageVO.setTotal(total);
		pageVO.setList(list);

		return pageVO;
	}

	public Map<String, Object> findBackRedbagInfo(int amount, String tradeNo) {
		String sql = "select sum(amount) totalAmount,sum(amount)/" + amount
				+ " redbagCount from netbar_fund_detail where ser_numbers='" + tradeNo + "'  and direction=0";
		return queryDao.querySingleMap(sql);

	}

	public Map<String, Object> findBackValueAddedCardInfo(int amount, String tradeNo) {
		String sql = "select sum(amount) totalAmount,sum(amount)/" + amount
				+ " valueAddedCardCount from netbar_fund_detail where ser_numbers='" + tradeNo + "'  and direction=0";
		return queryDao.querySingleMap(sql);

	}

	public List<NetbarFundDetail> findByNetbarIdAndValidAndStatusAndDirectionAndType(Long netbarId, int valid,
			int status, int direction, int type) {
		return netbarFundDetailDao.findByNetbarIdAndValidAndStatusAndDirectionAndType(netbarId, valid, status,
				direction, type);

	}

	/**
	 * 根据网吧资金明细,计算网吧资金
	 */
	public Map<String, Object> statisFundByNetbarId(Long netbarId) {
		if (netbarId == null) {
			return null;
		}

		String sql = SqlJoiner.join(
				"SELECT sum( IF (type = 5 or type = 7, amount, 0) * IF (direction = - 1, - 1, 1) ) quota,",
				" sum( IF (type != 5 and type != 7, amount, 0) * IF (direction = - 1, - 1, 1) ) accounts",
				" FROM netbar_fund_detail fd WHERE is_valid = 1 AND ( type != 3 OR (type = 3 AND status = 2) ) AND netbar_id = ",
				netbarId.toString());
		return queryDao.querySingleMap(sql);
	}

	/**
	 * 根据id查询交易明细
	 */
	public List<Map<String, Object>> querySumDetail(String orderIds, Long netbarId) {
		String sql = "SELECT case when type=5 then amount end as prizesum,case when type<>5 then amount end as coinsum "
				+ "FROM(SELECT sum(nfd.amount) amount,type FROM netbar_fund_detail nfd "
				+ "WHERE nfd.is_valid = 1 AND nfd.id IN (" + orderIds
				+ ")  AND nfd.ser_numbers IS NOT NULL AND nfd.type IN (0, 2, 3, 4, 5, 6, 7) AND nfd.netbar_id ="
				+ netbarId
				+ " GROUP BY nfd.ser_numbers,nfd.direction,nfd.netbar_id,nfd.type ORDER BY nfd.create_date DESC) aaa";
		List<Map<String, Object>> dataList = queryDao.queryMap(sql);
		return dataList;
	}

	public List<NetbarFundDetail> findValidBySerNumbers(String serNumbers) {
		if (StringUtils.isBlank(serNumbers)) {
			return null;
		}

		return netbarFundDetailDao.findBySerNumbersAndValid(serNumbers, CommonConstant.INT_BOOLEAN_TRUE);
	}

	public NetbarFundDetail findValidBySerNumbersAndType(String requestSN, int type) {
		return netbarFundDetailDao.findBySerNumbersAndValidAndType(requestSN, CommonConstant.INT_BOOLEAN_TRUE, type);
	}
}