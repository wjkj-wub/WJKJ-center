package com.miqtech.master.service.netbar;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.netbar.NetbarStaffDao;
import com.miqtech.master.entity.netbar.NetbarMerchant;
import com.miqtech.master.entity.netbar.NetbarStaff;
import com.miqtech.master.utils.EncodeUtils;
import com.miqtech.master.utils.PageUtils;

@Component
public class NetbarStaffService {

	@Autowired
	NetbarStaffDao netbarStaffDao;
	@Autowired
	QueryDao queryDao;

	public List<Map<String, Object>> findPageData(NetbarMerchant currentMerchant, int page) {
		Map<String, Object> params = Maps.newHashMap();
		int start = (page - 1) * PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		params.put("start", start);
		params.put("pageSize", PageUtils.ADMIN_DEFAULT_PAGE_SIZE);
		params.put("netbarId", currentMerchant.getNetbarId());
		String sql = " select s.*, IFNULL(sum(total_amount),0) totalAmount, IFNULL(sum(amount),0) amount from netbar_t_staff s left join netbar_r_order o ON s.id = o.operate_staff_id and o.netbar_id =:netbarId  and o.is_valid > 0 where s.is_valid = 1  and s.netbar_id = :netbarId "
				+ " group by s.id order by s.create_date desc limit :start, :pageSize ";
		return queryDao.queryMap(sql, params);
	}

	public long countStaff(NetbarMerchant currentMerchant) {
		String sql = "select count(id) from netbar_t_staff where is_valid = 1 and netbar_id="
				+ currentMerchant.getNetbarId();
		Number count = queryDao.query(sql);
		return count.intValue();
	}

	public NetbarStaff findById(Long id) {
		return netbarStaffDao.findById(id);
	}

	public NetbarStaff findByTelephone(String phone) {
		return netbarStaffDao.findByTelephoneAndValid(phone, 1);
	}

	public void saveStaff(NetbarStaff staff) {
		if (staff.getId() == null) {// 新增：产生邀请码
			Long netbarId = staff.getNetbarId();
			Integer staffsCount = ((Number) queryDao
					.query("select count(id) from netbar_t_staff where netbar_id=" + netbarId)).intValue();
			staff.setInvitationCode(netbarId + "a" + (staffsCount + 1));
		}
		staff.setValid(1);
		Date date = new Date();
		staff.setCreateDate(date);
		staff.setUpdateDate(date);

		netbarStaffDao.save(staff);
	}

	public void removeStaff(Long id) {
		queryDao.update("update netbar_t_staff set is_valid = 0 where id=" + id);
	}

	public List<NetbarStaff> findByInvitationCode(String invitation, int valid) {
		invitation = StringUtils.trim(invitation);
		return netbarStaffDao.findByInvitationCodeAndValid(invitation, valid);
	}

	public NetbarStaff findByTelephoneAndPassword(String phone, String password) {
		String pwd = EncodeUtils.base64Md5(password);
		List<NetbarStaff> staffs = netbarStaffDao.findByTelephoneAndPasswordAndValidOrderByCreateDate(phone, pwd, 1);
		if (CollectionUtils.isNotEmpty(staffs)) {
			return staffs.get(0);
		}
		return null;
	}

	public List<NetbarStaff> findByNameOrTelephone(String name, String telephone) {
		return netbarStaffDao.findByNameOrTelephoneAndValid(name, telephone, 1);
	}

	public NetbarStaff findByName(String name) {
		return netbarStaffDao.findByNameAndValid(name, 1);
	}
}
