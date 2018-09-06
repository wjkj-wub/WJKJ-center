package com.miqtech.master.service.msg;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.msg.MsgPushLogDao;
import com.miqtech.master.entity.msg.MsgPushLog;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.vo.PageVO;

@Component
public class MsgPushLogService {
	@Autowired
	private QueryDao queryDao;
	@Autowired
	private MsgPushLogDao msgPushLogDao;

	public PageVO page(Map<String, Object> params) {
		PageVO vo = new PageVO();

		String countSql = " select count(1) from msg_push_log mpl     ";
		String querySql = " select mpl.id,mpl.title,mpl.content,mpl.client_type  clientType,mpl.client_info clientInfo,mpl.create_date createDate,mpl.info_title infoTitle,mpl.info_type infoType,mpl.module_name moduleName,mpl.sub_module_name subModuleName	"
				+ "	from msg_push_log mpl 	";
		String whereSql = " where mpl.is_valid=1 ";
		String orderSql = " order by mpl.create_date desc  ";
		String groupbySql = " group by  mpl.id  ";
		String leftJoin = "	";
		if (params.containsKey("infoType")) {
			int infoType = NumberUtils.toInt(params.get("infoType").toString());
			switch (infoType) {//1系统 2资讯 3官方赛事 4 娱乐赛 5 约战 6 金币商城 7 金币商城商品 8 金币任务9悬赏令
			case 1:
				whereSql += " and info_type=1";
				break;
			case 2:
				whereSql += " and info_type=2";
				break;
			case 3:
				whereSql += " and info_type=3";
				break;
			case 4:
				whereSql += " and info_type=4";
				break;
			case 5:
				whereSql += " and info_type=5";
				break;
			case 6:
				whereSql += " and info_type=6";
				break;
			case 7:
				whereSql += " and info_type=7";
				break;
			case 8:
				whereSql += " and info_type=8";
				break;
			case 9:
				whereSql += " and info_type=9";
				break;
			default:
				break;
			}
		}
		if (params.containsKey("title")) {
			whereSql = whereSql + " and mpl.title like '%" + params.get("title") + "%'";
		}

		if (params.containsKey("startDate") && params.containsKey("endDate")) {
			whereSql = whereSql + "	and mpl.create_date>='" + params.get("startDate") + "'	"
					+ "	and mpl.create_date<='" + params.get("endDate") + "' 	";
		}

		countSql = " select count(1) from (" + countSql + leftJoin + whereSql + groupbySql + ") a";
		int page = NumberUtils.toInt(params.get("page").toString());
		Number totalCount = queryDao.query(countSql);

		if (totalCount == null || totalCount.intValue() <= 0) {
			vo.setTotal(0);
			vo.setCurrentPage(page);
			vo.setIsLast(1);
			return vo;
		}

		int start = PageUtils.calcStart(page);
		String limitSql = "limit " + start + "," + PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		querySql = querySql + leftJoin + whereSql + groupbySql + orderSql + limitSql;
		List<Map<String, Object>> queryMap = queryDao.queryMap(querySql);
		if (CollectionUtils.isNotEmpty(queryMap)) {
			vo.setList(queryMap);
		}
		vo.setTotal(totalCount.longValue());
		vo.setCurrentPage(page);
		vo.setIsLast(PageUtils.isBottom(page, totalCount.longValue()));
		return vo;
	}

	public void save(MsgPushLog log) {
		msgPushLogDao.save(log);

	}
}