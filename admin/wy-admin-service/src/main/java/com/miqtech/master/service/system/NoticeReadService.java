package com.miqtech.master.service.system;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.dao.common.NoticeReadDao;
import com.miqtech.master.entity.common.NoticeRead;

/**
 * 系统通知(商户)阅读记录service
 */
@Component
public class NoticeReadService {
	@Autowired
	private NoticeReadDao noticeReadDao;

	public NoticeRead findById(Long id) {
		return noticeReadDao.findOne(id);
	}

	public void save(NoticeRead noticeRead) {
		noticeReadDao.save(noticeRead);
	}

}