package com.miqtech.master.service.pc.lol;

import com.google.common.collect.Maps;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.utils.SqlJoiner;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class LolHeroRoleService {

	@Autowired
	private QueryDao queryDao;

	public List<Map<String, Object>> getLolHeroRoleListByTag(String tagCn) {
		Map<String, Object> params = Maps.newHashMap();
		String condition = " pc_lol_hero_role.is_valid = 1";
		if (StringUtils.isNotBlank(tagCn)) {
			condition = SqlJoiner.join(condition, " AND pc_lol_hero_tag.tag_cn = :tagCn");
			params.put("tagCn", tagCn);
		}

		String sql = SqlJoiner.join("SELECT pc_lol_hero_role.*,pc_lol_hero_tag.tag_cn", " FROM pc_lol_hero_role",
				" LEFT JOIN pc_lol_hero_tag ON pc_lol_hero_role.id = pc_lol_hero_tag.hero_id AND pc_lol_hero_tag.is_valid = 1",
				" WHERE", condition, " group by pc_lol_hero_role.id");
		return queryDao.queryMap(sql, params);
	}

	/**
	 * 根据英雄ID获取英雄头像
	 */
	public String getHeroIconById(Long id) {
		if (id == null) {
			return null;
		}

		String sql = SqlJoiner.join("SELECT icon FROM pc_lol_hero_role", " WHERE is_valid=1 AND pc_lol_hero_role.id =",
				id.toString());
		Map<String, Object> map = queryDao.querySingleMap(sql);
		return MapUtils.getString(map, "icon");
	}
}
