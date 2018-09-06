package com.miqtech.master.service.lottery;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.lottery.LotteryOptionDao;
import com.miqtech.master.entity.lottery.LotteryOption;
import com.miqtech.master.utils.BeanUtils;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.vo.PageVO;

@Component
public class LotteryOptionService {

	@Autowired
	private QueryDao queryDao;
	@Autowired
	private LotteryOptionDao lotteryOptionDao;

	/**
	 * 查出所有有效活动
	 */
	public List<LotteryOption> getAllValid() {
		return lotteryOptionDao.findAllValid();
	}

	/**
	 * 通过ID查询抽奖活动
	 */
	public LotteryOption findValidById(Long id) {
		return lotteryOptionDao.findByIdAndValid(id, CommonConstant.INT_BOOLEAN_TRUE);
	}

	/**
	 * 通过ID查询
	 */
	public LotteryOption findById(Long id) {
		return lotteryOptionDao.findById(id);
	}

	/**
	 * 保存活动设置
	 */
	public LotteryOption save(LotteryOption option) {
		if (option != null) {
			Date now = new Date();
			option.setUpdateDate(now);
			if (option.getId() != null) {
				LotteryOption oldOption = findById(option.getId());
				if (oldOption != null) {
					option = BeanUtils.updateBean(oldOption, option);
				}
			} else {
				option.setValid(CommonConstant.INT_BOOLEAN_TRUE);
				option.setCreateDate(now);
			}
			return lotteryOptionDao.save(option);
		}
		return null;
	}

	/**
	 * 删除（逻辑删除）
	 */
	public void delete(Long id) {
		String sql = "UPDATE lottery_t_option SET is_valid = 0, update_date = NOW() WHERE id = :id".replaceAll(":id",
				id.toString());
		queryDao.update(sql);
	}

	/**
	 * 还原删除的数据
	 */
	public void restore(Long id) {
		String sql = "UPDATE lottery_t_option SET is_valid = 1, update_date = NOW() WHERE id = :id".replaceAll(":id",
				id.toString());
		queryDao.update(sql);
	}

	/**
	 * 分页
	 */
	public PageVO page(int page, Map<String, Object> params) {
		String sql = SqlJoiner
				.join("SELECT o.id, name, o.plate_img plateImg, o.introduce, o.start_date startDate, o.end_date endDate, o.create_date createDate, o.is_valid isValid",
						" FROM lottery_t_option o", buildConditions(page, params));
		List<Map<String, Object>> list = queryDao.queryMap(sql);

		PageVO vo = new PageVO();
		vo.setList(list);

		String countSql = SqlJoiner.join("SELECT COUNT(1) FROM lottery_t_option o", buildConditions(0, params));
		Number count = (Number) queryDao.query(countSql);
		if (count == null) {
			count = 0;
		}

		vo.setTotal(count.longValue());
		vo.setIsLast(page * PageUtils.ADMIN_DEFAULT_PAGE_SIZE >= count.intValue() ? 1 : 0);
		return vo;
	}

	/**
	 * 产生分页查询的查询条件
	 */
	private String buildConditions(int page, Map<String, Object> params) {
		String conditions = " WHERE 1";

		if (params != null) {
			Set<String> keys = params.keySet();
			if (CollectionUtils.isNotEmpty(keys)) {
				for (String k : keys) {
					String value = String.valueOf(params.get(k));
					if (k.equals("beginDate")) {
						conditions = SqlJoiner.join(conditions, " AND o.create_date > '", value, "'");
					} else if (k.equals("endDate")) {
						conditions = SqlJoiner.join(conditions, " AND o.create_date < ADDDATE('", value,
								"',INTERVAL 1 DAY)");
					} else if (k.equals("valid")) {
						conditions = SqlJoiner.join(conditions, " AND o.is_valid = ", value);
					} else {
						conditions = SqlJoiner.join(conditions, " AND o.", k, " LIKE '%", value, "%'");
					}
				}
			}
		}

		conditions = SqlJoiner.join(conditions, " ORDER BY o.create_date DESC");

		if (page > 0) {
			Integer rows = PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
			Integer startRow = (page - 1) * rows;
			conditions = SqlJoiner.join(conditions, " LIMIT ", startRow.toString(), ",", rows.toString());
		}

		return conditions;
	}
}
