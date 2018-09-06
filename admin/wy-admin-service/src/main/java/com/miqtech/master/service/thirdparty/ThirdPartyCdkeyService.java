package com.miqtech.master.service.thirdparty;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.thirdparty.ThirdPartyCdkeyDao;
import com.miqtech.master.entity.thirdparty.ThirdPartyCdkey;
import com.miqtech.master.service.common.StringRedisOperateService;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.vo.PageVO;

@Component
public class ThirdPartyCdkeyService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ThirdPartyCdkeyService.class);
	private static final int MAX_WAIT_TIME = 10;// cdkey在使用中时最大等待的次数,避免死锁
	private static final int IN_USING_WAIT_TIME = 5;// cdkey使用中的等待时间
	private String REIDS_KEY_IN_USING = "wy_api_thirdparty_cdkey_using_";// 是否操作中标识:_{categoryId}

	@Autowired
	private QueryDao queryDao;
	@Autowired
	private ThirdPartyCdkeyDao thirdPartyCdkeyDao;
	@Autowired
	private StringRedisOperateService stringRedisOperateService;

	public ThirdPartyCdkey findById(Long id) {
		if (id == null) {
			return null;
		}

		return thirdPartyCdkeyDao.findOne(id);
	}

	public ThirdPartyCdkey findOneUnusedValidByCategoryId(Long categoryId) {
		if (categoryId == null) {
			return null;
		}
		return thirdPartyCdkeyDao.findFirst1ByCategoryIdAndIsUsedAndValid(categoryId, CommonConstant.INT_BOOLEAN_FALSE,
				CommonConstant.INT_BOOLEAN_TRUE);
	}

	/**
	 * 获取一个未用的cdkey,并将其置为已用
	 */
	public ThirdPartyCdkey useOneCdkeyByCategoryId(Long categoryId) {
		if (categoryId == null) {
			return null;
		}

		// 检查操作中标识,避免多个用户同时争抢同个cdkey
		int waitTime = 0;
		String redisKey = REIDS_KEY_IN_USING + categoryId;
		while (CommonConstant.INT_BOOLEAN_TRUE.toString().equals(stringRedisOperateService.getData(redisKey))
				&& ++waitTime < MAX_WAIT_TIME) {
			try {
				Thread.sleep(IN_USING_WAIT_TIME);
			} catch (Exception e) {
				LOGGER.error("等待异常:", e);
			}
		}
		stringRedisOperateService.setData(redisKey, CommonConstant.INT_BOOLEAN_TRUE.toString());

		// 取出一个当前分类下,未用的且有效的cdkey,设置为已使用
		ThirdPartyCdkey cdkey = findOneUnusedValidByCategoryId(categoryId);
		if (cdkey != null) {
			cdkey.setIsUsed(CommonConstant.INT_BOOLEAN_TRUE);
			cdkey = save(cdkey);
		}

		stringRedisOperateService.setData(redisKey, CommonConstant.INT_BOOLEAN_FALSE.toString());
		return cdkey;
	}

	public ThirdPartyCdkey save(ThirdPartyCdkey cdkey) {
		if (cdkey == null) {
			return null;
		}
		return thirdPartyCdkeyDao.save(cdkey);
	}

	public List<ThirdPartyCdkey> save(List<ThirdPartyCdkey> cdkeys) {
		if (CollectionUtils.isEmpty(cdkeys)) {
			return null;
		}
		return (List<ThirdPartyCdkey>) thirdPartyCdkeyDao.save(cdkeys);
	}

	public void delete(Long id) {
		ThirdPartyCdkey cdkey = findById(id);
		if (cdkey == null) {
			return;
		}

		cdkey.setValid(CommonConstant.INT_BOOLEAN_FALSE);
		save(cdkey);
	}

	/**
	 * 分页列表
	 */
	public PageVO page(int page, Map<String, String> searchParams) {
		String condition = " WHERE is_valid = 1";
		String totalCondition = condition;
		Map<String, Object> params = Maps.newHashMap();

		if (MapUtils.isNotEmpty(searchParams)) {
			String categoryId = MapUtils.getString(searchParams, "categoryId");
			if (NumberUtils.isNumber(categoryId)) {
				condition = SqlJoiner.join(condition, " AND category_id = ", categoryId);
				totalCondition = SqlJoiner.join(totalCondition, " AND category_id = ", categoryId);
			}
			String cdkey = MapUtils.getString(searchParams, "cdkey");
			if (StringUtils.isNotBlank(cdkey)) {
				String likeCdkey = "%" + cdkey + "%";
				params.put("cdkey", likeCdkey);
				condition = SqlJoiner.join(condition, " AND cdkey LIKE :cdkey");
				totalCondition = SqlJoiner.join(totalCondition, " AND cdkey LIKE '", likeCdkey, "'");
			}
			String isUsed = MapUtils.getString(searchParams, "isUsed");
			if (NumberUtils.isNumber(isUsed)) {
				condition = SqlJoiner.join(condition, " AND is_used = ", isUsed);
				totalCondition = SqlJoiner.join(totalCondition, " AND is_used = ", isUsed);
			}
		}

		String totalSql = SqlJoiner.join("SELECT COUNT(1) FROM third_party_cdkey", totalCondition);
		Number total = queryDao.query(totalSql);

		List<Map<String, Object>> list = null;
		if (total != null && total.intValue() > 0) {
			String limitSql = PageUtils.getLimitSql(page);
			String sql = SqlJoiner.join("SELECT id, cdkey, is_used isUsed FROM third_party_cdkey", condition, limitSql);
			list = queryDao.queryMap(sql, params);
		}

		return new PageVO(page, list, total);
	}
}
