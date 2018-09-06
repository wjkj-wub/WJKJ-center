package com.miqtech.master.admin.web.controller.backend;

import com.alibaba.fastjson.JSONObject;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.consts.RedbagConstant;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.entity.common.SystemRedbag;
import com.miqtech.master.service.common.StringRedisOperateService;
import com.miqtech.master.service.system.SystemRedbagService;
import com.miqtech.master.utils.BeanUtils;
import com.miqtech.master.utils.DateUtils;
import com.miqtech.master.utils.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.support.atomic.RedisAtomicDouble;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.beans.PropertyEditorSupport;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("redbag/setting")
public class RedbagSettingController extends BaseController {

	private static final Logger LOGGER = LoggerFactory.getLogger(RedbagSettingController.class);

	@Autowired
	private SystemRedbagService systemRedbagService;
	@Autowired
	private StringRedisOperateService stringRedisOperateService;

	/**
	 * 红包设置列表
	 */
	@RequestMapping("list/{page}")
	public ModelAndView list(@PathVariable("page") int page, String type, String explain, String beginTime,
			String endTime) {
		ModelAndView mv = new ModelAndView("/redbag/settingList");

		if (StringUtils.isBlank(type)) {// 周红包列表
			type = "3";
		}

		// 处理参数
		Map<String, Object> params = new HashMap<String, Object>(16);
		if (StringUtils.isNotBlank(beginTime)) {
			params.put("beginTime", beginTime);
		}
		if (StringUtils.isNotBlank(endTime)) {
			params.put("endTime", endTime);
		}
		if (StringUtils.isNotBlank(type)) {
			params.put("type", type);
		}
		if (StringUtils.isNotBlank(explain)) {
			params.put("explain", explain);
		}

		Page<SystemRedbag> settingPage = systemRedbagService.page(page, params);
		pageModels(mv, settingPage.getContent(), page, settingPage.getTotalElements());
		mv.addObject("params", params);
		mv.addObject("type", type);

		return mv;
	}

	/**
	 * 红包详情
	 */
	@ResponseBody
	@RequestMapping("detail/{id}")
	public JsonResponseMsg detail(@PathVariable("id") long id) {
		JsonResponseMsg result = new JsonResponseMsg();
		SystemRedbag redbag = systemRedbagService.findOne(id);

		JSONObject jsonRedbag = (JSONObject) JSONObject.toJSON(redbag);
		long redbagId = redbag.getId();
		jsonRedbag.put("surplusAmount",
				stringRedisOperateService.getData(RedbagConstant.getRedisKeySurplusAmount(redbagId)));
		jsonRedbag.put("usedAmount", stringRedisOperateService.getData(RedbagConstant.getRedisKeyUsedAmount(redbagId)));

		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS, jsonRedbag);
		return result;
	}

	/**
	 * 保存
	 */
	@ResponseBody
	@RequestMapping("save")
	public JsonResponseMsg save(HttpServletRequest req, SystemRedbag systemRedbag) {
		JsonResponseMsg result = new JsonResponseMsg();

		if (systemRedbag != null) {
			Date date = new Date();
			systemRedbag.setUpdateDate(date);
			boolean isInsert = false;
			if (systemRedbag.getId() != null) {
				SystemRedbag old = systemRedbagService.findOne(systemRedbag.getId());
				systemRedbag = BeanUtils.updateBean(old, systemRedbag);
			} else {
				isInsert = true;
				systemRedbag.setValid(CommonConstant.INT_BOOLEAN_TRUE);
				systemRedbag.setCreateDate(date);
			}

			// 更新缓存金额值
			RedisConnectionFactory connFactory = stringRedisOperateService.getRedisTemplate().getConnectionFactory();
			RedisAtomicDouble redisSurplusAmount = null;
			RedisAtomicDouble redisUsedAmount = null;
			RedisAtomicDouble redisTotalAmount = null;
			if (RedbagConstant.REDBAG_TYPE_WEEKLY.equals(systemRedbag.getType()) && !isInsert) {// 更新
				// 初始化redis访问对象
				redisSurplusAmount = new RedisAtomicDouble(
						RedbagConstant.getRedisKeySurplusAmount(systemRedbag.getId()), connFactory);
				redisUsedAmount = new RedisAtomicDouble(RedbagConstant.getRedisKeyUsedAmount(systemRedbag.getId()),
						connFactory);
				redisTotalAmount = new RedisAtomicDouble(RedbagConstant.getRedisKeyTotalAmount(systemRedbag.getId()),
						connFactory);

				// 检测 设置金额 是否合理
				BigDecimal usedAmount = new BigDecimal(redisUsedAmount.get());
				BigDecimal newTotalAmount = new BigDecimal(systemRedbag.getTotalmoney());
				if (newTotalAmount.compareTo(usedAmount) < 0) {
					result.fill(CommonConstant.CODE_ERROR_LOGIC, "设置失败，总金额少于已花费（" + usedAmount + "）");
					return result;
				}
			}

			systemRedbag = systemRedbagService.save(systemRedbag);

			if (RedbagConstant.REDBAG_TYPE_WEEKLY.equals(systemRedbag.getType())) {
				if (isInsert) {
					// 初始化redis中的值
					long redbagId = systemRedbag.getId();
					redisSurplusAmount = new RedisAtomicDouble(RedbagConstant.getRedisKeySurplusAmount(redbagId),
							connFactory);
					redisUsedAmount = new RedisAtomicDouble(RedbagConstant.getRedisKeyUsedAmount(redbagId), connFactory);
					redisTotalAmount = new RedisAtomicDouble(RedbagConstant.getRedisKeyTotalAmount(redbagId),
							connFactory);

					redisTotalAmount.set(systemRedbag.getTotalmoney().doubleValue());
					redisSurplusAmount.set(systemRedbag.getTotalmoney().doubleValue());
					redisUsedAmount.set(0.0);
				} else {
					// 计算新增金额
					BigDecimal newTotal = new BigDecimal(systemRedbag.getTotalmoney());
					BigDecimal oldTotal = new BigDecimal(redisTotalAmount.get());
					BigDecimal changeAmount = newTotal.subtract(oldTotal);

					// 更新总值 和 剩余值
					redisTotalAmount.addAndGet(changeAmount.doubleValue());
					redisSurplusAmount.addAndGet(changeAmount.doubleValue());
				}
			}
		}

		String configIcon = req.getParameter("shareRedbagIcon");
		if (StringUtils.isNotBlank(configIcon)) {
			stringRedisOperateService.setData(RedbagConstant.REDIS_KEY_REDBAG_SHARE_ICON, configIcon);
		}
		String configTitle = req.getParameter("shareRedbagTitle");
		if (StringUtils.isNotBlank(configTitle)) {
			stringRedisOperateService.setData(RedbagConstant.REDIS_KEY_REDBAG_SHARE_TITLE, configTitle);
		}
		String configContent = req.getParameter("shareRedbagContent");
		if (StringUtils.isNotBlank(configContent)) {
			stringRedisOperateService.setData(RedbagConstant.REDIS_KEY_REDBAG_SHARE_CONTENT, configContent);
		}

		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
		return result;
	}

	/**
	 * 删除
	 */
	@ResponseBody
	@RequestMapping("delete/{id}")
	public JsonResponseMsg delete(@PathVariable("id") long id) {
		JsonResponseMsg result = new JsonResponseMsg();
		systemRedbagService.delete(id);
		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
		return result;
	}

	/**
	 * 编辑
	 */
	@RequestMapping("edit")
	public ModelAndView edit(String type) {
		ModelAndView mv = new ModelAndView("redbag/edit");
		int typeInt = NumberUtils.toInt(type, 0);
		Map<String, Object> redbag = systemRedbagService.findOneByType(typeInt);
		mv.addObject("redbag", redbag);
		mv.addObject("type", typeInt);

		if (RedbagConstant.REDBAG_TYPE_SHARE.equals(typeInt)) {
			mv.addObject("shareRedbagIcon",
					stringRedisOperateService.getData(RedbagConstant.REDIS_KEY_REDBAG_SHARE_ICON));
			mv.addObject("shareRedbagTitle",
					stringRedisOperateService.getData(RedbagConstant.REDIS_KEY_REDBAG_SHARE_TITLE));
			mv.addObject("shareRedbagContent",
					stringRedisOperateService.getData(RedbagConstant.REDIS_KEY_REDBAG_SHARE_CONTENT));
			mv.addObject("showShareRedbagSetting", true);
		} else {
			mv.addObject("showShareRedbagSetting", false);
		}

		return mv;
	}

	@InitBinder
	private void initDateBinder(WebDataBinder binder) {
		binder.registerCustomEditor(Date.class, new PropertyEditorSupport() {
			@Override
			public void setAsText(String value) {
				Date date = null;
				try {
					date = DateUtils.stringToDateYyyyMMddhhmmss(value);
				} catch (ParseException e) {
					LOGGER.error("格式化时间参数异常", e);
				}
				setValue(date);
			}
		});
	}
}
