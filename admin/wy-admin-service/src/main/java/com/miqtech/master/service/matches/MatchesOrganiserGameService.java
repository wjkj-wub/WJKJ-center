package com.miqtech.master.service.matches;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.matches.MatchesOrganiserGameDao;
import com.miqtech.master.entity.matches.MatchesOrganiserGame;

@Service
public class MatchesOrganiserGameService {
	@Autowired
	private MatchesOrganiserGameDao matchesOrganiserGameDao;
	@Autowired
	private QueryDao queryDao;

	public MatchesOrganiserGame save(MatchesOrganiserGame matchesOrganiserGame) {
		return matchesOrganiserGameDao.save(matchesOrganiserGame);
	}

	public List<MatchesOrganiserGame> save(List<MatchesOrganiserGame> list) {
		return (List<MatchesOrganiserGame>) matchesOrganiserGameDao.save(list);
	}

	/**
	 * 查询主办方下所有的游戏
	 * @return id游戏id name游戏名称
	 */
	public List<Map<String, Object>> getGameList(Long organiserId) {
		String sql = "select b.id,b.name from matches_organiser_game a left join activity_r_items b on a.items_id=b.id and b.is_valid=1 where a.is_valid=1 and a.organiser_id="
				+ organiserId;
		return queryDao.queryMap(sql);
	}

	/**
	 * 返回该主办方下的所有赛事游戏
	 * @param organiserId
	 * @return
	 */
	public List<MatchesOrganiserGame> findAllByOrganiserId(Long organiserId) {
		return matchesOrganiserGameDao.findByOrganiserId(organiserId);
	}

	/**
	 * 得到主办方下所有选择游戏的信息
	 * @param 主办方id
	 */
	public List<Map<String, Object>> getGameInfoList(Long organiserId) {
		String sql = "select if(isnull(t.id),1,0) nocheck,ari.id,ari.name "
				+ " from activity_r_items ari left join (select * from  matches_organiser_game mog where mog.organiser_id="
				+ organiserId + " and mog.is_valid=1) t ON ari.id = t.items_id "
				+ " where ari.is_valid=1 group by ari.id ";
		return queryDao.queryMap(sql);
	}

}
