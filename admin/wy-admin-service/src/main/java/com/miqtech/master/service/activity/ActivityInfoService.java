package com.miqtech.master.service.activity;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.miqtech.master.config.SystemConfig;
import com.miqtech.master.consts.*;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.activity.ActivityApplyDao;
import com.miqtech.master.dao.activity.ActivityInfoDao;
import com.miqtech.master.dao.activity.ActivityMemberDao;
import com.miqtech.master.dao.activity.ActivityTeamDao;
import com.miqtech.master.dao.amuse.AmuseActivityInfoDao;
import com.miqtech.master.entity.activity.*;
import com.miqtech.master.entity.amuse.AmuseActivityInfo;
import com.miqtech.master.entity.user.UserInfo;
import com.miqtech.master.service.common.ObjectRedisOperateService;
import com.miqtech.master.service.netbar.NetbarUserService;
import com.miqtech.master.thirdparty.service.JpushService;
import com.miqtech.master.thirdparty.util.img.ImgUploadUtil;
import com.miqtech.master.utils.DateUtils;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.QrcodeUtil;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.vo.PageVO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.*;

/**
 * 赛事信息管理
 */
@Component
public class ActivityInfoService {
	/*
	 * 赛事接口中赛事状态sql 赛事状态：1-报名中；2-报名预热中；3-报名已截止;4-赛事已结束 ;5比赛进行中
	 */
	public static final String activityStatusSql = " if(begin_time<=NOW() and NOW()<ADDDATE(over_time, INTERVAL 1 DAY),1,if(NOW()<begin_time,2,if(start_time<=NOW() and NOW()<ADDDATE(end_time, INTERVAL 1 DAY),5,if(NOW()>end_time,4,3)))) status ";
	public static final String activityStatusSort = "1,5,2,3,4,6";// 状态排序顺序，统一在此维护

	@Autowired
	private QueryDao queryDao;
	@Autowired
	private ActivityInfoDao activityInfoDao;
	@Autowired
	private ActivityApplyDao activityApplyDao;
	@Autowired
	private ActivityMemberDao activityMemberDao;
	@Autowired
	private ActivityTeamDao activityTeamDao;
	@Autowired
	private JpushService msgOperateService;
	@Autowired
	private NetbarUserService netbarUserService;
	@Autowired
	private AmuseActivityInfoDao amuseActivityInfoDao;
	@Autowired
	private ObjectRedisOperateService objectRedisOperateService;
	@Autowired
	private ActivityCardService activityCardService;
	@Autowired
	SystemConfig systemConfig;

	public ActivityInfo save(ActivityInfo activityInfo) {
		removeRedisCache();
		return activityInfoDao.save(activityInfo);
	}

	private void removeRedisCache() {
		Joiner joiner = Joiner.on("_");
		String compositiveListKey = joiner.join(CacheKeyConstant.API_CACHE_COMPOSITIVE_LIST, 1);
		objectRedisOperateService.setData(compositiveListKey, null);
		String athleticsListKey = joiner.join(CacheKeyConstant.API_CACHE_ATHLETICS_LIST, 1);
		objectRedisOperateService.setData(athleticsListKey, null);
	}

	public void delete(long id) {
		ActivityInfo activityInfo = activityInfoDao.findById(id);
		if (activityInfo != null) {
			activityInfo.setValid(CommonConstant.INT_BOOLEAN_FALSE);
			activityInfoDao.save(activityInfo);
		}
		removeRedisCache();
	}

	/**
	 * 查所有有赛事id，title
	 */
	public List<Map<String, Object>> findAllActivityIdandTitle() {
		String sqlQuery = "select id,title from activity_t_info where is_valid=1";
		return queryDao.queryMap(sqlQuery);
	}

	/**
	 * #api# 赛事列表 状态：1-报名进行中；2-报名未开始；3-报名已截止；4-赛事已结束
	 */
	public PageVO list(int page, int rows) {
		String querySql = SqlJoiner.join(
				"select info.id, info.title, info.icon icon, if(isnull(start_time), '', date_format(start_time, '%Y-%m-%d %H:%i:%s')) start_time, ",
				" if(isnull(end_time), '', date_format(end_time, '%Y-%m-%d %H:%i:%s')) end_time,",
				" date_format(info.create_date, '%Y-%m-%d %H:%i:%s') create_date, it.name item_name, it.pic item_pic, ",
				" if(date_add(end_time, INTERVAL 1 DAY) >= NOW(), if(begin_time > NOW(), 2, if(date_add(over_time, INTERVAL 1 DAY) < NOW(), 3, 1)), 4) status ",
				" from activity_t_info info left join activity_r_items it on info.item_id = it.id and it.is_valid = 1 ",
				" where info.is_valid = 1 and is_ground = 1 ",
				" order by status asc, sort_num desc, create_date desc limit :startNum, :pageNum");
		Map<String, Object> params = new HashMap<String, Object>(4);
		params.put("startNum", (page - 1) * rows);
		params.put("pageNum", rows);
		List<Map<String, Object>> result = queryDao.queryMap(querySql, params);
		PageVO vo = new PageVO();
		vo.setList(result);
		String sqlTotal = "select count(1) from activity_t_info where is_valid = 1 and is_ground=1";// 有效并在app显示
		Number countNum = queryDao.query(sqlTotal);
		if (page * rows >= countNum.intValue()) {
			vo.setIsLast(1);
		}
		return vo;
	}

	/**
	 * 2016cec-赛事查询接口
	 */
	public List<Map<String, Object>> _2016cecList() {
		String querySql = SqlJoiner.join(
				"select   ai.id,  ai.area_code areaCode,  ai.item_id gameId,  ag.name gameName ",
				"from  activity_t_info ai   left join activity_r_items ag     on ai.item_id = ag.id ",
				"where  (    ai.title like '%中国电子竞技%'     or ai.title like '%CEC%'  ) order by ai.area_code ");
		List<Map<String, Object>> result = queryDao.queryMap(querySql);
		return result;
	}

	/**
	 * 2016cec-赛事地点
	 */
	public List<Map<String, Object>> _2016cecActivityAddressList(Long activityId) {
		String querySql = SqlJoiner.join(
				"select   id netbarId,  name netbarName from  netbar_t_info where find_in_set(id,(select netbars from activity_t_info where id = ",
				activityId.toString(), ")) order by id ");
		List<Map<String, Object>> result = queryDao.queryMap(querySql);
		return result;
	}

	/**
	 * 查询赛事有效总数
	 *
	 * @return
	 */
	public int getTotal() {
		String sqlTotal = "select count(1) from activity_t_info where is_valid = 1 and is_ground=1";// 有效并在app显示
		Number total = (Number) queryDao.query(sqlTotal);
		return total.intValue();
	}

	/**
	 * #api# 赛事详情
	 */
	public Map<String, Object> detail(long id, Long userId, int memberLimitSize) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("id", id);
		// 选择赛事信息
		String sqlActivity = SqlJoiner.join(
				"select a.summary,a.way,b.num applyNum,c.server_name,a.id, title, a.icon,mobile_required,idcard_required,nickname_required,qq_required,labor_required,a.item_id,i.server_required,i.name item_name,i.icon item_pic,favor_num,person_allow,team_allow,",
				activityStatusSql,
				", if(isnull(start_time), '', date_format(start_time, '%Y-%m-%d %H:%i:%s')) start_time,",
				" if(isnull(end_time), '', date_format(end_time, '%Y-%m-%d %H:%i:%s')) end_time, ",
				" date_format(a.create_date, '%Y-%m-%d %H:%i:%s') create_date ",
				" from activity_t_info a left join (select sub_id,count(1) favor_num from user_r_favor where type=3 and is_valid=1 group by sub_id)x on a.id=x.sub_id left join activity_r_items i on a.item_id = i.id left join (select activity_id,count(1) num from activity_t_member  where is_valid=1 group by activity_id)b on a.id=b.activity_id left join activity_r_items_server c on a.server_id=c.id where a.id = :id and a.is_valid = 1");
		Map<String, Object> result = queryDao.querySingleMap(sqlActivity, params);
		if (MapUtils.isNotEmpty(result)) {
			boolean limitMember = memberLimitSize > 0;
			String limitSql = StringUtils.EMPTY;
			if (limitMember) {
				limitSql = " limit 0," + memberLimitSize;
			}
			// 选择赛事的报名选手
			String membersSql = SqlJoiner.join(
					"select u.id,u.id userId, ifnull(u.nickname,u.username) nickname, u.icon icon ",
					" from activity_t_member m left join user_t_info u on m.user_id = u.id and u.is_valid = 1 ",
					" where m.activity_id = :id and m.is_valid = 1 and m.is_enter = 1  order by m.create_date desc ",
					limitSql);
			result.put("members", queryDao.queryMap(membersSql, params));
			if (limitMember) {
				String memberCountSql = " select count(1) from activity_t_member m where m.activity_id = " + id
						+ " and m.is_valid = 1 and m.is_enter = 1 ";
				result.put("memberCount", queryDao.query(memberCountSql));
			}
			int favorStatus = -1;// 用户的收藏情况 -1:未登录 1:收藏 0:未收藏
			if (userId != null) {
				String favorSql = "select count(1) from user_r_favor where user_id = " + userId + " and type = "
						+ UserConstant.FAVOR_TYPE_ACTIVITY + " and is_valid = 1 and sub_id = " + id;
				Number favorCount = queryDao.query(favorSql);
				favorStatus = favorCount.intValue() > 0 ? CommonConstant.INT_BOOLEAN_TRUE
						: CommonConstant.INT_BOOLEAN_FALSE;
			}
			result.put("has_favor", favorStatus);
			// 查询赛事下面是否有资讯
			String infoIdSql = "select id from activity_t_over_activities where is_valid = 1 and activity_id = " + id
					+ " order by id asc limit 0,1";
			Number infoId = queryDao.query(infoIdSql);
			result.put("info_id", infoId == null ? 0 : infoId.longValue());
		} else {
			result = Maps.newHashMap();
		}
		result.put("schedule", getApplyDatesAndNetbars(id, 1, userId));
		return result;
	}

	/**
	 * #api# 收藏
	 */
	public Map<String, Object> favor(long userId, long actId) {
		boolean isFavor = false;
		// 查询用户是否有过收藏记录
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("userId", userId);
		params.put("actId", actId);
		String sqlUserFavors = "select * from user_r_favor where user_id = :userId and sub_id = :actId and type = "
				+ UserConstant.FAVOR_TYPE_ACTIVITY;
		List<Map<String, Object>> favors = queryDao.queryMap(sqlUserFavors, params);
		if (CollectionUtils.isNotEmpty(favors)) {
			if ("1".equals(favors.get(0).get("is_valid").toString())) {
				String sqlCancelFavor = "update user_r_favor set is_valid = 0 where user_id = " + userId
						+ " and sub_id = " + actId + " and type = " + UserConstant.FAVOR_TYPE_ACTIVITY;
				queryDao.update(sqlCancelFavor);
				isFavor = false;
			} else {
				String sqlAddFavor = "update user_r_favor set is_valid = 1 where user_id = " + userId + " and sub_id = "
						+ actId + " and type = " + UserConstant.FAVOR_TYPE_ACTIVITY;
				queryDao.update(sqlAddFavor);
				isFavor = true;
			}
		} else {
			String sqlInsertFavor = "insert into user_r_favor(user_id, sub_id, type, is_valid, update_date, create_date) values("
					+ userId + ", " + actId + ", " + UserConstant.FAVOR_TYPE_ACTIVITY + ", 1, NOW(), NOW());";
			queryDao.update(sqlInsertFavor);
			isFavor = true;
		}

		Map<String, Object> result = new HashMap<String, Object>();
		result.put("is_favor", isFavor);
		return result;
	}

	/**
	 * #api# 查询地点
	 */
	public List<Map<String, Object>> getAddress(long id, Long userId) {
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();

		// 查询用户当前赛事的报名记录
		List<Map<String, Object>> userMembers = null;
		if (userId != null) {
			String sqlUserMembers = "select id, round,team_id from activity_t_member where activity_id = " + id
					+ " and user_id = " + userId + " and is_valid = 1 and is_enter = 1";
			userMembers = queryDao.queryMap(sqlUserMembers);
		}

		// 查询赛事信息 -> 单场次 直接取， 多场次 查询场次
		String sqlActivity = "select * from activity_t_info where id = " + id;
		Map<String, Object> act = queryDao.querySingleMap(sqlActivity);
		if (act != null) {
			// 添加默认场次值
			if (act.get("round_count") == null) {
				act.put("round_count", 1);
			}

			if (act.get("round_count").toString().equals("1")) {// 单场次
				act.put("round", "1");
				if (userId != null) {
					result.add(clearRounds(act, userMembers));
				} else {
					result.add(clearRounds(act));
				}
			} else {
				// 查询赛事场次
				String sqlRound = "select * from activity_r_rounds where activity_id = " + id + " order by round desc";
				List<Map<String, Object>> rounds = queryDao.queryMap(sqlRound);
				if (CollectionUtils.isNotEmpty(rounds)) {// 存在场次信息
					for (Map<String, Object> round : rounds) {
						// 将场次网吧列表 添加到结果集
						if (userId != null) {
							result.add(clearRounds(round, userMembers));
						} else {
							result.add(clearRounds(round));
						}
					}
				}
			}
		}

		return result;
	}

	/**
	 * #api# 重组场次下的网吧信息， 并匹配用户是否报名过
	 */
	private Map<String, Object> clearRounds(Map<String, Object> round, List<Map<String, Object>> userMembers) {
		Map<String, Object> result = clearRounds(round);
		// 匹配用户是否报名过
		if (userMembers != null) {
			int hasApply = 0;
			for (Map<String, Object> m : userMembers) {
				if (round.get("round").toString().equals(m.get("round").toString())) {
					if (NumberUtils.toInt(m.get("team_id").toString()) > 0) {
						hasApply = 2;
					} else {
						hasApply = 1;
					}
					break;
				}
			}
			result.put("hasApply", hasApply);
		}
		return result;
	}

	/**
	 * #api# 重组场次下的网吧信息
	 */
	private Map<String, Object> clearRounds(Map<String, Object> round) {
		Date beginTime = (Date) round.get("begin_time");
		Date overTime = (Date) round.get("over_time");

		// 记录当前场次号
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("round", round.get("round"));
		result.put("begin_time", DateUtils.dateToString(beginTime, null));
		result.put("over_time", DateUtils.dateToString(overTime, null));

		// 匹配地区
		List<Map<String, Object>> areas = null;
		if (round != null) {
			String as = (String) round.get("areas");
			if (StringUtils.isNotBlank(as)) {
				String sqlAreas = SqlJoiner.join("select name from sys_t_area where area_code in (", as,
						") and is_valid = 1");
				areas = queryDao.queryMap(sqlAreas);
			}
		}
		StringBuilder areasStr = new StringBuilder();
		if (CollectionUtils.isNotEmpty(areas)) {
			for (Map<String, Object> a : areas) {
				if (areasStr.length() > 0) {
					areasStr.append(" ");
				}
				areasStr.append(a.get("name"));
			}
		}
		result.put("areas", areasStr.toString());
		// 判断当前赛事是否在报名阶段
		Date now = new Date();
		int inApplyNew = 1;// 默认可报名
		boolean inApply = true;
		beginTime = org.apache.commons.lang3.time.DateUtils.truncate(beginTime, Calendar.DAY_OF_MONTH);
		overTime = org.apache.commons.lang3.time.DateUtils
				.truncate(org.apache.commons.lang3.time.DateUtils.addDays(overTime, 1), Calendar.DAY_OF_MONTH);
		if (now.before(beginTime)) {
			inApplyNew = 0;// 未开始
			inApply = false;// 兼容老版本android 标识
		} else if (now.after(overTime)) {
			inApplyNew = 2;// 已结束
			inApply = false;// 兼容老版本android 标识
		}
		result.put("inApply", inApply);
		result.put("inApplyNew", inApplyNew);
		// 是否报名显示 未登陆 状态
		result.put("hasApply", -1);

		// 查询网吧列表
		List<Map<String, Object>> netbars = null;
		if (round != null) {
			String ns = (String) round.get("netbars");
			if (StringUtils.isNotBlank(ns)) {
				String sqlNetbars = SqlJoiner.join(
						"select id, name netbar_name,address,price_per_hour, if(isnull(icon), '', icon) icon from netbar_t_info where id in (",
						ns, ")");
				netbars = queryDao.queryMap(sqlNetbars);
			}
		}
		result.put("netbars", netbars == null ? new ArrayList<>() : netbars);
		return result;
	}

	/**
	 * #api# 某个用户收藏的赛事列表
	 *
	 * @param userId
	 * @param page
	 * @param rows
	 * @return
	 */
	public PageVO listReged(Long userId, Integer page, Integer rows) {
		// 查询赛事列表
		String sqlList = "select  id,   title, icon icon, date_format(start_time, '%Y-%m-%d %H:%i:%s') start_time, date_format(end_time, '%Y-%m-%d %H:%i:%s') end_time, "
				+ activityStatusSql
				+ " from    activity_t_info where is_valid = 1 and id in(	select distinct activity_id from activity_t_member where user_id=:userId and is_valid=1)  order by   find_in_set(status, '"
				+ activityStatusSort + "') limit :startNum, :pageNum";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("startNum", (page - 1) * rows);
		params.put("pageNum", rows);
		params.put("userId", userId);
		List<Map<String, Object>> result = queryDao.queryMap(sqlList, params);

		// 组装vo
		PageVO vo = new PageVO();
		vo.setList(result);
		String sqlTotal = "select count(1) from (select distinct activity_id from activity_t_member where user_id="
				+ userId + ") a";
		BigInteger bi = (BigInteger) queryDao.query(sqlTotal);
		if (page * rows >= bi.intValue()) {
			vo.setIsLast(1);
		}
		return vo;
	}

	public ActivityInfo queryActivityByIdForShare(Long id) {
		String sql = "select * from activity_t_info a where a.id=:id and is_valid=1";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("id", id);
		return queryDao.query(sql, params, ActivityInfo.class);
	}

	public List<UserInfo> queryUserIconForActivity(Long id) {
		String sql = "select c.* from activity_r_apply a,user_t_info c,activity_t_member am where a.activity_id=:id and a.type=1 and a.target_id=am.id and am.user_id = c.id union all select c.* from activity_r_apply a,activity_t_member b,user_t_info c where a.activity_id=:id and type=2 and target_id=b.team_id and b.user_id=c.id";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("id", id);
		return queryDao.queryObjectList(sql, params, UserInfo.class);
	}

	/**
	 * 分页查询 未开始的活动列表，及网吧的赛点报名情况
	 */
	public Map<String, Object> getNotStartedActivity(int page, int rows, Long netbarId) {
		if (netbarId == null) {
			netbarId = -1L;
		}
		Map<String, Object> result = new HashMap<String, Object>();
		String sqlList = " select info.id, info.organizer,info.need,info.address, info.match_name match_name, info.remark, info.time, ma.is_free,ma.id applyId, if(count(ma.id) > 0, 1, 0) has_apply,item.pic item_pic, item.name item_name  "
				+ " from " + "  wj_match info " + " left join netbar_r_match_apply ma  "
				+ " on info.id = ma.activity_id " + "   and ma.netbar_id =  " + netbarId
				+ "  and ma.is_valid = 1 left join activity_r_items item  on info.item_id = item.id"
				+ " where info.is_valid = 1 " + " group by info.id " + " order by info.create_date desc limit "
				+ (page - 1) * rows + ", " + rows;
		result.put("list", queryDao.queryMap(sqlList));
		String sqlTotal = "select count(1) from wj_match where  is_valid = 1";
		Number number = queryDao.query(sqlTotal);
		result.put("total", number);
		result.put("page", page);
		result.put("rows", rows);
		result.put("totalPage", PageUtils.calcTotalPage(number.intValue()));
		return result;
	}

	/**
	 * 通过ID查询活动信息
	 */
	public ActivityInfo findById(Long id) {
		return activityInfoDao.findById(id);
	}

	public Map<String, Object> toApply(Long activityId, Long userId) {
		String sql = "";
		Map<String, Object> params = new HashMap<String, Object>();
		sql = "select realname,telephone,qq,idcard from user_t_info where id=:userId";
		params.put("userId", userId);
		Map<String, Object> result = queryDao.querySingleMap(sql, params);

		if (result != null) {
			ActivityInfo activityInfo = findById(activityId);
			if (activityInfo != null) {
				String netbarIds = activityInfo.getNetbars();
				if (StringUtils.isNotBlank(netbarIds)) {
					sql = SqlJoiner.join("select id,name from netbar_t_info where id in(", netbarIds, ")");
					List<Map<String, Object>> netbars = queryDao.queryMap(sql);
					result.put("netbars", netbars);
				}
			}
		}

		return result;
	}

	/**
	 * 通过卡片提交个人报名
	 */
	public boolean submitPersonalApply(Long activityId, Long userId, Long netbarId, ActivityCard card, int round,
			Integer source) {
		return submitPersonalApply(activityId, userId, netbarId, card.getRealName(), card.getTelephone(),
				card.getIdCard(), card.getQq(), null, round, card.getId(), source);
	}

	/**
	 * 通过基本信息提交个人报名
	 */
	public boolean submitPersonalApply(Long activityId, Long userId, Long netbarId, String name, String telephone,
			String idcard, String qq, String labor, int round) {
		return submitPersonalApply(activityId, userId, netbarId, name, telephone, idcard, qq, labor, round, null, null);
	}

	/**
	 * 提交个人报名
	 */
	@Transactional
	public boolean submitPersonalApply(Long activityId, Long userId, Long netbarId, String name, String telephone,
			String idcard, String qq, String labor, int round, Long cardId, Integer source) {
		if (isApply(activityId, userId, round, 0)) {
			return false;
		}

		ActivityMember activityMember = new ActivityMember();
		activityMember.setCardId(cardId);
		activityMember.setActivityId(activityId);
		activityMember.setUserId(userId);
		activityMember.setTeamId(0L);// 表示个人
		activityMember.setRound(round);
		activityMember.setName(name);
		activityMember.setIdCard(idcard);
		activityMember.setTelephone(telephone);
		activityMember.setQq(qq);
		activityMember.setLabor(labor);
		activityMember.setIsMonitor(0);
		activityMember.setIsEnter(1);
		activityMember.setValid(1);
		activityMember.setSigned(0);
		activityMember.setCreateDate(new Date());
		activityMember.setUpdateDate(new Date());
		if (source != null && source == 1) {
			activityMember.setSigned(1);
		}
		activityMember = activityMemberDao.save(activityMember);
		ActivityApply activityApply = new ActivityApply();
		activityApply.setActivityId(activityId);
		activityApply.setTargetId(activityMember.getId());
		activityApply.setType(1);
		activityApply.setNetbarId(netbarId);
		activityApply.setRound(round);
		activityApply.setValid(1);
		activityApply.setCreateDate(new Date());
		activityApplyDao.save(activityApply);

		// 报名赛事，绑定网吧
		netbarUserService.bindNetbar(userId, netbarId);
		pushActivityMsg(activityInfoDao.findOne(activityId), userId, round);
		return true;
	}

	private void pushActivityMsg(ActivityInfo activity, Long userId, int round) {
		String content = "您已成功报名" + activity.getTitle() + (round > 0 ? "第" + round + "场" : "");
		msgOperateService.notifyMemberAliasMsg(Msg4UserType.ACTIVITY.ordinal(), userId,
				MsgConstant.PUSH_MSG_TYPE_ACTIVITY, "赛事消息", content, true, activity.getId());
	}

	/**
	 * 通过卡片创建战队
	 */
	public Map<String, Object> createTeam(Long activityId, Long userId, Long netbarId, String teamName,
			ActivityCard card, int round, Integer source) {
		return createTeam(activityId, userId, netbarId, teamName, card.getRealName(), null, card.getTelephone(),
				card.getIdCard(), card.getQq(), null, round, card.getId(), source);
	}

	/**
	 * 通过基本信息创建战队
	 */
	public Map<String, Object> createTeam(Long activityId, Long userId, Long netbarId, String teamName, String name,
			String server, String telephone, String idcard, String qq, String labor, int round) {
		return createTeam(activityId, userId, netbarId, teamName, name, server, telephone, idcard, qq, labor, round,
				null, null);
	}

	/**
	 * 创建战队
	 */
	@Transactional
	public Map<String, Object> createTeam(Long activityId, Long userId, Long netbarId, String teamName, String name,
			String server, String telephone, String idcard, String qq, String labor, int round, Long cardId,
			Integer source) {
		Map<String, Object> result = new HashMap<String, Object>();
		if (isApply(activityId, userId, round, 1)) {
			return result;
		}
		ActivityTeam activityTeam = new ActivityTeam();
		activityTeam.setNetbarId(netbarId);
		activityTeam.setActivityId(activityId);
		activityTeam.setRound(round);
		activityTeam.setName(teamName);
		activityTeam.setServer(server);
		activityTeam.setValid(1);
		activityTeam.setCreateDate(new Date());
		activityTeam = activityTeamDao.save(activityTeam);

		ActivityMember activityMember = new ActivityMember();
		activityMember.setCardId(cardId);
		activityMember.setActivityId(activityId);
		activityMember.setUserId(userId);
		activityMember.setTeamId(activityTeam.getId());
		activityMember.setRound(round);
		activityMember.setName(name);
		activityMember.setIdCard(idcard);
		activityMember.setTelephone(telephone);
		activityMember.setQq(qq);
		activityMember.setLabor(labor);
		activityMember.setIsMonitor(1);
		activityMember.setIsEnter(1);
		activityMember.setValid(1);
		activityMember.setCreateDate(new Date());
		if (source != null && source == 1) {
			activityMember.setSigned(1);
		}
		activityMember = activityMemberDao.save(activityMember);

		ActivityApply activityApply = new ActivityApply();
		activityApply.setActivityId(activityId);
		activityApply.setTargetId(activityTeam.getId());
		activityApply.setNetbarId(netbarId);
		activityApply.setRound(round);
		activityApply.setType(2);
		activityApply.setValid(1);
		activityApply.setCreateDate(new Date());
		activityApplyDao.save(activityApply);
		// 绑定网吧和用户
		netbarUserService.bindNetbar(userId, netbarId);
		activityTeam.setMemId(activityMember.getId());
		activityTeam.setQrcode(createQrcode(activityTeam));
		activityTeamDao.save(activityTeam);

		// 如果个人已报名删除个人报名
		List<ActivityApply> applyList = activityApplyDao.findByActivityIdAndTargetIdAndTypeAndRoundAndValid(activityId,
				userId, 1, round, 1);
		if (applyList.size() > 0) {
			activityApply = applyList.get(0);
			activityApply.setValid(0);
			activityApplyDao.save(activityApply);
		}
		List<ActivityMember> memberList = activityMemberDao
				.findByActivityIdAndUserIdAndRoundAndValidAndTeamId(activityId, userId, round, 1, 0L);
		if (memberList.size() > 0) {
			activityMember = memberList.get(0);
			activityMember.setValid(0);
			activityMemberDao.save(activityMember);
		}
		result.put("teamId", activityTeam.getId());
		//pushActivityMsg(activityInfoDao.findOne(activityId), userId, round);
		return result;
	}

	public String createQrcode(ActivityTeam team) {
		String sql = "select over_time from activity_r_rounds where is_valid=1 and activity_id=" + team.getActivityId()
				+ " and round=" + team.getRound();
		Date endTime = queryDao.query(sql);
		if (endTime != null) {
			String[] info = { "网娱大师比赛战队邀请", team.getName(),
					"该二维码将在" + DateUtils.dateToString(endTime, DateUtils.YYYY_MM_DD + "比赛报名结束时失效") };
			QrcodeUtil util = new QrcodeUtil();
			util.setLogopath(systemConfig.getQrLogoPath());
			Map<String, String> result = ImgUploadUtil.uploadQrcodePath(
					systemConfig.getAppDomain() + "load/wy?teamId=" + team.getId(), info, "wy-web-api",
					ImgUploadUtil.genFilePath("activityTeam"), new QrcodeUtil());
			if (result != null && result.containsKey(ImgUploadUtil.KEY_MAP_SRC)) {
				return result.get(ImgUploadUtil.KEY_MAP_SRC);
			}
		}
		return null;
	}

	/**
	 * 是否已报名(个人报名始终teamId=0)
	 */
	public boolean isApply(Long activityId, Long userId, int round, int teamId) {
		List<ActivityMember> memberList = null;
		if (teamId != 0) {
			memberList = activityMemberDao.findByActivityIdAndUserIdAndRoundAndValidAndTeamIdNot(activityId, userId,
					round, 1, 0L);
			if (memberList.size() > 0) {
				return true;
			} else {
				return false;
			}
		}
		memberList = activityMemberDao.findByActivityIdAndUserIdAndRoundAndValid(activityId, userId, round, 1);
		// 判断该赛事该场次个人或战队是否已报名
		if (memberList.size() > 0) {
			return true;
		}
		return false;
	}

	public List<Map<String, Object>> matchApplyNumData(Long activityId, String startDate, String endDate) {
		String sql = SqlJoiner.join("select title,date,sum(num) num from ",
				"(select a.id,a.title,DATE_FORMAT(b.create_date, '%Y-%m-%d') date,count(c.id) num from activity_t_info a",
				" left join activity_r_apply b on a.id=b.activity_id and type=2 and b.is_valid=1",
				" left join activity_t_member c on b.target_id=c.team_id and c.is_valid=1",
				" where a.is_valid=1 and a.id=:activityId and b.create_date>=:startDate and b.create_date<=:endDate group by DATE_FORMAT(b.create_date, '%Y-%m-%d')",
				"union all ",
				"select a.id,a.title,DATE_FORMAT(b.create_date, '%Y-%m-%d') date,count(b.id) num from activity_t_info a ",
				" left join activity_r_apply b on a.id=b.activity_id and type=2 and b.is_valid=1 ",
				" where a.is_valid=1 and a.id=:activityId and b.create_date>=:startDate and b.create_date<=:endDate group by DATE_FORMAT(b.create_date, '%Y-%m-%d'))z group by id,title,date");
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("activityId", activityId);
		params.put("startDate", startDate);
		params.put("endDate", endDate);
		List<Map<String, Object>> result = queryDao.queryMap(sql, params);
		if (result == null) {
			return new ArrayList<Map<String, Object>>();
		}
		return result;
	}

	public List<ActivityInfo> findByValid() {
		return activityInfoDao.findByValid(1);
	}

	/**
	 * 查询后台需要的分页数据
	 */
	public PageVO adminActivities(int page, Map<String, Object> params) {
		PageVO result = new PageVO();

		String sql = SqlJoiner.join(
				"SELECT ai.id, ai.netbar_id netbarId, ai.title, ai.icon, ai.score, ai.is_ground isGround, ai.in_wx inWx, ai.start_time startTime, ai.end_time endTime,ai.qrcode,",
				" it.name itemName, count(DISTINCT IF(m.team_id = 0, concat(m.user_id, '_', round), null)) member, count(DISTINCT IF(m.team_id != 0, concat(m.user_id, '_', round), null)) teamMember, a.name areaName, ",
				" sum(IF(ih.type = 1, 1, 0)) indexSum, sum(IF(ih.type = 4, 1, 0)) hallSum, sum(if(ih.type = 6, 1, 0)) recommendSum, sum(if(imi.category = 1, 1, 0)) mid1Sum,",
				" sum(if(imi.category = 2, 1, 0)) mid2Sum, sum(if(ia.belong = 0, 1, 0)) indexAdvSum, sum(if(ia.belong = 1, 1, 0)) hallAdvSum,",
				ActivityInfoService.activityStatusSql,
				" FROM activity_t_info ai LEFT JOIN activity_r_items it ON ai.item_id = it.id AND it.is_valid = 1",
				" LEFT JOIN activity_t_member m ON ai.id = m.activity_id AND m.is_valid = 1 AND m.round > 0",
				" LEFT JOIN sys_t_area a on CONCAT(LEFT(ai.area_code, 2), '0000') = a.area_code",
				" LEFT JOIN index_hot ih ON ai.id = ih.target_id AND ih.type IN (1, 4, 6) AND ih.is_valid = 1",
				" LEFT JOIN index_mid_img imi ON ai.id = imi.target_id",
				" LEFT JOIN index_t_advertise ia ON ia.target_id = ai.id AND ia.type = 10 AND ia.is_valid = 1",
				" WHERE ai.is_valid = 1");

		// 追加查询条件
		Set<String> keys = params.keySet();
		if (keys != null && !keys.isEmpty()) {
			for (String key : keys) {
				String param = (String) params.get(key);
				if (StringUtils.isNotBlank(param)) {
					if ("userAreaCode".equals(key)) {
						if (StringUtils.isNotBlank(param)) {
							sql = SqlJoiner.join(sql, " AND LEFT(ai.area_code, 2) = LEFT('", param, "', 2)");
						}
					} else if ("areaCode".equals(key)) {
						if (StringUtils.isNotBlank(param)) {
							sql = SqlJoiner.join(sql, " AND LEFT(ai.area_code, 2) = LEFT('", param, "', 2)");
						}
					} else if ("beginDate".equals(key)) {
						sql = SqlJoiner.join(sql, " AND ai.start_time >= '", param, "'");
					} else if ("endDate".equals(key)) {
						sql = SqlJoiner.join(sql, " AND ai.end_time <= '", param, "'");
					} else if ("status".equals(key)) {
						if ("1".equals(param)) {
							sql = SqlJoiner.join(sql, " AND begin_time <= NOW() AND NOW() < over_time");
						} else if ("2".equals(param)) {
							sql = SqlJoiner.join(sql, " AND NOW() < begin_time");
						} else if ("3".equals(param)) {
							sql = SqlJoiner.join(sql, " AND over_time < NOW() AND NOW() < start_time");
						} else if ("4".equals(param)) {
							sql = SqlJoiner.join(sql, " AND NOW() > end_time");
						} else if ("5".equals(param)) {
							sql = SqlJoiner.join(sql,
									" AND NOW() >= start_time AND NOW() < end_time AND NOW() > over_time");
						}
					} else if ("title".equals(key)) {
						sql = SqlJoiner.join(sql, " AND ai.title LIKE '%", param, "%'");
					} else if (StringUtils.isNotBlank(param)) {
						sql = SqlJoiner.join(sql, " AND ai.", key, " = '", String.valueOf(param), "'");
					}
				}
			}
		}
		sql = SqlJoiner.join(sql, " GROUP BY ai.id ORDER BY find_in_set(status, '", activityStatusSort,
				"'), ai.end_time ASC, ai.id DESC");
		sql = SqlJoiner.join(sql, " LIMIT ", String.valueOf((page - 1) * PageUtils.ADMIN_DEFAULT_PAGE_SIZE), ", ",
				String.valueOf(PageUtils.ADMIN_DEFAULT_PAGE_SIZE));

		List<Map<String, Object>> list = queryDao.queryMap(sql);
		result.setList(list);

		String sqlCount = SqlJoiner.join("SELECT COUNT(1) FROM (",
				"SELECT 1 FROM activity_t_info ai LEFT JOIN activity_r_items it ON ai.item_id = it.id AND it.is_valid = 1",
				" LEFT JOIN activity_t_member m ON ai.id = m.activity_id WHERE ai.is_valid = 1");
		if (keys != null && !keys.isEmpty()) {
			for (String key : keys) {
				String param = (String) params.get(key);
				if (StringUtils.isNotBlank(param)) {
					if ("userAreaCode".equals(key)) {
						sqlCount = SqlJoiner.join(sqlCount, " AND LEFT(ai.area_code, 2) = LEFT('", param, "', 2)");
					} else if ("areaCode".equals(key)) {
						if (StringUtils.isNotBlank(param)) {
							sqlCount = SqlJoiner.join(sqlCount, " AND LEFT(ai.area_code, 2) = LEFT('", param, "', 2)");
						}
					} else if ("beginDate".equals(key)) {
						sqlCount = SqlJoiner.join(sqlCount, " AND ai.start_time >= '", param, "'");
					} else if ("endDate".equals(key)) {
						sqlCount = SqlJoiner.join(sqlCount, " AND ai.end_time <= '", param, "'");
					} else if ("state".equals(key)) {
						if ("1".equals(param)) {
							sqlCount = SqlJoiner.join(sqlCount, " AND (now() >= start_time AND now() <= end_time))");
						} else if ("2".equals(param)) {
							sqlCount = SqlJoiner.join(sqlCount, " AND now() < start_time");
						} else if ("3".equals(param)) {
							sqlCount = SqlJoiner.join(sqlCount, " AND now() > end_time");
						}
					} else if ("title".equals(key)) {
						sqlCount = SqlJoiner.join(sqlCount, " AND ai.title LIKE '%", param, "%'");
					} else if (StringUtils.isNotBlank(param)) {
						sqlCount = SqlJoiner.join(sqlCount, " AND ", key, " LIKE '%", String.valueOf(param), "%'");
					}
				}
			}
		}
		sqlCount = SqlJoiner.join(sqlCount, " GROUP BY ai.id) t");
		Number count = queryDao.query(sqlCount);
		if (count == null) {
			count = 0;
		}
		result.setTotal(count.longValue());
		if (page * PageUtils.ADMIN_DEFAULT_PAGE_SIZE >= count.longValue()) {
			result.setIsLast(CommonConstant.INT_BOOLEAN_TRUE);
		}
		return result;
	}

	/**
	 * 综合赛事列表
	 */
	public PageVO compositiveActivityPage(int page, Integer rows, Integer lastTotal, Map<String, Object> params) {
		String sqlConditions = " WHERE 1 = 1";
		Map<String, Object> sqlParams = Maps.newHashMap();
		String totalConditions = " WHERE 1 = 1";
		String areaCode = MapUtils.getString(params, "areaCode");
		if (StringUtils.isNotBlank(areaCode)) {
			sqlConditions = SqlJoiner.join(sqlConditions, " AND n.area_code = :areaCode");
			sqlParams.put("areaCode", areaCode);
			totalConditions = SqlJoiner.join(totalConditions, " AND n.area_code = '", areaCode, "'");
		}
		Integer state = MapUtils.getInteger(params, "state");
		if (state != null && state != 0) {
			sqlConditions = SqlJoiner.join(sqlConditions, " AND a.state = ", state.toString());
			totalConditions = SqlJoiner.join(totalConditions, " AND a.state = ", state.toString());
		}
		Long itemId = MapUtils.getLong(params, "itemId");
		if (itemId != null) {
			sqlConditions = SqlJoiner.join(sqlConditions, " AND a.itemId = ", itemId.toString());
			totalConditions = SqlJoiner.join(totalConditions, " AND a.itemId = ", itemId.toString());
		}

		// 经纬度查询
		String sqlDistanceSql = "";
		String latitude = MapUtils.getString(params, "latitude");
		String longitude = MapUtils.getString(params, "longitude");
		if (StringUtils.isNotBlank(latitude) && StringUtils.isNotBlank(longitude)) {
			sqlDistanceSql = " IF (type = 0, NULL, calc_distance (:latitude, n.latitude, :longitude, n.longitude)) distance,";
			sqlParams.put("latitude", latitude);
			sqlParams.put("longitude", longitude);
		} else {
			sqlDistanceSql = " 0 distance,";
		}

		String totalSql = SqlJoiner.join("SELECT COUNT(1) FROM ((",
				" SELECT a.id, a.title, a.item_id itemId, a.end_date endTime, a.start_date startTime, a.summary, a.type, a.max_num maxNum, a.create_date createDate, ai.icon, 0 applyCount,",
				" IF (now() < a.apply_start, 2, IF (now() >= a.apply_start AND now() < a.start_date, 1, IF (now() >= a.start_date AND now() < a.end_date, 5, 4))) state,",
				" a.netbar_id netbarId, a.item_id, 0 sort_num", " FROM amuse_t_activity a ",
				" LEFT JOIN amuse_r_activity_icon ai ON a.id = ai.activity_id AND ai.is_main = 1 AND ai.is_valid = 1 WHERE a.is_valid = 1 and a.state = 2",
				" GROUP BY a.id ORDER BY state, a.create_date, ai.id DESC )", " UNION ALL (",
				" SELECT a.id, a.title, a.item_id, a.start_time startTime, a.end_time endTime, a.summary, if(way = 2, 4, 0) type, null maxNum, a.create_date, a.icon, count(ar.id) count,",
				activityStatusSql, " , NULL netbar_id, a.item_id, a.sort_num",
				" FROM activity_t_info a LEFT JOIN activity_t_member ar ON a.id = ar.activity_id AND ar.is_valid = 1",
				" LEFT JOIN netbar_t_info n ON a.netbar_id = n.id WHERE a.is_valid = 1 AND a.is_ground = 1",
				" GROUP BY a.id ORDER BY state, a.create_date, ai.id DESC )",
				" ) a LEFT JOIN netbar_t_info n ON a.netbarId = n.id LEFT JOIN sys_t_area sa ON n.area_code = sa.area_code",
				totalConditions);
		Number total = queryDao.query(totalSql);
		if (total == null) {
			total = 0;
		}

		String limit = "";
		Integer pageSize = rows == null ? PageUtils.API_DEFAULT_PAGE_SIZE : rows;
		if (page > 0) {
			int addStartRow = lastTotal == null ? 0 : total.intValue() - lastTotal;
			Integer startRow = (page - 1) * pageSize + addStartRow;
			limit = SqlJoiner.join(" LIMIT ", startRow.toString(), ", ", pageSize.toString());
		}
		String sql = SqlJoiner.join("SELECT a.*, n.name netbarName,", sqlDistanceSql,
				" sa.name area_name, n.area_code FROM ((",
				" SELECT a.id, a.title, a.item_id itemId, a.start_date startTime, a.end_date endTime, a.summary, a.type, a.max_num maxNum,a.create_date createDate, ai.icon, 0 applyCount,",
				" IF (now() < a.apply_start, 2, IF (now() >= a.apply_start AND now() < a.start_date, 1, IF (now() >= a.start_date AND now() < a.end_date, 5, 4))) state,",
				" a.netbar_id netbarId, a.item_id, 0 sort_num", " FROM amuse_t_activity a ",
				" LEFT JOIN netbar_t_info n ON a.netbar_id = n.id LEFT JOIN amuse_r_activity_icon ai ON a.id = ai.activity_id AND ai.is_main = 1 AND ai.is_valid = 1 WHERE a.is_valid = 1 and a.state = 2",
				" GROUP BY a.id )", " UNION ALL (",
				" SELECT a.id, a.title, a.item_id, a.start_time startTime, a.end_time endTime, a.summary, if(way = 2, 4, 0) type, null maxNum, a.create_date, a.icon, count(ar.id) count,",
				activityStatusSql, " , NULL netbar_id, a.item_id, a.sort_num",
				" FROM activity_t_info a LEFT JOIN activity_t_member ar ON a.id = ar.activity_id AND ar.is_valid = 1 WHERE a.is_valid = 1 AND a.is_ground = 1",
				" GROUP BY a.id )",
				" ) a LEFT JOIN netbar_t_info n ON a.netbarId = n.id LEFT JOIN sys_t_area sa ON n.area_code = sa.area_code",
				sqlConditions, " ORDER BY find_in_set(state, '", activityStatusSort, "'), a.createDate DESC, a.id DESC",
				limit);// state: 1 报名中, 2 报名预热中, 3 报名已截止, 4 赛事已结束, 5 赛事进行中(6
																																																																																																																																																																																																																																																																																																																																																									// 赛事未开始)
		List<Map<String, Object>> list = queryDao.queryMap(sql, sqlParams);

		if (CollectionUtils.isNotEmpty(list)) {
			for (Map<String, Object> activity : list) {
				Integer type = MapUtils.getInteger(activity, "type");
				if (type != 4 && type != 0) {
					Long id = MapUtils.getLong(activity, "id");
					String memberCountSql = SqlJoiner.join(
							"SELECT count(ar.id) + if(ISNULL(a.virtual_apply), 0, a.virtual_apply) count FROM ( SELECT * from amuse_t_activity where id = ",
							id.toString(),
							" ) a LEFT JOIN amuse_r_activity_record ar ON ar.activity_id = a.id AND ar.is_valid = 1");
					Number memberCount = queryDao.query(memberCountSql);
					if (memberCount == null) {
						memberCount = 0;
					}

					activity.put("applyCount", memberCount);
				}
			}
		}

		PageVO vo = new PageVO();
		vo.setList(list);
		vo.setTotal(total.intValue());
		int isLast = total.intValue() > page * pageSize ? 0 : 1;
		vo.setIsLast(isLast);
		return vo;
	}

	/**
	 * 查询用于app推荐的官方赛
	 *
	 * @return
	 */
	public List<Map<String, Object>> queryActivityForAppRecommend(int sign) {
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		// String signSql = "";
		// if (sign == 1) {
		// signSql = " and recommend_sign<>1";
		// } else if (sign == 2) {
		// signSql = " and recommend_sign<>2";
		// }
		String sql = "select id,title,areas from activity_t_info a where a.is_valid=1 and a.is_ground=1 and ((a.start_time<now() and now()<a.end_time) or (now()<a.over_time)) order by a.create_date desc";
		result.addAll(queryDao.queryMap(sql));
		return result;
	}

	/**
	 * 检查某项赛事是否被设置在广告推荐中
	 */
	public boolean isRecommend(Long activityId) {
		if (activityId != null) {
			String sql = SqlJoiner.join("select sum(count) from (",
					" select count(1) count from index_hot where type in (1, 4, 6) and is_valid = 1 and target_id = ",
					activityId.toString(),
					" union all select count(1) count from index_mid_img where type = 1 and is_valid = 1 and target_id = ",
					activityId.toString(),
					" union all select count(1) count from index_t_advertise where type = 10 and is_valid = 1 and target_id = ",
					activityId.toString(), " ) a");
			Number count = queryDao.query(sql);
			if (count != null && count.intValue() > 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 保存推荐标志
	 *
	 * @param type
	 * @param sign
	 * @param id
	 */
	public void saveRecommendSign(Integer type, Integer sign, Long id, boolean isSave) {
		if (type == null || sign == null || id == null) {
			return;
		}
		Integer recommendSign = -1;
		Integer newSign = -1;
		ActivityInfo activityInfo = null;
		AmuseActivityInfo amuse = null;
		if (type == 10) {
			activityInfo = activityInfoDao.findOne(id);
			recommendSign = activityInfo.getRecommendSign();
		} else if (type == 11) {
			amuse = amuseActivityInfoDao.findOne(id);
			recommendSign = amuse.getRecommendSign();
		}
		if (sign == 1) {
			if (isSave) {
				if (recommendSign != null && recommendSign == 2) {
					newSign = 0;
				} else {
					newSign = 1;
				}
			} else {
				if (recommendSign == 0) {
					newSign = 2;
				} else {
					newSign = -1;
				}
			}
		} else if (sign == 2) {
			if (isSave) {
				if (recommendSign != null && recommendSign == 1) {
					newSign = 0;
				} else {
					newSign = 2;
				}
			} else {
				if (recommendSign == 0) {
					newSign = 1;
				} else {
					newSign = -1;
				}
			}
		}
		if (type == 10) {
			activityInfo.setRecommendSign(newSign);
			activityInfoDao.save(activityInfo);
		} else if (type == 11) {
			amuse.setRecommendSign(newSign);
			amuseActivityInfoDao.save(amuse);
		}
	}

	public boolean alreadyRecommend(Integer type, Integer sign, Long id) {
		Integer recommendSign = null;
		if (type == 10) {
			ActivityInfo activityInfo = activityInfoDao.findOne(id);
			recommendSign = activityInfo.getRecommendSign();
		} else if (type == 11) {
			AmuseActivityInfo amuse = amuseActivityInfoDao.findOne(id);
			recommendSign = amuse.getRecommendSign();
		} else {
			return false;
		}
		if (recommendSign == 0) {
			return true;
		}
		if (sign == 1 && recommendSign == 1) {
			return true;
		}
		if (sign == 2 && recommendSign == 2) {
			return true;
		}
		return false;
	}

	/**
	 * 获取报名时间及场次信息
	 */
	public List<Map<String, Object>> getApplyDatesAndNetbars(Long activityId, Integer containTeam, Long userId) {
		if (activityId != null) {
			Joiner joiner = Joiner.on("_");
			String cacheKey = joiner.join(CacheKeyConstant.API_CACHE_ACTIVITY_APPLY_DATES_NETBARS,
					activityId.toString());
			List<Map<String, Object>> rounds = null;

			// 无缓存时重组
			if (CollectionUtils.isEmpty(rounds)) {
				String roundSql = SqlJoiner.join(
						"SELECT activity_id activityId, begin_time beginTime, over_time overTime, netbars, areas, round,",
						activityStatusSql, " FROM (SELECT *, over_time start_time",
						" FROM activity_r_rounds WHERE activity_id = ", activityId.toString(),
						" AND is_valid = 1) t ORDER BY t.over_time ASC");
				rounds = queryDao.queryMap(roundSql);

				if (CollectionUtils.isNotEmpty(rounds)) {
					// 查询场次下所有地区与网吧
					String areaSql = SqlJoiner.join("SELECT round_areas.round, a.name, a.area_code areaCode",
							" FROM ( SELECT DISTINCT ar.round, LEFT (a.area_code, 2) area_code",
							" FROM activity_r_rounds ar LEFT JOIN sys_t_area a ON FIND_IN_SET(a.area_code, ar.areas)",
							" WHERE ar.is_valid = 1 AND a.is_valid = 1 AND ar.activity_id = ", activityId.toString(),
							" ORDER BY ar.round ASC, a.id ASC ) round_areas",
							" LEFT JOIN sys_t_area a ON CONCAT(round_areas.area_code, '0000') = a.area_code WHERE a.is_valid = 1");
					List<Map<String, Object>> areas = queryDao.queryMap(areaSql);
					String netbarSql = SqlJoiner.join(
							"SELECT ar.round, n.name, n.area_code, n.address, n.longitude, n.latitude, n.price_per_hour, n.icon, n.id",
							" FROM activity_r_rounds ar JOIN netbar_t_info n ON FIND_IN_SET(n.id, ar.netbars)",
							" WHERE ar.is_valid = 1 AND n.is_valid = 1 AND n.is_release = 1 AND ar.activity_id = ",
							activityId.toString(), " ORDER BY ar.round ASC, n.id ASC");
					List<Map<String, Object>> netbars = queryDao.queryMap(netbarSql);

					// 匹配地区与网吧
					if (CollectionUtils.isNotEmpty(areas)) {
						if (CollectionUtils.isNotEmpty(netbars)) {
							for (Map<String, Object> a : areas) {
								List<Map<String, Object>> aNetbars = new ArrayList<Map<String, Object>>();
								for (Iterator<Map<String, Object>> it = netbars.iterator(); it.hasNext();) {
									Map<String, Object> n = it.next();
									Integer aRound = MapUtils.getInteger(a, "round");
									Integer nRound = MapUtils.getInteger(n, "round");
									String aAreaCode = MapUtils.getString(a, "areaCode");
									if (StringUtils.isNotBlank(aAreaCode)) {
										aAreaCode = aAreaCode.substring(0, 2);
									}
									String nAreaCode = MapUtils.getString(n, "area_code");
									if (StringUtils.isNotBlank(nAreaCode)) {
										nAreaCode = nAreaCode.substring(0, 2);
									}
									if (aRound != null && aRound.equals(nRound)
											&& StringUtils.equals(aAreaCode, nAreaCode)) {
										n.remove("round");
										aNetbars.add(n);
										it.remove();
									}
								}
								a.put("netbars", aNetbars);
							}
						}
					}

					// 匹配场次与地区
					for (Map<String, Object> r : rounds) {
						// r.put("activityId", null);
						Integer rRound = MapUtils.getInteger(r, "round");
						List<Map<String, Object>> rAreas = new ArrayList<Map<String, Object>>();
						if (CollectionUtils.isNotEmpty(areas)) {
							for (Iterator<Map<String, Object>> it = areas.iterator(); it.hasNext();) {
								Map<String, Object> a = it.next();
								Integer aRound = MapUtils.getInteger(a, "round");
								if (rRound != null && rRound.equals(aRound)) {
									a.remove("round");
									rAreas.add(a);
									it.remove();
								}
							}
						}

						r.put("areas", rAreas);
						r.put("date", r.get("overTime"));
						r.remove("netbars");
						r.remove("beginTime");
						r.remove("overTime");
						r.remove("id");
					}

					objectRedisOperateService.setData(cacheKey, rounds);
				}
			}

			// 匹配用户报名情况
			if (null != userId && CollectionUtils.isNotEmpty(rounds)) {
				List<Map<String, Object>> userMembers = null;
				if (null != userId) {
					String sqlUserMembers = "select id, round,team_id from activity_t_member where activity_id = "
							+ activityId + " and user_id = " + userId + " and is_valid = 1 and is_enter = 1";
					userMembers = queryDao.queryMap(sqlUserMembers);
				}

				for (Map<String, Object> r : rounds) {
					// 匹配用户报名情况
					Integer rRound = MapUtils.getInteger(r, "round");
					int hasApply = 0;
					if (CollectionUtils.isNotEmpty(userMembers)) {
						for (Map<String, Object> m : userMembers) {
							Integer mRound = MapUtils.getInteger(m, "round");
							if (null != rRound && rRound.equals(mRound)) {
								Long teamId = MapUtils.getLong(m, "team_id");
								if (null != teamId && teamId > 0) {
									hasApply = 2;// 战队报名
								} else {
									hasApply = 1;// 个人报名
								}
								break;
							}
						}
					}
					r.put("hasApply", hasApply);
				}
			}

			if (CommonConstant.INT_BOOLEAN_TRUE.equals(containTeam) && CollectionUtils.isNotEmpty(rounds)) {
				for (Map<String, Object> r : rounds) {
					insertAppliedTeam(activityId, r);
				}
			}

			return rounds;
		}

		return null;
	}

	/**
	 * 查询已报战队
	 */
	public PageVO appliedTeams(int page, Long lastTotal, Integer rows, Long userId, Long activityId, Integer round,
			Long netbarId) {
		PageVO vo = new PageVO();
		vo.setCurrentPage(page);

		if (activityId != null) {
			// 设置默认值
			if (null == round) {
				round = 1;
			}
			String netbarSql = SqlJoiner.join("SELECT netbars, ", activityStatusSql,
					" FROM (SELECT *, over_time start_time FROM activity_r_rounds WHERE activity_id = ",
					activityId.toString(), " AND round = ", round.toString(), " AND is_valid = 1 limit 1) t");
			Map<String, Object> aRound = queryDao.querySingleMap(netbarSql);
			Integer status = MapUtils.getInteger(aRound, "status");
			if (null == netbarId) {
				String netbars = MapUtils.getString(aRound, "netbars");
				if (StringUtils.isNotBlank(netbars)) {
					int splitIndex = netbars.indexOf(",");
					if (splitIndex > -1) {
						netbars = netbars.substring(0, splitIndex);
					}
				}

				// 匹配不出网吧ID时为0，查询结果将为空
				netbarId = NumberUtils.toLong(netbars, 0);
			}

			String totalSql = SqlJoiner.join("SELECT count(1) count", " FROM activity_t_team t ",
					" WHERE t.is_valid = 1 AND t.activity_id = ", activityId.toString(), " AND t.round = ",
					round.toString(), " AND t.netbar_id = ", netbarId.toString());
			Number total = queryDao.query(totalSql);

			String limit = "";
			Integer pageSize = rows == null ? PageUtils.API_DEFAULT_PAGE_SIZE : rows;
			if (page > 0) {
				int addStartRow = lastTotal == null ? 0 : total.intValue() - lastTotal.intValue();
				Integer startRow = (page - 1) * pageSize + addStartRow;
				limit = SqlJoiner.join(" LIMIT ", startRow.toString(), ", ", pageSize.toString());
			}

			String isJoinField = "";
			String isJoinJoin = "";
			if (null != userId) {
				isJoinField = ",IF(ISNULL(um.id), 0, 1) is_join";
				isJoinJoin = " LEFT JOIN activity_t_member um ON t.id = um.team_id AND um.is_valid = 1 AND um.is_enter = 1 AND um.user_id = "
						+ userId;
			}
			String sql = SqlJoiner.join(
					"SELECT t.id team_id, t.name team_name, m.name header, m.user_id header_user_id, count(1) num, t.round, 5 total_num",
					isJoinField, " FROM activity_t_team t LEFT JOIN activity_t_member m ON t.mem_id = m.id",
					" LEFT JOIN activity_t_member cm ON t.id = cm.team_id AND cm.is_valid = 1", isJoinJoin,
					" WHERE t.is_valid = 1 AND t.activity_id = ", activityId.toString(), " AND t.round = ",
					round.toString(), " AND t.netbar_id = ", netbarId.toString(), " GROUP BY t.id", limit);
			List<Map<String, Object>> list = queryDao.queryMap(sql);
			if (CollectionUtils.isNotEmpty(list)) {
				for (Map<String, Object> m : list) {
					m.put("status", status);
				}
			}

			vo.setList(list);
			vo.setTotal(total.intValue());
			int isLast = total.intValue() > page * pageSize ? 0 : 1;
			vo.setIsLast(isLast);
			return vo;
		}

		vo.setTotal(0);
		vo.setIsLast(1);
		return vo;
	}

	/**
	 * 赛程加入已报战队信息
	 *
	 * @param activityId
	 * @param r
	 */
	public void insertAppliedTeam(Long activityId, Map<String, Object> r) {
		String id = String.valueOf(activityId);
		String sql = SqlJoiner.join(
				"select team_id,num,total_num,team_name,header,round from (select a.create_date,a.id team_id,a.name team_name,num,5 total_num,d.nickname header,a.round from (activity_t_team a,activity_t_member c,user_t_info d) left join(select team_id,count(1) num from activity_t_member where is_valid=1 group by team_id)b on a.id=b.team_id where a.is_valid=1 and a.id=c.team_id and c.is_monitor=1 and c.is_valid=1 and c.user_id=d.id and a.activity_id=",
				id,
				")x where (select count(1) from (select a.create_date,a.id,a.name,num,5 total_num,d.nickname,a.round from (activity_t_team a,activity_t_member c,user_t_info d) left join(select team_id,count(1) num from activity_t_member where is_valid=1 group by team_id)b on a.id=b.team_id where a.is_valid=1 and a.id=c.team_id and c.is_monitor=1 and c.is_valid=1 and c.user_id=d.id and a.activity_id=",
				id, ")y where x.round=y.round and x.create_date<y.create_date)<4 order by round,create_date desc");
		List<Map<String, Object>> list = queryDao.queryMap(sql);
		List<Map<String, Object>> teamList = new ArrayList<Map<String, Object>>();
		Number round = (Number) r.get("round");
		if (CollectionUtils.isNotEmpty(list)) {
			for (Map<String, Object> map : list) {
				Object round_ = map.get("round");
				if (round_ != null && ((Number) round_).intValue() == round.intValue()) {
					map.put("round", null);
					teamList.add(map);
				}
			}
			r.put("teams", teamList);
		}

	}

	/**
	 * 后台管理系统推送模块查询官方赛事
	 */
	public List<Map<String, Object>> findValidByTitle(String title) {
		String sql = "select id,title from activity_t_info where is_valid =1 and title like '%" + title
				+ "%' and end_time>now()";
		return queryDao.queryMap(sql);
	}

	/**
	 * 查询场次信息
	 */
	public PageVO queryChangciInfo(Integer page, Long id, String overTime, String areaInfoId, Long netbarInfoId) {

		int pageSize = PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		int start = (page - 1) * pageSize;
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("start", start);
		params.put("pageSize", pageSize);
		String addsql = "";
		if (StringUtils.isNotBlank(overTime)) {
			addsql += " and date_format(a.over_time,'%Y-%m-%d')='" + overTime + "'";
		}
		if (StringUtils.isNotBlank(areaInfoId)) {
			addsql += " and b.area_code=" + areaInfoId;
		}
		if (netbarInfoId != null) {
			addsql += " and b.id=" + netbarInfoId;
		}

		String querysql = "SELECT a.id,a.activity_id,a.over_time,a.netbars,b.area_code,b.name netbarName,c.name areaName,d.qrcode,d.id codeId FROM activity_r_rounds a LEFT JOIN netbar_t_info b ON FIND_IN_SET(b.id,a.netbars) LEFT JOIN sys_t_area c ON b.area_code=c.area_code left join activity_netbar_qrcode d on d.activity_id=a.activity_id and d.netbar_id=b.id and a.round=d.round where a.is_valid = 1 and a.activity_id="
				+ id;
		String sqlTotal = "select count(1) from (SELECT 1 FROM activity_r_rounds a LEFT JOIN netbar_t_info b ON FIND_IN_SET(b.id,a.netbars) LEFT JOIN sys_t_area c ON b.area_code=c.area_code where a.is_valid = 1 and a.activity_id="
				+ id;
		if (addsql != "") {
			sqlTotal += addsql;
			querysql += addsql;
		}
		querysql += " group by a.round,b.id limit :start,:pageSize";
		sqlTotal += " group by a.round,b.id) a";
		List<Map<String, Object>> result = queryDao.queryMap(querysql, params);
		PageVO vo = new PageVO();
		vo.setList(result);
		Number countNum = queryDao.query(sqlTotal);
		if (countNum != null) {
			vo.setTotal(countNum.longValue());
		}

		return vo;
	}

	/**
	 * 查询比赛地区
	 */
	public List<Map<String, Object>> queryAreaInfo(Long id) {
		String sql = "SELECT c.area_code,c.name areaName FROM activity_r_rounds a LEFT JOIN netbar_t_info b ON FIND_IN_SET(b.id, a.netbars) LEFT JOIN sys_t_area c ON b.area_code = c.area_code WHERE a.is_valid = 1 AND c.name is not null and a.activity_id = "
				+ id + " group by c.name";
		return queryDao.queryMap(sql);
	}

	/**
	 * 查询比赛赛点
	 */
	public List<Map<String, Object>> queryNetbarInfo(Long id) {
		String sql = "SELECT b.id,b.name netbarName FROM activity_r_rounds a LEFT JOIN netbar_t_info b ON FIND_IN_SET(b.id, a.netbars) WHERE a.is_valid = 1 AND b.name is not null and a.activity_id = "
				+ id + " group by b.name order by b.id";
		return queryDao.queryMap(sql);
	}

	/**
	 * 查询比赛信息
	 */
	public List<Map<String, Object>> queryGameInfo(Long id) {
		String sql = "select a.qrcode,a.title,b.name areaname,a.begin_time,a.over_time,a.end_time from activity_t_info a  LEFT JOIN sys_t_area b on CONCAT(LEFT(a.area_code, 2), '0000') = b.area_code where a.id = "
				+ id;
		return queryDao.queryMap(sql);
	}

	/**
	 * 查询场次网吧信息
	 */
	public List<Map<String, Object>> queryRoundInfo(Long id) {
		String sql = "select a.round,a.netbars from activity_r_rounds a  where a.activity_id = " + id
				+ " and a.is_valid = 1";
		return queryDao.queryMap(sql);
	}

	public Map<String, Object> qrcode(Long userId, Long id, Integer round, Long netbarId, Long teamId) {
		Map<String, Object> result = new HashMap<String, Object>();
		String sql = "select count(1) from activity_r_rounds where is_valid=1 and activity_id=" + id + " and round="
				+ round + " and now()<date_add(over_time,INTERVAL 1 day)";
		Number n = queryDao.query(sql);
		if (n == null || n.intValue() == 0) {
			result.put("code", 2);// 二维码已过期
			return result;
		}
		sql = "select signed from activity_t_member where is_valid=1 and activity_id=" + id + " and round=" + round
				+ " and is_enter=1 and user_id=" + userId;
		Map<String, Object> map = queryDao.querySingleMap(sql);
		if (map != null) {
			if (map.get("signed") == null || ((Number) map.get("signed")).intValue() == 0) {
				queryDao.update("update activity_t_member set signed=1 where is_valid=1 and activity_id=" + id
						+ " and round=" + round + " and is_enter=1 and user_id=" + userId);
				result.put("code", 4);// 签到完成
				return result;
			} else {
				result.put("code", 3);// 已经报名当前场次
				return result;
			}
		}
		if (teamId == null) {
			teamId = 0L;
		}
		sql = "select case when a.person_allow=1 then 1 when a.team_allow=1 then 2 end person_or_team,a.id, a.title, b.over_time, d. name area, c. name, c.address, e. name team_name,e.id team_id from activity_t_info a left join activity_r_rounds b on a.id = b.activity_id left join netbar_t_info c on c.id ="
				+ netbarId
				+ " left join sys_t_area d on c.area_code = d.area_code left join activity_t_team e on e.id =" + teamId
				+ " where a.id =" + id + " and b.round = " + round;
		result.put("activityInfo", queryDao.querySingleMap(sql));
		result.put("card", activityCardService.findValidOneByUserId(userId));
		return result;

	}

	public int _2016cecQueryRoundByActivityIdAndNetbarId(Long activityId, Long netbarId) {
		Map<String, Object> result = queryDao
				.querySingleMap("select round from activity_r_rounds where is_valid =1 and activity_id=" + activityId
						+ " and netbars='" + netbarId + "'");
		if (MapUtils.isNotEmpty(result)) {
			Number round = (Number) result.get("round");
			if (round != null) {
				return round.intValue();
			}
		}
		return 1;
	}
}