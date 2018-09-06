package com.miqtech.master.service.netbar;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.netbar.NetbarFeedbackDao;
import com.miqtech.master.entity.netbar.NetbarFeedback;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.vo.PageVO;

@Component
public class NetbarFeedbackService {
	@Autowired
	private QueryDao queryDao;
	@Autowired
	private NetbarFeedbackDao netbarFeedbackDao;

	public PageVO queryList(String phone, String content, Integer state, String date, Integer page) {
		PageVO vo = new PageVO();
		String sql = "";
		String limitSql = " limit :start,:pageSize";
		String phoneSql = "";
		String contentSql = "";
		String stateSql = "";
		String dateSql = "";
		if (StringUtils.isNotBlank(phone)) {
			phoneSql = SqlJoiner.join(" and c.owner_telephone like '", phone, "%'");
		}
		if (StringUtils.isNotBlank(content)) {
			contentSql = SqlJoiner.join(" and a.content like '%", content, "%'");
		}
		if (state != null) {
			stateSql = SqlJoiner.join(" and a.state=", String.valueOf(state));
		}
		if (StringUtils.isNotBlank(date)) {
			dateSql = SqlJoiner.join(" and date_format(a.create_date, '%Y-%m-%d')='", date, "'");
		}
		sql = SqlJoiner
				.join("select count(1) from (netbar_feedback a,netbar_t_info b,netbar_t_merchant c) where a.is_valid=1 and a.netbar_id=b.id and a.merchant_id=c.id",
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
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("start", start);
		params.put("pageSize", pageSize);
		sql = SqlJoiner
				.join("select a.id,a.img,a.create_date,a.content,c.owner_name,b.name,b.address,c.owner_telephone,a.state from (netbar_feedback a,netbar_t_info b,netbar_t_merchant c) where a.is_valid=1 and a.netbar_id=b.id and a.merchant_id=c.id",
						phoneSql, contentSql, stateSql, dateSql, limitSql);
		vo.setList(queryDao.queryMap(sql, params));
		return vo;
	}

	public Map<String, Object> queryById(Long id) {
		String sql = SqlJoiner
				.join("select a.id,a.img,a.create_date,a.content,c.owner_name,b.name,b.address,c.owner_telephone,a.state from (netbar_feedback a,netbar_t_info b,netbar_t_merchant c) where a.is_valid=1 and a.netbar_id=b.id and a.merchant_id=c.id and a.id=",
						String.valueOf(id));
		return queryDao.querySingleMap(sql);
	}

	public NetbarFeedback findById(Long id) {
		return netbarFeedbackDao.findOne(id);
	}

	public void save(NetbarFeedback netbarFeedback) {
		netbarFeedbackDao.save(netbarFeedback);
	}
}
