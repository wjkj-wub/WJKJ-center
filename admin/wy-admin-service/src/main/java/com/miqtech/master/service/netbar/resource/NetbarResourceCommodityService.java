package com.miqtech.master.service.netbar.resource;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.consts.NetbarConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.netbar.resource.NetbarResourceCommodityDao;
import com.miqtech.master.entity.netbar.NetbarInfo;
import com.miqtech.master.entity.netbar.resource.NetbarResourceCommodity;
import com.miqtech.master.entity.netbar.resource.NetbarResourceCommodityProperty;
import com.miqtech.master.service.common.ObjectRedisOperateService;
import com.miqtech.master.service.netbar.NetbarInfoService;
import com.miqtech.master.utils.BeanUtils;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.utils.StringUtils;
import com.miqtech.master.vo.PageVO;

@Component
public class NetbarResourceCommodityService {

	private static final String REDIS_KEY_RECOMMAND_COMMODITY = "wy_resource_recommand_commodity";// 对应地区、会员级别的 推荐的商品wy_resource_recommand_commodity_{areaCode}_{levels}

	@Autowired
	private ObjectRedisOperateService objectRedisOperateService;
	@Autowired
	private NetbarResourceCommodityDao netbarResourceCommodityDao;
	@Autowired
	private QueryDao queryDao;
	@Autowired
	private NetbarResourceCommodityPropertyService netbarResourceCommodityPropertyService;
	@Autowired
	private NetbarInfoService netbarInfoService;

	/*
	 * 查对象
	 */
	public NetbarResourceCommodity findById(Long id) {
		return netbarResourceCommodityDao.findOne(id);
	}

	public NetbarResourceCommodity findValidById(Long id) {
		return netbarResourceCommodityDao.findByIdAndValid(id, CommonConstant.INT_BOOLEAN_TRUE);
	}

	/**
	 * 通过项目ID查询资源商城商品
	 */
	public List<Map<String, Object>> queryByPropertyIds(List<Long> ids) {
		Joiner emptyJoiner = Joiner.on("");
		String idStrs = StringUtils.EMPTY;
		if (CollectionUtils.isNotEmpty(ids)) {
			for (Long id : ids) {
				if (idStrs.length() > 0) {
					idStrs = emptyJoiner.join(idStrs, ",");
				}
				idStrs = emptyJoiner.join(ids, id.toString());
			}
		}
		return queryByPropertyIds(idStrs);
	}

	/**
	 * 通过项目ID查询资源商城商品
	 */
	public List<Map<String, Object>> queryByPropertyIds(String ids) {
		if (StringUtils.isNotBlank(ids)) {
			String sql = "SELECT nrc.*, nrcp.id propertyId FROM netbar_resource_commodity nrc JOIN netbar_resource_commodity_property nrcp ON nrc.id = nrcp.commodity_id WHERE nrcp.id IN ('"
					+ ids + "')";
			return queryDao.queryMap(sql);
		}
		return null;
	}

	/**
	 * 查询资源商品，并附带商品下的项目
	 */
	public NetbarResourceCommodity findValidByIdWithProperites(Long id) {
		NetbarResourceCommodity commodity = findValidById(id);
		if (commodity != null) {
			List<NetbarResourceCommodityProperty> properties = netbarResourceCommodityPropertyService
					.findValidByCommodityIdAndStatus(commodity.getId(), 2);
			commodity.setProperties(properties);
		}
		return commodity;
	}

	/*
	 * 查在售商品id and name
	 */
	public List<Map<String, Object>> getidsAndNames() {
		String sql = SqlJoiner.join("SELECT c.id, c.name, c.name pname, p.id propertyId, p.name propertyName",
				" FROM netbar_resource_commodity c",
				" JOIN netbar_resource_commodity_property p ON c.id = p.commodity_id AND p.is_valid = 1 AND p.status = 2",
				" WHERE c.is_valid = 1 GROUP BY c.id");
		List<Map<String, Object>> list = queryDao.queryMap(sql);
		return list;
	}

	/*
	 * 查置顶商品列表-最多5个
	 */
	public List<Map<String, Object>> getTopCommodity(String areaCode) {
		areaCode = StringUtils.substring(areaCode, 0, 2) + "0000";
		String sql = StringUtils.EMPTY;
		String limitNum = "5";
		if (areaCode == null || areaCode.equals("000000")) {
			areaCode = "000000";
			limitNum = "2";
		}
		sql = SqlJoiner.join(
				"select p.id ,p.is_top no, p.name pname from netbar_resource_commodity p  where p.is_valid=1 and p.province='",
				areaCode, "' and p.is_top > 0 order by no limit ", limitNum);
		List<Map<String, Object>> list = queryDao.queryMap(sql);
		return list;
	}

	/*
	 * 改置顶
	 */
	public void changetop(long id, int topNum) {
		String sql = "update netbar_resource_commodity set is_top=" + topNum + " where id=" + id;
		queryDao.update(sql);
	}

	/*
	 * 修改置顶顺序
	 */
	public void changeTopByIdAndNo(Long id, int no) {
		String sql = "update netbar_resource_commodity set is_top=" + no + " where id=" + id;
		queryDao.update(sql);
	}

	/*
	 * 查置顶顺序
	 */
	public String queryTopNos(String areaCode) {
		String nos = StringUtils.EMPTY;
		String sql = "select GROUP_CONCAT(is_top) nos from netbar_resource_commodity where is_valid=1 and province = '"
				+ areaCode + "' and is_top>0 order by is_top limit 5";
		Map<String, Object> map = queryDao.querySingleMap(sql);
		if (MapUtils.isNotEmpty(map)) {
			Object object = map.get("nos");
			nos = null == object ? "" : object.toString();
		}
		return nos;
	}

	/*
	 * 保存/更新对象
	 */
	public NetbarResourceCommodity save(NetbarResourceCommodity netbarResourceCommodity) {
		if (null != netbarResourceCommodity) {
			Date now = new Date();
			if (null != netbarResourceCommodity.getId()) {
				netbarResourceCommodity.setUpdateDate(now);
				NetbarResourceCommodity old = findById(netbarResourceCommodity.getId());
				if (null != old) {
					netbarResourceCommodity = BeanUtils.updateBean(old, netbarResourceCommodity);
				}
			} else {
				netbarResourceCommodity.setValid(CommonConstant.INT_BOOLEAN_TRUE);
				netbarResourceCommodity.setCreateDate(now);
			}
			return netbarResourceCommodityDao.save(netbarResourceCommodity);
		}
		return null;
	}

	/*
	 * 后台管理列表--商品
	 */
	public PageVO pageListWyadmin(int page, Map<String, Object> paramsIn) {
		String sqlLimit = StringUtils.EMPTY;
		String field = "select c.id commodityId, c.name, c.is_top isTop, c.use_quo_ratio useQuoRatio, t1.name typeName, t2.name typeNameP, a.name areaName, c.qualifications";
		if (!"1".equals(MapUtils.getString(paramsIn, "noLimit", ""))) {
			page = page < 1 ? 1 : page;
			int size = PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
			int start = (page - 1) * size;
			sqlLimit = " limit " + start + ", " + size;
		} else {
			field = SqlJoiner.join(field, ", c.executes, c.execute_phone");
		}
		String joinCTotal = StringUtils.EMPTY;
		String joinT1 = " left join netbar_commodity_category t1 on t1.id=c.category_id";
		String joinT1Total = StringUtils.EMPTY;
		String joinT2 = " left join netbar_commodity_category t2 on t2.id=t1.pid";
		String where = " where c.is_valid=1";
		String groupBy = StringUtils.EMPTY;
		String distinct = "1";
		String orderBy = " order by c.create_date desc";
		if (null != paramsIn) {
			String name = MapUtils.getString(paramsIn, "name", "");
			if (StringUtils.isNotBlank(name)) {
				where = SqlJoiner.join(where, " and c.name like '%", name, "%'");
			}
			String areaCode = MapUtils.getString(paramsIn, "areaCode", "");
			if (StringUtils.isNotBlank(areaCode)) {
				Boolean isActivityAdmin = MapUtils.getBoolean(paramsIn, "isActivityAdmin");
				if (isActivityAdmin) {
					where = SqlJoiner.join(where, " and (left(c.province,2) = '", areaCode.substring(0, 2),
							"' OR c.province = '000000')");
				} else {
					where = SqlJoiner.join(where, " and left(c.province,2) = '", areaCode.substring(0, 2), "'");
				}
			}
			String categoryId = MapUtils.getString(paramsIn, "categoryId", "");
			if (StringUtils.isNotBlank(categoryId)) {
				where = SqlJoiner.join(where, "  and c.category_id = ", categoryId);
			}
			String qualifications = MapUtils.getString(paramsIn, "qualifications", "");
			if (StringUtils.isNotBlank(qualifications)) {
				where = SqlJoiner.join(where, "  and c.qualifications = ", qualifications);
			}
			String comTag = MapUtils.getString(paramsIn, "comTag", "");
			if (StringUtils.isNotBlank(comTag)) {
				where = SqlJoiner.join(where, "  and c.com_tag = ", comTag);
			}

			String categoryPid = MapUtils.getString(paramsIn, "categoryPid", "");
			if (StringUtils.isNotBlank(categoryPid)) {
				joinT1 = SqlJoiner.join(" right join netbar_commodity_category t1 on t1.id=c.category_id and t1.pid=",
						categoryPid);
			}

			String beginDate = MapUtils.getString(paramsIn, "beginDate", "");
			if (StringUtils.isNotBlank(beginDate)) {
				where = SqlJoiner.join(where, " and DATE(c.create_date) >= DATE('", beginDate, "')");
			}
			String endDate = MapUtils.getString(paramsIn, "endDate", "");
			if (StringUtils.isNotBlank(endDate)) {
				where = SqlJoiner.join(where, " and DATE(c.create_date) <= DATE('", endDate, "')");
			}

		}

		String sql = SqlJoiner.join(field, " from netbar_resource_commodity c", joinT1, joinT2,
				" left join sys_t_area a on a.area_code=c.province", where, groupBy, orderBy, sqlLimit);
		String sqlTotal = SqlJoiner.join("select count(", distinct, ") from netbar_resource_commodity c", joinCTotal,
				joinT1Total, where);
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

	/*
	 * 商户端商品列表
	 */
	public PageVO pageList(int page, Map<String, Object> paramsIn) {
		String sqlLimit = StringUtils.EMPTY;
		String field = "select p.commodity_id commodityId, p.id, p.property_no propertyNo, c.name, p.name propertyName, c.is_top isTop, p.price, c.use_quo_ratio useQuoRatio, p.measure, t1.name typeName, t2.name typeNameP, a.name areaName, c.qualifications, p.inventory_total inventoryTotal, p.inventory, p.status";
		if (!"1".equals(MapUtils.getString(paramsIn, "noLimit", ""))) {
			page = page < 1 ? 1 : page;
			int size = PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
			int start = (page - 1) * size;
			sqlLimit = " limit " + start + ", " + size;
		} else {
			field = SqlJoiner.join(field, ", c.executes, c.execute_phone");
		}
		String joinC = SqlJoiner.join(" left join netbar_resource_commodity c on c.id=p.commodity_id");
		String joinCTotal = StringUtils.EMPTY;
		String joinT1 = " left join netbar_commodity_category t1 on t1.id=c.category_id";
		String joinT1Total = StringUtils.EMPTY;
		String joinT2 = " left join netbar_commodity_category t2 on t2.id=t1.pid";
		String where = " where p.is_valid=1";
		String groupBy = StringUtils.EMPTY;
		String distinct = "1";
		String orderBy = " order by p.create_date desc";
		if (StringUtils.isNotBlank(MapUtils.getString(paramsIn, "merchant", ""))) {
			field = "select p.commodity_id commodityId, p.fake_sold_num soldNum, p.id, c.name, p.name propertyName, c.is_top isTop, if(ifnull(c.is_top,0)<1,999,c.is_top) orderTop, c.url, p.price, ifnull(p.vip_ratio,1) vipRatio, ifnull(p.gold_rebate,1) goldRebate, c.use_quo_ratio useQuoRatio, p.measure, a.name areaName, c.qualifications, p.inventory_total inventoryTotal, p.inventory, c.com_tag comTag";
			orderBy = " order by orderTop, p.create_date desc";
			where = SqlJoiner.join(where,
					" and p.id in (select min(id) from netbar_resource_commodity_property where p.is_valid=1");
			String status = MapUtils.getString(paramsIn, "status", "");
			if (StringUtils.isNotBlank(status)) {
				where = SqlJoiner.join(where, " and p.status=", status);
			}
			where = SqlJoiner.join(where, " group by commodity_id)");
			distinct = "DISTINCT p.commodity_id";
		}
		if (null != paramsIn) {
			String name = MapUtils.getString(paramsIn, "name", "");
			if (StringUtils.isNotBlank(name)) {
				joinC = SqlJoiner.join(
						" right join netbar_resource_commodity c on c.id=p.commodity_id and c.name like '%", name,
						"%' and c.is_valid = 1");
				joinCTotal = joinC;
			}
			String areaCode = MapUtils.getString(paramsIn, "areaCode", "");
			if (StringUtils.isNotBlank(areaCode)) {
				Boolean isActivityAdmin = MapUtils.getBoolean(paramsIn, "isActivityAdmin");
				String condition = StringUtils.EMPTY;
				if (isActivityAdmin) {
					condition = "(left(c.province,2) = '" + areaCode.substring(0, 2) + "' OR c.province = '000000')";
				} else {
					condition = "left(c.province,2) = '" + areaCode.substring(0, 2) + "'";
				}
				if (StringUtils.isNotBlank(name)) {
					joinC = SqlJoiner.join(joinC, " and ", condition);
				} else {
					joinC = SqlJoiner.join(
							" right join netbar_resource_commodity c on c.id=p.commodity_id and c.is_valid = 1 and ",
							condition);
				}
				joinCTotal = joinC;
			}
			String categoryId = MapUtils.getString(paramsIn, "categoryId", "");
			if (StringUtils.isNotBlank(categoryId)) {
				if (StringUtils.isAllBlank(name, areaCode)) {
					joinC = SqlJoiner.join(
							" right join netbar_resource_commodity c on c.id=p.commodity_id and c.is_valid = 1 and c.category_id = ",
							categoryId);
				} else {
					joinC = SqlJoiner.join(joinC, " and c.category_id=", categoryId);
				}
				joinCTotal = joinC;
			}
			String qualifications = MapUtils.getString(paramsIn, "qualifications", "");
			if (StringUtils.isNotBlank(qualifications)) {
				if (StringUtils.isAllBlank(name, areaCode, categoryId)) {
					joinC = SqlJoiner.join(
							" right join netbar_resource_commodity c on c.id=p.commodity_id and c.is_valid = 1 and c.qualifications = ",
							qualifications);
				} else {
					joinC = SqlJoiner.join(joinC, " and c.qualifications=", qualifications);
				}
				joinCTotal = joinC;
			}
			String comTag = MapUtils.getString(paramsIn, "comTag", "");
			if (StringUtils.isNotBlank(comTag)) {
				if (StringUtils.isAllBlank(name, areaCode, categoryId, qualifications)) {
					joinC = SqlJoiner.join(
							" right join netbar_resource_commodity c on c.id=p.commodity_id and c.is_valid = 1 and c.com_tag = ",
							comTag);
				} else {
					joinC = SqlJoiner.join(joinC, " and c.com_tag=", comTag);
				}
				joinCTotal = joinC;
			}

			String categoryPid = MapUtils.getString(paramsIn, "categoryPid", "");
			if (StringUtils.isNotBlank(categoryPid)) {
				joinT1 = SqlJoiner.join(" right join netbar_commodity_category t1 on t1.id=c.category_id and t1.pid=",
						categoryPid);
				if (StringUtils.isNotBlank(joinCTotal)) {
					joinT1Total = joinT1;
				} else {
					joinT1Total = SqlJoiner.join(
							" left join netbar_resource_commodity c on c.is_valid = 1 and c.id=p.commodity_id", joinT1);
				}
			}

			String beginDate = MapUtils.getString(paramsIn, "beginDate", "");
			if (StringUtils.isNotBlank(beginDate)) {
				where = SqlJoiner.join(where, " and DATE(p.create_date) >= DATE('", beginDate, "')");
			}
			String endDate = MapUtils.getString(paramsIn, "endDate", "");
			if (StringUtils.isNotBlank(endDate)) {
				where = SqlJoiner.join(where, " and DATE(p.create_date) <= DATE('", endDate, "')");
			}

			int order = NumberUtils.toInt(MapUtils.getString(paramsIn, "order", ""), -1);
			if (order == 4) { //按销量
				orderBy = " order by (p.inventory_total-p.inventory) desc";
			} else if (order == 1) { //按价格，高-低
				orderBy = " order by p.price desc";
			} else if (order == 3) { //按价格，低-高
				orderBy = " order by p.price";
			}
		}

		String sql = SqlJoiner.join(field, " from netbar_resource_commodity_property p", joinC, joinT1, joinT2,
				" left join sys_t_area a on a.area_code=c.province", where, groupBy, orderBy, sqlLimit);
		String sqlTotal = SqlJoiner.join("select count(", distinct, ") from netbar_resource_commodity_property p",
				joinCTotal, joinT1Total, where);
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

	/**
	 * 推荐商品(销售量最多且余量足够的商品)
	 */
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> recommand(Long netbarId, Integer num) {
		if (num == null || num <= 0) {
			num = 3;
		}

		// 根据网吧匹配地区及会员级别
		NetbarInfo netbar = netbarInfoService.findById(netbarId);
		String areaCode = netbar.getAreaCode();
		if (StringUtils.isBlank(areaCode)) {
			areaCode = "000000";
		}
		if (areaCode.length() > 2) {
			areaCode = areaCode.substring(0, 2) + "0000";
		}
		Integer levels = netbar.getLevels();
		if (levels == null) {
			levels = NetbarConstant.NETBAR_LEVELS_NOTMEMBER;
		}

		// 根据地区及会员级别推荐商品,2天换一批
		Joiner joiner = Joiner.on("_");
		String key = joiner.join(REDIS_KEY_RECOMMAND_COMMODITY, areaCode, levels);
		List<Map<String, Object>> recommands = (List<Map<String, Object>>) objectRedisOperateService.getData(key);
		if (CollectionUtils.isEmpty(recommands)) {
			String sql = SqlJoiner.join(
					"SELECT * FROM ( SELECT nrc.id, nrc.name, nrcp.name propertyName, nrc.url, nrcp.price, nrcp.rebate, count(nro.id) + (case when nrcp.fake_sold_num is null then 0 else nrcp.fake_sold_num end ) count",
					" FROM netbar_resource_commodity nrc",
					" JOIN netbar_resource_commodity_property nrcp ON nrc.id = nrcp.commodity_id",
					" LEFT JOIN netbar_resource_order nro ON nrc.id = nro.commodity_id AND nrcp.id = nro.property_id AND nro.is_valid = 1",
					" WHERE ( nrc.qualifications = 0 OR nrc.qualifications = ", levels.toString(), " )",
					" AND ( nrc.province = '000000' OR LEFT (nrc.province, 2) = LEFT ('", areaCode, "', 2) )",
					" AND nrcp.status = 2 AND nrc.is_recommend = 1",
					" GROUP BY nrc.id ORDER BY nrcp.id ) t ORDER BY rand() LIMIT ", num.toString());
			recommands = queryDao.queryMap(sql);
			objectRedisOperateService.setData(key, recommands);
			objectRedisOperateService.expire(key, 2, TimeUnit.DAYS);
		}

		return recommands;
	}

	/**
	 * 查询订单地区及销售数量
	 */
	public Map<String, Object> queryAreaAndSalesById(Long id) {
		if (id == null) {
			return null;
		}

		String sql = SqlJoiner.join("SELECT nrc.id, nrc.name, nrc.province, sa.name provinceName,",
				" SUM(nro.buy_num)+ (case when nrcp.fake_sold_num is null then 0 else nrcp.fake_sold_num end ) count",
				" FROM netbar_resource_commodity_property nrcp",
				" LEFT JOIN netbar_resource_commodity nrc ON nrcp.commodity_id = nrc.id",
				" LEFT JOIN sys_t_area sa ON nrc.province = sa.area_code",
				" LEFT JOIN netbar_resource_order nro ON nrcp.id = nro.property_id AND nro.is_valid = 1 AND nro.status != ",
				NetbarConstant.NETBAR_RESOURCE_ORDER_STATUS_CANCEL.toString(), " WHERE nrcp.id = ", id.toString(),
				" GROUP BY nrc.id");
		return queryDao.querySingleMap(sql);
	}

	public Map<String, Object> toConfirmUp(String id) {
		String sql = "select a.name,c.name area,a.qualifications,group_concat(b.name) as sub_name,group_concat(b.id) as sub_id,group_concat(b.status) as sub_status,a.description from (netbar_resource_commodity a,netbar_resource_commodity_property b) left join sys_t_area c on a.province = c.area_code where a.id="
				+ id + " and b.commodity_id=a.id";
		return queryDao.querySingleMap(sql);
	}

	public void confirmUp(String propertyIds) {
		String sql = "update netbar_resource_commodity_property set status=if(status=2, 2, 3) where id in ("
				+ propertyIds + ")";
		queryDao.update(sql);
	}

	public Map<String, Object> toConfirmC(String id) {
		String sql = "select c.status,c.id,b.name,a.name sub_name,a.settl_date,b.executes,b.execute_phone,b.description from netbar_resource_commodity_property a,netbar_resource_commodity b,netbar_resource_order c where c.id="
				+ id + " and a.commodity_id=b.id and c.property_id=a.id";
		return queryDao.querySingleMap(sql);
	}

	public void confirmC(String id) {
		String sql = "update netbar_resource_order set status=if(status=1,3,2) where id=" + id;
		queryDao.update(sql);
	}

	public Map<String, Object> toConfirmB(String id) {
		String sql = "select c.id,c.status,b.name,a.name sub_name,a.settl_date,b.description,d.name netbar_name,d.telephone,d.address,d.longitude,d.latitude from netbar_resource_commodity_property a,netbar_resource_commodity b,netbar_resource_order c,netbar_t_info d where a.commodity_id=b.id and c.id="
				+ id + " and c.netbar_id=d.id and c.commodity_id=b.id and c.property_id=a.id";
		return queryDao.querySingleMap(sql);
	}

	public void confirmB(String id) {
		String sql = "update netbar_resource_order set status=if(status=2,3,1) where id=" + id;
		queryDao.update(sql);
	}

	public int topStatus(int oper, String ids, String userAreaCode, int numLimit) {

		boolean isTopSame = queryIsTopSameByIds(ids);
		if (!isTopSame) {
			return -1;
		}
		boolean isProvinceSame = queryIsProvinceSameByIds(ids);
		if (!isProvinceSame) {
			return -2;
		}
		if (oper == 1) {
			int toTopNum = ids.split(",").length;
			if (isOverNumLimit(userAreaCode, numLimit, toTopNum)) {
				return -3;
			}
		}

		return 0;
	}

	/*
	 * 根据商品id判断其置顶状态是否一致
	 */
	private boolean queryIsTopSameByIds(String ids) {
		boolean result = true;
		String sql = "select COUNT(DISTINCT IF(is_top>0,1,0)) num from netbar_resource_commodity where id in(" + ids
				+ ")";
		Number number = queryDao.query(sql);
		if (null != number && number.intValue() > 1) {
			result = false;
		}
		return result;
	}

	/*
	 * 根据商品id判断其省份是否一致
	 */
	private boolean queryIsProvinceSameByIds(String ids) {
		boolean result = true;
		String sql = "select COUNT(DISTINCT IF(province>0,1,0)) num from netbar_resource_commodity where id in(" + ids
				+ ")";
		Number number = queryDao.query(sql);
		if (null != number && number.intValue() > 1) {
			result = false;
		}
		return result;
	}

	/*
	 * 判断地区是否已经超出置顶条数限制
	 */
	private boolean isOverNumLimit(String areaCode, int limitNum, int toTopNum) {
		boolean result = false;
		String sql = "select COUNT(1) num from netbar_resource_commodity where is_valid =1 and province='" + areaCode
				+ "' and is_top>0";
		Number number = queryDao.query(sql);
		if (null != number && (number.intValue() + toTopNum) > limitNum) {
			result = true;
		}
		return result;
	}

}