package com.miqtech.master.service.mall;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.bounty.BountyPrizeDao;
import com.miqtech.master.dao.mall.CommodityHistoryDao;
import com.miqtech.master.dao.mall.UserRecInfoDao;
import com.miqtech.master.entity.bounty.BountyPrize;
import com.miqtech.master.entity.mall.CommodityHistory;
import com.miqtech.master.entity.mall.UserRecInfo;
import com.miqtech.master.utils.BeanUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.vo.PageVO;

/**
 * 用户收货信息service
 */
@Component
public class UserRecInfoService {
	@Autowired
	private UserRecInfoDao userRecInfoDao;
	@Autowired
	private QueryDao queryDao;
	@Autowired
	private CommodityHistoryDao commodityHistoryDao;
	@Autowired
	private BountyPrizeDao bountyPrizeDao;

	/**
	 * 根据id查实体
	 */
	public UserRecInfo getEntityById(long id) {
		return userRecInfoDao.findOne(id);
	}

	/**
	 * 通过用户ID查询用户的收款地址
	 */
	public UserRecInfo findByUserId(Long userId) {
		List<UserRecInfo> users = userRecInfoDao.findByUserIdAndValid(userId, CommonConstant.INT_BOOLEAN_TRUE);
		if (CollectionUtils.isNotEmpty(users)) {
			return users.get(0);
		} else {
			return null;
		}
	}

	/**
	 * 根据用户id及historyId查询记录
	 */
	public UserRecInfo findValidByUserIdAndHistoryId(Long userId, Long historyId) {
		if (userId == null || historyId == null) {
			return null;
		}

		return userRecInfoDao.findByUserIdAndHistoryIdAndValid(userId, historyId, CommonConstant.INT_BOOLEAN_TRUE);
	}

	/**
	 * 根据用户ID查询收货信息
	 */
	public Map<String, Object> queryRecordByUserId(long userId) {
		String sqlQuery = "select id, user_id userId, name, telephone, address, account from mall_r_user_info where user_id="
				+ userId + " limit 1";
		List<Map<String, Object>> list = queryDao.queryMap(sqlQuery);
		if (CollectionUtils.isNotEmpty(list) && list.get(0) != null) {
			return list.get(0);
		}
		return null;
	}

	/**
	 * 保存
	 */
	public UserRecInfo save(UserRecInfo userRecInfo) {
		if (userRecInfo != null) {
			Date now = new Date();
			userRecInfo.setUpdateDate(now);
			if (userRecInfo.getId() != null) {// 编辑
				UserRecInfo oldInfo = getEntityById(userRecInfo.getId());
				if (oldInfo != null) {
					userRecInfo = BeanUtils.updateBean(oldInfo, userRecInfo);
				}
			} else {// 新增
				userRecInfo.setValid(CommonConstant.INT_BOOLEAN_TRUE);
				userRecInfo.setCreateDate(now);
			}
			return userRecInfoDao.save(userRecInfo);
		}
		return null;
	}

	/**
	 * ##后台管理##:查询，分页
	 */
	public PageVO listPage(int page, int rows, Map<String, Object> params) {
		String sqlQuery = "select id, user_id userId, name, telephone, address, account from mall_r_user_info where is_valid=1";
		String sqlCount = "select count(1) from mall_r_user_info where is_valid=1";
		if (null != params.get("name")) {
			sqlQuery = SqlJoiner.join(sqlQuery, " and area_name like concat('%', :name, '%')");
			sqlCount = SqlJoiner.join(sqlCount, " and area_name like '%" + params.get("name") + "%'");
		}
		params.put("page", (page - 1) * rows);
		params.put("rows", rows);
		sqlQuery = SqlJoiner.join(sqlQuery, " limit :page, :rows");

		PageVO pageVO = new PageVO();
		pageVO.setList(queryDao.queryMap(sqlQuery, params));

		Number total = queryDao.query(sqlCount);
		pageVO.setTotal(total.intValue());

		return pageVO;
	}

	/**
	 * 查询该用户所有收获信息
	 */
	public List<Map<String, Object>> getAreaList() {
		String sqlQuery = "select id, user_id userId, name, telephone, address, account from mall_r_user_info where is_valid=1";
		return queryDao.queryMap(sqlQuery);
	}

	public Map<String, Object> editMallUserInfo(Long userId, Long historyId, String name, String telephone,
			String address, String account) {
		Map<String, Object> result = null;
		if (StringUtils.isBlank(name) && StringUtils.isBlank(telephone) && StringUtils.isBlank(address)
				&& StringUtils.isBlank(account)) {
			return result = queryDao.querySingleMap(
					"select id,name,telephone,address,account from mall_r_user_info a where a.is_valid=1 and user_id="
							+ userId + " and history_id is null");
		} else {
			UserRecInfo userRecInfo = userRecInfoDao.findByUserIdAndHistoryIdIsNullAndValid(userId, 1);
			if (userRecInfo == null) {
				userRecInfo = new UserRecInfo();
				userRecInfo.setName(name);
				userRecInfo.setTelephone(telephone);
				userRecInfo.setAddress(address);
				userRecInfo.setAccount(account);
				userRecInfo.setUserId(userId);
				userRecInfo.setValid(1);
				userRecInfo.setCreateDate(new Date());
				userRecInfoDao.save(userRecInfo);//最新用户兑奖信息
				userRecInfo.setId(null);
				userRecInfo.setHistoryId(historyId);
				userRecInfoDao.save(userRecInfo);//本次兑换的兑奖信息
			} else {
				if (StringUtils.isNotBlank(name)) {
					userRecInfo.setName(name);
				}
				if (StringUtils.isNotBlank(telephone)) {
					userRecInfo.setTelephone(telephone);
				}
				if (StringUtils.isNotBlank(address)) {
					userRecInfo.setAddress(address);
				}
				if (StringUtils.isNotBlank(account)) {
					userRecInfo.setAccount(account);
				}
				userRecInfoDao.save(userRecInfo);//最新用户兑奖信息
				userRecInfo.setId(null);
				userRecInfo.setHistoryId(historyId);
				userRecInfo.setCreateDate(new Date());
				userRecInfoDao.save(userRecInfo);//本次兑换的兑奖信息
			}
			CommodityHistory commodityHistory = commodityHistoryDao.findOne(historyId);
			if (commodityHistory.getStatus() == -1) {
				commodityHistory.setStatus(0);
				if(commodityHistory.getCommoditySource().equals(3)){ // 判断当类型为悬赏令的时候将状态值为4
					commodityHistory.setStatus(4);
					BountyPrize bountyPrize = bountyPrizeDao.findOne(commodityHistory.getCommodityId());
					Integer awardSubType = bountyPrize.getAwardSubType();
					if(awardSubType.equals(6)||awardSubType.equals(8)){ // 充值话费和流量
						commodityHistory.setAccount(telephone);
					}
					commodityHistory.setCreateDate(new Date());
				}
				if (StringUtils.isNotBlank(account)) {
					commodityHistory.setAccount(account);
				}
				commodityHistoryDao.save(commodityHistory);
			}
			return result;
		}
	}
}
