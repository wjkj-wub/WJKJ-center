package com.miqtech.master.service.mall;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.mall.CommodityHistoryCdkeyDao;
import com.miqtech.master.dao.mall.CommodityHistoryDao;
import com.miqtech.master.dao.mall.CommodityInfoDao;
import com.miqtech.master.dao.mall.TurntableVirtualDao;
import com.miqtech.master.entity.mall.CommodityHistory;
import com.miqtech.master.entity.mall.CommodityHistoryCdkey;
import com.miqtech.master.entity.mall.CommodityInfo;
import com.miqtech.master.utils.BeanUtils;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.vo.PageVO;

@Component
public class TurntableVirtualService {
	@Autowired
	TurntableVirtualDao turntableVirtualDao;
	@Autowired
	QueryDao queryDao;
	@Autowired
	CommodityHistoryCdkeyDao commodityHistoryCdkeyDao;
	@Autowired
	CommodityInfoDao commodityDao;
	@Autowired
	CommodityHistoryDao commodityHistoryDao;

	/**
	 * 保存资讯
	 */
	public CommodityHistory save(CommodityHistory item) {
		if (item != null) {
			item.setValid(CommonConstant.INT_BOOLEAN_TRUE);
			CommodityHistory info = turntableVirtualDao.save(item);
			return info;
		}
		return null;
	}

	/**
	 * 根据ID查询
	 */
	public CommodityHistory findById(Long id) {
		return turntableVirtualDao.findOne(id);
	}

	public CommodityInfo findByCommodityInfoId(Long id) {
		return commodityDao.findOne(id);
	}

	/**
	 * 后台分页
	 */
	public PageVO adminPage(int page, Integer type, String prizeName) {
		String limitSql = PageUtils.getLimitSql(page);
		List<Map<String, Object>> list = null;
		Number total = 0;
		if (type == 1) {
			if (prizeName == "" || prizeName == null) {
				String totalSql = "SELECT COUNT(1) FROM mall_r_commodity_history m WHERE m.is_valid = 1 and m.user_id is null and m.commodity_source=2";
				total = queryDao.query(totalSql);

				if (total != null && total.longValue() > 0) {
					String sql = SqlJoiner.join(
							"SELECT b.prize_name prizeName,a.virtual_phone virtualPhone FROM mall_r_commodity_history a left join mall_t_turntable_prize b on a.commodity_id=b.id WHERE a.is_valid = 1 and a.user_id is null and a.commodity_source=2 group by a.id order by b.id desc,a.create_date desc",
							limitSql);
					list = queryDao.queryMap(sql);
				}
			} else {
				String totalSql = "SELECT COUNT(1)  FROM (select 1 from mall_r_commodity_history a left join mall_t_turntable_prize b on a.commodity_id=b.id WHERE a.is_valid = 1 and a.user_id is null and a.commodity_source=2 and b.category_id="
						+ prizeName + " group by a.id) a";
				total = queryDao.query(totalSql);

				if (total != null && total.longValue() > 0) {
					String sql = SqlJoiner.join(
							"SELECT b.prize_name prizeName,a.virtual_phone virtualPhone FROM mall_r_commodity_history a left join mall_t_turntable_prize b on a.commodity_id=b.id WHERE a.is_valid = 1 and a.user_id is null and a.commodity_source=2 and b.category_id=",
							prizeName, " group by a.id order by b.id desc,a.create_date desc", limitSql);
					list = queryDao.queryMap(sql);
				}
			}
		} else {
			if (prizeName == "" || prizeName == null) {
				String totalSql = "SELECT COUNT(1) FROM mall_r_commodity_history m WHERE m.is_valid = 1 and m.user_id is null and m.commodity_source=1";
				total = queryDao.query(totalSql);
				if (total != null && total.longValue() > 0) {
					String sql = SqlJoiner.join(
							"SELECT a.create_date createDate,a.virtual_phone virtualPhone,b.name FROM mall_r_commodity_history a left join mall_t_commodity b on a.commodity_id=b.id WHERE a.is_valid = 1 and a.user_id is null and a.commodity_source=1 group by a.id order by b.id desc,a.create_date desc",
							limitSql);
					list = queryDao.queryMap(sql);
				}
			} else {
				String totalSql = "SELECT COUNT(1) FROM (select 1 from mall_r_commodity_history a left join mall_t_commodity b on a.commodity_id=b.id WHERE a.is_valid = 1 and a.user_id is null and a.commodity_source=1 and b.id="
						+ prizeName + " group by a.id) a";
				total = queryDao.query(totalSql);
				if (total != null && total.longValue() > 0) {
					String sql = SqlJoiner.join(
							"SELECT a.create_date createDate,a.virtual_phone virtualPhone,b.name FROM mall_r_commodity_history a left join mall_t_commodity b on a.commodity_id=b.id WHERE a.is_valid = 1 and a.user_id is null and a.commodity_source=1 and b.id=",
							prizeName, " group by a.id order by b.id desc,a.create_date desc", limitSql);
					list = queryDao.queryMap(sql);
				}
			}
		}
		return new PageVO(page, list, total);
	}

	/**
	 * 查询众筹中众筹活动信息
	 */
	public List<Map<String, Object>> queryCrowdFunding() {
		String sql = "select a.id,a.name,a.create_date from mall_t_commodity a where a.area_id=3 and a.is_valid=1 and a.crowdfund_status=0";
		return queryDao.queryMap(sql);
	}

	/**
	 * 查询全部众筹活动信息
	 */
	public List<Map<String, Object>> queryAllCrowdFunding() {
		String sql = "select a.id,a.name from mall_t_commodity a where a.area_id=3 and a.is_valid=1";
		return queryDao.queryMap(sql);
	}

	/**
	 * 查询众筹商品的总金币，单价以及现有参与人数
	 */
	public List<Map<String, Object>> queryCrowdingGoods(Long prizeName) {
		String sql = "select a.coins,a.price,b.personCount personCount from mall_t_commodity a left join (select count(1) personCount,commodity_id,commodity_source from mall_r_commodity_history where commodity_source=1 group by commodity_id)  b on b.commodity_id = a.id where a.is_valid = 1 and a.area_id=3 and a.id="
				+ prizeName;
		return queryDao.queryMap(sql);
	}

	/*
	 * 保存兑换码
	 */
	public CommodityHistoryCdkey saveCdkey(CommodityHistoryCdkey item) {
		if (item != null) {
			item.setValid(CommonConstant.INT_BOOLEAN_TRUE);
			item.setCreateDate(new Date());
			CommodityHistoryCdkey info = commodityHistoryCdkeyDao.save(item);
			return info;
		}
		return null;
	}

	/**
	 * 查询转盘奖品数量
	 */
	public List<Map<String, Object>> queryGoodsNum(Long prizeName) {
		String sql = "select a.prize_count,(case when b.commodity_id is null then 0 else count(1) end) as personCount from mall_t_turntable_prize a"
				+ " LEFT JOIN mall_r_commodity_history b ON a.id = b.commodity_id and b.commodity_source=2 where a.is_valid = 1 and a.id="
				+ prizeName + " group by a.id";
		return queryDao.queryMap(sql);
	}

	/**
	 * 计算消耗的金币
	 */
	public Number gueryConsumeCoins(String prizeName) {
		return queryDao
				.query("select sum(coin) from mall_r_commodity_history where status=0 and is_valid=1 and commodity_source=1 and commodity_id="
						+ NumberUtils.toLong(prizeName) + " group by commodity_id");
	}

	/**
	 * 查询开奖方式
	 */
	public Integer gueryRunLotteryWay(String prizeName) {
		String sql = "select a.auto_drawn from mall_t_commodity a where a.area_id=3 and a.is_valid=1 and a.id="
				+ NumberUtils.toLong(prizeName);
		Number sortNo = queryDao.query(sql);
		if (null != sortNo) {
			return sortNo.intValue();
		}
		return 0;
	}

	/**
	 * 保存众筹商品众筹状态
	 */
	public CommodityInfo saveOrUpdate(CommodityInfo commodityInfo) {
		if (null != commodityInfo) {
			Date now = new Date();
			commodityInfo.setUpdateDate(now);
			if (null != commodityInfo.getId()) {
				CommodityInfo old = findByCommodityInfoId(commodityInfo.getId());
				if (null != old) {
					commodityInfo = BeanUtils.updateBean(old, commodityInfo);
				}
			} else {
				commodityInfo.setValid(CommonConstant.INT_BOOLEAN_TRUE);
				commodityInfo.setCreateDate(now);
			}
			return commodityDao.save(commodityInfo);
		}
		return commodityInfo;
	}

	public CommodityHistory saveOrUpdate(CommodityHistory commodityHistory) {
		if (null != commodityHistory) {
			Date now = new Date();
			commodityHistory.setUpdateDate(now);
			if (null != commodityHistory.getId()) {
				CommodityHistory old = commodityHistoryDao.findOne(commodityHistory.getId());
				if (null != old) {
					commodityHistory = BeanUtils.updateBean(old, commodityHistory);
				}
			} else {
				commodityHistory.setValid(CommonConstant.INT_BOOLEAN_TRUE);
				commodityHistory.setCreateDate(now);
			}
			return commodityHistoryDao.save(commodityHistory);
		}
		return commodityHistory;
	}

}
