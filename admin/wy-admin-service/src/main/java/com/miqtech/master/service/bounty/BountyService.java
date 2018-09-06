package com.miqtech.master.service.bounty;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.bounty.BountyDailyTipDao;
import com.miqtech.master.dao.bounty.BountyDao;
import com.miqtech.master.entity.bounty.Bounty;
import com.miqtech.master.entity.bounty.BountyDailyTip;
import com.miqtech.master.entity.bounty.BountyPrize;
import com.miqtech.master.utils.BeanUtils;
import com.miqtech.master.utils.DateUtils;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.vo.PageVO;

@Component
public class BountyService {

	@Autowired
	private QueryDao queryDao;
	@Autowired
	private BountyDao bountyDao;
	@Autowired
	private BountyDailyTipDao bountyDailyTipDao;
	@Autowired
	private BountyPrizeService bountyPrizeService;

	public Iterable<Bounty> findAll() {
		return bountyDao.findAll();
	}

	public Bounty findById(Long id) {
		if (id == null) {
			return null;
		}
		return bountyDao.findOne(id);
	}

	public void savList(List<Bounty> list) {
		bountyDao.save(list);
	}

	public Bounty save(Bounty bounty) {
		return bountyDao.save(bounty);
	}

	public void disable(Long id) {
		if (id == null) {
			return;
		}

		Bounty bounty = new Bounty();
		bounty.setId(id);
		bounty.setValid(CommonConstant.INT_BOOLEAN_FALSE);
		insertOrUpdate(bounty);
	}

	public Bounty insertOrUpdate(Bounty bounty) {
		if (bounty == null) {
			return null;
		}
		Map<String, Object> itemId = getRule(bounty.getItemId());
		if ((itemId != null) && itemId.get("rule") != null) {
			bounty.setRule(itemId.get("rule").toString());
		} else {
			bounty.setRule("");
		}
		Date now = new Date();
		bounty.setUpdateDate(now);
		if (bounty.getId() != null) {
			Bounty old = findById(bounty.getId());
			if (old != null) {
				bounty = BeanUtils.updateBean(old, bounty);
			}
		} else {
			bounty.setValid(CommonConstant.INT_BOOLEAN_TRUE);
			bounty.setCreateDate(now);
		}
		return bountyDao.save(bounty);
	}

	/**
	 * @param page
	 * @param type 1英雄联盟 2炉石传说 3王者荣耀
	 * @return
	 * @throws ParseException
	 */
	public PageVO getPage(Integer page, Long itemId) throws ParseException {
		StringBuilder sb = new StringBuilder(
				" SELECT b.id,if(isnull(t.applyNum),0,t.applyNum) applyNum,b.start_time startTime,b.end_time endTime,b.reward,b.target,b.status "
						+ " FROM master.bounty b "
						+ " left join (select count(distinct bg.user_id) applyNum,bg.bounty_id from bounty_grade bg group by bg.bounty_id) t on t.bounty_id=b.id where ");
		if (itemId == null) {
			itemId = 1L;
		}
		if (page == null) {
			page = 1;
		}
		sb.append(" b.item_id=" + itemId);
		sb.append(" order by b.start_time desc");
		sb.append(PageUtils.getLimitSql(page));
		List<Map<String, Object>> mids = queryDao.queryMap(sb.toString());
		int j = 0;
		Map<String, Object> t = new HashMap<>();
		for (int i = mids.size() - 1; i >= 0; i--) {
			j++;
			t = mids.get(i);
			t.put("status", statusBounty(t.get("startTime").toString(), t.get("endTime").toString(),
					t.get("status").toString()));
			t.put("time", "第" + j + "期");
		}
		String sqlCount = "select count(1) from bounty where item_id=" + itemId + " and is_valid=1";
		Number total = queryDao.query(sqlCount);
		PageVO vo = new PageVO();
		vo.setList(mids);
		vo.setTotal(total.intValue());
		vo.setCurrentPage(page);
		vo.setIsLast(PageUtils.isBottom(page, total.intValue()));
		return vo;
	}

	/**
	 * @param startTime
	 * @param endTime
	 * @return 0待进行 1进行中 2待审核 3已结束
	 * @throws ParseException
	 */
	public int statusBounty(String startTime, String endTime, String status) throws ParseException {
		Date startDate = DateUtils.stringToDate(startTime.substring(0, startTime.length() - 2),
				DateUtils.YYYY_MM_DD_HH_MM_SS);
		Date endDate = DateUtils.stringToDate(endTime.substring(0, endTime.length() - 2),
				DateUtils.YYYY_MM_DD_HH_MM_SS);
		Calendar cal = Calendar.getInstance();
		cal.setTime(endDate);
		cal.add(Calendar.DATE, -2);
		Date now = new Date();
		if (status.equals("1")) {
			return 3;
		}
		if (now.after(startDate) && now.before(cal.getTime())) {
			return 1;
		}
		if (now.after(cal.getTime())) {
			return 2;
		}
		return 0;
	}

	/**
	 * 悬赏令获奖名单
	 * @param title
	 * @return
	 */
	public PageVO prizeManList(Integer page, Integer pageSize, Integer infoCount, Long bountyId) {
		Integer limitStart;
		if (infoCount == null || infoCount == 0) {
			limitStart = (page - 1) * pageSize;
			infoCount = 0;
		} else {
			limitStart = (page - 1) * pageSize + infoCount;
		}
		Bounty bounty = bountyDao.findOne(bountyId);
		String desc = " order by bg.grade desc";
		String asc = " order by bg.grade asc";
		StringBuilder sql = new StringBuilder(
				"select t.user_id userId,bz.bounty_id bountyId,utf.id,utf.nickname,utf.icon,bg.grade,bg.img from"
						+ "(select commodity_id,user_id from mall_r_commodity_history where commodity_source=3 and is_valid=1) t "
						+ "left join bounty_prize bz on bz.id=t.commodity_id left join user_t_info utf on utf.id=t.user_id "
						+ "left join bounty_grade bg on bg.user_id=t.user_id and bz.bounty_id=bg.bounty_id and bg.state=3 where bz.bounty_id="
						+ bountyId + " and bz.is_valid=1 ");
		if (bounty.getOrderType().equals("1")) {
			sql.append(desc);
		} else {
			sql.append(asc);
		}
		sql.append(" limit :limitStart,:pageSize");
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("limitStart", limitStart);
		params.put("pageSize", pageSize);
		List<Map<String, Object>> awardMen = queryDao.queryMap(sql.toString(), params);
		String sqlstr = "SELECT count(1) FROM mall_r_commodity_history WHERE commodity_source = 3 AND is_valid = 1 and commodity_id in (select id from bounty_prize where bounty_id="
				+ bountyId + ")";
		Number total = queryDao.query(sqlstr);
		PageVO pageVO = new PageVO();
		pageVO.setList(awardMen);
		pageVO.setCurrentPage(page);
		pageVO.setIsLast(total.intValue() <= (page * pageSize + infoCount) ? 1 : 0);
		pageVO.setTotal(total.intValue());
		return pageVO;
	}

	public List<Map<String, Object>> findValidByTitle(String title) {
		String sql = "select id,title from bounty where is_valid =1 and title like '%" + title
				+ "%' and end_time>now()";
		return queryDao.queryMap(sql);
	}

	public List<Map<String, Object>> queryEventForAppRecommend() {
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		String sql = "select id, title from bounty a where a.is_valid=1  order by a.create_date desc";
		result.addAll(queryDao.queryMap(sql));
		return result;
	}

	public List<Map<String, Object>> findByItemId(Long itemId, Long bountyId) {
		if (bountyId == null) {
			bountyId = 0L;
		}
		String sql = "select * from bounty where create_date>'2016-11-30 00:00:00' and id not in (" + bountyId
				+ ") and item_id=" + itemId + " order by end_time desc";
		return queryDao.queryMap(sql);
	}

	/**
	 * @param bountyId悬赏令id
	 * @return
	 * @throws ParseException
	 */
	public Map<String, Object> bountyBrief(Long bountyId) throws ParseException {
		Bounty bounty = findById(bountyId);
		List<BountyDailyTip> tips = bountyDailyTipDao.findByBountyIdOrderByCreateDateDesc(bountyId);
		int status = statusBounty(bounty.getStartTime().toString(), bounty.getEndTime().toString(),
				bounty.getStatus().toString());
		if (status == 1) {
			bounty.setStatus(1);
		}
		int readOnly = 1;
		if (status == 0) {
			readOnly = 0;
		}
		Map<String, Object> result = new HashMap<>();
		result.put("targetStr", bounty.getTarget());
		result.put("readOnly", readOnly);
		result.put("tips", tips);
		return null;
	}

	public Map<String, Object> getRule(Long itemId) {
		String sql = "select rule from bounty where item_id=" + itemId
				+ " and is_valid=1 and create_date>'2016-11-30 00:00:00' limit 1";
		return queryDao.querySingleMap(sql);
	}

	/**
	 * 返回审核成绩 页面悬赏令基本信息
	 * @param bountyId
	 * @return
	 * @throws ParseException
	 */
	public Map<String, Object> getBountyInfo(Long bountyId) throws ParseException {
		String sql = "select b.id,b.prize_virtual_num virtualNum,b.start_time,b.end_time,b.reward,b.target,b.status,t.applyNum,if(isnull(t.winNum),0+b.prize_virtual_num,(t.winNum+b.prize_virtual_num)) winNum from bounty b left join (select bounty_id,count(1) applyNum,count(if(grade>=1,true,null)) winNum from bounty_grade bg where bg.bounty_id="
				+ bountyId + ") t on t.bounty_id=b.id where b.id=" + bountyId;
		Map<String, Object> info = queryDao.querySingleMap(sql);
		double awardNum = 0;
		if (info.get("status").toString().equals("1")) {
			BountyPrize bountyPrize = bountyPrizeService.findValidByBountyId(bountyId);
			Integer winNum = NumberUtils.toInt(info.get("winNum").toString());
			if (winNum == 0) {
				awardNum = NumberUtils.toDouble(bountyPrize.getAwardNum().toString());
			} else {
				awardNum = Math.ceil(NumberUtils.toDouble(bountyPrize.getAwardNum().toString())
						/ NumberUtils.toInt(info.get("winNum").toString()));
			}
			info.put("awardNum", awardNum);
		} else {
			info.put("awardNum", awardNum);
		}
		String startStr = info.get("start_time").toString();
		String endStr = info.get("end_time").toString();
		info.put("status", statusBounty(startStr, endStr, info.get("status").toString()));
		if (info != null) {
			String regEx = "[^0-9]";
			Pattern p = Pattern.compile(regEx);
			Matcher m = p.matcher(info.get("reward").toString());
			Long reward = NumberUtils.toLong(m.replaceAll("").trim());
			info.put("reward", reward + "");
		}
		return info;
	}

}
