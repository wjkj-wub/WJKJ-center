package com.miqtech.master.service.msg;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.msg.MsgReadLog4SysDao;
import com.miqtech.master.entity.msg.MsgReadLog4Sys;
import com.miqtech.master.utils.DateUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.vo.PageVO;

/**
 * 我的消息service
 */
@Component
public class Msg4UserService {
	/*type: 1.订单 2.活动 3.系统  */
	private final static int TYPE_ORDER = 1;
	private final static int TYPE_ACTICITY = 2;
	private final static int TYPE_SYS = 3;
	private final static int TYPE_COMMENT = 4;

	@Autowired
	private QueryDao queryDao;
	@Autowired
	MsgReadLog4SysDao msgReadLog4SysDao;

	/**
	 * 查看我的消息接口,兼容原生app第一版
	 */
	public PageVO myMsg(Long userId, int page, int pageSize) {
		Date endDate = new Date();
		Date beginDate = DateUtils.addMonths(endDate, -1);

		String dateGTSql = " and create_date >='" + DateUtils.dateToString(beginDate, DateUtils.YYYY_MM_DD);
		String dateLTSql = "' and create_date <='" + DateUtils.dateToString(endDate, DateUtils.YYYY_MM_DD_HH_MM_SS)
				+ "'";

		String sql = SqlJoiner.join("select sum(num) from (select count(id) num from msg_t_user where user_id = ",
				userId.toString(), dateGTSql, dateLTSql,
				" and is_valid !=-1 union select count(id) num from msg_t_sys where type=2 and is_valid=1 ", dateGTSql,
				dateLTSql, " and id not in (select msg_id from msg_r_sys_read_log where user_id = " + userId
						+ ")) uniontable");
		Number totalCount = queryDao.query(sql);
		if (null != totalCount && totalCount.intValue() > 0) {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("userId", userId);
			String limitSql = "";
			if (page != -1 && pageSize != -1) {
				int start = (page - 1) * pageSize;
				params.put("start", start);
				params.put("pageSize", pageSize);
				limitSql = " limit :start,:pageSize";
			}
			params.put("beginDate", beginDate);
			params.put("endDate", endDate);
			String dateGTParamSql = " and s.create_date >= :beginDate";
			String dateLTParamSql = " and s.create_date <= :endDate ";

			sql = SqlJoiner
					.join("select * from",
							" 	(select id, ifnull(title, '') title, ifnull(content, '') content, ifnull(create_date, '') create_date,  type, is_read ",
							" 		from msg_t_user s where s.user_id = :userId and s.is_valid != -1 ",
							dateGTParamSql,
							dateLTParamSql,
							"	 union",
							" 	 select s.id, ifnull(s.title, '') title, ifnull(s.content, '') content, ifnull(s.create_date, '') create_date, 0 type, count(srl.user_id) is_read ",
							" 		from msg_t_sys s left join msg_r_sys_read_log srl  on srl.user_id = :userId  and s.id = srl.msg_id where ",
							"s.is_valid != -1", dateGTParamSql, dateLTParamSql,
							" and s.type = 2 and s.is_valid =1 and id not in  (select msg_id from msg_r_sys_read_log where user_id = "
									+ userId + ")) uniontable where id>0", " order by create_date desc ", limitSql);
			PageVO vo = new PageVO(queryDao.queryMap(sql, params));
			if (page * pageSize >= totalCount.intValue()) {
				vo.setIsLast(1);
			}
			return vo;
		} else {
			PageVO vo = new PageVO(new ArrayList<Map<String, Object>>());
			return vo;
		}
	}

	/**
	 * 按类型查看我的消息接口
	 */
	public PageVO myMsg(Long userId, int page, int pageSize, int type) {

		String typeConvert = typeConvert(type);
		if (StringUtils.isBlank(typeConvert)) {
			PageVO vo = new PageVO(new ArrayList<Map<String, Object>>());
			return vo;
		}
		Date endDate = new Date();
		Date beginDate = DateUtils.addMonths(endDate, -1);

		String dateGTSql = "  create_date >='" + DateUtils.dateToString(beginDate, DateUtils.YYYY_MM_DD);
		String dateLTSql = "' and create_date <='" + DateUtils.dateToString(endDate, DateUtils.YYYY_MM_DD_HH_MM_SS)
				+ "'";

		String sysCountSql = StringUtils.EMPTY;
		String sysQuerySql = StringUtils.EMPTY;
		if (type == Msg4UserService.TYPE_SYS) {
			sysCountSql = " union select count(id) num from msg_t_sys where "
					+ dateGTSql
					+ dateLTSql
					+ " and type =2 and is_valid=1  and  id not in (select msg_id from msg_r_sys_read_log where type = 2 and is_valid =- 1 and user_id = "
					+ userId + ") ";

			String dateGTParamSql = "  s.create_date >= :beginDate";
			String dateLTParamSql = " and s.create_date <= :endDate ";
			sysQuerySql = " union select s.id, ifnull(s.title, '') title, ifnull(s.content, '') content, ifnull(s.create_date, '') create_date, -1 type, if(count(srl.user_id) > 0, 1, 0) is_read ,0 obj_id"
					+ " 		from msg_t_sys s left join msg_r_sys_read_log srl  on srl.user_id = :userId  and s.id = srl.msg_id where "
					+ dateGTParamSql
					+ dateLTParamSql
					+ " and s.id not in (select msg_id from msg_r_sys_read_log where type=2 and user_id =:userId and is_valid=-1 ) and s.is_valid=1 and  s.type=2 group by s.id ";

		}
		String sql = SqlJoiner.join("select sum(num) from (select count(id) num from msg_t_user where  " + dateGTSql
				+ dateLTSql + " and user_id = ", userId.toString(), " and is_valid !=-1 and type in (" + typeConvert
				+ ")" + sysCountSql + ") uniontable");
		Number totalCount = queryDao.query(sql);
		if (null != totalCount && totalCount.intValue() > 0) {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("userId", userId);
			if (type == Msg4UserService.TYPE_SYS) {
				params.put("beginDate", DateUtils.dateToString(beginDate, DateUtils.YYYY_MM_DD));
				params.put("endDate", DateUtils.dateToString(endDate, DateUtils.YYYY_MM_DD_HH_MM_SS));
			}
			String limitSql = "";
			if (page != -1) {
				int start = (page - 1) * pageSize;
				params.put("start", start);
				params.put("pageSize", pageSize);
				limitSql = " limit :start,:pageSize";
			}
			String querySql = SqlJoiner
					.join("select * from",
							" 	(select id, ifnull(title, '') title, ifnull(content, '') content, ifnull(create_date, '') create_date,  type, is_read ,obj_id",
							" 		from msg_t_user where ", dateGTSql, dateLTSql,
							" and id>0 and user_id = :userId and is_valid !=-1 and type in (", typeConvert, ") ",
							sysQuerySql, "	order by create_date desc ) uniontable    ", limitSql);
			PageVO vo = new PageVO(queryDao.queryMap(querySql, params));
			if (page * pageSize >= totalCount.intValue()) {
				vo.setIsLast(1);
			}
			return vo;
		} else {
			PageVO vo = new PageVO(new ArrayList<Map<String, Object>>());
			return vo;
		}
	}

	/**
	 * 设置用户所有消息为已读状态
	 */
	public void msgRead(Long userId) {
		String sql = SqlJoiner.join("update msg_t_user set is_read=1 where user_id=", userId.toString());
		queryDao.update(sql);
	}

	/**
	 * 设置用户消息为已读状态
	 */
	public void msgRead(Long userId, int type, Long id) {
		if (type == -1) {
			MsgReadLog4Sys log = msgReadLog4SysDao.findByUserIdAndMsgIdAndType(userId, id, 2);
			if (null == log) {
				log = new MsgReadLog4Sys();
				log.setCreateDate(new Date());
				log.setMsgId(id);
				log.setValid(1);
				log.setType(2);
				log.setUserId(userId);
				msgReadLog4SysDao.save(log);
			}
		} else {
			String sql = SqlJoiner.join("update msg_t_user set is_read=1 where user_id=", userId.toString(),
					" and type=", String.valueOf(type), " and id =", id.toString());
			queryDao.update(sql);
		}
	}

	/**
	 * 删除用户消息
	 */
	public void delete(Long userId, int type, Long id) {
		if (type == -1) {
			MsgReadLog4Sys log = msgReadLog4SysDao.findByUserIdAndMsgIdAndType(userId, id, 2);
			if (null == log) {
				log = new MsgReadLog4Sys();
				log.setCreateDate(new Date());
				log.setMsgId(id);
				log.setValid(-1);
				log.setType(2);
				log.setUserId(userId);
			} else {
				log.setValid(-1);
				log.setUpdateDate(new Date());
			}
			msgReadLog4SysDao.save(log);
		} else {
			String sql = SqlJoiner.join("update msg_t_user set is_valid=-1 where user_id=", userId.toString(),
					" and type=", String.valueOf(type), " and id =", id.toString());
			queryDao.update(sql);
		}
	}

	/**
	 * 某个用户所有未读消息数量
	 */
	public int unReadMsgCount(Long userId) {
		int result = 0;
		Map<String, Integer> unReadMsgCountGroupByType = unReadMsgCountGroupByType(userId);
		for (Map.Entry<String, Integer> entry : unReadMsgCountGroupByType.entrySet()) {
			result += entry.getValue();
		}
		return result;
	}

	/**
	 * 查看用户分类的未读消息数量
	 */
	public Map<String, Integer> unReadMsgCountGroupByType(Long userId) {

		Date endDate = new Date();
		Date beginDate = DateUtils.addMonths(endDate, -1);

		String dateGTSql = "  create_date >='" + DateUtils.dateToString(beginDate, DateUtils.YYYY_MM_DD);
		String dateLTSql = "' and create_date <='" + DateUtils.dateToString(endDate, DateUtils.YYYY_MM_DD_HH_MM_SS)
				+ "'";
		int sysCountNum = 0;
		int orderCountNum = 0;
		int activityCountNum = 0;
		int commentCountNum = 0;
		Map<String, Integer> counts = Maps.newHashMap();
		String sysCountSql = "select -1 type, count(1) countNum from msg_t_sys ms  where " + dateGTSql + dateLTSql
				+ " and ms.id not in( select msg_id from msg_r_sys_read_log where user_id =" + userId
				+ ") and ms.type=2 and ms.is_valid =1 ";
		Map<String, Object> sysCount = queryDao.querySingleMap(sysCountSql);
		String sql = "select type,count(1) countNum from msg_t_user where is_read=0 and is_valid!=-1 and " + dateGTSql
				+ dateLTSql + " and user_id =" + userId + " group by type";
		List<Map<String, Object>> result = queryDao.queryMap(sql);
		result.add(sysCount);

		for (Map<String, Object> map : result) {
			int type = NumberUtils.toInt(map.get("type").toString());
			int count = NumberUtils.toInt(map.get("countNum").toString());
			switch (type) {
			case -1: {
				sysCountNum += count;
				break;
			}
			case 0: {
				sysCountNum += count;
				break;
			}
			case 1: {
				sysCountNum += count;
				break;
			}
			case 2: {
				sysCountNum += count;
				break;
			}
			case 3: {
				orderCountNum += count;
				break;
			}
			case 4: {
				orderCountNum += count;
				break;
			}
			case 5: {
				activityCountNum += count;
				break;
			}
			case 6: {
				activityCountNum += count;
				break;
			}
			case 7: {
				activityCountNum += count;
				break;
			}
			case 8: {
				sysCountNum += count;
				break;
			}
			case 9: {
				commentCountNum += count;
				break;
			}
			case 10: {
				sysCountNum += count;
				break;
			}
			case 11: {
				sysCountNum += count;
				break;
			}
			case 12: {
				sysCountNum += count;
				break;
			}
			case 13: {
				sysCountNum += count;
				break;
			}
			case 14: {
				sysCountNum += count;
				break;
			}
			case 15: {
				sysCountNum += count;
				break;
			}
			case 16: {
				activityCountNum += count;
				break;
			}
			case 17: {
				sysCountNum += count;
				break;
			}
			default: {
				break;
			}
			}
		}
		counts.put("sys", sysCountNum);
		counts.put("activity", activityCountNum);
		counts.put("order", orderCountNum);
		counts.put("comment", commentCountNum);
		return counts;
	}

	private String typeConvert(int type) {
		switch (type) {
		case TYPE_SYS: {
			return "0,1,2,8,10,12,13,14,15,17";////系统 :红包消息 会员消息 评论点赞 商品兑换 众筹夺宝 兑奖专区商品详情 悬赏令
		}
		case TYPE_ORDER: {
			return "3,4";//订单类: 预定消息 支付消息
		}
		case TYPE_ACTICITY: {
			return "5,6,7,16";//活动类:赛事消息 约战消息 娱乐赛消息,自发赛消息
		}
		case TYPE_COMMENT: {
			return "9";//评论消息
		}
		default:
			return null;
		}
	}
}
