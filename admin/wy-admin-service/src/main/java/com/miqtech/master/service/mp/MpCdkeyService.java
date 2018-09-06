package com.miqtech.master.service.mp;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.dao.mp.MpCdkeyDao;
import com.miqtech.master.entity.mp.MpCdkey;

@Component
public class MpCdkeyService {

	@Autowired
	private MpCdkeyDao mpCdkeyDao;

	public MpCdkey findValidByOpenIdAndCategoryId(String openId, Long categoryId) {
		if (StringUtils.isBlank(openId) || categoryId == null) {
			return null;
		}

		List<MpCdkey> cdkeys = mpCdkeyDao.findByOpenIdAndCategoryIdAndValid(openId, categoryId,
				CommonConstant.INT_BOOLEAN_TRUE);
		if (CollectionUtils.isEmpty(cdkeys)) {
			return null;
		}

		return cdkeys.get(0);
	}

	public MpCdkey save(MpCdkey cdkey) {
		return mpCdkeyDao.save(cdkey);
	}

}
