package com.miqtech.master.service.common;

import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.common.AppDynamicEntryDao;
import com.miqtech.master.entity.Pager;
import com.miqtech.master.entity.common.AppDynamicEntry;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.vo.PageVO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 热门推荐信息接口
 *
 */
@Component
public class IndexService {
	@Autowired
	private AppDynamicEntryDao appDynamicEntryDao;
	@Autowired
	private QueryDao queryDao;
	@Autowired
	private IndexMidImgService indexMidImgService;

	/**
	 * 动态入口
	 * 
	 * @param isShow
	 * @return
	 */
	public List<AppDynamicEntry> findByIsShow(Integer isShow) {
		return appDynamicEntryDao.findByIsShowOrderBySortDesc(1);
	}

	/**
	 * 热门赛事
	 * 
	 * @param areaCode
	 * @return
	 */
	public List<Map<String, Object>> queryHotActivity(String areaCode) {
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		String sql = "";
		String citySql = "";
		String areaCodeSql = " and a.area_code=000000";
		String limitSql = " limit :start,:size";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("start", 0);
		params.put("size", 4);
		sql = SqlJoiner
				.join("SELECT * FROM ( SELECT a.sort,a.type, b.id, b.icon, count(c.id) applyNum, b.people_num max_num, b.title, b.start_time,way,null server,null netbar_name FROM ( index_hot a, activity_t_info b ) LEFT JOIN activity_t_member c ON b.id = c.activity_id and c.is_valid=1 WHERE a.is_valid=1 and a.type = 1 AND a.target_id = b.id ",
						areaCodeSql,
						" GROUP BY b.id UNION SELECT a.sort, a.type, b.id, d.icon, (if(count(c.id)+b.virtual_apply>b.max_num,b.max_num,count(c.id)+b.virtual_apply)) applyNum, b.max_num, b.title, b.start_date start_time,way,b.server,e.name netbar_name FROM ( index_hot a, amuse_t_activity b, amuse_r_activity_icon d ) LEFT JOIN amuse_r_activity_record c ON b.id = c.activity_id left join netbar_t_info e on e.id=b.netbar_id WHERE a.is_valid=1 and a.type = 2 AND a.target_id = b.id AND d.activity_id = a.target_id AND d.is_main = 1 and c.is_valid=1 and c.state in(0,1) ",
						areaCodeSql, " GROUP BY b.id ) a ORDER BY a.sort DESC", limitSql);
		if (StringUtils.isBlank(areaCode)) {
			result = queryDao.queryMap(sql, params);
		} else {
			areaCodeSql = SqlJoiner.join(" and a.area_code like '", areaCode, "%'");
			citySql = SqlJoiner
					.join("SELECT * FROM ( SELECT a.sort,a.type, b.id, b.icon, count(c.id) applyNum, b.people_num max_num, b.title, b.start_time,way, null server,null netbar_name FROM ( index_hot a, activity_t_info b ) LEFT JOIN activity_t_member c ON b.id = c.activity_id and c.is_valid=1 WHERE a.is_valid=1 and a.type = 1 AND a.target_id = b.id and c.is_valid=1 ",
							areaCodeSql,
							" GROUP BY b.id UNION SELECT a.sort, a.type, b.id, d.icon, (if(count(c.id)+b.virtual_apply>b.max_num,b.max_num,count(c.id)+b.virtual_apply)) applyNum, b.max_num, b.title, b.start_date start_time,way,b.server,e.name netbar_name FROM ( index_hot a, amuse_t_activity b, amuse_r_activity_icon d ) LEFT JOIN amuse_r_activity_record c ON b.id = c.activity_id left join netbar_t_info e on e.id=b.netbar_id WHERE a.is_valid=1 and a.type = 2 AND a.target_id = b.id AND d.activity_id = a.target_id AND d.is_main = 1 and c.is_valid=1 and c.state in(0,1) ",
							areaCodeSql, " GROUP BY b.id) a ORDER BY a.sort DESC", limitSql);
			result.addAll(queryDao.queryMap(citySql, params));
			if (result.size() < 4) {
				params.put("size", 4 - result.size());
				result.addAll(queryDao.queryMap(sql, params));
			}
		}
		return result;
	}

	/**
	 * 热门约战
	 * 
	 * @param areaCode
	 * @return
	 */
	public List<Map<String, Object>> queryHotBattle(String areaCode) {
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		String sql = "";
		// String areaCodeSql = "";
		String limitSql = " limit :start,:size";
		int size = 5;
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("start", 0);
		params.put("size", 5);
		if (StringUtils.isBlank(areaCode)) {
			sql = SqlJoiner
					.join("SELECT 3 type, b.id,(select count(1) from activity_r_match_apply where is_valid=1 and match_id=b.id) applyNum, b.people_num max_num, b.begin_time start_time, d. NAME title, b.server, e.nickname, e.icon, e.id user_id, e.sex, f. NAME netbar_name,if(b.way=1,2,1) way FROM ( activity_t_matches b, activity_r_items d, user_t_info e )  LEFT JOIN netbar_t_info f ON b.netbar_id = f.id WHERE b.is_valid=1 and b.item_id = d.id AND b.user_id = e.id AND way = 1 AND b.begin_time > now() GROUP BY b.id ORDER BY applyNum DESC ",
							limitSql);
			result = queryDao.queryMap(sql, params);
		} else {
			// areaCodeSql = SqlJoiner.join(" and a.area_code like '", areaCode,
			// "%'");
			// 暂时不用 sql = SqlJoiner
			// .join("SELECT a.sort, a.type, b.id, count(c.id) applyNum, b.people_num max_num, b.begin_time start_time, d. NAME title, b.server, e.nickname, e.icon,e.id user_id,e.sex,IF(b.netbar_id is null,2,1) way,f.name netbar_name FROM ( index_hot a, activity_t_matches b, activity_r_items d, user_t_info e ) LEFT JOIN activity_r_match_apply c ON b.id = c.match_id left join netbar_t_info f on b.netbar_id=f.id WHERE a.is_valid=1 and a.type = 3 AND a.target_id = b.id AND d.id = b.item_id AND e.id = b.user_id ",
			// areaCodeSql, " GROUP BY c.match_id");
			// result.addAll(queryDao.queryMap(sql));
			if (result.size() < size) {
				sql = SqlJoiner
						.join("SELECT 3 type, b.id,(select count(1) from activity_r_match_apply where is_valid=1 and match_id=b.id) applyNum, b.people_num max_num, b.begin_time start_time, d. NAME title, b.server, e.nickname, e.icon, e.id user_id, e.sex, f. NAME netbar_name,if(b.way=1,2,1) way FROM ( activity_t_matches b, activity_r_items d, user_t_info e ) LEFT JOIN netbar_t_info f ON b.netbar_id = f.id WHERE b.is_valid=1 and b.item_id = d.id AND b.user_id = e.id  AND (f.area_code LIKE '",
								areaCode, "%' or way=1) AND b.begin_time > now() GROUP BY b.id ORDER BY applyNum DESC",
								limitSql);
				params.put("size", size - result.size());
				result.addAll(queryDao.queryMap(sql, params));
			}
		}
		return result;
	}

	/**
	 * 腰图1
	 * 
	 * @return
	 */
	public Map<String, Object> queryMid1(String areaCode) {
		Map<String, Object> result = new HashMap<String, Object>();
		if (StringUtils.isBlank(areaCode)) {
			areaCode = "000000";
		}
		String sql = "";
		List<Map<String, Object>> list;
		list = indexMidImgService.queryByCategoryAndAreaCode(1, areaCode);
		if (CollectionUtils.isEmpty(list)) {
			list = indexMidImgService.queryByCategoryAndAreaCode(1, "000000");
		}
		if (list.size() > 0) {
			Map<String, Object> map = list.get(0);
			String type = map.get("type").toString();
			String id = map.get("target_id").toString();
			// 官方赛
			if (type.equals("1")) {
				sql = "SELECT 1 type,b.id,a.category, b.icon, count(c.id) applyNum, b.title, b.start_time, 1 way FROM ( index_mid_img a, activity_t_info b ) LEFT JOIN activity_t_member c ON b.id = c.activity_id AND c.is_valid = 1 WHERE a.category = 1 AND a.type = 1 AND a.target_id = b.id and a.target_id ="
						+ id;
				result.put("match", queryDao.querySingleMap(sql));
				sql = "SELECT b.icon FROM ( activity_t_member a, user_t_info b ) WHERE a.is_valid = 1 AND a.user_id = b.id AND a.activity_id ="
						+ ((Number) map.get("target_id")).longValue() + " ORDER BY a.create_date LIMIT 0, 5";
				result.put("icons", queryDao.queryMap(sql));
			} else if (type.equals("2")) {
				sql = "SELECT 2 type,b.id,a.category, d.icon, count(c.id) applyNum, b.title, b.start_date start_time, b.way FROM ( index_mid_img a, amuse_t_activity b, amuse_r_activity_icon d ) LEFT JOIN amuse_r_activity_record c ON b.id = c.activity_id AND c.is_valid = 1 WHERE a.category = 1 AND a.type = 2 AND a.target_id = b.id AND a.target_id = d.activity_id AND d.is_main = 1 and a.target_id ="
						+ id;
				result.put("match", queryDao.querySingleMap(sql));
				sql = "SELECT b.icon FROM ( amuse_r_activity_record a, user_t_info b ) WHERE a.is_valid = 1 AND a.user_id = b.id AND a.activity_id = "
						+ ((Number) map.get("target_id")).longValue() + " ORDER BY a.create_date LIMIT 0, 5";
				result.put("icons", queryDao.queryMap(sql));
			}
		}
		return result;

	}

	/**
	 * 腰图2
	 * 
	 * @return
	 */
	public Map<String, Object> queryMid2(String areaCode) {
		Map<String, Object> result = new HashMap<String, Object>();
		if (StringUtils.isBlank(areaCode)) {
			areaCode = "00";
		}
		String sql = "";
		List<Map<String, Object>> list;
		list = indexMidImgService.queryByCategoryAndAreaCode(2, areaCode);
		if (CollectionUtils.isEmpty(list)) {
			list = indexMidImgService.queryByCategoryAndAreaCode(2, "000000");
		}
		if (list.size() > 0) {
			Map<String, Object> map = list.get(0);
			String type = map.get("type").toString();
			if (type.equals("1")) {
				String id = map.get("target_id").toString();
				sql = SqlJoiner.join("select id,icon,1 type from activity_t_info where id=", id);
				result = queryDao.querySingleMap(sql);
			} else if (type.equals("2")) {
				String id = map.get("target_id").toString();
				sql = SqlJoiner
						.join("select a.id,b.icon,2 type from (amuse_t_activity a,amuse_r_activity_icon b) where a.id=b.activity_id and b.is_main=1 and b.is_valid=1 and a.id=",
								id);
				result = queryDao.querySingleMap(sql);
			} else {
				result.put("icon", map.get("img"));
				result.put("type", map.get("type"));
				result.put("url", map.get("url"));
			}
		}
		return result;
	}

	/**
	 * 搜索赛事
	 * 
	 * @param activityName
	 * @param page
	 * @param pageSize
	 * @return
	 */
	public PageVO searchActivity(String activityName, Integer page, Integer pageSize) {
		String sql = SqlJoiner
				.join("select count(id) from (select id from activity_t_info a where a.is_valid=1 and a.title like '%",
						activityName,
						"%' UNION all select a.id from (amuse_t_activity a,amuse_r_activity_icon c) where a.is_valid=1 and a.id=c.activity_id and c.is_main=1 and c.is_valid=1 and a.title like '%",
						activityName, "%')a");
		if (pageSize == null) {
			pageSize = PageUtils.API_DEFAULT_PAGE_SIZE;
		}
		if (page == null) {
			page = 1;
		}
		int start = (page - 1) * pageSize;
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("start", start);
		params.put("pageSize", pageSize);
		Number totalCount = queryDao.query(sql);
		PageVO vo = new PageVO();
		if (totalCount != null) {
			vo.setTotal(totalCount.intValue());
		}
		if (start + pageSize >= vo.getTotal()) {
			vo.setIsLast(1);
		}
		sql = SqlJoiner
				.join("SELECT case when a.way=1 then 0 else 4 end type,a.id, a.title, a.icon, IFNULL(a.way, 1) way, a.start_time startTime, a.end_time endTime, a.summary, applyCount, NULL distance, a.create_date, IF ( a.begin_time < NOW() AND NOW() < a.over_time, 1, IF ( NOW() < a.begin_time, 2, IF ( a.start_time < NOW() AND NOW() < a.end_time, 5, IF (NOW() > a.end_time, 4, 3)))) state FROM activity_t_info a LEFT JOIN ( SELECT activity_id, count(activity_id) AS applyCount FROM activity_t_member WHERE is_valid = 1 GROUP BY activity_id ) b ON a.id = b.activity_id WHERE a.is_valid = 1 AND a.title LIKE '%",
						activityName,
						"%' UNION SELECT a.type,a.id, a.title, c.icon, a.way, a.start_date startTime, a.end_date endTime, a.summary, applyCount, calc_distance ( d.longitude, 120, d.latitude, 30 ) distance, a.create_date, IF ( now() < a.apply_start, 2, IF ( now() >= a.apply_start AND now() < a.start_date, 1, IF ( now() >= a.start_date AND now() < a.end_date, 5, 4 ))) state FROM ( amuse_t_activity a, amuse_r_activity_icon c ) LEFT JOIN ( SELECT count(id) applyCount, activity_id FROM amuse_r_activity_record WHERE is_valid = 1 AND state IN (0, 1) GROUP BY activity_id ) b ON a.id = b.activity_id LEFT JOIN netbar_t_info d ON a.netbar_id = d.id WHERE a.is_valid = 1 AND a.id = c.activity_id AND c.is_main = 1 AND c.is_valid = 1 AND a.title LIKE '%",
						activityName,
						"%' ORDER BY find_in_set(state, '1,5,2,3,4,6'), create_date DESC LIMIT :start,:pageSize");
		vo.setList(queryDao.queryMap(sql, params));
		return vo;
	}

	/**
	 * 搜索网吧
	 */
	public PageVO searchNetbar(String areaCode, String netbarName, String longitude, String latitude, Integer page,
			Integer pageSize) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("netbarName", netbarName);
		double longitudeDouble = NumberUtils.toDouble(longitude);
		double latitudeDouble = NumberUtils.toDouble(latitude);
		String calcDistince = "";
		String orderBy = "";
		String areaCodeSql = "";
		if (longitudeDouble > 0 && latitudeDouble > 0) {
			params.put("longitude", longitudeDouble);
			params.put("latitude", latitudeDouble);
			calcDistince = " calc_distance (longitude, :longitude, latitude, :latitude) distance,";
			orderBy = " order by  calc_distance (longitude, :longitude, latitude, :latitude)  asc ";
		} else {
			orderBy = SqlJoiner.join(" order by INSTR(a.name,'", netbarName, "')");
		}
		if (StringUtils.isNotBlank(areaCode)) {
			areaCodeSql = SqlJoiner.join(" and a.area_code like '", areaCode, "%'");
		}
		if (pageSize == null) {
			pageSize = PageUtils.API_DEFAULT_PAGE_SIZE;
		}
		if (page == null) {
			page = 1;
		}
		int start = (page - 1) * pageSize;
		params.put("start", start);
		params.put("pageSize", pageSize);
		PageVO vo = new PageVO();
		String sql = SqlJoiner.join("select count(1) from netbar_t_info a where name like '%", netbarName,
				"%' and a.is_release = 1 and a.is_valid = 1 ", areaCodeSql);
		Number totalCount = queryDao.query(sql);
		if (totalCount != null) {
			vo.setTotal(totalCount.intValue());
		}
		if (start + pageSize >= vo.getTotal()) {
			vo.setIsLast(1);
		}
		sql = SqlJoiner
				.join("select a.hot,a.longitude, a.latitude, a.id, a.name netbar_name, a.address, a.price, a.price_per_hour, a.icon, a.is_recommend,",
						calcDistince,
						" a.rebate, a.rebate_start_date, a.rebate_end_date, a.discount_info, ",
						" case when ( rebate > 0 and rebate <= 90 and now() >= rebate_start_date and now() <= rebate_end_date ) or (a.discount_info is not null and length(trim(a.discount_info)) >= 1 ) then 1 else 0 end has_rebate, ",
						" case when b.is_valid = 1 then 1 else 0 end is_order,if((hot is null or hot=0),0,1) is_hot,if(g.netbar_id is null and i.netbar_id is null,0,1) is_activity,if(h.netbar_id is null,0,1) is_benefit ",
						" from ( select a.hot,a.longitude, a.latitude, a.id, a.name, a.address, a.price, a.price_per_hour, a.icon, a.is_recommend,",
						calcDistince,
						" a.rebate, a.rebate_start_date, a.rebate_end_date, a.discount_info ",
						" from netbar_t_info a ",
						" where name like concat('%', :netbarName, '%') and a.is_release = 1 and a.is_valid = 1 ",
						areaCodeSql,
						orderBy,
						" limit :start,:pageSize) a",
						" left join netbar_t_merchant b on a.id = b.netbar_id  and b.is_valid=1",
						" left join(select a.netbar_id from netbar_resource_order a,netbar_resource_commodity_property b,netbar_resource_commodity c,netbar_commodity_category d where a.is_valid=1 and a.status=3 and a.property_id=b.id and date_format(b.settl_date, '%Y-%m-%d')=date_format(now(), '%Y-%m-%d') and a.commodity_id=c.id and c.category_id=d.id and d.is_show_app=1)g on a.id=g.netbar_id ",
						" LEFT JOIN ( SELECT a.netbar_id FROM netbar_resource_order a, netbar_resource_commodity_property b, netbar_resource_commodity c, netbar_commodity_category d WHERE a. STATUS = 3 AND a.is_valid = 1 AND a.property_id = b.id AND a.commodity_id = c.id AND c.category_id = d.id AND d. NAME = '增值券' AND b.validity IS NOT NULL AND now() > date_format(a.create_date, '%Y-%m-%d') AND now() < date_format( date_add( a.create_date, INTERVAL 1 + b.validity DAY ), '%Y-%m-%d' )) h ON a.id = h.netbar_id ",
						" left join (select netbar_id from netbar_t_recharge_activity a where  a.start_status=1 and a.is_valid=1)i on a.id=i.netbar_id ",
						" group by a.id", orderBy);
		vo.setList(queryDao.queryMap(sql, params));
		return vo;
	}

	/**
	 * 搜索用户
	 * 
	 * @param name
	 * @param page
	 * @param pageSize
	 * @return
	 */
	public PageVO searchUser(String name, Integer page, Integer pageSize) {
		if (pageSize == null) {
			pageSize = PageUtils.API_DEFAULT_PAGE_SIZE;
		}
		if (page == null) {
			page = 1;
		}
		int start = (page - 1) * pageSize;
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("start", start);
		params.put("pageSize", pageSize);
		PageVO vo = new PageVO();
		String sql = SqlJoiner.join("select count(1) from user_t_info where nickname like '%", name,
				"%' order by INSTR(nickname,'", name, "')");
		Number totalCount = queryDao.query(sql);
		if (totalCount != null) {
			vo.setTotal(totalCount.intValue());
		}
		if (start + pageSize >= vo.getTotal()) {
			vo.setIsLast(1);
		}
		sql = SqlJoiner.join("select id,icon,nickname,sex from user_t_info where nickname like '%", name,
				"%' order by INSTR(nickname,'", name, "') limit :start,:pageSize");
		vo.setList(queryDao.queryMap(sql, params));
		return vo;
	}

	public Map<String, Object> commonInfo(Long userId) {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put(
				"headlines",
				queryDao.queryMap("select id, title,type from activity_t_over_activities a where a.is_valid = 1 and a.is_show = 1 and a.timer_date < now() AND a.pid <= 0 order by a.timer_date desc limit 6"));
		if (userId != 0) {
			Map<String, Object> map = queryDao
					.querySingleMap("select * from (( select a.id,1 type,a.title, c.over_time start_time, null need_sign, null need_sign_minute, null signed from activity_t_info a left join activity_t_member b on a.id = b.activity_id left join activity_r_rounds c on a.id = c.activity_id and b.round = c.round where a.is_valid=1 and b.user_id = "
							+ userId
							+ " and b.is_valid = 1 and now() < c.end_time order by case when now() < c.over_time then 1 else 2 end, UNIX_TIMESTAMP(c.over_time) - UNIX_TIMESTAMP(now()) limit 1 ) union all ( select a.id,2 type,a. name title, d.activity_begin start_time, a.need_sign, a.need_sign_minute, b.signed from ( oet_event a, oet_event_round d ) left join oet_event_member b on d.id = b.round_id where a.is_valid=1 and a.id = d.event_id and b.user_id = "
							+ userId
							+ " and b.is_valid = 1 and d. status <> 3 order by case when now() < d.activity_begin then 1 else 2 end, UNIX_TIMESTAMP(d.activity_begin) - UNIX_TIMESTAMP(now()) limit 1 )) a order by case when now() < a.start_time then 1 else 2 end, UNIX_TIMESTAMP(a.start_time) - UNIX_TIMESTAMP(now()) limit 1");
			if (map != null && !map.isEmpty()) {
				Number needSign = (Number) map.get("need_sign");
				Number needSignMinute = (Number) map.get("need_sign_minute");
				Number signed = (Number) map.get("signed");
				long startTime = ((Timestamp) map.get("start_time")).getTime();
				long nowTime = System.currentTimeMillis();
				if (needSign != null && needSignMinute != null) {
					long signTime = startTime - 1000 * 60 * needSignMinute.intValue();
					if (nowTime < signTime) {
						map.put("tip", "距离签到开启");
						map.put("time", signTime - nowTime);
					} else if (nowTime < startTime) {
						if (signed == null || signed.intValue() == 0) {
							map.put("tip", "距离签到截止");
						} else {
							map.put("tip", "距离开赛");
						}
						map.put("time", startTime - nowTime);
					} else {
						map.put("tip", "赛事进行中");
					}
				} else {
					if (nowTime < startTime) {
						map.put("tip", "距离开赛");
						map.put("time", startTime - nowTime);
					} else {
						map.put("tip", "赛事进行中");
					}
				}
				map.remove("start_time");
				map.remove("need_sign");
				map.remove("need_sign_minute");
				map.remove("signed");
				result.put("myMatch", map);
			}
		}
		return result;
	}

	public PageVO matchList(Long itemId, Integer state, Integer type, Pager pager) {
		PageVO vo = new PageVO();
		String conditionSql = "";
		String dateSql = "";
		String oetDateSql = "";
		if (itemId != null) {
			conditionSql += " and item_id=" + itemId;
		}
		if (itemId == null) {
			dateSql = " and end_time>DATE_ADD(now(),INTERVAL -90 day) ";
			oetDateSql = " and activity_end>DATE_ADD(now(),INTERVAL -90 day) ";
		}
		if (state == -1 && itemId != null) {
			conditionSql += " and state<>3 ";
		} else if (state < 3 && state != -1) {
			conditionSql += " and state=" + state;
		} else if (state == 3) {
			conditionSql += " and state=3";
		}
		if (type != null && type != 0) {
			conditionSql += " and type=" + type;
		}
		Number total = queryDao
				.query("select count(1) from ( select a.item_id, ifnull(( select if ( date_format(now(), '%Y-%m-%d') < date_format(begin_time, '%Y-%m-%d'), 0, if ( date_format(now(), '%Y-%m-%d') <= date_format(over_time, '%Y-%m-%d'), 1, if ( date_format(now(), '%Y-%m-%d') <= date_format(end_time, '%Y-%m-%d'), 2, 3 ))) state from activity_r_rounds where activity_id = a.id and date_format(now(), '%Y-%m-%d') <= date_format(end_time, '%Y-%m-%d') order by round limit 1 ), 3 ) state, 1 type from activity_t_info a left join activity_r_items e on a.item_id = e.id where a.is_valid = 1 "
						+ dateSql
						+ "  union all  select a.item_id, if ( now() < d.apply_begin, 0, if ( now() < d.apply_end, 1, if (d. status = 3, 3, 2))) state, 2 type from ( oet_event a, oet_event_round d ) left join activity_r_items e on a.item_id = e.id where a.is_valid = 1 and a.id = d.event_id and a.is_show = 1 and (a.version is null or a.version<0.6) "
						+ oetDateSql
						+ " union all select a.item_id, if (now() < a.end_time, 2, 3) state,3 type from bounty a left join activity_r_items d on a.item_id = d.id where a.is_valid = 1 and a.is_publish = 1 and now() > a.start_time "
						+ dateSql + ") a where 1 = 1 " + conditionSql);
		if (pager.total >= total.intValue()) {
			vo.setIsLast(1);
		}
		vo.setTotal(total.intValue());
		if (itemId == null) {
			conditionSql += " and (end_time>DATE_ADD(now(),INTERVAL -90 day) or end_time is null) ";
		}
		vo.setList(queryDao
				.queryMap("select * from ( select null count_down,a.summary,a.create_date, a.item_id, e. name item_name, 1 type, a.id, a.icon, a.title, a.start_time, a.end_time,(select count(1) from activity_t_member where is_valid=1 and a.id=activity_id) applyNum, ifnull(( select if ( date_format(now(), '%Y-%m-%d') < date_format(begin_time, '%Y-%m-%d'), 0, if ( date_format(now(), '%Y-%m-%d') <= date_format(over_time, '%Y-%m-%d'), 1, if ( date_format(now(), '%Y-%m-%d') <= date_format(end_time, '%Y-%m-%d'), 2, 3 ))) state from activity_r_rounds where activity_id = a.id and date_format(now(), '%Y-%m-%d') <= date_format(end_time, '%Y-%m-%d') order by round limit 1 ), 3 ) state, d.oet_name sponsor,d.icon sponsor_icon, ( select begin_time from activity_r_rounds where activity_id = a.id and date_format(now(), '%Y-%m-%d') <= date_format(end_time, '%Y-%m-%d') order by round limit 1 ) apply_begin, ( select over_time from activity_r_rounds where activity_id = a.id and date_format(now(), '%Y-%m-%d') <= date_format(end_time, '%Y-%m-%d') order by round limit 1 ) apply_end, null max_num, null target,null regime,null mode from activity_t_info a left join sys_t_user d on a.create_user_id = d.id left join activity_r_items e on a.item_id = e.id where a.is_valid = 1  union all select null count_down,a.brief summary,a.create_date, a.item_id, e. name item_name, 2 type, d.id, a.poster icon, a. name title, d.activity_begin start_time, d.activity_end end_time,(select count(1) from oet_event_member where is_valid=1 and d.id=round_id) applyNum, if ( now() < d.apply_begin, 0, if ( now() < d.apply_end, 1, if (d. status = 3, 3, 2))) state, (select y.nickname from (oet_sys_user x,user_t_info y) where x.id=a.sponsor_id and x.user_id=y.id) sponsor,(select y.icon from (oet_sys_user x,user_t_info y) where x.id=a.sponsor_id and x.user_id=y.id) sponsor_icon, d.apply_begin,d.apply_end, d.max_num, null target,d.regime,a.mode from ( oet_event a, oet_event_round d ) left join activity_r_items e on a.item_id = e.id where a.is_valid = 1 and a.id = d.event_id and a.is_show=1 and (a.version is null or a.version<0.6) union all select if(UNIX_TIMESTAMP(a.end_time)-UNIX_TIMESTAMP(now())<0,null,(UNIX_TIMESTAMP(a.end_time)-UNIX_TIMESTAMP(now()))*1000) count_down,null summary,a.create_date, a.item_id, d. name item_name, 3 type, a.id, a.cover icon, a.title,a.start_time, a.end_time, (select count(distinct user_id) from bounty_grade where is_valid=1 and a.id=bounty_id) applyNum, if (now() < a.end_time, 2, 3) state, b.oet_name sponsor,b.icon sponsor_icon, null apply_begin, null apply_end, null max_num, a.target,null regime,null mode from bounty a left join sys_t_user b on a.create_user_id = b.id left join activity_r_items d on a.item_id = d.id where a.is_valid = 1 and a.is_publish = 1 and now()>a.start_time) a where 1 = 1 "
						+ conditionSql
						+ " order by find_in_set(state,'1,2,0,3'), case when state = 0 then apply_begin when state = 1 then apply_begin when state = 2 then apply_end when state = 3 then end_time end, create_date desc limit "
						+ pager.start + "," + pager.pageSize));
		return vo;
	}
}
