package com.miqtech.master.service.netbar.resource;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.miqtech.master.config.SystemConfig;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.netbar.resource.NetbarResourceCommodityPropertyDao;
import com.miqtech.master.entity.netbar.resource.NetbarResourceCommodity;
import com.miqtech.master.entity.netbar.resource.NetbarResourceCommodityProperty;
import com.miqtech.master.thirdparty.util.SMSMessageUtil;
import com.miqtech.master.thirdparty.util.ShortUrlUtils;
import com.miqtech.master.utils.BeanUtils;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.utils.StringUtils;
import com.miqtech.master.vo.PageVO;

@Component
public class NetbarResourceCommodityPropertyService {

	private final static Logger LOGGER = LoggerFactory.getLogger(NetbarResourceCommodityPropertyService.class);
	private final static String URL_SERVER_CONFIRM = "netbar/resource/toConfirmUp?commodityId=";

	@Autowired
	private SystemConfig systemConfig;
	@Autowired
	private NetbarResourceCommodityService netbarResourceCommodityService;
	@Autowired
	private NetbarResourceCommodityPropertyDao netbarResourceCommodityPropertyDao;
	@Autowired
	private QueryDao queryDao;

	/*
	 * 查对象
	 */
	public NetbarResourceCommodityProperty findById(Long id) {
		return netbarResourceCommodityPropertyDao.findOne(id);
	}

	/**
	 * 根据多个ID查询
	 */
	public List<NetbarResourceCommodityProperty> findByIds(List<Long> ids) {
		return netbarResourceCommodityPropertyDao.findByIdIn(ids);
	}

	/*
	 * 根据商品id判断其状态是否一致
	 */
	public boolean queryStatusSameByIds(String ids) {
		boolean result = true;
		String sql = "select COUNT(DISTINCT status) num from netbar_resource_commodity_property where id in(" + ids
				+ ")";
		Number number = queryDao.query(sql);
		if (null != number && number.intValue() > 1) {
			result = false;
		}
		return result;
	}

	/*
	 * 根据cids查pids
	 */
	public String queryPidsBYCids(String cids) {
		String pids = StringUtils.EMPTY;
		String sql = "select GROUP_CONCAT(DISTINCT id) nos from netbar_resource_commodity_property where commodity_id in("
				+ cids + ")";
		Map<String, Object> map = queryDao.querySingleMap(sql);
		if (MapUtils.isNotEmpty(map)) {
			Object object = map.get("nos");
			pids = null == object ? "" : object.toString();
		}
		return pids;
	}

	/**
	 * 通过id查找
	 */
	public List<Map<String, Object>> queryByIds(String ids) {
		String sql = "select * from netbar_resource_commodity_property where commodity_id in(" + ids + ")";
		return queryDao.queryMap(sql);
	}

	/**
	 * 根据ID查询有效的资源商品项目
	 */
	public NetbarResourceCommodityProperty findValidById(Long id) {
		return netbarResourceCommodityPropertyDao.findByIdAndValid(id, CommonConstant.INT_BOOLEAN_TRUE);
	}

	/**
	 * 查询资源商品下的所有项目
	 */
	public List<NetbarResourceCommodityProperty> findValidByCommodityIdAndStatus(Long commodityId, Integer status) {
		return netbarResourceCommodityPropertyDao.findByCommodityIdAndStatusAndValid(commodityId, status,
				CommonConstant.INT_BOOLEAN_TRUE);
	}

	/*
	 * 改状态
	 */
	public void changeStatus(String ids, int oper) {
		if (StringUtils.isBlank(ids)) {
			return;
		}

		String set = " set is_top=0, status=" + oper;
		String sql = "update netbar_resource_commodity_property " + set + " where id in(" + ids + ")";
		queryDao.update(sql);
	}

	/**
	 * 上架通知服务商
	 */
	public void enableNotify(Long commodityId) {
		if (commodityId != null) {
			NetbarResourceCommodity commodity = netbarResourceCommodityService.findById(commodityId);
			if (commodity != null) {
				String executePhone = commodity.getExecutePhone();
				if (commodityId != null && StringUtils.isNotBlank(executePhone)) {
					try {
						String[] phoneNum = { executePhone };
						String url = ShortUrlUtils
								.toShortUrl(systemConfig.getAdminDomain() + URL_SERVER_CONFIRM + commodityId) + " ";
						String[] params = { url };
						SMSMessageUtil.sendTemplateMessage(phoneNum, "7234", params);
					} catch (Exception e) {
						LOGGER.error("资源商品通知第三方服务上架确认短信异常:", e);
					}
				}
			}
		}
	}

	/**
	 * 上架通知服务商
	 */
	public void notifyWangyuCommodityAdmin(Long commodityId) {
		if (commodityId != null) {
			NetbarResourceCommodity commodity = netbarResourceCommodityService.findById(commodityId);
			if (commodity != null) {
				String executePhone = commodity.getExecutePhone();
				if (commodityId != null && StringUtils.isNotBlank(executePhone)) {
					try {
						String[] phoneNum = { executePhone };
						String[] params = {};
						SMSMessageUtil.sendTemplateMessage(phoneNum, "8252", params);
					} catch (Exception e) {
						LOGGER.error("资源商品上架网娱自由商品通知短信异常:", e);
					}
				}
			}
		}
	}

	public boolean checkSettlDateByCidAndPropertyNameAndSettlDateExist(Long commodityId, String propertyName,
			String settlDate, Long proid) {
		String sql = "select count(1) from netbar_resource_commodity_property where is_valid=1 and status=2 and commodity_id="
				+ commodityId + " and name='" + propertyName + "' and DATE(settl_date)='" + settlDate + "'";
		if (null != proid && proid.longValue() > 0) {
			sql = sql + " and id <> " + proid;
		}
		Number count = queryDao.query(sql);
		if (null != count && count.intValue() > 0) {
			return true;
		}
		return false;
	}

	/*
	 * 保存/更新对象
	 */
	public NetbarResourceCommodityProperty save(NetbarResourceCommodityProperty netbarResourceCommodityProperty) {
		if (null != netbarResourceCommodityProperty) {
			Date now = new Date();
			if (null != netbarResourceCommodityProperty.getId()) {
				netbarResourceCommodityProperty.setUpdateDate(now);
				NetbarResourceCommodityProperty old = findById(netbarResourceCommodityProperty.getId());
				if (null != old) {
					netbarResourceCommodityProperty = BeanUtils.updateBean(old, netbarResourceCommodityProperty);
				}
			} else {
				netbarResourceCommodityProperty.setValid(CommonConstant.INT_BOOLEAN_TRUE);
				netbarResourceCommodityProperty.setCreateDate(now);
			}
			return netbarResourceCommodityPropertyDao.save(netbarResourceCommodityProperty);
		}
		return null;
	}

	public String getNewPropertyNo() {
		String no = StringUtils.EMPTY;
		String sql = "select property_no from netbar_resource_commodity_property where is_valid=1 and DATE(create_date)=DATE(NOW()) order by property_no desc limit 1";
		Map<String, Object> map = queryDao.querySingleMap(sql);
		if (MapUtils.isNotEmpty(map) && null != map.get("property_no")) {
			String propertyNo = map.get("property_no").toString();
			String head = propertyNo.substring(0, propertyNo.length() - 3);
			String tail = propertyNo.substring(propertyNo.length() - 3);
			DecimalFormat df3 = new DecimalFormat("000");
			no = head + df3.format(NumberUtils.toLong(tail) + 1);
		} else {
			SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
			String date = format.format(new Date());
			no = date + "001";
		}

		return no;
	}

	/*
	 * 后台管理列表--项目
	 */
	public PageVO pageList(int page, Map<String, Object> paramsIn) {
		String sqlLimit = StringUtils.EMPTY;
		String field = "select p.commodity_id commodityId, p.id, p.property_no propertyNo, c.name, p.name propertyName, c.is_top isTop, p.price, c.use_quo_ratio useQuoRatio, p.measure, t1.name typeName, t2.name typeNameP, a.name areaName, c.qualifications, p.inventory_total inventoryTotal, p.inventory, ifnull(p.status,0) status, p.cate_type cateType, p.settl_date settlDate";
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
			field = "select p.commodity_id commodityId, p.id, c.name, p.name propertyName, c.is_top isTop, c.url, p.price, ifnull(p.vip_ratio,1) vipRatio, ifnull(p.gold_rebate,1) goldRebate, c.use_quo_ratio useQuoRatio, p.measure, a.name areaName, c.qualifications, p.inventory_total inventoryTotal, p.inventory, c.com_tag comTag";
			orderBy = " order by c.is_top desc, p.create_date desc";
			where = SqlJoiner.join(where,
					" and p.id in (select min(id) from netbar_resource_commodity_property group by commodity_id)");
			distinct = "DISTINCT p.commodity_id";
		}
		if (null != paramsIn) {
			String cid = MapUtils.getString(paramsIn, "cid", "0");
			if (StringUtils.isNotBlank(cid)) {
				where = SqlJoiner.join(where, " and p.commodity_id=", cid);
			}
			String pname = MapUtils.getString(paramsIn, "pname", "");
			if (StringUtils.isNotBlank(pname)) {
				where = SqlJoiner.join(where, " and p.name like '%", pname, "%'");
			}

			String name = MapUtils.getString(paramsIn, "name", "");
			if (StringUtils.isNotBlank(name)) {
				joinC = SqlJoiner.join(
						" right join netbar_resource_commodity c on c.id=p.commodity_id and c.name like '%", name,
						"%'");
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
					joinC = SqlJoiner.join(" right join netbar_resource_commodity c on c.id=p.commodity_id and ",
							condition);
				}
				joinCTotal = joinC;
			}
			String categoryId = MapUtils.getString(paramsIn, "categoryId", "");
			if (StringUtils.isNotBlank(categoryId)) {
				if (StringUtils.isAllBlank(name, areaCode)) {
					joinC = SqlJoiner.join(
							" right join netbar_resource_commodity c on c.id=p.commodity_id and c.category_id = ",
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
							" right join netbar_resource_commodity c on c.id=p.commodity_id and c.qualifications = ",
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
							" right join netbar_resource_commodity c on c.id=p.commodity_id and c.com_tag = ", comTag);
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
					joinT1Total = SqlJoiner.join(" left join netbar_resource_commodity c on c.id=p.commodity_id",
							joinT1);
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
			String status = MapUtils.getString(paramsIn, "status", "");
			if (StringUtils.isNotBlank(status)) {
				where = SqlJoiner.join(where, " and p.status=", status);
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
	 * 查询某网吧在指定时段内能购买的所有项目
	 */
	public List<Map<String, Object>> queryByNetbarIdAndBeginDateAndEndDate(Long netbarId, String beginDate,
			String endDate) {

		if (netbarId == null || StringUtils.isBlank(beginDate) || StringUtils.isBlank(endDate)) {
			return null;
		}

		String sql = SqlJoiner.join(
				"SELECT cp.id, cp.name, cp.settl_date, c.province, cp.rebate, cp.gold_rebate, cp.vip_ratio, cp.jewel_ratio, cp.inventory",
				" FROM netbar_resource_commodity_property cp JOIN netbar_resource_commodity c ON cp.commodity_id = c.id",
				" JOIN netbar_t_info n ON (LEFT (c.province, 2) = LEFT (n.area_code, 2) or c.province = '000000' or c.province is null) AND n.id = ",
				netbarId.toString(), " WHERE cp.is_valid = 1 AND c.is_valid = 1 AND cp.status = 2 AND cp.inventory > 0",
				" AND ( ( cp.cate_type = 0 AND cp.settl_date >= '", beginDate, "' AND cp.settl_date <= '", endDate,
				"' ) OR cp.cate_type != 0 )");
		return queryDao.queryMap(sql);
	}
}