package com.miqtech.master.service.mall;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.mall.MallCdkeyDao;
import com.miqtech.master.entity.mall.MallCdkey;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.vo.PageVO;

@Component
public class MallCdkeyService {
	@Autowired
	private MallCdkeyDao mallCdkeyDao;
	@Autowired
	private QueryDao queryDao;

	/**列表查询
	 * @param phone
	 * @param cdkey
	 * @param isUse
	 * @param page
	 * @return
	 */
	public PageVO queryList(String phone, String cdkey, Integer isUse, Integer page) {
		String sql = "";
		String cdkeySql = "";
		String phoneSql = "";
		String isUseSql = "";
		if (StringUtils.isNotBlank(cdkey)) {
			cdkeySql = " and cdkey like '%" + cdkey + "%'";
		}
		if (StringUtils.isNotBlank(phone)) {
			phoneSql = " and b.username like '%" + phone + "%'";
		}
		if (isUse != null) {
			isUseSql = " and a.is_use=" + String.valueOf(isUse);
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
		sql = SqlJoiner
				.join("select count(1) from mall_t_cdkey a left join mall_r_commodity_history z on a.history_id=z.id left join user_t_info b on z.user_id=b.id left join mall_t_commodity c on a.item_id=c.id where a.is_valid=1",
						cdkeySql, phoneSql, isUseSql);
		PageVO vo = new PageVO();
		Number totalCount = queryDao.query(sql);
		if (totalCount != null) {
			vo.setTotal(totalCount.intValue());
		}

		if (start + pageSize >= vo.getTotal()) {
			vo.setIsLast(1);
		}
		sql = SqlJoiner
				.join("select a.*,b.username phone,c.name from mall_t_cdkey a left join mall_r_commodity_history z on a.history_id=z.id left join user_t_info b on z.user_id=b.id left join mall_t_commodity c on a.item_id=c.id where a.is_valid=1",
						cdkeySql, phoneSql, isUseSql, limitSql);
		vo.setCurrentPage(page);
		vo.setList(queryDao.queryMap(sql, params));
		return vo;

	}

	public MallCdkey findById(Long id) {
		return mallCdkeyDao.findOne(id);

	}

	public void save(MallCdkey cdkey) {
		mallCdkeyDao.save(cdkey);
	}

	public void del(Long id) {
		mallCdkeyDao.delete(id);
	}

	/**查询使用和未使用的cdkey
	 * @param stockId
	 * @param isUse
	 * @return
	 */
	public PageVO queryByIsUse(String stockId, String isUse, Integer page) {
		int pageSize = PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		if (page == null) {
			page = 1;
		}
		int start = (page - 1) * pageSize;
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("start", start);
		params.put("pageSize", pageSize);
		String limitSql = " limit :start,:pageSize";
		String sql = SqlJoiner.join("select count(1) from mall_t_cdkey a where a.stock_id=", stockId, " and a.is_use=",
				isUse);
		PageVO vo = new PageVO();
		Number totalCount = queryDao.query(sql);
		if (totalCount != null) {
			vo.setTotal(totalCount.intValue());
		}
		sql = SqlJoiner.join(
				"select a.cdkey,a.begin_time,a.end_time,a.create_date from mall_t_cdkey a where a.stock_id=",
				String.valueOf(stockId), " and a.is_use=", isUse, " order by a.create_date desc ", limitSql);
		vo.setCurrentPage(page);
		vo.setList(queryDao.queryMap(sql, params));
		return vo;
	}
}
