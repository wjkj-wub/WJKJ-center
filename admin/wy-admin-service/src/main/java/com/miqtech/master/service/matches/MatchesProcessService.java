package com.miqtech.master.service.matches;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.matches.MatchesProcessDao;
import com.miqtech.master.entity.matches.MatchesProcess;

@Service
public class MatchesProcessService {
	@Autowired
	private MatchesProcessDao matchesProcessDao;
	@Autowired
	private QueryDao queryDao;

	public List<MatchesProcess> save(List<MatchesProcess> list) {
		return (List<MatchesProcess>) matchesProcessDao.save(list);
	}

	public List<Map<String, Object>> findByMatchesId(Long matchId) {
		String sql = "select * from matches_process where match_id=" + matchId + " and is_valid=1";
		return queryDao.queryMap(sql);
	}

	public MatchesProcess findById(Long id) {
		return matchesProcessDao.findOne(id);
	}
}
