package com.miqtech.master.service.audition;

import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.audition.AuditionAwardDao;
import com.miqtech.master.entity.audition.AuditionAwardConfig;
import com.miqtech.master.utils.ExcelUtils;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.utils.StringUtils;
import com.miqtech.master.vo.PageVO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * 海选赛-获胜奖励 Service
 * @author zhangyuqi
 * 2017年06月14日
 */
@Service
public class AuditionAwardService {

	private static final Logger LOGGER = LoggerFactory.getLogger(AuditionAwardService.class);

	// 金币发放列表对应导出文件标题栏
	private static final String[] EXPORT_FILE_COLUMN_NAME = { "时间", "APP名", "直接领取", "下载领取", "用户ID" };

	@Resource
	private QueryDao queryDao;
	@Resource
	private AuditionAwardDao auditionAwardDao;

	/**
	 * 获取获胜奖励配置信息
	 */
	public AuditionAwardConfig getAuditionAwardConfig() {
		try {
			List<AuditionAwardConfig> awardConfigs = auditionAwardDao.findAllByValid(CommonConstant.INT_BOOLEAN_TRUE);
			return CollectionUtils.isEmpty(awardConfigs) ? null : awardConfigs.get(0);
		} catch (Exception e) {
			LOGGER.error("获取获胜奖励配置信息失败：{}", e);
			throw e;
		}
	}

	/**
	 * 获取APP ID 列表
	 */
	public List<Map<String, Object>> getGameIds() {
		String sql = "SELECT id, name from game_t_info where is_valid = 1";
		return queryDao.queryMap(sql);
	}

	/**
	 * 保存获胜奖励配置信息
	 */
	public void saveAuditionAwardConfig(AuditionAwardConfig auditionAwardConfig) {
		try {
			auditionAwardDao.save(auditionAwardConfig);
		} catch (Exception e) {
			LOGGER.error("保存获胜奖励配置信息失败：{}", e);
			throw e;
		}
	}

	/**
	 * 获取金币发放统计列表
	 */
	public PageVO getAuditionAwardList(Integer page, Integer pageSize, String keyName) {
		List<Map<String, Object>> appList = null;
		Number total;
		String condition = StringUtils.EMPTY;

		if (StringUtils.isNotBlank(keyName)) {
			condition = SqlJoiner.join(condition, " AND g.`name` like'%", keyName, "%' ");
		}

		try {
			String totalSql = "SELECT COUNT(DISTINCT a.id) FROM audition_award_record  r"
					+ " LEFT JOIN audition_award a ON r.award_id = a.id"
					+ " LEFT JOIN game_t_info g ON a.app_id = g.id         "
					+ "WHERE r.is_valid = 1 AND g.is_valid = 1 " + condition;
			total = queryDao.query(totalSql);

			if (total != null && total.intValue() != 0) {
				appList = getCoinGrantInfo(PageUtils.getLimitSql(page, pageSize), condition);
			}
		} catch (Exception e) {
			LOGGER.error("获取海选赛金币发放统计列表失败：{}", e);
			throw e;
		}
		return new PageVO(page, appList, total, pageSize);
	}

	/**
	 * 导出金币发放统计列表
	 */
	public void export(HttpServletResponse res) throws Exception {
		String[][] contents;
		try {
			List<Map<String, Object>> coinAwardList = this.getCoinGrantInfo(null, null);
			contents = new String[coinAwardList.size() + 1][];
			// 设置标题行
			contents[0] = EXPORT_FILE_COLUMN_NAME;
			// 设置内容
			if (!CollectionUtils.isEmpty(coinAwardList)) {
				for (int i = 0; i < coinAwardList.size(); i++) {
					Map<String, Object> obj = coinAwardList.get(i);
					String[] row = new String[EXPORT_FILE_COLUMN_NAME.length];
					row[0] = MapUtils.getString(obj, "date");
					row[1] = MapUtils.getString(obj, "appName");
					row[2] = MapUtils.getString(obj, "receiveCoin");
					row[3] = MapUtils.getString(obj, "downloadCoin");
					row[4] = MapUtils.getString(obj, "userId");
					contents[i + 1] = row;
				}
			}

			ExcelUtils.exportExcel("金币发放统计列表", contents, false, res);
		} catch (Exception e) {
			LOGGER.error("导出金币发放统计列表发生错误：{}", e);
			throw e;
		}
	}

	/**
	 * 获取金币发放统计信息
	 */
	private List<Map<String, Object>> getCoinGrantInfo(String limit, String condition) {
		List<Map<String, Object>> infoList;

		limit = limit == null ? StringUtils.EMPTY : limit;
		condition = condition == null ? StringUtils.EMPTY : condition;

		try {
			String sql = "SELECT u.telephone phone, g.`name` appName, aar1.coin receiveCoin, aar2.coin downloadCoin,"
					+ " IF (ISNULL(aar2.create_date), aar1.create_date, aar2.create_date) date"
					+ " FROM audition_award award                     "
					+ " LEFT JOIN game_t_info g ON award.app_id = g.id"
					+ " LEFT JOIN user_t_info u ON award.user_id = u.id"
					+ " LEFT JOIN audition_award_record aar1 ON award.id = aar1.award_id AND aar1.type = 1"
					+ " LEFT JOIN audition_award_record aar2 ON award.id = aar2.award_id AND aar2.type = 2"
					+ " WHERE g.is_valid = 1 AND aar1.create_date IS NOT NULL ORDER BY date DESC " + condition + limit;
			infoList = queryDao.queryMap(sql);
		} catch (Exception e) {
			LOGGER.error("获取金币发放统计信息失败：{}", e);
			throw e;
		}

		return infoList;
	}
}
