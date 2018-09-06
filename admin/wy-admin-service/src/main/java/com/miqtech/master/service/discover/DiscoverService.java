package com.miqtech.master.service.discover;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.utils.AreaUtil;

@Component
public class DiscoverService {
	@Autowired
	private QueryDao queryDao;

	public Map<String, Object> discoverService(String areaCode, String longitude, String latitude) {
		Map<String, Object> result = new HashMap<String, Object>();
		if (StringUtils.isNotBlank(areaCode) && StringUtils.isNotBlank(longitude) && StringUtils.isNotBlank(latitude)) {

			double longitudeMax = NumberUtils.toDouble(longitude) + 1;
			double longitudeMin = NumberUtils.toDouble(longitude) - 1;

			double latitudeMax = NumberUtils.toDouble(latitude) + 1;

			double latitudeMin = NumberUtils.toDouble(latitude) - 1;

			areaCode = AreaUtil.getAreaCode(areaCode);
			result.put("netbar",
					queryDao.querySingleMap(
							"select a.id, a.icon, a. name netbar_name, a.price_per_hour, calc_distance ( a.longitude,"
									+ longitude + ", a.latitude, " + latitude
									+ " ) distance from ( netbar_t_info a, netbar_t_merchant b ) "
									+ " where a.is_valid = 1 and b.is_valid = 1 and a.is_release = 1 and a.id = b.netbar_id "
									+ " and calc_distance (a.longitude," + longitude + ", a.latitude, " + latitude
									+ " ) <= 10 and a.longitude>=" + longitudeMin + " and a.longitude<=" + longitudeMax
									+ " and a.latitude>=" + latitudeMin + " and a.latitude<=" + latitudeMax
									+ " and a.area_code like '" + areaCode + "%'" + " order by distance limit 1"));
		}
		return result;
	}
}
