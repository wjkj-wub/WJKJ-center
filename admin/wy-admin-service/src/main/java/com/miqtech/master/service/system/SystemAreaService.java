package com.miqtech.master.service.system;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import com.google.common.base.Joiner;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.common.SystemAreaDao;
import com.miqtech.master.entity.common.SystemArea;
import com.miqtech.master.service.common.StringRedisOperateService;
import com.miqtech.master.utils.PinyinUtils;
import com.miqtech.master.utils.SqlJoiner;

/**
 * 地理信息service
 */
@Component
public class SystemAreaService {
	@Autowired
	private SystemAreaDao systemAreaDAO;
	@Autowired
	private QueryDao queryDao;
	@Autowired
	private StringRedisOperateService stringRedisOperateService;

	public SystemArea save(SystemArea area) {
		return systemAreaDAO.save(area);
	}

	public void delete(long id) {
		SystemArea area = findById(id);
		if (area != null) {
			area.setValid(CommonConstant.INT_BOOLEAN_FALSE);
			save(area);
		}
	}

	/**
	 * 查找所有的省级地区
	 */
	public List<SystemArea> queryAllRoot() {
		return systemAreaDAO.findAllRoot();
	}

	/**
	 * 查找有效的省级地区
	 */
	public List<SystemArea> queryValidRoot() {
		return systemAreaDAO.findValidRoot();
	}

	/**
	 * 查找某个地区的所有子地区
	 */
	public List<SystemArea> queryAllChildren(String code) {
		String likeCode = convertCode(code);
		return systemAreaDAO.findAllChildren(likeCode, code);
	}

	/**
	 * 查找某个地区的所有有效子地区
	 */
	public List<SystemArea> queryValidChildren(String code) {
		String likeCode = convertCode(code);
		return systemAreaDAO.findValidChildren(likeCode, code);
	}

	/**
	 * 查找该区域下的所有有效区域地区
	 * eg:code为浙江省 则返回浙江省所有的区域地区
	 */
	public List<SystemArea> queryAllValidChildren(String code) {
		String likeCode = convertAllCode(code);
		return systemAreaDAO.findValidChildren(likeCode, code);
	}

	private String convertAllCode(String code) {
		if (StringUtils.endsWith(code, "0000")) {
			code = StringUtils.substring(code, 0, 2);
			return code + "%";
		} else if (StringUtils.endsWith(code, "00")) {
			code = StringUtils.substring(code, 0, 4);
			return code + "%";
		}
		return code;
	}

	/**
	 * 通过名称查询夏季地区
	 */
	public List<Map<String, Object>> queryValidChildrenByName(String name) {
		String sql = "SELECT c.* FROM sys_t_area p LEFT JOIN sys_t_area c ON p.id = c.pid WHERE p.name LIKE '%:likeName%'"
				.replaceAll(":likeName", name);
		return queryDao.queryMap(sql);
	}

	private String convertCode(String code) {
		if (StringUtils.endsWith(code, "0000")) {
			code = StringUtils.substring(code, 0, 2);
			return code + "%" + "00";
		} else if (StringUtils.endsWith(code, "00")) {
			code = StringUtils.substring(code, 0, 4);
			return code + "%";
		}
		return code;
	}

	/**
	 * 查找某个用户(录入人员或审核人员)有效的省级地区
	 */
	public List<SystemArea> queryValidRootLimit(Long userId) {
		return systemAreaDAO.findValidRootLimit(userId);
	}

	/**
	 * 查找某个用户(录入人员或审核人员)某个地区下的有效的地区信息
	 */
	public List<SystemArea> queryValidChildrenLimit(String code, Long userId) {
		return systemAreaDAO.findValidChildrenLimit(code, userId);
	}

	/**
	 * 查找所有有效的二级城市
	 */
	public List<SystemArea> queryAllValidCity() {
		return systemAreaDAO.queryAllValidCity();
	}

	/**
	 * 查找所有的二级城市
	 */
	public List<SystemArea> queryAllCity() {
		return systemAreaDAO.queryAllCity();
	}

	/**
	 * 查询所有有效的，且areaCode符合条件的 二级城市
	 */
	public List<SystemArea> queryCityByAreaCodes(String areaCodes, boolean onlyValidArea) {
		if (areaCodes == null) {
			if (onlyValidArea) {
				return queryAllValidCity();
			} else {
				return queryAllCity();
			}
		}

		if (onlyValidArea) {
			return systemAreaDAO.findByValidAndAreaCodeLikeAndAreaCodeNotLikeAndAreaCodeIn(
					CommonConstant.INT_BOOLEAN_TRUE, "%00", "%0000", areaCodes.split(","));
		} else {
			return systemAreaDAO.findByAreaCodeLikeAndAreaCodeNotLikeAndAreaCodeIn("%00", "%0000",
					areaCodes.split(","));
		}
	}

	/**
	 * 查询所有有效的 三级区域
	 */
	public List<SystemArea> queryAllValidRegions() {
		return systemAreaDAO.findByValidAndAreaCodeNotLike(CommonConstant.INT_BOOLEAN_TRUE, "%00");
	}

	/**
	 * 查询所有 三级区域
	 */
	public List<SystemArea> queryAllRegions() {
		return systemAreaDAO.findByAreaCodeNotLike("%00");
	}

	public SystemArea findByName(String areaName) {
		SystemArea findByName = null;
		try {
			findByName = systemAreaDAO.findByName(areaName);
		} catch (Exception e) {
		}
		return findByName;
	}

	public SystemArea findByCode(String areaCode) {
		SystemArea findByAreaCode = systemAreaDAO.findByAreaCode(areaCode);
		return findByAreaCode;
	}

	public List<Map<String, Object>> queryAll() {
		String sql = "select id,name,pid pId from sys_t_area where pid is not null ";
		return queryDao.queryMap(sql);
	}

	public List<Map<String, Object>> queryAllValid() {
		String sql = "select id,name,pid pId from sys_t_area where pid is not null and is_valid=1 ";
		return queryDao.queryMap(sql);
	}

	public List<Map<String, Object>> queryAllValidCode(List<String> codes) {
		String codeSql = "";
		String concat = "";
		Joiner joiner = Joiner.on(" or ");
		if (codes.size() > 0) {
			for (int i = 0; i < codes.size(); i++) {
				String str = "area_code like '" + codes.get(i) + "%'";
				if (i == 0) {
					concat += str;
				} else {
					concat = joiner.join(concat, str);
				}
			}
			codeSql = " and (" + concat + ")";
		}
		String sql = "select area_code code,id,name,pid pId from sys_t_area where pid is not null and is_valid=1 "
				+ codeSql;
		return queryDao.queryMap(sql);
	}

	/**
	 * 查找活动对应的区域
	 */
	public List<Map<String, Object>> queryAllValidCodeByInviteCode(String id) {
		String sql = "select a.area_code code,a.id,a.name,a.pid pId,case when b.id is null then 'false' else 'true' end checked,case when b.id is null then 'false' else 'true' end open from sys_t_area a left join invitecode_activity b on locate(a.area_code,b.area)<>0 and b.id="
				+ id + " where a.pid is not null and a.is_valid=1";
		return queryDao.queryMap(sql);
	}

	/**
	 * 根据录入或者审核人员管辖的地区
	 * @param userId 录入或审核人员id
	 */
	public List<Map<String, Object>> queryAllWithChecked(Long userId) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("userId", userId);
		String sql = SqlJoiner.join(
				" select a.id,name,pid pId,case when b.id is null then 'false' else 'true' end checked,case when b.id is null then 'false' else 'true' end open ",
				" from sys_t_area a left join sys_r_user_area b on a.id=b.area_id and sys_user_id=:userId where a.is_valid=1");
		return queryDao.queryMap(sql, params);
	}

	/**
	 * 根据录入或者审核人员管辖的地区
	 * @param userId 录入或审核人员id
	 */
	public List<Map<String, Object>> queryAllOnlyChecked(Long userId) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("userId", userId);
		String sql = SqlJoiner.join("select a.id,name,pid pId, 'true' checked, 'true' open,'true' chkDisabled ",
				" from sys_t_area a,sys_r_user_area b where a.is_valid=1 and a.id=b.area_id and sys_user_id=:userId");
		return queryDao.queryMap(sql, params);
	}

	/**
	 * 查询省市级地区树
	 */
	public List<SystemArea> getTree(boolean onlyValidArea) {
		return getTree(null, onlyValidArea);
	}

	/**
	 * 查询省市地级地区树，并筛选市级地区
	 */
	public List<SystemArea> getTree(String areaCodes, boolean onlyValidArea) {
		// 查询城市
		List<SystemArea> cities = null;
		if (StringUtils.isNotBlank(areaCodes)) {
			cities = queryCityByAreaCodes(areaCodes, onlyValidArea);
		} else {
			if (onlyValidArea) {
				cities = queryAllValidCity();
			} else {
				cities = queryAllCity();
			}
		}

		// 查询省及区域
		List<SystemArea> root = null;
		List<SystemArea> regions = null;
		if (onlyValidArea) {
			root = queryValidRoot();
			regions = queryAllValidRegions();
		} else {
			root = queryAllRoot();
			regions = queryAllRegions();
		}

		// 组装树形结构
		for (SystemArea province : root) {
			for (Iterator<SystemArea> it = cities.iterator(); it.hasNext();) {
				SystemArea city = it.next();
				if (province.getId().equals(city.getPid())) {
					for (Iterator<SystemArea> regionIt = regions.iterator(); regionIt.hasNext();) {
						SystemArea region = regionIt.next();
						if (city.getId().equals(region.getPid())) {
							city.addChild(region);
							regionIt.remove();
						}
					}
					province.addChild(city);
					it.remove();
				}
			}
		}

		return root;
	}

	public List<SystemArea> findByValid(Integer valid) {
		return systemAreaDAO.findByValid(valid);
	}

	public SystemArea findById(Long id) {
		return systemAreaDAO.findOne(id);
	}

	public List<SystemArea> findByPid(Long pid) {
		return systemAreaDAO.findByPid(pid);
	}

	/**
	 * 更新所有有效地区数据的拼音字段
	 */
	public void updatePinyin() {
		Specification<SystemArea> spec = new Specification<SystemArea>() {
			@Override
			public Predicate toPredicate(Root<SystemArea> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return null;
			}
		};
		List<SystemArea> areas = systemAreaDAO.findAll(spec);
		for (SystemArea area : areas) {
			area.setPinyin(PinyinUtils.toSimplePinyin(area.getName()));
			save(area);
		}
	}

	public List<SystemArea> findAll() {
		return systemAreaDAO.findAll();
	}

	public String getAreaNamesFromAreaCodes(String areaCodes) {
		areaCodes = areaCodes.replaceFirst(",", "");
		String[] array = areaCodes.split(",");
		StringBuilder sb = new StringBuilder();
		if (array != null) {
			for (String s : array) {
				sb.append(stringRedisOperateService.getData(CommonConstant.AREA_KEY + s));
				sb.append(",");
			}
		}
		return sb.toString();
	}

	/**
	 * 查找活动对应的区域
	 */
	public List<Map<String, Object>> queryForOpen() {
		String sql = "select a.area_code code,a.id,a.name,a.pid pId,case when a.is_valid=0 then 'false' else 'true' end checked from sys_t_area a where a.pid is not null";
		return queryDao.queryMap(sql);
	}

	/**开通城市
	 * @param code
	 */
	public void open(String code) {
		String sql = "";
		if (code.length() >= 4) {
			sql = "update sys_t_area set is_valid=1 where pid is not null and is_valid=0 and area_code="
					+ code.substring(0, 2) + "0000";
			queryDao.update(sql);
		}
		if (code.length() == 6) {
			sql = "update sys_t_area set is_valid=1 where pid is not null and is_valid=0 and area_code="
					+ code.substring(0, 4) + "00";
			queryDao.update(sql);
		}
		sql = " update sys_t_area set is_valid=1 where pid is not null and is_valid=0 and area_code like '" + code
				+ "%'";
		queryDao.update(sql);

	}

	/**关闭城市
	 * @param code
	 */
	public void close(String code) {
		String sql = " update sys_t_area set is_valid=0 where pid is not null and is_valid=1 and area_code like '"
				+ code + "%'";
		queryDao.update(sql);

	}

	/**
	 * 得到该区域的上级地区名
	 * @param preStr map名字的前缀
	 * @param code 地区的code
	 */
	public Map<String, Object> getAllNameByAreaCode(String code, String preStr) {
		String provinceArea = code.substring(0, 2) + "0000"; //省级区域code
		String cityArea = code.substring(0, 4) + "00";//市级区域code
		String areaSt = code.substring(0, 6);//区域code
		SystemArea province = findByCode(provinceArea);
		SystemArea city = findByCode(cityArea);
		SystemArea area = findByCode(areaSt);
		Map<String, Object> result = new HashMap<String, Object>();
		result.put(preStr + "province", province.getName());
		result.put(preStr + "city", city.getName());
		result.put(preStr + "area", area.getName());
		return result;
	}

	/**
	 * 根据区areacode获取完整地址信息
	 */
	public String getAreaInfoByCode(String areaCode){
		if(StringUtils.endsWith(areaCode,"00")){
			return null;
		}
		String sql="select REPLACE(GROUP_CONCAT(name),',','') name from" +
				" (select name from sys_t_area where area_code=CONCAT(SUBSTR('"+areaCode+"',1,2),'0000')" +
				" union" +
				" select name from sys_t_area where area_code=CONCAT(SUBSTR('"+areaCode+"',1,4),'00')" +
				" union" +
				" select name from sys_t_area where area_code='"+areaCode+"')a";
		return queryDao.query(sql);
	}
}