package com.miqtech.master.service.netbar;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.netbar.NetbarImgDao;
import com.miqtech.master.entity.netbar.NetbarImg;

/**
 * 网吧图片管理service
 */
@Component
public class NetbarImgService {

	@Autowired
	private NetbarImgDao netbarImgDao;

	public void save(List<NetbarImg> netbarImgs) {
		netbarImgDao.save(netbarImgs);
	}

	public List<NetbarImg> findByTmpNetbarId(Long id) {
		return netbarImgDao.findByTmpNetbarId(id);
	}

	public List<NetbarImg> findByNetbarId(Long id) {
		return netbarImgDao.findByNetbarId(id);
	}

	/**
	 * 删除网吧图片(设置图片有效)
	 */
	public void delete(List<NetbarImg> dbimgs) {
		if (CollectionUtils.isNotEmpty(dbimgs)) {
			for (NetbarImg img : dbimgs) {
				img.setValid(-1);
			}
			netbarImgDao.save(dbimgs);
		}
	}

	/**
	 * 未审核图片通过审核,可见改为不可见  不可见改为可见
	 */
	public void setVisiable(List<NetbarImg> dbimgs) {
		if (CollectionUtils.isNotEmpty(dbimgs)) {
			for (NetbarImg img : dbimgs) {
				Integer valid = img.getValid();
				if (img.getVerified() == 0) {
					if (valid == 0) {
						img.setValid(1);
						img.setVerified(1);
					}
					if (valid == 1) {
						img.setValid(0);
						img.setVerified(1);
					}
				}
			}
			netbarImgDao.save(dbimgs);
		}

	}

	public List<Map<String, Object>> findVerifiedImgByNetbarId(long netbarId) {
		String sql = "select url from netbar_r_imgs where (verified = 1 and is_valid = 1 )  and netbar_id = "
				+ netbarId;
		return queryDao.queryMap(sql);
	}

	public NetbarImg findByUrl(String url) {
		return netbarImgDao.findByUrl(url);
	}

	@Autowired
	private QueryDao queryDao;

	public List<Map<String, Object>> findUnVerifiedImgByNetbarId(Long netbarId) {
		String sql = "select url from netbar_r_imgs where ((verified = 1 and is_valid = 1 ) or (verified = 0 and is_valid = 0)) and netbar_id = "
				+ netbarId;
		return queryDao.queryMap(sql);
	}

	public List<Map<String, Object>> findUrlUnverifiedByTmpNetbarId(Long tmpNetbarId) {
		String sql = "select url from netbar_r_imgs where ((verified = 1 and is_valid = 1 ) or (verified = 0 and is_valid = 0)) and tmp_netbar_id = "
				+ tmpNetbarId;
		return queryDao.queryMap(sql);
	}

	public List<NetbarImg> findUnverifiedByTmpNetbarId(Long id) {
		return netbarImgDao.findByTmpNetbarIdAndVerified(id, 0);
	}

}