package com.miqtech.master.service.official;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.consts.official.OfficialWebsiteConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.official.OfficialWebsiteLinkDao;
import com.miqtech.master.entity.website.OfficialWebsiteLink;
import com.miqtech.master.service.common.ObjectRedisOperateService;
import com.miqtech.master.utils.BeanUtils;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.utils.StringUtils;
import com.miqtech.master.vo.PageVO;

/**
 * 官网视频、游戏链接
 */
@Component
public class OfficialWebsiteLinkService {
	@Autowired
	private OfficialWebsiteLinkDao officialWebsiteLinkDao;
	@Autowired
	private QueryDao queryDao;
	@Autowired
	private ObjectRedisOperateService objectRedisOperateService;

	/*
	 * 查对象
	 */
	public OfficialWebsiteLink findById(Long id) {
		return officialWebsiteLinkDao.findOne(id);
	}

	/*
	 * 保存/更新对象
	 */
	public OfficialWebsiteLink save(OfficialWebsiteLink officialWebsiteLink) {
		if (null != officialWebsiteLink) {
			Date now = new Date();
			officialWebsiteLink.setUpdateDate(now);
			if (null != officialWebsiteLink.getId()) {
				OfficialWebsiteLink old = findById(officialWebsiteLink.getId());
				if (null != old) {
					officialWebsiteLink = BeanUtils.updateBean(old, officialWebsiteLink);
				}
			} else {
				officialWebsiteLink.setValid(CommonConstant.INT_BOOLEAN_TRUE);
				officialWebsiteLink.setCreateDate(now);
			}
			int type = NumberUtils.toInt(officialWebsiteLink.getType().toString(), 0);
			if (type == 1) {
				operateRedis(OfficialWebsiteConstant.REDIS_OFFICIALWEBSITE_VIDEO);
			} else if (type == 2) {
				operateRedis(OfficialWebsiteConstant.REDIS_OFFICIALWEBSITE_TOY);
			}
			return officialWebsiteLinkDao.save(officialWebsiteLink);
		}
		return null;
	}

	private void operateRedis(String redisKey) {
		objectRedisOperateService.delData(redisKey);
	}

	/*
	 * 更改valid状态
	 */
	public void updateValidById(long id, int valid) {
		if (valid != 1) {
			valid = 0;
		}
		String sqlUpdate = "UPDATE official_website_t_link SET is_valid = " + valid + " WHERE id = " + id;
		queryDao.update(sqlUpdate);
	}

	/*
	 * 后台管理：列表
	 */
	public PageVO pageList(int page, Map<String, Object> paramsSearch) {
		if (page < 1) {
			page = 1;
		}
		int rows = PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		Object sizeObj = paramsSearch.get("size");
		if (null != sizeObj ) {
			int size =  NumberUtils.toInt(sizeObj.toString()) ;
			if (size>=0) {
				rows = size;
			}
		}
		int start = (page - 1) * rows;
		Map<String, Object> params = Maps.newHashMap();
		params.put("start", start);
		params.put("rows", rows);
		if (null != paramsSearch.get("valid")) {
			params.put("valid", paramsSearch.get("valid"));
		} else {
			params.put("valid", CommonConstant.INT_BOOLEAN_TRUE);
		}
		String sqlQuery = "SELECT id, type, title, url, icon, is_valid, create_date FROM official_website_t_link WHERE is_valid = :valid";
		String sqlCount = "SELECT COUNT(1) FROM official_website_t_link WHERE is_valid = " + params.get("valid");
		if (null != paramsSearch.get("type")) {
			params.put("type", paramsSearch.get("type"));
			sqlQuery = SqlJoiner.join(sqlQuery, " AND type = :type");
			sqlCount = SqlJoiner.join(sqlCount, " AND type = ", paramsSearch.get("type").toString());
		}
		if (null != paramsSearch.get("title")) {
			params.put("title", paramsSearch.get("title"));
			sqlQuery = SqlJoiner.join(sqlQuery, " AND title LIKE CONCAT('%',:title,'%')");
			sqlCount = SqlJoiner.join(sqlCount, " AND title LIKE CONCAT('%', '", paramsSearch.get("title").toString(),
					"' ,'%')");
		}
		sqlQuery = SqlJoiner.join(sqlQuery, " ORDER BY create_date DESC LIMIT :start, :rows");
		Number total = queryDao.query(sqlCount);
		PageVO pageVO = new PageVO();
		pageVO.setList(queryDao.queryMap(sqlQuery, params));
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
	public List<Map<String, Object>> getLinkListByType(int type, Map<String, Object> params) {
		String sqlOrderAndLimit = StringUtils.EMPTY;
		if (type == 1) {
			sqlOrderAndLimit = " ORDER BY create_date DESC LIMIT 6";
		}
		String sqlQuery = SqlJoiner.join(
				"SELECT id, type, icon, title, url, create_date FROM official_website_t_link WHERE is_valid = 1 AND type = ",
				String.valueOf(type), sqlOrderAndLimit);
		return queryDao.queryMap(sqlQuery);
	}
}