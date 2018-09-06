package com.miqtech.master.admin.web.task;

import java.util.Date;
import java.util.Iterator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.entity.bounty.Bounty;
import com.miqtech.master.service.bounty.BountyService;

@Component
public class BountyTask {

	@Autowired
	private BountyService bountyService;

	//	@Scheduled(cron = "0 0/5 * * * ?")
	public void publishActivityInfo() {
		Iterable<Bounty> lists = bountyService.findAll();
		Iterator<Bounty> it = lists.iterator();
		Date nowDate = new Date();
		Bounty bounty = new Bounty();
		while (it.hasNext()) {
			bounty = it.next();
			//下架已经结束但未下架悬赏令
			if (bounty.getEndTime().before(nowDate) && bounty.getIsPublish() == 1) {
				bounty.setIsPublish(0);
				bountyService.save(bounty);
			}
			//上架到达时间但未上架的悬赏令
			if (bounty.getEndTime().after(nowDate) && bounty.getStartTime().before(nowDate) && bounty.getStatus() == 0
					&& bounty.getIsPublish() == 0) {
				bounty.setIsPublish(1);
				bountyService.save(bounty);
			}
		}
	}
}
