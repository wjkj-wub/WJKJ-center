package com.miqtech.master.service.netbar;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.netbar.NetbarEvaluationDao;
import com.miqtech.master.dao.netbar.NetbarEvaluationPraiseDao;
import com.miqtech.master.entity.netbar.NetbarEvaluation;
import com.miqtech.master.entity.netbar.NetbarEvaluationPraise;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.vo.PageVO;

@Component
public class NetbarEvaluationService {
	@Autowired
	private NetbarEvaluationDao netbarEvaluationDao;
	@Autowired
	private NetbarEvaluationPraiseDao netbarEvaluationPraiseDao;
	@Autowired
	private QueryDao queryDao;

	public NetbarEvaluation save(NetbarEvaluation eva) {
		return netbarEvaluationDao.save(eva);
	}

	/**
	 *
	 * 用户点击有用操作,评论有用数+1
	 * @param evaIdLong
	 * @param evaIdLong2
	 * @return
	 */
	public int praiseEva(Long userId, Long evaIdLong, long orderId) {
		NetbarEvaluation eva = netbarEvaluationDao.findOne(evaIdLong);
		Integer praised = eva.getPraised();
		praised = praised == null ? 0 : praised;
		eva.setPraised(praised + 1);
		eva = netbarEvaluationDao.save(eva);

		NetbarEvaluationPraise praise = new NetbarEvaluationPraise();
		praise.setCreateDate(new Date());
		praise.setEvaId(eva.getId());
		praise.setUserId(userId);
		praise.setValid(1);
		praise.setOrderId(orderId);
		netbarEvaluationPraiseDao.save(praise);
		return praised;
	}

	public PageVO findNetbarEvaluationList(Long userId, Long netbarId, int page, int pageSize, int commentsCount) {
		PageVO vo = new PageVO();
		int start = (page - 1) * pageSize;
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("netbarId", netbarId);
		String sql = "select count(eva.id) total from netbar_t_evaluation eva  where  ((content is not null and content <>'') or  eva.id in (select distinct eva_id from netbar_r_evaluation_imgs ) ) and eva.netbar_id = :netbarId ";
		Map<String, Object> total = queryDao.querySingleMap(sql, params);
		Number totalCount = (Number) total.get("total");
		if (page * pageSize >= totalCount.intValue()) {
			vo.setIsLast(1);
		}
		int limitStart;
		if (commentsCount == 0) {
			limitStart = (page - 1) * pageSize;
		} else {
			limitStart = ((page - 1) * pageSize + (totalCount.intValue() - commentsCount));
		}
		params.put("start", limitStart);
		String praiseSql = StringUtils.EMPTY;
		String columnSql = StringUtils.EMPTY;

		if (userId > 0) {
			praiseSql = " left join netbar_t_evaluation_praise p         on p.eva_id = eva.id        and p.user_id = :userId ";
			columnSql = " ,case        when count(p.id) > 0        then 1        else 0    end isPraised ";
			params.put("userId", userId);
		}

		if (totalCount.intValue() > 0) {
			sql = SqlJoiner
					.join("select    eva.user_id ,eva.id, eva.praised, eva.content,  eva.is_anonymous, ui.nickname, ui.icon, round(        (            enviroment + equipment + network + service        ) / 4, 1    ) avgScore, eva.create_date ,group_concat(img.url order by img.create_date ) as imgs  ",
							columnSql,
							"  from    netbar_t_evaluation eva    left join user_t_info ui on eva.user_id = ui.id left join netbar_r_evaluation_imgs img on img.eva_id = eva.id  ",
							praiseSql,
							" where       ((content is not null and content <>'') or eva.id in (select distinct eva_id from netbar_r_evaluation_imgs ) )",
							"   and eva.netbar_id = :netbarId group by eva.id order by eva.create_date desc  limit :start,:pageSize");
			params.put("start", start);
			params.put("pageSize", pageSize);
			result = queryDao.queryMap(sql, params);
			vo.setList(result);
		}
		return vo;
	}

	public boolean isExist(long orderId, Long userId) {
		NetbarEvaluation eva = netbarEvaluationDao.findByOrderIdAndUserIdAndValid(orderId, userId, 1);
		return eva != null;
	}
}
