package com.miqtech.master.service.audition;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.miqtech.master.config.SystemConfig;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.consts.uwan.UwanNetbarConstant;
import com.miqtech.master.dao.audition.AuditionMatchDetailLolDao;
import com.miqtech.master.entity.audition.AuditionMatchDetailLol;
import com.miqtech.master.entity.uwan.UwanNetbar;
import com.miqtech.master.entity.uwan.UwanUser;
import com.miqtech.master.service.common.StringRedisOperateService;
import com.miqtech.master.service.uwan.UwanNetbarService;
import com.miqtech.master.service.uwan.UwanUserService;
import com.miqtech.master.thirdparty.util.uwan.UwanInterface;
import com.miqtech.master.utils.DateUtils;

@Component
public class AuditionMatchDetailLolService {

	private static final Logger logger = LoggerFactory.getLogger(AuditionMatchDetailLolService.class);
	
	private static final int BATCH_UPDATE_COUNT = 500;// 批量更新时每批的数量

	@Autowired
	private StringRedisOperateService stringRedisOperateService;
	@Autowired
	private SystemConfig systemConfig;
	@Autowired
	private AuditionMatchDetailLolDao auditionMatchDetailLolDao;
	@Autowired
	private UwanNetbarService uwanNetbarService;
	@Autowired
	private UwanUserService uwanUserService;

	/**
	 * 批量保存
	 */
	public void batchSave(List<AuditionMatchDetailLol> details) {
		if (CollectionUtils.isEmpty(details)) {
			return;
		}

		auditionMatchDetailLolDao.save(details);
	}

	/**
	 * 查询网吧的赛事记录
	 */
	public void saveNetbarMatches() {
		long startMatchTime = DateUtils.getYesterday().getTime();
		long endMatchTime = DateUtils.getToday().getTime();
		this.saveNetbarMatches(startMatchTime, endMatchTime);
	}
	
	/**
	 * 查询网吧的赛事记录
	 */
	public void saveNetbarMatches(Long startMatchTime, Long endMatchTime) {
		String uwanGateway = systemConfig.getUwanGateway();

		List<UwanNetbar> uwanNetbars = uwanNetbarService.findBySource(UwanNetbarConstant.SOURCE_NETBAR);
		if (CollectionUtils.isEmpty(uwanNetbars)) {
			return;
		}

 		// 数据同步中标识,避免数据同步中进行计算而导致结果不正确
 		String syncKey = UwanNetbarConstant.REDIS_KEY_UWAN_NETBAR_MATCH_SYNCING;
 		stringRedisOperateService.setData(syncKey, CommonConstant.INT_BOOLEAN_TRUE.toString(), 12, TimeUnit.HOURS);
 
 		// 组装已存在的网吧信息,以匹配网娱网吧ID
 		List<Map<String, Long>> barIdMaps = Lists.newArrayList();
		for (UwanNetbar uwanNetbar : uwanNetbars) {
			Long uwanBarId = uwanNetbar.getUwanBarId();
			Long netbarId = uwanNetbar.getNetbarId();

			Map<String, Long> barIdMap = Maps.newHashMap();
			barIdMap.put("barId", uwanBarId);
			barIdMap.put("netbarId", netbarId);
			barIdMaps.add(barIdMap);
		}

		Date now = new Date();
		List<AuditionMatchDetailLol> updateDetails = Lists.newArrayList();
		for (Map<String, Long> barIdMap : barIdMaps) {
			Long barId = MapUtils.getLong(barIdMap, "barId");
			Long netbarId = MapUtils.getLong(barIdMap, "netbarId");
			if (barId == null) {
				continue;
			}

			JSONArray yesterdayNetbarMatches = UwanInterface.getNetbarMatches(uwanGateway, barId, startMatchTime, endMatchTime);
			if (CollectionUtils.isNotEmpty(yesterdayNetbarMatches)) {
				List<String> updateUwanUserIds = Lists.newArrayList();
				List<UwanUser> updateUwanUsers = Lists.newArrayList();
				for (int i = 0; i < yesterdayNetbarMatches.size(); i++) {
					JSONObject match = yesterdayNetbarMatches.getJSONObject(i);

					// 添加比赛记录到待更新列表
					Integer matchType = match.getInteger("matchType");
					JSONArray results = match.getJSONArray("results");
					if (CollectionUtils.isNotEmpty(results)) {
						for (int j = 0; j < results.size(); j++) {
							JSONObject json = results.getJSONObject(j);
							AuditionMatchDetailLol detail = JSON.parseObject(json.toJSONString(),
									AuditionMatchDetailLol.class);
							detail.setMatchType(matchType);
							detail.setNetbarId(netbarId);

 							Long nowBarId = detail.getBarId();
 							if (nowBarId == null) {
 								logger.error("缺少barId:" + json.toJSONString());
 								continue;
 							}
 							if (!nowBarId.equals(barId)) {
 								logger.error("barId不一致,barId:" + barId + ";json:" + json);
 								continue;
 							}
							
							// 用户信息设置
							Long uwanUserId = json.getLong("uwanUserId");
							detail.setPlayerId(uwanUserId);

							// 记录优玩用户
							String uwanUserIdStr = uwanUserId.toString();
							if (!updateUwanUserIds.contains(uwanUserIdStr)) {
								JSONObject uwanUser = json.getJSONObject("uwanUser");
								UwanUser updateUwanUser = new UwanUser();
								updateUwanUser.setUwanUserId(uwanUserIdStr);
								updateUwanUser.setNickanme(uwanUser.getString("nickname"));
								updateUwanUser.setIcon(uwanUser.getString("icon"));
								updateUwanUser.setTelephone(uwanUser.getString("telephone"));
								updateUwanUser.setUpdateDate(now);
								updateUwanUser.setCreateDate(now);
								updateUwanUser.setValid(CommonConstant.INT_BOOLEAN_TRUE);
								updateUwanUsers.add(updateUwanUser);
								updateUwanUserIds.add(uwanUserIdStr);
							}

							updateDetails.add(detail);
						}
					}

					// 用户数据处理
					List<UwanUser> existsUwanUsers = uwanUserService.findValidByUwanUserIds(updateUwanUserIds);
					if (CollectionUtils.isNotEmpty(existsUwanUsers)) {
						for (UwanUser updateUwanUser : updateUwanUsers) {
							String updateUwanUserId = updateUwanUser.getUwanUserId();
							for (UwanUser uwanUser : existsUwanUsers) {
								String nowUwanUserId = uwanUser.getUwanUserId();
								if (updateUwanUserId.equals(nowUwanUserId)) {
									updateUwanUser.setId(uwanUser.getId());
									break;
								}
							}
						}
					}
					uwanUserService.batchUpdate(updateUwanUsers);
				}
			}

			// 避免一次插入过多数据,分批进行批量保存
			if (updateDetails.size() / BATCH_UPDATE_COUNT >= 1) {
				this.batchSave(updateDetails);
				updateDetails = Lists.newArrayList();
			}
		}
		this.batchSave(updateDetails);
		stringRedisOperateService.delData(syncKey);
	}

	public static void main(String[] args) {
		System.out.println(499 / 500);
		System.out.println(500 / 500);
	}
}
