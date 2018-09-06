package com.miqtech.master.service.system;

import java.util.TimerTask;

import org.springframework.stereotype.Component;

import com.miqtech.master.thirdparty.service.JpushService;

@Component
public class PushService extends TimerTask {
	private int way;
	private String title;
	private String content;
	private Long id;

	public PushService() {
		super();
	}

	public PushService(int way, String title, String content, Long id) {
		super();
		this.way = way;
		this.title = title;
		this.content = content;
		this.id = id;
	}

	@Override
	public void run() {
		JpushService msgOperateService = new JpushService();
		if (0 == way) { //广播（所有人）
			msgOperateService.notifyAllSysMsg("广播", title, content, true);
		} else if (1 == way) { //单个用户
			msgOperateService.notifyMemberAliasMsg(0, id, "单个用户", title, content, true, null);
		} else if (2 == way) { //全体用户
			msgOperateService.notifyMemberTagMsg("全体用户", title, content, true, null);
			//		} else if (3 == way) { //单个商户
			//			msgOperateService.notifyMerchantAliasMsg(0, id, "单个商户", title, content, true, null);
			//		} else if (4 == way) { //全体商户
			//			msgOperateService.notifyMerchantTagMsg("全体商户", title, content, true, null);
		}
	}

}
