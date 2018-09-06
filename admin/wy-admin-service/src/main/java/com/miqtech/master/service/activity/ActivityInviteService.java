package com.miqtech.master.service.activity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.consts.ActivityConstant;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.activity.ActivityInviteDao;
import com.miqtech.master.entity.activity.ActivityInvite;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.vo.PageVO;

/**
 * 约战和赛事邀请记录service
 */
@Component
public class ActivityInviteService {

	@Autowired
	private QueryDao queryDao;
	@Autowired
	private ActivityInviteDao activityInviteDao;
	@Autowired
	private ActivityMemberService activityMemberService;

	/**插入邀请记录
	 * @param phone
	 * @param userId
	 * @param teamId
	 */
	public void insertActivityInvite(String phone, Long userId, Long teamId) {
		ActivityInvite activityInvite = new ActivityInvite();
		activityInvite.setInviteUserId(userId);
		activityInvite.setInvitedTelephone(phone);
		activityInvite.setTargetId(teamId);
		activityInvite.setType(2);
		activityInvite.setValid(1);
		activityInvite.setCreateDate(new Date());
		activityInvite.setStatus(0);
		activityInviteDao.save(activityInvite);
	}

	/**新的邀请
	 * @param phone
	 * @return
	 */
	public List<Map<String, Object>> newInvocation(String phone) {
		if (phone == null) {
			return new ArrayList<Map<String, Object>>();
		}
		String sql = SqlJoiner
				.join("select  a.id invocation_id,c.icon,c.nickname,b.name,b.id team_id from activity_r_invite a,activity_t_team b,user_t_info c where a.invited_telephone='",
						phone,
						"' and type=2 and a.status=0 and a.is_valid=1 and a.target_id=b.id and a.invite_user_id=c.id");
		return queryDao.queryMap(sql);
	}

	/**处理邀请
	 * @param id
	 * @param type
	 * @return
	 */
	public int doInvocation(Long id, String phone, Integer type, Long cardId) {
		ActivityInvite activityInvite = activityInviteDao.findOne(id);
		//已加入其他战队
		if (activityMemberService.isApply(activityInvite.getTargetId(), Long.valueOf(id)) == 1) {
			activityInvite.setStatus(-1);
			activityInviteDao.save(activityInvite);
			return -6;
		}
		String sql = "";
		if (type == 0) {
			activityInvite.setStatus(1);
			activityInviteDao.save(activityInvite);
			return 0;
		} else {
			sql = SqlJoiner.join("select count(1) from activity_t_member where is_valid=1 and team_id=",
					String.valueOf(activityInvite.getTargetId()));
			Number total = (Number) queryDao.query(sql);
			if (total.intValue() < 5) {
				int flag = activityMemberService.addTeammate(phone, activityInvite.getTargetId(), cardId);
				if (flag == -1) {
					return 2;//用户尚未注册或资料不完善
				} else if (flag == -2) {
					activityInvite.setStatus(-1);
					activityInviteDao.save(activityInvite);
					return 3;////该用户已添加
				} else if (flag == -3) {
					activityInvite.setStatus(-1);
					activityInviteDao.save(activityInvite);
					return 1;//战队人数已满
				}
				activityInvite.setStatus(2);
				activityInviteDao.save(activityInvite);
				return 0;
			} else {
				activityInvite.setStatus(-1);
				activityInviteDao.save(activityInvite);
				return 1;//战队人数已满
			}
		}

	}

	public void save(List<ActivityInvite> invites) {
		activityInviteDao.save(invites);
	}

	public ActivityInvite save(ActivityInvite invite) {
		return activityInviteDao.save(invite);
	}

	/**
	 * 通过用户ID查询被邀请的约战
	 */
	public List<Map<String, Object>> findByUserIdWithMatchInfo(Long userId) {
		String sql = SqlJoiner
				.join("SELECT i.id inviteId, iu.nickname inviterNickname, iu.icon inviterIcon, m.id matchId, m.title, m.remark, m.server, ai.name itemName, ai.icon itemIcon, biu.icon userIcon",
						" FROM activity_r_invite i LEFT JOIN activity_t_matches m ON i.target_id = m.id AND m.is_valid = 1",
						" LEFT JOIN activity_r_items ai ON m.item_id = ai.id AND ai.is_valid = 1",
						" LEFT JOIN user_t_info iu ON i.invite_user_id = iu.id AND iu.is_valid = 1",
						" LEFT JOIN user_t_info biu ON i.invited_telephone = biu.username AND biu.is_valid = 1",
						" WHERE i.type = 1 AND biu.id = {userId} AND i.`status` = 0 AND m.is_valid = 1 AND m.is_start = 0 AND m.begin_time >= NOW()")
				.replace("{userId}", userId.toString());
		return queryDao.queryMap(sql);
	}

	public ActivityInvite findById(Long inviteId) {
		return activityInviteDao.findByIdAndValid(inviteId, CommonConstant.INT_BOOLEAN_TRUE);
	}

	public ActivityInvite findByIdAndStatus(Long inviteId, Integer status) {
		return activityInviteDao.findByIdAndStatusAndValid(inviteId, status, CommonConstant.INT_BOOLEAN_TRUE);
	}

	/**
	 * 查询用户的被邀请数
	 */
	public long findIdByTelephoneAndTargetIdAndType(Long userId, Long targetId, Integer type) {
		String sql = "SELECT i.id FROM activity_r_invite i LEFT JOIN user_t_info ui ON i.invited_telephone = ui.username WHERE ui.id = :userId AND i.target_id = :targetId AND i.type = :type AND i.is_valid = 1 AND i.status = 0";
		Number id = (Number) queryDao.query(sql.replaceAll(":userId", userId.toString())
				.replaceAll(":targetId", targetId.toString()).replaceAll(":type", type.toString()));
		return id == null ? 0 : id.longValue();
	}

	/**
	 * 查询手机号在某个赛事中是否 已被邀请
	 */
	public List<ActivityInvite> findMatchInvitesByTelephones(long matchId, String[] telephones) {
		return findByTypeAndTargetIdAndInvitedTelephones(ActivityConstant.INVITE_TYPE_MATCH, matchId, telephones);
	}

	/**
	 * 查询手机号是否已经在某类型中被邀请
	 */
	public List<ActivityInvite> findByTypeAndTargetIdAndInvitedTelephones(int type, long targetId, String[] telephones) {
		return activityInviteDao.findByTypeAndTargetIdAndInvitedTelephoneIn(type, targetId, telephones);
	}

	/*
	 * 根据用户ID查询成功邀请信息
	 */
	public List<Map<String, Object>> getInviteInfoByUserId(Long userId) {
		String sql = SqlJoiner.join(
				"SELECT id, create_date date, invited_telephone telephone FROM activity_r_invite WHERE is_valid=1 AND status=2 AND invite_user_id=",
				userId.toString(), " ORDER BY create_date DESC");
		return queryDao.queryMap(sql);
	}

	/*
	 * 邀请信息列表，分页
	 */
	public PageVO inviteList(int page, Long userId) {
		Integer pageSize = PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		page = page < 1 ? 1 : page;
		Integer startRow = (page - 1) * pageSize;
		String sqlLimit = SqlJoiner.join(" LIMIT ", startRow.toString(), ", ", pageSize.toString());

		String sql = SqlJoiner.join(
				"SELECT id, create_date date, invited_telephone telephone FROM activity_r_invite WHERE is_valid=1 AND status=2 AND invite_user_id=",
				userId.toString(), " ORDER BY create_date DESC", sqlLimit);

		String totalSql = SqlJoiner.join(
				"SELECT COUNT(1) FROM activity_r_invite WHERE is_valid=1 AND status=2 AND invite_user_id=",
				userId.toString());
		Number totalNum = queryDao.query(totalSql);
		int total = 0;
		if (totalNum != null) {
			total = totalNum.intValue();
		}
		List<Map<String, Object>> list = null;
		if (total > 0) {
			list = queryDao.queryMap(sql);
		}

		PageVO vo = new PageVO();
		vo.setList(list);
		vo.setTotal(total);
		return vo;
	}
}
