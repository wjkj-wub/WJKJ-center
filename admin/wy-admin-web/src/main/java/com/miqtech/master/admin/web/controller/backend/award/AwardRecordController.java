package com.miqtech.master.admin.web.controller.backend.award;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.collect.Maps;
import com.miqtech.master.admin.web.controller.backend.BaseController;
import com.miqtech.master.entity.amuse.AmuseActivityInfo;
import com.miqtech.master.service.amuse.AmuseActivityInfoService;
import com.miqtech.master.service.award.AwardRecordService;
import com.miqtech.master.utils.DateUtils;
import com.miqtech.master.utils.ExcelUtils;
import com.miqtech.master.utils.StringUtils;
import com.miqtech.master.vo.PageVO;

@Controller
@RequestMapping("award/record")
public class AwardRecordController extends BaseController {

	private static final Logger LOGGER = LoggerFactory.getLogger(AwardRecordController.class);

	@Autowired
	private AmuseActivityInfoService amuseActivityService;
	@Autowired
	private AwardRecordService awardRecordService;

	/**
	 * 分页
	 */
	@RequestMapping("list/{page}")
	public ModelAndView page(@PathVariable("page") int page, String account, String activityId, String startDate,
			String endDate, String type, String subType) {
		ModelAndView mv = new ModelAndView("award/recordList");

		if (StringUtils.isBlank(type)) {
			type = "1";
		}

		Map<String, String> searchParams = Maps.newHashMap();
		searchParams.put("account", account);
		searchParams.put("activityId", activityId);
		searchParams.put("startDate", startDate);
		searchParams.put("endDate", endDate);
		searchParams.put("type", type);
		searchParams.put("subType", subType);
		mv.addObject("params", searchParams);
		List<AmuseActivityInfo> activities = amuseActivityService.findAllValid();
		mv.addObject("activities", activities);

		PageVO vo = awardRecordService.page(page, searchParams);
		pageModels(mv, vo.getList(), page, vo.getTotal());

		return mv;
	}

	/**
	 * 导出审核列表
	 */
	@RequestMapping("export/{page}")
	public ModelAndView exportPersonal(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("page") int page, String account, String activityId, String startDate, String endDate,
			String type, String subType) {
		ModelAndView mv = new ModelAndView("amuse/verifyList");

		Map<String, String> searchParams = Maps.newHashMap();
		searchParams.put("account", account);
		searchParams.put("activityId", activityId);
		searchParams.put("startDate", startDate);
		searchParams.put("endDate", endDate);
		searchParams.put("type", type);
		searchParams.put("subType", subType);
		mv.addObject("params", searchParams);
		PageVO vo = awardRecordService.page(page, searchParams);
		List<Map<String, Object>> list = vo.getList();

		// 查询excel标题
		String title = "库存";

		// 编辑excel内容
		String[][] contents = new String[list.size() + 1][];

		// 设置标题行
		String[] contentTitle = new String[7];
		contentTitle[0] = "订单编号";
		contentTitle[1] = "领奖者账号";
		contentTitle[2] = "网娱账号";
		contentTitle[3] = "赛事名";
		contentTitle[4] = "奖品类别";
		contentTitle[5] = "数量";
		contentTitle[6] = "发放时间";
		contents[0] = contentTitle;

		// 设置内容
		for (int i = 0; i < list.size(); i++) {
			Map<String, Object> verify = list.get(i);
			String[] row = new String[7];
			row[0] = MapUtils.getString(verify, "serial");
			Integer awardType = MapUtils.getInteger(verify, "awardType");
			Integer awardSubType = MapUtils.getInteger(verify, "awardSubType");
			String accountStr = StringUtils.EMPTY;
			if (awardType != null) {
				if (awardType.equals(1)) {
					accountStr = MapUtils.getString(verify, "username");
				} else if (awardType.equals(2)) {
					if (awardSubType != null) {
						if (awardSubType.equals(3) || awardSubType.equals(4)) {
							accountStr = MapUtils.getString(verify, "telephone");
						} else if (awardSubType.equals(5)) {
							accountStr = MapUtils.getString(verify, "qq");
						}
					}
				} else if (awardType.equals(3)) {
					accountStr = MapUtils.getString(verify, "username");
				}
			}
			row[1] = accountStr;
			row[2] = MapUtils.getString(verify, "username");
			row[3] = MapUtils.getString(verify, "activityTitle");
			String awardName = StringUtils.EMPTY;
			if (awardType != null) {
				if (awardType != null) {
					if (awardType.equals(1)) {
						if (awardSubType != null) {
							if (awardSubType.equals(1)) {
								awardName = "红包";
							} else if (awardSubType.equals(2)) {
								awardName = "金币";
							}
						}
					} else if (awardType.equals(2)) {
						if (awardSubType != null) {
							if (awardSubType.equals(3)) {
								awardName = "话费";
							} else if (awardSubType.equals(4)) {
								awardName = "流量";
							} else if (awardSubType.equals(5)) {
								awardName = "Q币";
							}
						}
					} else if (awardType.equals(3)) {
						awardName = MapUtils.getString(verify, "awardTypeName");
					}
				}
			}
			row[4] = awardName;
			row[5] = MapUtils.getString(verify, "amount");
			Date createDate = (Date) verify.get("createDate");
			String createDateStr = StringUtils.EMPTY;
			if (createDate != null) {
				createDateStr = DateUtils.dateToString(createDate, DateUtils.YYYY_MM_DD_HH_MM_SS);
			}
			row[6] = createDateStr;
			contents[i + 1] = row;
		}

		try {
			ExcelUtils.exportExcel(title, contents, false, response);
		} catch (Exception e) {
			LOGGER.error("导出数据异常：", e);
		}
		return null;
	}
}
