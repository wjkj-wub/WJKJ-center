package com.miqtech.master.service.activity;

import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.consts.Msg4UserType;
import com.miqtech.master.consts.MsgConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.activity.*;
import com.miqtech.master.entity.activity.*;
import com.miqtech.master.entity.user.UserInfo;
import com.miqtech.master.service.netbar.NetbarUserService;
import com.miqtech.master.service.user.UserInfoService;
import com.miqtech.master.thirdparty.service.JpushService;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.vo.PageVO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Component
public class ActivityMemberService {
	@Autowired
	private ActivityMemberDao activityMemberDao;
	@Autowired
	private ActivityTeamDao activityTeamDao;
	@Autowired
	private ActivityInfoDao activityInfoDao;
	@Autowired
	private ActivityApplyDao activityApplyDao;
	@Autowired
	private QueryDao queryDao;
	@Autowired
	private UserInfoService userInfoService;
	@Autowired
	private JpushService msgOperateService;
	@Autowired
	private ActivityInviteService activityInviteService;
	@Autowired
	private ActivityCardDao activityCardDao;

	public ActivityMember save(ActivityMember activityMember) {
		return activityMemberDao.save(activityMember);
	}

	public List<ActivityMember> save(List<ActivityMember> members) {
		if (CollectionUtils.isEmpty(members)) {
			return null;
		}
		return (List<ActivityMember>) activityMemberDao.save(members);
	}

	/**
	 * 保存个人报名信息
	 */
	public ActivityMember savePersonal(ActivityMember activityMember, String netbarId) {
		if (activityMember.getId() != null && NumberUtils.isNumber(netbarId)) {
			String sql = SqlJoiner.join("UPDATE activity_r_apply SET netbar_id = ", netbarId,
					" WHERE target_id = " + activityMember.getId(), " AND type = 1");
			queryDao.update(sql);
		}
		return save(activityMember);
	}

	/**
	 * 保存战队报名的个人信息
	 */
	public ActivityMember saveTeam(ActivityMember activityMember, String netbarId) {
		if (activityMember.getId() != null && NumberUtils.isNumber(netbarId)) {
			String sql = SqlJoiner.join("UPDATE activity_r_apply SET netbar_id = ", netbarId,
					" WHERE target_id = (SELECT team_id FROM activity_t_member WHERE id = " + activityMember.getId(),
					") AND type = 2");
			queryDao.update(sql);
		}
		return save(activityMember);
	}

	public ActivityMember findById(long id) {
		return activityMemberDao.findOne(id);
	}

	/**
	 * 查询个人详情
	 */
	public Map<String, Object> getMemberDetailById(long id) {
		String sql = SqlJoiner.join(
				"SELECT a.activity_id activityId, a.round, a.netbar_id netbarId, m.id, m.name, m.id_card idCard, m.telephone, m.server, m.qq, m.labor, m.create_date createDate",
				" FROM activity_t_member m LEFT JOIN activity_r_apply a ON m.id = a.target_id",
				" WHERE a.type = 1 AND m.is_valid = 1 AND m.id = ", String.valueOf(id));
		return queryDao.querySingleMap(sql);
	}

	/**
	 * 查询战队详情
	 */
	public Map<String, Object> getTeamDetailById(long id) {
		String sql = SqlJoiner.join(
				"SELECT a.netbar_id netbarId, m.id, m.`name`, m.id_card idCard, m.telephone, t.`server`, m.team_id teamId, m.qq, m.labor, m.create_date createDate, m.is_monitor isMonitor",
				" FROM activity_r_apply a", " LEFT JOIN activity_t_team t ON a.target_id = t.id AND t.is_valid = 1",
				" LEFT JOIN activity_t_member m ON t.id = m.team_id AND m.is_valid = 1",
				" WHERE	a.type = 2 AND a.is_valid = 1 AND m.id = ", String.valueOf(id));
		return queryDao.querySingleMap(sql);
	}

	@Transactional
	public boolean exitTeam(Long userId, Long teamId) {
		List<ActivityMember> list = activityMemberDao.findByTeamIdAndUserIdAndValid(teamId, userId, 1);
		if (list.size() > 0) {
			ActivityMember activityMember = list.get(0);
			if (activityMember.getIsMonitor().equals(0)) {
				activityMember.setValid(0);
				activityMemberDao.save(activityMember);
			} else if (activityMember.getIsMonitor().equals(1)) {//战队创建人
				list = activityMemberDao.findByTeamIdAndValid(teamId, 1);
				for (ActivityMember obj : list) {
					obj.setValid(0);
					activityMemberDao.save(obj);
				}
				List<ActivityTeam> teamList = activityTeamDao.findByMemIdAndValid(activityMember.getId(), 1);
				if (teamList.size() > 0) {
					ActivityTeam activityTeam = teamList.get(0);
					activityTeam.setValid(0);
					activityTeamDao.save(activityTeam);
					ActivityApply activityApply = activityApplyDao.findByTargetIdAndTypeAndRoundAndValid(teamId, 2,
							activityTeam.getRound(), 1);
					if (null != activityApply) {
						activityApply.setValid(0);//设置报名信息无效
						activityApplyDao.save(activityApply);
					}
				}

			}
			return true;
		}
		return false;
	}

	public Map<String, Object> myTeamList(Long userId, Integer page, Integer pageSize) {
		Map<String, Object> params = new HashMap<String, Object>();
		String sql = "";
		Map<String, Object> result = new HashMap<String, Object>();
		sql = SqlJoiner.join("select count(1) from activity_t_member b where  b.user_id=", String.valueOf(userId),
				" and b.is_valid=1");
		Number totalCount = queryDao.query(sql);
		if (page * pageSize >= totalCount.intValue()) {
			result.put("isLast", 1);
		} else {
			result.put("isLast", 0);
		}
		Integer start = (page - 1) * pageSize;
		params.put("userId", userId);
		params.put("start", start);
		params.put("pageSize", pageSize);
		sql = SqlJoiner.join(
				"select a.id team_id, a.name team_name, b.is_monitor, nickname header, count(d.team_id) num, 5 total_num, a.activity_id, a.round, i.title from activity_t_member b ",
				"left join activity_t_team a on a.id = b.team_id and a.is_valid = 1 ",
				"left join user_t_info e on e.id = b.user_id ", "left join activity_t_info i on i.id = b.activity_id ",
				"left join (select team_id from activity_t_member where is_valid = 1) d on a.id = d.team_id ",
				"where  b.is_valid = 1 and a.is_valid = 1 and b.user_id =:userId  group by b.team_id  desc limit :start,:pageSize");
		result.put("teamList", queryDao.queryMap(sql, params));
		return result;
	}

	/**
	 * 添加队员
	 */
	public int addTeammate(String telephone, Long teamId, Long cardId) {
		UserInfo userInfo = userInfoService.queryByName(telephone);
		if (userInfo == null) {
			return -1;//用户尚未注册
		}
		List<ActivityMember> list = activityMemberDao.findByTeamIdAndUserIdAndValid(teamId, userInfo.getId(), 1);
		List<ActivityMember> teammates = activityMemberDao.findByTeamIdAndValid(teamId, 1);
		if (list.size() == 0 && teammates.size() < 5 && teammates.size() > 0) {
			ActivityMember obj = teammates.get(0);
			ActivityMember activityMember = new ActivityMember();
			Long activityId = obj.getActivityId();
			activityMember.setActivityId(activityId);
			activityMember.setUserId(userInfo.getId());
			activityMember.setTeamId(teamId);
			activityMember.setRound(obj.getRound());
			activityMember.setIdCard(userInfo.getIdCard());
			activityMember.setTelephone(userInfo.getTelephone());
			activityMember.setQq(userInfo.getQq());
			activityMember.setIsMonitor(0);
			activityMember.setIsEnter(1);
			activityMember.setValid(1);
			activityMember.setCreateDate(new Date());
			if (cardId != null) {
				activityMember.setCardId(cardId);
				ActivityCard activityCard = activityCardDao.findOne(cardId);
				activityMember.setName(activityCard.getRealName());
			}
			activityMemberDao.save(activityMember);
			pushActivityMsg(activityInfoDao.findById(activityId), userInfo.getId(), obj.getRound());
			return 0;
		} else if (teammates.size() >= 5) {
			return -3;//战队人数已满
		} else {
			return -2;//该用户已添加
		}
	}

	/**
	 * 添加队员
	 */
	public int addTeammate(Long userId, Long teamId) {
		UserInfo userInfo = userInfoService.findById(userId);
		if (userInfo == null) {
			return -1;//用户尚未注册或资料不完善
		} else {
			userInfo.setProfileStatus();//更新用户的资料完善状态
		}
		if (userInfo.getProfileStatus() == 0) {
			return -1;//用户尚未注册或资料不完善
		} else {
			List<ActivityMember> list = activityMemberDao.findByTeamIdAndUserIdAndValid(teamId, userInfo.getId(), 1);
			List<ActivityMember> teammates = activityMemberDao.findByTeamIdAndValid(teamId, 1);
			if (list.size() == 0 && teammates.size() < 5 && teammates.size() > 0) {
				ActivityMember obj = teammates.get(0);
				ActivityMember activityMember = new ActivityMember();
				Long activityId = obj.getActivityId();
				activityMember.setActivityId(activityId);
				activityMember.setUserId(userInfo.getId());
				activityMember.setTeamId(teamId);
				activityMember.setRound(obj.getRound());
				activityMember.setName(userInfo.getRealName());
				activityMember.setIdCard(userInfo.getIdCard());
				activityMember.setTelephone(userInfo.getTelephone());
				activityMember.setQq(userInfo.getQq());
				activityMember.setIsMonitor(0);
				activityMember.setIsEnter(1);
				activityMember.setValid(1);
				activityMember.setCreateDate(new Date());
				activityMemberDao.save(activityMember);
				pushActivityMsg(activityInfoDao.findById(activityId), userInfo.getId(), obj.getRound());
				return 0;
			}
			return -2;//该用户已添加
		}
	}

	@Autowired
	private NetbarUserService netbarUserService;

	/**
	 * 通过参赛卡加入战队
	 */
	public int joinTeam(Long teamId, Long userId, ActivityCard card, ActivityMember activityMember) {
		return joinTeam(teamId, userId, card.getRealName(), card.getTelephone(), card.getIdCard(), card.getQq(), null,
				activityMember, card.getId());
	}

	/**
	 * 通过基本信息加入战队
	 */
	public int joinTeam(Long teamId, Long userId, String name, String telephone, String idCard, String qq, String labor,
			ActivityMember activityMember) {
		return joinTeam(teamId, userId, name, telephone, idCard, qq, labor, activityMember, null);
	}

	/**
	 * 加入战队
	 */
	public int joinTeam(Long teamId, Long userId, String name, String telephone, String idCard, String qq, String labor,
			ActivityMember activityMember, Long cardId) {
		int applyFlag = isApply(teamId, userId);
		if (applyFlag == 0) {
			return -1;//团队信息已无效
		} else if (applyFlag == 1) {
			if (activityMember != null) {
				activityMember.setIsAccept(2);
				activityMemberDao.save(activityMember);
			}
			return -2;//该用户已加入,不能重复加入
		} else if (applyFlag == 3) {
			if (activityMember != null) {
				activityMember.setIsAccept(2);
				activityMemberDao.save(activityMember);
			}
			return -4;//赛事已经截止报名
		}
		List<ActivityMember> list = activityMemberDao.findByTeamIdAndUserIdAndValid(teamId, userId, 1);
		if (list.size() > 0) {
			return -2;//该用户已加入,不能重复加入
		}
		list = activityMemberDao.findByTeamIdAndValid(teamId, 1);
		int listsize = list.size();
		if (listsize > 0 && listsize < 5) {
			if (activityMember == null) {
				ActivityMember apply = activityMemberDao.findByTeamIdAndUserIdAndValidAndIsAccept(teamId, userId, 0, 0);
				if (apply != null) {
					return -5;//已申请加入
				}
				activityMember = list.get(0);
				ActivityMember obj = new ActivityMember();
				Long activityId = activityMember.getActivityId();
				obj.setCardId(cardId);
				obj.setActivityId(activityId);
				obj.setUserId(userId);
				obj.setTeamId(teamId);
				obj.setRound(activityMember.getRound());
				if (cardId == null) {
					obj.setName(name);
					obj.setIdCard(idCard);
					obj.setTelephone(telephone);
					obj.setQq(qq);
					obj.setLabor(labor);
				} else {
					ActivityCard card = activityCardDao.findOne(cardId);
					obj.setName(card.getRealName());
					obj.setIdCard(card.getIdCard());
					obj.setTelephone(card.getTelephone());
					obj.setQq(card.getQq());
					obj.setCardId(cardId);
				}
				obj.setIsMonitor(0);
				obj.setIsEnter(1);
				obj.setValid(0);
				obj.setIsAccept(0);
				obj.setCreateDate(new Date());
				activityMemberDao.save(obj);
				//报名赛事，绑定网吧
				ActivityTeam activityTeam = activityTeamDao.findOne(teamId);
				if (activityTeam.getValid() == 1) {
					netbarUserService.bindNetbar(userId, activityTeam.getNetbarId());
				}
				Integer round = activityMember.getRound();
				//如果个人已报名删除个人报名
				List<ActivityApply> applyList = activityApplyDao
						.findByActivityIdAndTargetIdAndTypeAndRoundAndValid(activityId, userId, 1, round, 1);
				if (applyList.size() > 0) {
					ActivityApply activityApply = applyList.get(0);
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
			} else {
				activityMember.setValid(1);
				activityMember.setIsAccept(1);
				activityMemberDao.save(activityMember);
				//报名赛事，绑定网吧
				ActivityTeam activityTeam = activityTeamDao.findOne(teamId);
				if (activityTeam.getValid() == 1) {
					netbarUserService.bindNetbar(userId, activityTeam.getNetbarId());
				}
				pushActivityMsg(activityInfoDao.findOne(activityMember.getActivityId()), activityMember.getUserId(),
						activityMember.getRound());
			}
			return 0;
		} else {
			return -3;//战队人数已满
		}
	}

	/**
	 * 是否已报名其他战队(个人报名始终teamId=0)
	 */
	public int isApply(Long teamId, Long userId) {
		ActivityMember member = activityMemberDao.findByTeamIdAndIsMonitorAndValid(teamId, 1, 1);
		if (member == null) {
			return 0;//不存此团队
		}
		Long activityId = member.getActivityId();
		ActivityInfo info = activityInfoDao.findById(activityId);
		if (new Date().after(DateUtils.addDays(DateUtils.truncate(info.getOverTime(), Calendar.DAY_OF_MONTH), 1))) {
			return 3;
		}

		List<ActivityMember> members = activityMemberDao
				.findByActivityIdAndUserIdAndRoundAndValidAndTeamIdNot(activityId, userId, member.getRound(), 1, 0L);
		if (CollectionUtils.isNotEmpty(members)) {
			return 1;//已经报名
		}
		return 2;//可以报名其他战队
	}

	private void pushActivityMsg(ActivityInfo activity, Long userId, int round) {
		String content = "您已成功报名" + activity.getTitle() + (round > 0 ? "第" + round + "场" : "");
		msgOperateService.notifyMemberAliasMsg(Msg4UserType.ACTIVITY.ordinal(), userId,
				MsgConstant.PUSH_MSG_TYPE_ACTIVITY, "赛事消息", content, true, activity.getId());
	}

	public List<Map<String, Object>> myTeammate(Long teamId) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("teamId", teamId);
		String sql = "select a.is_monitor,a.id member_id,b.username telephone,b.nickname,b.icon,case when b.realname is not null and b.idcard is not null and b.qq is not null then 1 else 0 end isCompleted from activity_t_member a,user_t_info b where a.team_id=:teamId and a.is_valid=1 and a.user_id=b.id";
		return queryDao.queryMap(sql, params);
	}

	public boolean removeTeammate(Long memberId, Long userId) {
		ActivityMember activityMember = activityMemberDao.findOne(memberId);
		if (activityMember != null) {
			if (activityMember.getIsMonitor().equals(1)) {
				return false;
			}
			List<ActivityMember> list = activityMemberDao.findByActivityIdAndUserIdAndRoundAndValidAndTeamId(
					activityMember.getActivityId(), userId, activityMember.getRound(), 1, activityMember.getTeamId());
			if (CollectionUtils.isNotEmpty(list)) {
				if (list.get(0).getIsMonitor().equals(1)) {
					activityMember.setValid(0);
					activityMemberDao.save(activityMember);
					return true;
				}
			}
		}
		return false;
	}

	public Map<String, Object> alreadyAppliedTeam(Long userId, Long activityId, Long netbarId, String areaCode,
			int page, int pageSize) {
		Map<String, Object> result = new HashMap<String, Object>();
		String sql = "";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("activityId", activityId);
		sql = "select netbars from activity_t_info where id=:activityId and is_valid=1";
		Map<String, Object> netbars = queryDao.querySingleMap(sql, params);
		StringBuffer netbarIdsBuffer = new StringBuffer();
		String ids = "";
		String roundsNetbarIdsQuerySql = "select netbars from activity_r_rounds where activity_id=:activityId and is_valid=1";
		List<Map<String, Object>> netbarIdsInRounds = queryDao.queryMap(roundsNetbarIdsQuerySql, params);
		if (CollectionUtils.isNotEmpty(netbarIdsInRounds)) {
			for (Map<String, Object> netbarIdsInRound : netbarIdsInRounds) {
				String nids = netbarIdsInRound.get("netbars").toString();
				if (StringUtils.isNotBlank(nids)) {
					netbarIdsBuffer.append(nids).append(",");
				}
			}
		}
		if (netbars != null) {
			ids = netbarIdsBuffer.append((String) netbars.get("netbars")).toString();
		} else {
			ids = netbarIdsBuffer.toString();
		}
		ids = StringUtils.removeStart(ids, ",");
		ids = StringUtils.removeEnd(ids, ",");
		if (StringUtils.isEmpty(ids)) {
			return result;
		}
		sql = SqlJoiner.join("select b.area_code,b.name city from netbar_t_info a,sys_t_area b where a.id in (", ids,
				") and CONCAT(SUBSTR(a.area_code,1,4),'00')=b.area_code group by b.area_code");
		List<Map<String, Object>> citys = queryDao.queryMap(sql);
		if (CollectionUtils.isNotEmpty(citys)) {
			for (Map<String, Object> map : citys) {
				String code = (String) map.get("area_code");
				sql = SqlJoiner.join("select a.id,a.name netbar_name from netbar_t_info a where a.id in (", ids,
						") and CONCAT(SUBSTR(a.area_code,1,4),'00')=", code);
				List<Map<String, Object>> bars = queryDao.queryMap(sql);
				map.put("netbars", bars);
			}
		}
		if (netbarId == null && areaCode == null && page == 1) {
			result.put("condition", citys);
		}

		ActivityInfo activity = activityInfoDao.findById(activityId);
		Date overTime = activity.getOverTime();
		Date beginTime = activity.getBeginTime();
		Date now = DateUtils.truncate(new Date(), Calendar.DAY_OF_MONTH);
		result.put("inApply",
				(overTime != null && beginTime != null) && !(now.before(beginTime)) && now.before(overTime));//返回赛事状态 true可报名 false不可报名

		String fieldSql = "";
		String conditionSql = "";
		params.clear();
		if (userId != null) {
			fieldSql = ",count(e.team_id) is_join";
			conditionSql = " left join (select team_id from activity_t_member where is_valid=1 and user_id=:userId)e on a.id=e.team_id";
			params.put("userId", userId);
		}
		if (areaCode == null || (areaCode != null && netbarId != null)) {
			if (netbarId != null) {
				ids = String.valueOf(netbarId);
			}
			sql = SqlJoiner.join("select count(1) from (activity_t_team a,activity_t_member b)  where a.netbar_id in(",
					ids, ") and a.is_valid=1 and a.mem_id=b.id");
			Number totalCount = queryDao.query(sql);
			if (page * pageSize >= totalCount.intValue()) {
				result.put("isLast", 1);
			} else {
				result.put("isLast", 0);
			}
			int start = (page - 1) * pageSize;
			params.put("start", start);
			params.put("activityId", activityId);
			params.put("pageSize", pageSize);
			sql = SqlJoiner.join(
					"select a.id team_id,a.name team_name,b.name header,count(c.team_id) num,5 total_num,a.round",
					fieldSql,
					" from (activity_t_team a,activity_t_member b) left join (select team_id from activity_t_member where is_valid=1)c on a.id=c.team_id",
					conditionSql, " where a.netbar_id in(", ids,
					") and a.activity_id=:activityId and a.is_valid=1 and a.mem_id=b.id group by a.id order by a.create_date desc limit :start,:pageSize");
			List<Map<String, Object>> teams = queryDao.queryMap(sql, params);
			result.put("teams", teams);
			return result;
		} else {
			params.put("areaCode", areaCode);
			sql = SqlJoiner.join(
					"select count(1) from (activity_t_team a,activity_t_member b,netbar_t_info d)  where a.netbar_id in(",
					ids, ") and a.is_valid=1 and a.mem_id=b.id and CONCAT(SUBSTR(d.area_code,1,4),'00')=", areaCode,
					" and a.netbar_id=d.id");
			Number totalCount = queryDao.query(sql);
			if (page * pageSize >= totalCount.intValue()) {
				result.put("isLast", 1);
			} else {
				result.put("isLast", 0);
			}
			int start = (page - 1) * pageSize;
			params.put("start", start);
			params.put("activityId", activityId);
			params.put("pageSize", pageSize);
			sql = SqlJoiner.join(
					"select a.id team_id,a.name team_name,b.name header,count(c.team_id) num,5 total_num,a.round",
					fieldSql,
					" from (activity_t_team a,activity_t_member b,netbar_t_info d) left join (select team_id from activity_t_member where is_valid=1)c on a.id=c.team_id",
					conditionSql, " where a.netbar_id in(", ids,
					") and a.activity_id=:activityId and a.is_valid=1 and a.mem_id=b.id and CONCAT(SUBSTR(d.area_code,1,4),'00')=:areaCode and a.netbar_id=d.id group by a.id order by a.create_date desc limit :start,:pageSize");
			List<Map<String, Object>> teams = queryDao.queryMap(sql, params);
			result.put("teams", teams);
			return result;

		}

	}

	/**
	 * 根据参数map生成个人报名信息的查询条件
	 */
	private String personalMembersConditions(Map<String, Object> params) {
		String sql = "";
		Set<String> keys = params.keySet();
		if (keys != null && !keys.isEmpty()) {
			for (Iterator<String> it = keys.iterator(); it.hasNext();) {
				String key = it.next();
				String param = (String) params.get(key);
				if (StringUtils.isNotBlank(param)) {
					if (key.equals("startDate")) {
						sql = SqlJoiner.join(sql, " AND m.create_date", " >= '", param, "'");
					} else if (key.equals("endDate")) {
						sql = SqlJoiner.join(sql, " AND m.create_date", " < ADDDATE('", param, "',INTERVAL 1 DAY)");
					} else if (key.equals("netbar_id")) {
						sql = SqlJoiner.join(sql, " AND a.", key, " LIKE '%", param, "%'");
					} else if (key.startsWith("%")) {
						sql = SqlJoiner.join(sql, " AND m.", key, " LIKE '%", param, "%'");
					} else if ("name".equals(key)) {
						sql = SqlJoiner.join(sql, " AND m.name LIKE '%", param, "%'");
					} else if ("telephone".equals(key)) {
						sql = SqlJoiner.join(sql, " AND m.telephone LIKE '%", param, "%'");
					} else {
						sql = SqlJoiner.join(sql, " AND m.", key, " = '", String.valueOf(param), "'");
					}
				}
			}
		}
		return sql;
	}

	/**
	 * 分页查询个人报名的用户信息
	 * page: 不传入参数时不做分页
	 */
	public PageVO personalMembers(int page, Map<String, Object> params) {
		PageVO result = new PageVO();

		// 列表数据
		String sql = SqlJoiner.join(
				"SELECT g.seat_number seatNumber, n.name netbarName, m.id, m.activity_id, m.name, m.server, m.id_card idCard, m.telephone, m.qq, m.labor, m.create_date createDate, m.round, m.signed, g.rank",
				" FROM activity_t_member m LEFT JOIN activity_r_apply a ON m.id = a.target_id",
				" LEFT JOIN netbar_t_info n ON a.netbar_id = n.id and n.is_valid = 1",
				" LEFT JOIN activity_group g ON m.id = g.target_id AND g.is_team != 1 AND g.is_valid = 1",
				" WHERE m.is_valid = 1 and m.round > 0 and m.team_id = 0");
		sql = SqlJoiner.join(sql, personalMembersConditions(params));// 查询条件
		sql = SqlJoiner.join(sql, " ORDER BY m.activity_id ASC, g.seat_number, m.id DESC, m.round ASC");// 排序方式
		if (page > 0) {
			sql = SqlJoiner.join(sql, " LIMIT ", String.valueOf((page - 1) * PageUtils.ADMIN_DEFAULT_PAGE_SIZE), ", ",
					String.valueOf(PageUtils.ADMIN_DEFAULT_PAGE_SIZE));// 分页
		}
		List<Map<String, Object>> list = queryDao.queryMap(sql);
		result.setList(list);

		// 总数
		String sqlCount = "SELECT COUNT(1) FROM activity_t_member m LEFT JOIN activity_r_apply a ON m.id = a.target_id WHERE m.is_valid = 1 and m.round > 0 and m.team_id = 0";
		sqlCount = SqlJoiner.join(sqlCount, personalMembersConditions(params));
		Number count = queryDao.query(sqlCount);
		result.setTotal(count.longValue());
		if (page * PageUtils.ADMIN_DEFAULT_PAGE_SIZE >= count.longValue()) {
			result.setIsLast(CommonConstant.INT_BOOLEAN_TRUE);
		}
		return result;
	}

	/**
	 * 删除一条个人报名记录（包括报名信息）
	 */
	public void deletePersonMember(long id) {
		String deleteMemberSql = "UPDATE activity_t_member SET is_valid = 0 WHERE id = " + id;
		queryDao.update(deleteMemberSql);
		String deleteApplySql = "UPDATE activity_r_apply SET is_valid = 0 WHERE target_id = " + id + " AND type = 1";
		queryDao.update(deleteApplySql);
	}

	/**
	 * 删除一条战队报名记录
	 */
	public void deleteTeamMember(long id) {
		String sql = "UPDATE activity_t_member SET is_valid = 0 WHERE id = " + id;
		queryDao.update(sql);

		// 如果是队长，删除所有用户
		String teamSql = SqlJoiner.join("SELECT team_id, is_monitor FROM activity_t_member WHERE id = ",
				String.valueOf(id));
		Map<String, Object> team = queryDao.querySingleMap(teamSql);
		Number isMonitor = (Number) team.get("is_monitor");
		if (CommonConstant.INT_BOOLEAN_TRUE.equals(isMonitor.intValue())) {
			// 删除战队成员
			Number teamId = (Number) team.get("team_id");
			String deleteMemberSql = SqlJoiner.join("UPDATE activity_t_member SET is_valid = 0 WHERE team_id = ",
					teamId.toString());
			queryDao.update(deleteMemberSql);

			// 删除战队信息
			String deleteTeamSql = SqlJoiner.join("UPDATE activity_t_team SET is_valid = 0 WHERE id = ",
					teamId.toString());
			queryDao.update(deleteTeamSql);

			// 删除战队报名信息
			String deleteApplySql = SqlJoiner.join("UPDATE activity_r_apply SET is_valid = 0 WHERE target_id = ",
					teamId.toString(), " AND type = 2");
			queryDao.update(deleteApplySql);
		}
	}

	/**
	 * 邀请系统用户参加自己的战队
	 */
	public int invocateUser(Long teamId, String invocationIds, Long userId) {
		int newCount = 0;
		String[] split = StringUtils.split(invocationIds, ",");
		if (split != null) {
			newCount = split.length;
		}
		String countSql = "select count(id) from activity_t_member where is_valid=1  and team_id=" + teamId;
		Number count = queryDao.query(countSql);
		if (count.intValue() + newCount > 5) {
			return -1;
		}
		if (split.length > 0) {
			for (String id : split) {
				//已加入其他战队
				if (isApply(teamId, Long.valueOf(id)) == 1) {
					return -7;
				}
				String existSql = "select count(1) from activity_r_invite a where a.invite_user_id="
						+ String.valueOf(userId)
						+ " and type=2 and invited_telephone=(select username from user_t_info where id =" + id
						+ ") and status=0 and target_id=" + String.valueOf(teamId);
				Number existCount = queryDao.query(existSql);
				if (existCount.intValue() > 0) {
					return -2;
				}
				String sql = SqlJoiner.join("select count(1) from activity_t_member where team_id=",
						String.valueOf(teamId), " and user_id=", String.valueOf(id), " and is_valid=1");
				count = queryDao.query(sql);
				if (count.intValue() > 0) {
					return -6;
				}
				//添加邀请记录
				UserInfo userInfo = userInfoService.findById(Long.valueOf(id));
				activityInviteService.insertActivityInvite(userInfo.getUsername(), userId, teamId);
			}
		}
		return 0;
	}

	public ActivityMember findByTeamIdAndValidAndIsMonitor(Long teamId, int isValid, int isMonitor) {
		return activityMemberDao.findByTeamIdAndIsMonitorAndValid(teamId, isValid, isMonitor);
	}

	/**加入战队申请列表
	 * @param teamId
	 * @return
	 */
	public List<Map<String, Object>> joinTeamApplyList(Long teamId) {
		String sql = "update activity_t_member set is_read=1 where is_valid=0 and is_accept=0 and team_id=" + teamId;
		queryDao.update(sql);
		sql = SqlJoiner.join(
				"select a.id,b.icon,b.nickname,a.labor from activity_t_member a,user_t_info b where a.team_id=",
				String.valueOf(teamId),
				" and a.is_valid=0 and is_accept=0 and a.user_id=b.id order by a.create_date desc");
		return queryDao.queryMap(sql);
	}

	/**接受加入战队申请
	 * @param id
	 */
	public int acceptTeamApply(Long id, Long userId) {
		ActivityMember activityMember = activityMemberDao.findOne(id);
		if (activityMember != null) {
			Long teamId = activityMember.getTeamId();
			if (isApply(teamId, activityMember.getUserId()) == 1) {
				return -10;//已加入其他战队
			}
			return joinTeam(teamId, activityMember.getUserId(), null, null, null, null, null, activityMember, null);
		}
		return -5;
	}

	/**清空加入战队申请列表
	 * @param id
	 */
	public boolean clearTeamApply(Long teamId) {
		String sql = SqlJoiner.join("update activity_t_member a set a.is_accept=2 where a.team_id=",
				String.valueOf(teamId), " and a.is_valid=0 and is_accept=0");
		int row = queryDao.update(sql);
		if (row > 0) {
			return true;
		}
		return false;
	}

	/**
	 * 更新过期的战队申请
	 */
	public void updateOutOfDateTeamApply() {
		String date = com.miqtech.master.utils.DateUtils.dateToString(DateUtils.addDays(new Date(), -30),
				com.miqtech.master.utils.DateUtils.YYYY_MM_DD_HH_MM_SS);
		String sql = "update activity_t_member set is_accept=2 where is_valid=0 and is_accept=0 and create_date<'"
				+ date + "'";
		queryDao.update(sql);
	}

	/**
	 * 通过用户ID查询个人报名或战队报名的信息
	 */
	public ActivityMember findOneByActivityIdAndUserIdAndType(Long activityId, Long userId, boolean personal) {
		String typeCondition = personal ? " AND team_id = 0" : " AND team_id != 0";
		String sql = SqlJoiner.join("SELECT * FROM activity_t_member WHERE activity_id = ", activityId.toString(),
				" AND user_id = ", userId.toString(), " AND is_valid = 1", typeCondition);
		Map<String, Object> member = queryDao.querySingleMap(sql);

		if (MapUtils.isNotEmpty(member)) {
			ActivityMember m = new ActivityMember();
			m.setId(MapUtils.getLong(member, "id"));
			m.setActivityId(MapUtils.getLong(member, "activity_id"));
			m.setUserId(MapUtils.getLong(member, "user_id"));
			m.setTeamId(MapUtils.getLong(member, "team_id"));
			m.setRound(MapUtils.getInteger(member, "round"));
			m.setName(MapUtils.getString(member, "name"));
			m.setIdCard(MapUtils.getString(member, "id_card"));
			m.setTelephone(MapUtils.getString(member, "telephone"));
			m.setQq(MapUtils.getString(member, "qq"));
			m.setServer(MapUtils.getString(member, "server"));
			m.setLabor(MapUtils.getString(member, "labor"));
			m.setIsMonitor(MapUtils.getInteger(member, "is_monitor"));
			m.setIsEnter(MapUtils.getInteger(member, "is_enter"));
			m.setInRecord(MapUtils.getInteger(member, "in_record"));
			m.setValid(MapUtils.getInteger(member, "is_valid"));
			m.setUpdateUserId(MapUtils.getLong(member, "update_user_id"));
			m.setCreateUserId(MapUtils.getLong(member, "create_user_id"));
			m.setUpdateDate((Date) MapUtils.getObject(member, "update_date"));
			m.setCreateDate((Date) MapUtils.getObject(member, "create_date"));
			return m;
		}

		return null;
	}

	/**
	 * 签到或取消签到
	 */
	public ActivityMember sign(Long id, Integer signed) {
		if (id == null) {
			return null;
		}
		if (signed == null) {
			signed = CommonConstant.INT_BOOLEAN_TRUE;
		}

		ActivityMember member = findById(id);
		if (member == null) {
			return null;
		}

		member.setUpdateDate(new Date());
		member.setSigned(signed);
		return save(member);
	}

	/**
	 * 查询个人报名信息
	 */
	public Map<String, Object> queryValidByTelephoneAndActivityAndNetbarIdAndRound(String telephone, Long activityId,
			Long netbarId, Integer round) {
		if (StringUtils.isBlank(telephone) || activityId == null || netbarId == null || round == null) {
			return null;
		}

		String sql = SqlJoiner.join("SELECT m.id memberId FROM activity_t_member m",
				" JOIN activity_r_apply a ON m.id = a.target_id AND a.type = 1",
				" JOIN user_t_info u ON m.user_id = u.id", " WHERE m.is_valid = 1 AND u.username = '", telephone,
				"' AND a.activity_id = ", activityId.toString(), " AND a.netbar_id = ", netbarId.toString(),
				" AND a.round = ", round.toString(), " ORDER BY a.create_date desc LIMIT 1");
		return queryDao.querySingleMap(sql);
	}

	/**
	 * 查询战队人员信息
	 */
	public List<Map<String, Object>> queryValidByTeamId(Long teamId) {
		if (teamId == null) {
			return null;
		}

		String sql = SqlJoiner.join("select * from activity_t_member where is_valid = 1 and team_id = ",
				teamId.toString());
		return queryDao.queryMap(sql);
	}
}