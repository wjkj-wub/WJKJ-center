package com.miqtech.master.service.activity;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.activity.ActivityItemServerDao;
import com.miqtech.master.entity.activity.ActivityServer;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.vo.PageVO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class ActivityItemServerService {
	private static final Logger LOGGER = LoggerFactory.getLogger(ActivityItemServerService.class);

	@Autowired
	private ActivityItemServerDao activityItemServerDao;
	@Autowired
	private QueryDao queryDao;

	/**
	 * 查询所有有效的服务器
	 */
	public List<ActivityServer> findAll() {
		return activityItemServerDao.findByValid(1);
	}

	/**
	 * 根据itemId查一级服务器列表
	 */
	public List<Map<String, Object>> queryStairList(long itemId) {
		String sqlSuery = "select id, server_name from activity_r_items_server where is_valid=1 and parent_server_id=0 and item_id="
				+ itemId;
		return queryDao.queryMap(sqlSuery);
	}

	/**
	 * 根据itemId查一级服务器数量
	 */
	public int queryStairTotal(long itemId) {
		String sqlQuery = "select count(1) from activity_r_items_server where is_valid=1 and parent_server_id=0 and item_id="
				+ itemId;
		Number total = (Number) queryDao.query(sqlQuery);
		return total.intValue();
	}

	/**
	 * 根据一级服务器id查二级服务器列表
	 */
	public List<Map<String, Object>> querySecondaryList(Long pid) {
		String sqlQuery = "select id, item_id, server_name from activity_r_items_server where is_valid=1 and parent_server_id="
				+ pid;
		return queryDao.queryMap(sqlQuery);
	}

	/**
	 * 根据服务器名称，游戏ID，查一级服务器ID
	 */
	public Map<String, Object> findByName(String name, long itemId) {
		String sqlQuery = "select id from activity_r_items_server where is_valid = 1 and parent_server_id = 0 and server_name = '"
				+ name + "' and item_id = " + itemId;
		return queryDao.querySingleMap(sqlQuery);
	}

	/**
	 * 同个游戏下，根绝区名查信息：确保同个游戏区名的唯一性
	 */
	public List<Map<String, Object>> judgeParentServerExist(String name, long itemId) {
		String sqlQuery = "select id, is_valid valid from activity_r_items_server where parent_server_id = 0 and server_name = '"
				+ name + "' and item_id = " + itemId + " limit 1";
		return queryDao.queryMap(sqlQuery);
	}

	/**
	 * (同个游戏，同个区）二级服务器名称是否已存在
	 */
	public boolean judgeServerExist(String name, long itemId, long parentId) {
		String sqlCount = "select count(1) from activity_r_items_server where is_valid = 1 and parent_server_id = "
				+ parentId + " and server_name = '" + name + "' and item_id = " + itemId;
		Number total = (Number) queryDao.query(sqlCount);
		return total.intValue() > 0;
	}

	/**
	 * 根据ID查询游戏服务器
	 */
	public ActivityServer findById(Long id) {
		return activityItemServerDao.findById(id);
	}

	/**
	 * 保存
	 */
	public ActivityServer saveOrUpdate(ActivityServer item) {
		return activityItemServerDao.save(item);
	}

	/**
	 * 根据ID删除(is_valid置为0)
	 */
	public void deleteById(Long itemId) {
		ActivityServer activityServer = activityItemServerDao.findOne(itemId);
		if (activityServer != null) {
			activityServer.setValid(CommonConstant.INT_BOOLEAN_FALSE);
			activityItemServerDao.save(activityServer);
		}
	}

	/**
	 * 获取赛事列表
	 */
	public List<Map<String, Object>> getItems(int page, int rows) {
		Map<String, Object> params = new HashMap<String, Object>(4);
		params.put("pageStart", (page - 1) * rows);
		params.put("pageNum", rows);
		String sqlItems = " select * from activity_r_items m  where m.is_valid = 1 group by m.id order by m.create_date desc limit :pageStart, :pageNum";
		List<Map<String, Object>> dataList = queryDao.queryMap(sqlItems, params);

		return dataList;
	}

	/**
	 * 分页查询
	 */
	public Page<ActivityServer> page(int page, Map<String, Object> params) {
		params.put("valid", CommonConstant.INT_BOOLEAN_TRUE);
		return activityItemServerDao.findAll(buildSpecification(params), buildPageRequest(page));
	}

	/**
	 * 分页查询条件
	 */
	private Specification<ActivityServer> buildSpecification(final Map<String, Object> searchParams) {
		Specification<ActivityServer> spec = new Specification<ActivityServer>() {
			@Override
			public Predicate toPredicate(Root<ActivityServer> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
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
	 * 服务器列表，分页
	 */
	public PageVO getServerList(int page, int rows, Map<String, Object> params, String isParent) {
		//		//二级服务器总数
		//		String sqlCount = "select count(1) from activity_r_items_server where is_valid = 1 and parent_server_id not in(0) and item_id = "
		//				+ params.get("itemId");
		//		//二级服务器列表
		//		String sqlServer = SqlJoiner.join("select id, server_name, parent_server_id from activity_r_items_server",
		//				" where is_valid = 1 and item_id = :itemId and parent_server_id not in(0)",
		//				" order by parent_server_id, server_name limit :pageStart, :pageNum");
		//		//一级服务器列表
		//		String sqlParent = SqlJoiner.join("select id, server_name, parent_server_id from activity_r_items_server",
		//				" where id in (select distinct t.parent_server_id from (",
		//				" select parent_server_id from activity_r_items_server",
		//				" where is_valid = 1 and item_id = :itemId and parent_server_id not in(0)",
		//				" order by parent_server_id, server_name limit :pageStart, :pageNum) as t)", " order by server_name");
		//
		//		if (null != params.get("name")) {
		//			sqlCount = "select count(1) from activity_r_items_server where is_valid = 1 and parent_server_id not in(0) and item_id = "
		//					+ params.get("itemId") + " and server_name like '%" + params.get("name") + "%'";
		//			sqlServer = SqlJoiner
		//					.join("select id, server_name, parent_server_id from activity_r_items_server",
		//							" where is_valid = 1 and server_name like concat('%',:name,'%') and item_id = :itemId and parent_server_id not in(0)",
		//							" order by parent_server_id, server_name limit :pageStart, :pageNum");
		//			sqlParent = SqlJoiner
		//					.join("select id, server_name, parent_server_id from activity_r_items_server",
		//							" where id in (select distinct t.parent_server_id from (",
		//							" select parent_server_id from activity_r_items_server",
		//							" where is_valid = 1 and server_name like concat('%',:name,'%') and item_id = :itemId and parent_server_id not in(0)",
		//							" order by parent_server_id, server_name limit :pageStart, :pageNum) as t)",
		//							" order by server_name");
		//		}

		//		List<Map<String, Object>> parentList = queryDao.queryMap(sqlParent, params);

		//		//将一级，二级服务器列表合并（按照父级关联顺序）
		//		List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
		//		for (int i = parentList.size() - 1; i > -1; i--) {
		//			resultList.add(parentList.get(i));
		//			for (Map<String, Object> map : serverList) {
		//				if (parentList.get(i).get("id").equals(map.get("parent_server_id"))) {
		//					resultList.add(map);
		//				}
		//			}
		//		}
		String sqlCount = "select count(1) from activity_r_items_server where is_valid = 1";
		String sqlServer = SqlJoiner.join("select id, server_name, parent_server_id from activity_r_items_server",
				" where is_valid = 1");
		if (null != params.get("itemId")) {
			sqlCount = SqlJoiner.join(sqlCount, " and item_id = " + params.get("itemId"));
			sqlServer = SqlJoiner.join(sqlServer, " and item_id = :itemId");
		}
		if (isParent.equals("1")) { //游戏区
			sqlCount = SqlJoiner.join(sqlCount, " and parent_server_id = 0");
			sqlServer = SqlJoiner.join(sqlServer, " and parent_server_id = 0");
		} else { //服务器
			if (null != params.get("pId")) {
				sqlCount = SqlJoiner.join(sqlCount, " and parent_server_id=" + params.get("pId"));
				sqlServer = SqlJoiner.join(sqlServer, " and parent_server_id = :pId");
			}
		}
		if (null != params.get("name")) {
			sqlCount = SqlJoiner.join(sqlCount, " and server_name like '%" + params.get("name") + "%'");
			sqlServer = SqlJoiner.join(sqlServer, " and server_name like concat('%',:name,'%')");
		}
		sqlServer = SqlJoiner.join(sqlServer, " order by parent_server_id, server_name limit :pageStart, :pageNum");
		params.put("pageStart", (page - 1) * rows);
		params.put("pageNum", rows);

		List<Map<String, Object>> serverList = queryDao.queryMap(sqlServer, params);
		PageVO vo = new PageVO();
		vo.setList(serverList);
		// 分页
		Number total = (Number) queryDao.query(sqlCount);
		vo.setTotal(total.longValue());
		if (page * rows >= total.intValue()) {
			vo.setIsLast(1);
		}
		return vo;
	}

	/**
	 * 根据游戏和服务区名称模糊匹配大区列表
	 */
	public Map<String, Object> getServerListByName(String gameName, String serverName, int size) {
		Map<String, Object> result = new HashMap<>();
		String sqlLimit = StringUtils.EMPTY;
		if (size > 0) {
			sqlLimit = " limit 0," + size;
		}
		String sqlServer = SqlJoiner
				.join("select s.id serverId, s.server_name serverName, p.server_name serverAreaName, i.name itemName from activity_r_items_server s",
						" left join activity_r_items i on s.item_id = i.id",
						" left join activity_r_items_server p on s.parent_server_id = p.id",
						" where s.is_valid = 1 and i.is_valid = 1 and i.name like '%" + gameName
								+ "%' and s.server_name like '%" + serverName + "%' group by s.item_id ", sqlLimit);
		String sqlCount = SqlJoiner.join("select count(1) from activity_r_items_server s",
				" left join activity_r_items i on s.item_id = i.id",
				" where s.is_valid = 1 and i.is_valid = 1 and i.name like '%" + gameName
						+ "%' and s.server_name like '%" + serverName + "%' ");

		result.put("serverList", queryDao.queryMap(sqlServer));

		Number total = (Number) queryDao.query(sqlCount);
		result.put("total", total.longValue());

		return result;
	}

	/**
	 * 根据游戏id获取一二级服务区数据
	 */
	public Map<String, Object> getServerListByItemId(Long itemId, int size) {
		Map<String, Object> result = new HashMap<>();
		String sqlLimit = StringUtils.EMPTY;
		if (size > 0) {
			sqlLimit = " limit 0," + size;
		}
		String sqlServer = SqlJoiner
				.join("select s.id serverId, s.server_name serverName, p.server_name serverAreaName from activity_r_items_server s",
						" left join activity_r_items_server p on s.parent_server_id = p.id",
						" where s.is_valid = 1 and s.item_id = " + itemId
								+ " and s.parent_server_id not in(0) order by s.parent_server_id ", sqlLimit);
		String sqlCount = SqlJoiner.join("select count(1) from activity_r_items_server s",
				" where s.is_valid = 1 and s.item_id = " + itemId + " and s.parent_server_id not in(0)");

		result.put("serverList", queryDao.queryMap(sqlServer));
		Number total = (Number) queryDao.query(sqlCount);
		result.put("total", total.longValue());

		return result;
	}

	public List<String> getByItemId(long itemId) {
		List<String> result = Lists.newArrayList();
		String sql = " select server_name name from activity_r_items_server where item_id =:itemId and is_valid =1 and parent_server_id >0  ";
		Map<String, Object> params = Maps.newHashMap();
		params.put("itemId", itemId);
		List<Map<String, Object>> queryMap = queryDao.queryMap(sql, params);

		if (CollectionUtils.isNotEmpty(queryMap)) {
			for (Map<String, Object> map : queryMap) {
				if (map.containsKey("name")) {
					result.add(map.get("name").toString());
				}
			}
		}
		return result;
	}
}