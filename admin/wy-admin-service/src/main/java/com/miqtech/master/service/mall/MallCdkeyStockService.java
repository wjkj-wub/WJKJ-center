package com.miqtech.master.service.mall;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.mall.MallCdkeyDao;
import com.miqtech.master.dao.mall.MallCdkeyStockDao;
import com.miqtech.master.entity.mall.MallCdkey;
import com.miqtech.master.entity.mall.MallCdkeyStock;
import com.miqtech.master.utils.DateUtils;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.vo.PageVO;

@Component
public class MallCdkeyStockService {
	@Autowired
	private MallCdkeyStockDao mallCdkeyStockDao;
	@Autowired
	private MallCdkeyDao mallCdkeyDao;
	@Autowired
	private QueryDao queryDao;

	public MallCdkeyStock findById(Long id) {
		return mallCdkeyStockDao.findOne(id);
	}

	/**
	 * 查出商品ID为空的cdkey
	 */
	public List<Map<String, Object>> findCdkeysForCommodity() {
		String sqlCdkey = "select id, name from mall_t_cdkey_stock where is_valid=1 and commodity_id is null";
		return queryDao.queryMap(sqlCdkey);
	}

	@Transactional
	public void save(MallCdkeyStock stock, List<MallCdkey> list) {
		Date min = new Date();
		Date max = new Date();
		if (stock.getBeginTime() == null || stock.getEndTime() == null) {
			for (MallCdkey obj : list) {
				if (obj.getBeginTime() != null && obj.getBeginTime().getTime() - min.getTime() < 0) {
					min = obj.getBeginTime();
				}
				if (obj.getEndTime() != null && obj.getEndTime().getTime() - max.getTime() > 0) {
					max = obj.getEndTime();
				}
			}
			stock.setBeginTime(min);
			stock.setEndTime(max);
		}
		mallCdkeyStockDao.save(stock);
		Long id = stock.getId();
		if (list != null) {
			for (MallCdkey obj : list) {
				obj.setStockId(id);
				obj.setBeginTime(stock.getBeginTime());
				obj.setEndTime(stock.getEndTime());
				mallCdkeyDao.save(obj);
			}
		}
	}

	/**列表分页查询
	 * @return
	 */
	public PageVO queryList(String name, Integer sort, Integer status, Integer page) {
		String sql = "";
		String nameSql = "";
		String sortSql = "";
		String statusSql = "";
		String date = DateUtils.dateToString(new Date(), DateUtils.YYYY_MM_DD_HH_MM);
		if (sort == null || sort == 0) {
			sortSql = "order by a.create_date desc";
		} else {
			sortSql = " order by remain";
		}
		if (status == null) {

		} else if (status == 0) {
			statusSql = " and date_format(IFNULL(a.end_time, '2100-01-01'), '%Y-%m-%d %H:%i')<'" + date + "'";
		} else if (status == 1) {
			statusSql = " and date_format(IFNULL(a.end_time, '2100-01-01'), '%Y-%m-%d %H:%i')>='" + date + "'";
		}
		if (StringUtils.isNotBlank(name)) {
			nameSql = " and a.name like '%" + name.trim() + "%' ";
		}
		int pageSize = PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		if (page == null) {
			page = 1;
		}
		int start = (page - 1) * pageSize;
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("start", start);
		params.put("pageSize", pageSize);
		String limitSql = " limit :start,:pageSize";
		sql = SqlJoiner.join(
				"SELECT count(DISTINCT a.id) total FROM mall_t_cdkey_stock a, mall_t_cdkey b where a.id = b.stock_id ",
				nameSql, statusSql);
		PageVO vo = new PageVO();
		Number totalCount = queryDao.query(sql);
		if (totalCount != null) {
			vo.setTotal(totalCount.intValue());
		}
		sql = SqlJoiner
				.join("SELECT a.id,a.commodity_id,a.code,a.name,a.begin_time,a.end_time,a.create_date,count(b.id) total,sum(if(is_use=0,1,0)) remain,if(date_format(IFNULL(a.end_time, '2100-01-01'), '%Y-%m-%d %H:%i')<'",
						date,
						"',0,1) status FROM mall_t_cdkey_stock a LEFT JOIN mall_t_cdkey b ON a.id = b.stock_id where 1=1 ",
						nameSql, statusSql, " group by b.stock_id ", sortSql, limitSql);
		vo.setCurrentPage(page);
		vo.setList(queryDao.queryMap(sql, params));
		return vo;
	}

	@Transactional
	public void del(Long id) {
		mallCdkeyStockDao.delete(id);
		List<MallCdkey> list = mallCdkeyDao.findByStockIdAndIsUse(id, 0);
		if (CollectionUtils.isNotEmpty(list)) {
			for (MallCdkey obj : list) {
				mallCdkeyDao.delete(obj);
			}
		}
	}

	/**获取编码
	 * @return
	 */
	public String getCode() {
		String sql = "select max(code) from mall_t_cdkey_stock";
		String code = queryDao.query(sql);
		if (StringUtils.isBlank(code)) {
			code = "01001001";
		} else {
			code = "0" + String.valueOf(Long.valueOf(code) + 1);
		}
		return code;
	}

	/**
	 * 根据商品ID查询对应cdkey库存
	 */
	public int getCdkeyInventoryByCommodityId(long commodityId) {
		String sqlCount = SqlJoiner.join(
				"select count(1) from mall_t_cdkey c right join mall_t_cdkey_stock s on s.id=c.stock_id and s.commodity_id=",
				String.valueOf(commodityId), " where c.is_use=0");
		Number count = queryDao.query(sqlCount);
		if (null != count) {
			return count.intValue();
		}
		return 0;
	}
}
