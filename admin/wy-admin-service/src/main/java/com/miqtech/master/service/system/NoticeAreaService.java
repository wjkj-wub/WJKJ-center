package com.miqtech.master.service.system;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.dao.common.NoticeAreaDao;
import com.miqtech.master.entity.common.NoticeArea;

/**
 * 系统通知(商户)地区service
 */
@Component
public class NoticeAreaService {
	@Autowired
	private NoticeAreaDao noticeAreaDao;

	public NoticeArea findById(Long id) {
		return noticeAreaDao.findOne(id);
	}

	/**
	 * 根据公告Id和areaCode查询有效记录
	 */
	public NoticeArea findByNoticeIdAndAreaCode(Long noticeId, String areaCode) {
		return noticeAreaDao.findByNoticeIdAndAreaCode(noticeId, areaCode);
	}

	public void save(NoticeArea noticeArea) {
		noticeAreaDao.save(noticeArea);
	}

}