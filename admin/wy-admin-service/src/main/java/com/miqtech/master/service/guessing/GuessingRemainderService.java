package com.miqtech.master.service.guessing;

import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.utils.ExcelUtils;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.vo.PageVO;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * 竞猜余量  Service
 * @author zhangyuqi
 * 2017年6月1日
 */
@Service
public class GuessingRemainderService {

	private static Logger logger = LoggerFactory.getLogger(GuessingRemainderService.class);

	// 竞猜余量列表对应导出文件标题栏
	private static final String[] EXPORT_FILE_COLUMN_NAME = { "标题", "猜中人数", "参与人数", "本场正余量", "本场负余量", "当前余量额" };

	@Resource
	private QueryDao queryDao;

	/**
	 * 查询所有竞猜余量列表
	 * @param page		当前页数
	 * @param pageSize	单页显示数量
	 * @param keyTitle	竞猜标题关键字(搜索条件)
	 */
	public PageVO findGuessingRemainderList(int page, Integer pageSize, String keyTitle) {
		List<Map<String, Object>> guessingInfoList = null;
		Number total;

		try {
			String condition = StringUtils.EMPTY;
			String limit = PageUtils.getLimitSql(page, pageSize);

			if (StringUtils.isNotBlank(keyTitle)) {
				condition = SqlJoiner.joinWithoutSpace(condition, " AND title LIKE '%", keyTitle, "%' ");
			}

			String totalSql = "SELECT count(id) FROM guessing_info WHERE is_valid = 1 AND status = 3" + condition;
			total = queryDao.query(totalSql);

			if (total != null && total.intValue() != 0) {
				guessingInfoList = this.getGuessingRemainderList(condition, limit);
			}
		} catch (Exception e) {
			logger.error("获取竞猜余量列表失败：{}", e);
			throw e;
		}
		return new PageVO(page, guessingInfoList, total);
	}

	/**
	 * 导出竞猜余量列表信息
	 */
	public void export(HttpServletResponse res) throws Exception {
		String[][] contents;
		try {
			List<Map<String, Object>> guessingRemainderList = this.getGuessingRemainderList(null, null);
			contents = new String[guessingRemainderList.size() + 1][];
			// 设置标题行
			contents[0] = EXPORT_FILE_COLUMN_NAME;
			// 设置内容
			if (!CollectionUtils.isEmpty(guessingRemainderList)) {
				for (int i = 0; i < guessingRemainderList.size(); i++) {
					Map<String, Object> obj = guessingRemainderList.get(i);
					String[] row = new String[EXPORT_FILE_COLUMN_NAME.length];
					row[0] = MapUtils.getString(obj, "title");
					row[1] = MapUtils.getString(obj, "winnerCount");
					row[2] = MapUtils.getString(obj, "totalUserCount");
					row[3] = MapUtils.getString(obj, "positiveRemainder");
					row[4] = MapUtils.getString(obj, "negativeRemainder");
					row[5] = MapUtils.getString(obj, "currentRemainder");
					contents[i + 1] = row;
				}
			}

			ExcelUtils.exportExcel("竞猜余量列表", contents, false, res);
		} catch (Exception e) {
			logger.error("导出竞猜余量列表信息发生错误：{}", e);
			throw e;
		}
	}

	/**
	 * 根据查询条件和 limit 查询竞猜列余量表结果集
	 */
	private List<Map<String, Object>> getGuessingRemainderList(String condition, String limit) {

		condition = condition == null ? StringUtils.EMPTY : condition;

		limit = limit == null ? StringUtils.EMPTY : limit;

		String sql = "SELECT o.title title, o.current_remainder currentRemainder,"
				+ " o.positive_remainder positiveRemainder, o.negative_remainder negativeRemainder,"
				+ " IFNULL(t.winnerCount, 0) winnerCount, IFNULL(t.totalUserCount, 0) totalUserCount"
				+ " FROM guessing_info AS o             "
				+ "	LEFT JOIN (SELECT r.guessing_info_id guessingId, COUNT(r.id) totalUserCount,"
				+ "		SUM(IF(om.is_winner = 1, 1, 0)) winnerCount     "
				+ "		FROM guessing_record r                          "
				+ "		LEFT JOIN guessing_info_item om ON r.guessing_info_id = om.guessing_info_id"
				+ "		AND r.guessing_item_id = om.guessing_item_id"
				+ "		WHERE r.is_valid = 1 AND r.is_adding_coin = 0 AND om.is_valid = 1 GROUP BY r.guessing_info_id"
				+ "		) t ON t.guessingId = o.id                        "
				+ " WHERE o.is_valid = 1 AND o.`status` = 3 " + condition + " ORDER BY o.update_date DESC " + limit;

		return queryDao.queryMap(sql);
	}
}
