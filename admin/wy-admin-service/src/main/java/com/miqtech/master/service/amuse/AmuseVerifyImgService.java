package com.miqtech.master.service.amuse;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.dao.amuse.AmuseVerifyImgDao;
import com.miqtech.master.entity.amuse.AmuseVerifyImg;

/**
 * 娱乐赛审核图片service
 */
@Component
public class AmuseVerifyImgService {
	@Autowired
	private AmuseVerifyImgDao amuseVerifyImgDao;

	public void save(List<AmuseVerifyImg> imgs) {
		amuseVerifyImgDao.save(imgs);
	}

	/**
	 * 查询用户在某赛事下的认证图片
	 */
	public List<AmuseVerifyImg> findByVerifyId(Long verifyId) {
		return amuseVerifyImgDao.findByVerifyId(verifyId);
	}

}
