package com.miqtech.master.admin.web.controller.api.audition;

import com.miqtech.master.admin.web.annotation.CrossDomain;
import com.miqtech.master.admin.web.annotation.LoginValid;
import com.miqtech.master.admin.web.controller.api.BaseController;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.entity.audition.AuditionAwardConfig;
import com.miqtech.master.exception.ParameterErrorException;
import com.miqtech.master.service.audition.AuditionAwardService;
import com.miqtech.master.utils.CookieUtils;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.StringUtils;
import com.miqtech.master.vo.PageVO;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 海选赛获胜奖励 操作管理
 * @author zhangyuq
 * 2017年06月14日
 */
@Controller
@RequestMapping("api/audition/award")
public class AuditionAwardController extends BaseController {

	@Resource
	private AuditionAwardService auditionAwardService;

	private static final String RECEIVE_MIN_COIN = "audition_award_receive_min_coin";// 领取金币最小值key
	private static final String RECEIVE_MAX_COIN = "audition_award_receive_max_coin";// 领取金币最大值key
	private static final String DOWNLOAD_MIN_COIN = "audition_award_download_min_coin";// 下载金币最小值key
	private static final String DOWNLOAD_MAX_COIN = "audition_award_download_max_coin";// 下载金币最大值key
	private static final String AUDITION_AWARD_APP_IDS = "audition_award_app_ids";// 推广APP ID 列表key

	/**
	 * 获取金币奖励发放统计列表
	 */
	@RequestMapping(value = "/list")
	@LoginValid(valid = true)
	@ResponseBody
	public JsonResponseMsg getAuditionAwardList(Integer page, Integer pageSize, String keyName) {
		JsonResponseMsg result = new JsonResponseMsg();

		// 判断page是否合法，返回合法值
		page = PageUtils.getPage(page);
		PageVO pageList = auditionAwardService.getAuditionAwardList(page, pageSize, keyName);
		result.setObject(pageList);
		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);

		return result;
	}

	/**
	 * 获取获胜奖励配置信息
	 */
	@RequestMapping(value = "/config")
	@LoginValid(valid = true)
	@ResponseBody
	public JsonResponseMsg getAuditionAwardConfig() {
		JsonResponseMsg result = new JsonResponseMsg();

		AuditionAwardConfig awardConfig = getAuditionAwardFromRedis();
		boolean isPutRedis = false;
		if (awardConfig == null) {
			isPutRedis = true;
			awardConfig = auditionAwardService.getAuditionAwardConfig();
		}

		Map<String, Object> map = new HashMap<>(16);
		if (awardConfig == null) {
			awardConfig = new AuditionAwardConfig();
		} else if (isPutRedis) {
			// 将获胜奖励配置信息重新存入redis
			putAuditionAwardConfigIntoRedis(awardConfig.getReceiveMinCount(), awardConfig.getReceiveMaxCount(),
					awardConfig.getDownloadMinCount(), awardConfig.getDownloadMaxCount(), awardConfig.getAppIds());
		}

		map.put("receiveMinCount", awardConfig.getReceiveMinCount());
		map.put("receiveMaxCount", awardConfig.getReceiveMaxCount());
		map.put("downloadMinCount", awardConfig.getDownloadMinCount());
		map.put("downloadMaxCount", awardConfig.getDownloadMaxCount());
		map.put("appIds", awardConfig.getAppIds());
		map.put("gameList", auditionAwardService.getGameIds());
		result.setObject(map);
		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);

		return result;
	}

	/**
	 * 保存获胜奖励配置信息
	 */
	@RequestMapping(value = "/config/save", method = RequestMethod.POST)
	@LoginValid(valid = true)
	@ResponseBody
	public JsonResponseMsg saveAuditionAwardConfig(HttpServletRequest request) throws Exception {
		JsonResponseMsg result = new JsonResponseMsg();
		Integer receiveMinCount;
		Integer receiveMaxCount;
		Integer downloadMinCount;
		Integer downloadMaxCount;
		String appIds;
		String userId;

		try {
			userId = CookieUtils.getCookie(request, BaseController.WEB_ADMIN_LOGIN_USERID);
			receiveMinCount = Integer.valueOf(request.getParameter("receiveMinCount"));
			receiveMaxCount = Integer.valueOf(request.getParameter("receiveMaxCount"));
			downloadMinCount = Integer.valueOf(request.getParameter("downloadMinCount"));
			downloadMaxCount = Integer.valueOf(request.getParameter("downloadMaxCount"));
			appIds = request.getParameter("appIds");

			if (userId == null || receiveMinCount == null || receiveMaxCount == null || downloadMinCount == null
					|| downloadMaxCount == null || StringUtils.isBlank(appIds)) {
				throw new ParameterErrorException();
			}
		} catch (Exception e) {
			throw new ParameterErrorException();
		}

		// 验证推广APP是否重复
		if (appIds.contains(",")) {
			String[] appIdArray = appIds.split(",");
			for (int i = 0; i < appIdArray.length - 1; i++) {
				for (int j = appIdArray.length - 1; j == i; j++) {
					if (appIdArray[i].equals(appIdArray[j])) {
						return result.fill(CommonConstant.CODE_ERROR_LOGIC, "推广app不能重复");
					}
				}
			}
		}

		AuditionAwardConfig auditionAwardConfig = new AuditionAwardConfig();
		Date now = new Date();

		AuditionAwardConfig dbAuditionAwardConfig = auditionAwardService.getAuditionAwardConfig();
		if (dbAuditionAwardConfig != null) {
			// 更新配置信息
			dbAuditionAwardConfig.setUpdateDate(now);
			dbAuditionAwardConfig.setUpdateUserId(Long.valueOf(userId));
			auditionAwardConfig = dbAuditionAwardConfig;
		} else {
			// 添加配置信息
			auditionAwardConfig.setCreateDate(now);
			auditionAwardConfig.setCreateUserId(Long.valueOf(userId));
			auditionAwardConfig.setValid(CommonConstant.INT_BOOLEAN_TRUE);
		}

		auditionAwardConfig.setReceiveMinCount(receiveMinCount);
		auditionAwardConfig.setReceiveMaxCount(receiveMaxCount);
		auditionAwardConfig.setDownloadMinCount(downloadMinCount);
		auditionAwardConfig.setDownloadMaxCount(downloadMaxCount);
		auditionAwardConfig.setAppIds(appIds);

		auditionAwardService.saveAuditionAwardConfig(auditionAwardConfig);
		// 将发放配置存入redis
		putAuditionAwardConfigIntoRedis(receiveMinCount, receiveMaxCount, downloadMinCount, downloadMaxCount, appIds);

		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);

		return result;
	}

	/**
	 * 导出金币发放统计列表信息
	 */
	@RequestMapping(value = "/export")
	@LoginValid(valid = true)
	@CrossDomain(value = true)
	@ResponseBody
	public JsonResponseMsg export(HttpServletResponse res) throws Exception {
		JsonResponseMsg result = new JsonResponseMsg();

		auditionAwardService.export(res);
		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);

		return result;
	}

	/**
	 * 将获胜奖励配置信息存入redis
	 */
	private void putAuditionAwardConfigIntoRedis(Integer receiveMinCoin, Integer receiveMaxCoin,
			Integer downloadMinCoin, Integer downloadMaxCoin, String appIds) {
		redisOperateService.setData(RECEIVE_MIN_COIN, receiveMinCoin.toString());
		redisOperateService.setData(RECEIVE_MAX_COIN, receiveMaxCoin.toString());
		redisOperateService.setData(DOWNLOAD_MIN_COIN, downloadMinCoin.toString());
		redisOperateService.setData(DOWNLOAD_MAX_COIN, downloadMaxCoin.toString());
		redisOperateService.setData(AUDITION_AWARD_APP_IDS, appIds);
	}

	/**
	 * 从redis中获取获胜奖励配置信息
	 */
	private AuditionAwardConfig getAuditionAwardFromRedis() {
		AuditionAwardConfig awardConfig = null;
		String receiveMinCoin = redisOperateService.getData(RECEIVE_MIN_COIN);
		String receiveMaxCoin = redisOperateService.getData(RECEIVE_MAX_COIN);
		String downloadMinCoin = redisOperateService.getData(DOWNLOAD_MIN_COIN);
		String downloadMaxCoin = redisOperateService.getData(DOWNLOAD_MAX_COIN);
		String appIds = redisOperateService.getData(AUDITION_AWARD_APP_IDS);
		if (StringUtils.isAllNotBlank(receiveMinCoin, receiveMaxCoin, downloadMinCoin, downloadMaxCoin, appIds)) {
			awardConfig = new AuditionAwardConfig();
			awardConfig.setReceiveMinCount(NumberUtils.toInt(receiveMinCoin));
			awardConfig.setReceiveMaxCount(NumberUtils.toInt(receiveMaxCoin));
			awardConfig.setDownloadMinCount(NumberUtils.toInt(downloadMinCoin));
			awardConfig.setDownloadMaxCount(NumberUtils.toInt(downloadMaxCoin));
			awardConfig.setAppIds(appIds);
		}

		return awardConfig;
	}
}
