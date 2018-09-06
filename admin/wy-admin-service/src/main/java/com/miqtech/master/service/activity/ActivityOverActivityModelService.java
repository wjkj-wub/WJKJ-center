package com.miqtech.master.service.activity;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.activity.ActivityOverActivityModelDao;
import com.miqtech.master.entity.activity.ActivityOverActivity;
import com.miqtech.master.entity.activity.ActivityOverActivityModel;
import com.miqtech.master.utils.BeanUtils;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.vo.PageVO;

@Component
public class ActivityOverActivityModelService {

	@Autowired
	private QueryDao queryDao;
	@Autowired
	private ActivityOverActivityModelDao activityOverActivityModelDao;
	@Autowired
	private ActivityOverActivityService activityOverActivityService;

	public List<ActivityOverActivityModel> findValidAll() {
		return activityOverActivityModelDao.findByValid(CommonConstant.INT_BOOLEAN_TRUE);
	}

	public List<ActivityOverActivityModel> findValidByInfoId(Long infoId) {
		if (infoId == null) {
			return null;
		}
		return activityOverActivityModelDao.findByInfoIdAndValid(infoId, CommonConstant.INT_BOOLEAN_TRUE);
	}

	public ActivityOverActivityModel findById(Long id) {
		return activityOverActivityModelDao.findOne(id);
	}

	public ActivityOverActivityModel save(ActivityOverActivityModel model) {
		return activityOverActivityModelDao.save(model);
	}

	public ActivityOverActivityModel saveOrUpdate(ActivityOverActivityModel model) {
		if (model == null) {
			return null;
		}

		Date now = new Date();
		model.setUpdateDate(now);
		if (model.getId() != null) {
			ActivityOverActivityModel old = findById(model.getId());
			if (old != null) {
				model = BeanUtils.updateBean(old, model);
			}
		} else {
			model.setValid(CommonConstant.INT_BOOLEAN_TRUE);
			model.setCreateDate(now);
		}

		return save(model);
	}

	public List<ActivityOverActivityModel> save(List<ActivityOverActivityModel> models) {
		if (CollectionUtils.isEmpty(models)) {
			return null;
		}
		return (List<ActivityOverActivityModel>) activityOverActivityModelDao.save(models);
	}

	/**
	 * 删除(禁用)模版
	 */
	public void disableById(Long id) {
		if (id == null) {
			return;
		}

		ActivityOverActivityModel model = findById(id);
		if (model != null) {
			model.setValid(CommonConstant.INT_BOOLEAN_FALSE);
			save(model);
		}
	}

	/**
	 * 后台分页
	 */
	public PageVO adminPage(int page, Map<String, String> searchParams) {
		String limitSql = PageUtils.getLimitSql(page);

		String totalSql = "SELECT COUNT(1) FROM activity_over_activity_model m WHERE m.is_valid = 1";
		Number total = queryDao.query(totalSql);

		List<Map<String, Object>> list = null;
		if (total != null && total.longValue() > 0) {
			String sql = SqlJoiner.join(
					"SELECT id, title, create_date createDate, creater FROM activity_over_activity_model m",
					" WHERE m.is_valid = 1 ORDER BY m.create_date DESC", limitSql);
			list = queryDao.queryMap(sql);
		}

		return new PageVO(page, list, total);
	}

	/**
	 * 根据资讯ID添加模版
	 */
	public void modelByInfoId(Long infoId, boolean add) {
		if (infoId == null) {
			return;
		}

		if (add) {
			ActivityOverActivity info = activityOverActivityService.findById(infoId);
			if (info == null) {
				return;
			}

			ActivityOverActivityModel model = new ActivityOverActivityModel();
			model.setInfoId(info.getId());
			model.setTitle(info.getTitle());
			model.setCreater(info.getCreater());
			model.setRemark(info.getRemark());
			model.setValid(CommonConstant.INT_BOOLEAN_TRUE);
			Date now = new Date();
			model.setUpdateDate(now);
			model.setCreateDate(now);
			save(model);
		} else {
			List<ActivityOverActivityModel> models = findValidByInfoId(infoId);
			if (CollectionUtils.isNotEmpty(models)) {
				for (ActivityOverActivityModel m : models) {
					m.setValid(CommonConstant.INT_BOOLEAN_FALSE);
				}
			}
			save(models);
		}
	}
}