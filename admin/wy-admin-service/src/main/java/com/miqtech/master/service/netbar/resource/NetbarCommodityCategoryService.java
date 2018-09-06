package com.miqtech.master.service.netbar.resource;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.netbar.resource.NetbarCommodityCategoryDao;
import com.miqtech.master.entity.netbar.resource.NetbarCommodityCategory;
import com.miqtech.master.utils.BeanUtils;

@Component
public class NetbarCommodityCategoryService {
	@Autowired
	private NetbarCommodityCategoryDao netbarCommodityCategoryDao;
	@Autowired
	private QueryDao queryDao;

	/*
	 * 查对象
	 */
	public NetbarCommodityCategory findById(Long id) {
		return netbarCommodityCategoryDao.findOne(id);
	}

	/*
	 * 保存/更新对象
	 */
	public NetbarCommodityCategory save(NetbarCommodityCategory netbarCommodityCategory) {
		if (null != netbarCommodityCategory) {
			Date now = new Date();
			if (null != netbarCommodityCategory.getId()) {
				netbarCommodityCategory.setUpdateDate(now);
				NetbarCommodityCategory old = findById(netbarCommodityCategory.getId());
				if (null != old) {
					netbarCommodityCategory = BeanUtils.updateBean(old, netbarCommodityCategory);
				}
			} else {
				netbarCommodityCategory.setValid(CommonConstant.INT_BOOLEAN_TRUE);
				netbarCommodityCategory.setCreateDate(now);
			}
			return netbarCommodityCategoryDao.save(netbarCommodityCategory);
		}
		return null;
	}

	/*
	 * 改valid
	 */
	public void stateChange(long id, int valid) {
		String sql = "update netbar_commodity_category set is_valid=" + valid + " where id=" + id;
		queryDao.update(sql);
	}

	/*
	 * 查大类别
	 */
	public List<Map<String, Object>> getSuperCategory() {
		String sql = "select id, name from netbar_commodity_category where is_valid=1 and pid=0 or pid is null";
		List<Map<String, Object>> list = queryDao.queryMap(sql);
		return list;
	}

	/*
	 * 根据大类别id查旗下小类别
	 */
	public List<Map<String, Object>> getCategoryByPid(long pid) {
		String sql = "select id, name from netbar_commodity_category where is_valid=1 and pid=" + pid;
		List<Map<String, Object>> list = queryDao.queryMap(sql);
		return list;
	}

	/**
	 * 通过资源商城商品ID查询
	 */
	public Map<String, Object> queryByNetbarResourceCommodityId(Long netbarResourceCommodityId) {
		if (netbarResourceCommodityId != null) {
			String sql = "SELECT ncc.* FROM netbar_resource_commodity nrc JOIN netbar_commodity_category ncc ON nrc.category_id = ncc.id AND ncc.is_valid = 1 WHERE nrc.id = "
					+ netbarResourceCommodityId;
			return queryDao.querySingleMap(sql);
		}
		return null;
	}

}