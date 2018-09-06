package com.miqtech.master.service.cohere;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.cohere.CoherePrizeDao;
import com.miqtech.master.entity.cohere.CoherePrize;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.vo.PageVO;

@Service
public class CoherePrizeService {
	@Autowired
	private CoherePrizeDao coherePrizeDao;
	@Autowired
	private QueryDao queryDao;
	@Autowired
	private CohereDebrisService cohereDebrisService;

	public List<CoherePrize> getCoherePrizeByActivityId(Long activityId) {
		return coherePrizeDao.findByActivityIdAndValid(activityId, 1);
	}

	/**
	 * 得到除奖品一的所有奖品
	 *
	 * @param activityId
	 * @return
	 */
	public List<Map<String, Object>> getCoherePrizeByActivityIdExp(Long activityId) {
		String sql = "select * from cohere_prize where is_valid=1 and activity_id=" + activityId
				+ " and num not in (1)";
		return queryDao.queryMap(sql);
	}

	/**
	 * 获取大奖信息
	 */
	public List<Map<String, Object>> getCohereFirstPrizeByActivityId(Long activityId) {
		String sql = "select a.num,if(a.counts<0,'∞',a.counts) as counts,count(distinct b.id) prizeCount,a.name,a.url_crosswise,a.url_vertical from cohere_prize a left join cohere_prize_history b on a.id=b.prize_id and b.is_valid=1 and b.is_get=1 "
				+ " where a.is_valid=1  and a.activity_id=" + activityId + " and a.num=1";
		return queryDao.queryMap(sql);
	}

	/**
	 * 得到活动下的所有碎片
	 *
	 * @param activityId
	 * @return
	 */
	public List<Map<String, Object>> getSomeByActivityId(Long activityId) {
		String sql = "select id,url_vertical,name,probability,if(isnull(counts),0,counts) counts,if(isnull(value),0,value) value,type from cohere_prize where activity_id="
				+ activityId;
		return queryDao.queryMap(sql);
	}

	/**
	 * 保存奖品信息
	 *
	 * @param coherePrizes
	 * @return
	 */
	public CoherePrize saveOrUpdate(CoherePrize coherePrize) {
		return coherePrizeDao.save(coherePrize);
	}

	/**
	 * 批量保存奖品信息
	 *
	 * @param coherePrizes
	 * @return
	 */
	public List<CoherePrize> saveOrUpdate(List<CoherePrize> coherePrizes, Long activityId) {
		List<CoherePrize> list = new ArrayList<>();
		for (CoherePrize coherePrize : coherePrizes) {
			list.add(coherePrizeDao.save(coherePrize));
		}
		cohereDebrisService.probabilityCohere(activityId, true); // 设置融合概率
		return list;
	}

	public CoherePrize findById(Long id) {
		return coherePrizeDao.findOne(id);
	}

	public boolean decreaseNum(Long id) {
		String sql = "update cohere_prize set num = num-1 where id=" + id;
		queryDao.update(sql);
		return true;
	}

	/**
	 * 预先保存该活动下的所有奖品
	 *
	 * @param activityId
	 * @param num
	 * @return
	 */
	public List<CoherePrize> saveAuto(Long activityId, Integer num) {
		CoherePrize coherePrize = null;
		List<CoherePrize> list = new ArrayList<>();
		for (int i = 0; i < num; i++) {
			coherePrize = new CoherePrize();
			coherePrize.setActivityId(activityId);
			coherePrize.setCreateDate(new Date());
			coherePrize.setValid(1);
			coherePrize.setNum(i + 1);
			saveOrUpdate(coherePrize);
			list.add(coherePrize);
		}
		return list;
	}

	/**
	 * 返回已发奖品数量
	 *
	 * @param activityId
	 * @return
	 */
	public Integer sendPrizeCount(Long prizeId) {
		String sql = "select count(1) from cohere_prize_history where prize_id=" + prizeId
				+ " and is_get=1 and is_valid=1";
		Number num = queryDao.query(sql);
		return num.intValue();
	}

	/**
	 * 返回该活动下的所有奖品信息
	 *
	 */
	public PageVO searchInfo(Map<String, Object> params) {
		String limitSql = PageUtils.getLimitSql(NumberUtils.toInt(params.get("page").toString()));
		String searchSql = "";
		if (StringUtils.isNotBlank(params.get("prizeType").toString())) {
			searchSql = SqlJoiner.join(searchSql, " and cp.num=" + params.get("prizeType").toString());
		}
		if (StringUtils.isNotBlank(params.get("prizeState").toString())) {
			if (params.get("prizeState").toString().equals("1")) {
				searchSql = SqlJoiner.join(searchSql, " and cph.state !=3");
			} else if (params.get("prizeState").toString().equals("2")) {
				searchSql = SqlJoiner.join(searchSql, " and cph.state=3");
			} else if (params.get("prizeState").toString().equals("3")) {
				searchSql = SqlJoiner.join(searchSql, " and cph.question=1");
			}
		}
		if (StringUtils.isNotBlank(params.get("searchUser").toString())) {
			searchSql = SqlJoiner.join(searchSql,
					" and uti.nickname like '%" + params.get("searchUser").toString() + "%'");
		}
		if (StringUtils.isNotBlank(params.get("startTime").toString())) {
			searchSql = SqlJoiner.join(searchSql, " and cph.create_date>'" + params.get("startTime").toString() + "'");
		}
		if (StringUtils.isNotBlank(params.get("endTime").toString())) {
			searchSql = SqlJoiner.join(searchSql, " and cph.create_date<'" + params.get("endTime").toString() + "'");
		}

		String sql = "select cph.id,cp.name prizeName,cph.user_id,cph.account,cph.create_date,cph.server_name serveName,cph.game_name gameName,cph.is_get,cph.state,uti.create_date registerTime,uti.telephone,uti.nickname,if(isnull(cph.question),0,question) question,0 btState from cohere_prize_history cph join cohere_prize cp on cp.id = cph.prize_id and cp.activity_id="
				+ params.get("activityId").toString()
				+ " join user_t_info uti on uti.id=cph.user_id where cph.is_valid=1 and cp.num not in (1) " + searchSql
				+ limitSql;
		List<Map<String, Object>> lists = queryDao.queryMap(sql);
		if (lists != null) {
			String state = "";
			String question = "";
			for (Map<String, Object> list : lists) {
				state = list.get("state").toString();
				question = list.get("question").toString();
				if (state.equals("3") || state.equals("2")) {
					if (state.equals("3")) {
						list.put("btState", 0);
					} else {
						list.put("btState", 1);
					}
				} else if (state.equals("4")) {
					list.put("btState", 4);
				} else {
					if (question.equals("0")) {
						list.put("btState", 2);
					} else {
						list.put("btState", 3);
					}
				}
			}
		}
		String sqlCount = "select count(1) from cohere_prize_history cph left join cohere_prize cp on cp.id=cph.prize_id join user_t_info uti on uti.id=cph.user_id where cp.num not in (1) and cp.activity_id="
				+ params.get("activityId").toString() + searchSql;
		Number num = queryDao.query(sqlCount);
		PageVO pageVO = new PageVO();
		pageVO.setCurrentPage(NumberUtils.toInt(params.get("page").toString()));
		pageVO.setList(lists);
		pageVO.setTotal(num.longValue());
		pageVO.setIsLast(lists.size() < NumberUtils.toInt(params.get("page").toString()) ? 1 : 0);
		return pageVO;
	}

	public List<Map<String, Object>> export(Map<String, Object> params, String prizeHistoryIds, Integer type,
			Long activityId) {
		String searchSql = "";
		if (StringUtils.isNotBlank(params.get("prizeType").toString())) {
			searchSql = SqlJoiner.join(searchSql, " and cp.num=" + params.get("prizeType").toString());
		}
		if (StringUtils.isNotBlank(params.get("prizeState").toString())) {
			if (params.get("prizeState").toString().equals("1")) {
				searchSql = SqlJoiner.join(searchSql, " and cph.state !=3");
			} else if (params.get("prizeState").toString().equals("2")) {
				searchSql = SqlJoiner.join(searchSql, " and cph.state=3");
			} else if (params.get("prizeState").toString().equals("3")) {
				searchSql = SqlJoiner.join(searchSql, " and cph.question=1");
			}
		}
		if (StringUtils.isNotBlank(params.get("searchUser").toString())) {
			searchSql = SqlJoiner.join(searchSql, " and uti.nickname=" + params.get("searchUser").toString());
		}
		if (StringUtils.isNotBlank(params.get("startTime").toString())) {
			searchSql = SqlJoiner.join(searchSql, " and cph.create_date>'" + params.get("startTime").toString() + "'");
		}
		if (StringUtils.isNotBlank(params.get("endTime").toString())) {
			searchSql = SqlJoiner.join(searchSql, " and cph.create_date<'" + params.get("endTime").toString() + "'");
		}
		String sql = "select cph.id,cp.name prizeName,cph.user_id,cph.account,cph.create_date,cph.server_name,cph.game_name,cph.is_get,cph.state,uti.create_date registerTime,uti.telephone,uti.nickname,if(isnull(cph.question),0,question) question,0 btState from cohere_prize_history cph join cohere_prize cp on cp.id = cph.prize_id and cp.activity_id="
				+ activityId + " join user_t_info uti on uti.id=cph.user_id where cph.is_valid=1 " + searchSql;
		if (type == 0) {
			List<Map<String, Object>> lists = queryDao.queryMap(sql);
			return lists;
		} else {
			sql += " and cph.id in (" + prizeHistoryIds + ")";
			List<Map<String, Object>> lists = queryDao.queryMap(sql);
			return lists;
		}
	}
}
