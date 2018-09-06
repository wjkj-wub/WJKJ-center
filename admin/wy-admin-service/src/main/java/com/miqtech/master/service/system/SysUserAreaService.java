package com.miqtech.master.service.system;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.common.SystemUserAreaDao;
import com.miqtech.master.entity.common.SysUserArea;
import com.miqtech.master.entity.common.SystemArea;

/**
 * 系统录入或者审核人员地区service
 */
@Component
public class SysUserAreaService {

	@Autowired
	private QueryDao queryDao;
	@Autowired
	private SystemUserAreaDao sysUserAreaDao;

	/**
	 * 更新用户所管辖的地区
	 * @param areaIds 地区ids
	 * @param userId 录入或审核人员id
	 */
	@Transactional
	public void update(String areaIds, Long userId) {
		sysUserAreaDao.deleteBySysUserId(userId);
		if (StringUtils.isNotBlank(areaIds)) {
			String[] ids = areaIds.split(",");
			for (String s : ids) {
				SysUserArea sysUserArea = new SysUserArea();
				sysUserArea.setSysUserId(userId);
				sysUserArea.setAreaId(Long.valueOf(s));
				sysUserAreaDao.save(sysUserArea);
			}
		}
	}

	@Transactional
	public void update(List<SystemArea> list, Long userId) {
		sysUserAreaDao.deleteBySysUserId(userId);
		if (list != null && list.size() > 0) {
			for (SystemArea obj : list) {
				SysUserArea sysUserArea = new SysUserArea();
				sysUserArea.setSysUserId(userId);
				sysUserArea.setAreaId(obj.getId());
				sysUserAreaDao.save(sysUserArea);
			}
		}
	}

	@Transactional
	public void update(String[] areas, Long userId) {
		delete(userId);
		if (ArrayUtils.isNotEmpty(areas)) {
			List<SysUserArea> uas = new ArrayList<SysUserArea>();
			for (String areaCode : areas) {
				SysUserArea a = new SysUserArea();
				a.setAreaCode(areaCode);
				a.setSysUserId(userId);
				uas.add(a);
			}

			sysUserAreaDao.save(uas);
		}
	}

	public void delete(Long userId) {
		String sql = "DELETE FROM sys_r_user_area WHERE sys_user_id = " + userId.toString();
		queryDao.update(sql);
	}

	public List<SysUserArea> findBySysUserId(Long userId) {
		return sysUserAreaDao.findBySysUserIdOrderByAreaIdAsc(userId);
	}

	public SysUserArea queryBySysUserId(Long id) {
		List<SysUserArea> areas = sysUserAreaDao.findBySysUserId(id);
		if (CollectionUtils.isNotEmpty(areas)) {
			return areas.get(0);
		}
		return null;
	}

	public String findUserAreaCode(Long userId) {
		SysUserArea area = queryBySysUserId(userId);
		if (null != area) {
			return area.getAreaCode();
		}

		return null;
	}
}
