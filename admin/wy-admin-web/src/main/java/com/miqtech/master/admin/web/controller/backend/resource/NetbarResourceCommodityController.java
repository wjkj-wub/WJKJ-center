package com.miqtech.master.admin.web.controller.backend.resource;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
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

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.miqtech.master.admin.web.controller.backend.BaseController;
import com.miqtech.master.admin.web.util.Servlets;
import com.miqtech.master.config.SystemConfig;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.consts.NetbarConstant;
import com.miqtech.master.consts.OperateLogConstant;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.entity.common.OperateLog;
import com.miqtech.master.entity.common.SystemUser;
import com.miqtech.master.entity.netbar.resource.NetbarResourceCommodity;
import com.miqtech.master.entity.netbar.resource.NetbarResourceCommodityProperty;
import com.miqtech.master.service.common.OperateLogService;
import com.miqtech.master.service.netbar.resource.NetbarCommodityCategoryService;
import com.miqtech.master.service.netbar.resource.NetbarResourceCommodityPropertyService;
import com.miqtech.master.service.netbar.resource.NetbarResourceCommodityService;
import com.miqtech.master.service.system.SysUserAreaService;
import com.miqtech.master.service.system.SystemAreaService;
import com.miqtech.master.thirdparty.util.img.ImgUploadUtil;
import com.miqtech.master.utils.ExcelUtils;
import com.miqtech.master.vo.PageVO;

@Controller
@RequestMapping("netbar/resource/commodity")
public class NetbarResourceCommodityController extends BaseController {
	private static final Logger LOGGER = LoggerFactory.getLogger(NetbarResourceCommodityController.class);
	@Autowired
	private SystemConfig systemConfig;
	@Autowired
	private NetbarResourceCommodityService netbarResourceCommodityService;
	@Autowired
	private NetbarResourceCommodityPropertyService netbarResourceCommodityPropertyService;
	@Autowired
	private NetbarCommodityCategoryService netbarCommodityCategoryService;
	@Autowired
	private SystemAreaService systemAreaService;
	@Autowired
	private SysUserAreaService sysUserAreaService;
	@Autowired
	private OperateLogService operateLogService;

	/*
	 * 列表
	 */
	@RequestMapping("/list/{page}")
	public ModelAndView list(HttpServletRequest request, @PathVariable("page") int page, String name, String beginDate,
			String endDate, String areaCode, String categoryPid, String categoryId, String status, String order,
			String qualifications) {
		ModelAndView mv = new ModelAndView("resource/commodityList");
		Map<String, Object> params = Maps.newHashMap();
		params.put("name", name);
		params.put("beginDate", beginDate);
		params.put("endDate", endDate);
		params.put("categoryId", categoryId);
		params.put("categoryPid", categoryPid);
		params.put("status", status);
		params.put("qualifications", qualifications);
		params.put("order", order);

		SystemUser user = Servlets.getSessionUser(request);

		String userAreaCode = sysUserAreaService.findUserAreaCode(user.getId());
		userAreaCode = null == userAreaCode ? "000000" : userAreaCode;
		boolean isActivityAdmin = false;
		if (StringUtils.startsWith(userAreaCode, "00")) {
			isActivityAdmin = true;
			mv.addObject("provinces", systemAreaService.queryAllRoot());
			params.put("areaCode", areaCode);
		} else {
			String provinceAreaCode = StringUtils.substring(userAreaCode, 0, 2) + "0000";
			mv.addObject("provinces", ImmutableList.of(systemAreaService.findByCode(provinceAreaCode)));
			mv.addObject("userAreaCode", userAreaCode);

			// 管理员可搜索区域仅全国与自己归属地
			if (StringUtils.isBlank(areaCode)) {
				areaCode = "000000";
			} else if (!"000000".equals(areaCode)) {
				areaCode = provinceAreaCode;
			}
			params.put("areaCode", areaCode);
		}

		params.put("isActivityAdmin", isActivityAdmin);
		PageVO pageVO = netbarResourceCommodityService.pageListWyadmin(page, params);
		pageModels(mv, pageVO.getList(), page, pageVO.getTotal());
		mv.addObject("params", params);
		mv.addObject("categorys", netbarCommodityCategoryService.getCategoryByPid(NumberUtils.toLong(categoryPid, -1)));
		mv.addObject("superCategorys", netbarCommodityCategoryService.getSuperCategory());
		mv.addObject("commoditys", netbarResourceCommodityService.getidsAndNames());
		mv.addObject("tops", netbarResourceCommodityService.getTopCommodity(userAreaCode));
		return mv;
	}

	/*
	 * 根据大类别id查旗下小类别
	 */
	@ResponseBody
	@RequestMapping("/categorys/{pid}")
	public JsonResponseMsg getcategorysByPid(@PathVariable("pid") long pid) {
		JsonResponseMsg result = new JsonResponseMsg();

		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS,
				netbarCommodityCategoryService.getCategoryByPid(pid));
		return result;
	}

	private String handelIds(String ids) {
		ids = ids.replaceAll(" ", "");
		if (StringUtils.isNotBlank(ids)) {
			if (ids.startsWith(",")) {
				ids = ids.substring(1, ids.length());
			}
			if (ids.endsWith(",")) {
				ids = ids.substring(0, ids.length() - 1);
			}
		}

		return ids;
	}

	/*
	 * 请求对象信息
	 */
	@ResponseBody
	@RequestMapping("/info/{id}")
	public JsonResponseMsg detail(@PathVariable("id") long id) {
		JsonResponseMsg result = new JsonResponseMsg();

		Map<String, Object> map = Maps.newHashMap();
		NetbarResourceCommodity commodity = netbarResourceCommodityService.findById(id);
		map.put("commodity", commodity);
		long categoryId = null == commodity.getCategoryId() ? 0 : commodity.getCategoryId();
		map.put("category", netbarCommodityCategoryService.findById(categoryId));

		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS, map);
		return result;
	}

	/*
	 * 编辑
	 */
	@ResponseBody
	@RequestMapping("/save")
	public JsonResponseMsg save(HttpServletRequest req, NetbarResourceCommodity netbarResourceCommodity) {
		JsonResponseMsg result = new JsonResponseMsg();

		saveCommodity(req, netbarResourceCommodity);

		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
		return result;
	}

	private NetbarResourceCommodity saveCommodity(HttpServletRequest req,
			NetbarResourceCommodity netbarResourceCommodity) {
		//上传图片
		MultipartFile iconFile = Servlets.getMultipartFile(req, "icon_file");
		if (null != iconFile) {
			if (null != netbarResourceCommodity) {
				String systemName = "wy-web-admin";
				String src = ImgUploadUtil.genFilePath("netbar_resource");
				Map<String, String> imgPath = ImgUploadUtil.save(iconFile, systemName, src);
				netbarResourceCommodity.setUrl(imgPath.get(ImgUploadUtil.KEY_MAP_SRC));
			}
		}
		return netbarResourceCommodityService.save(netbarResourceCommodity);
	}

	/*
	 * 上下架（批量）
	 */
	@ResponseBody
	@RequestMapping("/changeStatus/{ids}/{oper}")
	public JsonResponseMsg changeStatus(HttpServletRequest req, @PathVariable("ids") String ids,
			@PathVariable("oper") int oper) {
		JsonResponseMsg result = new JsonResponseMsg();

		List<Map<String, Object>> properties = netbarResourceCommodityPropertyService.queryByIds(ids);
		if (CollectionUtils.isEmpty(properties)) {
			return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
		}
		for (Map<String, Object> p : properties) {
			Integer status = MapUtils.getInteger(p, "status");
			String name = MapUtils.getString(p, "name");
			if (oper == 2) {// 上架,状态须为 0下架 或 3已确认
				if (status != 0 && status != 3) {
					return result.fill(CommonConstant.CODE_ERROR_LOGIC, "商品 " + name + " 的当前状态不允许上架操作");
				}
			} else {// 下架,状态须为 2发布中
				if (status != 2) {
					return result.fill(CommonConstant.CODE_ERROR_LOGIC, "商品 " + name + " 的当前状态不允许下架操作");
				}
			}
		}

		ids = handelIds(ids);
		ids = netbarResourceCommodityPropertyService.queryPidsBYCids(ids);
		if (StringUtils.isNotBlank(ids)) {
			// 上架操作在确认短信中更新状态,下架操作直接下架商品
			netbarResourceCommodityPropertyService.changeStatus(ids, oper);

			// 增加操作记录
			SystemUser sysUser = Servlets.getSessionUser(req);
			List<OperateLog> logs = Lists.newArrayList();
			Iterable<String> idIter = Splitter.on(",").split(ids);
			for (String id : idIter) {
				long idLong = NumberUtils.toLong(id);
				// 记录商品上架下架日志
				String operate = StringUtils.EMPTY;
				Integer logType = null;
				if (oper == 2) {
					operate = "上架商品";
					logType = OperateLogConstant.TYPE_ADMIN_RESOURCE_RELEASE_COMMODITY;
				} else if (oper == 0) {
					operate = "下架商品";
					logType = OperateLogConstant.TYPE_ADMIN_RESOURCE_CLOSE_COMMODITY;
				}

				if (StringUtils.isNotBlank(operate)) {
					OperateLog operateLog = new OperateLog(OperateLogConstant.SYS_TYPE_ADMIN, sysUser.getId(), idLong,
							logType, operate);
					logs.add(operateLog);
				}
			}
			if (CollectionUtils.isNotEmpty(logs)) {
				operateLogService.save(logs);
			}
		}

		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
		return result;
	}

	/*
	 * 置顶/取消（批量）
	 */
	@ResponseBody
	@RequestMapping("/changetop/{ids}/{oper}")
	public JsonResponseMsg changetop(HttpServletRequest request, @PathVariable("ids") String ids,
			@PathVariable("oper") int oper) {
		JsonResponseMsg result = new JsonResponseMsg();
		oper = oper == -1 ? 0 : oper;//0：取消置顶 1：设置置顶
		ids = handelIds(ids);//处理id：处理首字母为，结尾字母为，
		SystemUser user = Servlets.getSessionUser(request);
		String userAreaCode = sysUserAreaService.findUserAreaCode(user.getId());
		userAreaCode = null == userAreaCode ? "000000" : userAreaCode;

		int topLimitNum = userAreaCode.equals("000000") ? 2 : 5;
		int operateFlag = netbarResourceCommodityService.topStatus(oper, ids, userAreaCode, topLimitNum);
		if (operateFlag == -1) {
			result.fill(CommonConstant.CODE_ERROR_PARAM, "您提交操作的数据状态包含已置顶和未置顶两种数据！");
			return result;
		} else if (operateFlag == -2) {
			result.fill(CommonConstant.CODE_ERROR_PARAM, "待操作的数据不能跨省份！");
			return result;
		} else if (operateFlag == -3) {
			if (topLimitNum == 5) {
				result.fill(CommonConstant.CODE_ERROR_PARAM, "该省份置顶商品数量不能超过5个！");
				return result;
			} else {
				result.fill(CommonConstant.CODE_ERROR_PARAM, "全国地区的商品最多置顶数量为2个，请先取消别的商品置顶！");
				return result;
			}
		}

		String[] idArray = StringUtils.split(ids, ",");

		String nos = netbarResourceCommodityService.queryTopNos(userAreaCode);
		for (String id : idArray) {
			if (oper == 1) {
				for (int i = 1; i <= topLimitNum; i++) {
					if (!nos.contains(String.valueOf(i))) {
						netbarResourceCommodityService.changetop(NumberUtils.toLong(id), i);
						nos = nos + "," + String.valueOf(i);
						break;
					}
				}

			} else {
				netbarResourceCommodityService.changetop(NumberUtils.toLong(id), oper);
			}
		}

		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
		return result;
	}

	/*
	 * 置顶顺序管理
	 */
	@ResponseBody
	@RequestMapping("/topManage/{ids}/{nos}")
	public JsonResponseMsg topManage(@PathVariable("ids") String ids, @PathVariable("nos") String nos) {
		JsonResponseMsg result = new JsonResponseMsg();

		String[] idArray = ids.split(",");
		String[] noArray = nos.split(",");
		int idLen = idArray.length;
		int noLen = noArray.length;
		if (noLen < idLen) {
			result.fill(CommonConstant.CODE_ERROR_PARAM, "排序号不能为空");
			return result;
		}
		for (int i = 0; i < noLen; i++) {
			netbarResourceCommodityService.changeTopByIdAndNo(NumberUtils.toLong(idArray[i]),
					NumberUtils.toInt(noArray[i]));

		}

		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
		return result;
	}

	/**
	 * 导出excel
	 */
	@RequestMapping("/exportExcel")
	public ModelAndView exportInfo(HttpServletResponse res, String page, String name, String beginDate, String endDate,
			String areaCode, String categoryId, String categoryPid, String status, String qualifications,
			String order) {
		// 查询报名数据
		int pageInt = NumberUtils.toInt(page, 1);
		Map<String, Object> params = Maps.newHashMap();
		params.put("name", name);
		params.put("beginDate", beginDate);
		params.put("endDate", endDate);
		params.put("areaCode", areaCode);
		params.put("categoryId", categoryId);
		params.put("categoryPid", categoryPid);
		params.put("status", status);
		params.put("qualifications", qualifications);
		params.put("order", order);
		params.put("noLimit", "1");
		List<Map<String, Object>> list = netbarResourceCommodityService.pageListWyadmin(pageInt, params).getList();

		// Excel标题
		String title = "资源商城商品信息";

		// 编辑excel内容
		String[][] contents = new String[list.size() + 1][];

		// 设置标题行
		String[] contentTitle = new String[7];
		contentTitle[0] = "商品名称";
		contentTitle[1] = "大类别";
		contentTitle[2] = "细分";
		contentTitle[3] = "地区";
		contentTitle[4] = "资格";
		contentTitle[5] = "联系人";
		contentTitle[6] = "联系人电话";
		contents[0] = contentTitle;

		// 设置内容
		if (CollectionUtils.isNotEmpty(list)) {
			for (int i = 0; i < list.size(); i++) {
				Map<String, Object> map = list.get(i);
				String[] row = new String[7];
				row[0] = MapUtils.getString(map, "name", "");
				row[1] = MapUtils.getString(map, "typeNameP", "");
				row[2] = MapUtils.getString(map, "typeName", "");
				row[3] = MapUtils.getString(map, "areaName", "");
				int qualificationInt = MapUtils.getIntValue(map, "qualifications", 0);
				row[4] = qualificationInt == 1 ? "会员"
						: qualificationInt == 2 ? "金牌" : qualificationInt == 3 ? "钻石" : "无";
				row[5] = MapUtils.getString(map, "executes", "");
				row[6] = MapUtils.getString(map, "execute_phone", "");
				contents[i + 1] = row;
			}
		}
		// 导出Excel
		try {
			ExcelUtils.exportExcel(title, contents, res);
		} catch (Exception e) {
			LOGGER.error("导出数据异常：", e);
		}
		return null;
	}

	/**
	 * 预览
	 */
	@RequestMapping("preview")
	public ModelAndView preview(String commodityId, String propertyId) {
		ModelAndView mv = new ModelAndView("resource/preview");

		// 查询当前商品
		Long commodityIdLong = NumberUtils.toLong(commodityId, 0);
		NetbarResourceCommodity commodity = netbarResourceCommodityService.findValidByIdWithProperites(commodityIdLong);
		mv.addObject("commodity", commodity);

		// 查询当前商品项目
		Long propertyIdLong = NumberUtils.toLong(propertyId);
		NetbarResourceCommodityProperty property = null;
		List<NetbarResourceCommodityProperty> properties = null;
		if (commodity != null) {
			properties = commodity.getProperties();
		}
		if (propertyIdLong > 0 && commodity != null && CollectionUtils.isNotEmpty(properties)) {
			for (NetbarResourceCommodityProperty p : commodity.getProperties()) {
				if (propertyIdLong.equals(p.getId())) {
					property = p;
					break;
				}
			}
		}
		// 匹配不出项目时取第一个
		if (property == null && CollectionUtils.isNotEmpty(properties)) {
			property = properties.get(0);
		}
		if (property == null) {
			return mv;
		}
		mv.addObject("property", property);

		// 根据项目的类别,匹配页面
		if (NetbarConstant.NETBAR_RESOURCE_COMMODITY_CATE_TYPE_NUMBER.equals(property.getCateType())) {
			mv.setViewName("resource/preview_redbag");
		} else {
			// 档期类型,按项目名分组档期
			Map<String, List<NetbarResourceCommodityProperty>> nameProperties = Maps.newHashMap();
			if (CollectionUtils.isNotEmpty(properties)) {
				for (NetbarResourceCommodityProperty p : properties) {
					String propertyName = p.getPropertyName();
					List<NetbarResourceCommodityProperty> ps = nameProperties.get(propertyName);
					if (ps == null) {
						ps = Lists.newArrayList();
					}
					ps.add(p);
					nameProperties.put(propertyName, ps);
				}
			}
			mv.addObject("nameProperties", nameProperties);
			mv.addObject("namePropertiesJSON", JSONObject.toJSONString(nameProperties));
		}

		// 必须购买商品的项目，匹配出商品信息
		if (NetbarConstant.NETBAR_RESOURCE_PROPERTY_QUALIFI_COMMODITY.equals(property.getQualifiType())) {
			Float conditions = property.getConditions();
			if (conditions != null) {
				NetbarResourceCommodity c = netbarResourceCommodityService.findValidById(conditions.longValue());
				mv.addObject("conditionsCommodity", c);
			}
		}

		// 获取商品地区及订单信息
		Map<String, Object> areaAndSales = netbarResourceCommodityService.queryAreaAndSalesById(commodityIdLong);
		mv.addObject("areaAndSales", areaAndSales);

		mv.addObject("imgServer", systemConfig.getImgServerDomain());
		return mv;
	}

}
