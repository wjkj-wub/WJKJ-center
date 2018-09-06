package com.miqtech.master.service.mall;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.consts.mall.CommodityConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.mall.CommodityHistoryDao;
import com.miqtech.master.dao.mall.MallMsgDao;
import com.miqtech.master.entity.mall.CommodityHistory;
import com.miqtech.master.entity.mall.MallMsg;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.vo.PageVO;

@Component
public class MallMsgService {
	@Autowired
	private QueryDao queryDao;
	@Autowired
	private CommodityHistoryDao commodityHistoryDao;
	@Autowired
	private MallMsgDao mallMsgDao;

	public PageVO queryList(Integer type, String phone, String content, Integer page) {
		String sql = "";
		String typeSql = "";
		String phoneSql = "";
		String contentSql = "";
		if (type != null) {
			typeSql = " and a.type=" + String.valueOf(type);
		}
		if (StringUtils.isNotBlank(content)) {
			contentSql = " and a.content like '%" + content + "%'";
		}
		if (StringUtils.isNotBlank(phone)) {
			phoneSql = " and b.username like '%" + phone + "%'";
		}
		int pageSize = PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		if (page == null) {
			page = 1;
		}
		int start = (page - 1) * pageSize;
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("start", start);
		params.put("pageSize", pageSize);
		String limitSql = " limit :start,:pageSize";
		sql = SqlJoiner.join("select count(1) from mall_t_msg a,user_t_info b where a.user_id=b.id", typeSql,
				contentSql, phoneSql);
		PageVO vo = new PageVO();
		Number totalCount = queryDao.query(sql);
		if (totalCount != null) {
			vo.setTotal(totalCount.intValue());
		}

		if (start + pageSize >= vo.getTotal()) {
			vo.setIsLast(1);
		}
		sql = SqlJoiner.join("select a.*,b.username phone from mall_t_msg a,user_t_info b where a.user_id=b.id",
				typeSql, contentSql, phoneSql, " order by a.create_date desc", limitSql);
		vo.setCurrentPage(page);
		vo.setList(queryDao.queryMap(sql, params));
		return vo;
	}

	/**
	 * 提交用户异常请求
	 */
	public void userExceptionSubmit(long userId, long exchangeHistoryId, int type) {
		// 1.更新兑换历史状态为2-异常
		CommodityHistory commodityHistory = commodityHistoryDao.findOne(exchangeHistoryId);
		commodityHistory.setStatus(CommodityConstant.INT_STATUS_EXCEPTION);

		commodityHistoryDao.save(commodityHistory);

		// 2.增加商城（异常）消息
		MallMsg mallMsg = new MallMsg();
		mallMsg.setUserId(userId);
		mallMsg.setType(2); //1-反馈消息，2-兑换异议，3-兑换消息
		if (1 == type) {
			mallMsg.setContent("没有收到兑换的商品");
		} else if (2 == type) {
			mallMsg.setContent("兑换商品已经被使用");
		} else if (3 == type) {
			mallMsg.setContent("金币实际消耗量错误");
		}
		mallMsg.setTargetId(exchangeHistoryId);
		mallMsg.setValid(CommonConstant.INT_BOOLEAN_TRUE);
		mallMsg.setCreateDate(new Date());

		mallMsgDao.save(mallMsg);
	}

}
