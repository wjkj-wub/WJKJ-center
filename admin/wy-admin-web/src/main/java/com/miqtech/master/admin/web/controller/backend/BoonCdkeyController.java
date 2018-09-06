package com.miqtech.master.admin.web.controller.backend;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.miqtech.master.admin.web.util.Servlets;
import com.miqtech.master.config.SystemConfig;
import com.miqtech.master.consts.BoonConstant;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.consts.RedbagConstant;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.entity.boon.BoonCdkey;
import com.miqtech.master.entity.common.SystemRedbag;
import com.miqtech.master.service.boon.BoonCdkeyService;
import com.miqtech.master.service.system.SystemRedbagService;
import com.miqtech.master.utils.DateUtils;
import com.miqtech.master.utils.ExcelUtils;
import com.miqtech.master.utils.HttpRequestUtil;
import com.miqtech.master.utils.StringUtils;
import com.miqtech.master.vo.PageVO;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;

@Controller
@RequestMapping("boon/cdkey")
public class BoonCdkeyController extends BaseController {

	private static Logger LOGGER = LoggerFactory.getLogger(BoonCdkeyController.class);

	@Autowired
	private SystemConfig systemConfig;
	@Autowired
	private BoonCdkeyService boonCdkeyService;
	@Autowired
	private SystemRedbagService systemRedbagService;

	/**
	 * 查询列表
	 */
	@RequestMapping("list/{page}")
	public ModelAndView list(@PathVariable("page") int page, String cdkey, String production, String type,
			String isUsed, String createDateBegin, String createDateEnd) {
		ModelAndView mv = new ModelAndView("boon/list");
		if (StringUtils.isAllBlank(cdkey, production, type, isUsed, createDateBegin, createDateEnd)) {// 不允许空查询
			PageVO vo = new PageVO(page, null, 0);
			pageModels(mv, vo.getList(), vo.getCurrentPage(), vo.getTotal());
			return mv;
		}

		Map<String, String> params = Maps.newHashMap();
		params.put("cdkey", cdkey);
		params.put("production", production);
		params.put("type", type);
		params.put("isUsed", isUsed);
		params.put("createDateBegin", createDateBegin);
		params.put("createDateEnd", createDateEnd);
		mv.addObject("params", params);

		PageVO vo = boonCdkeyService.list(page, params);
		pageModels(mv, vo.getList(), vo.getCurrentPage(), vo.getTotal());

		return mv;
	}

	/**
	 * 生成CDKEY页面
	 */
	@RequestMapping("generator")
	public ModelAndView generator() {
		ModelAndView mv = new ModelAndView("boon/generator");

		// 默认过期时间为下个月今天
		Date expired = DateUtils.getToday();
		expired = DateUtils.addSeconds(expired, -1);
		expired = DateUtils.addDays(expired, 1);
		expired = DateUtils.addMonths(expired, 1);
		String expiredDate = DateUtils.dateToString(expired, DateUtils.YYYY_MM_DD_HH_MM_SS);
		mv.addObject("expiredDate", expiredDate);

		// 红包类型的金额为固定值
		List<SystemRedbag> redbags = systemRedbagService.queryByPayType(RedbagConstant.REDBAG_TYPE_CDKEY);
		mv.addObject("redbags", redbags);

		return mv;
	}

	/**
	 * 生产
	 */
	@ResponseBody
	@RequestMapping("generate")
	public JsonResponseMsg generate(String expireDate, String production, String configs) {
		JsonResponseMsg result = new JsonResponseMsg();

		// 检查参数
		if (StringUtils.isBlank(configs) || StringUtils.isBlank(expireDate) || StringUtils.isBlank(production)) {
			return result.fill(CommonConstant.CODE_ERROR_PARAM, CommonConstant.MSG_ERROR_PARAM);
		}

		// 检查是否重复生产
		List<BoonCdkey> cdkeys = boonCdkeyService.findByProduction(production);
		if (CollectionUtils.isNotEmpty(cdkeys)) {
			return result.fill(-6, "当前用途已使用,请更换内容");
		}

		// 解析配置并生成Cdkey
		JSONArray configsJSON = JSONObject.parseArray(configs);
		if (CollectionUtils.isEmpty(configsJSON)) {
			return result.fill(CommonConstant.CODE_ERROR_PARAM, CommonConstant.MSG_ERROR_PARAM);
		}
		for (Object o : configsJSON) {
			if (o != null) {
				JSONObject json = (JSONObject) o;
				String type = json.getString("type");
				String amount = json.getString("amount");
				String number = json.getString("number");

				Integer typeInt = null;
				if ("1".equals(type)) {
					typeInt = BoonConstant.BOON_CDKEY_TYPE_REDBAG;
				} else {
					typeInt = BoonConstant.BOON_CDKEY_TYPE_COIN;
				}
				Integer amountInt = NumberUtils.toInt(amount);
				Integer numberInt = NumberUtils.toInt(number);
				if (amountInt > 0 && numberInt > 0) {
					boonCdkeyService.produceCdkeys(typeInt, amountInt, numberInt, expireDate, production);
				}
			}
		}

		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
	}

	/**
	 * 根据生产用途导出
	 */
	@RequestMapping("statis")
	public ModelAndView statis(String production, String export) {
		ModelAndView mv = new ModelAndView("boon/statis");
		List<Map<String, Object>> statis = boonCdkeyService.statisByProduction(production);
		mv.addObject("statis", statis);
		mv.addObject("export", "1".equals(export));
		mv.addObject("production", production);
		return mv;
	}

	/**
	 * 按生产用途导出
	 */
	@RequestMapping("export")
	public ModelAndView export(HttpServletRequest req, HttpServletResponse res, String production) {
		List<Map<String, Object>> list = boonCdkeyService.queryByProduction(production);
		// 查询excel标题
		String title = "网娱大师兑换码";

		// 编辑excel内容
		String[][] contents = new String[list.size() + 1][];

		// 设置标题行
		String[] contentTitle = new String[5];
		contentTitle[0] = "类型";
		contentTitle[1] = "额度";
		contentTitle[2] = "兑换码";
		contentTitle[3] = "过期时间";
		contentTitle[4] = "用途";
		contents[0] = contentTitle;

		// 设置内容
		if (CollectionUtils.isNotEmpty(list)) {
			for (int i = 0; i < list.size(); i++) {
				Map<String, Object> verify = list.get(i);
				String[] row = new String[5];
				row[0] = MapUtils.getString(verify, "type");
				row[1] = MapUtils.getString(verify, "amount");
				row[2] = MapUtils.getString(verify, "cdkey");
				row[3] = MapUtils.getString(verify, "expiredDate");
				row[4] = MapUtils.getString(verify, "production");
				contents[i + 1] = row;
			}
		}

		try {
			ExcelUtils.exportExcel(title, contents, false, res);
		} catch (Exception e) {
			LOGGER.error("导出数据异常：", e);
		}
		return null;
	}

	/**
	 * 皮肤cdkey管理页面
	 */
	@RequestMapping("skin")
	public ModelAndView skin() {
		ModelAndView mv = new ModelAndView("boon/skin");
		return mv;
	}

	/**
	 * 导入皮肤cdkey
	 * @throws ParseException 
	 */
	@ResponseBody
	@RequestMapping("importSkin")
	public JsonResponseMsg importSkin(HttpServletRequest req) throws ParseException {
		JsonResponseMsg result = new JsonResponseMsg();

		// 获取excel
		MultipartFile file = Servlets.getMultipartFile(req, "file");
		if (file == null) {
			return result.fill(-1, "缺少文件");
		}

		Workbook wb = ExcelUtils.readMultipartFile(file);
		if (wb == null) {
			return result.fill(CommonConstant.CODE_ERROR_PARAM, CommonConstant.MSG_ERROR_PARAM);
		}
		Sheet[] sheets = wb.getSheets();
		if (ArrayUtils.isEmpty(sheets) || sheets.length <= 0) {
			return result.fill(CommonConstant.CODE_ERROR_PARAM, CommonConstant.MSG_ERROR_PARAM);
		}

		// 读取工作表及行
		List<BoonCdkey> cdkeys = Lists.newArrayList();
		Sheet sheet = wb.getSheet(0);
		int rows = sheet.getRows();
		if (rows > 1) {
			String production = "";
			String cdkey = "";
			String type = "";
			Date expiredDate = DateUtils.stringToDate("2022-08-10 2" + "3:59:59", DateUtils.YYYY_MM_DD_HH_MM_SS);
			Date now = new Date();

			for (int r = 1; r < rows; r++) {
				Cell[] cells = sheet.getRow(r);
				if (ArrayUtils.isNotEmpty(cells) && cells.length >= 1) {
					cdkey = cells[0].getContents();
					if (StringUtils.isBlank(cdkey)) {
						break;
					}
					type = cells[1].getContents();
					production = cells[2].getContents();
					if (StringUtils.isNotBlank(cdkey)) {
						BoonCdkey bc = new BoonCdkey();
						bc.setCdkey(cdkey);
						bc.setProduction(production);
						bc.setExpiredDate(expiredDate);
						bc.setType(NumberUtils.toInt(type));
						bc.setUpdateDate(now);
						bc.setCreateDate(now);
						cdkeys.add(bc);
					}
				}
			}
		}
		if (CollectionUtils.isEmpty(cdkeys)) {
			return result.fill(-2, "未识别到须导入的cdkey");
		}

		boonCdkeyService.save(cdkeys);

		// 通知api重载腾讯皮肤cdkey
		String apiServers = systemConfig.getApiServers();
		String[] apiServersSplit = StringUtils.split(apiServers, ",");
		if (ArrayUtils.isNotEmpty(apiServersSplit)) {
			for (String apiServer : apiServersSplit) {
				String url = apiServer + "cdkey/reloadTencentSkinCdkeys";
				HttpRequestUtil.sendGet(url, "");
			}
		}

		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS, cdkeys.size());
	}
}
