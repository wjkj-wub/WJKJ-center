package com.miqtech.master.service.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.service.activity.ActivityInfoService;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.vo.PageVO;

@Component
public class AthleticsService {
	@Autowired
	private QueryDao queryDao;

	/**竞技大厅卡片转动区
	 * @param areaCode
	 * @return
	 */
	public List<Map<String, Object>> queryAthleticsRecomend(String areaCode) {
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		String sql = "select id,type,target_id from index_hot where is_valid=1 and area_code=000000 and (type=4 or type=5) order by sort desc limit 0,10";
		if (StringUtils.isBlank(areaCode)) {
			List<Map<String, Object>> ids = queryDao.queryMap(sql);
			if (CollectionUtils.isNotEmpty(ids)) {
				return getAthleticsRecomend(ids, result);
			}
		} else {
			sql = "select id,type,target_id from index_hot where is_valid=1 and area_code like '" + areaCode
					+ "%' and (type=4 or type=5) order by sort desc limit 0,4";
			List<Map<String, Object>> ids = queryDao.queryMap(sql);
			if (CollectionUtils.isNotEmpty(ids)) {
				result = getAthleticsRecomend(ids, result);
			}
			int size = 10 - result.size();
			sql = "select id,type,target_id from index_hot where is_valid=1 and area_code=000000 and (type=4 or type=5) order by sort desc limit 0,"
					+ size;
			ids = queryDao.queryMap(sql);
			if (CollectionUtils.isNotEmpty(ids)) {
				result = getAthleticsRecomend(ids, result);
			}
		}
		return result;
	}

	public List<Map<String, Object>> getAthleticsRecomend(List<Map<String, Object>> ids,
			List<Map<String, Object>> result) {
		String sql = "";
		Map<String, Object> m = null;
		for (Map<String, Object> map : ids) {
			if (map.get("type").equals(4)) {
				sql = "select a.id,1 type,1 way,a.title,a.icon,a.start_time,a.end_time,count(b.id) applyNum,a.people_num max_num,a.summary from activity_t_info a left join activity_t_member b on a.id=b.activity_id and b.is_valid=1 where a.id="
						+ map.get("target_id") + " and a.is_valid=1 group by a.id";
				m = queryDao.querySingleMap(sql);
				if (m != null) {
					result.add(queryDao.querySingleMap(sql));
				}
			} else if (map.get("type").equals(5)) {
				sql = "select a.id,2 type,a.way,a.title,c.icon,a.start_date start_time,a.end_date end_time,(count(b.id) + IFNULL(a.virtual_apply,0)) applyNum,a.max_num,a.summary from (amuse_t_activity a,amuse_r_activity_icon c) left join amuse_r_activity_record b on a.id=b.activity_id and b.is_valid=1 and b.state in(0,1) where a.id="
						+ map.get("target_id")
						+ " and a.is_valid=1 and a.id=c.activity_id and c.is_main=1 and c.is_valid=1 group by a.id";
				m = queryDao.querySingleMap(sql);
				if (m != null) {
					result.add(queryDao.querySingleMap(sql));
				}
			}
		}
		return result;
	}

	/**官方或娱乐赛推荐
	 * @param areaCode
	 * @param type
	 * @return
	 */
	public Map<String, Object> activityRecommend(String areaCode, Integer type) {
		Map<String, Object> result = new HashMap<String, Object>();
		String sql = "";
		if (StringUtils.isBlank(areaCode)) {
			areaCode = "000000";
		}
		if (type == 1) {
			sql = SqlJoiner
					.join("select b.id,b.title,b.icon,b.start_time from (index_hot a,activity_t_info b) where a.is_valid=1 and a.type=6 and a.target_id=b.id and a.area_code like '",
							areaCode, "%'", " order by a.sort desc");
			List<Map<String, Object>> list = queryDao.queryMap(sql);
			if (list.isEmpty()) {
				sql = SqlJoiner
						.join("select b.id,b.title,b.icon,b.start_time from (index_hot a,activity_t_info b) where a.is_valid=1 and a.type=6 and a.target_id=b.id and a.area_code=000000 ",
								" order by a.sort desc");
				list = queryDao.queryMap(sql);
			}
			if (CollectionUtils.isNotEmpty(list)) {
				result = list.get(0);
			}
		} else if (type == 2) {
			sql = SqlJoiner
					.join("select b.id,b.title,c.icon mainIcon,b.start_date startDate from (index_hot a,amuse_t_activity b,amuse_r_activity_icon c) where a.is_valid=1 and a.type=7 and a.target_id=b.id and b.id=c.activity_id and c.is_main=1 and c.is_valid=1 and a.area_code like '",
							areaCode, "%'", " order by a.sort desc");
			List<Map<String, Object>> list = queryDao.queryMap(sql);
			if (list.isEmpty()) {
				sql = SqlJoiner
						.join("select b.id,b.title,c.icon mainIcon,b.start_date startDate from (index_hot a,amuse_t_activity b,amuse_r_activity_icon c) where a.is_valid=1 and a.type=7 and a.target_id=b.id and b.id=c.activity_id and c.is_main=1 and c.is_valid=1 and a.area_code=000000 ",
								" order by a.sort desc");
				list = queryDao.queryMap(sql);
			}
			if (CollectionUtils.isNotEmpty(list)) {
				result = list.get(0);
			}
		}
		return result;
	}

	/**最新活动
	 * @param page
	 * @param pageSize
	 * @param lastTotal
	 * @param isLimit
	 * @return
	 */
	public PageVO latestActivity(Integer page, Integer pageSize, Integer lastTotal, String areaCode) {
		String sql = "";
		String limitSql = " limit :start,:pageSize";
		PageVO vo = new PageVO();
		String areaCodeSql = "";
		if (!areaCode.equals("000000")) {
			areaCodeSql = " and locate('" + areaCode + "',areas) ";
		}
		if (page == null) {
			sql = "select a.id,a.title,a.icon,a.start_time,a.end_time,applyNum,a.summary,a.way,"
					+ ActivityInfoService.activityStatusSql
					+ " from activity_t_info a left join (	SELECT  activity_id,count(activity_id) AS applyNum FROM activity_t_member WHERE is_valid = 1 	GROUP BY 	activity_id) b on a.id=b.activity_id   where a.is_valid=1 and a.is_ground = 1 "
					+ areaCodeSql + " order by find_in_set(status, '" + ActivityInfoService.activityStatusSort
					+ "') , sort_num desc,a.create_date desc limit 0,10";
			vo.setList(queryDao.queryMap(sql));
		} else {
			Map<String, Object> params = new HashMap<String, Object>();
			if (pageSize == null) {
				pageSize = PageUtils.API_DEFAULT_PAGE_SIZE;
			}
			params.put("pageSize", pageSize);
			sql = "select count(1) from activity_t_info a where a.is_valid=1 and a.is_ground = 1";
			Number total = queryDao.query(sql);
			if (lastTotal == null) {
				lastTotal = total.intValue();
			}
			if (total != null) {
				vo.setTotal(total.intValue());
				if (1 == page) {
					params.put("start", 0);
				} else if (1 < page) {
					params.put("start", (page - 1) * pageSize + total.intValue() - lastTotal);
				}
				if (page * pageSize >= total.intValue()) {
					vo.setIsLast(1);
				}
			}
			sql = "select a.id,a.title,a.icon,a.start_time,a.end_time,applyNum,a.summary,a.way,"
					+ ActivityInfoService.activityStatusSql
					+ " from activity_t_info a left join (	SELECT  activity_id,count(activity_id) AS applyNum FROM activity_t_member WHERE is_valid = 1 	GROUP BY 	activity_id) b on a.id=b.activity_id   where a.is_valid=1 and a.is_ground = 1  order by find_in_set(status, '"
					+ ActivityInfoService.activityStatusSort + "') , sort_num desc,a.create_date desc " + limitSql;
			vo.setList(queryDao.queryMap(sql, params));
		}
		return vo;
	}

	/**最新发布约战
	 */
	public PageVO latestBattle(Integer page, Integer pageSize, Integer lastTotal, String areaCode, Integer scope,
			Integer status, Integer category) {
		String sql = "";
		String limitSql = " limit :start,:pageSize";
		String scopeSql = "";
		String statusSql = "";
		String categorySql = "";
		if (StringUtils.isBlank(areaCode)) {
			scopeSql = " and (way=1) ";
		} else {
			if (scope == null) {
				scopeSql = " and (way=2 and b.area_code like '" + areaCode + "%' or way=1)";
			} else if (scope == 0) {
				scopeSql = " and (way=2 and b.area_code like '" + areaCode + "%')";
			} else if (scope == 1) {
				scopeSql = " and (way=1) ";
			}
		}
		if (status != null) {
			statusSql = " and (case when (if(a.block_time is not null,now()<a.block_time,now()<a.begin_time)) then 0 when now()>a.begin_time then 1 else 2 end)="
					+ status;
		}
		if (category != null) {
			categorySql = " and a.item_id=" + category;
		}
		PageVO vo = new PageVO();
		if (page == null) {
			sql = SqlJoiner
					.join("SELECT b.name netbar_name,d.name item_name,c.id releaser_id, c.icon releaser_icon, c.nickname,c.sex, a.id, a.server, a.begin_time, a.way, (select count(1) from activity_r_match_apply where is_valid = 1 and match_id=a.id) apply_count, a.people_num, CASE WHEN ( IF ( a.block_time IS NOT NULL, now() < a.block_time, now() < a.begin_time )) THEN 0 WHEN now() > a.begin_time THEN 1 ELSE 2 END status FROM (activity_t_matches a) LEFT JOIN netbar_t_info b ON a.netbar_id = b.id LEFT JOIN user_t_info c ON a.user_id = c.id LEFT JOIN activity_r_items d ON a.item_id = d.id  WHERE a.is_valid = 1 ",
							scopeSql, statusSql, categorySql, " GROUP BY a.id ORDER BY a.id DESC limit 0,10");
			vo.setList(queryDao.queryMap(sql));
		} else {
			Map<String, Object> params = new HashMap<String, Object>();
			if (pageSize == null) {
				pageSize = PageUtils.API_DEFAULT_PAGE_SIZE;
			}
			params.put("pageSize", pageSize);
			sql = "select count(1) from activity_t_matches a left join netbar_t_info b on a.netbar_id=b.id where a.is_valid=1"
					+ scopeSql + statusSql + categorySql;
			Number total = queryDao.query(sql);
			if (total != null) {
				vo.setTotal(total.intValue());
				if (1 == page) {
					params.put("start", 0);
				} else if (1 < page) {
					params.put("start", (page - 1) * pageSize + total.intValue() - lastTotal);
				}
				if (page * pageSize >= total.intValue()) {
					vo.setIsLast(1);
				}
			}
			sql = SqlJoiner
					.join("SELECT b.name netbar_name,d.name item_name, c.icon releaser_icon,c.id releaser_id, c.nickname,c.sex, a.id, a.server, a.begin_time, a.way, (select count(1) from activity_r_match_apply where is_valid = 1 and match_id=a.id) apply_count, a.people_num, CASE WHEN ( IF ( a.block_time IS NOT NULL, now() < a.block_time, now() < a.begin_time )) THEN 0 WHEN now() > a.begin_time THEN 1 ELSE 2 END status FROM (activity_t_matches a) LEFT JOIN netbar_t_info b ON a.netbar_id = b.id LEFT JOIN user_t_info c ON a.user_id = c.id LEFT JOIN activity_r_items d ON a.item_id = d.id WHERE a.is_valid = 1 ",
							scopeSql, statusSql, categorySql, " GROUP BY a.id ORDER BY a.id DESC", limitSql);
			vo.setList(queryDao.queryMap(sql, params));
		}
		return vo;
	}
}
