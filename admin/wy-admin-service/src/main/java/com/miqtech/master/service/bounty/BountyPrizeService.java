package com.miqtech.master.service.bounty;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.bounty.BountyPrizeDao;
import com.miqtech.master.entity.bounty.BountyPrize;

@Component
public class BountyPrizeService {

	@Autowired
	private BountyPrizeDao bountyPrizeDao;
	@Autowired
	private QueryDao queryDao;

	public BountyPrize findById(Long id) {
		return bountyPrizeDao.findOne(id);
	}

	public BountyPrize findValidByBountyId(Long bountyId) {
		if (bountyId == null) {
			return null;
		}

		return bountyPrizeDao.findByBountyIdAndValid(bountyId, CommonConstant.INT_BOOLEAN_TRUE);
	}

	public List<BountyPrize> batchInsertOrUpdate(List<BountyPrize> prizes) {
		if (CollectionUtils.isEmpty(prizes)) {
			return null;
		}

		Date now = new Date();
		for (BountyPrize p : prizes) {
			p.setUpdateDate(now);
			if (p.getId() == null) {
				p.setCreateDate(now);
				p.setValid(CommonConstant.INT_BOOLEAN_TRUE);
			}
		}

		return (List<BountyPrize>) bountyPrizeDao.save(prizes);
	}

	public BountyPrize save(BountyPrize bountyPrize) {
		return bountyPrizeDao.save(bountyPrize);
	}

	public Map<String, Object> prizeInfo(Long userId, Long bountyId) {
		String sql = "select t.id historyId, bz.award_type,bz.award_sub_type,bz.award_name, b.type,b.title,t.tran_no,t.create_date, t.status,t.user_id,bz.bounty_id,utf.nickname,utf.icon,bg.grade from"
				+ "(select mrch.id,mrch.create_date, mrch.commodity_id,mrch.user_id,if(isnull(mrui.id),-1,mrch.status) status,tran_no from mall_r_commodity_history mrch left JOIN mall_r_user_info mrui ON mrui.history_id = mrch.id where commodity_source=3 and mrch.is_valid=1) t "
				+ "left join bounty_prize bz on bz.id=t.commodity_id "
				+ "left join user_t_info utf on utf.id=t.user_id "
				+ "join bounty_grade bg on bg.user_id=t.user_id and bz.bounty_id=bg.bounty_id and bg.state=3 "
				+ "join bounty b on b.id=bz.bounty_id where bz.bounty_id=" + bountyId
				+ " order by bz.id asc,bg.create_date asc";
		List<Map<String, Object>> info = queryDao.queryMap(sql);
		Map<String, Object> mid = new HashMap<>();
		Boolean awardType = Boolean.valueOf(Boolean.TRUE); //商品类型
		Calendar cal = Calendar.getInstance();
		Integer day = 0;
		String awardSubType = "";
		if (!info.isEmpty()) {
			for (int i = 0; i < info.size(); i++) {
				mid = info.get(i);
				cal.setTime((Date) mid.get("create_date"));
				day = cal.get(Calendar.DAY_OF_MONTH);
				// commodity_type 1 自有商品 2实物3流量话费4虚拟充值(除去流量和话费)
				// status 0正在发放中 -1未填写信息 1发放成功 2奖励信息已过期
				awardSubType = mid.get("award_sub_type").toString(); //奖品小类型 3-实物,1-自有红包,5-自有金币,8-充值话费,6-充值流量,7-充值Q币
				awardType = mid.get("award_type").toString().equals("1");
				if (!awardType) { //非网娱自有商品
					//根据award_type 和  award_sub_type设置commodity_type
					if (awardSubType.equals("3")) {
						mid.put("commodity_type", 2);
					} else if (awardSubType.equals("8")) {
						mid.put("commodity_type", 3);
					} else if (awardSubType.equals("6")) {
						mid.put("commodity_type", 3);
					} else if (awardSubType.equals("7")) {
						mid.put("commodity_type", 4);
					} else {
						mid.put("commodity_type", 0);
					}

					if (!mid.get("status").toString().matches("(.*)(-1|1)(.*)")) {
						mid.put("status", "0");//正在发放中
					}
					if (mid.get("status").toString().matches("1")) {
						mid.put("status", "1");//发放成功
					}
					if (mid.get("status").toString().matches("-1")) { //判断未填写信息的时长
						cal.setTime(new Date());
						if ((cal.get(Calendar.DAY_OF_MONTH) - day) > 7) {
							mid.put("status", "2"); //奖励信息已过期
						}
					}
				} else { //网娱自有
					mid.put("commodity_type", 1); // commodity_type 1 自有商品
					mid.put("status", "1"); //奖励发放成功
				}
				mid.put("ranking", i + 1); //排行榜和独占鳌头
				if (mid.get("user_id").toString().equals(userId + "")) {
					if (mid.get("type").equals("1")) { //普通悬赏令
						mid.put("ranking", 1);
					}
					return mid;
				}

			}
		}
		return null;
	}
}
