package com.miqtech.master.service.system;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;
import com.miqtech.master.dao.QueryDao;

/**
 * 市辖区和全城信息映射
 */
@Component
public class SystemAreaMappingService {
	@Autowired
	private QueryDao queryDao;

	private static Map<String, String> mappingData = Maps.newHashMap();

	/**
	 * 查找所有映射数据
	 */
	public Map<String, String> queryAllMappingData() {
		if (MapUtils.isNotEmpty(mappingData)) {
			return mappingData;
		}
		String sql = "select s_code,t_code from sys_t_area_code_map ";
		List<Map<String, Object>> queryMap = queryDao.queryMap(sql);
		Map<String, String> result = Maps.newHashMap();
		if (CollectionUtils.isNotEmpty(queryMap)) {
			for (Map<String, Object> map : queryMap) {
				result.put((String) map.get("s_code"), (String) map.get("t_code"));
			}
			mappingData = result;
			return result;
		}
		return result;
	}

	public String getTargetCityCode(String sourceCode) {
		if (StringUtils.isBlank(sourceCode)) {
			return null;
		}
		String result = this.queryAllMappingData().get(sourceCode);
		if (result == null) {
			result = sourceCode;
		}
		return result;
	}
}