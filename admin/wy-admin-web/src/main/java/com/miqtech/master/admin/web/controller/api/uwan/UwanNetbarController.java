package com.miqtech.master.admin.web.controller.api.uwan;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.collect.Lists;
import com.miqtech.master.admin.web.controller.api.BaseController;
import com.miqtech.master.admin.web.util.Servlets;
import com.miqtech.master.config.SystemConfig;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.consts.uwan.UwanNetbarConstant;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.entity.uwan.UwanNetbar;
import com.miqtech.master.service.audition.AuditionMatchDetailLolService;
import com.miqtech.master.service.netbar.NetbarRankService;
import com.miqtech.master.service.uwan.UwanNetbarService;
import com.miqtech.master.thirdparty.util.uwan.UwanInterface;
import com.miqtech.master.utils.DateUtils;
import com.miqtech.master.utils.ExcelUtils;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;

@Controller
@RequestMapping("uwanNetbar")
public class UwanNetbarController extends BaseController {

	private static final Logger logger = LoggerFactory.getLogger(UwanNetbarController.class);

	@Autowired
	private SystemConfig systemConfig;
	@Autowired
	private UwanNetbarService uwanNetbarService;
	@Autowired
	private AuditionMatchDetailLolService auditionMatchDetailLolService;
	@Autowired
	private NetbarRankService netbarRankService;

	/**
	 * 导入优玩网吧(吧内赛及网吧大战类型)
	 */
	@ResponseBody
	@RequestMapping("import")
	public JsonResponseMsg importNetbars(HttpServletRequest req) {
		MultipartFile file = Servlets.getMultipartFile(req, "file");
		if (file == null) {
			return new JsonResponseMsg().fill(CommonConstant.CODE_ERROR_PARAM, "缺少文件");
		}

		Workbook wb = ExcelUtils.readMultipartFile(file);
		if (wb == null) {
			return new JsonResponseMsg().fill(CommonConstant.CODE_ERROR_PARAM, "无法读取excel内容");
		}

		String uwanGateway = systemConfig.getUwanGateway();
		Date now = new Date();
		Sheet sheet = wb.getSheet(0);
		int rows = sheet.getRows();
		List<UwanNetbar> updateUwanNetbars = Lists.newArrayList();
		for (int i = 1; i < rows; i++) {
			Cell[] cells = sheet.getRow(i);
			if (cells == null || cells.length < 3) {
				logger.error("数据字段缺失,行号:" + i);
				continue;
			}

			String wyNetbarIdStr = cells[0].getContents();
			String uwanNetbarIdStr = cells[1].getContents();
			String netbarName = cells[2].getContents();
			if (!NumberUtils.isNumber(wyNetbarIdStr) || !NumberUtils.isNumber(uwanNetbarIdStr)) {
				logger.error("网吧ID格式化异常,wyNetbarId:" + wyNetbarIdStr + ";uwanNetbarId:" + uwanNetbarIdStr + ";");
				continue;
			}

			UwanNetbar updateUwanNetbar = new UwanNetbar();
			long uwanBarId = NumberUtils.toLong(uwanNetbarIdStr);
			updateUwanNetbar.setUwanBarId(uwanBarId);
			long netbarId = NumberUtils.toLong(wyNetbarIdStr);
			updateUwanNetbar.setNetbarId(netbarId);
			updateUwanNetbar.setNetbarType(UwanNetbarConstant.TYPE_NETBAR);
			updateUwanNetbar.setSource(UwanNetbarConstant.SOURCE_NETBAR);
			updateUwanNetbar.setUpdateDate(now);
			updateUwanNetbars.add(updateUwanNetbar);
			logger.debug("增加一条待导入网吧,netbarId:" + wyNetbarIdStr);

			boolean binded = UwanInterface.bindNetbar(uwanGateway, netbarId, uwanBarId, netbarName);
			if (!binded) {
				logger.error("网吧绑定失败,netbarId:" + netbarId + ",netbarName:" + netbarName);
			}
		}

		uwanNetbarService.batchUpdate(updateUwanNetbars);
		return new JsonResponseMsg().fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
	}

	/**
	 * 获取昨天网吧战的数据
	 */
	@ResponseBody
	@RequestMapping("saveYesterdayNetbarMatches")
	public JsonResponseMsg saveYesterdayNetbarMatches(String date) {
		try {
			Date queryDate = DateUtils.stringToDate(date, DateUtils.YYYY_MM_DD);
			Calendar c = Calendar.getInstance();
			c.setTime(queryDate);
			c.set(Calendar.HOUR_OF_DAY, 0);
			c.set(Calendar.MINUTE, 0);
			c.set(Calendar.SECOND, 0);
			c.set(Calendar.MILLISECOND, 0);
			long startMatchTime = c.getTimeInMillis();
			c.add(Calendar.DAY_OF_YEAR, 1);
			long endMatchTime = c.getTimeInMillis();
			auditionMatchDetailLolService.saveNetbarMatches(startMatchTime, endMatchTime);
		} catch (Exception e) {
			logger.error("格式化时间异常:", e);
		}
		return new JsonResponseMsg().fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
	}

	/**
	 * 产生网吧排行
	 */
	@ResponseBody
	@RequestMapping("generateNetbarRank")
	public JsonResponseMsg generateNetbarRank() {
		netbarRankService.generateNetbarRank();
		return new JsonResponseMsg().fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
	}

	/**
	 * 拉取最近的比赛数据
	 */
	@ResponseBody
	@RequestMapping("pullRecent")
	public JsonResponseMsg pullRecent(String startDate, String count) throws ParseException {
		Date queryDate = null;
		try {
			queryDate = DateUtils.stringToDate(startDate, DateUtils.YYYY_MM_DD);
		} catch (Exception e) {
		}
		int countInt = NumberUtils.toInt(count, 0);
		if (queryDate == null || countInt <= 0) {
			return new JsonResponseMsg().fill(CommonConstant.CODE_ERROR_PARAM, CommonConstant.MSG_ERROR_PARAM);
		}

		Calendar c = Calendar.getInstance();
		c.setTime(queryDate);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		for (int i = 0; i < countInt; i++) {
			long startMatchTime = c.getTimeInMillis();
			c.add(Calendar.DAY_OF_YEAR, 1);
			long endMatchTime = c.getTimeInMillis();
			auditionMatchDetailLolService.saveNetbarMatches(startMatchTime, endMatchTime);
		}

		return new JsonResponseMsg().fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
	}

}
