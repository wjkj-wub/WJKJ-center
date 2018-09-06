package com.miqtech.master.service.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.activity.ActivityItemDao;
import com.miqtech.master.entity.activity.ActivityItem;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.vo.PageVO;

@Component
public class ActivityItemService {
	private static final Logger LOGGER = LoggerFactory.getLogger(ActivityItemService.class);

	@Autowired
	private ActivityItemDao activityItemDao;
	@Autowired
	private QueryDao queryDao;

	/**
	 * 查询所有有效的 赛事项目
	 */
	public List<ActivityItem> findAll() {
		return activityItemDao.findByValid(1);
	}

	public List<Map<String, Object>> queryAll() {
		String sql = "select id item_id,name item_name,icon item_icon ,server_required from activity_r_items where is_valid=1";
		return queryDao.queryMap(sql);
	}

	/**
	 * 根据ID查询赛事
	 */
	public ActivityItem findById(Long id) {
		return activityItemDao.findById(id);
	}

	/**
	 * 保存赛事
	 */
	public ActivityItem saveOrUpdate(ActivityItem item) {
		return activityItemDao.save(item);
	}

	/**
	 * 根据ID删除赛事(is_valid置为0)
	 */
	public void deleteById(Long itemId) {
		ActivityItem activityItem = activityItemDao.findOne(itemId);
		if (activityItem != null) {
			activityItem.setValid(CommonConstant.INT_BOOLEAN_FALSE);
			activityItemDao.save(activityItem);
		}
	}

	/**
	 * 获取赛事列表
	 */
	public List<Map<String, Object>> getItems(int page, int rows) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("pageStart", (page - 1) * rows);
		params.put("pageNum", rows);
		String sqlItems = " select * from activity_r_items m  where m.is_valid = 1 group by m.id order by m.create_date desc limit :pageStart, :pageNum";
		List<Map<String, Object>> dataList = queryDao.queryMap(sqlItems, params);

		return dataList;
	}

	/**
	 * 分页查询
	 */
	public Page<ActivityItem> page(int page, Map<String, Object> params) {
		params.put("valid", CommonConstant.INT_BOOLEAN_TRUE);
		return activityItemDao.findAll(buildSpecification(params), buildPageRequest(page));
	}

	/**
	 * 分页查询条件
	 */
	private Specification<ActivityItem> buildSpecification(final Map<String, Object> searchParams) {
		Specification<ActivityItem> spec = new Specification<ActivityItem>() {
			@Override
			public Predicate toPredicate(Root<ActivityItem> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				List<Predicate> ps = Lists.newArrayList();

				Set<String> keys = searchParams.keySet();
				for (String key : keys) {
					try {
						Predicate isReleasePredicate = cb.like(root.get(key).as(String.class),
								SqlJoiner.join("%", String.valueOf(searchParams.get(key)), "%"));
						ps.add(isReleasePredicate);
					} catch (Exception e) {
						LOGGER.error("添加查询条件异常：", e);
					}
				}

				query.where(cb.and(ps.toArray(new Predicate[ps.size()])));
				return query.getRestriction();
			}
		};
		return spec;
	}

	/**
	 * 分页排序
	 */
	private PageRequest buildPageRequest(int pageNumber) {
		return new PageRequest(pageNumber - 1, PageUtils.ADMIN_DEFAULT_PAGE_SIZE, new Sort(Direction.DESC, "id"));
	}

	/**
	 * 赛事项目列表，分页
	 */
	public PageVO getItemList(int page, int rows, Map<String, Object> params) {
		String sqlItemAndServer = SqlJoiner
				.join("select i.id, i.name, i.icon, i.pic, i.is_valid, i.server_required, s.server_name from activity_r_items i",
						" left join activity_r_items_server s on s.item_id = i.id", " where i.is_valid = 1");
		if (null != params.get("name")) {
			sqlItemAndServer = SqlJoiner.join(sqlItemAndServer, " and i.name like concat( '%',:name,'%')");
		}
		sqlItemAndServer = SqlJoiner.join(sqlItemAndServer, " group by i.id limit :pageStart, :pageNum");
		params.put("pageStart", (page - 1) * rows);
		params.put("pageNum", rows);
		List<Map<String, Object>> itemList = null;
		try {
			itemList = queryDao.queryMap(sqlItemAndServer, params);
		} catch (Exception e) {
			LOGGER.error("查询赛事项目列表分页异常：", e);
		}

		PageVO vo = new PageVO();
		vo.setList(itemList);
		// 分页
		String sqlCount = "select count(1) from activity_r_items where is_valid = 1";
		Number total = (Number) queryDao.query(sqlCount);
		vo.setTotal(total.longValue());
		if (page * rows >= total.intValue()) {
			vo.setIsLast(1);
		}
		return vo;
	}

	public List<Map<String, Object>> itemWithMatchNum(Integer state) {
		String endSql = "";
		String activityEndSql = "";
		if (state <= 0) {
			endSql = "<=";
			activityEndSql = "<>";
		} else {
			endSql = ">=";
			activityEndSql = "=";
		}
		List<Map<String, Object>> result = queryDao
				.queryMap("select a.id item_id, a. name item_name, a.icon item_icon,ifnull(b.num, 0) num from activity_r_items a left join ( select item_id, sum(num) num from (select a.item_id, count(1) num from ( select a.item_id, ifnull(( select if ( date_format(now(), '%Y-%m-%d') < date_format(begin_time, '%Y-%m-%d'), 0, if ( date_format(now(), '%Y-%m-%d') <= date_format(over_time, '%Y-%m-%d'), 1, if ( date_format(now(), '%Y-%m-%d') <= date_format(end_time, '%Y-%m-%d'), 2, 3 ))) state from activity_r_rounds where activity_id = a.id and date_format(now(), '%Y-%m-%d') <= date_format(end_time, '%Y-%m-%d') order by round limit 1 ), 3 ) state from activity_t_info a where a.is_valid = 1) a where state "
						+ activityEndSql
						+ " 3 group by a.item_id union all select a.item_id, count(1) num from ( select a.item_id, if ( now() < d.apply_begin, 0, if ( now() < d.apply_end, 1, if (d. status = 3, 3, 2))) state from ( oet_event a, oet_event_round d ) where a.is_valid = 1 and a.id = d.event_id and a.is_show=1 and (a.version is null or a.version<0.6)) a where state "
						+ activityEndSql
						+ " 3 group by item_id union all select a.item_id, count(1) num from bounty a where a.is_valid = 1 and a.is_publish = 1 and now() > a.start_time and now() "
						+ endSql
						+ " a.end_time group by item_id ) a group by item_id ) b on a.id = b.item_id where a.is_valid = 1");
		if (state == -1) {
			return result;
		}
		for (Map<String, Object> map : result) {
			Long itemIdLong = ((Number) map.get("item_id")).longValue();
			String gfsSql = "";
			String zfsSql = "";
			String xslSql = "";
			String totalSql = "";
			if (state <= 0) {
				//官方赛
				gfsSql = "select sum(num) num, ifnull(state ,- 1) state from ( select * from ( select count(id) num, ifnull(( select if ( date_format(now(), '%Y-%m-%d') < date_format(begin_time, '%Y-%m-%d'), 0, if ( date_format(now(), '%Y-%m-%d') <= date_format(over_time, '%Y-%m-%d'), 1, if ( date_format(now(), '%Y-%m-%d') <= date_format(end_time, '%Y-%m-%d'), 2, 3 ))) state from activity_r_rounds where activity_id = a.id and date_format(now(), '%Y-%m-%d') <= date_format(end_time, '%Y-%m-%d') order by round limit 1 ), 3) state from activity_t_info a where a.is_valid = 1 and now() < a.end_time and a.item_id = "
						+ itemIdLong
						+ " group by state ) a where state <> 3 union ALL select 0 num, 0 state union ALL select 0 num, 1 state union ALL select 0 num, 2 state ) a group by state WITH ROLLUP";
				//自发赛
				zfsSql = "select sum(num) num, ifnull(state ,- 1) state from ( select * from ( select count(d.id) num, if ( now() < d.apply_begin, 0, if ( now() < d.apply_end, 1, if (d. status = 3, 3, 2))) state from ( oet_event a, oet_event_round d ) where a.is_valid = 1 and a.id = d.event_id and a.is_show=1 and (a.version is null or a.version<0.6) and a.item_id ="
						+ itemIdLong
						+ " group by state) a where state <> 3 group by state union ALL select 0 num, 0 state union ALL select 0 num, 1 state union ALL select 0 num, 2 state ) a group by state WITH ROLLUP";
				//悬赏令
				xslSql = "select sum(num) num, ifnull(state ,- 1) state from ( select count(a.id) num, if (now() < a.end_time, 2, 3) state from bounty a where a.is_valid = 1 and a.is_publish = 1 and now() > a.start_time and now() < a.end_time and a.item_id = "
						+ itemIdLong + " group by state union ALL select 0 num, 2 state ) a group by state WITH ROLLUP";
				totalSql = "select sum(num) num,state from (" + gfsSql + " union all " + zfsSql + " union all "
						+ xslSql + ")a group by state";
				List<Map<String, Object>> condition = new ArrayList<Map<String, Object>>();
				Map<String, Object> gfsMap = new HashMap<String, Object>();
				Map<String, Object> zfsMap = new HashMap<String, Object>();
				Map<String, Object> xslMap = new HashMap<String, Object>();
				Map<String, Object> totalMap = new HashMap<String, Object>();
				List<Map<String, Object>> stateList = null;
				gfsMap.put("type", 1);
				stateList = queryDao.queryMap("select * from (" + gfsSql + ")a order by state");
				if (CollectionUtils.isNotEmpty(stateList)) {
					gfsMap.put("num", ((Number) stateList.get(0).get("num")).intValue());
				}
				gfsMap.put("state", stateList);

				zfsMap.put("type", 2);
				stateList = queryDao.queryMap("select * from (" + zfsSql + ")a order by state");
				if (CollectionUtils.isNotEmpty(stateList)) {
					zfsMap.put("num", ((Number) stateList.get(0).get("num")).intValue());
				}
				zfsMap.put("state", stateList);

				xslMap.put("type", 3);
				stateList = queryDao.queryMap("select * from (" + xslSql + ")a order by state");
				if (CollectionUtils.isNotEmpty(stateList)) {
					xslMap.put("num", ((Number) stateList.get(0).get("num")).intValue());
				}
				xslMap.put("state", stateList);

				totalMap.put("type", 0);
				stateList = queryDao.queryMap(totalSql);
				if (CollectionUtils.isNotEmpty(stateList)) {
					totalMap.put("num", ((Number) stateList.get(0).get("num")).intValue());
				}
				totalMap.put("state", queryDao.queryMap(totalSql));
				condition.add(totalMap);
				condition.add(gfsMap);
				condition.add(zfsMap);
				condition.add(xslMap);
				map.put("condition", condition);
			} else {
				gfsSql = "select count(1) num from ( select a.item_id, ifnull(( select if ( date_format(now(), '%Y-%m-%d') < date_format(begin_time, '%Y-%m-%d'), 0, if ( date_format(now(), '%Y-%m-%d') <= date_format(over_time, '%Y-%m-%d'), 1, if ( date_format(now(), '%Y-%m-%d') <= date_format(end_time, '%Y-%m-%d'), 2, 3 ))) state from activity_r_rounds where activity_id = a.id and date_format(now(), '%Y-%m-%d') <= date_format(end_time, '%Y-%m-%d') order by round limit 1 ), 3 ) state from activity_t_info a where a.is_valid = 1 and item_id = "
						+ itemIdLong + " ) a where state = 3";
				zfsSql = "select count(1) num from ( select if ( now() < d.apply_begin, 0, if ( now() < d.apply_end, 1, if (d. status = 3, 3, 2))) state from ( oet_event a, oet_event_round d ) where a.is_valid = 1 and a.id = d.event_id and a.is_show=1 and (a.version is null or a.version<0.6) and item_id="
						+ itemIdLong + ") a where state = 3";
				xslSql = "select count(1) num from bounty a where a.is_valid=1 and a.is_publish=1 and now() > a.start_time and now()>a.end_time and item_id="
						+ itemIdLong;
				totalSql = "select sum(num) num from(" + gfsSql + " union all " + zfsSql + " union all " + xslSql
						+ ")a";
				List<Map<String, Object>> condition = new ArrayList<Map<String, Object>>();
				Map<String, Object> gfsMap = new HashMap<String, Object>();
				Map<String, Object> zfsMap = new HashMap<String, Object>();
				Map<String, Object> xslMap = new HashMap<String, Object>();
				Map<String, Object> totalMap = new HashMap<String, Object>();
				gfsMap.put("type", 1);
				gfsMap.put("num", queryDao.query(gfsSql));
				zfsMap.put("type", 2);
				zfsMap.put("num", queryDao.query(zfsSql));
				xslMap.put("type", 3);
				xslMap.put("num", queryDao.query(xslSql));
				totalMap.put("type", 0);
				totalMap.put("num", queryDao.query(totalSql));
				condition.add(totalMap);
				condition.add(gfsMap);
				condition.add(zfsMap);
				condition.add(xslMap);
				map.put("condition", condition);
			}
		}
		//客户端要求返回此种格式数据
		return result;
	}
}