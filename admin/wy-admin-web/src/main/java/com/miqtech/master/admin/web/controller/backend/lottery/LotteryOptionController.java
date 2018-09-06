package com.miqtech.master.admin.web.controller.backend.lottery;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.miqtech.master.admin.web.controller.backend.BaseController;
import com.miqtech.master.admin.web.util.Servlets;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.entity.lottery.LotteryOption;
import com.miqtech.master.service.lottery.LotteryOptionService;
import com.miqtech.master.thirdparty.util.img.ImgUploadUtil;
import com.miqtech.master.utils.DateUtils;
import com.miqtech.master.utils.StringUtils;
import com.miqtech.master.vo.PageVO;

@Controller
@RequestMapping("lottery/option")
public class LotteryOptionController extends BaseController {

	private static final Logger LOGGER = LoggerFactory.getLogger(LotteryOptionController.class);

	@Autowired
	private LotteryOptionService lotteryOptionService;

	/**
	 * 分页
	 */
	@RequestMapping("list/{page}")
	public ModelAndView list(@PathVariable("page") int page, String name, String beginDate, String endDate,
			String valid) {
		ModelAndView mv = new ModelAndView("lottery/optionList");

		if (StringUtils.isBlank(valid)) {
			valid = CommonConstant.INT_BOOLEAN_TRUE.toString();
		}

		Map<String, Object> params = new HashMap<String, Object>();
		if (StringUtils.isNotBlank(name)) {
			params.put("name", name);
		}
		if (StringUtils.isNotBlank(beginDate)) {
			params.put("beginDate", beginDate);
		}
		if (StringUtils.isNotBlank(endDate)) {
			params.put("endDate", endDate);
		}
		if (StringUtils.isNotBlank(valid)) {
			params.put("valid", valid);
		}

		PageVO vo = lotteryOptionService.page(page, params);
		pageModels(mv, vo.getList(), page, vo.getTotal());
		mv.addObject("params", params);

		return mv;
	}

	/**
	 * 查询活动详情
	 */
	@ResponseBody
	@RequestMapping("detail/{id}")
	public JsonResponseMsg detail(@PathVariable("id") long id) {
		JsonResponseMsg result = new JsonResponseMsg();
		LotteryOption option = lotteryOptionService.findValidById(id);
		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS, option);
	}

	/**
	 * 保存活动设置
	 */
	@ResponseBody
	@RequestMapping("save")
	public JsonResponseMsg save(HttpServletRequest req, String id, String name, String startDate, String endDate,
			String introduce) {
		JsonResponseMsg result = new JsonResponseMsg();

		// 检查参数
		MultipartFile platImg = Servlets.getMultipartFile(req, "plateImgFile");
		if (StringUtils.isAllBlank(id, name, startDate, endDate, introduce) && platImg == null) {
			return result.fill(CommonConstant.CODE_ERROR_PARAM, CommonConstant.MSG_ERROR_PARAM);
		}

		// 更新对象
		LotteryOption option = new LotteryOption();
		if (NumberUtils.isNumber(id)) {
			option.setId(NumberUtils.toLong(id));
		}
		if (StringUtils.isNotBlank(name)) {
			option.setName(name);
		}
		if (StringUtils.isNotBlank(startDate)) {
			try {
				Date d = DateUtils.stringToDate(startDate, DateUtils.YYYY_MM_DD_HH_MM_SS);
				option.setStartDate(d);
			} catch (ParseException e) {
				LOGGER.error("格式化时间异常:", e);
			}
		}
		if (StringUtils.isNotBlank(endDate)) {
			try {
				Date d = DateUtils.stringToDate(endDate, DateUtils.YYYY_MM_DD_HH_MM_SS);
				option.setEndDate(d);
			} catch (ParseException e) {
				LOGGER.error("格式化时间异常:", e);
			}
		}
		if (StringUtils.isNotBlank(introduce)) {
			option.setIntroduce(introduce);
		}
		if (platImg != null) {
			Map<String, String> imgUrls = ImgUploadUtil.save(platImg, "wy_web_admin",
					ImgUploadUtil.genFilePath("lottery"));
			option.setPlateImg(imgUrls.get(ImgUploadUtil.KEY_MAP_SRC));
		}
		lotteryOptionService.save(option);

		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
	}

	/**
	 * 删除活动
	 */
	@ResponseBody
	@RequestMapping("delete/{id}")
	public JsonResponseMsg delete(@PathVariable("id") long id, String restore) {
		JsonResponseMsg result = new JsonResponseMsg();
		if (CommonConstant.INT_BOOLEAN_TRUE.toString().equals(restore)) {
			lotteryOptionService.restore(id);
		} else {
			lotteryOptionService.delete(id);
		}
		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
	}

}
