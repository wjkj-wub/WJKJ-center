package com.miqtech.master.service.mall;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.support.atomic.RedisAtomicInteger;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.consts.mall.CoinConstant;
import com.miqtech.master.consts.mall.TaskConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.mall.MallTaskDao;
import com.miqtech.master.entity.mall.MallTask;
import com.miqtech.master.service.common.StringRedisOperateService;
import com.miqtech.master.utils.BeanUtils;
import com.miqtech.master.utils.DateUtils;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.vo.PageVO;

/**
 * 商城任务Service
 */
@Component
public class MallTaskService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MallTaskService.class);

	@Autowired
	private QueryDao queryDao;
	@Autowired
	private MallTaskDao mallTaskDao;
	@Autowired
	private CoinHistoryService coinHistoryService;
	@Autowired
	private StringRedisOperateService stringRedisOperateService;

	public MallTask findById(long id) {
		return mallTaskDao.findByIdAndValid(id, CommonConstant.INT_BOOLEAN_TRUE);
	}

	public MallTask findByTypeAndIdentify(int type, int identify) {
		List<MallTask> tasks = mallTaskDao.findByTypeAndIdentify(type, identify);
		if (CollectionUtils.isNotEmpty(tasks)) {
			return tasks.get(0);
		}
		return null;
	}

	/**
	 * 查出所有Task（有效）
	 */
	public List<MallTask> taskList() {
		Map<String, Object> params = new HashMap<>();
		params.put("valid", CommonConstant.INT_BOOLEAN_TRUE);
		return mallTaskDao.findAll(buildSpecification(params));
	}

	/**
	 * 分页查询
	 */
	public Page<MallTask> page(Integer page, Map<String, Object> params) {
		return mallTaskDao.findAll(buildSpecification(params), buildPageRequest(page));
	}

	/**
	 * 分页排序
	 */
	private PageRequest buildPageRequest(int pageNumber) {
		return new PageRequest(pageNumber - 1, PageUtils.ADMIN_DEFAULT_PAGE_SIZE, new Sort(Direction.ASC, "sortNum"));
	}

	/**
	 * 分页查询条件
	 */
	private Specification<MallTask> buildSpecification(final Map<String, Object> searchParams) {
		if (!searchParams.containsKey("valid")) {
			searchParams.put("valid", CommonConstant.INT_BOOLEAN_TRUE);
		}

		Specification<MallTask> spec = new Specification<MallTask>() {
			@Override
			public Predicate toPredicate(Root<MallTask> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				if (searchParams != null) {
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
				}

				return query.getRestriction();
			}
		};
		return spec;
	}

	/**
	 * 保存任务
	 */
	public MallTask save(MallTask task) {
		if (task != null) {
			Date now = new Date();
			task.setUpdateDate(now);
			if (task.getId() == null) {
				task.setValid(CommonConstant.INT_BOOLEAN_TRUE);
				task.setCreateDate(now);
			} else {
				MallTask oldTask = findById(task.getId());
				task = BeanUtils.updateBean(oldTask, task);
			}

			return mallTaskDao.save(task);
		}
		return null;
	}

	/**
	 * 删除任务
	 */
	public void delete(long id) {
		MallTask task = findById(id);
		if (task != null) {
			task.setValid(CommonConstant.INT_BOOLEAN_FALSE);
			save(task);
		}
	}

	/**
	 * 查询日常任务列表
	 */
	public List<Map<String, Object>> dailyTasks(Long userId, String clientType) {
		return getTasksByType(TaskConstant.TASK_TYPE_DAILY, userId, clientType);
	}

	/**
	 * 查询新手任务列表
	 */
	public List<Map<String, Object>> teachingTasks(Long userId, String clientType) {
		return getTasksByType(TaskConstant.TASK_TYPE_TEACHING, userId, clientType);
	}

	/**
	 * 通过类型查询任务列表（如传入用户ID则查询完成情况）
	 */
	private List<Map<String, Object>> getTasksByType(Integer type, Long userId, String clientType) {
		if (type == null) {
			type = TaskConstant.TASK_TYPE_DAILY;
		}

		// 根据传入的类型选择icon字段
		String iconColumn = CommonConstant.CLIENT_TYPE_IOS.toString().equals(clientType) ? "ios_icon" : "android_icon";

		String sql = null;
		if (userId != null) {
			// 日常任务追加当天的限制条件
			String todayCondation = TaskConstant.TASK_TYPE_DAILY.equals(type)
					? " AND h.create_date > DATE_FORMAT(NOW(), '%Y-%m-%d')" : "";

			sql = SqlJoiner.join("SELECT t.id, t.name, t.", iconColumn,
					" icon, t.coin, t.limit, t.text, count(h.id) accomplish_time, IF (count(h.id) >= t.limit, 1, 0) all_accomplish,t.simple_remark remark ",
					" FROM mall_t_task t LEFT JOIN mall_r_coin_history h ON t.id = h.target_id AND h.type = 1 AND h.is_valid = 1 AND h.user_id = ",
					userId.toString(), todayCondation, " WHERE t.is_valid = 1 AND t.type = ", type.toString(),
					" GROUP BY t.id ORDER BY sort_num ASC, t.id ASC");
		} else {
			sql = SqlJoiner.join("SELECT t.id, t.", iconColumn,
					" icon, t.coin, t.name, t.limit, t.text,t.simple_remark remark  FROM mall_t_task t WHERE t.is_valid = 1 AND t.type = ",
					type.toString(), " ORDER BY sort_num ASC, t.id ASC");
		}

		return queryDao.queryMap(sql);
	}

	/**
	 * 触发用户的某项任务 接口统计： 每日任务：
	 *  1 - 打开客户端 /ad；
	 *  2 - 参加约战 /activity/match/publishBattle；
	 *  3 - 发起约战 /activity/match/applyMatch；
	 *  4 - 预定网吧 netbar/doReserve；
	 *  5 - 支付上网费用 /weixinNotify /alipayNotify /orderPay；
	 *  6 - 关注他人 /concernOrCancel
	 *  7 - 收藏游戏 /game/favor
	 * 新手任务：
	 *  1 - 完善用户信息 editUser；
	 *  2 - 完善参赛信息 editUser；
	 *  3 - 首次支付 /weixinNotify /alipayNotify /orderPay；
	 *  4 - 首次发布约战 /activity/match/publishBattle；
	 *  5 - 首次兑换商品；
	 */
	@SuppressWarnings("unchecked")
	public boolean trigger(Map<String, Object> completeInfo, long userId, int taskType, int taskIdentify) {
		boolean result = false;
		MallTask task = findByTypeAndIdentify(taskType, taskIdentify);
		if (task != null && CommonConstant.INT_BOOLEAN_TRUE.equals(task.getValid())) {
			try {
				// 查询用户对该任务完成的次数
				String todayCondition = TaskConstant.TASK_TYPE_DAILY.equals(taskType)
						? " AND h.create_date > DATE_FORMAT(NOW(), '%Y-%m-%d')" : "";// 日常任务追加当天的限制条件
				String sql = SqlJoiner.join(
						"SELECT count(1) FROM mall_r_coin_history h WHERE h.is_valid = 1 AND h.type = ",
						CoinConstant.HISTORY_TYPE_TASK.toString(), " AND h.target_id = ", task.getId().toString(),
						" AND h.user_id = ", String.valueOf(userId), todayCondition);
				Number count = queryDao.query(sql);

				if (task.getLimit().compareTo(count.intValue()) > 0) {
					// redis中限制限额
					String key = TaskConstant.JOINER.join(TaskConstant.REDIS_TASK_COMPLETE, String.valueOf(taskType),
							String.valueOf(taskIdentify), String.valueOf(userId));
					RedisConnectionFactory factory = stringRedisOperateService.getRedisTemplate()
							.getConnectionFactory();
					RedisAtomicInteger userCount = new RedisAtomicInteger(key, factory);
					userCount.expire(DateUtils.surplusTodaySencods(), TimeUnit.SECONDS);
					if (userCount.addAndGet(1) > task.getLimit()) {
						return false;
					}

					// 任务未达到限额，完成此任务
					coinHistoryService.addGoldHistoryPub(userId, task.getId(), CoinConstant.HISTORY_TYPE_TASK,
							task.getCoin(), CoinConstant.HISTORY_DIRECTION_INCOME);
					result = true;

					// 为接口添加任务完成信息
					if (completeInfo != null) {
						String completeTaskKey = "completeTasks";
						List<Map<String, Object>> completes = (List<Map<String, Object>>) completeInfo
								.get(completeTaskKey);

						if (completes == null) {
							completes = new ArrayList<Map<String, Object>>();
						}

						Map<String, Object> complete = new HashMap<String, Object>();
						complete.put("taskType", taskType);
						complete.put("taskIdentify", taskIdentify);
						complete.put("completeCount", count.intValue() + 1);
						complete.put("coin", task.getCoin());
						completes.add(complete);
						completeInfo.put(completeTaskKey, completes);
					}
				}
			} catch (Exception e) {
				LOGGER.error("日常任务处理异常：", e);
			}
		}
		return result;
	}

	/**
	 * 任务列表
	 */
	public PageVO adminPage(int page, Map<String, String> searchParams) {
		String condition = " t.is_valid = 1";
		String totalCondition = condition;

		String type = MapUtils.getString(searchParams, "type");
		if (NumberUtils.isNumber(type)) {
			condition = SqlJoiner.join(condition, " AND t.type = ", type);
			totalCondition = SqlJoiner.join(totalCondition, " AND t.type = ", type);
		}

		String totalSql = SqlJoiner.join("SELECT COUNT(1) FROM mall_t_task t WHERE", totalCondition);
		Number total = queryDao.query(totalSql);
		List<Map<String, Object>> list = null;
		if (total != null && total.intValue() > 0) {
			String limitSql = PageUtils.getLimitSql(page);
			String sql = SqlJoiner.join(
					"SELECT id, type, identify, name, coin, `limit`, text, remark FROM mall_t_task t WHERE",
					totalCondition, limitSql);
			list = queryDao.queryMap(sql);
		}

		return new PageVO(page, list, total);
	}
}
