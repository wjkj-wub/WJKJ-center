package com.miqtech.master.service.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.dao.activity.ActivityOverActivityImgDao;
import com.miqtech.master.entity.activity.ActivityOverActivityImg;

@Component
public class ActivityOverActivityImgService {

	@Autowired
	private ActivityOverActivityImgDao activityOverActivityImgDao;

	public List<ActivityOverActivityImg> findByIdIn(List<Long> ids) {
		if (CollectionUtils.isEmpty(ids)) {
			return null;
		}
		return activityOverActivityImgDao.findByIdIn(ids);
	}

	public List<ActivityOverActivityImg> findValidByOverActivityId(Long infoId) {
		if (infoId == null) {
			return null;
		}

		return activityOverActivityImgDao
				.findByActivityIdAndValidOrderByImgAsc(infoId, CommonConstant.INT_BOOLEAN_TRUE);
	}

	/**
	 * 查询图集并按详情分组
	 */
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> queryValidGroupByOverActivityId(Long infoId) {
		List<ActivityOverActivityImg> ais = findValidByOverActivityId(infoId);
		if (CollectionUtils.isEmpty(ais)) {
			return null;
		}

		List<Map<String, Object>> result = Lists.newArrayList();
		for (ActivityOverActivityImg ai : ais) {
			String remark = ai.getRemark();

			boolean exists = false;
			Long id = ai.getId();
			String img = ai.getImg();
			Map<String, Object> imgObj = Maps.newHashMap();
			imgObj.put("url", img);
			imgObj.put("id", id);
			for (Map<String, Object> r : result) {
				String rr = MapUtils.getString(r, "remark");
				if (StringUtils.isNotBlank(rr) && rr.equals(remark)) {
					List<Map<String, Object>> imgs = (List<Map<String, Object>>) r.get("imgs");
					imgs.add(imgObj);
					r.put("imgs", imgs);

					String ids = MapUtils.getString(r, "ids");
					if (StringUtils.isNotBlank(ids)) {
						ids += ",";
					}
					ids += id;
					r.put("ids", ids);

					exists = true;
				}
			}

			if (!exists) {
				Map<String, Object> r = Maps.newHashMap();
				r.put("ids", id.toString());
				r.put("remark", remark);
				ArrayList<Map<String, Object>> imgs = Lists.newArrayList();
				imgs.add(imgObj);
				r.put("imgs", imgs);
				result.add(r);
			}
		}
		return result;
	}

	/**
	 * 批量保存
	 */
	public List<ActivityOverActivityImg> save(List<ActivityOverActivityImg> ais) {
		return (List<ActivityOverActivityImg>) activityOverActivityImgDao.save(ais);
	}

	/**
	 * 更新图片的详情
	 */
	public void updateRemarkByIds(String remark, String ids) {
		if (StringUtils.isBlank(remark) || StringUtils.isBlank(ids)) {
			return;
		}

		// 批量设置并保存
		List<Long> idsLong = transIdsStrToIdsLong(ids);
		List<ActivityOverActivityImg> ais = findByIdIn(idsLong);
		if (CollectionUtils.isEmpty(ais)) {
			return;
		}
		for (ActivityOverActivityImg ai : ais) {
			ai.setRemark(remark);
		}
		save(ais);
	}

	/**
	 * 将分割字符串转为整形列表
	 */
	private List<Long> transIdsStrToIdsLong(String ids) {
		// 转化ids字符串为整形
		String[] idsSplit = StringUtils.split(ids, ",");
		if (ArrayUtils.isEmpty(idsSplit)) {
			return null;
		}
		List<Long> idsLong = Lists.newArrayList();
		for (String idStr : idsSplit) {
			if (NumberUtils.isNumber(idStr)) {
				idsLong.add(NumberUtils.toLong(idStr));
			}
		}
		return idsLong;
	}

	/**
	 * 移除多个图集图片
	 */
	public void disabledByIds(String ids) {
		if (StringUtils.isBlank(ids)) {
			return;
		}

		// 批量设置并保存
		List<Long> idsLong = transIdsStrToIdsLong(ids);
		disabledByIds(idsLong);
	}

	/**
	 * 移除多个图集图片
	 */
	public void disabledByIds(List<Long> ids) {
		List<ActivityOverActivityImg> ais = findByIdIn(ids);
		if (CollectionUtils.isEmpty(ais)) {
			return;
		}
		for (ActivityOverActivityImg ai : ais) {
			ai.setValid(CommonConstant.INT_BOOLEAN_FALSE);
		}
		save(ais);
	}

}
