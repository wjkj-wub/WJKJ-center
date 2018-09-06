package com.miqtech.master.service.event;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.consts.InformationConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.entity.user.UserFavor;
import com.miqtech.master.service.common.StringRedisOperateService;
import com.miqtech.master.service.user.UserFavorService;
import com.miqtech.master.vo.PageVO;

@Component
public class EventInformationService {

	@Autowired
	private QueryDao queryDao;
	@Autowired
	private StringRedisOperateService stringRedisOperateService;
	@Autowired
	private UserFavorService userFavorService;

	/**自发赛资讯列表
	 * @param page
	 * @param pageSize
	 * @param infoCount
	 * @author renchen
	 * @return
	 */
	public PageVO infoList(Integer page, Integer pageSize, Integer infoCount, Long roundId) {
		PageVO vo = new PageVO();
		String sql = "";
		sql = "select count(1) from oet_event_information where is_valid=1";
		Number totalCount = queryDao.query(sql);
		if (page * pageSize >= totalCount.intValue()) {
			vo.setIsLast(1);
		}
		vo.setTotal(totalCount.intValue());
		int limitStart;
		if (infoCount == 0) {
			limitStart = (page - 1) * pageSize;
		} else {
			limitStart = (page - 1) * pageSize + infoCount;
		}
		if (totalCount.intValue() > 0) {
			sql = "select concat('oet',aa.id) id, aa.type, aa.title, aa.icon, aa.read_num,aa.brief "
					+ " from oet_event_information aa "
					+ " where  aa.is_valid = 1 and aa.event_id in (select oer.event_id from oet_event_round oer where oer.id=:roundId ) order by aa.is_top DESC, aa.top_time DESC,aa.timer_date DESC,aa.create_date desc limit "
					+ " :limitStart,:pageSize";
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("limitStart", limitStart);
			params.put("pageSize", pageSize);
			params.put("roundId", roundId);
			List<Map<String, Object>> lists = queryDao.queryMap(sql, params);
			vo.setList(lists);
		}
		return vo;
	}

	/**自发赛资讯详情
	 * @param infoId
	 * @param isMp4
	 * @param isShare
	 * @param userId
	 * @author renchen
	 * @return
	 */
	public Map<String, Object> detail(Long infoId, boolean isMp4, boolean isShare, Long userId) {
		String sql = "";
		String videoUrl = " concat('http://img.wangyuhudong.com/',replace(a.video_url,'mp4','m3u8')) video_url ";
		sql = "SELECT a.cover,a.brief,a.keyword,a.icon,a.timer_date, concat('oet',a.id) id, a.read_num, a.remark, a.title, a.type, ifnull(b.favNum, 0) favNum, IF (d.id IS NULL, 0, 1) faved ,"
				+ " group_concat(c.img ORDER BY c.img) AS imgs, group_concat( c.remark ORDER BY c.img SEPARATOR '|||' ) AS introduces ,a.source,"
				+ videoUrl
				+ " FROM oet_event_information a left join (select count(1) favNum,sub_id from user_r_favor where is_valid = 1 and type=6 and sub_id ="
				+ infoId
				+ ")b on b.sub_id=a.id LEFT JOIN"
				+ " user_r_favor d ON a.id = d.sub_id AND d.type = 6 AND d.is_valid = 1 AND d.user_id ="
				+ userId
				+ " LEFT JOIN oet_event_information_img c ON a.id = c.information_id AND c.is_valid = 1 where a.id="
				+ infoId;
		Map<String, Object> map = queryDao.querySingleMap(sql);
		Map<String, Object> result = new HashMap<>();
		result.put("info", map);
		result.put("upDown", upDown(userId, infoId));
		return result;
	}

	//自发赛资讯readnum
	public void upOne(Long infoId) {
		String sql = "UPDATE oet_event_information set read_num = read_num+1 where id=" + infoId;
		queryDao.update(sql);
	}

	//顶或踩数据结构
	public Map<String, Object> upDown(Long userId, Long id) {
		Map<String, Object> upDown = new HashMap<String, Object>();
		String state = stringRedisOperateService.getData(InformationConstant.OET_USER_KEY + id + "_" + userId);
		Integer upTotalInt = 0;
		String upTotal = stringRedisOperateService.getData(InformationConstant.UP_OET_TOTAL_KEY + id);
		Integer downTotalInt = 0;
		String downTotal = stringRedisOperateService.getData(InformationConstant.DOWM_OET_TOTAL_KEY + id);
		if (upTotal != null) {
			upTotalInt = NumberUtils.toInt(upTotal);
		}
		if (downTotal != null) {
			downTotalInt = NumberUtils.toInt(downTotal);
		}
		upDown.put("state", state == null ? 0 : state);
		upDown.put("upTotal", upTotalInt);
		upDown.put("downTotal", downTotalInt);
		if (upTotalInt == 0 && downTotalInt == 0) {
			upDown.put("upPercent", 0);
			upDown.put("downPercent", 0);
		} else {
			int tmp = (int) Math.round(upTotalInt * 100d / (upTotalInt + downTotalInt));
			upDown.put("upPercent", tmp);
			upDown.put("downPercent", 100 - tmp);
		}
		return upDown;
	}

	/**收藏自发赛资讯(返回 0表示取消收藏成功,1表示收藏成功)
	 * @param infoId
	 * @param userId
	 * @author renchen
	 */
	public int fav(String infoId, long userId) {
		int result = -1;
		long infoIdLong = NumberUtils.toLong(infoId);
		UserFavor favor = userFavorService.findByUserIdAndSubIdAndTypeAndValid(userId, infoIdLong, 6, 1);
		if (favor != null) {
			if (favor.getValid() == 0) {
				favor.setValid(1);
				result = 1;
			} else {
				favor.setValid(0);
				result = 0;
			}
			userFavorService.save(favor);
		} else {
			favor = new UserFavor();
			favor.setCreateDate(new Date());
			favor.setSubId(infoIdLong);
			favor.setType(6);
			favor.setValid(1);
			favor.setUserId(userId);
			userFavorService.save(favor);
			result = 1;
		}
		return result;
	}

	/**
	 * 
	 */
}
