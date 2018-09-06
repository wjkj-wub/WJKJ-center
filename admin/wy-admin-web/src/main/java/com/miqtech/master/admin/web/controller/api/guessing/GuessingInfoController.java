package com.miqtech.master.admin.web.controller.api.guessing;

import com.miqtech.master.admin.web.annotation.CrossDomain;
import com.miqtech.master.admin.web.annotation.LoginValid;
import com.miqtech.master.admin.web.controller.api.BaseController;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.consts.official.GuessingConstant;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.entity.guessing.GuessingInfo;
import com.miqtech.master.entity.guessing.GuessingInfoItem;
import com.miqtech.master.exception.ParameterErrorException;
import com.miqtech.master.service.guessing.GuessingInfoService;
import com.miqtech.master.utils.CookieUtils;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.StringUtils;
import com.miqtech.master.vo.PageVO;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.Map;

/**
 * 竞猜管理操作
 * @author zhangyuqi
 * 2017年06月01日
 */
@Controller
@RequestMapping("api/guessing/info")
public class GuessingInfoController extends BaseController {

	@Resource
	private GuessingInfoService guessingInfoService;

	/**
	 * 获取竞猜列表信息
	 * @param page		当前页数
	 * @param keyTitle	竞猜标题关键字(搜索条件)
	 * @param keyStatus	竞猜状态关键字(搜索条件)
	 */
	@RequestMapping(value = "/list")
	@LoginValid(valid = true)
	@ResponseBody
	public JsonResponseMsg findGuessingList(Integer page, Integer pageSize, String keyTitle, Integer keyStatus) {
		JsonResponseMsg result = new JsonResponseMsg();
		page = PageUtils.getPage(page);
		PageVO pageList = guessingInfoService.findGuessingList(page, pageSize, keyTitle, keyStatus);
		result.setObject(pageList);
		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);

		return result;
	}

	/**
	 * 获取单个竞猜信息
	 */
	@RequestMapping(value = "/single")
	@LoginValid(valid = true)
	@ResponseBody
	public JsonResponseMsg single(Long guessingId) throws Exception {
		JsonResponseMsg result = new JsonResponseMsg();

		if (guessingId == null) {
			throw new ParameterErrorException();
		}

		Map<String, Object> guessingInfo = guessingInfoService.findSingleGuessing(guessingId);
		result.setObject(guessingInfo);
		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);

		return result;
	}

	/**
	 * 添加/更新竞猜信息
	 */
	@RequestMapping(value = "/save", method = RequestMethod.POST)
	@LoginValid(valid = true)
	@ResponseBody
	public JsonResponseMsg save(HttpServletRequest request) throws Exception {
		Long leftItemId;
		Long rightItemId;
		Long guessingId;
		String uid;
		String title;
		String leftSupportRate;
		String rightSupportRate;
		Date releaseDate;
		Date endDate;

		JsonResponseMsg result = new JsonResponseMsg();

		try {
			String guessingIdStr = request.getParameter("guessingId");
			guessingId = StringUtils.isBlank(guessingIdStr) ? null : Long.valueOf(guessingIdStr);
			uid = CookieUtils.getCookie(request, BaseController.WEB_ADMIN_LOGIN_USERID);
			leftItemId = Long.valueOf(request.getParameter("leftItemId"));
			rightItemId = Long.valueOf(request.getParameter("rightItemId"));
			title = request.getParameter("title");
			leftSupportRate = request.getParameter("leftSupportRate");
			rightSupportRate = request.getParameter("rightSupportRate");
			releaseDate = new Date(Long.valueOf(request.getParameter("releaseDate")));
			endDate = new Date(Long.valueOf(request.getParameter("endDate")));
		} catch (Exception e) {
			throw new ParameterErrorException();
		}

		// 验证参数非空
		if (uid == null || StringUtils.isBlank(title) || leftItemId == null || rightItemId == null) {
			throw new ParameterErrorException();
		}

		if (leftItemId.equals(rightItemId)) {
			return result.fill(CommonConstant.CODE_ERROR_LOGIC, "同一场竞猜中竞猜对象不能为同一个");
		}

		// 竞猜发布时间必须小于竞猜
		if (!releaseDate.before(endDate)) {
			return result.fill(CommonConstant.CODE_ERROR_PARAM, "竞猜发布时间必须小于竞猜截止时间");
		}

		Date now = new Date();
		Long userId = Long.valueOf(uid);

		// 创建实体对象信息
		GuessingInfo guessingInfo = generateGuessingInfo(userId, guessingId, title, releaseDate, endDate, now);
		GuessingInfoItem leftInfoItem = generateGuessingInfoItem(userId, guessingInfo.getId(), leftItemId, now,
				CommonConstant.INT_BOOLEAN_FALSE, leftSupportRate);
		GuessingInfoItem rightInfoItem = generateGuessingInfoItem(userId, guessingInfo.getId(), rightItemId, now,
				CommonConstant.INT_BOOLEAN_TRUE, rightSupportRate);

		guessingInfoService.saveOrUpdateWithItem(guessingInfo, leftInfoItem, rightInfoItem);
		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);

		return result;
	}

	/**
	 * 录入竞猜结果信息
	 */
	@RequestMapping(value = "/result/save", method = RequestMethod.POST)
	@LoginValid(valid = true)
	@ResponseBody
	public JsonResponseMsg saveGuessingInfoItem(HttpServletRequest request, Long guessingId, Integer winnerPosition,
			Integer leftItemScore, Integer rightItemScore) throws Exception {
		JsonResponseMsg result = new JsonResponseMsg();

		String uid = CookieUtils.getCookie(request, BaseController.WEB_ADMIN_LOGIN_USERID);
		if (uid == null || guessingId == null || winnerPosition == null) {
			throw new ParameterErrorException();
		}

		if (leftItemScore == null || rightItemScore == null) {
			return result.fill(-7, "比分不可为空");
		}

		GuessingInfo guessingInfo = guessingInfoService.findSingleGuessingInfo(guessingId);
		if (GuessingConstant.GUESSING_STATUS_END.equals(guessingInfo.getStatus())) {
			return result.fill(CommonConstant.CODE_ERROR_LOGIC, "不允许重复录入结果");
		}

		GuessingInfoItem leftInfoItem = guessingInfoService.findSingleGuessingInfoItem(guessingId,
				CommonConstant.INT_BOOLEAN_FALSE);
		GuessingInfoItem rightInfoItem = guessingInfoService.findSingleGuessingInfoItem(guessingId,
				CommonConstant.INT_BOOLEAN_TRUE);

		Date now = new Date();
		Long userId = Long.valueOf(uid);
		// 设置左边竞猜对象信息
		leftInfoItem.setUpdateUserId(userId);
		leftInfoItem.setUpdateDate(now);
		leftInfoItem.setScore(leftItemScore);
		leftInfoItem.setIsWinner(CommonConstant.INT_BOOLEAN_FALSE.equals(winnerPosition) ? 1 : 0);
		// 设置右边竞猜对象信息
		rightInfoItem.setUpdateUserId(userId);
		rightInfoItem.setUpdateDate(now);
		rightInfoItem.setScore(rightItemScore);
		rightInfoItem.setIsWinner(CommonConstant.INT_BOOLEAN_TRUE.equals(winnerPosition) ? 1 : 0);

		guessingInfoService.saveResult(userId, guessingInfo, leftInfoItem, rightInfoItem);
		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);

		return result;
	}

	/**
	 * 置顶竞猜信息
	 * @param guessingId	竞猜ID
	 */
	@RequestMapping(value = "/setFirst")
	@LoginValid(valid = true)
	@ResponseBody
	public JsonResponseMsg setFirst(Long guessingId) throws Exception {
		JsonResponseMsg result = new JsonResponseMsg();

		if (guessingId == null) {
			throw new ParameterErrorException();
		}

		guessingInfoService.setFirst(guessingId);
		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);

		return result;
	}

	/**
	 * 取消置顶
	 * @param guessingId	竞猜ID
	 */
	@RequestMapping(value = "/cancelFirst")
	@LoginValid(valid = true)
	@ResponseBody
	public JsonResponseMsg cancelFirst(Long guessingId) throws Exception {
		JsonResponseMsg result = new JsonResponseMsg();

		if (guessingId == null) {
			throw new ParameterErrorException();
		}

		guessingInfoService.cancelFirst(guessingId);
		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);

		return result;
	}

	/**
	 * 删除竞猜信息
	 * @param guessingId	竞猜ID集合
	 */
	@RequestMapping(value = "/delete")
	@LoginValid(valid = true)
	@ResponseBody
	public JsonResponseMsg delete(Long guessingId) throws Exception {
		JsonResponseMsg result = new JsonResponseMsg();

		if (guessingId == null) {
			throw new ParameterErrorException();
		}

		GuessingInfo guessingInfo = guessingInfoService.findSingleGuessingInfo(guessingId);
		guessingInfo.setValid(CommonConstant.INT_BOOLEAN_FALSE);
		guessingInfoService.saveOrUpdate(guessingInfo);
		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);

		return result;
	}

	/**
	 * 导出竞猜列表信息
	 */
	@RequestMapping(value = "/export")
	@LoginValid(valid = true)
	@CrossDomain(value = true)
	@ResponseBody
	public void export(HttpServletResponse res) throws Exception {
		guessingInfoService.export(res);
	}

	/**
	 * 创建竞猜对象
	 */
	private GuessingInfo generateGuessingInfo(Long userId, Long guessingId, String title, Date releaseDate,
			Date endDate, Date now) {
		GuessingInfo guessingInfo;

		if (guessingId != null) {// 更新
			GuessingInfo dbGuessingInfo = guessingInfoService.findSingleGuessingInfo(guessingId);

			if (dbGuessingInfo != null) {
				dbGuessingInfo.setUpdateUserId(userId);
				dbGuessingInfo.setUpdateDate(now);
				dbGuessingInfo.setTitle(title);
				dbGuessingInfo.setEndDate(endDate);
				dbGuessingInfo.setReleaseDate(releaseDate);
			}
			guessingInfo = dbGuessingInfo;
		} else {// 添加
			guessingInfo = new GuessingInfo();
			guessingInfo.setCreateUserId(userId);
			guessingInfo.setTitle(title);
			guessingInfo.setEndDate(endDate);
			guessingInfo.setReleaseDate(releaseDate);
			guessingInfo.setCreateDate(now);
			guessingInfo.setValid(CommonConstant.INT_BOOLEAN_TRUE);
			guessingInfo.setStatus(GuessingConstant.GUESSING_STATUS_NOT_START);
			if (releaseDate.before(now) || releaseDate.compareTo(now) == 0) {
				guessingInfo.setStatus(GuessingConstant.GUESSING_STATUS_UNDER_WAY);
			}
		}

		return guessingInfo;
	}

	/**
	 * 创建guessing_info_item对象
	 */
	private GuessingInfoItem generateGuessingInfoItem(Long userId, Long infoId, Long itemId, Date now, Integer position,
			String supportRate) {
		GuessingInfoItem infoItem;

		if (infoId != null) {
			infoItem = guessingInfoService.findSingleGuessingInfoItem(infoId, position);
			if (infoItem != null) {
				infoItem.setGuessingItemId(itemId);
				infoItem.setUpdateDate(now);
				infoItem.setUpdateUserId(userId);
			}
		} else {
			infoItem = new GuessingInfoItem();
			infoItem.setGuessingItemId(itemId);
			infoItem.setValid(CommonConstant.INT_BOOLEAN_TRUE);
			infoItem.setCreateUserId(userId);
			infoItem.setCreateDate(now);
			infoItem.setPosition(position);
			infoItem.setSupportRate(supportRate);
		}
		return infoItem;
	}

}
