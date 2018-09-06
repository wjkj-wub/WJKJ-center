package com.miqtech.master.service.netbar;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.netbar.NetbarCommentDao;
import com.miqtech.master.entity.netbar.NetbarComment;

/**
 * 网吧评论service
 */
@Component
public class NetbarCommentService {

	@Autowired
	private QueryDao queryDao;
	@Autowired
	private NetbarCommentDao netbarCommentDao;

	public NetbarComment findById(Long id) {
		return netbarCommentDao.findOne(id);
	}

	public void updateNetBarComment() {
		StringBuilder sql = new StringBuilder();
		sql.append(
				"select t.netbar_id ,group_concat(tag.name) commentName from (select   a.*,  if(    @a = netbar_id,    @rank \\:= @rank + 1,    @rank \\:= 1  ) as pm,")
				.append(" @a \\:= a.netbar_id from  (select     count(1) allNum,    nrct.tag_id,    nrc.netbar_id   from")
				.append("  master.netbar_r_comment_tag nrct     left join netbar_r_comment nrc       on nrct.netbar_comment_id = nrc.id ")
				.append(" group by nrc.netbar_id,    nrct.tag_id   order by netbar_id asc,    allNum desc) a,")
				.append(" (select     @rank \\:= 0,    @a \\:= null) B order by a.netbar_id desc,  a.allNum desc ")
				.append(" )  t left join netbar_tag tag on tag.id = t.tag_id  where t.pm<4  group by  netbar_id order by netbar_id  asc,pm asc ");
		List<Map<String, Object>> lists = queryDao.queryMap(sql.toString());
		for (Map<String, Object> list : lists) {
			queryDao.update("update netbar_t_info set tag='" + list.get("commentName").toString() + "' where id="
					+ list.get("netbar_id").toString());
		}
	}

	public void updateNetBarScore() {
		StringBuilder sql = new StringBuilder();
		sql.append("select round(sum(score)/count(1),2) avgScore ,netbar_id from netbar_r_comment group by netbar_id");
		List<Map<String, Object>> lists = queryDao.queryMap(sql.toString());
		for (Map<String, Object> list : lists) {
			queryDao.update("update netbar_t_info set score=" + list.get("avgScore").toString() + " where id="
					+ list.get("netbar_id").toString());
		}
	}
}