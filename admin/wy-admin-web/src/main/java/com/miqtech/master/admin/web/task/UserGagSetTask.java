package com.miqtech.master.admin.web.task;

import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.miqtech.master.dao.user.UserGagDao;
import com.miqtech.master.entity.user.UserGag;
import com.miqtech.master.service.amuse.AmuseActivityCommentService;

@Component
public class UserGagSetTask {
	@Autowired
	private AmuseActivityCommentService amuseActivityCommentService;
	@Autowired
	private UserGagDao userGagDao;

	@Scheduled(cron = "0 0 0 * * ?")
	public void publishActivityInfo() {
		List<UserGag> userGag = amuseActivityCommentService.findAll();
		if (CollectionUtils.isNotEmpty(userGag)) {
			for (UserGag aoa : userGag) {
				Integer days = aoa.getDays();
				Date createDate = aoa.getCreateDate();
				Date datenow = new Date();
				Long id = aoa.getId();
				if (createDate.getTime() + days * 24 * 60 * 60 * 1000 <= datenow.getTime()) {
					userGagDao.delete(id);
				}
			}
		}
	}
}
