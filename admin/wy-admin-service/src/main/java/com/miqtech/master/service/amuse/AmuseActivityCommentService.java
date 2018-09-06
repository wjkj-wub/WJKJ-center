package com.miqtech.master.service.amuse;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.amuse.AmuseActivityCommentDao;
import com.miqtech.master.dao.user.UserGagDao;
import com.miqtech.master.entity.amuse.AmuseActivityComment;
import com.miqtech.master.entity.user.UserGag;
import com.miqtech.master.utils.DateUtils;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.vo.PageVO;

/**
 * 娱乐赛评论service
 */
@Component
public class AmuseActivityCommentService {
	@Autowired
	private AmuseActivityCommentDao amuseActivityCommentDao;
	@Autowired
	private QueryDao queryDao;
	@Autowired
	private UserGagDao userGagDao;

	/**
	 * 保存
	 * 
	 * @return
	 */
	public AmuseActivityComment save(AmuseActivityComment amuseActivityComment) {
		if (amuseActivityComment != null) {
			return amuseActivityCommentDao.save(amuseActivityComment);
		}
		return null;
	}

	/**
	 * 根据ID查实体
	 */
	public AmuseActivityComment findById(Long id) {
		return amuseActivityCommentDao.findOne(id);
	}

	/**
	 * API3.0-3.1.0接口：评论列表
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> amuseCommentList(int page, int size, int replySize, int type, long amuseId,
			long userId) {
		int isLast = 0;
		page = page < 1 ? 1 : page;
		size = size <= 0 ? PageUtils.API_DEFAULT_PAGE_SIZE : size;
		replySize = replySize <= 0 ? 5 : replySize;
		int start = (page - 1) * size;
		String sqlLikeCount = ", (SELECT COUNT(1) FROM activity_comment_praise WHERE is_valid = 1 AND comment_id=b.id)+ifnull(b.virtual_like_count,0) likeCount";
		String sqlIsPraise = ", IF((SELECT COUNT(1) FROM activity_comment_praise WHERE is_valid = 1 AND comment_id=b.id AND user_id="
				+ userId + ")>0,1,0) isPraise";
		String sqlReplyCount = ", (SELECT count(1) FROM amuse_r_activity_comment WHERE is_valid = 1 AND type = :type AND amuse_id = :amuseId AND parent_id = b.id) replyCount";
		String sqlComments = SqlJoiner.join(
				"(SELECT u.id userId, u.icon, u.nickname, u2.nickname replyname, u2.id replyUserId, b.id, b.content, b.create_date createDate, b.parent_id, b.reply_id,b.img,ifnull(b.bounty_hunter_flag,0) bounty_hunter_flag",
				sqlLikeCount, sqlIsPraise, sqlReplyCount, " FROM amuse_r_activity_comment b",
				" LEFT JOIN user_t_info u ON u.id = b.user_id",
				" LEFT JOIN user_t_info u2 ON u2.id = ( SELECT user_id FROM amuse_r_activity_comment WHERE id = b.reply_id )",
				" WHERE b.is_valid = 1 AND b.parent_id = 0 AND b.type = :type AND b.amuse_id = :amuseId",
				" ORDER BY b.create_date DESC LIMIT :start , :rows)", " UNION ",
				"(SELECT u.id userId, u.icon, u.nickname, u2.nickname replyname, u2.id replyUserId, b.id, b.content, b.create_date createDate, b.parent_id, b.reply_id,b.img,ifnull(b.bounty_hunter_flag,0) bounty_hunter_flag, 0 likeCount, 0 isPraise, 0 replyCount",
				" FROM (SELECT c.*, IF(@area = amuse_id AND @parent_id = parent_id, @rank \\:= @rank + 1, @rank \\:= 1) AS rank, @area \\:= amuse_id, @parent_id \\:= parent_id",
				" FROM amuse_r_activity_comment c ,(select @area \\:= NULL ,@parent_id \\:=0,  @rank \\:=0) aaaa WHERE c.is_valid = 1 AND c.parent_id > 0 AND c.parent_id IN",
				" (SELECT id FROM (SELECT id FROM amuse_r_activity_comment WHERE is_valid = 1 AND parent_id = 0 AND type = :type AND amuse_id = :amuseId ORDER BY create_date DESC LIMIT :start, :rows) a )",
				" ORDER BY amuse_id, parent_id, create_date ) b", " LEFT JOIN user_t_info u ON u.id = b.user_id",
				" LEFT JOIN user_t_info u2 ON u2.id = ( SELECT user_id FROM amuse_r_activity_comment WHERE id = b.reply_id ) WHERE rank <= :replyRows )",
				" ORDER BY CASE WHEN parent_id = 0 THEN id ELSE parent_id END DESC, CASE WHEN parent_id = 0 THEN NULL ELSE createDate END");
		Map<String, Object> params = Maps.newHashMap();
		params.put("type", type);
		params.put("amuseId", amuseId);
		params.put("start", start);
		params.put("rows", size);
		params.put("replyRows", replySize);
		String sqlTotal = "select count(1) from amuse_r_activity_comment where is_valid=1 and parent_id=0 and amuse_id="
				+ amuseId + " and type=" + type;
		Number total = queryDao.query(sqlTotal);
		if (null == total || start + size >= total.intValue()) {
			isLast = 1;
		}
		List<Map<String, Object>> listComp = null;
		if (null != total && total.intValue() > 0) {
			listComp = queryDao.queryMap(sqlComments, params);
		}
		// 处理结果集
		List<Map<String, Object>> list = null;
		if (CollectionUtils.isNotEmpty(listComp)) {
			list = Lists.newArrayList();
			int commentIndex = 0;
			for (int i = 0; i < listComp.size(); i++) {
				Map<String, Object> map = listComp.get(i);
				Object parentIdObj = map.get("parent_id");
				int parentId = null == parentIdObj ? 0 : NumberUtils.toInt(parentIdObj.toString());
				if (parentId == 0) {

					list.add(map);
					int floorNum = total.intValue() - commentIndex - (page - 1) * size;
					map.put("floor_num", floorNum);
					commentIndex = commentIndex + 1;
				} else {
					int index = list.size() - 1;
					Map<String, Object> listOfLast = list.get(index);
					if (index >= 0 && listOfLast.containsKey("replyList")) {
						((List<Map<String, Object>>) listOfLast.get("replyList")).add(map);
					} else {
						List<Map<String, Object>> replyList = Lists.newArrayList();
						replyList.add(map);
						listOfLast.put("replyList", replyList);
					}
				}
			}
		}
		Map<String, Object> returnMap = Maps.newHashMap();
		returnMap.put("list", list);
		returnMap.put("isLast", isLast);
		returnMap.put("total", total == null ? 0 : total.intValue());
		return returnMap;
	}

	/**
	 * API3.1.0接口：热门评论10条
	 */
	public List<Map<String, Object>> hotCommentList(int replySize, int type, long amuseId, long userId) {
		String sqlLikeCount = ", (SELECT COUNT(1) FROM activity_comment_praise WHERE is_valid = 1 AND comment_id=b.id)+ifnull(b.virtual_like_count,0) likeCount";
		String sqlIsPraise = ", IF((SELECT COUNT(1) FROM activity_comment_praise WHERE is_valid = 1 AND comment_id=b.id AND user_id="
				+ userId + ")>0,1,0) isPraise";
		String sqlReplyCount = ", (SELECT count(1) FROM amuse_r_activity_comment WHERE is_valid = 1 AND type = :type AND amuse_id = :amuseId AND parent_id = b.id) replyCount";
		String sqlComments = SqlJoiner.join(
				"(SELECT u.id userId, u.icon, u.nickname, u2.nickname replyname, u2.id replyUserId, b.id, b.content, b.create_date createDate, b.parent_id, b.reply_id,b.img,ifnull(b.bounty_hunter_flag,0) bounty_hunter_flag",
				sqlLikeCount, sqlIsPraise, sqlReplyCount, " FROM amuse_r_activity_comment b",
				" LEFT JOIN user_t_info u ON u.id = b.user_id",
				" LEFT JOIN user_t_info u2 ON u2.id = ( SELECT user_id FROM amuse_r_activity_comment WHERE id = b.reply_id )",
				" WHERE b.is_valid = 1 AND b.parent_id = 0 AND b.type = :type AND b.amuse_id = :amuseId",
				" HAVING likeCount>0 ORDER BY likeCount DESC LIMIT 0 , 10)", " UNION ",
				"(SELECT u.id userId, u.icon, u.nickname, u2.nickname replyname, u2.id replyUserId, b.id, b.content, b.create_date createDate, b.parent_id, b.reply_id,b.img,ifnull(b.bounty_hunter_flag,0) bounty_hunter_flag, 0 likeCount, 0 isPraise, 0 replyCount",
				" FROM (SELECT c.*, IF(@area = amuse_id AND @parent_id = parent_id, @rank \\:= @rank + 1, @rank \\:= 1) AS rank, @area \\:= amuse_id, @parent_id \\:= parent_id",
				" FROM amuse_r_activity_comment c ,(select @area \\:= NULL ,@parent_id \\:=0,  @rank \\:=0) aaaa WHERE c.is_valid = 1 AND c.parent_id > 0 AND c.parent_id IN",
				" (SELECT id FROM (SELECT id, (SELECT COUNT(1) FROM activity_comment_praise WHERE is_valid = 1 AND comment_id = bbb.id)likeCount FROM amuse_r_activity_comment bbb WHERE is_valid = 1 AND parent_id = 0 AND type = :type AND amuse_id = :amuseId HAVING likeCount>0 ORDER BY likeCount DESC LIMIT 0, 10) a )",
				" ORDER BY amuse_id, parent_id, create_date ) b", " LEFT JOIN user_t_info u ON u.id = b.user_id",
				" LEFT JOIN user_t_info u2 ON u2.id = ( SELECT user_id FROM amuse_r_activity_comment WHERE id = b.reply_id ) WHERE rank <= 5 )",
				" ORDER BY parent_id, likeCount DESC, createDate ");
		Map<String, Object> params = Maps.newHashMap();
		params.put("type", type);
		params.put("amuseId", amuseId);
		List<Map<String, Object>> listComp = queryDao.queryMap(sqlComments, params);
		// 处理结果集
		List<Map<String, Object>> hotList = null;
		if (CollectionUtils.isNotEmpty(listComp)) {
			hotList = Lists.newArrayList();
			int listCompSize = listComp.size();
			for (int i = 0; i < listCompSize; i++) {
				Map<String, Object> map = listComp.get(i);
				Object parentIdObj = map.get("parent_id");
				long parentId = null == parentIdObj ? 0 : NumberUtils.toLong(parentIdObj.toString());
				if (parentId == 0) {
					hotList.add(map);
					Object idObj = map.get("id");
					int start = 0;
					for (int j = i + 1; j < listCompSize; j++) {
						if (null != idObj && idObj.equals(listComp.get(j).get("parent_id"))) {
							start = j;
							break;
						}
					}
					Object replyCountObj = map.get("replyCount");
					int replyCount = null == replyCountObj ? 0 : NumberUtils.toInt(replyCountObj.toString());
					if (replyCount > 0) {
						List<Map<String, Object>> replyList = Lists.newArrayList();
						replyCount = replyCount > 5 ? 5 : replyCount;
						for (int k = start; k < start + replyCount; k++) {
							replyList.add(listComp.get(k));
						}
						hotList.get(hotList.size() - 1).put("replyList", replyList);
					}
				} else {
					break;
				}
			}
		}
		return hotList;
	}

	/**
	 * API3.1.0接口：楼中楼更多回复列表
	 */
	public Map<String, Object> amuseReplyList(int page, int size, int type, long amuseId, long parentId, long replyId,
			long userId) {
		if (page < 1) {
			page = 1;
		}
		if (size <= 0) {
			size = 20;
		}
		int rows = size;
		int start = (page - 1) * rows;
		int isLast = 0;
		Map<String, Object> returnMap = Maps.newHashMap();
		// 适配N-20的情况
		if (replyId > 0) {
			int overtwenty = 0;
			String sqlN = SqlJoiner.join("select count(1) from amuse_r_activity_comment where is_valid=1 and type=",
					String.valueOf(type), " and amuse_id=", String.valueOf(amuseId), " and parent_id=",
					String.valueOf(parentId), " and id < ", String.valueOf(replyId));
			int N = 0;
			Number num = queryDao.query(sqlN);
			if (null != num) {
				N = num.intValue();
			}
			if (N > 20) {
				start += N - 20;
				overtwenty = 1;
			}
			returnMap.put("overtwenty", overtwenty);
		}
		if (page == 1) {
			String sqlLikeCount = " (SELECT COUNT(1) FROM activity_comment_praise WHERE is_valid = 1 AND comment_id=c.id)+ifnull(c.virtual_like_count,0) likeCount,";
			String sqlIsPraise = " IF((SELECT COUNT(1) FROM activity_comment_praise WHERE is_valid = 1 AND comment_id=c.id AND user_id="
					+ userId + ")>0,1,0) isPraise,";
			String sqlParent = SqlJoiner.join("select ", sqlLikeCount, sqlIsPraise,
					" u.id userId, u.icon, u.nickname, c.id, c.content, c.create_date createDate, c.parent_id, c.reply_id,c.img,ifnull(c.bounty_hunter_flag,0) bounty_hunter_flag from amuse_r_activity_comment c",
					" left join user_t_info u on u.id=c.user_id where c.id=", String.valueOf(parentId));
			Map<String, Object> parent = queryDao.querySingleMap(sqlParent);
			returnMap.put("parent", parent);
		}
		String sqlReplys = SqlJoiner.join(
				"select u.id userId, u.nickname, u2.id replyUserId, u2.nickname replyname, c.id, c.content, c.create_date createDate, c.parent_id, c.reply_id,c.img,ifnull(c.bounty_hunter_flag,0) bounty_hunter_flag from amuse_r_activity_comment c",
				" left join user_t_info u on u.id=c.user_id",
				" left join user_t_info u2 on u2.id=(SELECT user_id FROM amuse_r_activity_comment WHERE id = c.reply_id )",
				" where c.is_valid=1 and c.type=", String.valueOf(type), " and c.amuse_id=", String.valueOf(amuseId),
				" and c.parent_id=", String.valueOf(parentId), " order by c.create_date asc",
				" limit " + start + ", " + rows);
		List<Map<String, Object>> list = queryDao.queryMap(sqlReplys);
		String sqlTotal = SqlJoiner.join("select count(1) from amuse_r_activity_comment where is_valid=1 and type=",
				String.valueOf(type), " and amuse_id=", String.valueOf(amuseId), " and parent_id=",
				String.valueOf(parentId));
		Number total = queryDao.query(sqlTotal);
		if (null == total || start + rows >= total.intValue()) {
			isLast = 1;
		}
		returnMap.put("list", list);
		returnMap.put("isLast", isLast);
		return returnMap;
	}

	/**
	 * 根据ID查询娱乐赛评论信息
	 */
	public Map<String, Object> getAmuseInfoById(long commentId) {
		String sqlQuery = SqlJoiner.join(
				"/*master*/ select u.id userId, u.icon, u.nickname, u2.id replyUserId, u2.nickname replyname, c.id, c.content, c.create_date createDate, c.parent_id, c.reply_id,c.img from amuse_r_activity_comment c",
				" left join user_t_info u on u.id=c.user_id",
				" LEFT JOIN user_t_info u2 ON u2.id=(SELECT user_id FROM amuse_r_activity_comment WHERE id=c.reply_id)",
				" where c.is_valid=1 and c.id=" + commentId);
		Map<String, Object> commentInfo = queryDao.querySingleMap(sqlQuery);

		return commentInfo;
	}

	/**
	 * 消息模块-查询我的评论
	 */
	public PageVO myComment(Long userId, int page, int pageSize) {
		PageVO vo = new PageVO();
		String sql = SqlJoiner.join(
				"SELECT count(1) FROM amuse_r_activity_comment a WHERE a.reply_id IN ( SELECT id FROM amuse_r_activity_comment a WHERE a.user_id =",
				String.valueOf(userId),
				" AND a.is_valid = 1 ) and id in(select obj_id from msg_t_user where type=9 and is_valid=1)");
		Number total = queryDao.query(sql);
		if (total != null) {
			vo.setTotal(total.intValue());
		}
		if (page * pageSize >= total.intValue()) {
			vo.setIsLast(1);
		}
		String limitSql = "";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("userId", userId);
		if (page != -1 && pageSize != -1) {
			params.put("start", (page - 1) * pageSize);
			params.put("pageSize", pageSize);
			limitSql = " limit :start,:pageSize";
		}
		sql = SqlJoiner.join(
				"select a.*,f.obj_id,f.id,f.is_read from (SELECT 9 type,a.id comment_id, a.parent_id, a.user_id,a.amuse_id activity_id,a.type activity_type, b.nickname, b.icon, a.create_date, a.content, c.content my_content, e.nickname reply_nickname FROM amuse_r_activity_comment a LEFT JOIN user_t_info b ON a.user_id = b.id LEFT JOIN amuse_r_activity_comment c ON a.reply_id = c.id LEFT JOIN amuse_r_activity_comment d ON c.reply_id = d.id LEFT JOIN user_t_info e ON d.user_id = e.id WHERE a.reply_id IN ( SELECT id FROM amuse_r_activity_comment a WHERE a.user_id =:userId ) ORDER BY create_date DESC )a ",
				" join msg_t_user f on a.comment_id=f.obj_id and f.type=9 and f.is_valid=1 order by create_date DESC ",
				limitSql);
		vo.setList(queryDao.queryMap(sql, params));
		return vo;
	}

	public PageVO queryAllComments(Map<String, Object> params) {
		PageVO vo = new PageVO();
		String countSql = " select aac.id from amuse_r_activity_comment aac ";
		String querySql = "";
		String whereSql = " where aac.is_valid =1 and commentSubject.is_valid = 1 ";
		String orderSql = " order by aac.create_date desc ";
		String groupbySql = " group  by aac.id ";
		int page = NumberUtils.toInt(params.get("page").toString());
		int start = PageUtils.calcStart(page);
		String limitSql = "limit " + start + "," + PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		if (params.containsKey("commentId")) {
			whereSql = whereSql + " and aac.id = " + params.get("commentId");
		}
		if (params.containsKey("startDate") && params.containsKey("endDate")) {
			whereSql = whereSql + "	and aac.create_date>='" + params.get("startDate") + "'	"
					+ "	and aac.create_date<'" + params.get("endDate") + " 23:59:59' 	";
		}
		if (params.containsKey("content")) {
			whereSql = whereSql + " and aac.content like '%" + params.get("content") + "%'	 ";
		}
		if (params.containsKey("nickname")) {
			whereSql = whereSql + "	and ui.nickname like '%" + params.get("nickname") + "%'	";
		}
		if (params.containsKey("orderLikeCount")) {
			orderSql = " order by aac.like_count desc ,aac.create_date desc ";
		}
		if (!params.containsKey("moduleId") && !params.containsKey("commentType") && !params.containsKey("nickname")) {
			querySql = " select aac.id ,aac.content,aac.create_date ,count(acp.id) like_count,aac.contentType,aac.title, aac.subjectType,group_concat(distinct aam.name) moudleName, preaac.content preContent, ui.nickname,if(gag.user_id is null,0,1) as flag,aac.user_id,CONCAT(gag.days,',',gag.create_date) contentadd,gag.id gagId,aac.amuse_id	"
					+ "	from (select aac.id ,aac.content,aac.create_date,aac.type contentType,commentSubject.title,aac.reply_id, commentSubject.type subjectType,aac.user_id,aac.amuse_id from amuse_r_activity_comment aac";
		} else {
			querySql = " select aac.id ,aac.content,aac.create_date ,count(acp.id) like_count,aac.type contentType,commentSubject.title, commentSubject.type subjectType,group_concat(distinct aam.name) moudleName, preaac.content preContent, ui.nickname,if(gag.user_id is null,0,1) as flag,aac.user_id,CONCAT(gag.days,',',gag.create_date) contentadd,gag.id gagId,aac.amuse_id	"
					+ "	from amuse_r_activity_comment aac  left join activity_comment_praise acp on acp.comment_id = aac.id and acp.is_valid=1	";
		}

		int tableType = NumberUtils.toInt(params.get("tableType").toString());
		String leftJoin = "";
		switch (tableType) {
		case 1:
			if (!params.containsKey("moduleId") && !params.containsKey("commentType")
					&& !params.containsKey("nickname")) {
				querySql = querySql
						+ " left join amuse_t_activity commentSubject on commentSubject.id = aac.amuse_id and commentSubject.is_valid = 1 "
						+ whereSql + groupbySql + orderSql + limitSql + ")aac";
			}
			leftJoin = leftJoin
					+ " left join amuse_t_activity commentSubject on commentSubject.id = aac.amuse_id and commentSubject.is_valid = 1";

			querySql = querySql.replace("commentSubject.type subjectType,group_concat(distinct aam.name) moudleName,",
					"");
			break;
		case 2:
			if (!params.containsKey("moduleId") && !params.containsKey("commentType")
					&& !params.containsKey("nickname")) {
				querySql = querySql
						+ " left join amuse_t_activity commentSubject on commentSubject.id = aac.amuse_id and commentSubject.is_valid = 1 "
						+ whereSql + groupbySql + orderSql + limitSql + ")aac";
			}
			leftJoin = leftJoin
					+ " left join activity_t_info commentSubject on commentSubject.id = aac.amuse_id and commentSubject.is_valid = 1";

			querySql = querySql.replace("commentSubject.type subjectType,group_concat(distinct aam.name) moudleName,",
					"");
			break;

		case 3:
			if (!params.containsKey("moduleId") && !params.containsKey("commentType")
					&& !params.containsKey("nickname")) {
				querySql = querySql
						+ " left join amuse_t_activity commentSubject on commentSubject.id = aac.amuse_id and commentSubject.is_valid = 1 "
						+ whereSql + groupbySql + orderSql + limitSql + ")aac";
			}
			leftJoin = leftJoin
					+ " left join bounty commentSubject on commentSubject.id = aac.amuse_id and commentSubject.is_valid = 1";

			querySql = querySql.replace("commentSubject.type subjectType,group_concat(distinct aam.name) moudleName,",
					"");
			break;

		default:
			if (!params.containsKey("moduleId") && !params.containsKey("commentType")
					&& !params.containsKey("nickname")) {
				querySql = querySql
						+ " left join activity_t_over_activities commentSubject on commentSubject.id = aac.amuse_id and commentSubject.is_valid = 1 "
						+ whereSql + groupbySql + orderSql + limitSql + ")aac"
						+ " left join activity_over_activity_module_info  aami on aami.over_activity_id = aac.amuse_id"
						+ " left join activity_over_activity_module  aam on aam.id = aami.module_id and aam.is_valid = 1 ";
			}
			leftJoin = leftJoin
					+ " left join activity_t_over_activities commentSubject on commentSubject.id = aac.amuse_id and commentSubject.is_valid = 1"
					+ " left join activity_over_activity_module_info  aami on aami.over_activity_id =commentSubject.id"
					+ " left join activity_over_activity_module  aam on aam.id = aami.module_id and aam.is_valid = 1 ";

			if (params.containsKey("moduleId")) {
				whereSql = whereSql + "	and aam.id in (select id from activity_over_activity_module where pid = "
						+ params.get("moduleId") + " or id = " + params.get("moduleId") + " )	";
			}

			if (params.containsKey("commentType")) {
				whereSql = whereSql + " and commentSubject.type=" + params.get("commentType");
			}

			break;
		}
		if (!params.containsKey("moduleId") && !params.containsKey("commentType") && !params.containsKey("nickname")) {
			querySql += " left join amuse_r_activity_comment preaac on aac.reply_id = preaac.id	"
					+ "	left join user_t_info ui on aac.user_id = ui.id	"
					+ " left join user_t_gag gag on aac.user_id=gag.user_id";
		}
		leftJoin += "	left join amuse_r_activity_comment preaac on aac.reply_id = preaac.id	"
				+ "	left join user_t_info ui on aac.user_id = ui.id	"
				+ " left join user_t_gag gag on aac.user_id=gag.user_id";
		countSql = " select count(1) from (" + countSql + leftJoin + whereSql + groupbySql + ") a";
		Number totalCount = queryDao.query(countSql);

		if (totalCount == null || totalCount.intValue() <= 0) {
			vo.setTotal(0);
			vo.setCurrentPage(page);
			vo.setIsLast(1);
			return vo;
		}
		if (!params.containsKey("moduleId") && !params.containsKey("commentType") && !params.containsKey("nickname")) {
			querySql = querySql + " left join activity_comment_praise acp on acp.comment_id = aac.id and acp.is_valid=1"
					+ groupbySql;
		} else {
			querySql = querySql + leftJoin + whereSql + groupbySql + orderSql + limitSql;
		}
		List<Map<String, Object>> queryMap = queryDao.queryMap(querySql);
		if (CollectionUtils.isNotEmpty(queryMap)) {
			vo.setList(queryMap);
		}
		vo.setTotal(totalCount.longValue());
		vo.setCurrentPage(page);
		vo.setIsLast(PageUtils.isBottom(page, totalCount.longValue()));
		return vo;
	}

	public PageVO querySubject(Map<String, Object> params) {

		PageVO vo = new PageVO();
		String countSql = "";
		String querySql = "";
		String whereSql = "";
		String leftJoinSql = "";
		String orderSql = "";

		int tableType = NumberUtils.toInt(params.get("tableType").toString());
		switch (tableType) {
		case 1:// 娱乐赛评论
			countSql = " select count(1) from ( select subject.id from amuse_t_activity  subject  ";
			querySql = " select subject.id,subject.title,subject.create_date,count(comment.id) ccount,comment.amuse_id  from amuse_t_activity subject ";
			whereSql = " where  subject.is_valid=1 ";
			if (params.containsKey("startDate") && params.containsKey("endDate")) {
				whereSql = whereSql + "	and subject.create_date>='" + params.get("startDate") + "'	"
						+ "	and subject.create_date<'" + params.get("endDate") + " 23:59:59' 	";
			}

			leftJoinSql = leftJoinSql
					+ " left join amuse_r_activity_comment comment on comment.type = 1 and comment.amuse_id = subject.id and comment.amuse_id = subject.id and comment.is_valid=1 ";
			orderSql = " order by create_date desc ";
			if (params.containsKey("orderComment")) {
				orderSql = " order by ccount asc ,create_date desc ";
			}
			break;
		case 2:// 官方赛评论
			countSql = " select count(1) from (select subject.id from activity_t_info  subject ";
			querySql = " select subject.id,subject.title,subject.create_date,count(comment.id) ccount,comment.amuse_id  from activity_t_info subject";
			whereSql = " where  subject.is_valid=1 ";
			if (params.containsKey("startDate") && params.containsKey("endDate")) {
				whereSql = whereSql + "	and subject.create_date>='" + params.get("startDate") + "'	"
						+ "	and subject.create_date<'" + params.get("endDate") + " 23:59:59' 	";
			}
			orderSql = " order by create_date desc ";
			if (params.containsKey("orderComment")) {
				orderSql = "  order by ccount asc ,create_date desc ";
			}

			leftJoinSql = leftJoinSql
					+ " left join amuse_r_activity_comment comment on comment.type = 2 and comment.amuse_id = subject.id and comment.amuse_id = subject.id  and comment.is_valid=1 ";

			break;
		case 3:// 悬赏令评论
			countSql = " select count(1) from (select subject.id from bounty  subject ";
			querySql = " select subject.id,subject.title,subject.create_date,count(comment.id) ccount,comment.amuse_id  from bounty subject";
			whereSql = " where  subject.is_valid=1 ";
			if (params.containsKey("startDate") && params.containsKey("endDate")) {
				whereSql = whereSql + "	and subject.create_date>='" + params.get("startDate") + "'	"
						+ "	and subject.create_date<'" + params.get("endDate") + " 23:59:59' 	";
			}
			orderSql = " order by create_date desc ";
			if (params.containsKey("orderComment")) {
				orderSql = "  order by ccount asc ,create_date desc ";
			}

			leftJoinSql = leftJoinSql
					+ " left join amuse_r_activity_comment comment on comment.type = 6 and comment.amuse_id = subject.id and comment.amuse_id = subject.id  and comment.is_valid=1 ";

			break;

		default:// 资讯评论
			countSql = " select count(1) from ( select subject.id from activity_t_over_activities  subject  ";
			querySql = " select subject.id,subject.title,subject.timer_date create_date,count(comment.id) ccount ,aam.name moudleName,subject.type,comment.amuse_id from activity_t_over_activities subject ";
			whereSql = " where subject.is_valid =1 and subject.timer_date<=now() and subject.is_show=1 ";
			if (params.containsKey("startDate") && params.containsKey("endDate")) {
				whereSql = whereSql + "	and subject.timer_date>='" + params.get("startDate") + "'	"
						+ "	and subject.timer_date<'" + params.get("endDate") + " 23:59:59' 	";
			}

			if (params.containsKey("moduleId")) {
				whereSql = whereSql + "	and aam.id in (select id from activity_over_activity_module where pid = "
						+ params.get("moduleId") + " or id = " + params.get("moduleId") + " )	";
			}
			if (params.containsKey("commentType")) {
				whereSql = whereSql + " and subject.type=" + params.get("commentType");
			}
			orderSql = " order by create_date desc ";
			if (params.containsKey("orderComment")) {
				orderSql = " order by ccount asc ,create_date desc ";
			}

			leftJoinSql = leftJoinSql
					+ " left join amuse_r_activity_comment comment on comment.type = 3 and comment.amuse_id = subject.id and comment.amuse_id = subject.id and comment.is_valid=1 "
					+ " left join activity_over_activity_module_info  aami on aami.over_activity_id = subject.id"
					+ " left join activity_over_activity_module  aam on aam.id = aami.module_id and aam.is_valid =1 ";
			break;
		}

		if (params.containsKey("infoId")) {
			whereSql = whereSql + " and subject.id = " + params.get("infoId");
		}

		if (params.containsKey("title")) {
			whereSql = whereSql + "and subject.title like '%" + params.get("title") + "%'	 ";
		}

		int page = NumberUtils.toInt(params.get("page").toString());
		Number totalCount = queryDao.query(countSql + leftJoinSql + whereSql + " group by subject .id) aaa");

		if (totalCount == null || totalCount.intValue() <= 0) {
			vo.setTotal(0);
			vo.setCurrentPage(page);
			vo.setIsLast(1);
			return vo;
		}

		int start = PageUtils.calcStart(page);
		String limitSql = "limit " + start + "," + PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		querySql = "select * from (" + querySql + leftJoinSql + whereSql + " group by subject.id)  aaa " + orderSql
				+ limitSql;
		List<Map<String, Object>> queryMap = queryDao.queryMap(querySql);
		if (CollectionUtils.isNotEmpty(queryMap)) {
			vo.setList(queryMap);
		}
		vo.setTotal(totalCount.longValue());
		vo.setCurrentPage(page);
		vo.setIsLast(PageUtils.isBottom(page, totalCount.longValue()));
		return vo;
	}

	/**
	 * 保存禁言用户信息
	 */
	public UserGag save(UserGag userGag) {
		if (userGag != null) {
			return userGagDao.save(userGag);
		}
		return null;
	}

	/**
	 * 删除禁言用户
	 */
	public List<UserGag> findAll() {
		return userGagDao.findAll();
	}

	/**
	 * 删除禁言用户信息
	 */
	public void delete(Long id) {
		if (id != null) {
			userGagDao.delete(id);
			return;
		}
		return;
	}

	/**
	 * 查询评论信息
	 */
	public PageVO queryComment(Long amuseId, String nickname, String content, String orderLikeCount, String page) {
		PageVO vo = new PageVO();
		String addSql = "";
		String orderSql = "";
		String limitSql = "";
		int page1 = NumberUtils.toInt(page);
		int start = PageUtils.calcStart(page1);
		limitSql = "limit " + start + "," + PageUtils.ADMIN_DEFAULT_PAGE_SIZE;

		if (StringUtils.isNotBlank(nickname)) {
			addSql = "	and d.nickname like '%" + nickname + "%'	";
		}
		if (StringUtils.isNotBlank(content)) {
			addSql = addSql + " and a.content like '%" + content + "%'	 ";
		}

		if (StringUtils.isNotBlank(orderLikeCount)) {
			orderSql = " group by a.id,f.id order by like_count desc ,a.create_date desc ";
		} else {
			orderSql = " group by a.id,f.id order by a.create_date desc ";
		}
		String querySql = "select a.content,b.content precontent,a.id,a.create_date,count(c.id) like_count,d.nickname,if(gag.user_id is null,0,1) as flag,a.user_id,CONCAT(gag.days,',',gag.create_date) contentadd,gag.id gagId,a.amuse_id "
				+ " from amuse_r_activity_comment a left join amuse_r_activity_comment b on a.reply_id=b.id "
				+ " left join activity_comment_praise c on c.comment_id = a.id and c.is_valid=1 "
				+ " left join user_t_info d on a.user_id = d.id and d.is_valid = 1 "
				+ " left join activity_over_activity_module_info e on e.over_activity_id = a.amuse_id "
				+ " left join activity_over_activity_module f on f.id = e.module_id and f.is_valid = 1 "
				+ " left join user_t_gag gag on a.user_id=gag.user_id where a.amuse_id=" + amuseId
				+ " and a.is_valid=1 ";

		String countSql = "select count(1) from ("
				+ "select a.id from amuse_r_activity_comment a left join  user_t_info d on a.user_id = d.id "
				+ "left join activity_over_activity_module_info e on e.over_activity_id = a.amuse_id "
				+ "left join activity_over_activity_module f on f.id = e.module_id where  a.is_valid=1 and a.amuse_id="
				+ amuseId;
		if (StringUtils.isNotBlank(addSql)) {
			querySql = querySql + addSql + orderSql + limitSql;
			countSql = countSql + addSql + " group by a.id,f.id)aaa";
		} else {
			querySql = querySql + orderSql + limitSql;
			countSql = countSql + " group by a.id,f.id)aaa";
		}
		List<Map<String, Object>> queryMap = queryDao.queryMap(querySql);
		if (CollectionUtils.isNotEmpty(queryMap)) {
			vo.setList(queryMap);
		}
		Number totalCount = queryDao.query(countSql);
		vo.setTotal(totalCount.longValue());
		return vo;
	}

	/**
	 * 禁言校验
	 * 
	 * @param userId
	 * @return
	 */
	public String gagCheck(Long userId) {
		Map<String, Object> map = queryDao
				.querySingleMap("select days,create_date from user_t_gag where user_id=" + userId);
		if (map == null || map.isEmpty()) {
			return null;
		} else {
			int days = ((Number) map.get("days")).intValue();
			if (days == -1) {
				return "你已被禁言";
			}
			Date freeDate = DateUtils.addDays(new Date(((Timestamp) map.get("create_date")).getTime()), days);
			Date now = new Date();
			if (freeDate.getTime() > now.getTime()) {
				int leftdays = (int) Math.ceil((freeDate.getTime() - now.getTime()) / 1000 / 3600 / 24d);
				if (leftdays <= 1) {
					return "你已被禁言" + days + "天，明日可用";
				} else {
					return "你已被禁言" + days + "天，还剩" + leftdays + "天";
				}
			}
		}
		return null;
	}

	public List<Map<String, Object>> quickComment() {
		return queryDao.queryMap("select id,img,comment from comment_shortcut where is_valid=1 order by sort_num");
	}

	/**
	 * 查询直播相关的评论信息
	 */
	public PageVO queryLiveComments(Map<String, Object> params) {
		PageVO vo = new PageVO();
		int commentType = NumberUtils.toInt(params.get("commentType").toString());
		String querySql = "";
		if (commentType == 1) {
			// 查看直播
			querySql = " select                                                                                                        "
					+ "   aac.id,                                                                                                     "
					+ "   aac.img,                                                                                                    "
					+ "   aac.content,                                                                                                "
					+ "   aac.create_date,                                                                                            "
					+ "   aac.type contentType,                                                                                       "
					+ "   aac.virtual_like_count virtualLikeCount,                                                                    "
					+ "   preaac.content preContent,                                                                                  "
					+ "   lol.user_id luId,                                                                                            "
					+ "   lol.room_id roomId,                                                                                          "
					+ "   lui.nickname luNickname,                                                                                    "
					+ "   ui.nickname commentUserNickname                                                                             "
					+ " from                                                                                                          "
					+ "   amuse_r_activity_comment aac left join amuse_r_activity_comment preaac on aac.reply_id = preaac.id          "
					+ "   left join live_on_live lol on aac.amuse_id  = lol.id                                                          "
					+ "   left join user_t_info lui on lui.id  = lol.user_id                                                           "
					+ "   left join user_t_info ui on aac.user_id = ui.id                                                             "
					+ "   where  aac.is_valid = 1 and aac.type = 7 and ui.is_valid = 1 and lol.is_valid = 1 and lui.is_valid = 1 ";
		} else {
			// 查看历史视频 "
			querySql = "   select                                                                                                     "
					+ "   aac.id,                                                                                                    "
					+ "   aac.img,                                                                                                   "
					+ "   aac.content,                                                                                               "
					+ "   aac.create_date,                                                                                           "
					+ "   aac.type contentType,                                                                                      "
					+ "   aac.virtual_like_count virtualLikeCount,                                                                   "
					+ "   preaac.content preContent,                                                                                 "
					+ "   lu.user_id luId,                                                                                           "
					+ "   lu.room_id roomId,                                                                                         "
					+ "   lui.nickname luNickname,                                                                                   "
					+ "   ui.nickname commentUserNickname,                                                                           "
					+ "   lv.title                                                                                                   "
					+ " from                                                                                                         "
					+ "   amuse_r_activity_comment aac left join amuse_r_activity_comment preaac on aac.reply_id = preaac.id         "
					+ "   left join live_up lu on aac.amuse_id  = lu.user_id                                                         "
					+ "   left join user_t_info lui on lui.id  = lu.user_id                                                          "
					+ "   left join user_t_info ui on aac.user_id = ui.id                                                            "
					+ "   left join live_video lv on aac.amuse_id = lv.id                                                            "
					+ "   where  aac.is_valid = 1 and aac.type = 8 and ui.is_valid = 1";
		}
		String whereSql = "";
		Object queryTypeObj = params.get("queryType");
		int queryType = NumberUtils.toInt(queryTypeObj == null ? null : queryTypeObj.toString(), -1);
		String content = params.get("content") == null ? "" : params.get("content").toString();
		if (StringUtils.isNotBlank(content)) {
			content = StringUtils.trim(content);
			if (queryType == 0) {
				whereSql += " and ui.nickname ='" + content + "'	 ";
			} else if (queryType == 1) {
				whereSql += " and lui.nickname='" + content + "'	 ";
			} else if (queryType == 2) {
				if (commentType == 1) {
					whereSql += " and lol.user_id=" + content;
				} else {
					whereSql += " and lu.user_id=" + content;
				}
			} else if (queryType == 4) {
				if (params.containsKey("content")) {
					whereSql += " and aac.content like '%" + content + "%'	 ";
				}
			} else if (queryType == 3) {
				if (commentType == 1) {
					whereSql += " and lol.room_id ='" + content + "'";
				} else {
					whereSql += " and lv.title like '%" + content + "%'	 ";
				}

			}
		}

		String orderSql = " order by aac.create_date desc ";

		if (params.containsKey("startDate") && params.containsKey("endDate")) {
			whereSql = whereSql + "	and aac.create_date>='" + params.get("startDate") + "'	"
					+ "	and aac.create_date<'" + params.get("endDate") + " 23:59:59' 	";
		}

		int page = NumberUtils.toInt(params.get("page").toString());
		Number totalCount = queryDao.query("select count(1) from (" + querySql + ") a");

		if (totalCount == null || totalCount.intValue() <= 0) {
			vo.setTotal(0);
			vo.setCurrentPage(page);
			vo.setIsLast(1);
			return vo;
		}

		int start = PageUtils.calcStart(page);
		String limitSql = "limit " + start + "," + PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		querySql = "select  a.*, count(acp.id) likeCount from (" + querySql + whereSql + orderSql + limitSql
				+ " ) a  left join activity_comment_praise acp     on acp.comment_id = a.id     group by a.id";
		List<Map<String, Object>> queryMap = queryDao.queryMap(querySql);
		if (CollectionUtils.isNotEmpty(queryMap)) {
			vo.setList(queryMap);
		}
		vo.setTotal(totalCount.longValue());
		vo.setCurrentPage(page);
		vo.setIsLast(PageUtils.isBottom(page, totalCount.longValue()));
		return vo;
	}
}
