package com.miqtech.master.service.amuse;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.support.atomic.RedisAtomicInteger;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.miqtech.master.consts.AmuseConstant;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.amuse.AmuseActivityInfoDao;
import com.miqtech.master.entity.amuse.AmuseActivityInfo;
import com.miqtech.master.entity.amuse.AmuseActivityRecord;
import com.miqtech.master.entity.amuse.AmuseVerify;
import com.miqtech.master.service.common.ObjectRedisOperateService;
import com.miqtech.master.utils.DateUtils;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.utils.StringUtils;
import com.miqtech.master.vo.PageVO;

/**
 * 娱乐赛操作service
 */
@Component
public class AmuseActivityInfoService {
	@Autowired
	private AmuseActivityInfoDao activityInfoDao;
	@Autowired
	private QueryDao queryDao;
	@Autowired
	private AmuseActivityRecordService amuseActivityRecordService;
	@Autowired
	private AmuseVerifyService amuseVerifyService;
	@Autowired
	private ObjectRedisOperateService objectRedisOperateService;

	public List<AmuseActivityInfo> findAllValid() {
		return activityInfoDao.findByValid(CommonConstant.INT_BOOLEAN_TRUE);
	}

	/**
	 * 保存
	 */
	public void save(AmuseActivityInfo activityInfo) {
		if (activityInfo != null) {
			activityInfoDao.save(activityInfo);
		}
	}

	/**
	 * 根据ID查实体
	 */
	public AmuseActivityInfo findById(Long id) {
		return activityInfoDao.findOne(id);
	}

	/**
	 * 根据多个ID查询有效娱乐赛
	 */
	public List<AmuseActivityInfo> findValidByIds(List<Long> ids) {
		if (CollectionUtils.isEmpty(ids)) {
			return null;
		}
		return activityInfoDao.findByIdInAndValid(ids, CommonConstant.INT_BOOLEAN_TRUE);
	}

	/**
	 * API3.0接口：娱乐赛列表
	 * 0.type为空,大厅列表：一级排序--发布时间
	 * 1.type=1,官方线上列表：一级排序-进行状态,二级排序-发布时间
	 * 2.type=2,官方线下列表：一级排序-进行状态,二级排序-距离,三级排序-发布时间
	 * 3.type=3,网吧线下列表：一级排序-进行状态,二级排序-距离,三级排序-发布时间
	 */
	public Map<String, Object> appAmuseList(int page, Map<String, Object> params) {
		int rows = PageUtils.API_DEFAULT_PAGE_SIZE;
		int isLast = 0;
		if (page < 1) {
			page = 1;
		}
		int start = (page - 1) * rows;
		int whichIcon = 1; //0-banner图，1-列表图（小）
		String sqlList = " select a.id, mainIcon, if((ifnull(applyNum,0))+(ifnull(a.virtual_apply,0)) > a.max_num, a.max_num, (ifnull(applyNum,0))+(ifnull(a.virtual_apply,0))) applyNum, a.title, a.sub_title subTitle, a.summary, a.type, a.way, n.price_per_hour netbarPrice, n.name netbarName, s.name areaName, a.reward, a.rule, a.max_num maxNum, a.start_date startDate, a.end_date endDate";
		String calcDistince = ", 0 distance";
		//时间状态：2-报名即将开始，0-报名中，1-进行中，3-赛事已结束
		//统一 ：时间状态： 1 报名中, 2 报名预热中, 3 报名已截止, 4 赛事已结束, 5 赛事进行中(6 赛事未开始)（用于排序：'1,5,2,3,4,6'）
		String timeStatus = ", IF (now() < a.apply_start, 2, IF (now() >= a.apply_start AND now() < a.start_date, 1, IF (now() >= a.start_date AND now() < a.end_date, 5, 4))) timeStatus";
		String sqlApplyNum = " left join (select count(id) applyNum, activity_id from amuse_r_activity_record where is_valid=1 and state in(0,1) group by activity_id)r on r.activity_id=a.id";
		String sqlAreaCode = "";
		String sqlJoinAreaCode = " LEFT join netbar_t_info n on n.id=a.netbar_id";
		String sqlType = "";
		String sqlTakeType = "";
		String sqlItemId = "";
		String sqlAwardtype = "";
		String sqlOrderBy = " order by";
		String currentCity = "";
		String areaCode = null;
		if (null == params) {
			params = Maps.newHashMap();
		}
		if (params.get("areaCode") != null) {
			areaCode = params.get("areaCode").toString();
		}
		if (null != params.get("longitude") && null != params.get("latitude")) { //传了经纬度
			calcDistince = ", calc_distance (n.longitude, :longitude, n.latitude, :latitude) distance";
		}
		if (null != params.get("type")) {
			int typeInt = NumberUtils.toInt(params.get("type").toString());
			whichIcon = 1;// 列表采用列表图
			sqlType = " and a.type=:type";
			if (typeInt == 1) {
				sqlOrderBy = SqlJoiner.join(sqlOrderBy,
						" find_in_set(timeStatus, '1,5,2,3,4,6') asc, a.release_date desc");
			} else if (typeInt == 2 || typeInt == 3) {
				sqlOrderBy = SqlJoiner.join(sqlOrderBy,
						" find_in_set(timeStatus, '1,5,2,3,4,6') asc, distance asc, a.release_date desc");
				if (null != params.get("areaCode")) { //传了地区code,精确到前4位(市级)
					sqlAreaCode = " and n.area_code like concat(:areaCode, '%')";
				}
			} else {
				sqlOrderBy = SqlJoiner.join(sqlOrderBy, " a.release_date desc");
			}

			if (typeInt != 2 && typeInt != 3) {
				params.remove("areaCode");
			}
		} else {
			sqlOrderBy = SqlJoiner.join(sqlOrderBy, " find_in_set(timeStatus, '1,5,2,3,4,6') asc, a.release_date desc");
			whichIcon = 1;
			params.remove("areaCode");
			sqlAreaCode = "";
			if (areaCode != null) {
				currentCity = " and (way=2 or n.area_code like '" + areaCode + "%')";
			} else {
				currentCity = " and way=2 ";
			}
		}
		String sqlMainIcon = " left join (select icon mainIcon, activity_id from amuse_r_activity_icon where is_main="
				+ whichIcon + ")i on i.activity_id=a.id";
		if (null != params.get("takeType")) {
			sqlTakeType = " and a.take_type=:takeType";
		}
		if (null != params.get("itemId")) {
			sqlItemId = " and a.item_id=:itemId";
		}
		if (null != params.get("awardtype")) {
			sqlAwardtype = " and a.award_type=:awardtype";
		}
		sqlList = SqlJoiner.join(sqlList, calcDistince, timeStatus, " from amuse_t_activity a", sqlApplyNum,
				sqlMainIcon, sqlJoinAreaCode, " left join sys_t_area s on s.area_code=n.area_code",
				" where a.is_valid=1 and a.state=2 ", sqlAreaCode, currentCity, sqlType, sqlTakeType, sqlItemId,
				sqlAwardtype);
		sqlList = SqlJoiner.join(sqlList, sqlOrderBy, " limit :page, :rows");
		params.put("page", (page - 1) * PageUtils.API_DEFAULT_PAGE_SIZE);
		params.put("rows", PageUtils.API_DEFAULT_PAGE_SIZE);
		List<Map<String, Object>> list = queryDao.queryMap(sqlList, params);

		String sqlTotal = SqlJoiner.join("select count(1) total from amuse_t_activity a ", sqlJoinAreaCode,
				" where a.is_valid=1 and a.state=2 ", sqlAreaCode, currentCity, sqlType, sqlTakeType, sqlItemId,
				sqlAwardtype);
		params.remove("longitude");
		params.remove("page");
		params.remove("rows");
		params.remove("latitude");
		Map<String, Object> total = queryDao.querySingleMap(sqlTotal, params);

		if (null == total || null == total.get("total")
				|| (start + rows) >= NumberUtils.toInt(total.get("total").toString())) {
			isLast = 1;
		}
		Map<String, Object> returnMap = Maps.newHashMap();
		returnMap.put("list", list);
		returnMap.put("isLast", isLast);
		return returnMap;
	}

	/**
	 * API3.0接口：娱乐赛详情，包括状态
	 */
	public Map<String, Object> getAmuseDetailInfoById(long amuseId, Long userId) {
		String sqlAmuseInfo = SqlJoiner
				.join("select a.id, a.summary, a.way, a.virtual_apply, banner, a.title, a.sub_title subTitle, ifnull(i.name,'') itemName, i.icon itemIcon, a.server, n.price_per_hour netbarPrice, n.name netbarName, n.id netbarId, n.longitude, n.latitude, n.icon netbarIcon, a.reward, a.rule, a.start_date startDate, a.end_date endDate, a.max_num maxNum,favor_num, if(x.id is null,0,1) has_favor,a.verify_content verifyContent",
						" from amuse_t_activity a left join (select sub_id,count(1) favor_num from user_r_favor where type=5 and is_valid=1 group by sub_id)y on a.id=y.sub_id left join user_r_favor x on a.id=x.sub_id and x.type=5 and x.is_valid=1 and x.user_id=",
						String.valueOf(userId), " left join netbar_t_info n on n.id=a.netbar_id",
						" left join activity_r_items i on i.id=a.item_id",
						" left join (select icon banner, activity_id from amuse_r_activity_icon where is_main = 1 and activity_id="
								+ amuseId + "  limit 1)i on activity_id=" + amuseId,
						" where a.is_valid=1 and a.id=" + amuseId);
		Map<String, Object> returnMap = queryDao.querySingleMap(sqlAmuseInfo);
		if (null == returnMap) {
			return null;
		}
		List<Map<String, Object>> applyerList = amuseApplyerList(amuseId, 10);
		returnMap.put("applyerList", applyerList);
		int applyNum = getApplyNumByAmuseId(amuseId) + (NumberUtils
				.toInt(null == returnMap.get("virtual_apply") ? "" : returnMap.get("virtual_apply").toString()));
		if (null != returnMap.get("maxNum")) {
			int maxNum = NumberUtils.toInt(returnMap.get("maxNum").toString());
			if (applyNum > maxNum) {
				applyNum = maxNum;
			}
		}
		returnMap.put("applyNum", applyNum);
		return returnMap;
	}

	/**
	 * API3.0接口：娱乐赛报名人员列表
	 */
	public List<Map<String, Object>> amuseApplyerList(long amuseId, int size) {

		String sqlApplyers = SqlJoiner.join("select u.id userId, u.icon, u.nickname from amuse_r_activity_record r",
				" left join user_t_info u on u.id=r.user_id",
				" where r.is_valid=1 and r.state in(0,1) and r.activity_id=" + amuseId);
		if (size > 0) {
			sqlApplyers = SqlJoiner.join(sqlApplyers, " limit 0, " + size);
		}
		List<Map<String, Object>> result = queryDao.queryMap(sqlApplyers);
		if (!CollectionUtils.isEmpty(result)) {
			int tmp = size - result.size();
			result.addAll(this.virtualUser(tmp));
		} else {
			result.addAll(this.virtualUser(size));
		}
		return result;
	}

	public PageVO applyList(Long id, int type, Integer page, Integer pageSize) {
		PageVO vo = new PageVO();
		String sql = "";
		if (type == 1) {
			sql = SqlJoiner.join("select count(1) from amuse_r_activity_record r",
					" left join user_t_info u on u.id=r.user_id",
					" where r.is_valid=1 and r.state in(0,1) and r.activity_id=" + id);
		} else if (type == 2) {
			sql = SqlJoiner.join("select count(1) ",
					" from activity_t_member m left join user_t_info u on m.user_id = u.id and u.is_valid = 1 ",
					" where m.activity_id =" + id,
					" and m.is_valid = 1 and m.is_enter = 1  order by m.create_date desc ");
		}
		Number total = queryDao.query(sql);
		if (total != null) {
			vo.setTotal(total.intValue());
		}
		if (page == null) {
			page = 1;
		}
		if (pageSize == null) {
			pageSize = PageUtils.API_DEFAULT_PAGE_SIZE;
		}
		if (page * pageSize >= total.intValue()) {
			vo.setIsLast(1);
		}
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("start", (page - 1) * pageSize);
		params.put("pageSize", pageSize);
		if (type == 1) {
			sql = SqlJoiner.join("select u.id userId, u.icon, u.nickname from amuse_r_activity_record r",
					" left join user_t_info u on u.id=r.user_id",
					" where r.is_valid=1 and r.state in(0,1) and r.activity_id=" + id, " limit :start,:pageSize");
		} else if (type == 2) {
			sql = SqlJoiner.join("select u.id,u.id userId, u.nickname, u.icon icon ",
					" from activity_t_member m left join user_t_info u on m.user_id = u.id and u.is_valid = 1 ",
					" where m.activity_id =" + id,
					" and m.is_valid = 1 and m.is_enter = 1  order by m.create_date desc limit :start,:pageSize");
		}
		vo.setList(queryDao.queryMap(sql, params));
		return vo;
	}

	public List<Map<String, Object>> virtualUser(int virtualNum) {
		return queryDao.queryMap(
				"select id userId,nickname,icon from user_t_info where username like '17155952___' order by RAND() limit "
						+ virtualNum);
	}

	/**
	 * API3.0接口：报名信息必填条目
	 */
	public Map<String, Object> amuseApplyRequiredInfo(long amuseId) {
		String sqlApplyers = "select tel_req telephone, name_req name, account_req gameAccount, server_req server, qq_req qq, id_card_req idCard, team_name_req teamName from amuse_t_activity where id="
				+ amuseId;
		return queryDao.querySingleMap(sqlApplyers);
	}

	/**
	 * API3.0接口：查娱乐赛时间状态
	 * 时间状态：1-报名中；;2-报名预热中；3-报名已截止;4-赛事已结束5比赛进行中 6-认证已截止
	 */
	public Map<String, Object> getAmuseTimeStatus(long amuseId) {
		String sqlTimeStatus = "select IF (now() < a.apply_start, 2, IF (now() >= a.apply_start AND now() < a.start_date, 1, IF (now() >= a.start_date AND now() < a.end_date, 5, if(now() < a.verify_end_date, 4, 6)))) timeStatus from amuse_t_activity a where id="
				+ amuseId;
		return queryDao.querySingleMap(sqlTimeStatus);
	}

	/**
	 * API3.0接口：查用户报名状态
	 * 报名状态：-1-报名后放弃比赛/未报名；0-报名后未提交审核；1-已提交审核
	 */
	public int getAmuseApplyStatus(Long amuseId, long userId) {
		String sqlAmuseId = StringUtils.EMPTY;
		if (null != amuseId) {
			sqlAmuseId = SqlJoiner.join(" and activity_id = ", amuseId.toString());
		}

		String sqlSubmitStatus = SqlJoiner.join(
				"select count(1) from amuse_r_activity_record where is_valid=1 and state = 1 and user_id = ",
				String.valueOf(userId), sqlAmuseId);
		Number countSubmit = queryDao.query(sqlSubmitStatus);
		if (null != countSubmit && countSubmit.intValue() > 0) {
			return 1;
		} else {
			String sqlApplyStatus = SqlJoiner.join(
					"select count(1) from amuse_r_activity_record where is_valid=1 and state = 0 and user_id = ",
					String.valueOf(userId), sqlAmuseId);
			Number countApply = queryDao.query(sqlApplyStatus);
			if (null != countApply && countApply.intValue() > 0) {
				return 0;
			}
		}
		return -1;
	}

	/**
	 * 检查用户是否在某场娱乐赛中已报名
	 */
	public boolean isApplied(Long userId, Long activityId) {
		if (userId == null || activityId == null) {
			return false;
		}

		String sql = SqlJoiner.join(
				"SELECT count(1) FROM amuse_r_activity_record r WHERE r.state >= 0 and r.user_id = ", userId.toString(),
				" and r.activity_id = ", activityId.toString());
		Number count = queryDao.query(sql);
		if (null == count || count.intValue() <= 0) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * 查用户当前是否有 已报名未提交 娱乐赛
	 */
	public boolean judgeApplyed(long userId) {
		String sqlApplyStatus = "SELECT count(1) FROM amuse_r_activity_record r LEFT JOIN amuse_t_activity a ON a.id = r.activity_id WHERE r.is_valid = 1 AND r.state = 0 AND NOW() < a.verify_end_date AND r.user_id = "
				+ userId;
		Number countApply = queryDao.query(sqlApplyStatus);
		if (null != countApply && countApply.intValue() > 0) {
			return true;
		}
		return false;
	}

	/**
	 * API3.0：查某娱乐赛已报名人数
	 */
	public int getApplyNumByAmuseId(long amuseId) {
		String sqlCount = "select count(1) from amuse_r_activity_record where is_valid=1 and state in(0,1) and activity_id="
				+ amuseId;
		Number count = queryDao.query(sqlCount);
		if (null == count) {
			return 0;
		} else {
			return count.intValue();
		}
	}

	/**
	 * API3.0：查用户今天是否有完成过一场比赛：false-否,true-是
	 */
	public boolean judgeFinishOneAmuseToday(long userId) {
		boolean result = false;
		String sqlFinishToday = "select count(1) from amuse_r_activity_record where state=1 and TO_DAYS(verify_date)=TO_DAYS(now()) and user_id="
				+ userId;
		Number countToday = queryDao.query(sqlFinishToday);
		if (null != countToday && countToday.intValue() > 0) {
			result = true;
		}
		return result;
	}

	/**
	 * 查询用户的报名及认证状态信息
	 */
	public Map<String, Object> getUserApplyStatus(Long activityId, Long userId) {
		Map<String, Object> result = Maps.newHashMap();

		boolean hasApply = false;
		boolean hasVerify = false;
		if (activityId == null || userId == null) {
			result.put("hasApply", hasApply);
			result.put("hasVerify", hasVerify);
			return result;
		}

		// 查询报名及认证情况
		AmuseActivityRecord record = amuseActivityRecordService.findValidOneByActivityIdAndUserId(activityId, userId);
		if (record != null) {
			hasApply = true;

			AmuseVerify verify = amuseVerifyService.findValidOneByActivityIdAndUserId(activityId, userId);
			if (verify != null) {
				hasVerify = true;
			}
		}

		result.put("hasApply", hasApply);
		result.put("hasVerify", hasVerify);
		return result;
	}

	/**
	 * API接口：（娱乐赛）活动列表
	 * 1.有限展示线上比赛，way=2
	 * 2.展示当前定位城市的所有线下比赛，按照距离远近从近到远排列，way=1
	 */
	public List<Map<String, Object>> list(int page, String longitude, String latitude, String areaCode, int way) {
		if (page < 1) {
			page = 1;
		}
		String sqlNum = ", (select count(r.id) from amuse_r_activity_record r where r.is_valid=1 and r.activity_id=a.id) num";
		String sqlMainIcon = ",  (select icon from amuse_r_activity_icon i where i.activity_id=a.id and i.is_main=1) mainIcon";
		//线上
		String sqlOnline = SqlJoiner.join(
				" select a.id, 0 flag, a.create_date, a.title, a.type, a.way, '' netbarName, '' areaName, a.server, a.reward, a.rule, a.max_num maxNum, a.contact, a.is_release, a.is_recommend, a.start_date startDate, a.end_date endDate, 0 distance",
				sqlNum, sqlMainIcon, " from amuse_t_activity a ", " where a.is_valid=1 and a.way=2");
		//线下
		String sqlOffline = " select a.id, 1 flag, a.create_date, a.title, a.type, a.way, n.name netbarName, s.name areaName, a.server, a.reward, a.rule, a.max_num maxNum, a.contact, a.is_release, a.is_recommend, a.start_date startDate, a.end_date endDate";
		String sqlAreaCode = "";
		String calcDistince = ", 0 distance";
		Map<String, Object> params = Maps.newHashMap();
		if (StringUtils.isAllNotBlank(longitude, latitude)) { //传了经纬度
			params.put("longitude", longitude);
			params.put("latitude", latitude);
			calcDistince = ", calc_distance (n.longitude, :longitude, n.latitude, :latitude) distance";
		}
		if (StringUtils.isNotBlank(areaCode)) { //传了地区code
			params.put("areaCode", areaCode);
			sqlAreaCode = " and n.area_code like concat(:areaCode, '%')";
		}
		sqlOffline = SqlJoiner.join(sqlOffline, calcDistince, sqlNum, sqlMainIcon, " from amuse_t_activity a",
				" right join netbar_t_info n on n.id=a.netbar_id", sqlAreaCode,
				" left join sys_t_area s on s.area_code=n.area_code", " where a.is_valid=1 and a.way=1");
		params.put("page", (page - 1) * PageUtils.API_DEFAULT_PAGE_SIZE);
		params.put("rows", PageUtils.API_DEFAULT_PAGE_SIZE);
		String sql = SqlJoiner.join("select * from (", sqlOnline, " union all ", sqlOffline,
				") result order by flag asc, distance asc, create_date desc limit :page, :rows");
		//		if (way == 2) {
		//			String sql = SqlJoiner.join(sqlOnline, " limit " + page + ", " + PageUtils.API_DEFAULT_PAGE_SIZE);
		//			return queryDao.queryMap(sql);
		//		} else if (way == 1) {
		//			String sql = SqlJoiner.join(sqlOffline, " limit :page, :rows");
		//			return queryDao.queryMap(sql, params);
		//		} else {
		//			String sql = SqlJoiner.join("select * from (", sqlOnline, " union ", sqlOffline, ") result limit :page, :rows");
		return queryDao.queryMap(sql, params);
		//		}

	}

	/**
	 * API接口：根据id查商品详细信息，报名状态
	 */
	public Map<String, Object> getDetailById(Long activityId, Long userId) {
		if (null == activityId || !NumberUtils.isNumber(activityId.toString())) {
			return null;
		}
		String sqlNum = ", (select count(r.id) from amuse_r_activity_record r where r.is_valid=1 and r.activity_id=a.id) num";
		String sqlInfo = SqlJoiner.join(
				"select a.id, a.title, a.type, a.way, n.name netbarName, n.address, s.name areaName, a.server, a.reward, a.rule, a.max_num, a.contact, a.start_date startDate, a.end_date endDate, a.verify_content verifyContent",
				sqlNum, " from amuse_t_activity a", " left join netbar_t_info n on n.id=a.netbar_id",
				" left join sys_t_area s on s.area_code=n.area_code", " where a.is_valid=1 and a.id=" + activityId);
		String sqlbanner = "select icon from amuse_r_activity_icon where is_main <>1 and activity_id=" + activityId
				+ " limit 1";//banner图，暂时只有一张2015.11.6
		//查娱乐赛信息
		Map<String, Object> infoMap = queryDao.querySingleMap(sqlInfo);
		if (null == infoMap) {
			return null;
		}
		//娱乐赛时间状态
		infoMap.put("timeStatus", getAmuseTimeStatusById(activityId));
		//查用户报名状态isApply:-4-未登录；0-可报名；1-已报名；2-已报满；3-未开始；4；已结束
		if (null != userId) {
			String sqlApply = "select count(1) from amuse_r_activity_record r where r.is_valid=1 and r.activity_id="
					+ activityId + " and user_id=" + userId;
			int timeStatus = getAmuseTimeStatusById(activityId);
			if (timeStatus == 0) {
				infoMap.put("isApply", 3);
			} else if (timeStatus == 2) {
				infoMap.put("isApply", 4);
			} else {
				AmuseActivityInfo amuseActivityInfo = findById(activityId);
				if (null != amuseActivityInfo.getMaxNum() && amuseActivityRecordService
						.queryNumByAmuseActivityId(activityId) >= amuseActivityInfo.getMaxNum()) {
					infoMap.put("isApply", 2);
				} else {
					Number count = queryDao.query(sqlApply);
					if (null == count || count.intValue() < 1) {
						infoMap.put("isApply", 0);
					} else {
						infoMap.put("isApply", 1);

						// 查询认证状态
						AmuseVerify verify = amuseVerifyService.findValidOneByActivityIdAndUserId(activityId, userId);
						infoMap.put("isVerify", verify == null);
					}
				}
			}
		} else {
			infoMap.put("isApply", -4);
		}
		//查banner图
		Map<String, Object> map = queryDao.querySingleMap(sqlbanner);
		if (!CollectionUtils.isEmpty(map)) {
			infoMap.put("banner", map.get("icon"));
		} else {
			infoMap.put("banner", null);
		}

		return infoMap;
	}

	/**
	 * 根据id查活动时间状态：0-未开始，1-进行中，2-已结束
	 */
	public int getAmuseTimeStatusById(long activityId) {
		String sqlQuery = "select if(a.end_date < NOW(), 2, if(a.start_date > NOW(), 0, 1)) timeStatus from amuse_t_activity a where a.is_valid=1 and a.id="
				+ activityId;
		Map<String, Object> map = queryDao.querySingleMap(sqlQuery);
		if (!CollectionUtils.isEmpty(map) && null != map.get("timeStatus")) {
			return Integer.parseInt(map.get("timeStatus").toString());
		}
		return 0;
	}

	/**
	 * 后台管理:娱乐赛列表，分页
	 */
	public PageVO listPage(int page, Map<String, Object> params) {
		if (page < 1) {
			page = 1;
		}
		if (null == params) {
			params = Maps.newHashMap();
			params.put("valid", 1);
		} else if (null == params.get("valid")) {
			params.put("valid", 1);
		}
		String timeStatus = ", if(NOW() < a.end_date, 0, 1) isEnd"; //0-未结束，1-已结束
		String sqlApplyNum = " left join (select count(id) applyNum, activity_id from amuse_r_activity_record where is_valid=1 group by activity_id)r on r.activity_id=a.id";
		String sqlIcon = " left join (select id iconId, icon mainIcon, activity_id from amuse_r_activity_icon where is_main=1 )i1 on i1.activity_id=a.id";
		String sqlbanner = " left join (select id bannerId, icon banner, activity_id from amuse_r_activity_icon where is_main=0 )i2 on i2.activity_id=a.id";
		String sqlIndexShow = " left join (select group_concat(belong) indexShow, target_id from index_t_advertise where is_valid=1 and type=11  GROUP BY target_id)s1 on s1.target_id=a.id"; //indexShow：0-首页，1-竞技大厅
		String sqlMidShow = " left join (select group_concat(category) midShow, target_id from index_mid_img where is_valid=1 GROUP BY target_id)s2 on s2.target_id=a.id"; //midShow：1-腰图1，2-腰图2
		String sqlHotShow = " left join (select group_concat(type) hotShow, target_id from index_hot where is_valid=1 GROUP BY target_id)s3 on s3.target_id=a.id"; //hotType：2-热门娱乐赛，5-竞技大厅卡片转动区娱乐赛，7-娱乐比赛推荐
		String sqlNetbar = StringUtils.EMPTY;
		String sqlCount = "select count(1) from amuse_t_activity a";
		String sqlCountNetbar = StringUtils.EMPTY;
		if (null != params.get("areaCode")) { //精确到省级
			sqlNetbar = " right join netbar_t_info n on n.id=a.netbar_id and n.area_code like concat(:areaCode,'%')";
			sqlCountNetbar = " right join netbar_t_info n on n.id=a.netbar_id";
			sqlCount = SqlJoiner.join(sqlCount, sqlCountNetbar, " and n.area_code like '",
					params.get("areaCode").toString(), "%'");
		}
		if (null != params.get("netbarName")) {
			if (StringUtils.isBlank(sqlNetbar)) {
				sqlNetbar = " right join netbar_t_info n on n.id=a.netbar_id and n.name like concat('%',:netbarName,'%')";
			} else {
				sqlNetbar = SqlJoiner.join(sqlNetbar, " and n.name like concat('%',:netbarName,'%')");
			}
			if (StringUtils.isBlank(sqlCountNetbar)) {
				sqlCountNetbar = " right join netbar_t_info n on n.id=a.netbar_id";
				sqlCount = SqlJoiner.join(sqlCount, sqlCountNetbar);
			}
			sqlCount = SqlJoiner.join(sqlCount, " and n.name like '%", params.get("netbarName").toString(), "%'");
		}
		if (StringUtils.isBlank(sqlNetbar)) {
			sqlNetbar = " left join netbar_t_info n on n.id=a.netbar_id";
		}

		String sqlQuery = SqlJoiner.join(
				"select a.summary, a.verify_content verifyContent, a.deliver_day deliverDay,a.id, a.remark,",
				" indexShow, midShow, hotShow, applyNum, virtual_apply virtualApply, mainIcon, iconId, banner,",
				" bannerId, a.title, a.sub_title, a.server, a.way, a.netbar_id netbarId, n.name netbarName,",
				" n.address, n.telephone, mcc.name awardTypeName, a.state, a.item_id itemId, a.award_type awardType,",
				" a.award_sub_type awardSubType, a.award_amount awardAmount, a.reward, a.rule, a.contact,",
				" a.contact_type contactType, a.tel_req telReq, a.name_req nameReq, a.account_req accountReq,",
				" a.server_req serverReq, a.qq_req qqReq, a.id_card_req idCardReq, a.take_type takeType,",
				" a.max_num maxNum, a.apply_start applyStart, a.start_date startDate, a.end_date endDate,",
				" a.verify_end_date verifyEndDate, a.grant_msg grantMsg, a.is_valid valid", timeStatus,
				" from amuse_t_activity a", sqlApplyNum, sqlIcon, sqlbanner, sqlIndexShow, sqlMidShow, sqlHotShow,
				sqlNetbar, " LEFT JOIN mall_t_commodity_category mcc ON a.award_type = 3 AND a.award_sub_type = mcc.id",
				" where a.is_valid = :valid");
		sqlCount = SqlJoiner.join(sqlCount, " where a.is_valid=" + params.get("valid"));

		if (null != params.get("wy")) { //官方
			sqlQuery = SqlJoiner.join(sqlQuery, " and a.type in(1,2)");
			sqlCount = SqlJoiner.join(sqlCount, " and a.type in(1,2)");
			params.remove("wy");
		} else { //网吧
			sqlQuery = SqlJoiner.join(sqlQuery, " and a.type=3");
			sqlCount = SqlJoiner.join(sqlCount, " and a.type=3");
		}
		if (null != params.get("userId")) {
			sqlQuery = SqlJoiner.join(sqlQuery, " and a.create_user_id=:userId");
			sqlCount = SqlJoiner.join(sqlCount, " and a.create_user_id=" + params.get("userId"));
		}
		if (null != params.get("title")) {
			sqlQuery = SqlJoiner.join(sqlQuery, " and a.title like concat('%', :title, '%')");
			sqlCount = SqlJoiner.join(sqlCount, " and a.title like '%" + params.get("title") + "%'");
		}
		if (null != params.get("way")) {
			sqlQuery = SqlJoiner.join(sqlQuery, " and a.way=:way");
			sqlCount = SqlJoiner.join(sqlCount, " and a.way=" + params.get("way"));
		}
		if (null != params.get("awardType")) {
			sqlQuery = SqlJoiner.join(sqlQuery, " and a.award_type=:awardType");
			sqlCount = SqlJoiner.join(sqlCount, " and a.award_type=" + params.get("awardType"));
		}
		if (null != params.get("state")) {
			sqlQuery = SqlJoiner.join(sqlQuery, " and a.state=:state");
			sqlCount = SqlJoiner.join(sqlCount, " and a.state=" + params.get("state"));
		}
		if (null != params.get("startDate")) {
			sqlQuery = SqlJoiner.join(sqlQuery, " and a.start_date >= :startDate");
			sqlCount = SqlJoiner.join(sqlCount, " and a.start_date >= '" + params.get("startDate") + "'");
		}
		if (null != params.get("endDate")) {
			sqlQuery = SqlJoiner.join(sqlQuery, " and a.end_date <= ADDDATE(:endDate, INTERVAL 1 DAY)");
			sqlCount = SqlJoiner.join(sqlCount, " and a.end_date <=  ADDDATE('", params.get("endDate").toString(),
					"', INTERVAL 1 DAY)");
		}

		params.put("start", (page - 1) * PageUtils.ADMIN_DEFAULT_PAGE_SIZE);
		params.put("rows", PageUtils.ADMIN_DEFAULT_PAGE_SIZE);
		sqlQuery = SqlJoiner.join(sqlQuery, " order by a.create_date desc limit :start, :rows");

		PageVO pageVO = new PageVO();
		pageVO.setList(queryDao.queryMap(sqlQuery, params));

		Number total = queryDao.query(sqlCount);
		if (null == total) {
			pageVO.setTotal(0);
		} else {
			pageVO.setTotal(total.intValue());
		}

		return pageVO;
	}

	/**
	 * ##后台管理##:娱乐赛报名统计，分页
	 */
	public PageVO listApply(int page, Map<String, Object> params) {
		if (page < 1) {
			page = 1;
		}
		String sqlQuery = SqlJoiner.join(
				"select r.id, u.nickname, u.telephone, r.create_date createDate, r.qq, r.game_account gameAccount, r.server, r.id_card idCard, r.team_name teamName",
				" from amuse_r_activity_record r");
		String sqlCount = "select count(1) from amuse_r_activity_record r";
		String sqlJoin = StringUtils.EMPTY;
		if (null != params.get("nickname")) {
			sqlJoin = " right join user_t_info u on u.id=r.user_id";
			sqlQuery = SqlJoiner.join(sqlQuery, sqlJoin, " and u.nickname like concat('%', :nickname, '%')");
			sqlCount = SqlJoiner.join(sqlCount, sqlJoin, " and u.nickname like '%" + params.get("nickname") + "%'");
		}
		if (null != params.get("telephone")) {
			if (StringUtils.isBlank(sqlJoin)) {
				sqlJoin = " right join user_t_info u on u.id=r.user_id";
				sqlQuery = SqlJoiner.join(sqlQuery, sqlJoin);
				sqlCount = SqlJoiner.join(sqlCount, sqlJoin);
			}
			sqlQuery = SqlJoiner.join(sqlQuery, " and u.telephone like concat('%', :telephone, '%')");
			sqlCount = SqlJoiner.join(sqlCount, " and u.telephone like '%" + params.get("telephone") + "%'");
		}

		if (StringUtils.isBlank(sqlJoin)) {
			sqlJoin = " left join user_t_info u on u.id=r.user_id";
			sqlQuery = SqlJoiner.join(sqlQuery, sqlJoin);
			sqlCount = SqlJoiner.join(sqlCount, sqlJoin);
		}
		sqlQuery = SqlJoiner.join(sqlQuery, " where r.is_valid=1 and r.activity_id=:activityId");
		sqlCount = SqlJoiner.join(sqlCount, " where r.is_valid=1 and r.activity_id=" + params.get("activityId"));
		if (null != params.get("qq")) {
			sqlQuery = SqlJoiner.join(sqlQuery, " and r.qq like concat('%', :qq, '%')");
			sqlCount = SqlJoiner.join(sqlCount, " and r.qq like '%" + params.get("qq") + "%'");
		}
		if (null != params.get("startDate")) {
			sqlQuery = SqlJoiner.join(sqlQuery, " and r.create_date >= :startDate");
			sqlCount = SqlJoiner.join(sqlCount, " and r.create_date >= ", params.get("startDate").toString());
		}
		if (null != params.get("endDate")) {
			sqlQuery = SqlJoiner.join(sqlQuery, " and r.create_date <= ADDDATE(:endDate, INTERVAL 1 DAY)");
			sqlCount = SqlJoiner.join(sqlCount, " and r.create_date <= ADDDATE(", params.get("endDate").toString(),
					", INTERVAL 1 DAY)");
		}

		String sqlOrderAndLimit = " order by r.create_date";
		if (null == params.get("no_limit")) {
			params.put("start", (page - 1) * PageUtils.ADMIN_DEFAULT_PAGE_SIZE);
			params.put("rows", PageUtils.ADMIN_DEFAULT_PAGE_SIZE);
			sqlOrderAndLimit = SqlJoiner.join(sqlOrderAndLimit, " limit :start, :rows");
		} else {
			params.remove("no_limit");
		}
		sqlQuery = SqlJoiner.join(sqlQuery, sqlOrderAndLimit);

		PageVO pageVO = new PageVO();
		pageVO.setList(queryDao.queryMap(sqlQuery, params));

		Number total = queryDao.query(sqlCount);
		if (null != total) {
			pageVO.setTotal(total.intValue());
		} else {
			pageVO.setTotal(0);
		}

		return pageVO;
	}

	/**
	 * 根据活动ID更新valid状态
	 */
	public void updateStatusByAmuseId(long amuseId, int valid) {
		if (valid != 1) {
			valid = 0;
		}
		AmuseActivityInfo amuseActivityInfo = findById(amuseId);
		if (null != amuseActivityInfo) {
			amuseActivityInfo.setValid(valid);
			amuseActivityInfo.setUpdateDate(new Date());
			save(amuseActivityInfo);
		}
	}

	/**查询用于app推荐的娱乐赛
	 * @return
	 */
	public List<Map<String, Object>> queryAmuseForAppRecommend(int sign) {
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		//		String signSql = "";
		//		if (sign == 1) {
		//			signSql = " and recommend_sign<>1";
		//		} else if (sign == 2) {
		//			signSql = " and recommend_sign<>2";
		//		}
		String sql = "select id,title from amuse_t_activity a where a.is_valid=1 and now()<a.end_date order by a.create_date desc";
		result.addAll(queryDao.queryMap(sql));
		return result;

	}

	/**
	 * 生成订单编号
	 * @param type 娱乐官方线上为1,娱乐官方线下为2,官方赛事线上为3,官方赛事线下为4
	 * @param awardType 自有商品为1,库存商品为2,第三方为3
	 */
	public String genSerial(Integer type, Integer awardType) {
		if (null == type) {
			type = 0;
		}
		if (null == awardType) {
			awardType = 0;
		}

		// 以时间+类型作为订单前部分
		String tradeNo = DateUtils.dateToString(new Date(), "yyyyMMddHHmmssSSS") + "-" + type + "-" + awardType;

		// 累加递增部分
		Joiner joiner = Joiner.on("_");
		String redisKey = joiner.join(AmuseConstant.REDIS_KEY_AMUSE_ORDER_REPEAT, tradeNo);
		RedisConnectionFactory factory = objectRedisOperateService.getRedisTemplate().getConnectionFactory();
		RedisAtomicInteger redisRepeat = new RedisAtomicInteger(redisKey, factory);
		int repeat = redisRepeat.incrementAndGet();
		if (repeat <= 1) {
			redisRepeat.expire(1, TimeUnit.MINUTES);
		}
		String repeatStr = repeat < 10 ? "00" + repeat : repeat < 100 ? "0" + repeat : String.valueOf(repeat);

		return tradeNo.substring(3, tradeNo.length()) + repeatStr;
	}

	/**
	 * 后台管理系统推送模块查询可用的娱乐赛
	 */
	public List<Map<String, Object>> findValidByTitle(String title) {
		String sql = "select id,title from amuse_t_activity where is_valid =1 and title like '%" + title
				+ "%' and end_date>now()";
		return queryDao.queryMap(sql);
	}
}
