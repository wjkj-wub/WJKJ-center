package com.miqtech.master.service;

import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 处理分页数据 service
 *
 * @author zhangyuqi
 * @create 2017年10月18日
 */
@Service
public class PagingService {
	@Autowired
	private QueryDao queryDao;

	/**
	 * 获取总数量
	 */
	protected Number getTotal(String sql) {
		return queryDao.query(sql);
	}

	/**
	 * 根据总数量，当前页，List<Object>列表获取分页后的map
	 */
	protected Map<String, Object> getPagingMapWithObjectList(Number total, Integer page, List<Object> objectList) {
		return this.getPagingMap(null, null, total, page, null, objectList);
	}

	/**
	 * 根据总数量，当前页，List<Map<String, Object>>获取分页后的map
	 */
	protected Map<String, Object> getPagingMap(Number total, Integer page, List<Map<String, Object>> list) {
		return this.getPagingMap(null, null, total, page, list, null);
	}

	/**
	 * 根据查询总数sql，查询list sql，当前页获取分页后的map
	 */
	protected Map<String, Object> getPagingMap(String totalSql, String sql, Integer page) {
		return this.getPagingMap(totalSql, sql, null, page, null, null);
	}

	/**
	 * 获取分页后的map
	 */
	private Map<String, Object> getPagingMap(String totalSql, String sql, Number total, Integer page,
			List<Map<String, Object>> list, List<Object> objectList) {
		Map<String, Object> map = new HashMap<>(16);

		if (StringUtils.isNotBlank(totalSql)) {
			total = getTotal(totalSql);
			if (total.intValue() > 0 && StringUtils.isNotBlank(sql)) {
				list = queryDao.queryMap(sql);
			}
		}
		total = total == null ? 0 : total;
		map.put("total", total);
		map.put("currentPage", page);
		map.put("isLast", PageUtils.isBottom(page, total.longValue()));
		if (!CollectionUtils.isEmpty(objectList)) {
			map.put("list", objectList);
		} else {
			map.put("list", list);
		}
		return map;
	}

}
