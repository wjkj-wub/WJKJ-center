package com.miqtech.master.service.user;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.user.UserFavorDao;
import com.miqtech.master.entity.user.UserFavor;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.vo.PageVO;

/**
 * 用户收藏信息service
 */
@Component
public class UserFavorService {

	@Autowired
	private UserFavorDao userFavorDao;
	@Autowired
	private QueryDao queryDao;

	/**
	 * 保存收藏信息
	 */
	public UserFavor save(UserFavor userFavor) {
		return userFavorDao.save(userFavor);
	}

	public UserFavor findByUserIdAndSubIdAndTypeAndValid(long userId, long subId, int type, int valid) {
		return userFavorDao.findByUserIdAndSubIdAndTypeAndValid(userId, subId, type, valid);
	}

	public Map<String, Object> activityFavor(Long userId, Long id, Integer type) {
		UserFavor favor = userFavorDao.findByUserIdAndSubIdAndType(userId, id, type);
		Map<String, Object> map = new HashMap<String, Object>();
		if (favor == null) {
			favor = new UserFavor();
			favor.setUserId(userId);
			favor.setSubId(id);
			favor.setType(type);
			favor.setValid(1);
			favor.setCreateDate(new Date());
			map.put("has_favor", 1);
		} else {
			favor.setValid(favor.getValid() == 0 ? 1 : 0);
			if (favor.getValid() == 1) {
				map.put("has_favor", 1);
			} else {
				map.put("has_favor", 0);
			}
		}
		userFavorDao.save(favor);
		return map;
	}

	public PageVO myActivityFavor(String userId, Integer page, Integer pageSize) {
		PageVO vo = new PageVO();
		String sql = SqlJoiner.join(
				"select count(1) from user_r_favor where is_valid=1 and (type=3 or type=5) and user_id=", userId);
		Number total = queryDao.query(sql);
		if (total != null) {
			vo.setTotal(total.intValue());
		}
		if (page == null) {
			page = 1;
		}
		if (pageSize == null) {
			pageSize = PageUtils.API_DEFAULT_PAGE_SIZE;
		}
		if (total.intValue() <= page * pageSize) {
			vo.setIsLast(1);
		}
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("start", (page - 1) * pageSize);
		params.put("pageSize", pageSize);
		sql = SqlJoiner
				.join("SELECT b.id,1 type,a.create_date, title, icon, start_time, end_time, IF ( begin_time <= NOW() AND NOW() < ADDDATE(over_time, INTERVAL 1 DAY), 1, IF ( NOW() < begin_time, 2, IF ( start_time <= NOW() AND NOW() < ADDDATE(end_time, INTERVAL 1 DAY), 5, IF (NOW() > end_time, 4, 3)))) status FROM ( user_r_favor a, activity_t_info b ) WHERE a.is_valid = 1 AND b.is_valid = 1 AND a.type = 3 AND a.sub_id = b.id AND a.user_id =",
						userId,
						" UNION ALL SELECT b.id,2 type,a.create_date, title, c.icon, start_date start_time, end_date end_time, IF ( now() < apply_start, 2, IF ( now() >= apply_start AND now() < start_date, 1, IF ( now() >= start_date AND now() < end_date, 5, 4 ))) status FROM ( user_r_favor a, amuse_t_activity b, amuse_r_activity_icon c ) WHERE a.is_valid = 1 AND b.is_valid = 1 AND a.type = 5 AND a.sub_id = b.id AND b.id = c.activity_id AND c.is_main = 1 AND c.is_valid = 1 AND a.user_id =",
						userId, " ORDER BY create_date DESC limit :start,:pageSize");
		vo.setList(queryDao.queryMap(sql, params));
		return vo;
	}
}