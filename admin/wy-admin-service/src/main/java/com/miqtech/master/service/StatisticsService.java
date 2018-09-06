package com.miqtech.master.service;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.vo.PageVO;

@Component
public class StatisticsService {

	private static final Integer TYPE_WB_HZ = 1;
	private static final Integer TYPE_WB_KT = 2;
	private static final Integer TYPE_WB_ZF = 3;
	private static final Integer TYPE_YH_XZ = 4;
	private static final Integer TYPE_YH_HY = 5;
	private static final Integer TYPE_YH_QD = 6;
	private static final Integer TYPE_YH_LJ = 7;

	@Autowired
	private QueryDao queryDao;

	/**
	 * 获取统计数据分页数据
	 */
	public PageVO getPageVOByType(Integer page, Integer rows, Integer type) {
		if (page == null || page.intValue() <= 0) {
			page = 1;
		}
		if (rows == null) {
			rows = 1000;
		}

		Map<String, String> columnAndTableMap = this.getColumnAndTableByType(type);
		String column = MapUtils.getString(columnAndTableMap, "column");
		String table = MapUtils.getString(columnAndTableMap, "table");

		PageVO pageVO = new PageVO();
		String totalSql = SqlJoiner.join("SELECT COUNT(1) FROM", table);
		Number total = queryDao.query(totalSql);
		if (total == null) {
			total = 0;
		}
		if (page * rows >= total.intValue()) {
			pageVO.setIsLast(CommonConstant.INT_BOOLEAN_TRUE);
		}

		if (total.intValue() > 0) {
			String sql = SqlJoiner.join("SELECT ", column, " FROM ", table,
					" ORDER BY create_date DESC LIMIT :startNum, :pageNum");
			Map<String, Object> params = Maps.newHashMap();
			params.put("startNum", (page - 1) * rows);
			params.put("pageNum", rows);

			List<Map<String, Object>> list = queryDao.queryMap(sql, params);
			pageVO.setList(list);
		}

		return pageVO;
	}

	/**
	 * 通过类型查询所有统计数据
	 */
	public List<Map<String, Object>> getAllByType(Integer type) {
		Map<String, String> columnAndTableMap = this.getColumnAndTableByType(type);
		String column = MapUtils.getString(columnAndTableMap, "column");
		String table = MapUtils.getString(columnAndTableMap, "table");

		String sql = SqlJoiner.join("SELECT ", column, " FROM ", table, " ORDER BY create_date");
		return queryDao.queryMap(sql);
	}
	
	public List<Map<String,Object>> getWbChatDataByMonth(Integer type,String month){
		Map<String, String> columnAndTableMap = this.getColumnAndTableByType(type);
		String table = MapUtils.getString(columnAndTableMap, "table");
		String sql = SqlJoiner.join("SELECT create_date AS eDay, newly_add total FROM ",table," WHERE DATE_FORMAT(create_date, '%Y-%m') = '"+month+"' ORDER BY eDay ASC");
		return queryDao.queryMap(sql);
	}
	
	/**
	 * 获取页面图表数据
	 * @param type
	 * @return
	 */
	public List<Map<String,Object>> getWbChartData(Integer type){
		Map<String, String> columnAndTableMap = this.getColumnAndTableByType(type);
		String table = MapUtils.getString(columnAndTableMap, "table");
		String sql = SqlJoiner.join("SELECT DATE_FORMAT(create_date, '%Y-%m') AS month, SUM(newly_add) AS total FROM ",table," GROUP BY DATE_FORMAT(create_date, '%Y-%m')");
		return queryDao.queryMap(sql);
	}
	
	
	/**
	 * 根据查询类型,定位数据库字段及表格
	 */
	private Map<String, String> getColumnAndTableByType(Integer type) {
		String column = null;
		String table = null;
		if (TYPE_WB_HZ.equals(type)) {
			/*column = "id, DATE_FORMAT(create_date, '%Y-%m') createDate, data";
			table = "zzz_wj_wb_hz_statistics";*/
			
			column = "id, DATE_FORMAT(create_date, '%Y-%m-%d') createDate, newly_add data,cumulative";
			table = "zzz_wj_wb_hz_new_statistics";
		} else if (TYPE_WB_KT.equals(type)) {
			/*column = "id, DATE_FORMAT(create_date, '%Y-%m') createDate, data";
			table = "zzz_wj_wb_kt_statistics";*/
			
			column = "id, DATE_FORMAT(create_date, '%Y-%m-%d') createDate, newly_add data,cumulative";
			table = "zzz_wj_wb_kt_new_statistics";
		} else if (TYPE_WB_ZF.equals(type)) {
			/*column = "id, DATE_FORMAT(create_date, '%Y-%m') createDate, data";
			table = "zzz_wj_wb_zf_statistics";*/
			
			column = "id, DATE_FORMAT(create_date, '%Y-%m-%d') createDate, newly_add data,cumulative";
			table = "zzz_wj_wb_zf_new_statistics";
		} else if (TYPE_YH_XZ.equals(type)) {
			column = "id, create_date createDate, xz data";
			table = "zzz_wj_yh_statistics";
		} else if (TYPE_YH_HY.equals(type)) {
			column = "id, create_date createDate, hy data";
			table = "zzz_wj_yh_statistics";
		} else if (TYPE_YH_QD.equals(type)) {
			column = "id, create_date createDate, qd data";
			table = "zzz_wj_yh_statistics";
		} else if (TYPE_YH_LJ.equals(type)) {
			column = "id, create_date createDate, lj data";
			table = "zzz_wj_yh_statistics";
		} else {// 默认查网吧合作
			column = "id, DATE_FORMAT(create_date, '%Y-%m-%d') createDate, newly_add data,cumulative";
			table = "zzz_wj_wb_hz_new_statistics";
			
			/*column = "id, DATE_FORMAT(create_date, '%Y-%m') createDate, data";
			table = "zzz_wj_wb_hz_statistics";*/
		}

		Map<String, String> result = Maps.newHashMap();
		result.put("column", column);
		result.put("table", table);
		return result;
	}
}
