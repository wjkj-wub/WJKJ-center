package com.miqtech.master.service.netbar;

import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.netbar.NetbarErrorRecoveryDao;
import com.miqtech.master.entity.netbar.NetbarErrorRecovery;
import com.miqtech.master.service.system.SystemAreaService;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.vo.PageVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 网吧纠错历史信息
 * 
 * @author Administrator
 *
 */
@Component
public class NetbarErrorRecoveryService {

	@Autowired
	private QueryDao queryDao;
	@Autowired
	private SystemAreaService systemAreaService;
	@Autowired
	private NetbarErrorRecoveryDao netbarErrorRecoveryDao;

	private static Pattern p = Pattern.compile("^[1][3,4,5,8][0-9]{9}$");

	/**
	 * 返回该区域下所有的网吧反馈信息
	 * 
	 * @param areaCode
	 * @return
	 */
	public PageVO findAllByAreaCode(String areaCode, String name, Integer page, String state) {
		String codeLike = convertAllCode(areaCode);
		String limit = StringUtils.EMPTY;
		Integer pageSize = 30;
		if (page > 0) {
			Integer startRow = (page - 1) * pageSize;
			limit = SqlJoiner.join(" LIMIT ", startRow.toString(), ", ", pageSize.toString());
		}
		StringBuilder sqlCount = new StringBuilder(
				"select count(1) allNum from netbar_error_recovery ner left join netbar_t_info nti on nti.id=ner.netbar_id left join user_t_info uti on uti.id=ner.user_id where ner.is_valid=1");
		StringBuilder sql = new StringBuilder(
				"select ner.*,nti.name bname,nti.address baddress,nti.longitude blongitude,nti.latitude blatitude,if(isnull(neri.url),neri.url, CONCAT('http://img.wangyuhudong.com/',neri.url)) bimg,nti.is_release isRelease,nti.area_code barea_code,nti.img from netbar_error_recovery ner left join netbar_error_recovery_img neri on neri.recovery_id=ner.id left join netbar_t_info nti on nti.id=ner.netbar_id left join user_t_info uti on uti.id=ner.user_id where ner.is_valid=1 ");
		if (StringUtils.isNotBlank(areaCode) && !areaCode.equals("000000")) {
			sql.append(" and ner.area_code REGEXP " + codeLike);
			sqlCount.append(" and ner.area_code REGEXP " + codeLike);
		}
		if (StringUtils.isNotBlank(name)) {
			// 匹配电话号码
			Matcher m = p.matcher(name);
			if (m.matches()) {
				sql.append(" and uti.telephone like '%" + name + "%'");
				sqlCount.append(" and uti.telephone like '%" + name + "%'");
			} else {
				sql.append(" and nti.name like '%" + name + "%'");
				sqlCount.append(" and nti.name like '%" + name + "%'");
			}
		}
		if (StringUtils.isNotBlank(state) && !state.equals("-1")) {
			sql.append(" and ner.status=" + state);
			sqlCount.append(" and ner.status=" + state);
		}
		sql.append(limit);
		List<Map<String, Object>> netbarErrorRecoveryList = queryDao.queryMap(sql.toString());
		for (Map<String, Object> netbarErrorRecovery : netbarErrorRecoveryList) {
			if (netbarErrorRecovery.get("img") != null) {
				netbarErrorRecovery.put("img",
						"http://img.wangyuhudong.com/" + netbarErrorRecovery.get("img").toString());
			}
			if (netbarErrorRecovery.get("area_code") != null) {
				netbarErrorRecovery.putAll(
						systemAreaService.getAllNameByAreaCode(netbarErrorRecovery.get("area_code").toString(), ""));
			}
			netbarErrorRecovery.putAll(
					systemAreaService.getAllNameByAreaCode(netbarErrorRecovery.get("barea_code").toString(), "b"));
		}
		Number totalCount = queryDao.query(sqlCount.toString());
		PageVO vo = new PageVO();
		vo.setList(netbarErrorRecoveryList);
		vo.setCurrentPage(page);
		vo.setTotal(totalCount.intValue());
		vo.setIsLast(totalCount.intValue() > page * 30 ? 1 : 0);
		return vo;
	}

	private String convertAllCode(String areaCode) {
		if (StringUtils.endsWith(areaCode, "0000")) {
			areaCode = StringUtils.substring(areaCode, 0, 2);
			return "'" + areaCode + "[0-9]{4}'";
		} else if (StringUtils.endsWith(areaCode, "00")) {
			areaCode = StringUtils.substring(areaCode, 0, 4);
			return "'" + areaCode + "[0-9]{2}'";
		}
		return "'" + areaCode + "'";
	}

	public Map<String, Object> countResult() {
		String sql = "select count(1) allNum,count(if(status>0,null,1)) channelNum,count(if(status=0,null,1)) noChannelNum from netbar_error_recovery where is_valid=1";
		return queryDao.querySingleMap(sql);
	}

	public NetbarErrorRecovery findById(Long id) {
		return netbarErrorRecoveryDao.findOne(id);
	}

	public NetbarErrorRecovery save(NetbarErrorRecovery netbarErrorRecovery) {
		return netbarErrorRecoveryDao.save(netbarErrorRecovery);
	}

	public List<NetbarErrorRecovery> findByIds(String ids) {
		String sql = "select * from netbar_error_recovery where is_valid=1 and status=0 and id in (" + ids + ")";
		return queryDao.queryObject(sql, NetbarErrorRecovery.class);
	}

	// 查询所有待审核的网吧
	public List<NetbarErrorRecovery> findByStatus() {
		String sql = "select * from netbar_error_recovery where is_valid=1 and status=0";
		return queryDao.queryObject(sql, NetbarErrorRecovery.class);
	}

	// 批量保存
	public List<NetbarErrorRecovery> save(List<NetbarErrorRecovery> list) {
		return (List<NetbarErrorRecovery>) netbarErrorRecoveryDao.save(list);
	}
}
