package com.miqtech.master.service.cohere;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.cohere.CohereDebrisHistoryDao;
import com.miqtech.master.entity.cohere.CohereDebrisHistory;
import com.miqtech.master.vo.PageVO;

@Component
public class CohereDebrisHistoryService {
	@Autowired
	private CohereDebrisHistoryDao cohereDebrisHistoryDao;
	@Autowired
	private QueryDao queryDao;

	public CohereDebrisHistory saveOrUpdate(CohereDebrisHistory cohereDebrisHistory) {
		return cohereDebrisHistoryDao.save(cohereDebrisHistory);
	}

	/**
	 * 我的碎片级记录分页，每页10条
	 *
	 * @param userId
	 * @param page
	 * @return
	 */
	public PageVO getDebrisHistory(Long activityId, Long userId, Integer page, Integer pageSize) {
		String totalSql = "SELECT count(*) FROM ( SELECT a.id, a.user_id, b.title, '-1' AS is_get, '' AS prizeName, a.create_date, '-2' AS prizeHistoryId, a.in_type AS type, '' AS state FROM cohere_debris_history a "
				+ " LEFT JOIN cohere_debris b ON a.debris_id = b.id  WHERE a.user_id = " + userId
				+ " AND a.is_valid = 1 AND b.activity_id = " + activityId + " AND b.is_valid = 1 "
				+ " UNION SELECT a.id, a.user_id, GROUP_CONCAT(d.title), b.is_get, c. NAME AS prizeName, a.create_date, b.id prizeHistoryId, CASE a.out_type WHEN 1 THEN '5' END AS type, b.state FROM cohere_debris_history a LEFT JOIN cohere_prize_history b ON a.out_id = b.id  LEFT JOIN cohere_prize c ON b.prize_id = c.id LEFT JOIN cohere_debris d ON a.debris_id = d.id WHERE d.activity_id = "
				+ activityId + " AND a.out_type =1 AND a.user_id = " + userId
				+ " AND b.is_valid = 1 AND c.is_valid = 1 AND d.is_valid = 1 GROUP BY a.user_id, a.out_id "
				+ " UNION SELECT a.id, a.user_id, b.title, '-2' AS is_get, '' AS prizeName, a.create_date, '-2' AS prizeHistoryId, CASE a.out_type WHEN 2 THEN '6' END AS type, '' AS state FROM cohere_debris_history a LEFT JOIN cohere_debris b ON a.debris_id = b.id WHERE b.activity_id = "
				+ activityId + " AND a.out_type =2 AND a.user_id = " + userId
				+ "   AND a.is_valid = 1 AND b.is_valid = 1 GROUP BY a.user_id,a.id ) aa ";
		Number totalCount = queryDao.query(totalSql);
		if (null != totalCount && totalCount.intValue() > 0) {
			int start = (page - 1) * pageSize;
			String sql = "SELECT * FROM ( SELECT a.id, a.user_id, b.title, '-1' AS is_get, '' AS prizeName, a.create_date, '-2' AS prizeHistoryId, a.in_type AS type, '' AS state FROM cohere_debris_history a "
					+ " LEFT JOIN cohere_debris b ON a.debris_id = b.id  WHERE a.user_id = " + userId
					+ " AND a.is_valid = 1 AND b.activity_id = " + activityId + " AND b.is_valid = 1 "
					+ " UNION SELECT a.id, a.user_id, GROUP_CONCAT(d.title), b.is_get, c. NAME AS prizeName, a.update_date, if(b.is_get=1,b.id,'-2') as prizeHistoryId, CASE a.out_type WHEN 1 THEN '5' END AS type, b.state FROM cohere_debris_history a LEFT JOIN cohere_prize_history b ON a.out_id = b.id  LEFT JOIN cohere_prize c ON b.prize_id = c.id AND c.is_valid = 1 LEFT JOIN cohere_debris d ON a.debris_id = d.id WHERE d.activity_id = "
					+ activityId + " AND a.out_type =1 AND a.out_id IS NOT NULL AND a.user_id = " + userId
					+ " AND b.is_valid = 1 AND d.is_valid = 1 GROUP BY a.user_id, a.out_id "
					+ " UNION SELECT a.id, a.user_id, b.title, '-2' AS is_get, '' AS prizeName, a.update_date, '-2' AS prizeHistoryId, CASE a.out_type WHEN 2 THEN '6' END AS type, '' AS state FROM cohere_debris_history a LEFT JOIN cohere_debris b ON a.debris_id = b.id WHERE b.activity_id = "
					+ activityId + " AND a.out_type =2 AND a.user_id = " + userId
					+ "   AND a.is_valid = 1 AND b.is_valid = 1 GROUP BY a.user_id,a.id ) aa ORDER BY aa.create_date desc "
					+ " limit " + start + " ," + pageSize;
			PageVO vo = new PageVO(queryDao.queryMap(sql));
			if (page * pageSize >= totalCount.intValue()) {
				vo.setIsLast(1);
			}
			vo.setTotal(totalCount.longValue());
			return vo;
		} else {
			PageVO vo = new PageVO(new ArrayList<Map<String, Object>>());
			return vo;
		}
	}

	public CohereDebrisHistory findByValidAndUserIdAndDrawIdAndIsUsed(Integer isUsed, Long userId, Long drawId) {
		return cohereDebrisHistoryDao.findByValidAndUserIdAndDrawIdAndIsUsed(1, userId, drawId, isUsed);
	}

	public CohereDebrisHistory getPepareCohereDebris(Long userId, Long drawId) {
		return cohereDebrisHistoryDao.findByIsUsedAndOutTypeAndValidAndUserIdAndDrawIdAndOutIdIsNull(0, 1, 1, userId,
				drawId);
	}

	public List<Map<String, Object>> getDebrisHistoryByUrlcode(String urlCode) {
		String idSql = " select cdh.create_date,cdh.user_id from  cohere_debris_history cdh where cdh.is_valid = 1   and cdh.url_code='"
				+ urlCode + "' order by cdh.create_date desc limit 5";
		List<Map<String, Object>> queryMap = queryDao.queryMap(idSql);
		if (CollectionUtils.isNotEmpty(queryMap)) {
			for (Map<String, Object> map : queryMap) {
				String sql = "select headimg,  nickname from mp_t_user where user_id  = " + map.get("user_id")
						+ " and is_valid = 1 order by create_date desc limit 1";
				Map<String, Object> mpUserInfo = queryDao.querySingleMap(sql);
				if (MapUtils.isNotEmpty(mpUserInfo)) {
					map.put("headimg", mpUserInfo.get("headimg"));
					map.put("nickname", mpUserInfo.get("nickname"));
				}
			}
		}
		return queryMap;
	}

	/**
	 * 获取用户分享的碎片信息
	 *
	 * @param drawId
	 *            碎片记录id
	 * @param sharedUserId
	 *            分享人用户id
	 * @return
	 */
	public Map<String, Object> getSharedDebris(Long drawId, Long sharedUserId) {
		String sql = " select cdh.id,ca.title,cp.url_crosswise, cp.counts ,cp.name ,cp.url_vertical,ui.nickname,cd.id debrisId,cd.num debrisNum,cd.title debrisName ,cd.url debrisUrl ,	case when (cdh.out_id is null or   cdh.out_id =-1) then  0  else 1 end status "
				+ " from cohere_debris_history cdh "
				+ " left join cohere_debris cd on cd.id = cdh.debris_id and cd.is_valid=1"
				+ " left join cohere_activity ca on ca.id = cd.activity_id and ca.is_valid = 1"
				+ " left join cohere_prize cp  on cp.activity_id = ca.id and cp.num=1 and cp.is_valid=1"
				+ " left join user_t_info ui on ui.id = cdh.user_id where cdh.user_id = " + sharedUserId.toString()
				+ " and  cdh.draw_id =" + drawId + " and cdh.out_type = 2  and ui.id = " + sharedUserId.toString()
				+ " order by cdh.create_date desc  limit 1";
		return queryDao.querySingleMap(sql);
	}

	public CohereDebrisHistory findById(Long cdhId) {
		return cohereDebrisHistoryDao.findOne(cdhId);

	}

}
