package com.miqtech.master.service.cohere;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.support.atomic.RedisAtomicInteger;
import org.springframework.stereotype.Component;

import com.miqtech.master.consts.CacheKeyConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.cohere.CohereActivityDao;
import com.miqtech.master.entity.cohere.CohereActivity;
import com.miqtech.master.service.common.ObjectRedisOperateService;
import com.miqtech.master.vo.PageVO;

@Component
public class CohereActivityService {
	@Autowired
	private QueryDao queryDao;
	@Autowired
	private CohereActivityDao cohereActivityDao;
	@Autowired
	private CoherePrizeService coherePrizeService;
	@Autowired
	private CohereDebrisService cohereDebrisService;
	@Autowired
	private JedisConnectionFactory redisConnectionFactory;
	@Autowired
	private ObjectRedisOperateService objectRedisOperateService;

	public CohereActivity getCohereActivitybyId(Long activityId) {
		return cohereActivityDao.findById(activityId);
	}

	/**
	 * 管理页面编辑活动界面
	 * 
	 * @param activityId
	 * @return 管理页面编辑活动需要字段
	 */
	public PageVO getActivityInfoForWeb(Integer page, String title, String startTimeBegin, String startTimeEnd,
			String state) {
		Integer pageSize = 5; // 默认显示5个数据
		Integer limitSize = (page - 1) * pageSize;
		String likeSql = " ";
		if (!StringUtils.isBlank(title)) {
			likeSql += " and ca.title like '%" + title + "%'";
		}
		if (!StringUtils.isBlank(startTimeBegin)) {
			likeSql += " and ca.begin_time<'" + startTimeBegin + "' ";
		}
		if (!StringUtils.isBlank(startTimeEnd)) {
			likeSql += " and ca.end_time>'" + startTimeEnd + "' ";
		}
		if (!StringUtils.isBlank(state)) {
			if (state.equals("1")) {// 未发布
				likeSql += " and ca.state=1 ";
			} else if (state.equals("2")) {// 进行中
				likeSql += " and ca.state=2 and ca.begin_time<now() and ca.end_time>now() ";
			} else { // 已过期
				likeSql += " and ca.end_time < now() ";
			}
		}
		String sql = "select ca.id,ca.title,ca.begin_time,ca.end_time,ca.state,ca.update_date from cohere_activity ca where ca.is_valid=1 "
				+ likeSql + " limit :limitSize,:pageSize";
		String sqlCount = "select count(1) count from cohere_activity ca where ca.is_valid=1" + likeSql;
		Number num = queryDao.query(sqlCount);
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("pageSize", pageSize);
		params.put("limitSize", limitSize);
		PageVO pager = new PageVO();
		List<Map<String, Object>> mids = queryDao.queryMap(sql, params);
		for (Map<String, Object> mid : mids) {
			List<Map<String, Object>> prizes = coherePrizeService
					.getSomeByActivityId(NumberUtils.toLong(mid.get("id").toString()));
			List<Map<String, Object>> debriss = cohereDebrisService
					.getSomeByActivityId(NumberUtils.toLong(mid.get("id").toString()));
			mid.put("prizes", prizes);
			mid.put("debriss", debriss);
			for (Map<String, Object> prize : prizes) {
				RedisAtomicInteger prizeCount = new RedisAtomicInteger(
						CacheKeyConstant.COHERE_PRIZE_COUNT + prize.get("id"), redisConnectionFactory);
				if (prizeCount.get() != -99) {
					prize.put("sendNum", NumberUtils.toInt(prize.get("counts").toString()) - prizeCount.get());
				} else {
					prize.put("sendNum",
							coherePrizeService.sendPrizeCount(NumberUtils.toLong(prize.get("id").toString())));
				}
			}

			for (Map<String, Object> debris : debriss) {
				RedisAtomicInteger debrisCount = new RedisAtomicInteger(
						CacheKeyConstant.COHERE_DEBRIS_COUNT + debris.get("id"), redisConnectionFactory);
				if (debrisCount.get() != -99) {
					debris.put("sendNum", NumberUtils.toInt(debris.get("counts").toString()) - debrisCount.get());
				} else {
					debris.put("sendNum",
							cohereDebrisService.sendDebrisCount(NumberUtils.toLong(debris.get("id").toString())));
				}
			}
			String activityState = mid.get("state").toString();
			if (!activityState.equals("1")) {
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				try {
					Date beginDate = format.parse(mid.get("begin_time").toString());
					Date endDate = format.parse(mid.get("end_time").toString());
					if (beginDate.before(new Date()) && endDate.after(new Date())) {
						mid.put("state", 2);
					} else {
						mid.put("state", 3);
					}
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
			if (activityState.equals("3")) {
				mid.put("state", 3);
			}
		}
		pager.setList(mids);
		pager.setCurrentPage(page);
		pager.setTotal(num.longValue());
		pager.setIsLast(mids.size() < pageSize ? 1 : 0);
		return pager;
	}

	public PageVO getActivityStatisticsInfo(Long activityId, Integer page, String searchDate, boolean limitFlag,
			String dayString) {
		String querySql = "";
		if (StringUtils.isNotBlank(searchDate)) {
			querySql = " and datecreate<" + searchDate;
		}
		if (StringUtils.isNotBlank(dayString)) {
			if (!dayString.equals("all")) {
				dayString = dayString.substring(0, dayString.length() - 1);
				querySql = " and datecreate in (" + dayString + " )";
			}
		} else {
			querySql = " and 1=0 ";
		}
		String totalSql = "SELECT count(*) FROM ( SELECT sum(CASE t.num  WHEN 1 THEN  t.dsum  ELSE  0  END) sum1, sum(CASE t.num  WHEN 2 THEN  t.dsum  ELSE  0  END) sum2, sum(CASE t.num  WHEN 3 THEN  t.dsum  ELSE  0  END) sum3, sum(CASE t.num  WHEN 4 THEN  t.dsum  ELSE  0  END  ) sum4, t.datecreate "
				+ " FROM ( SELECT COUNT(cohere_draw.debris_id) dsum, cohere_debris.num AS num, DATE_FORMAT(  cohere_draw.create_date,  '%Y%m%d' ) datecreate FROM cohere_draw LEFT JOIN cohere_debris ON cohere_draw.debris_id = cohere_debris.id where cohere_debris.activity_id ="
				+ activityId
				+ " GROUP BY cohere_debris.num, datecreate ORDER BY cohere_debris.num ASC ) t GROUP BY t.datecreate 	) d "
				+ " left JOIN ( SELECT sum(CASE num  WHEN 1 THEN  t.dsum  ELSE  0  END) psum1, sum(CASE num  WHEN 2 THEN  t.dsum  ELSE  0  END ) psum2, sum(CASE num  WHEN 3 THEN  t.dsum  ELSE  0  END ) psum3, sum(CASE num  WHEN 4 THEN  t.dsum  ELSE  0  END ) psum4, t.prizeDate "
				+ " FROM ( SELECT COUNT(prize_id) dsum, cohere_prize.num as num, DATE_FORMAT( cohere_prize_history.update_date, '%Y%m%d' ) prizeDate FROM cohere_prize_history LEFT JOIN cohere_prize ON cohere_prize_history.prize_id = cohere_prize.id WHERE cohere_prize_history.state > 0 and cohere_prize.activity_id="
				+ activityId + " GROUP BY prize_id, prizeDate ORDER BY prize_id ASC ) t 	GROUP BY t.prizeDate ) p "
				+ " ON p.prizeDate = d.datecreate where 1=1 ";
		Number totalCount = queryDao.query(totalSql);
		if (null != totalCount && totalCount.intValue() > 0) {
			String sql = "SELECT * FROM 	( SELECT sum(  CASE t.num  WHEN 1 THEN  t.sum  ELSE  0  END ) sum1, sum( CASE t.num  WHEN 2 THEN  t.sum  ELSE  0  END  )sum2, sum(CASE t.num  WHEN 3 THEN  t.sum  ELSE  0  END) sum3, sum(CASE t.num  WHEN 4 THEN  t.sum  ELSE  0  END) sum4, t.datecreate,t.personCount "
					+ " FROM ( SELECT COUNT(cohere_draw.debris_id) sum, cohere_debris.num AS num, DATE_FORMAT(  cohere_draw.create_date,  '%Y%m%d' ) datecreate,count(distinct cohere_draw.user_id) as personCount FROM cohere_draw LEFT JOIN cohere_debris ON cohere_draw.debris_id = cohere_debris.id where cohere_debris.activity_id ="
					+ activityId
					+ " GROUP BY cohere_debris.num, datecreate ORDER BY cohere_debris.num ASC ) t GROUP BY t.datecreate 	) d "
					+ " left JOIN ( SELECT sum(CASE num  WHEN 1 THEN  t.sum  ELSE  0  END ) psum1, sum(CASE num  WHEN 2 THEN  t.sum  ELSE  0  END ) psum2, sum(CASE num  WHEN 3 THEN  t.sum  ELSE  0  END )psum3, sum(CASE num  WHEN 4 THEN  t.sum  ELSE  0  END )psum4, t.prizeDate "
					+ " FROM ( SELECT COUNT(prize_id) sum, cohere_prize.num as num, DATE_FORMAT( cohere_prize_history.update_date, '%Y%m%d' ) prizeDate FROM cohere_prize_history LEFT JOIN cohere_prize ON cohere_prize_history.prize_id = cohere_prize.id WHERE cohere_prize_history.state > 0 and cohere_prize.activity_id="
					+ activityId
					+ " GROUP BY prize_id, prizeDate ORDER BY prize_id ASC ) t 	GROUP BY t.prizeDate ) p "
					+ " ON p.prizeDate = d.datecreate where 1=1 ";
			sql += querySql + " order by datecreate desc ";
			if (limitFlag) {
				int start = (page - 1) * 30;
				sql += "limit " + start + ",30";
			}
			List<Map<String, Object>> list = queryDao.queryMap(sql);
			PageVO vo = new PageVO(list);
			if (page * 30 >= totalCount.intValue()) {
				vo.setIsLast(1);
			}
			vo.setTotal(totalCount.longValue());
			return vo;
		} else {
			PageVO vo = new PageVO(new ArrayList<Map<String, Object>>());
			return vo;
		}
	}

	/**
	 * 查询今昨两天统计信息
	 */
	public List<Map<String, Object>> getActivityStatiDailyList(Long activityId, String dateString, Integer page) {
		String sql = "SELECT * FROM 	( SELECT sum(  CASE t.num  WHEN 1 THEN  t.sum  ELSE  0  END ) sum1, sum( CASE t.num  WHEN 2 THEN  t.sum  ELSE  0  END  )sum2, sum(CASE t.num  WHEN 3 THEN  t.sum  ELSE  0  END) sum3, sum(CASE t.num  WHEN 4 THEN  t.sum  ELSE  0  END) sum4, t.datecreate,t.personCount "
				+ " FROM ( SELECT COUNT(cohere_draw.debris_id) sum, cohere_debris.num AS num, DATE_FORMAT(  cohere_draw.create_date,  '%Y%m%d' ) datecreate,count(distinct cohere_draw.user_id) as personCount FROM cohere_draw LEFT JOIN cohere_debris ON cohere_draw.debris_id = cohere_debris.id where cohere_debris.activity_id ="
				+ activityId
				+ " GROUP BY cohere_debris.num, datecreate ORDER BY cohere_debris.num ASC ) t GROUP BY t.datecreate 	) d "
				+ " left JOIN ( SELECT sum(CASE num  WHEN 1 THEN  t.sum  ELSE  0  END ) psum1, sum(CASE num  WHEN 2 THEN  t.sum  ELSE  0  END ) psum2, sum(CASE num  WHEN 3 THEN  t.sum  ELSE  0  END )psum3, sum(CASE num  WHEN 4 THEN  t.sum  ELSE  0  END )psum4, t.prizeDate "
				+ " FROM ( SELECT COUNT(prize_id) sum, cohere_prize.num as num, DATE_FORMAT( cohere_prize_history.update_date, '%Y%m%d' ) prizeDate FROM cohere_prize_history LEFT JOIN cohere_prize ON cohere_prize_history.prize_id = cohere_prize.id WHERE cohere_prize_history.state > 0 and cohere_prize.activity_id="
				+ activityId + " GROUP BY prize_id, prizeDate ORDER BY prize_id ASC ) t 	GROUP BY t.prizeDate ) p "
				+ " ON p.prizeDate = d.datecreate where 1=1 and datecreate in (" + dateString + ") group by datecreate";
		List<Map<String, Object>> list = queryDao.queryMap(sql);
		return list;

	}

	public PageVO getActivityStatiDailyInfo(Long activityId, String date, Integer page, boolean limitFlag,
			String userIdString) {
		String querySql = "";
		if (StringUtils.isNotBlank(userIdString) && !userIdString.equals("all")) {
			userIdString = userIdString.substring(0, userIdString.length() - 1);
			querySql = " where cc.user_id in (" + userIdString + ")";
		} else if (StringUtils.isBlank(userIdString)) {
			querySql = " where 1=0 ";
		}
		String totalSql = "select count(*) from"
				+ " (select GROUP_CONCAT(drawNum) as drawNum,GROUP_CONCAT(prizeNum) as prizeNum,user_id from ("
				+ " select GROUP_CONCAT(num) drawNum,user_id,'' as prizeNum from("
				+ " select DATE_FORMAT(a.create_date, '%Y%m%d') days,b.num,a.user_id from cohere_draw a left join cohere_debris b on a.debris_id=b.id where a.is_valid=1 and b.is_valid=1 and b.activity_id="
				+ activityId + ") aa " + " where days='" + date + "' group by user_id" + " union all"
				+ " select '' as drawNum,user_id,GROUP_CONCAT(num) prizeNum from("
				+ " select DATE_FORMAT(a.update_date, '%Y%m%d') days,b.num,a.user_id from cohere_prize_history a left join cohere_prize b on a.prize_id=b.id where a.is_valid=1 and a.state>0 and b.is_valid=1 and b.activity_id="
				+ activityId + ") bb" + " where days='" + date + "' group by user_id) cc ";
		totalSql += querySql + " group by cc.user_id) dd";
		Number totalCount = queryDao.query(totalSql);
		if (null != totalCount && totalCount.intValue() > 0) {
			String sql = "select dd.drawNum,dd.prizeNum,uti.nickname,uti.telephone,uti.create_date,user_id from"
					+ " (select GROUP_CONCAT(drawNum) as drawNum,GROUP_CONCAT(prizeNum) as prizeNum,user_id from ("
					+ " select GROUP_CONCAT(num) drawNum,user_id,'' as prizeNum from("
					+ " select DATE_FORMAT(a.create_date, '%Y%m%d') days,b.num,a.user_id from cohere_draw a left join cohere_debris b on a.debris_id=b.id where a.is_valid=1 and b.is_valid=1 and b.activity_id="
					+ activityId + ") aa " + " where days='" + date + "' group by user_id" + " union all"
					+ " select '' as drawNum,user_id,GROUP_CONCAT(num) prizeNum from("
					+ " select DATE_FORMAT(a.update_date, '%Y%m%d') days,b.num,a.user_id from cohere_prize_history a left join cohere_prize b on a.prize_id=b.id where a.is_valid=1 and b.is_valid=1 AND a.state > 0 and b.activity_id="
					+ activityId + ") bb" + " where days='" + date + "' group by user_id) cc ";
			sql += querySql
					+ " group by cc.user_id) dd left join user_t_info uti on dd.user_id=uti.id where uti.is_valid=1";
			if (limitFlag) {
				int start = (page - 1) * 10;
				sql += " limit " + start + ",10";
			}
			List<Map<String, Object>> list = queryDao.queryMap(sql);
			list = getDebrisAndPrizeCounts(list);
			PageVO vo = new PageVO(list);
			if (page * 10 >= totalCount.intValue()) {
				vo.setIsLast(1);
			}
			vo.setTotal(totalCount.longValue());
			return vo;
		} else {
			PageVO vo = new PageVO(new ArrayList<Map<String, Object>>());
			return vo;
		}
	}

	public PageVO getActivityStatiUser(Long activityId, Long userId, Integer page, String dayString) {
		String querySql = "";
		if (StringUtils.isNotBlank(dayString) && !dayString.equals("all")) {
			dayString = dayString.substring(0, dayString.length() - 1);
			querySql = " where days in (" + dayString + ")";
		} else if (StringUtils.isBlank(dayString)) {
			querySql = " where 1=0";
		}
		String totalSql = "select count(distinct days) from"
				+ " (select days,GROUP_CONCAT(num) drawNum,'' as prizeNum from("
				+ " select DATE_FORMAT(a.create_date, '%Y%m%d') days,b.num,a.user_id from cohere_draw a left join cohere_debris b on a.debris_id=b.id where a.is_valid=1 and b.is_valid=1 and b.activity_id="
				+ activityId + " and a.user_id=" + userId + ") aa " + " group by days" + " union"
				+ " select days,'' as drawNum,GROUP_CONCAT(num) prizeNum from("
				+ " select DATE_FORMAT(a.update_date, '%Y%m%d') days,b.num,a.user_id from cohere_prize_history a left join cohere_prize b on a.prize_id=b.id where a.is_valid=1  and a.state>0 and b.is_valid=1 and b.activity_id="
				+ activityId + " and a.user_id=" + userId + ") bb" + " group by days) cc ";
		totalSql += querySql;
		Number totalCount = queryDao.query(totalSql);
		if (null != totalCount && totalCount.intValue() > 0) {
			String sql = "select GROUP_CONCAT(drawNum) drawNum,GROUP_CONCAT(prizeNum) prizeNum,days,user_id from"
					+ " (select days,GROUP_CONCAT(num) drawNum,'' as prizeNum,user_id from("
					+ " select DATE_FORMAT(a.create_date, '%Y%m%d') days,b.num,a.user_id from cohere_draw a left join cohere_debris b on a.debris_id=b.id where a.is_valid=1 and b.is_valid=1 and b.activity_id="
					+ activityId + " and a.user_id=" + userId + ") aa " + " group by days" + " union"
					+ " select days,'' as drawNum,GROUP_CONCAT(num) prizeNum,user_id from("
					+ " select DATE_FORMAT(a.update_date, '%Y%m%d') days,b.num,a.user_id from cohere_prize_history a left join cohere_prize b on a.prize_id=b.id where a.is_valid=1 and a.state>0 and b.is_valid=1 and b.activity_id="
					+ activityId + " and a.user_id=" + userId + ") bb" + " group by days) cc ";
			sql += querySql + " group by days";
			List<Map<String, Object>> list = queryDao.queryMap(sql);
			list = getDebrisAndPrizeCounts(list);
			PageVO vo = new PageVO(list);
			if (page * 10 >= totalCount.intValue()) {
				vo.setIsLast(1);
			}
			vo.setTotal(totalCount.longValue());
			return vo;
		} else {
			PageVO vo = new PageVO(new ArrayList<Map<String, Object>>());
			return vo;
		}
	}

	/**
	 * 根据从数据库返回的数据统计各个道具及奖品的发出数
	 */
	private List<Map<String, Object>> getDebrisAndPrizeCounts(List<Map<String, Object>> list) {
		for (int i = 0; i < list.size(); i++) {
			Map<String, Object> map = list.get(i);
			String drawNum = map.get("drawNum").toString();
			int firDraw = 0, secDraw = 0, thiDraw = 0, forDraw = 0;
			if (StringUtils.isNotBlank(drawNum)) {
				String[] drawNumArray = drawNum.split(",");
				for (int j = 0; j < drawNumArray.length; j++) {
					if (drawNumArray[j].equals("1")) {
						firDraw++;
					} else if (drawNumArray[j].equals("2")) {
						secDraw++;
					} else if (drawNumArray[j].equals("3")) {
						thiDraw++;
					} else if (drawNumArray[j].equals("4")) {
						forDraw++;
					}
				}
			}
			map.put("firDraw", firDraw);
			map.put("secDraw", secDraw);
			map.put("thiDraw", thiDraw);
			map.put("forDraw", forDraw);
			String prizeNum = map.get("prizeNum").toString();
			int firPrize = 0, secPrize = 0, thiPrize = 0, forPrize = 0;
			if (StringUtils.isNotBlank(prizeNum)) {
				String[] prizeNumArray = prizeNum.split(",");
				for (int j = 0; j < prizeNumArray.length; j++) {
					if (prizeNumArray[j].equals("1")) {
						firPrize++;
					} else if (prizeNumArray[j].equals("2")) {
						secPrize++;
					} else if (prizeNumArray[j].equals("3")) {
						thiPrize++;
					} else if (prizeNumArray[j].equals("4")) {
						forPrize++;
					}
				}
			}
			map.put("firPrize", firPrize);
			map.put("secPrize", secPrize);
			map.put("thiPrize", thiPrize);
			map.put("forPrize", forPrize);
			list.set(i, map);
		}
		return list;
	}

	public CohereActivity saveOrUpdate(CohereActivity cohereActivity) {
		return cohereActivityDao.save(cohereActivity);
	}

	/**
	 * 活动信息修改时使redis里的活动信息失效
	 */
	public void invalidCacheActivityInfo(Long activityId) {
		String cohereActivityInfoKey = CacheKeyConstant.COHERE_ACTIVITY_INFO + activityId;
		objectRedisOperateService.delData(cohereActivityInfoKey);

	}

	/**
	 * 得到当前活动的状态
	 * 
	 * @param activityId
	 * @return
	 */
	public Integer getState(Long activityId) {
		String sql = "select begin_time,end_time,state,is_valid from cohere_activity where id=" + activityId;
		Map<String, Object> result = queryDao.querySingleMap(sql);
		if (result != null) {
			if (result.get("is_valid").toString().equals("0")) {
				return 2; // 已过期
			} else {
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				try {
					Date beginDate = format.parse(result.get("begin_time").toString());
					Date endDate = format.parse(result.get("end_time").toString());
					if (beginDate.before(new Date()) && endDate.after(new Date())) {
						return 1; // 正在进行
					} else if (beginDate.after(new Date())) {
						return 0;
					} else {
						return 2;
					}
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
		}
		return 2;
	}

}
