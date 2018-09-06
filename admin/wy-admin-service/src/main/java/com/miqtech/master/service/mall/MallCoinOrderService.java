package com.miqtech.master.service.mall;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.utils.DateUtils;
import com.miqtech.master.utils.ExcelUtils;
import com.miqtech.master.vo.PageVO;

import jxl.write.WriteException;

@Service
public class MallCoinOrderService {
	@Autowired
	private QueryDao queryDao;

	public PageVO getList(String telephone, String createDate, String updateDate, String type, boolean export,
			Integer page, Integer pageSize) throws ParseException {
		String querySql = "";
		if (StringUtils.isNotBlank(telephone)) {
			querySql += " and uti.telephone='" + telephone + "'";
		}
		if (StringUtils.isNotBlank(createDate)) {
			querySql += " and mco.create_date>='" + DateUtils.stampToDate(createDate, DateUtils.YYYY_MM_DD_HH_MM_SS)
					+ "'";
		}
		if (StringUtils.isNotBlank(updateDate)) {
			querySql += " and mco.update_date<='" + DateUtils.stampToDate(updateDate, DateUtils.YYYY_MM_DD_HH_MM_SS)
					+ "'";
		}
		if (StringUtils.isNotBlank(type)) {
			querySql += " and mco.type=" + type;
		}
		String countSql = "select count(mco.id) from mall_coin_order mco left join user_t_info uti on mco.user_id=uti.id where uti.is_valid=1"
				+ querySql;
		String limitSql = "";
		Number count = queryDao.query(countSql);
		if (count != null && count.intValue() > 0) {
			String sql = "select mco.id,mco.user_id,uti.telephone,mco.pay_amount,mco.obtain_amount,mco.coin_count,mco.create_date,mco.update_date, if(mco.type=1,'支付宝',if(mco.type=2,'微信','ios内购')) as type"
					+ " from mall_coin_order mco "
					+ " left join user_t_info uti on mco.user_id=uti.id where uti.is_valid=1 " + querySql
					+ " order by mco.create_date desc";
			if (!export) {
				limitSql += " limit " + (page - 1) * pageSize + "," + pageSize;
			}
			sql += limitSql;
			PageVO vo = new PageVO();
			vo.setTotal(count.intValue());
			if (count.intValue() <= page * pageSize) {
				vo.setIsLast(1);
			}
			vo.setList(queryDao.queryMap(sql));
			vo.setCurrentPage(page);
			vo.setTotalPage((int) Math.ceil(count.doubleValue() / pageSize));
			return vo;
		}
		return new PageVO(new ArrayList<>());
	}

	public void export(List<Map<String, Object>> list, HttpServletResponse res) throws WriteException, IOException {
		String[][] contents = new String[list.size() + 1][];
		String[] title = new String[9];
		title[0] = "充值订单ID";
		title[1] = "用户ID";
		title[2] = "手机号";
		title[3] = "实付账";
		title[4] = "实到帐";
		title[5] = "金币数";
		title[6] = "充值时间";
		title[7] = "到帐时间";
		title[8] = "充值方式";
		contents[0] = title;
		if (CollectionUtils.isNotEmpty(list)) {
			for (int i = 0; i < list.size(); i++) {
				String[] content = new String[9];
				Map<String, Object> map = list.get(i);
				content[0] = map.get("id") != null ? map.get("id").toString() : "";
				content[1] = map.get("user_id") != null ? map.get("user_id").toString() : "";
				content[2] = map.get("telephone") != null ? map.get("telephone").toString() : "";
				content[3] = map.get("pay_amount") != null ? map.get("pay_amount").toString() : "";
				content[4] = map.get("obtain_amount") != null ? map.get("obtain_amount").toString() : "";
				content[5] = map.get("coin_count") != null ? map.get("coin_count").toString() : "";
				content[6] = map.get("create_date") != null ? map.get("create_date").toString() : "";
				content[7] = map.get("update_date") != null ? map.get("update_date").toString() : "";
				content[8] = map.get("type") != null ? map.get("type").toString() : "";
				contents[i + 1] = content;
			}
		}
		String exportTitle = "金币充值明细";
		ExcelUtils.exportExcel(exportTitle, contents, res);
	}
}
