package com.miqtech.master.service.netbar;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.dao.QueryDao;

@Component
public class NetbarResourceAreaQuottaRaitoService {
	@Autowired
	private QueryDao queryDao;

	public List<Map<String, Object>> statisticQuottRaito(String areaCode, int level, String beginTime, String endTime) {
		String dateSql = "";
		if (StringUtils.isNotBlank(beginTime) && StringUtils.isNotBlank(endTime)) {
			dateSql = "create_date >='" + beginTime + "' and  create_date<='" + endTime + "' and ";
		}
		String ratio = null;
		if (level == 1) {
			ratio = "vip_ratio";
		} else if (level == 2) {
			ratio = "gold_ratio";
		} else if (level == 3) {
			ratio = "jewel_ratio";
		} else {
			return null;
		}
		String sql = "select " + ratio + " ratio from netbar_resource_area_quotta_raito where " + dateSql
				+ " area_code='" + areaCode + "' and is_valid in (0,1) limit 7  ";
		return queryDao.queryMap(sql);
	}

}
