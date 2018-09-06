package com.miqtech.master.service.matches;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.matches.MatchesCenueDao;
import com.miqtech.master.entity.matches.MatchesCenue;

@Service
public class MatchesCenueService {

	@Autowired
	private QueryDao queryDao;
	@Autowired
	private MatchesCenueDao matchesCenueDao;

	public List<Map<String, Object>> findByProcessId(Long processId) {
		String sql = "select id,netbar_id,REPLACE(GROUP_CONCAT(DATE_FORMAT(fight_date,'%Y-%m-%d')),',','；') as fightDates,division from matches_cenue where match_process_id="
				+ processId + " and is_valid=1 group by netbar_id";
		return queryDao.queryMap(sql);
	}

	public List<MatchesCenue> findEntityByProcessId(Long processId) {
		String sql = "select * from matches_cenue where match_process_id=" + processId + " and is_valid=1";
		return queryDao.queryObject(sql, MatchesCenue.class);
	}

	public List<MatchesCenue> save(List<MatchesCenue> list) {
		return (List<MatchesCenue>) matchesCenueDao.save(list);
	}

	public int getCenueCountByMatchesId(Long matchesId) {
		String sql = "select count(distinct a.id) from matches_cenue a left join matches_process b on a.match_process_id=b.id where a.is_valid=1 and b.is_valid=1 and b.match_id="
				+ matchesId;
		Number count = queryDao.query(sql);
		if (count == null || count.intValue() < 0) {
			return 0;
		}
		return count.intValue();
	}
}
