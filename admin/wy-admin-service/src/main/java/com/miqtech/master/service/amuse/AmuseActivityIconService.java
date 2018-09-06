package com.miqtech.master.service.amuse;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.amuse.AmuseActivityIconDao;
import com.miqtech.master.entity.amuse.AmuseActivityIcon;

/**
 * 娱乐赛Icon操作service
 */
@Component
public class AmuseActivityIconService {
	@Autowired
	private AmuseActivityIconDao activityIconDao;
	@Autowired
	private QueryDao queryDao;

	/**
	 * 保存
	 */
	public void save(AmuseActivityIcon activityIcon) {
		if (activityIcon != null) {
			activityIconDao.save(activityIcon);
		}
	}

	/**
	 * 根据ID查实体
	 */
	public AmuseActivityIcon findById(Long id) {
		return activityIconDao.findOne(id);
	}

	/**
	 * 根据活动ID查主图路径
	 */
	public String getMainIconByActivityId(long activityId) {
		String sqlQuery = "select icon from amuse_r_activity_icon where is_valid=1 and is_main=1 and activity_id="
				+ activityId;
		Map<String, Object> map = queryDao.querySingleMap(sqlQuery);
		if (!Collections.EMPTY_MAP.equals(map)) {
			if (null!=map.get("icon")) {
				return map.get("icon").toString();
			}
		}
		return StringUtils.EMPTY;
	}

}
