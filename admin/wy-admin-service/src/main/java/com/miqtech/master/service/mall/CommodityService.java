package com.miqtech.master.service.mall;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;
import com.miqtech.master.consts.mall.CommodityConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.mall.CommodityInfoDao;
import com.miqtech.master.entity.Pager;
import com.miqtech.master.entity.mall.CommodityCategory;
import com.miqtech.master.entity.mall.CommodityInfo;
import com.miqtech.master.utils.DateUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.utils.StringUtils;
import com.miqtech.master.vo.PageVO;

/**
 * （金币）商品操作service
 */
@Component
public class CommodityService {
	@Autowired
	private CommodityInfoDao commodityDao;
	@Autowired
	private QueryDao queryDao;
	@Autowired
	private MallCdkeyStockService mallCdkeyStockService;
	@Autowired
	private CommodityCategoryService commodityCategoryService;

	/**
	 * 查询商品列表（有效的）
	 */
	public Map<String, Object> list() {
		//查出商品区
		String sqlAreaId = "select distinct(id) from mall_t_commodity_area where is_valid=1 and id<>3";
		List<Map<String, Object>> areaIdList = queryDao.queryMap(sqlAreaId);
		//将所有商品按区分成多个list返给前端
		Map<String, Object> mapList = new HashMap<>();
		Object areaId = null;
		if (CollectionUtils.isNotEmpty(areaIdList)) {
			for (int i = 0; i < areaIdList.size(); i++) {
				areaId = areaIdList.get(i).get("id");
				if (null == areaId) {
					continue;
				}
				String sqlQuery = SqlJoiner
						.join("select c.id, ifnull(c.sort_no,99999999) sortNo, c.name commodityName, i.icon mainIcon, c.area_id areaId, a.area_name areaName, c.category_id categoryId, t.name categoryName, c.price, c.discount_price discountPrice",
								" from mall_t_commodity c",
								" left join mall_t_commodity_area a on a.id=" + areaId.toString(),
								" left join mall_t_commodity_category t on t.id = c.category_id",
								" left join mall_r_commodity_icon i on i.commodity_id = c.id",
								" where i.is_main = 1 and c.is_valid=1 and c.status=1 and c.area_id="
										+ areaId.toString(), " order by sortNo asc");
				List<Map<String, Object>> list = queryDao.queryMap(sqlQuery);
				if (CollectionUtils.isEmpty(list)) {
					list = new ArrayList<Map<String, Object>>();
				}
				mapList.put("areaList" + (i + 1), list);
			}
		}

		return mapList;
	}

	public Map<String, Object> mallCommodityList() {
		Map<String, Object> result = new HashMap<String, Object>();
		//众筹夺宝
		String sql = "select c.id, ifnull(c.sort_no, 99999999) sortNo, c. name commodityName, i.icon mainIcon, c.area_id areaId, a.area_name areaName, c.category_id categoryId, t. name categoryName, c.price, if(cast((c.coins - d.coins) / price AS signed )<0,0,cast((c.coins - d.coins) / price AS signed )) left_num, if(cast( d.coins / c.coins * 100 AS signed )>100,100,cast( d.coins / c.coins * 100 AS signed )) progress, c.crowdfund_status, if ( f.id is null, e.virtual_phone, f.username ) prize_phone from mall_t_commodity c left join mall_t_commodity_area a on a.id = 3 left join mall_t_commodity_category t on t.id = c.category_id left join mall_r_commodity_icon i on i.commodity_id = c.id and i.is_main = 1 left join ( select commodity_id, sum(coin) coins from mall_r_commodity_history where is_valid = 1 and commodity_source = 1 group by commodity_id ) d on d.commodity_id = c.id left join mall_r_commodity_history e on e.commodity_id = c.id and e.commodity_source = 1 and e.is_get = 1 and e.is_valid=1 left join user_t_info f on e.user_id = f.id where c.is_valid = 1 and c. status = 1 and c.area_id = 3 order by c.crowdfund_status,sortNo desc,c.create_date desc limit 3";
		result.put("grobTreasure", queryDao.queryMap(sql));
		//兑奖专区
		sql = "select c.id, ifnull(c.sort_no, 99999999) sortNo, c. name commodityName, i.icon mainIcon, c.area_id areaId, a.area_name areaName, c.category_id categoryId, t. name categoryName, c.price, c.discount_price discountPrice from mall_t_commodity c left join mall_t_commodity_area a on a.id = 1 left join mall_t_commodity_category t on t.id = c.category_id left join mall_r_commodity_icon i on i.commodity_id = c.id and i.is_main = 1 where c.is_valid = 1 and c. status = 1 and c.area_id = 1 order by sortNo asc limit 4";
		result.put("prizeArea", queryDao.queryMap(sql));
		return result;
	}

	public PageVO mallCommodityListByAreaId(Long areaId, Pager pager) {
		PageVO vo = new PageVO();
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("start", pager.start);
		params.put("pageSize", pager.pageSize);
		//众筹夺宝
		if (areaId == 3) {
			Number total = queryDao
					.query("select count(1) from mall_t_commodity c  where c.is_valid = 1 and c. status = 1 and c.area_id = 3");
			if (total != null) {
				if (pager.total >= total.intValue()) {
					vo.setIsLast(1);
				}
				vo.setTotal(total.intValue());
			}
			vo.setList(queryDao
					.queryMap(
							"select c.id, ifnull(c.sort_no, 99999999) sortNo, c. name commodityName, i.icon mainIcon, c.area_id areaId, a.area_name areaName, c.category_id categoryId, t. name categoryName, c.price, if(cast((c.coins-ifnull(d.coins,0))/price AS signed)<0,0,cast((c.coins-ifnull(d.coins,0))/price AS signed)) left_num, if(cast(d.coins/c.coins*100 AS signed)>100,100,cast(d.coins/c.coins*100 AS signed)) progress, c.crowdfund_status, if ( f.id is null, e.virtual_phone, f.username ) prize_phone from mall_t_commodity c left join mall_t_commodity_area a on a.id = 3 left join mall_t_commodity_category t on t.id = c.category_id left join mall_r_commodity_icon i on i.commodity_id = c.id and i.is_main = 1 left join ( select mh.commodity_id,  sum(mh.coin) coins from  mall_r_commodity_history mh,  mall_t_commodity cc where mh.commodity_id = cc.id   and area_id = 3   and mh.is_valid = 1   and mh.commodity_source = 1 group by mh.commodity_id ) d on d.commodity_id = c.id left join mall_r_commodity_history e on e.commodity_id = c.id and e.commodity_source = 1 and e.is_get = 1 and e.is_valid=1 left join user_t_info f on e.user_id = f.id where c.is_valid = 1 and c. status = 1 and c.area_id = 3 order by c.crowdfund_status,sortNo desc,c.create_date limit :start,:pageSize",
							params));
		} else if (areaId == 1) {//兑奖专区
			Number total = queryDao
					.query("select count(1) from mall_t_commodity c  where c.is_valid = 1 and c. status = 1 and c.area_id = 1");
			if (total != null) {
				if (pager.total >= total.intValue()) {
					vo.setIsLast(1);
				}
				vo.setTotal(total.intValue());
			}
			vo.setList(queryDao
					.queryMap(
							"select c.id, ifnull(c.sort_no, 99999999) sortNo, c. name commodityName, i.icon mainIcon, c.area_id areaId, a.area_name areaName, c.category_id categoryId, t. name categoryName, c.price, c.discount_price discountPrice from mall_t_commodity c left join mall_t_commodity_area a on a.id = 1 left join mall_t_commodity_category t on t.id = c.category_id left join mall_r_commodity_icon i on i.commodity_id = c.id and i.is_main = 1 where c.is_valid = 1 and c. status = 1 and c.area_id = 1 order by sortNo asc limit :start,:pageSize",
							params));
		}
		return vo;
	}

	/**
	 * 根据id查商品
	 */
	public CommodityInfo getCommodityById(long commodityId) {
		return commodityDao.findOne(commodityId);
	}

	/**
	 * 查目前商品最大排序号
	 */
	public int getLargestSortNo() {
		String sqlQuery = "select sort_no from mall_t_commodity where sort_no is not null and  area_id = 1  order by sort_no desc limit 1";
		Number sortNo = queryDao.query(sqlQuery);
		if (null != sortNo) {
			return sortNo.intValue();
		}
		return 0;
	}

	/**
	 * 查目前商品最小排序号
	 */
	public int getLeastSortNo() {
		String sqlQuery = "select sort_no from mall_t_commodity where sort_no is not null and  area_id = 1 order by sort_no asc limit 1";
		Number sortNo = queryDao.query(sqlQuery);
		if (null != sortNo) {
			return sortNo.intValue();
		}
		return 0;
	}

	/**
	 * 判断某排序号是否已存在（有效商品里）
	 */
	public boolean existSortNoValid(int sortNo) {
		String sqlCount = "select count(1) from mall_t_commodity where is_valid and  area_id = 1 and sort_no=" + sortNo;
		Number count = queryDao.query(sqlCount);
		if (null != count && count.intValue() > 0) {
			return true;
		}
		return false;
	}

	/**
	 * 排序号介于(oriSorno,newSortNo]所有商品ID
	 */
	public List<Map<String, Object>> getFrontIds(int newSortNo, int oriSorno, long oriId) {
		String sqlQuery = "select id from mall_t_commodity where id <> " + oriId + " and  area_id = 1 and sort_no <= "
				+ newSortNo + " and sort_no > " + oriSorno;
		return queryDao.queryMap(sqlQuery);
	}

	/**
	 * 排序号介于[newSortNo,oriSorno)所有商品ID
	 */
	public List<Map<String, Object>> getBehindIds(int newSortNo, int oriSorno, long oriId) {
		if (oriSorno == 0) {
			oriSorno = getLargestSortNo();
		}
		String sqlQuery = "select id from mall_t_commodity where id <> " + oriId + " and  area_id = 1 and sort_no >= "
				+ newSortNo + " and sort_no < " + oriSorno;
		return queryDao.queryMap(sqlQuery);
	}

	/**
	 * 查出有重复sorNo的ID组(以","隔开)
	 */
	public List<Map<String, Object>> getIdsGoupBySortNo(Long id) {
		String sqlFilter = StringUtils.EMPTY;
		if (null != id) {
			sqlFilter = " and id <> " + id;
		}
		String sqlQuery = SqlJoiner.join(
				"SELECT GROUP_CONCAT(id) ids, sort_no FROM mall_t_commodity where area_id = 1", sqlFilter,
				" GROUP BY sort_no HAVING count(id)>=2");
		return queryDao.queryMap(sqlQuery);
	}

	/**
	 * 根据ID修改排序号
	 */
	public void updateSortNoById(Long id, int sortNo) {
		String sqlUpdate = "UPDATE mall_t_commodity SET sort_no = " + sortNo + " WHERE area_id = 1 and id = " + id;
		queryDao.update(sqlUpdate);
	}

	/**
	 * 根据分区码、类别码，查出最大的商品编号
	 */
	public String getLatestCommodityIdByParams(String areaNo, String categoryNo) {
		String sqlQuery = "select item_no from mall_t_commodity where left(item_no,4)='" + areaNo + categoryNo
				+ "' order by item_no desc limit 1";
		Map<String, Object> map = queryDao.querySingleMap(sqlQuery);
		if (null != map && null != map.get("item_no")) {
			return map.get("item_no").toString();
		}
		return null;
	}

	/**
	 * 根据商品编号、展示区ID、类别ID，查出最新插入的商品ID
	 */
	public long getLargestItemNoByAreaAndCagetory(String itemNo, long areaId, long categoryId) {
		String sqlQuery = "select id from mall_t_commodity where item_no='" + itemNo + "'" + " and area_id=" + areaId
				+ " and category_id=" + categoryId + " order by create_date desc";
		return Long.parseLong(queryDao.querySingleMap(sqlQuery).get("id").toString());
	}

	/**
	 * 保存商品
	 */
	public void save(CommodityInfo commodityInfo) {
		commodityDao.save(commodityInfo);
	}

	/**
	 * 根据商品ID更新上下架状态
	 */
	public void updateStatusByCommodityId(long commodityId, int status) {
		CommodityInfo commodityInfo = getCommodityById(commodityId);
		commodityInfo.setStatus(status);
		commodityInfo.setUpdateDate(new Date());
		commodityDao.save(commodityInfo);
	}

	/**
	 * 根据id查商品活动时间状态（0-未开始，1-进行中，2-已结束）
	 */
	public int getCommodityStatusById(long commodityId) {
		String sqlQuery = "select if(c.end_date < NOW(), 2, if(c.start_date > NOW(), 0, 1)) timeStatus from mall_t_commodity c where is_valid=1 and c.id="
				+ commodityId;
		return Integer.parseInt(queryDao.querySingleMap(sqlQuery).get("timeStatus").toString());
	}

	/**
	 * 查用户金币余额
	 */
	public int getUserCoinById(long userId) {
		String sqlUserCoin = "select u.coin from user_t_info u where u.id=" + userId;
		Map<String, Object> result = queryDao.querySingleMap(sqlUserCoin);

		return result == null ? 0 : NumberUtils.toInt(result.get("coin") == null ? "0" : result.get("coin").toString());
	}

	/**
	 * 查商品历史表中用户兑换某商品的次数
	 */
	public int getCommodityHistoryCountByuserIdAndCommodityId(long userId, long commodityId) {
		String startDate = DateUtils.getCurrentSomeDay(1, "yyyy-MM-dd");
		String sqlHistory = "select count(1) from mall_r_commodity_history h where h.commodity_id=" + commodityId
				+ " and h.user_id=" + userId + " and create_date >='" + startDate + "'";

		return Integer.parseInt(queryDao.query(sqlHistory).toString());
	}

	/**
	 * 根据id查商品详细信息，状态
	 */
	public Map<String, Object> getDetailById(Long commodityId, Long userId) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("commodityId", commodityId);
		String sqlQuery = SqlJoiner
				.join("select c.third_type,c.id, c.area_id areaId, c.category_id categoryId, c.status, c.name, c.price, c.discount_price discountPrice, c.inventory, c.start_date startDate, c.end_date endDate, c.introduce, c.rule",
						" from mall_t_commodity c where c.is_valid=1 and c.id=:commodityId");
		String sqlIcon = "select icon from mall_r_commodity_icon where is_main <>1 and commodity_id=" + commodityId;//轮播图
		//查商品信息
		Map<String, Object> commodityMap = queryDao.querySingleMap(sqlQuery, params);
		if (null != commodityMap && null != commodityMap.get("status")
				&& commodityMap.get("status").toString().equals("1")) {
			double discountPrice = Double.parseDouble(commodityMap.get("discountPrice").toString()); //商品现价
			int inventory = Integer.parseInt(commodityMap.get("inventory").toString()); //商品库存
			int type = 0;
			Object categoryIdObj = commodityMap.get("categoryId");
			String categoryId = null == categoryIdObj ? "" : String.valueOf(categoryIdObj);
			CommodityCategory commodityCategory = commodityCategoryService.getCommodityCategoryById(NumberUtils
					.toLong(categoryId));
			if (null != commodityCategory) {
				type = null == commodityCategory.getType() ? 0 : commodityCategory.getType();
			}
			if (CommodityConstant.CATEGORY_TYPE_CDKEY.equals(type)) { //如果是CDKEY商品，则库存按商品对应的CDKEY库存来
				inventory = mallCdkeyStockService.getCdkeyInventoryByCommodityId(commodityId);
				commodityMap.put("inventory", inventory);
			}
			int timeStatus = getCommodityStatusById(commodityId); //商品活动时间状态

			if (null != userId) {
				Integer commodityLimit = getCommodityById(commodityId).getLimit();
				boolean unlimited = null == commodityLimit || commodityLimit < 1
						|| getCommodityHistoryCountByuserIdAndCommodityId(userId, commodityId) < commodityLimit; //是否满足可兑换次数
				if (unlimited) {
					if (inventory > 0) {
						if (timeStatus == 1) {
							int userCoin = getUserCoinById(userId); //用户剩余金币数量
							if (discountPrice <= userCoin) { //正常可兑换状态
								commodityMap.put("status", "1");
							} else { //用户金币不足
								commodityMap.put("status", "-1");
							}
						} else { //不在活动期间
							commodityMap.put("status", "2");
						}
					} else { //商品库存不足
						commodityMap.put("status", "0");
					}
				} else {
					commodityMap.put("status", "3");//超出可兑换次数
				}

			} else {
				commodityMap.put("status", "-4");//用户未登录
			}
		} else {
			if (null == commodityMap) {
				commodityMap = Maps.newHashMap();
			}
			commodityMap.put("status", "-2");//商品不存在或已下架，都当做下架状态:-2
			return commodityMap;
		}
		//查商品图片
		List<Map<String, Object>> iconsList = queryDao.queryMap(sqlIcon);
		if (iconsList.size() > 0) {
			commodityMap.put("icons", iconsList);
		}

		return commodityMap;
	}

	/**
	 * ##后台管理##:查询商品列表，分页
	 */
	public PageVO listPage(int page, int rows, Map<String, Object> params) {
		String sqlQuery = SqlJoiner
				.join("select c.id,ifnull(c.sort_no,99999999) sortNo, c.item_no itemNo, cd.name cdkeyName, c.name commodityName, c.status, i.icon mainIcon, c.area_id areaId, a.area_name areaName, c.category_id categoryId, c.third_type thirdType, t.name categoryName, c.price, c.discount, c.discount_price discountPrice, c.inventory, c.total_inventory totalInventory, c.start_date startDate, c.end_date endDate, c.is_provide isProvide, c.province, ifnull(sa.name,'全国') provinceName, c.top_show topShow, c.limit, c.quota, c.probability, c.introduce, c.rule, c.information_defualt informationDefualt",
						" from mall_t_commodity c", " left join mall_t_commodity_area a on a.id=c.area_id",
						" left join mall_t_commodity_category t on t.id = c.category_id",
						" left join mall_r_commodity_icon i on i.commodity_id = c.id",
						" left join sys_t_area sa on sa.area_code = c.province",
						" left join mall_t_cdkey_stock cd on cd.commodity_id = c.id and c.category_id=2",
						" where (i.is_main = 1 or i.is_main is null) and c.is_valid = 1 and c.area_id<>2 ");
		String sqlCount = SqlJoiner.join("select count(1) from mall_t_commodity c", " where c.is_valid=1");
		if (null != params.get("status")) { //上下架状态
			sqlQuery = SqlJoiner.join(sqlQuery, " and c.status=:status");
			sqlCount = SqlJoiner.join(sqlCount, " and c.status=" + params.get("status"));
		}
		if (null != params.get("name")) { //根据商品名查询
			sqlQuery = SqlJoiner.join(sqlQuery, " and c.name like concat('%', :name, '%')");
			sqlCount = SqlJoiner.join(sqlCount, " and c.name like '%" + params.get("name") + "%'");
		}
		if (null != params.get("areaId")) { //分类查询
			sqlQuery = SqlJoiner.join(sqlQuery, " and c.area_id=:areaId");
			sqlCount = SqlJoiner.join(sqlCount, " and c.area_id like '%" + params.get("areaId") + "%'");
		}
		if (null != params.get("province")) { //分地区查询
			sqlQuery = SqlJoiner.join(sqlQuery, " and c.province=:province");
			sqlCount = SqlJoiner.join(sqlCount, " and c.province='" + params.get("province") + "'");
		}
		if (page < 1) {
			page = 1;
		}
		params.put("page", (page - 1) * rows);
		params.put("rows", rows);
		sqlQuery = SqlJoiner.join(sqlQuery, " order by c.area_id, sortNo asc, c.create_date desc limit :page, :rows");

		PageVO pageVO = new PageVO();
		pageVO.setList(queryDao.queryMap(sqlQuery, params));

		Number total = queryDao.query(sqlCount);
		pageVO.setTotal(total.intValue());

		return pageVO;
	}

	/**
	 * 查出所有商品（有效）
	 */
	public List<Map<String, Object>> commodityList() {
		String sqlQuery = "select c.id, c.name from mall_t_commodity c where c.is_valid=1";
		return queryDao.queryMap(sqlQuery);
	}

	/**
	 * 根据title和类型查出商品（有效）
	 */
	public List<Map<String, Object>> findValidByTitleAndModule(String title, long moduleId) {
		String sqlQuery = "select c.id, c.name label  from mall_t_commodity c where c.is_valid=1 and c.name like '%"
				+ title + "%' and c.area_id=" + moduleId;
		return queryDao.queryMap(sqlQuery);
	}

	/**众筹夺宝商品详情
	 * @param id
	 * @param userId
	 * @param pager
	 * @return
	 */
	public Map<String, Object> commodityDetail(Long id, Long userId, Pager pager) {
		Map<String, Object> result = new HashMap<String, Object>();
		if (userId == null) {
			userId = 0L;
		}
		PageVO vo = new PageVO();
		Number number = queryDao.query("select count(1) from mall_r_commodity_history a where a.commodity_id=" + id
				+ " and a.commodity_source=1");
		if (number != null) {
			vo.setTotal(number.intValue());
			if (number.intValue() <= pager.total) {
				vo.setIsLast(1);
			}
		}
		vo.setList(queryDao
				.queryMap("select if ( b.id is null, a.virtual_phone, b.username ) prize_phone, ifnull(a.coin,0) pay_coin, a.create_date from mall_r_commodity_history a left join user_t_info b on a.user_id = b.id where a.is_valid=1 and a.commodity_id ="
						+ id
						+ " and a.commodity_source = 1 order by a.create_date desc limit "
						+ pager.start
						+ ","
						+ pager.pageSize));
		result.put("buy_record", vo);
		if (pager.page == 1) {
			result.put(
					"commodity_info",
					queryDao.querySingleMap("select a.price,a.id, b.icon mainIcon, a. name commodityName, if(cast(c.coins/a.coins*100 AS signed)>100,100,cast(c.coins/a.coins*100 AS signed)) progress, a.coins, if(cast((a.coins-ifnull(c.coins,0))/price AS signed)<0,0,cast((a.coins-ifnull(c.coins,0))/price AS signed)) left_num, cast(d.buy_num/a.price AS signed) buy_num, a.introduce, a.rule, a.crowdfund_status, if ( f.id is null, e.virtual_phone, f.username ) prize_phone, g.cdkey,group_concat(b.icon order by b.icon) as imgs from mall_t_commodity a left join mall_r_commodity_icon b on b.commodity_id = a.id and b.is_valid=1  and b.is_main<>1 left join ( select commodity_id, sum(coin) coins from mall_r_commodity_history where is_valid = 1 and commodity_source = 1 and commodity_id = "
							+ id
							+ " group by commodity_id ) c on c.commodity_id = a.id left join ( select sum(coin) buy_num, commodity_id from mall_r_commodity_history where is_valid = 1 and commodity_source = 1 and user_id ="
							+ userId
							+ " group by user_id,commodity_id ) d on d.commodity_id = a.id left join mall_r_commodity_history e on e.commodity_id = a.id and e.commodity_source = 1 and e.is_get = 1 and e.is_valid = 1 left join mall_commodity_history_cdkey g on g.history_id=e.id and g.is_valid=1 and g.is_selected=1 left join user_t_info f on e.user_id = f.id where a.id ="
							+ id + " group by a.id"));
		}
		return result;
	}
}
