package com.miqtech.master.thirdparty.service;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.miqtech.master.config.SystemConfig;
import com.miqtech.master.dao.msg.Msg4SysDao;
import com.miqtech.master.dao.msg.Msg4UserDao;
import com.miqtech.master.entity.msg.Msg4Sys;
import com.miqtech.master.entity.msg.Msg4User;
import com.miqtech.master.thirdparty.util.JPushUtils;

@Component
public class JpushService {

	private final static Logger logger = LoggerFactory.getLogger(JpushService.class);

	private final static ExecutorService es = Executors.newFixedThreadPool(
			Runtime.getRuntime().availableProcessors() * 2,
			new ThreadFactoryBuilder().setNameFormat("极光推送消息处理线程池-").build());

	@Autowired
	Msg4SysDao msg4SysDao;
	@Autowired
	Msg4UserDao msg4UserDao;
	@Autowired
	private SystemConfig systemConfig;

	public void notifyMemberAliasMsg(final int type, final Long memberId, final String category, final String title,
			final String content, final boolean isPush, final Long id) {
		es.execute(() -> {
			try {
				Msg4User msg = new Msg4User();
				msg.setContent(content);
				msg.setIsRead(0);
				msg.setTitle(title);
				msg.setType(type);
				msg.setValid(1);
				msg.setObjId(id == null ? 0 : id);
				msg.setUserId(memberId);
				msg.setCreateDate(new Date());
				Msg4User userMsg = msg4UserDao.save(msg);
				boolean result = userMsg != null;
				if (isPush && result) {
					String alias = systemConfig.getJpushClientAlias() + memberId;
					JPushUtils.sendToClientALias(alias, content, category, id.toString(), null);
				}
			} catch (Exception e) {
				logger.error("推送信息-notifyMemberMsg-异常:{}", e);
			}
		});
	}

	public void notifyMemberTagMsg(final String category, final String title, final String content,
			final boolean isPush, final Long id) {
		es.execute(() -> {
			try {
				Msg4Sys msg = new Msg4Sys();
				msg.setContent(content);
				msg.setType(2);
				msg.setValid(1);
				msg.setCreateDate(new Date());
				msg.setTitle(title);
				Msg4Sys sysMsg = msg4SysDao.save(msg);
				boolean result = sysMsg != null;
				if (isPush && result) {
					JPushUtils.sendToClientTag(systemConfig.getJpushClientTag(), content, category, id.toString(),
							null);
				}
			} catch (Exception e) {
				logger.error("推送信息-notifyMemberSysMsg-异常:{}", e);
			}
		});
	}

	public void notifyAllSysMsg(final String category, final String title, final String content, final boolean isPush) {
		es.execute(() -> {
			try {
				List<Msg4Sys> sys = Lists.newArrayList();
				Msg4Sys msgToMember = new Msg4Sys();
				msgToMember.setContent(content);
				msgToMember.setType(1);
				msgToMember.setValid(1);
				msgToMember.setTitle(title);
				msgToMember.setCreateDate(new Date());
				sys.add(msgToMember);
				if (isPush) {
					JPushUtils.sendToAll(content, category, null);
				}
			} catch (Exception e) {
				logger.error("推送信息-notifyMerchantAndMemberSysMsg-异常:{}", e);
			}
		});
	}

	public void notifyMemberTagMsgWithoutDB(final String tag, final String category, final String content,
			final String value, final String extendData) {
		es.execute(() -> {
			try {
				JPushUtils.sendToClientTag(tag, content, category, value, extendData);
			} catch (Exception e) {
				logger.error("推送信息-notifyMemberSysMsg-异常:{}", e);
			}
		});
	}

	public void notifyMemberAliasMsgWithoutDB(final int type, final Long memberId, final String category,
			final String content, final String value, final String extendData) {
		es.execute(() -> {
			try {
				String alias = systemConfig.getJpushClientAlias() + memberId;
				JPushUtils.sendToClientALias(alias, content, category, value, extendData);
			} catch (Exception e) {
				logger.error("推送信息-notifyMemberMsg-异常:{}", e);
			}
		});
	}

}
