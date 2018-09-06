package com.miqtech.master.admin.web.controller.backend.netbar;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.collect.Lists;
import com.miqtech.master.admin.web.controller.backend.BaseController;
import com.miqtech.master.admin.web.util.Servlets;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.entity.netbar.NetbarTag;
import com.miqtech.master.service.netbar.resource.NetbarTagService;
import com.miqtech.master.utils.ExcelUtils;
import com.miqtech.master.vo.PageVO;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;

@Controller
@RequestMapping("/netbar/tag")
public class NetbarTagController extends BaseController {
	@Autowired
	private NetbarTagService netbarTagService;

	@RequestMapping("/list/{page}")
	public ModelAndView tagList(@PathVariable("page") Integer page, String name, Integer tagIdsFlag) {
		ModelAndView mv = new ModelAndView("/netbar/tagList");
		if (page == null || page <= 0) {
			page = 1;
		}
		if (tagIdsFlag == null) {
			tagIdsFlag = 0;
		}
		mv.addObject("tagIdsFlag", tagIdsFlag);
		mv.addObject("tagName", name);
		PageVO vo = netbarTagService.getTagList(name, page);
		pageModels(mv, vo.getList(), page, vo.getTotal());
		return mv;
	}

	@RequestMapping("/edit")
	public ModelAndView tagEdit(Long tagId) {
		ModelAndView mv = new ModelAndView("/netbar/tagEdit");
		if (tagId != null) {
			NetbarTag netbarTag = netbarTagService.fingById(tagId);
			mv.addObject("netbarTag", netbarTag);
		}
		return mv;
	}

	@RequestMapping("/save")
	@ResponseBody
	public JsonResponseMsg saveTag(Long tagId, String name, Integer level) {
		JsonResponseMsg result = new JsonResponseMsg();
		NetbarTag netbarTag = new NetbarTag();
		if (tagId != null) {
			netbarTag = netbarTagService.fingById(tagId);
		}
		if (StringUtils.isNotBlank(name)) {
			netbarTag.setName(name);
		}
		if (level != null) {
			netbarTag.setLevel(level);
		}
		if (netbarTag != null) {
			netbarTag.setValid(1);
			netbarTag.setIsPublish(1);
		}
		netbarTagService.save(netbarTag);
		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
	}

	@RequestMapping("/delete")
	@ResponseBody
	public JsonResponseMsg deleteTag(Long tagId) {
		JsonResponseMsg result = new JsonResponseMsg();
		NetbarTag netbarTag = new NetbarTag();
		if (tagId != null) {
			netbarTag = netbarTagService.fingById(tagId);
		}
		if (netbarTag != null) {
			netbarTag.setValid(0);
		}
		netbarTagService.save(netbarTag);
		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
	}

	@RequestMapping("/batchDelete")
	@ResponseBody
	public JsonResponseMsg batchDelete(Integer type, String deleteIds) {
		JsonResponseMsg result = new JsonResponseMsg();
		List<NetbarTag> deleteList = Lists.newArrayList();
		// 全部删除
		if (type != null && type == 1) {
			List<NetbarTag> tagList = netbarTagService.findAll(null);
			if (CollectionUtils.isEmpty(tagList)) {
				return result.fill(CommonConstant.CODE_ERROR_PARAM, "无可删除的标签");
			}
			deleteList.addAll(tagList);
		} else {
			// 删除选中部分
			deleteIds = StringUtils.substring(deleteIds, 0, deleteIds.length() - 1);
			List<NetbarTag> tagList = netbarTagService.findAll(deleteIds);
			deleteList.addAll(tagList);
		}
		if (CollectionUtils.isNotEmpty(deleteList)) {
			for (int i = 0; i < deleteList.size(); i++) {
				NetbarTag netbarTag = deleteList.get(i);
				netbarTag.setValid(0);
				deleteList.set(i, netbarTag);
			}
			netbarTagService.save(deleteList);
		}
		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
	}

	@RequestMapping("/import")
	@ResponseBody
	public JsonResponseMsg importFile(HttpServletRequest req) {
		JsonResponseMsg result = new JsonResponseMsg();
		MultipartFile file = Servlets.getMultipartFile(req, "file");
		Workbook excel = ExcelUtils.readMultipartFile(file);

		if (excel == null) {
			return result.fill(CommonConstant.CODE_ERROR_PARAM, "缺少文件");
		}

		// 将表格中的信息转化为对象
		List<NetbarTag> netbarTagList = Lists.newArrayList();
		Sheet sheet = excel.getSheet(0);
		int rows = sheet.getRows();
		if (rows > 1) {
			Date nowDate = new Date();
			for (int r = 1; r < rows; r++) {
				Cell[] cells = sheet.getRow(r);
				if (ArrayUtils.isNotEmpty(cells) && cells.length >= 1) {
					String name = cells[0].getContents();
					String level = cells[1].getContents();
					NetbarTag netbarTag = new NetbarTag();
					netbarTag.setName(name);
					netbarTag.setLevel(NumberUtils.toInt(level));
					netbarTag.setIsPublish(1);
					netbarTag.setValid(1);
					netbarTag.setCreateDate(nowDate);
					netbarTagList.add(netbarTag);
				}
			}
			netbarTagService.save(netbarTagList);
		}
		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
	}
}
