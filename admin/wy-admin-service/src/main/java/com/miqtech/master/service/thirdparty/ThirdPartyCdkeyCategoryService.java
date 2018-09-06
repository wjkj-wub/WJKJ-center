package com.miqtech.master.service.thirdparty;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.thirdparty.ThirdPartyCdkeyCategoryDao;
import com.miqtech.master.entity.thirdparty.ThirdPartyCdkeyCategory;
import com.miqtech.master.utils.BeanUtils;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.vo.PageVO;

@Component
public class ThirdPartyCdkeyCategoryService {

	@Autowired
	private QueryDao queryDao;
	@Autowired
	private ThirdPartyCdkeyCategoryDao thirdPartyCdkeyCategoryDao;

	public ThirdPartyCdkeyCategory findById(Long id) {
		if (id == null) {
			return null;
		}

		return thirdPartyCdkeyCategoryDao.findOne(id);
	}

	public List<ThirdPartyCdkeyCategory> findValidByName(String name) {
		if (StringUtils.isBlank(name)) {
			return null;
		}
		return thirdPartyCdkeyCategoryDao.findByNameAndValid(name, CommonConstant.INT_BOOLEAN_TRUE);
	}

	public ThirdPartyCdkeyCategory save(ThirdPartyCdkeyCategory category) {
		if (category == null) {
			return null;
		}
		return thirdPartyCdkeyCategoryDao.save(category);
	}

	public ThirdPartyCdkeyCategory saveOrUpdate(ThirdPartyCdkeyCategory category) {
		if (category == null) {
			return null;
		}

		Date now = new Date();
		category.setUpdateDate(now);
		if (category.getId() != null) {
			ThirdPartyCdkeyCategory old = findById(category.getId());
			if (old != null) {
				category = BeanUtils.updateBean(old, category);
			}
		} else {
			category.setValid(CommonConstant.INT_BOOLEAN_TRUE);
			category.setCreateDate(now);
		}

		return save(category);
	}

	/**
	 * 分页列表
	 */
	public PageVO page(int page, Map<String, String> searchParams) {
		String condition = " WHERE is_valid = 1";
		String totalCondition = condition;

		Map<String, Object> params = Maps.newHashMap();
		if (MapUtils.isNotEmpty(searchParams)) {
			String name = MapUtils.getString(searchParams, "name");
			if (StringUtils.isNotBlank(name)) {
				String likeName = "%" + name + "%";
				params.put("name", likeName);
				condition = SqlJoiner.join(condition, " AND name LIKE :name");
				totalCondition = SqlJoiner.join(totalCondition, " AND name LIKE '", likeName, "'");
			}
		}

		String totalSql = SqlJoiner.join("SELECT COUNT(1) FROM third_party_cdkey_category tpcc", totalCondition);
		Number total = queryDao.query(totalSql);

		List<Map<String, Object>> list = null;
		if (total != null && total.intValue() > 0) {
			String limitSql = PageUtils.getLimitSql(page);
			String sql = SqlJoiner.join("SELECT * FROM third_party_cdkey_category tpcc", condition, limitSql);
			list = queryDao.queryMap(sql, params);
		}

		return new PageVO(page, list, total);
	}
}
