package com.miqtech.master.service.log;

import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.log.MerchantUserLogDao;
import com.miqtech.master.entity.log.MerchantUserLog;
import com.miqtech.master.entity.netbar.NetbarMerchant;
import com.miqtech.master.entity.netbar.NetbarStaff;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.utils.StringUtils;
import com.miqtech.master.vo.PageVO;

@Component
public class MerchantUserLogService {
	@Autowired
	private MerchantUserLogDao merchantUserLogDao;
	@Autowired
	private QueryDao queryDao;

	public void insertLog(Object obj, String msg, String info) {
		MerchantUserLog log = new MerchantUserLog();
		if (obj instanceof NetbarMerchant) {
			NetbarMerchant merchant = (NetbarMerchant) obj;
			log.setCreateUserId(merchant.getId());
			log.setUserType(1);
			log.setNetbarId(merchant.getNetbarId());
		} else {
			NetbarStaff staff = (NetbarStaff) obj;
			log.setCreateUserId(staff.getId());
			log.setUserType(2);
			log.setNetbarId(staff.getNetbarId());
		}
		log.setMsg(msg);
		log.setInfo(info);
		log.setCreateDate(new Date());
		merchantUserLogDao.save(log);
	}

	public PageVO queryList(Long netbarId, String keyword, String date, int page) {
		String sql = "";
		String keywordSql = "";
		String dateSql = "";
		if (StringUtils.isNotBlank(keyword)) {
			keywordSql = " and info like '%" + keyword + "%'";
		}
		if (StringUtils.isNotBlank(date)) {
			dateSql = " and date_format(create_date, '%Y-%m-%d')='" + date + "'";
		}
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("pageStart", (page - 1) * PageUtils.ADMIN_DEFAULT_PAGE_SIZE);
		params.put("pageNum", PageUtils.ADMIN_DEFAULT_PAGE_SIZE);

		sql = SqlJoiner.join("select * from merchant_t_operate_log where netbar_id=", String.valueOf(netbarId),
				keywordSql, dateSql, " order by create_date desc limit :pageStart,:pageNum");
		PageVO vo = new PageVO(queryDao.queryMap(sql, params));
		sql = SqlJoiner.join("select count(1) from merchant_t_operate_log where netbar_id=", String.valueOf(netbarId),
				keywordSql, dateSql);
		BigInteger bi = (BigInteger) queryDao.query(sql);
		vo.setTotal(bi.longValue());
		if (page * PageUtils.ADMIN_DEFAULT_PAGE_SIZE < bi.intValue()) {
			vo.setIsLast(1);
		}
		return vo;
	}
}
