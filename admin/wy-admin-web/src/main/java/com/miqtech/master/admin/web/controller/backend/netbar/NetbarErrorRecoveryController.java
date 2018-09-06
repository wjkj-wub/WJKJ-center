package com.miqtech.master.admin.web.controller.backend.netbar;

import com.miqtech.master.admin.web.controller.backend.BaseController;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.entity.common.SystemArea;
import com.miqtech.master.entity.netbar.NetbarErrorRecovery;
import com.miqtech.master.service.netbar.NetbarErrorRecoveryService;
import com.miqtech.master.service.system.SystemAreaService;
import com.miqtech.master.vo.PageVO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 网吧纠错信息以及奖励发放
 *
 * @author Administrator
 */
@Controller("netbarErrorRecovery")
@RequestMapping("netbarErrorRecovery")
public class NetbarErrorRecoveryController extends BaseController {

	@Autowired
	private SystemAreaService systemAreaService;

	@Autowired
	private NetbarErrorRecoveryService netbarErrorRecoveryService;

	/**
	 * 纠错列表
	 *
	 * @param page
	 * @param name
	 *            查找内容
	 * @param areaCode
	 *            地区选择
	 * @param state
	 *            审核状态
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/list/{page}")
	public ModelAndView netbarErrorRecovery(@PathVariable("page") int page, String name, String provinceVal,
			String areaVal, String cityVal, Integer type, String state) {
		ModelAndView mv = new ModelAndView();
		mv.setViewName("netbar/netbarErrorRecovery");
		List<SystemArea> areaList = systemAreaService.queryValidRoot();
		Map<String, Object> params = new HashMap<String, Object>(16);
		params.put("provinceVal", provinceVal);
		String areaCode = "000000";
		if (StringUtils.isNotBlank(provinceVal) && !provinceVal.equals("0")) {
			params.put("provinceVal", provinceVal);
			params.put("areaVal", areaVal);
			mv.addObject("cityList", systemAreaService.queryValidChildren(provinceVal));// 地区列表
			areaCode = provinceVal;
		}
		if (StringUtils.isNotBlank(cityVal) && !cityVal.equals("0")) {
			params.put("cityVal", cityVal);
			mv.addObject("areaList", systemAreaService.queryValidChildren(cityVal));// 地区列表
			areaCode = cityVal;
		}
		if (StringUtils.isNotBlank(areaVal) && !areaVal.equals("0")) {
			areaCode = areaVal;
		}
		mv.addObject("provinceList", areaList);// 地区列表
		params.put("name", name);
		params.put("state", state);
		params.putAll(netbarErrorRecoveryService.countResult());
		PageVO vo = netbarErrorRecoveryService.findAllByAreaCode(areaCode, name, page, state);
		pageModels(mv, vo.getList(), page, vo.getTotal());
		mv.addObject("params", params);
		return mv;
	}

	// 更改单条状态值
	@ResponseBody
	@RequestMapping("/changeStatus")
	public JsonResponseMsg changeStatus(Integer type, Long id) {
		JsonResponseMsg result = new JsonResponseMsg();
		if (type == null || id == null) {
			return result.fill(CommonConstant.CODE_ERROR_PARAM, CommonConstant.MSG_ERROR_PARAM);
		}
		// 采纳
		if (type == 1) {
			NetbarErrorRecovery netbarErrorRecovery = netbarErrorRecoveryService.findById(id);
			netbarErrorRecovery.setStatus(1);
			netbarErrorRecoveryService.save(netbarErrorRecovery);
			finishNetbarErrorRecoveryMallTask(netbarErrorRecovery);
		} else if (type == 2) {
			NetbarErrorRecovery netbarErrorRecovery = netbarErrorRecoveryService.findById(id);
			netbarErrorRecovery.setStatus(2);
			netbarErrorRecoveryService.save(netbarErrorRecovery);
		}
		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
	}

	// 批量更改状态值
	@ResponseBody
	@RequestMapping("/batchChange")
	public JsonResponseMsg batchChange(Integer type, String checkIds) {
		JsonResponseMsg result = new JsonResponseMsg();
		// 更改全部待审核网吧
		if (StringUtils.isNotBlank(checkIds)) {
			checkIds = checkIds.substring(0, checkIds.length() - 1);
			List<NetbarErrorRecovery> netbarErrorRecoveryList = netbarErrorRecoveryService.findByIds(checkIds);
			if (CollectionUtils.isNotEmpty(netbarErrorRecoveryList)) {
				for (int i = 0; i < netbarErrorRecoveryList.size(); i++) {
					NetbarErrorRecovery netbarErrorRecovery = netbarErrorRecoveryList.get(i);
					netbarErrorRecovery.setStatus(type);
					netbarErrorRecoveryList.set(i, netbarErrorRecovery);
					if (type == 1) {
						finishNetbarErrorRecoveryMallTask(netbarErrorRecovery);
					}
				}
				netbarErrorRecoveryService.save(netbarErrorRecoveryList);
			}
		} else {
			return result.fill(CommonConstant.CODE_ERROR_PARAM, "请选择要批量删除的信息");
		}
		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
	}

	private void finishNetbarErrorRecoveryMallTask(NetbarErrorRecovery netbarErrorRecovery) {
		return;
		// coinHistoryService.addGoldHistoryPub(netbarErrorRecovery.getUserId(),
		// netbarErrorRecovery.getId(),
		// CoinConstant.NETBAR_ERROR_RECOVERY, 200,
		// CoinConstant.HISTORY_DIRECTION_INCOME);

	}
}
