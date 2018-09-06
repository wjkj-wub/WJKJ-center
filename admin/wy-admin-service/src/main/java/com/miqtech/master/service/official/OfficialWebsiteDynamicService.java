package com.miqtech.master.service.official;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.consts.official.OfficialWebsiteConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.official.OfficialWebsiteDynamicDao;
import com.miqtech.master.entity.website.OfficialWebsiteDynamic;
import com.miqtech.master.service.common.ObjectRedisOperateService;
import com.miqtech.master.utils.BeanUtils;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.utils.StringUtils;
import com.miqtech.master.vo.PageVO;

/**
 * 官网动态新闻、精彩活动、轮播
 */
@Component
public class OfficialWebsiteDynamicService {
	private final static Joiner JOINER = Joiner.on("_");
	@Autowired
	private OfficialWebsiteDynamicDao officialWebsiteDynamicDao;
	@Autowired
	private QueryDao queryDao;
	@Autowired
	private ObjectRedisOperateService objectRedisOperateService;

	/*
	 * 查对象
	 */
	public OfficialWebsiteDynamic findById(Long id) {
		return officialWebsiteDynamicDao.findOne(id);
	}

	/*
	 * 根据id查content
	 */
	public List<Map<String, Object>> getContentById(Long id) {
		String sqlQuery = "SELECT title, content, create_date FROM official_website_t_dynamic WHERE id = " + id;
		return queryDao.queryMap(sqlQuery);
	}

	/*
	 * 增加阅读次数
	 */
	public void addCountById(Long id) {
		String sqlUpdate = "UPDATE official_website_t_dynamic SET count=(count+1) WHERE id = " + id;
		queryDao.update(sqlUpdate);
	}

	/*
	 * 保存/更新对象
	 */
	public OfficialWebsiteDynamic save(OfficialWebsiteDynamic officialWebsiteDynamic) {
		if (null != officialWebsiteDynamic) {
			Date now = new Date();
			String id = StringUtils.EMPTY;
			if (null!=officialWebsiteDynamic.getId()) {
				id = officialWebsiteDynamic.getId().toString();
				officialWebsiteDynamic.setUpdateDate(now);
				OfficialWebsiteDynamic old = findById(officialWebsiteDynamic.getId());
				if (null!=old) {
					officialWebsiteDynamic=BeanUtils.updateBean(old, officialWebsiteDynamic);
				}
			}else {
				officialWebsiteDynamic.setCount(0);
				officialWebsiteDynamic.setValid(CommonConstant.INT_BOOLEAN_TRUE);
				officialWebsiteDynamic.setCreateDate(now);
			}
			int type = NumberUtils.toInt(officialWebsiteDynamic.getType().toString(), 0);
			if (type == 1 || type == 0) {
				operateRedis(OfficialWebsiteConstant.REDIS_OFFICIALWEBSITE_BANNER, id);
			} else if (type == 2) {
				operateRedis(OfficialWebsiteConstant.REDIS_OFFICIALWEBSITE_MATCH, id);
			} else if (type == 3) {
				operateRedis(OfficialWebsiteConstant.REDIS_OFFICIALWEBSITE_ACTIVITY, id);
			} else if (type == 4) {
				operateRedis(OfficialWebsiteConstant.REDIS_OFFICIALWEBSITE_PROFESSION, id);
			} else if (type == 5) {
				operateRedis(OfficialWebsiteConstant.REDIS_OFFICIALWEBSITE_INFORMATION, id);
			} else if (type == 6) {
				operateRedis(OfficialWebsiteConstant.REDIS_OFFICIALWEBSITE_BEFORE, id);
			}
			return officialWebsiteDynamicDao.save(officialWebsiteDynamic);
		}
		return null;
	}

	private void operateRedis(String redisKey, String id) {
		objectRedisOperateService.delData(redisKey);
		if (StringUtils.isNotBlank(id)) {
			objectRedisOperateService.delData(JOINER.join(redisKey, "content", id));
		}
	}

	/*
	 * 更改valid状态
	 */
	public void updateValidById(long id, int valid) {
		if (valid != 1) {
			valid = 0;
		}
		String sqlUpdate = "UPDATE official_website_t_dynamic SET is_valid = " + valid + " WHERE id = " + id;
		queryDao.update(sqlUpdate);
	}

	/*
	 * 后台管理：列表
	 */
	public PageVO pageList(int page, Map<String, Object> params) {
		Map<String, Object> paramsFill = Maps.newHashMap();
		if (page < 1) {
			page = 1;
		}
		int rows = PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		if (null != params.get("size")) {
			int sizeInt = NumberUtils.toInt(params.get("size").toString());
			if (sizeInt > 0) {
				rows = sizeInt;
			}
		}
		int start = (page - 1) * rows;
		paramsFill.put("start", start);
		paramsFill.put("rows", rows);
		if (null == params.get("valid")) {
			paramsFill.put("valid", CommonConstant.INT_BOOLEAN_TRUE);
		} else {
			paramsFill.put("valid", params.get("valid"));
		}
		String sqlQuery = "SELECT id, IFNULL(count,0) count, type, title, summary content, icon, is_valid, create_date FROM official_website_t_dynamic WHERE is_valid = :valid";
		String sqlCount = "SELECT COUNT(1) FROM official_website_t_dynamic WHERE is_valid = " + paramsFill.get("valid");
		if (null != params.get("type")) {
			paramsFill.put("type", params.get("type"));
			sqlQuery = SqlJoiner.join(sqlQuery, " AND type = :type");
			sqlCount = SqlJoiner.join(sqlCount, " AND type = ", paramsFill.get("type").toString());
		}
		if (null != params.get("title")) {
			paramsFill.put("title", params.get("title"));
			sqlQuery = SqlJoiner.join(sqlQuery, " AND title LIKE CONCAT('%',:title,'%')");
			sqlCount = SqlJoiner.join(sqlCount, " AND title LIKE CONCAT('%', '", paramsFill.get("title").toString(),
					"' ,'%')");
		}
		String orderBy = " ORDER BY create_date DESC";
		Object orderObj = params.get("order");
		if (null != orderObj && orderObj.toString().equals("2")) {
			orderBy = " ORDER BY count DESC, create_date DESC";
		}
		params.remove("order");
		sqlQuery = SqlJoiner.join(sqlQuery, orderBy, " LIMIT :start, :rows");
		Number total = queryDao.query(sqlCount);
		PageVO pageVO = new PageVO();
		pageVO.setList(queryDao.queryMap(sqlQuery, paramsFill));
		if (null != total) {
			pageVO.setTotal(total.intValue());
		} else {
			pageVO.setTotal(0);
		}

		return pageVO;
	}

	/*
	 * 根据类型获取列表
	 */
	public List<Map<String, Object>> getDynamicListByType(int type, Map<String, Object> params) {
		String content = StringUtils.EMPTY;
		String sqlLimit = StringUtils.EMPTY;
		if (type < 5 && type > 1) {
			sqlLimit = " LIMIT 8";
		} else if (type == 5) {
			content = ", LEFT(content,50) content";
			sqlLimit = " LIMIT 3";
		}
		String typeCondition = StringUtils.EMPTY;
		if (type == 1) {
			content = ", LEFT(content,255) content";
			typeCondition = " AND type IN (0,1)";
		} else {
			typeCondition = SqlJoiner.join(" AND type = ", String.valueOf(type));
		}
		String sqlQuery = SqlJoiner.join("SELECT id, type, icon, title", content,
				", DATE_FORMAT(create_date,'%Y-%m-%d') create_date FROM official_website_t_dynamic WHERE is_valid = 1", typeCondition,
				" ORDER BY create_date DESC", sqlLimit);
		return queryDao.queryMap(sqlQuery);
	}
}