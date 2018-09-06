package com.miqtech.master.service.matches;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.matches.MatchesLeagueDao;
import com.miqtech.master.entity.matches.MatchesLeague;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.vo.PageVO;

@Service
public class MatchesLeagueService {
	@Autowired
	private MatchesLeagueDao matchesLeagueDao;
	@Autowired
	private QueryDao queryDao;

	public MatchesLeague save(MatchesLeague matchesLeague) {
		return matchesLeagueDao.save(matchesLeague);
	}

	public List<MatchesLeague> save(List<MatchesLeague> list) {
		return (List<MatchesLeague>) matchesLeagueDao.save(list);
	}

	public PageVO getLeagueList(Integer page, Long organiserId) {
		String totalSql = "select count(*) from matches_league where organiser_id=" + organiserId + " and is_valid=1";
		Number count = queryDao.query(totalSql);
		if (count != null && count.intValue() > 0) {
			int start = (page - 1) * PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
			String limitSql = " limit " + start + "," + PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
			String sql = "SELECT a.*, b.NAME gameName FROM matches_league a LEFT JOIN activity_r_items b ON a.items_id = b.id AND b.is_valid = 1 WHERE a.organiser_id = "
					+ organiserId + " AND a.is_valid = 1 order by a.create_date desc";
			sql += limitSql;
			List<Map<String, Object>> list = queryDao.queryMap(sql);
			PageVO vo = new PageVO(list);
			if (page * PageUtils.ADMIN_DEFAULT_PAGE_SIZE >= count.intValue()) {
				vo.setIsLast(1);
			}
			vo.setTotal(count.intValue());
			return vo;
		}
		return new PageVO(new ArrayList<>());
	}

	/**
	 * 根据主办方和游戏获取赛事列表
	 */
	public List<MatchesLeague> getLeagueListByItemsAndOrgan(Long itemsId, Long organiserId) {
		return matchesLeagueDao.findByItemsIdAndOrganiserIdAndValid(itemsId, organiserId, 1);
	}

	/**
	 * 返回该主办方下所有的赛事
	 * 
	 * @param organiserId
	 * @return
	 */
	public List<MatchesLeague> findByOrganiserId(Long organiserId) {
		return matchesLeagueDao.findByOrganiserId(organiserId);
	}

	public MatchesLeague findById(Long id) {
		return matchesLeagueDao.findOne(id);
	}
}
