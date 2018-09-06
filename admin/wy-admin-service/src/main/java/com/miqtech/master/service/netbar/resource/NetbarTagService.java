package com.miqtech.master.service.netbar.resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.netbar.NetbarTagDao;
import com.miqtech.master.entity.netbar.NetbarTag;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.vo.PageVO;

@Service
public class NetbarTagService {
	@Autowired
	private QueryDao queryDao;
	@Autowired
	private NetbarTagDao netbarDao;

	public PageVO getTagList(String name, Integer page) {
		String totalSql = "select count(*) from netbar_tag a where is_valid=1 and is_publish=1";
		String querySql = "";
		if (StringUtils.isNotBlank(name)) {
			querySql += " and a.name like '%" + name + "%'";
		}
		totalSql += querySql;
		Number count = queryDao.query(totalSql);
		if (count != null && count.intValue() > 0) {
			int start = (page - 1) * PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
			String limitSql = " limit " + start + "," + PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
			String sql = "select a.id ,a.name,a.level,count(distinct b.id) adoptCount from netbar_tag a left join netbar_r_comment_tag b on a.id=b.tag_id and b.is_valid=1 where a.is_valid=1 and a.is_publish=1 ";
			sql += querySql + " group by a.id" + limitSql;
			List<Map<String, Object>> tagList = queryDao.queryMap(sql);
			PageVO vo = new PageVO(tagList);
			if (page * PageUtils.ADMIN_DEFAULT_PAGE_SIZE >= count.intValue()) {
				vo.setTotal(1);
			}
			vo.setTotal(count.intValue());
			return vo;
		}
		return new PageVO(new ArrayList<>());
	}

	public NetbarTag fingById(Long id) {
		return netbarDao.findOne(id);
	}

	public NetbarTag save(NetbarTag netbarTag) {
		return netbarDao.save(netbarTag);
	}

	public List<NetbarTag> save(List<NetbarTag> list) {
		return (List<NetbarTag>) netbarDao.save(list);
	}

	public List<NetbarTag> findAll(String ids) {
		String querySql = "";
		if (StringUtils.isNotBlank(ids)) {
			querySql = " and id in (" + ids + ")";
		}
		String sql = "select * from netbar_tag where is_valid=1 and is_publish=1" + querySql;
		return queryDao.queryObject(sql, NetbarTag.class);
	}

}
