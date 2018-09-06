package com.miqtech.master.service.amuse;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.dao.amuse.AmuseAppealImgDao;
import com.miqtech.master.entity.amuse.AmuseAppealImg;

/**
 * 娱乐赛申诉图片service
 */
@Component
public class AmuseAppealImgService {
	@Autowired
	private AmuseAppealImgDao amuseAppealImgDao;

	/**
	 * 保存多张申诉图片
	 */
	public void save(List<AmuseAppealImg> imgs) {
		if (CollectionUtils.isNotEmpty(imgs)) {
			amuseAppealImgDao.save(imgs);
		}
	}

	public List<AmuseAppealImg> findByAppealId(Long appealId) {
		return amuseAppealImgDao.findByAppealId(appealId);
	}

}
