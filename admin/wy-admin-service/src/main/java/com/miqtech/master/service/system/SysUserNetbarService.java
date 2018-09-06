package com.miqtech.master.service.system;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Joiner;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.common.SysUserNetbarDao;
import com.miqtech.master.dao.common.SystemUserDao;
import com.miqtech.master.entity.common.SysUserNetbar;
import com.miqtech.master.entity.common.SystemUser;
import com.miqtech.master.entity.netbar.NetbarInfo;
import com.miqtech.master.service.common.StringRedisOperateService;
import com.miqtech.master.service.netbar.NetbarInfoService;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.vo.PageVO;

@Component
public class SysUserNetbarService {
	private static String NETBAR_ASSIGN = "netbar.assign.new.netbar";
	@Autowired
	private SysUserNetbarDao sysUserNetbarDao;
	@Autowired
	private SystemUserDao systemUserDao;
	@Autowired
	private QueryDao queryDao;
	@Autowired
	private StringRedisOperateService redisOperateService;
	@Autowired
	private NetbarInfoService netbarInfoService;

	public PageVO findSubAccount(Long userId, Integer page) {
		String sql = "";
		PageVO vo = new PageVO();
		sql = SqlJoiner.join("select count(1) from sys_t_user a where a.is_valid=1 and a.parent_id=",
				String.valueOf(userId));
		Number totalCount = queryDao.query(sql);
		if (totalCount != null) {
			vo.setTotal(totalCount.intValue());
		}
		int pageSize = PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		int start = (page - 1) * pageSize;
		if (start + pageSize >= vo.getTotal()) {
			vo.setIsLast(1);
		}
		List<SystemUser> systemUserList = netbarInfoService.getLevelDownAccount(userId);
		List<String> sqlList = new ArrayList<String>();
		for (SystemUser obj : systemUserList) {
			String id = String.valueOf(obj.getId());
			String ids = netbarInfoService.getSubAccountIds(obj.getId());
			ids = ids.replace(String.valueOf(userId) + ",", "");
			String join = SqlJoiner.join("select * from sys_t_user a left join (select ", id,
					" user_id,count(1) netbarNum from sys_r_user_netbar a where sys_user_id in(", ids,
					"))b on a.id=b.user_id where a.id=", id, " and (a.is_valid=1 or a.is_valid is null)");
			sqlList.add(join);
		}
		Joiner joiner = Joiner.on(" union ");
		sql = joiner.join(sqlList);
		sql = SqlJoiner.join(sql, " limit :start,:pageSize");
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("start", start);
		params.put("pageSize", pageSize);
		vo.setCurrentPage(page);
		vo.setList(queryDao.queryMap(sql, params));
		return vo;
	}

	public SystemUser findById(Long id) {
		return systemUserDao.findOne(id);
	}

	/**第一次分配所有网吧给一级账号
	 * @param list
	 * @param userId
	 */
	public void grantAllNetbar(List<NetbarInfo> list, Long userId) {
		for (NetbarInfo obj : list) {
			SysUserNetbar sysUserNetbar = new SysUserNetbar();
			sysUserNetbar.setSysUserId(userId);
			sysUserNetbar.setNetbarId(obj.getId());
			sysUserNetbar.setCreateDate(new Date());
			sysUserNetbarDao.save(sysUserNetbar);
		}
	}

	/**分配新增网吧给一级账号
	 * 
	 */
	public void grantNewNetbar() {
		boolean lock = redisOperateService.setIfAbsent(NETBAR_ASSIGN, "1");
		redisOperateService.expire(NETBAR_ASSIGN, 10, TimeUnit.MINUTES);
		if (lock) {
			SystemUser systemUser = systemUserDao.findByUserTypeAndParentIdIsNull(5);
			if (systemUser != null) {
				String sql = "select id from netbar_t_info where is_valid=1 and is_release=1 and id not in (select netbar_id from sys_r_user_netbar)";
				List<Map<String, Object>> ids = queryDao.queryMap(sql);
				Long netbarId = null;
				for (Map<String, Object> map : ids) {
					netbarId = ((BigInteger) map.get("id")).longValue();
					SysUserNetbar exist = this.findByNetbarId(netbarId);
					if (exist != null) {
						continue;
					}
					SysUserNetbar sysUserNetbar = new SysUserNetbar();
					sysUserNetbar.setSysUserId(systemUser.getId());
					sysUserNetbar.setNetbarId(netbarId);
					sysUserNetbar.setCreateDate(new Date());
					sysUserNetbar.setFirstName(systemUser.getRealname());
					sysUserNetbarDao.save(sysUserNetbar);
				}
			}
			redisOperateService.delData(NETBAR_ASSIGN);
		}
	}

	public SysUserNetbar findByNetbarId(Long netbarId) {
		return sysUserNetbarDao.findByNetbarId(netbarId);
	}

	/**分配网吧
	 * @param netbarIds
	 * @param userId
	 */
	public void assignNetbar(List<Map<String, Object>> netbarIds, Long userId, Long loginId) {
		SystemUser systemUser = systemUserDao.findOne(userId);
		if (systemUser != null) {
			for (Map<String, Object> map : netbarIds) {
				Long netbarId = ((BigInteger) map.get("id")).longValue();
				if (netbarId != null) {
					SysUserNetbar sysUserNetbar = findByNetbarId(netbarId);
					if (sysUserNetbar != null) {
						sysUserNetbar.setSysUserId(userId);
					} else {
						sysUserNetbar = new SysUserNetbar();
						sysUserNetbar.setSysUserId(userId);
						sysUserNetbar.setNetbarId(netbarId);
						sysUserNetbar.setCreateDate(new Date());
					}
					if (systemUser.getUserType() == 6) {
						sysUserNetbar.setSecondName(systemUser.getRealname());
					} else if (systemUser.getUserType() == 7) {
						sysUserNetbar.setThirdName(systemUser.getRealname());
					} else if (systemUser.getUserType() == 8) {
						sysUserNetbar.setFourthName(systemUser.getRealname());
					}
					sysUserNetbar.setUpdateUserId(loginId);
					sysUserNetbarDao.save(sysUserNetbar);
				}
			}
		}
	}

	/**分配单个网吧
	 * @param netbarIds
	 * @param userId
	 */
	public void assignNetbarSingle(Long netbarId, Long userId, Long loginId) {
		SystemUser systemUser = systemUserDao.findOne(userId);
		if (systemUser != null) {
			SysUserNetbar sysUserNetbar = findByNetbarId(netbarId);
			if (sysUserNetbar != null) {
				sysUserNetbar.setSysUserId(userId);
			} else {
				sysUserNetbar = new SysUserNetbar();
				sysUserNetbar.setSysUserId(userId);
				sysUserNetbar.setNetbarId(netbarId);
				sysUserNetbar.setCreateDate(new Date());
			}
			if (systemUser.getUserType() == 6) {
				sysUserNetbar.setSecondName(systemUser.getRealname());
			} else if (systemUser.getUserType() == 7) {
				sysUserNetbar.setThirdName(systemUser.getRealname());
			} else if (systemUser.getUserType() == 8) {
				sysUserNetbar.setFourthName(systemUser.getRealname());
			}
			sysUserNetbar.setUpdateUserId(loginId);
			sysUserNetbarDao.save(sysUserNetbar);
		}
	}

	public List<SysUserNetbar> findBySysUserId(Long id) {
		return sysUserNetbarDao.findBySysUserId(id);
	}
}
