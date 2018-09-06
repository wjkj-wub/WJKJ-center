package com.miqtech.master.admin.web.controller.backend;

import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.entity.common.IndexAdvertise;
import com.miqtech.master.entity.game.GameInfo;
import com.miqtech.master.service.game.GameInfoService;
import com.miqtech.master.service.index.IndexAdvertiseService;
import com.miqtech.master.utils.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("gameInfo/")
public class GameInfoController extends BaseController {
	@Autowired
	private IndexAdvertiseService indexAdvertiseService;
	@Autowired
	private GameInfoService gameInfoService;

	/**
	 * 分页列表
	 */
	@RequestMapping("/list/{page}")
	public ModelAndView users(@PathVariable("page") int page, String url) {
		ModelAndView mv = new ModelAndView("activity/advertiseList"); //跳转到advertiseList.ftl页面

		Map<String, Object> params = new HashMap<String, Object>(16);
		if (StringUtils.isNotBlank(url)) {
			params.put("url", url);
		}

		Page<IndexAdvertise> pageContent = indexAdvertiseService.page(page, params);
		pageModels(mv, pageContent.getContent(), page, pageContent.getTotalElements());
		return mv;
	}

	/**
	 * 请求详细信息（单个对象），跳转url
	 */
	@ResponseBody
	@RequestMapping("/detail/{id}")
	public ModelAndView syDetail(@PathVariable("id") long id) {
		ModelAndView mv = new ModelAndView("game/gameInfo"); //跳转到gameInfo.ftl页面
		GameInfo gameInfo = gameInfoService.findById(id);
		mv.addObject("gameInfo", gameInfo);
		mv.addObject("currentPage", 1);
		mv.addObject("isLastPage", 0);//0已到最后一页 1可以加载下一页
		mv.addObject("totalCount", 1);
		mv.addObject("totalPage", 1);
		return mv;
	}

	/**
	 * 新增或更新
	 */
	@ResponseBody
	@RequestMapping("/save")
	public JsonResponseMsg save(String id, String title, String describe, String type, String url) {
		JsonResponseMsg result = new JsonResponseMsg();

		if (StringUtils.isAllBlank(url) || (StringUtils.isNotBlank(id) && !NumberUtils.isNumber(id))) {
			result.fill(CommonConstant.CODE_ERROR_PARAM, CommonConstant.MSG_ERROR_PARAM);
			return result;
		}

		IndexAdvertise indexAdvertise = null;
		if (StringUtils.isNotBlank(id)) {
			indexAdvertise = indexAdvertiseService.findById(NumberUtils.toLong(id));
			if (indexAdvertise == null) {
				result.fill(CommonConstant.CODE_ERROR_LOGIC, "不存在用户");
				return result;
			}
		} else {
			indexAdvertise = new IndexAdvertise();
		}

		if (StringUtils.isNotBlank(title)) {
			indexAdvertise.setTitle(title);
		}
		if (StringUtils.isNotBlank(describe)) {
			indexAdvertise.setDescribe(describe);
		}
		if (StringUtils.isNotBlank(type)) {
			indexAdvertise.setType(Integer.valueOf(type));
		}
		if (StringUtils.isNotBlank(url)) {
			indexAdvertise.setUrl(url);
		}

		indexAdvertiseService.saveOrUpdate(indexAdvertise);
		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
		return result;
	}

	/**
	 * 删除
	 */
	@ResponseBody
	@RequestMapping("/delete/{id}")
	public JsonResponseMsg delete(@PathVariable("id") long id) {
		JsonResponseMsg result = new JsonResponseMsg();
		indexAdvertiseService.deleteById(id);
		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
		return result;
	}
}
