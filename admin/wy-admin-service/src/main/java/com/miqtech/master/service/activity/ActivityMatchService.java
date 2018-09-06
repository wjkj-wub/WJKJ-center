package com.miqtech.master.service.activity;

import java.math.BigInteger;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.activity.ActivityMatchApplyDao;
import com.miqtech.master.dao.activity.ActivityMatchDao;
import com.miqtech.master.entity.activity.ActivityMatch;
import com.miqtech.master.entity.activity.ActivityMatchApply;
import com.miqtech.master.service.netbar.NetbarUserService;
import com.miqtech.master.thirdparty.util.SMSMessageUtil;
import com.miqtech.master.utils.DateUtils;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.vo.PageVO;

/**
 * 约战service
 */
@Component
public class ActivityMatchService {
	private static final Logger LOGGER = LoggerFactory.getLogger(ActivityMatchService.class);

	@Autowired
	private QueryDao queryDao;
	@Autowired
	private ActivityMatchDao activityMatchDao;
	@Autowired
	private ActivityMatchApplyDao activityMatchApplyDao;
	@Autowired
	private NetbarUserService netbarUserService;

	/**
	 * 获取约战列表
	 */
	public PageVO getMatches(int page, int rows, Long gameId, int publisherType) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("pageStart", (page - 1) * rows);
		params.put("pageNum", rows);
		String gameIdFilterSql = StringUtils.EMPTY;
		if (gameId.longValue() > 0) {
			params.put("gameId", gameId);
			gameIdFilterSql = " and m.item_id = :gameId ";
		}
		String publisherFilterSql = StringUtils.EMPTY;
		if (publisherType >= 0) {
			params.put("publisherType", publisherType);
			publisherFilterSql = " and m.by_merchant = :publisherType";
		}
		String sqlMatches = SqlJoiner
				.join(" select m.id,m.title,date_format(m.begin_time,'%Y-%m-%d %H:%i:%s') begin_time,m.way,m.spoils,m.server,m.address,m.is_start, ma.apply_count,",
						" m.people_num,i.name item_name,i.pic item_bg_pic,i.pic item_bg_pic_media,i.icon item_pic,ui.icon releaser_icon,ui.sex releaser_sex,ui.id releaser_id ,ui.nickname nickname,m.by_merchant",
						" from activity_t_matches m ",
						" 	left join (select match_id,count(id) apply_count from activity_r_match_apply where   is_valid = 1 group by match_id) ma  on m.id = ma.match_id ",
						" 	left join activity_r_items i on m.item_id = i.id ",
						" 	left join user_t_info ui on m.user_id = ui.id and ui.is_valid = 1 ",
						" where m.is_valid = 1 ", //and m.is_start != 1 and begin_time > now()
						gameIdFilterSql, publisherFilterSql, " order by m.create_date desc limit :pageStart, :pageNum");
		List<Map<String, Object>> dataList = queryDao.queryMap(sqlMatches, params);
		if (CollectionUtils.isNotEmpty(dataList)) {
			for (Map<String, Object> data : dataList) {
				String sqlApplies = "select a.id apply_id ,a.user_id user_id, u.nickname user_nickname, u.icon user_icon,u.telephone telephone from activity_r_match_apply a left join user_t_info u on u.id = a.user_id where a.is_valid=1 and a.match_id ="
						+ data.get("id").toString() + " limit 5";
				List<Map<String, Object>> applyList = queryDao.queryMap(sqlApplies);
				if (CollectionUtils.isNotEmpty(applyList)) {
					data.put("applies", applyList);
				}
			}
		}

		PageVO vo = new PageVO(dataList);
		String sqlTotal = "select count(1) countNum from activity_t_matches m where m.is_valid = 1 " + gameIdFilterSql
				+ publisherFilterSql;//and is_start != 1 and begin_time > now()
		params.remove("pageNum");
		params.remove("pageStart");
		Map<String, Object> map = queryDao.querySingleMap(sqlTotal, params);

		Number allCountNum = (Number) map.get("countNum");
		if (page * rows >= allCountNum.intValue()) {
			vo.setIsLast(1);
		}
		return vo;
	}

	/**
	 * 查询已经报名的约战
	 */
	public PageVO getRegedMatches(Long userId, Integer page, Integer rows) {
		String sqlMatches = SqlJoiner
				.join("select ma.match_id id, m.title, date_format(m.begin_time, '%Y-%m-%d %H:%i:%s') begin_time, m.way, m.spoils,m.server,m.is_start,m.address,",
						" m.people_num, i.NAME item_name, i.pic item_bg_pic,i.pic item_bg_pic_media,i.icon item_pic, count(mat.id) apply_count, u.nickname nickname,u.icon releaser_icon ",
						" from activity_r_match_apply ma ",
						" left join activity_t_matches m on m.id = ma.match_id and m.is_valid=1 ",
						" left join activity_r_items i on m.item_id = i.id",
						" left join activity_r_match_apply mat on ma.match_id = mat.match_id and mat.is_valid=1 left join user_t_info u on m.user_id = u.id",
						" where ma.user_id =:userId and ma.is_valid = 1 and m.user_id != ma.user_id group by ma.create_date desc limit :pageStart, :pageNum");
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("pageStart", (page - 1) * rows);
		params.put("pageNum", rows);
		params.put("userId", userId);
		List<Map<String, Object>> regedList = queryDao.queryMap(sqlMatches, params);
		if (CollectionUtils.isNotEmpty(regedList)) {
			for (Map<String, Object> data : regedList) {
				String sqlApplies = "select a.id apply_id ,a.user_id user_id, u.nickname user_nickname, u.icon user_icon,u.telephone telephone from activity_r_match_apply a left join user_t_info u on u.id = a.user_id where a.is_valid=1 and a.match_id ="
						+ data.get("id").toString() + " limit 5";
				List<Map<String, Object>> applyList = queryDao.queryMap(sqlApplies);
				if (CollectionUtils.isNotEmpty(applyList)) {
					data.put("applies", applyList);
				}
			}
		}
		PageVO vo = new PageVO(regedList);
		String sqlTotal = "select count(1) from activity_r_match_apply ma where ma.is_valid = 1 and ma.match_id in (select id from activity_t_matches where is_valid = 1) and ma.user_id = "
				+ userId;
		BigInteger bi = (BigInteger) queryDao.query(sqlTotal);
		if (page * rows >= bi.intValue()) {
			vo.setIsLast(1);
		}
		return vo;
	}

	/**
	 * 查询我发布的约战信息
	 */
	public PageVO getPubedMatches(Long userId, Integer page, Integer rows) {
		String querySql = SqlJoiner
				.join("select m.id, m.title,date_format(m.begin_time,'%Y-%m-%d %H:%i:%s') begin_time, m.way,m.is_start,m.address, ",
						" m.spoils,m.server, count(ma.id) apply_count, m.people_num, i.name item_name, i.pic item_bg_pic,i.pic item_bg_pic_media,i.icon item_pic, u.nickname nickname,u.icon releaser_icon ",
						" from activity_t_matches m ",
						" left join activity_r_match_apply ma on m.id = ma.match_id and ma.is_valid = 1 ",
						" left join activity_r_items i on m.item_id = i.id",
						" left join user_t_info u on m.user_id = u.id",
						" where m.is_valid = 1 and m.user_id=:userId group by m.id,ma.match_id order by m.create_date DESC limit :pageStart, :pageNum");
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("pageStart", (page - 1) * rows);
		params.put("pageNum", rows);
		params.put("userId", userId);
		List<Map<String, Object>> pubedList = queryDao.queryMap(querySql, params);
		if (CollectionUtils.isNotEmpty(pubedList)) {
			for (Map<String, Object> data : pubedList) {
				String sqlApplies = "select a.id apply_id ,a.user_id user_id, u.nickname user_nickname, u.icon user_icon,u.telephone telephone from activity_r_match_apply a left join user_t_info u on u.id = a.user_id where a.is_valid=1 and a.match_id ="
						+ data.get("id").toString() + " limit 5";
				List<Map<String, Object>> applyList = queryDao.queryMap(sqlApplies);
				if (CollectionUtils.isNotEmpty(applyList)) {
					data.put("applies", applyList);
				}
			}
		}
		PageVO vo = new PageVO(pubedList);
		String sqlTotal = "select count(m.id) from activity_t_matches m where m.is_valid = 1 and m.user_id = " + userId;
		BigInteger bi = (BigInteger) queryDao.query(sqlTotal);
		vo.setTotal(bi.longValue());
		if (page * rows < bi.intValue()) {
			vo.setIsLast(1);
		}
		return vo;
	}

	/**
	 * 约战详情
	 */
	public Map<String, Object> getMatch(long id, int commentPageSize) {
		String sql = SqlJoiner
				.join("select m.id, m.title, m.rule, m.server,m.netbar_id, m.way, m.address,m.by_merchant, m.begin_time, m.spoils,m.remark, m.people_num,m.is_start, ",
						"i.pic item_pic, i.name item_name,i.icon icon, ui.id releaser_id, ui.nickname nickname, ui.telephone releaser_telephone, ui.icon releaser_icon ",
						" from activity_t_matches m ",
						" left join activity_r_items i on m.item_id = i.id and i.is_valid = 1 ",
						" left join user_t_info ui on m.user_id = ui.id and ui.is_valid = 1",
						" where m.is_valid = 1 and m.id = ", String.valueOf(id));
		Map<String, Object> match = queryDao.querySingleMap(sql);
		// 如查到约战，补充报名用户及评论信息
		if (match != null) {
			String sqlApplies = SqlJoiner
					.join("select ma.id apply_id, ma.user_id, ui.nickname user_nickname, ui.icon user_icon, ui.telephone telephone",
							" from activity_r_match_apply ma ",
							" left join user_t_info ui on ma.user_id = ui.id and ui.is_valid = 1",
							" where ma.is_valid = 1 and ma.match_id = ", String.valueOf(id));
			match.put("applies", queryDao.queryMap(sqlApplies));
			String sqlComments = SqlJoiner
					.join(" select mc.id id ,mc.content, mc.create_date, ui.id user_id,ui.nickname user_nickname, ui.icon user_icon ",
							" from activity_r_match_comment mc ",
							" left join user_t_info ui on mc.user_id = ui.id and ui.is_valid = 1 ",
							" where mc.is_valid = 1 and mc.match_id = ", String.valueOf(id),
							" order by mc.create_date desc limit 0, ", String.valueOf(commentPageSize));
			PageVO commentVo = new PageVO();
			commentVo.setList(queryDao.queryMap(sqlComments));
			String sqlCommentCount = "select count(1) from activity_r_match_comment where is_valid = 1 and match_id = "
					+ id;
			BigInteger count = (BigInteger) queryDao.query(sqlCommentCount);
			match.put("commentsCount", count);
			if (commentPageSize >= count.intValue()) {
				commentVo.setIsLast(CommonConstant.INT_BOOLEAN_TRUE);
			}
			match.put("comments", commentVo);
		}
		return match;
	}

	/**
	 * 开始约战
	 */
	public void startMatch(long id) {
		String sqlStartMatch = "update activity_t_matches set block_time = NOW(), is_start = 1 where id = " + id;
		queryDao.update(sqlStartMatch);
	}

	/**
	 * 停止约战
	 */
	public void stopMatch(long id) {
		String sqlStopMatch = "update activity_t_matches set block_time = null, is_start = 0 where id = " + id;
		queryDao.update(sqlStopMatch);
	}

	/**
	 * 移除约战成员
	 */
	public void removeApply(long applyId) {
		String sqlRemoveApply = "update activity_r_match_apply set is_valid = 0 where id = " + applyId;
		queryDao.update(sqlRemoveApply);
	}

	/**
	 * 关闭约战
	 */
	public void closeMatch(long id) {
		String sqlCloseMatch = "update activity_t_matches set is_valid = 0 where id = " + id;
		queryDao.update(sqlCloseMatch);
	}

	/**
	 * 加入约战
	 */
	public Map<String, Object> apply(long id, long userId) {
		Map<String, Object> result = new HashMap<String, Object>();
		// 检查约战是否已满
		String sqlMatchInfo = "select m.people_num, count(a.id) count,m.netbar_id ,m.way from activity_t_matches m left join activity_r_match_apply a on m.id = a.match_id and a.is_valid = 1 where m.id = "
				+ id + " group by m.id";
		Map<String, Object> matchInfo = queryDao.querySingleMap(sqlMatchInfo);
		Integer matchPeopleNum = (Integer) matchInfo.get("people_num");
		BigInteger matchApplyNum = (BigInteger) matchInfo.get("count");
		if (matchApplyNum.longValue() < matchPeopleNum.longValue()) {
			// 报名约战
			String sqlQueryApply = "select * from activity_r_match_apply where match_id = " + id + " and user_id = "
					+ userId;
			List<Map<String, Object>> applies = queryDao.queryMap(sqlQueryApply);
			if (CollectionUtils.isNotEmpty(applies)) {
				String sqlActiveApply = "update activity_r_match_apply set is_valid = 1 where id = "
						+ applies.get(0).get("id");
				queryDao.update(sqlActiveApply);
			} else {
				String sqlInsertApply = "insert into activity_r_match_apply(match_id, user_id, is_valid, update_date, create_date) values("
						+ id + ", " + userId + ", 1, NOW(), NOW())";
				queryDao.update(sqlInsertApply);
			}
			result.put("result", true);
			result.put("msg", "成功");

			//报名约战，进行网吧用户绑定
			if (matchInfo.containsKey("netbar_id") && matchInfo.containsKey("way")) {
				Number way = (Number) matchInfo.get("way");
				if (way.intValue() == 2) {
					Number netbarId = (Number) matchInfo.get("netbar_id");
					if (netbarId != null && netbarId.longValue() > 0) {
						netbarUserService.bindNetbar(userId, netbarId.longValue());
					}
				}
			}
		} else {
			result.put("result", false);
			result.put("msg", "报名人数已满");
		}

		return result;
	}

	/**
	 * 取消约战报名
	 */
	public void cancelApply(long id, long userId) {
		String sqlCancelApply = "update activity_r_match_apply set is_valid = 0 where match_id = " + id
				+ " and user_id = " + userId;
		queryDao.update(sqlCancelApply);
	}

	/**
	 * 根据ID查询约战
	 */
	public ActivityMatch findById(Long id) {
		return activityMatchDao.findById(id);
	}

	/**
	 * 保存约战
	 */
	public ActivityMatch saveOrUpdate(ActivityMatch activityMatch) {
		return activityMatchDao.save(activityMatch);
	}

	/**
	 * 根据ID删除约战(is_valid置为0)
	 */
	public void deleteById(Long matchId) {
		ActivityMatch activityMatch = activityMatchDao.findOne(matchId);
		if (activityMatch != null) {
			activityMatch.setValid(CommonConstant.INT_BOOLEAN_FALSE);
			activityMatchDao.save(activityMatch);
		}
	}

	/**
	 * 查询商户用户发布的约战
	 */
	public List<Map<String, Object>> getUserAvailableMerchantMatches(long userId, long netbarId) {
		String sql = SqlJoiner
				.join("select m.id, m.title, date_format(m.begin_time, '%Y-%m-%d %H:%i:%s') begin_time, m.way, m.spoils,m.server, m.people_num, m.by_merchant",
						" from activity_t_matches m",
						" where m.is_valid = 1 and m.is_start != 1 and m.begin_time > now() and m.by_merchant = 1",
						" and m.netbar_id = ", String.valueOf(netbarId), " and m.user_id = ", String.valueOf(userId));
		return queryDao.queryMap(sql);
	}

	/**
	 * 发布约战
	 */
	public Map<String, Object> publishBattle(Long userId, String title, Long itemId, Integer way, String server,
			String beginTime, Long netbarId, String netbarName, Integer peopleNum, String contactWay, String intro) {
		Map<String, Object> result = new HashMap<String, Object>();
		Map<String, Object> params = new HashMap<String, Object>();
		ActivityMatch activityMatch = new ActivityMatch();
		activityMatch.setUserId(userId);
		boolean isInNetbar = way != null && way.equals(2);
		if (isInNetbar) {
			activityMatch.setNetbarId(netbarId);
			activityMatch.setAddress(netbarName);
		}
		activityMatch.setItemId(itemId);
		activityMatch.setTitle(title);
		try {
			activityMatch.setBeginTime(DateUtils.stringToDateYyyyMMddhhmm(beginTime));
		} catch (ParseException e) {
			return result;
		}
		activityMatch.setPeopleNum(peopleNum);
		activityMatch.setByMerchant(0);
		activityMatch.setServer(server);
		activityMatch.setWay(way);
		activityMatch.setSpoils(intro);
		activityMatch.setRemark(contactWay);
		activityMatch.setValid(1);
		activityMatch.setIsStart(0);
		activityMatch.setCreateDate(new Date());
		activityMatch = activityMatchDao.save(activityMatch);
		// 保存报名记录
		ActivityMatchApply apply = new ActivityMatchApply();
		apply.setMatchId(activityMatch.getId());
		apply.setUserId(userId);
		apply.setValid(CommonConstant.INT_BOOLEAN_TRUE);
		apply.setCreateDate(new Date());
		activityMatchApplyDao.save(apply);

		params.put("id", activityMatch.getId());
		String sql = "select a.id ,a.title,a.netbar_id,a.people_num people_num,date_format(a.begin_time,'%Y-%m-%d %k:%i') begin_time,a.way,a.server,b.icon,b.id item_id,b.name item_name,count(match_id) apply_count from (activity_t_matches a,activity_r_items b) left join activity_r_match_apply c on c.match_id = a.id where a.id=:id and a.item_id=b.id  group by a.id";
		result = queryDao.querySingleMap(sql, params);

		if (null != activityMatch) {
			if (isInNetbar) {
				netbarUserService.bindNetbar(userId, netbarId);
			}
		}

		// 计算是否允许分享红包
		int canShareRedbag = CommonConstant.INT_BOOLEAN_FALSE;
		/*
		 * Map<String, Object> firstMatch =
		 * getUserTodaysFirstPublishMatch(userId); if
		 * (MapUtils.isNotEmpty(firstMatch)) { Number id = (Number)
		 * firstMatch.get("id"); if
		 * (activityMatch.getId().equals(id.longValue())) { canShareRedbag =
		 * CommonConstant.INT_BOOLEAN_TRUE; } }
		 */
		if (result == null) {
			result = Maps.newHashMap();
		}
		result.put("canShareRedbag", canShareRedbag);

		return result;
	}

	/**
	 * 查询用户当天发起的第一个约战
	 */
	public Map<String, Object> getUserTodaysFirstPublishMatch(long userId) {
		String sql = SqlJoiner
				.join("SELECT id, title FROM activity_t_matches WHERE DATE_FORMAT(create_date, '%Y-%m-%d') = CURRENT_DATE ()",
						" AND user_id = ", String.valueOf(userId), " ORDER BY create_date ASC LIMIT 0, 1");
		return queryDao.querySingleMap(sql);
	}

	/**
	 * 查询某个用户在某天的第一个约战
	 */
	public Map<String, Object> getUserFirstPublishMatchByDate(long userId, String Date) {
		String sql = SqlJoiner.join("SELECT id, title, create_date FROM activity_t_matches",
				" WHERE DATE_FORMAT(create_date, '%Y-%m-%d') = '", Date, "' AND user_id = ", String.valueOf(userId),
				" ORDER BY create_date ASC LIMIT 0, 1");
		return queryDao.querySingleMap(sql);
	}

	/**
	 * 查找约战报名人员列表
	 */
	public Map<String, Object> queryMatchAppliers(Long id, Long userId, int page, int pageSize) {
		String sqlMatchAppliers = SqlJoiner
				.join("select ma.id apply_id, ma.user_id, ui.nickname user_nickname, ui.icon user_icon, ui.telephone telephone",
						" from activity_r_match_apply ma ",
						" left join user_t_info ui on ma.user_id = ui.id and ui.is_valid = 1",
						" where ma.is_valid = 1 and ma.match_id =:id limit :pageStart,  :pageNum");
		Map<String, Object> params = Maps.newHashMap();
		params.put("pageStart", (page - 1) * pageSize);
		params.put("pageNum", pageSize);
		params.put("id", id);
		List<Map<String, Object>> appliers = queryDao.queryMap(sqlMatchAppliers, params);
		String countSql = " select count(1) from activity_r_match_apply where is_valid = 1 and match_id = " + id;
		Number count = queryDao.query(countSql);
		Map<String, Object> result = Maps.newHashMap();
		int isLast = PageUtils.apiIsBottom(page, count.longValue());
		result.put("isLast", isLast);
		result.put("list", appliers);
		return result;
	}

	public void sendInvocationMsg(String phoneNumStr, String content) {
		String[] phones = StringUtils.split(phoneNumStr, ",");
		if (ArrayUtils.isEmpty(phones)) {
			return;
		}
		for (String phone : phones) {
			SMSMessageUtil.sendMessage(phone, content);
		}
	}

	public Iterable<ActivityMatch> findUnStartActivityMatch(int startStatus, int valid) {
		return activityMatchDao.findByIsStartAndValid(startStatus, valid);
	}

	public ActivityMatch save(ActivityMatch activities) {
		return activityMatchDao.save(activities);
	}

	public void save(Iterable<ActivityMatch> activities) {
		activityMatchDao.save(activities);
	}

	/**
	 * 用户发起约战统计
	 *
	 * @param mobile
	 * @param page
	 * @return
	 */
	public PageVO userPublishNum(String mobile, int page) {
		String mobileSql = "";
		String sql = "";
		PageVO vo = new PageVO();
		if (StringUtils.isNotBlank(mobile)) {
			mobileSql = " where username=" + mobile;
		}
		sql = SqlJoiner
				.join("select count(1) total from (select count(1) from activity_t_matches a left join user_t_info b on a.user_id=b.id ",
						mobileSql, " group by user_id,username)a");
		Number totalCount = queryDao.query(sql);
		if (totalCount != null) {
			vo.setTotal(totalCount.intValue());
		}
		int pageSize = PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		int start = (page - 1) * pageSize;
		if (start + pageSize >= vo.getTotal()) {
			vo.setIsLast(1);
		}
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("start", start);
		params.put("pageSize", pageSize);
		sql = SqlJoiner
				.join("select user_id,username,count(1) num from activity_t_matches a left join user_t_info b on a.user_id=b.id ",
						mobileSql, " group by user_id,username order by num desc limit :start,:pageSize");
		vo.setCurrentPage(page);
		vo.setList(queryDao.queryMap(sql, params));
		return vo;
	}

	/**
	 * 报名约战人数统计
	 *
	 * @param mobile
	 * @param page
	 * @return
	 */
	public PageVO battleApplyNum(String title, int page) {
		String titleSql = "";
		String sql = "";
		PageVO vo = new PageVO();
		if (StringUtils.isNotBlank(title)) {
			titleSql = " where title like '%" + title + "%'";
		}
		sql = SqlJoiner
				.join("select count(1) total from (select a.id,title,count(1) num from activity_t_matches a left join activity_r_match_apply b on a.id=b.match_id ",
						titleSql, " group by id,title)a");
		Number totalCount = queryDao.query(sql);
		if (totalCount != null) {
			vo.setTotal(totalCount.intValue());
		}
		int pageSize = PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		int start = (page - 1) * pageSize;
		if (start + pageSize >= vo.getTotal()) {
			vo.setIsLast(1);
		}
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("start", start);
		params.put("pageSize", pageSize);
		sql = SqlJoiner
				.join("select a.id,title,count(1) num from activity_t_matches a left join activity_r_match_apply b on a.id=b.match_id ",
						titleSql, " group by id,title order by num desc limit :start,:pageSize");
		vo.setCurrentPage(page);
		vo.setList(queryDao.queryMap(sql, params));
		return vo;
	}

	/**
	 * 获取用户的所有约战（包括发布和报名的）
	 */
	public PageVO getUserMatches(long userId, int page, int pageSize) {
		PageVO vo = new PageVO();
		int pageStart = (page - 1) * pageSize;
		List<Map<String, Object>> list = Lists.newArrayList();
		if (userId < 4200000) {
			String sql = SqlJoiner
					.join("       select ai.id,ai.title title, ai.start_time begin_time, 2 way,'' address, '' content, ui.nickname, item.name item_name, ai.icon icon, ai.cover bgimg, 0 is_releaser, 2 type       ",
							"        ,if(date_add(end_time, INTERVAL 1 DAY) >= NOW(), if(begin_time > NOW(), 2, if(date_add(over_time, INTERVAL 1 DAY) < NOW(), 3, 1)), 4) status                  ",
							"    from                                                                                                                                                              ",
							"        activity_t_info ai                                                                                                                                            ",
							"        left join user_t_info ui                                                                                                                                      ",
							"            on ui.id = ",
							String.valueOf(userId),
							"        left join activity_r_items item                                                                                                                               ",
							"            on item.id = ai.item_id                                                                                                                                   ",
							"    where ai.id in                                                                                                                                                    ",
							"        (select                                                                                                                                                       ",
							"            activity_id                                                                                                                                               ",
							"        from                                                                                                                                                          ",
							"            activity_t_member                                                                                                                                         ",
							"        where user_id = ",
							String.valueOf(userId),
							"            and is_valid = 1)                                                                                                                                      ",
							" order by begin_time desc                                                                                                                                              ",
							" limit ", String.valueOf(pageStart), ", ", String.valueOf(pageSize));
			list = queryDao.queryMap(sql);
		}
		vo.setList(list);
		if (CollectionUtils.isNotEmpty(list)) {
			String countSql = " select count(1) num from activity_t_member a where a.is_valid = 1 and a.user_id ="
					+ userId; //该用户活动数量
			Number count = queryDao.query(countSql);
			vo.setTotal(count.longValue());
			if (page * pageSize >= count.intValue()) {
				vo.setIsLast(1);
			}
		} else {
			vo.setTotal(0);
		}

		return vo;
	}

	/**
	 * 约战列表，分页
	 */
	public PageVO getMatchList(int page, int rows, Map<String, Object> params) {
		String sqlOrder = " order by m.create_date desc limit :pageStart, :pageNum";

		String sqlCount = "select count(1) from activity_t_matches m left join user_t_info u on m.user_id = u.id left join netbar_t_info n on m.netbar_id = n.id where m.is_valid = 1";
		String sqlMatch = SqlJoiner
				.join("select m.id, m.user_id userId, m.netbar_id netbarId, m.item_id itemId, i.name itemName, m.title, m.icon,",
						" m.begin_time beginTime, m.create_date createDate, m.over_time overTime, m.block_time blockTime, m.rule, m.server, m.way, m.address, n.name netbarName,",
						" m.spoils, m.remark, m.is_start isStart, m.by_merchant byMerchant, u.username, m.people_num peopleNum,",
						" (select count(1) from activity_r_match_apply ma where ma.match_id = m.id and ma.is_valid = 1) applyCount from activity_t_matches m",
						" left join activity_r_items i on i.id = m.item_id",
						" left join user_t_info u on m.user_id = u.id",
						" left join netbar_t_info n on m.netbar_id = n.id", " where m.is_valid = 1");
		if (null != params.get("title")) {
			sqlCount = SqlJoiner.join(sqlCount, " and title like '%" + params.get("title") + "%'");
			sqlMatch = SqlJoiner.join(sqlMatch, " and m.title like concat( '%',:title,'%')");
		}
		if (null != params.get("startDateMin")) {
			sqlCount = SqlJoiner.join(sqlCount, " and begin_time >= '" + params.get("startDateMin") + "'");
			sqlMatch = SqlJoiner.join(sqlMatch, " and m.begin_time >= :startDateMin");
		}
		if (null != params.get("startDateMax")) {
			sqlCount = SqlJoiner.join(sqlCount, " and begin_time <= '" + params.get("startDateMax") + "'");
			sqlMatch = SqlJoiner.join(sqlMatch, " and m.begin_time <= :startDateMax");
		}
		if (null != params.get("username")) {
			sqlCount = SqlJoiner.join(sqlCount, " and u.username like '%" + params.get("username") + "%'");
			sqlMatch = SqlJoiner.join(sqlMatch, " and u.username like concat( '%',:username,'%')");
		}
		if (null != params.get("areaCode")) {
			sqlCount = SqlJoiner.join(sqlCount,
					" and (way = 1 or n.area_code like concat(left('" + params.get("areaCode") + "', 2), '%'))");
			sqlMatch = SqlJoiner.join(sqlMatch, " and (way = 1 or n.area_code like concat(left(:areaCode, 2), '%'))");
		}
		if (null != params.get("itemId")) {
			sqlCount = SqlJoiner.join(sqlCount, " and m.item_id = " + params.get("itemId"));
			sqlMatch = SqlJoiner.join(sqlMatch, " and m.item_id = :itemId");
		}
		if (null != params.get("way")) {
			sqlCount = SqlJoiner.join(sqlCount, " and m.way = " + params.get("way"));
			sqlMatch = SqlJoiner.join(sqlMatch, " and m.way = :way");
		}
		sqlMatch = SqlJoiner.join(sqlMatch, sqlOrder);
		params.put("pageStart", (page - 1) * rows);
		params.put("pageNum", rows);
		List<Map<String, Object>> matchList = null;
		try {
			matchList = queryDao.queryMap(sqlMatch, params);
		} catch (Exception e) {
			LOGGER.error("查询数据库异常：" + e);
		}

		PageVO vo = new PageVO();
		vo.setList(matchList);
		// 分页
		Number total = (Number) queryDao.query(sqlCount);
		vo.setTotal(total.longValue());
		if (page * rows >= total.intValue()) {
			vo.setIsLast(1);
		}
		return vo;
	}

	/**
	 * 分页查询
	 */
	public Page<ActivityMatch> page(int page, Map<String, Object> params) {
		params.put("valid", CommonConstant.INT_BOOLEAN_TRUE);
		return activityMatchDao.findAll(buildSpecification(params), buildPageRequest(page));
	}

	/**
	 * 分页查询条件
	 */
	private Specification<ActivityMatch> buildSpecification(final Map<String, Object> searchParams) {
		Specification<ActivityMatch> spec = new Specification<ActivityMatch>() {
			@Override
			public Predicate toPredicate(Root<ActivityMatch> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
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
	 * 前七天约战次数
	 *
	 * @param netbarId
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public List<Map<String, Object>> lastSevenDayBattle(String netbarId, String startDate, String endDate) {
		String sql = SqlJoiner
				.join("select date_format(b.create_date, '%Y-%m-%d') date,count(id) num  from activity_t_matches b where netbar_id=",
						netbarId, " and b.create_date>='", startDate, "' and b.create_date<='", endDate,
						"' group by date_format(b.create_date, '%Y-%m-%d')");
		return queryDao.queryMap(sql);
	}

	/**
	 * 获取用户的发起的和参与的所有有效约战
	 */
	public List<Map<String, Object>> getUserAvailableMatches(long userId) {
		String sql = SqlJoiner
				.join("SELECT m.id, m.title, date_format(m.begin_time, '%Y-%m-%d %H:%i:%s' ) begin_time, m.way, m.spoils, m.`server`, m.is_start, m.address, m.people_num,",
						" i.`name` item_name, i.pic item_bg_pic, i.pic item_bg_pic_media, i.icon item_pic, u.nickname, u.icon user_icon, r.nickname releaser_nickname, r.icon releaser_icon, if(a.user_id = m.user_id, 1, 0) is_captain, count(apply.id) apply_count ",
						" FROM activity_r_match_apply a", " LEFT JOIN activity_t_matches m ON a.match_id = m.id",
						" LEFT JOIN activity_r_items i ON m.item_id = i.id AND i.is_valid = 1",
						" LEFT JOIN user_t_info u on a.user_id = u.id AND u.is_valid = 1",
						" LEFT JOIN user_t_info r ON m.user_id = r.id AND r.is_valid = 1",
						" left join activity_r_match_apply apply on a.match_id = apply.match_id and apply.is_valid=1 ",
						" WHERE m.is_valid = 1 AND m.is_start != 1 AND m.begin_time > now() AND a.user_id = ",
						String.valueOf(userId), " and a.is_valid = 1 group by a.match_id");
		return queryDao.queryMap(sql);
	}

	/**查询约战人数详情
	 * @param id
	 * @return
	 */
	public PageVO queryApplyDetail(Long id, Integer page, Integer rows) {
		String sql = "select a.nickname,a.username from user_t_info a,activity_r_match_apply b where a.id=b.user_id and b.is_valid=1 and match_id="
				+ String.valueOf(id);
		PageVO vo = new PageVO(queryDao.queryMap(sql));
		String sqlTotal = "select count(1) from activity_r_match_apply where is_valid=1 and match_id="
				+ String.valueOf(id);
		BigInteger bi = (BigInteger) queryDao.query(sqlTotal);
		vo.setTotal(bi.longValue());
		if (page * rows < bi.intValue()) {
			vo.setIsLast(1);
		}
		return vo;
	}

	/**
	 * 根据网吧ID查约战报名信息,分页
	 */
	public PageVO matchInfoPageByNetbarId(int page, long netbarId) {
		if (page < 1) {
			page = 1;
		}
		PageVO pageVO = new PageVO();
		Map<String, Object> params = Maps.newHashMap();
		params.put("netbarId", netbarId);
		int start = (page - 1) * PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		params.put("start", start);
		params.put("rows", PageUtils.ADMIN_DEFAULT_PAGE_SIZE);

		String sqlQuery = SqlJoiner.join(
				" select m.id, m.netbar_id, m.title, m.icon, m.begin_time, m.people_num, m.way, m.is_start",
				" from activity_t_matches m", " where m.is_valid=1 and m.netbar_id=:netbarId", " limit :start, :rows");

		pageVO.setList(queryDao.queryMap(sqlQuery, params));
		pageVO.setTotal(queryMatchCountByNetbarId(netbarId));
		pageVO.setCurrentPage(page);
		if (start + PageUtils.ADMIN_DEFAULT_PAGE_SIZE >= pageVO.getTotal()) {
			pageVO.setIsLast(1);
		}

		return pageVO;
	}

	/**
	 * 根据网吧ID查约战报名总数
	 */
	public int queryMatchCountByNetbarId(long netbarId) {
		String sqlCount = " select count(1) from activity_t_matches m where m.is_valid=1 and m.netbar_id=" + netbarId;
		Number count = queryDao.query(sqlCount);
		if (null != count) {
			return count.intValue();
		}
		return 0;
	}

	/**
	 * 查询约战详情信息
	 */
	public Map<String, Object> findValidByIdWithNetbarInfo(Long matchId) {
		if (matchId != null) {
			String sql = SqlJoiner
					.join("SELECT m.id, m.title, m.item_id itemId, m.way, m.netbar_id netbarId, m.address, m.people_num peopleNum, m.remark, m.`server`, m.rule, m.begin_time beginTime, n.`name` netbarName, n.address netbarAddress",
							" FROM activity_t_matches m",
							" LEFT JOIN netbar_t_info n ON m.netbar_id = n.id AND n.is_valid = 1 WHERE m.id = ",
							matchId.toString(), " AND m.is_valid = 1");
			Map<String, Object> match = queryDao.querySingleMap(sql);
			return match;
		}
		return null;
	}
}