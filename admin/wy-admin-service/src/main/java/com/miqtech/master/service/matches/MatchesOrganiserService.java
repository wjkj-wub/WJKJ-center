package com.miqtech.master.service.matches;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.matches.MatchesOrganiserDao;
import com.miqtech.master.entity.matches.MatchesOrganiser;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.vo.PageVO;

@Service
public class MatchesOrganiserService {
	@Autowired
	private QueryDao queryDao;
	@Autowired
	private MatchesOrganiserDao matchesOrganiserDao;

	public PageVO getOrganiserList(Integer page) {
		String totalSql = "select count(*) from matches_organiser where is_valid=1";
		Number count = queryDao.query(totalSql);
		if (count != null && count.intValue() > 0) {
			int start = (page - 1) * PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
			String limitSql = " limit " + start + "," + PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
			String sql = "select * from matches_organiser where is_valid=1 order by create_date desc";
			sql += limitSql;
			List<Map<String, Object>> organiserList = queryDao.queryMap(sql);
			PageVO vo = new PageVO(organiserList);
			if (page * PageUtils.ADMIN_DEFAULT_PAGE_SIZE >= count.intValue()) {
				vo.setIsLast(1);
			}
			vo.setTotal(count.intValue());
			return vo;
		}
		return new PageVO(new ArrayList<>());
	}

	public MatchesOrganiser save(MatchesOrganiser matchesOrganiser) {
		return matchesOrganiserDao.save(matchesOrganiser);
	}

	public List<MatchesOrganiser> getOrganiserList() {
		return matchesOrganiserDao.findByValid(1);
	}

	public MatchesOrganiser findOne(Long organiserId) {
		return matchesOrganiserDao.findOne(organiserId);
	}
}
