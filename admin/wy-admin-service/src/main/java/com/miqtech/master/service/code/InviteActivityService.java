package com.miqtech.master.service.code;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.code.InviteActivityDao;
import com.miqtech.master.entity.code.InviteActivity;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.vo.PageVO;

@Component
public class InviteActivityService {
	@Autowired
	private InviteActivityDao inviteActivityDao;
	@Autowired
	private QueryDao queryDao;

	public InviteActivity findById(Long id) {
		return inviteActivityDao.findOne(id);
	}

	public void save(InviteActivity inviteActivity) {
		inviteActivityDao.save(inviteActivity);
	}

	public void del(Long id) {
		inviteActivityDao.delete(id);
	}

	public PageVO queryList(String name, String area, String code, Integer type, Integer page) {
		String sql = "";
		String nameSql = "";
		String areaSql = "";
		String codeSql = "";
		String typeSql = "";
		if (StringUtils.isNotBlank(name)) {
			nameSql = " and a.name like '%" + name + "%'";
		}
		if (StringUtils.isNotBlank(area)) {
			areaSql = " and locate('," + area + "',a.area)<>0";
		}
		if (StringUtils.isNotBlank(code)) {
			codeSql = " and locate('" + code + "',a.code)<>0";
		}
		if (type != null) {
			if (type == 1) {
				typeSql = " and date_format(now(), '%Y-%m-%d')>=date_format(a.start_time, '%Y-%m-%d') and date_format(now(), '%Y-%m-%d')<=date_format(a.end_time, '%Y-%m-%d')";
			} else if (type == 2) {
				typeSql = " and date_format(now(), '%Y-%m-%d')<date_format(a.start_time, '%Y-%m-%d')";
			} else if (type == 3) {
				typeSql = " and date_format(now(), '%Y-%m-%d')>date_format(a.end_time, '%Y-%m-%d')";
			}
		}
		int pageSize = PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		if (page == null) {
			page = 1;
		}
		int start = (page - 1) * pageSize;
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("start", start);
		params.put("pageSize", pageSize);
		String limitSql = " limit :start,:pageSize";
		sql = SqlJoiner.join("select count(1) from  invitecode_activity a where is_valid=1", nameSql, areaSql, codeSql,
				typeSql);
		PageVO vo = new PageVO();
		Number totalCount = queryDao.query(sql);
		if (totalCount != null) {
			vo.setTotal(totalCount.intValue());
		}

		if (start + pageSize >= vo.getTotal()) {
			vo.setIsLast(1);
		}
		sql = SqlJoiner.join("select * from  invitecode_activity a where is_valid=1", nameSql, areaSql, codeSql,
				typeSql, " order by a.create_date desc", limitSql);
		vo.setCurrentPage(page);
		vo.setList(queryDao.queryMap(sql, params));
		return vo;

	}

}
