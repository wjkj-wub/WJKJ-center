package com.miqtech.master.service.amuse;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.amuse.AmuseMsgFeedbackDao;
import com.miqtech.master.entity.amuse.AmuseMsgFeedback;
import com.miqtech.master.utils.BeanUtils;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.utils.StringUtils;
import com.miqtech.master.vo.PageVO;

/**
 * 娱乐赛反馈消息模版service
 */
@Component
public class AmuseMsgFeedbackService {

	@Autowired
	private QueryDao queryDao;
	@Autowired
	private AmuseMsgFeedbackDao amuseMsgFeedbackDao;

	public AmuseMsgFeedback findById(Long id) {
		return amuseMsgFeedbackDao.findOne(id);
	}

	/**
	 * 查询某类型的有效数据
	 */
	public List<AmuseMsgFeedback> findValidByType(Integer type) {
		return amuseMsgFeedbackDao.findByTypeAndValid(type, CommonConstant.INT_BOOLEAN_TRUE);
	}

	public AmuseMsgFeedback saveOrUpdate(AmuseMsgFeedback feedback) {
		if (null != feedback) {
			Date now = new Date();
			feedback.setUpdateDate(now);
			if (null != feedback.getId()) {
				AmuseMsgFeedback old = findById(feedback.getId());
				if (null != old) {
					feedback = BeanUtils.updateBean(old, feedback);
				}
			} else {
				if (null == feedback.getValid()) {
					feedback.setValid(CommonConstant.INT_BOOLEAN_TRUE);
				}
				feedback.setCreateDate(now);
			}
			return amuseMsgFeedbackDao.save(feedback);
		}
		return null;
	}

	public void delete(Long id) {
		AmuseMsgFeedback f = new AmuseMsgFeedback();
		f.setId(id);
		f.setValid(CommonConstant.INT_BOOLEAN_FALSE);
		saveOrUpdate(f);
	}

	/**
	 * 分页
	 */
	public PageVO page(int page, Map<String, Object> searchParams) {
		String sqlCondition = " WHERE is_valid = 1";
		String totalCondition = sqlCondition;
		Map<String, Object> params = Maps.newHashMap();

		String content = MapUtils.getString(searchParams, "content");
		if (StringUtils.isNotBlank(content)) {
			String likeContent = "%" + content + "%";
			sqlCondition = SqlJoiner.join(sqlCondition, " AND content LIKE :likeContent");
			params.put("likeContent", likeContent);
			totalCondition = SqlJoiner.join(totalCondition, " AND content LIKE '", likeContent, "'");
		}
		Boolean isAppSetting = MapUtils.getBoolean(searchParams, "isAppSetting");
		if (isAppSetting) {
			sqlCondition = SqlJoiner.join(sqlCondition, " AND type = 4");
			totalCondition = SqlJoiner.join(totalCondition, " AND type = 4");
		} else {
			sqlCondition = SqlJoiner.join(sqlCondition, " AND type != 4");
			totalCondition = SqlJoiner.join(totalCondition, " AND type != 4");
		}

		String limit = "";
		Integer pageSize = PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		if (page > 0) {
			Integer startRow = (page - 1) * pageSize;
			limit = SqlJoiner.join(" LIMIT ", startRow.toString(), ", ", pageSize.toString());
		}

		String sql = SqlJoiner.join(
				"SELECT id, type, content, is_valid valid, create_date createDate FROM amuse_msg_feedback",
				sqlCondition, " ORDER by create_date DESC", limit);
		List<Map<String, Object>> list = queryDao.queryMap(sql, params);

		String totalSql = SqlJoiner.join("SELECT COUNT(1) FROM amuse_msg_feedback", totalCondition);
		Number total = queryDao.query(totalSql);
		if (total == null) {
			total = 0;
		}

		PageVO vo = new PageVO();
		vo.setList(list);
		vo.setTotal(total.intValue());
		int isLast = total.intValue() > page * pageSize ? 1 : 0;
		vo.setIsLast(isLast);
		return vo;
	}
}
