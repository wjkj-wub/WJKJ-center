package com.miqtech.master.service.amuse;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.amuse.AmuseAwardTypeDao;
import com.miqtech.master.entity.amuse.AmuseAwardType;

/**
 * 娱乐赛奖品类型service
 */
@Component
public class AmuseAwardTypeService {
	@Autowired
	private AmuseAwardTypeDao amuseAwardTypeDao;
	@Autowired
	private QueryDao queryDao;

	/**
	 * 保存
	 */
	public void save(AmuseAwardType amuseAwardType) {
		if (amuseAwardType != null) {
			amuseAwardTypeDao.save(amuseAwardType);
		}
	}

	/**
	 * 根据ID查实体
	 */
	public AmuseAwardType findById(Long id) {
		return amuseAwardTypeDao.findOne(id);
	}

	/**
	 * API3.0,获取全部奖品类型
	 */
	public List<Map<String, Object>> getAllAwardTypeList() {
		String sqlQuery = "select type awardtype, name from amuse_r_award_type where is_valid=1 group by type";
		return queryDao.queryMap(sqlQuery);
	}

}
