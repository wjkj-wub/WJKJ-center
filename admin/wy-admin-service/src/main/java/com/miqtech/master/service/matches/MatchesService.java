package com.miqtech.master.service.matches;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.matches.MatchesDao;
import com.miqtech.master.entity.matches.Matches;
import com.miqtech.master.utils.DateUtils;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.vo.PageVO;

@Service
public class MatchesService {
	@Autowired
	private QueryDao queryDao;
	@Autowired
	private MatchesDao matchesDao;

	public PageVO getMatchesList(Integer page, Map<String, Object> params) throws ParseException {
		String totalSql = "select count(*) from (select id,title,organiser,sum(cenueCount) as cenueCount,gameId,organiserId,sum(provinceCount) as provinceCount,min(start_date "
				+ ") as start_date,max(end_date) as end_date,gameName,if(GROUP_CONCAT(process)<>'',GROUP_CONCAT(process),max(case when end_date<now() then processName else null end)) as process,state "
				+ " from (SELECT a.id,a.title,f.name as organiser,f.id as organiserId,b.start_date,b.end_date,a.items_id,count(c.id) AS cenueCount,case when NOW()>=b.start_date and b.end_date>=NOW() then b.NAME else null end as process,count(distinct left(d.area_code,2)) as provinceCount,if(a.update_date<>'',a.update_date,a.create_date) as date,e.name gameName,e.id gameId,b. NAME processName,a.state  FROM "
				+ " matches a LEFT JOIN matches_process b ON a.id = b.match_id AND b.is_valid = 1 LEFT JOIN matches_cenue c ON b.id = c.match_process_id  AND c.is_valid = 1 "
				+ " LEFT JOIN netbar_t_info d ON c.netbar_id = d.id AND d.is_valid = 1 left join activity_r_items e on a.items_id=e.id and e.is_valid "
				+ " left join matches_organiser f on a.organiser_id=f.id and f.is_valid=1 where a.is_valid = 1 "
				+ " group by a.id,b.id order by b.start_date asc)aa where 1=1 ";
		String querySql = "";
		if (params.get("title") != null) {
			String value = "%" + params.get("title") + "%";
			querySql += " and title like '" + value + "'";
		}
		if (params.get("organiserId") != null) {
			querySql += " and organiserId=" + params.get("organiserId");
		}
		if (params.get("itemsId") != null) {
			querySql += " and gameId=" + params.get("itemsId");
		}
		if (params.get("state") != null) {
			querySql += " and state=" + params.get("state");
		}
		if (params.get("beginDate") != null) {
			String beginDate = params.get("beginDate").toString();
			Date begin = DateUtils.stringToDateYyyyMMdd(beginDate);
			String beginString = DateUtils.dateToString(begin, "yyyy-MM-dd");
			querySql += " and start_date>='" + beginString + "'";
		}
		if (params.get("endDate") != null) {
			String endDate = params.get("endDate").toString();
			Date end = DateUtils.stringToDateYyyyMMdd(endDate);
			String endString = DateUtils.dateToString(end, "yyyy-MM-dd");
			querySql += " and end_date<='" + endString + "'";
		}
		totalSql += querySql + " group  by id)bb";
		Number totalCount = queryDao.query(totalSql);
		if (totalCount != null && totalCount.intValue() > 0) {
			int pageSize = PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
			int start = (page - 1) * pageSize;
			String limitSql = " limit " + start + "," + pageSize;
			String sql = "select id,title,organiser,sum(cenueCount) as cenueCount,gameId,organiserId,sum(provinceCount) as provinceCount,min(start_date "
					+ ") as start_date,max(end_date) as end_date,gameName,if(GROUP_CONCAT(process)<>'',GROUP_CONCAT(process),max(case when end_date<now() then processName else null end)) as process,state "
					+ " from (SELECT a.id,a.title,f.name as organiser,f.id as organiserId,b.start_date,b.end_date,a.items_id,count(c.id) AS cenueCount,case when NOW()>=b.start_date and b.end_date>=NOW() then b.NAME else null end as process,count(distinct left(d.area_code,2)) as provinceCount,if(a.update_date<>'',a.update_date,a.create_date) as date,e.name gameName,e.id gameId,b. NAME processName,a.state  FROM "
					+ " matches a LEFT JOIN matches_process b ON a.id = b.match_id AND b.is_valid = 1 LEFT JOIN matches_cenue c ON b.id = c.match_process_id  AND c.is_valid = 1 "
					+ " LEFT JOIN netbar_t_info d ON c.netbar_id = d.id AND d.is_valid = 1 left join activity_r_items e on a.items_id=e.id and e.is_valid "
					+ " left join matches_organiser f on a.organiser_id=f.id and f.is_valid=1 where a.is_valid = 1 "
					+ " group by a.id,b.id order by b.start_date asc)aa where 1=1 ";
			sql += querySql + " group  by id order by date desc" + limitSql;
			PageVO vo = new PageVO(queryDao.queryMap(sql));
			if (page * pageSize >= totalCount.intValue()) {
				vo.setIsLast(1);
			}
			vo.setTotal(totalCount.intValue());
			return vo;
		}
		return new PageVO(new ArrayList<>());
	}

	public Matches save(Matches matches) {
		return matchesDao.save(matches);
	}

	public Matches findById(Long id) {
		return matchesDao.findOne(id);
	}

	/**
	 * 查询所有赛事名
	 */
	public List<Map<String, Object>> getAllMatchesInfoList() {
		String sql = "select * from matches where is_valid=1 and state=1";
		return queryDao.queryMap(sql);
	}

	/**
	 * 查询所有海选赛
	 */
	public List<Map<String, Object>> getAllAuditionInfoList() {
		String sql = "select id,name from audition where is_valid=1 and is_release=1";
		return queryDao.queryMap(sql);
	}

	/**
	 * 查询赛事名、游戏名、主办方名
	 *
	 * @param mathchId
	 */
	public Map<String, Object> getInfo(Long mathchId) {
		String sql = "select a.title,b.name as organiserName,c.name as itemName from matches a left join matches_organiser b on a.organiser_id=b.id and b.is_valid=1 left join activity_r_items c on a.items_id=c.id and c.is_valid=1 where a.id="
				+ mathchId + " and a.is_valid=1";
		return queryDao.queryMap(sql).get(0);
	}

	public List<Map<String, Object>> findAll() {
		String sql = "select id,title from matches where is_valid =1";
		return queryDao.queryMap(sql);
	}

}
