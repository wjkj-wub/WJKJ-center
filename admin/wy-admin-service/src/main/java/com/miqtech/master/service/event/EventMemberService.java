package com.miqtech.master.service.event;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.event.EventMemberDao;
import com.miqtech.master.entity.event.EventMember;

@Component
public class EventMemberService {

	@Autowired
	private QueryDao queryDao;
	@Autowired
	private EventMemberDao eventMemberDao;
	@Autowired
	private EventGroupSeatService eventGroupSeatService;

	public Map<String, String> submitPersonalApply(Long userId, String name, String telephone, String idCard, String qq,
			String labor, Long round) {
		Map<String, String> result = new HashMap<String, String>();
		if (isApply(userId, round)) {
			result.put("result", "repeat");
			return result;
		}
		EventMember eventMember = new EventMember();
		eventMember.setCreateUserId(userId);
		eventMember.setTeamId(0L);// 表示个人
		eventMember.setUserId(userId);
		eventMember.setRoundId(round);
		eventMember.setName(name);
		eventMember.setIdcard(idCard);
		eventMember.setTelephone(telephone);
		eventMember.setQq(qq);
		eventMember.setLabor(labor);
		eventMember.setIsMonitor((byte) 0);
		eventMember.setIsEnter((byte) 1);
		eventMember.setValid(1);
		eventMember.setCreateDate(new Date());
		EventMember eventMemberSave = eventMemberDao.save(eventMember);
		if(!eventGroupSeatService.saveTargetByAdd(round, eventMemberSave.getId())){
			result.put("result", "number out limit");
			return result;
		};
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		result.put("result", "success");
		return result;
	}

	/**是否报名
	 * @param userId
	 * @param round
	 * @return boolean
	 */
	public boolean isApply(Long userId, Long roundId) {
		String sql = "select count(1) from oet_event_member where user_id=" + userId + " and round_id=" + roundId
				+ " and is_valid=1";
		Number count = queryDao.query(sql);
		if (count.intValue() > 0) {
			return true;
		} else {
			return false;
		}
	}

}
