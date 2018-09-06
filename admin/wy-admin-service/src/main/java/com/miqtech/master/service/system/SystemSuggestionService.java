package com.miqtech.master.service.system;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.common.SystemSuggestionDao;
import com.miqtech.master.entity.common.SystemSuggestion;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.vo.PageVO;

/**
 * 反馈信息service
 */
@Component
public class SystemSuggestionService {
	@Autowired
	private SystemSuggestionDao systemSuggestionDao;
	@Autowired
	private QueryDao queryDao;

	public SystemSuggestion save(SystemSuggestion suggestion) {
		return systemSuggestionDao.save(suggestion);

	};

	/**
	 * 后台管理，用户反馈列表，分页
	 */
	public List<Map<String, Object>> page(int page, Map<String, Object> params) {
		if (page < 1) {
			page = 1;
		}
		int rows = PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		int start = (page - 1) * rows;
		String sqlQuery = "select id, contact, content, create_date, is_valid from sys_t_suggestion where 1=1";
		String sqlTotal = "select count(1) from sys_t_suggestion where 1=1";
		if (null != params.get("contact")) {
			sqlQuery = SqlJoiner.join(sqlQuery, " and contact like concat('%',:contact,'%')");
			sqlTotal = SqlJoiner.join(sqlTotal, " and contact like concat('%',:contact,'%')");
		}
		if (null != params.get("content")) {
			sqlQuery = SqlJoiner.join(sqlQuery, " and content like concat('%',:content,'%')");
			sqlTotal = SqlJoiner.join(sqlTotal, " and content like concat('%',:content,'%')");
		}
		if (null != params.get("valid")) {
			sqlQuery = SqlJoiner.join(sqlQuery, " and is_valid=:valid");
			sqlTotal = SqlJoiner.join(sqlTotal, " and is_valid=:valid");
		}
		Map<String, Object> total = queryDao.querySingleMap(sqlTotal, params);

		params.put("start", start);
		params.put("rows", rows);
		sqlQuery = SqlJoiner.join(sqlQuery, " order by create_date desc", " limit :start, :rows");
		List<Map<String, Object>> list = queryDao.queryMap(sqlQuery, params);

		if (null != total && null != total.get("count(1)")) {
			params.put("total", total.get("count(1)"));
		} else {
			params.put("total", 0);
		}

		return list;
	}

	/**
	 * 处理返回消息状态
	 */
	public void changeValidById(long id, int valid) {
		if (valid != 0) {
			valid = 1;
		}
		SystemSuggestion systemSuggestion = systemSuggestionDao.findOne(id);
		if (null != systemSuggestion) {
			systemSuggestion.setValid(valid);
			systemSuggestion.setUpdateDate(new Date());
			systemSuggestionDao.save(systemSuggestion);
		}
	}

	/**
	 * 未处理消息数量
	 */
	public int getNnhandleNum() {
		String sqlCount = "select count(1) from sys_t_suggestion where is_valid=1 and state = 0";
		Number count = queryDao.query(sqlCount);
		if (null != count) {
			return count.intValue();
		}
		return 0;
	}

	public PageVO queryList(String phone, String content, Integer state, String date, Integer page) {
		PageVO vo = new PageVO();
		String sql = "";
		String phoneSql = "";
		String contentSql = "";
		String stateSql = "";
		String dateSql = "";
		Map<String, Object> params = Maps.newHashMap();
		if (StringUtils.isNotBlank(phone)) {
			String likePhone = phone + "%";
			phoneSql = SqlJoiner.join(" and (a.contact like '", likePhone, "' or b.username like '", likePhone, "')");
		}
		if (StringUtils.isNotBlank(content)) {
			contentSql = SqlJoiner.join(" and a.content like '%", content, "%'");
		}
		if (state != null) {
			stateSql = SqlJoiner.join(" and a.state=0", String.valueOf(state));
		}
		if (StringUtils.isNotBlank(date)) {
			dateSql = SqlJoiner.join(" and date_format(a.create_date, '%Y-%m-%d')='", date, "'");
		}
		sql = SqlJoiner
				.join("SELECT COUNT(1) FROM sys_t_suggestion a LEFT JOIN user_t_info b ON a.user_id = b.id WHERE a.is_valid = 1",
						phoneSql, contentSql, stateSql, dateSql);
		Number total = queryDao.query(sql);
		if (total != null) {
			vo.setTotal(total.intValue());
		}

		if (page == null) {
			page = 1;
		}
		int pageSize = PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		int start = (page - 1) * pageSize;
		String limitSql = " limit " + start + ", " + pageSize;

		sql = SqlJoiner
				.join("SELECT a.id, a.create_date, a.content, '' img, b.nickname, IFNULL(a.contact, b.username) phone, a.state, 1 type",
						" FROM sys_t_suggestion a LEFT JOIN user_t_info b ON a.user_id = b.id",
						" WHERE a.is_valid = 1", phoneSql, contentSql, stateSql, dateSql, " order by create_date desc",
						limitSql);
		List<Map<String, Object>> list = queryDao.queryMap(sql, params);
		vo.setList(list);
		return vo;
	}

	public Map<String, Object> queryDetail(Long id, int type) {
		Map<String, Object> result = null;
		String sql = "";
		if (type == 1) {
			sql = "select a.id,a.create_date,a.content,b.nickname,IFNULL(a.contact,b.username) phone,a.state, 1 type,a.remark from sys_t_suggestion a left JOIN user_t_info b on a.user_id=b.id where a.id="
					+ id;
			result = queryDao.querySingleMap(sql);
		} else if (type == 2) {
			sql = "SELECT a.id, a.create_date, a.describes content, b.img, c.nickname, c.username phone, a.state, 2 type,a.remark FROM amuse_t_appeal a LEFT JOIN ( SELECT group_concat(img) img, appeal_id FROM amuse_r_appeal_img GROUP BY appeal_id ) b ON a.id = b.appeal_id LEFT JOIN user_t_info c ON a.user_id = c.id WHERE a.id = "
					+ id;
			result = queryDao.querySingleMap(sql);
			if (result != null) {
				String imgsStr = MapUtils.getString(result, "img");
				if (StringUtils.isNotBlank(imgsStr)) {
					String[] imgs = imgsStr.split(",");
					result.put("imgs", imgs);
				}
			}
		}
		return result;
	}

	public SystemSuggestion findById(Long id) {
		return systemSuggestionDao.findOne(id);
	}
}