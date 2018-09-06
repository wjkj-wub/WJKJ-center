package com.miqtech.master.service.guessing;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.guessing.GuessingItemDao;
import com.miqtech.master.entity.guessing.GuessingItem;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.vo.PageVO;

/**
 * 竞猜对象service
 * @author 叶岸平
 */
@Component
public class GuessingItemService {

	private static Logger logger = LoggerFactory.getLogger(GuessingItemService.class);

	@Autowired
	private GuessingItemDao guessingItemDao;
	@Autowired
	private QueryDao queryDao;

	public GuessingItem saveOrUpdate(GuessingItem guessingItem) {
		return guessingItemDao.save(guessingItem);
	}

	public GuessingItem findById(Long itemId) {
		return guessingItemDao.findByIdAndValid(itemId, 1);
	}

	/**
	 * 查看队伍是否还有竞赛
	 */
	public Boolean isItemUsed(Long itemId) {
		String sql = "select count(g.id) count   from guessing_item gi  left join "
				+ " guessing_info_item git on gi.id=git.guessing_item_id   left join "
				+ " guessing_info g on git.guessing_info_id=g.id and g.status!=3  where gi.id='" + itemId + "'";
		Number result = queryDao.query(sql);
		Integer count = null == result ? 0 : result.intValue();
		return count != 0;
	}

	/**
	 * 查找竞猜对象列表
	 */
	public PageVO findItemList(String name, Integer page, Integer pageSize) {
		String conditionSql = StringUtils.EMPTY;
		String limitSql = PageUtils.getLimitSql(page, pageSize);
		if (StringUtils.isNotBlank(name)) {
			conditionSql = SqlJoiner.joinWithoutSpace(conditionSql, conditionSql, " and name LIKE '%", name, "%'");
		}
		String sql = SqlJoiner.join(" select * from guessing_item  where is_valid=1 ", conditionSql, limitSql);
		String countSql = SqlJoiner.join("select count(id) count from guessing_item  where is_valid=1 ", conditionSql,
				" order by create_date desc");
		List<Map<String, Object>> dataList = queryDao.queryMap(sql);
		Number countResult = queryDao.query(countSql);
		PageVO vo = new PageVO(page, dataList, countResult, pageSize);
		return vo;
	}

	/**
	 * 获取所有竞猜对象列表
	 */
	public List<GuessingItem> findAllItemList() {
		try {
			return guessingItemDao.findAllByValid(CommonConstant.INT_BOOLEAN_TRUE);
		} catch (Exception e) {
			logger.error("获取所有竞猜对象列表失败：{}", e);
		}
		return null;
	}
}
