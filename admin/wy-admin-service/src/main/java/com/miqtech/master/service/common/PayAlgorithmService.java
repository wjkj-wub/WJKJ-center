package com.miqtech.master.service.common;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

public class PayAlgorithmService {
	public static final String WY_API_PAY_ALGORITHM = "api_pay_algorithm";

	private String defaultAlgorithm = "1";//1 先打折在减红包金额  2先减去红包金额再打折
	@Autowired
	StringRedisOperateService stringRedisOperateService;

	public String getAlgorithm() {
		String data = stringRedisOperateService.getData(WY_API_PAY_ALGORITHM);
		if (StringUtils.isBlank(data)) {
			data = defaultAlgorithm;
			stringRedisOperateService.setData(WY_API_PAY_ALGORITHM, data);
		}
		return data;
	}

	public String getDefaultAlgorithm() {
		return defaultAlgorithm;
	}

	public void setDefaultAlgorithm(String defaultAlgorithm) {
		this.defaultAlgorithm = defaultAlgorithm;
	}

}
