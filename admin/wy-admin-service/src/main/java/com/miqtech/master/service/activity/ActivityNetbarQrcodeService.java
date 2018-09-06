package com.miqtech.master.service.activity;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.activity.ActivityNetbarQrcodeDao;
import com.miqtech.master.entity.activity.ActivityNetbarQrcode;
import com.miqtech.master.utils.BeanUtils;

@Component
public class ActivityNetbarQrcodeService {

	@Autowired
	private ActivityNetbarQrcodeDao activityNetbarQrcodeDao;

	@Autowired
	private QueryDao queryDao;

	public ActivityNetbarQrcode findById(Long id) {
		return activityNetbarQrcodeDao.findByIdAndValid(id, CommonConstant.INT_BOOLEAN_TRUE);
	}

	/**
	 * 保存场次二维码信息
	 */
	public ActivityNetbarQrcode saveQrcode(ActivityNetbarQrcode qrcode) {
		if (qrcode != null) {
			Date now = new Date();
			qrcode.setUpdateDate(now);

			if (qrcode.getId() != null) {
				ActivityNetbarQrcode oldBean = findById(qrcode.getId());
				qrcode = BeanUtils.updateBean(oldBean, qrcode);
			} else {
				qrcode.setValid(CommonConstant.INT_BOOLEAN_TRUE);
				qrcode.setCreateDate(now);
			}
			return activityNetbarQrcodeDao.save(qrcode);
		}
		return null;
	}

	public ActivityNetbarQrcode saveOrUpdate(ActivityNetbarQrcode item) {
		if (item != null) {
			Date now = new Date();
			item.setUpdateDate(now);
			if (item.getId() != null) {
				ActivityNetbarQrcode old = findById(item.getId());
				if (old != null) {
					item = BeanUtils.updateBean(old, item);

				}
			} else {
				item.setValid(CommonConstant.INT_BOOLEAN_TRUE);
				item.setCreateDate(now);
			}
			ActivityNetbarQrcode info = activityNetbarQrcodeDao.save(item);
			return info;
		}
		return null;
	}

	/**
	 * 删除场次二维码信息
	 */
	public void initChangciInfo(Long id) {
		String updateSql = "DELETE FROM activity_netbar_qrcode WHERE activity_id =" + id;
		queryDao.update(updateSql);

	}

	/**
	 * 通过netbarId获得比赛地区、比赛赛点信息
	 */
	public List<Map<String, Object>> findActivityRoundNetbarInfo(Long netBarId, Long id, Integer round) {
		if (netBarId != null) {
			String sql = "select a.name nerbarName,b.name areaName,c.over_time from netbar_t_info a left join sys_t_area b on a.area_code=b.area_code left join activity_r_rounds c on FIND_IN_SET(a.id,c.netbars) where a.id="
					+ netBarId + " and c.activity_id=" + id + " and c.round=" + round
					+ " and c.is_valid=1 group by a.id,c.round";
			return queryDao.queryMap(sql);
		}
		return null;
	}

	/**
	 * 通过netbarId获得比赛地区、比赛赛点信息
	 */
	public List<Map<String, Object>> findActivityRoundNetbarInfo(Long netBarId) {
		if (netBarId != null) {
			String sql = "select a.name nerbarName,b.name areaName from netbar_t_info a left join sys_t_area b on a.area_code=b.area_code where a.id="
					+ netBarId + " group by a.id";
			return queryDao.queryMap(sql);
		}
		return null;
	}

}
