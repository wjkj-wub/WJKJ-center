package com.miqtech.master.service.user;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.dao.user.UserComplaintDao;
import com.miqtech.master.entity.user.UserComplaint;

@Component
public class UserComplaintService {

	@Autowired
	private UserComplaintDao userComplaintDao;

	/**举报用户
	 * @param userId
	 * @param byUserId
	 */
	public boolean inform(Long userId, Long targetId, Integer type, Integer category, String remark, String byUserId) {
		List<UserComplaint> list = userComplaintDao.findByUserIdAndSubIdAndValid(userId, targetId, 0);
		if (list.size() > 0) {
			return false;
		} else {
			UserComplaint userComplaint = new UserComplaint();
			userComplaint.setUserId(userId);
			if (byUserId != null) {
				userComplaint.setType(1);
				userComplaint.setSubId(Long.valueOf(byUserId));
			} else {
				userComplaint.setType(type);
				userComplaint.setSubId(targetId);
			}
			userComplaint.setCreateDate(new Date());
			userComplaint.setValid(0);//0待确认1已确认
			userComplaint.setCategory(category);
			userComplaint.setRemark(remark);
			userComplaintDao.save(userComplaint);
			return true;
		}
	}
}