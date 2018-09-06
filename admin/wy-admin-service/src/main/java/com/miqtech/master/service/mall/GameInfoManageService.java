package com.miqtech.master.service.mall;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.mall.GameInfoManageDao;
import com.miqtech.master.entity.mall.MallGameInfo;
import com.miqtech.master.utils.BeanUtils;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.vo.PageVO;

@Component
public class GameInfoManageService {
	@Autowired
	QueryDao queryDao;
	@Autowired
	GameInfoManageDao gameInfoManageDao;

	public PageVO queryGameInfo(String name, int page, Integer status) {
		PageVO vo = new PageVO();
		int pageSize = PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		int start = (page - 1) * pageSize;
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("start", start);
		params.put("pageSize", pageSize);
		String totalSql = "select count(1) from mall_game_info  a where a.is_valid=1";
		String contentSql = "select a.id,a.name,a.image,a.publish_time,a.status,a.top_status,a.game_url,a.score,a.coin,a.rule,a.game_type,a.coin_consume,a.brief_rule from mall_game_info  a where a.is_valid=1";
		if (status != null) {
			totalSql = totalSql + " and a.status = " + status;
			contentSql = contentSql + " and a.status = " + status;

		}
		if (StringUtils.isNotBlank(name)) {
			totalSql = SqlJoiner.join(totalSql, " and name like '%", name, "%'");
			contentSql = SqlJoiner.join(contentSql, " and name like '%", name,
					"%' order by a.top_status desc,a.update_date desc limit :start,:pageSize");
		} else {
			contentSql = SqlJoiner.join(contentSql,
					" order by a.top_status desc,a.update_date desc limit :start,:pageSize");
		}

		Number total = queryDao.query(totalSql);
		if (total != null) {
			vo.setTotal(total.longValue());
		}
		vo.setList(queryDao.queryMap(contentSql, params));
		return vo;
	}

	/**
	 * 根据ID查询
	 */
	public MallGameInfo findById(Long id) {
		return gameInfoManageDao.findOne(id);
	}

	/**
	 * 保存游戏信息
	 */
	public MallGameInfo saveOrUpdate(MallGameInfo item) {
		if (item != null) {
			Date now = new Date();
			item.setUpdateDate(now);
			if (item.getId() != null) {
				MallGameInfo old = findById(item.getId());
				if (old != null) {
					item = BeanUtils.updateBean(old, item);
				}
			} else {
				item.setValid(CommonConstant.INT_BOOLEAN_TRUE);
				item.setCreateDate(now);
			}
			MallGameInfo info = gameInfoManageDao.save(item);
			return info;
		}
		return null;
	}

	/*
	 * h5游戏列表
	 */
	public List<Map<String, Object>> queryListForApp() {
		Map<String, Object> params = new HashMap<String, Object>();
		String sql = "select id game_id , image ,brief_rule ,game_heat ,name ,game_url ,rule FROM mall_game_info where is_valid=1 order by top_status desc , publish_time desc";
		List<Map<String, Object>> maps = queryDao.queryMap(sql, params);
		return maps;

	}

	/*
	 * 根据id和valid查询
	 */
	public MallGameInfo findByIdAndValid(Long id, Integer valid) {
		return gameInfoManageDao.findByIdAndValid(id, valid);
	}

	/*
	 * 增加游戏次数
	 */
	public void addGameHeat(Long gameId) {
		String sql = "update master.mall_game_info set game_heat=IF(isnull(game_heat),1,game_heat+1) where id="+gameId;
		queryDao.update(sql);
	}
}
