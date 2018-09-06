package com.miqtech.master.service.audition;

import com.miqtech.master.dao.QueryDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 *
 * @author shilina
 * @create 2017年10月25日
 */
@Service
public class AuditionService {
	@Autowired
	private QueryDao queryDao;

	public List<Map<String, Object>> queryInfoForAppRecommend(String startDate, String endDate) {
		String sql = "select id,name title from audition where is_release=1 and is_show=1 and is_valid=1 and create_date>'"
				+ startDate + "' and create_date <'" + endDate + "' order by create_date desc";
		return queryDao.queryMap(sql);
	}
}
