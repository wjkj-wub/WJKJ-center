package com.miqtech.master.admin.web.controller.backend.resource;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.miqtech.master.admin.web.controller.backend.BaseController;
import com.miqtech.master.admin.web.util.Servlets;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.consts.OperateLogConstant;
import com.miqtech.master.consts.SystemUserConstant;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.entity.common.OperateLog;
import com.miqtech.master.entity.common.SysValueAddedCard;
import com.miqtech.master.entity.common.SystemUser;
import com.miqtech.master.entity.netbar.resource.NetbarCommodityCategory;
import com.miqtech.master.entity.netbar.resource.NetbarResourceCommodity;
import com.miqtech.master.entity.netbar.resource.NetbarResourceCommodityProperty;
import com.miqtech.master.service.common.OperateLogService;
import com.miqtech.master.service.netbar.resource.NetbarCommodityCategoryService;
import com.miqtech.master.service.netbar.resource.NetbarResourceCommodityPropertyService;
import com.miqtech.master.service.netbar.resource.NetbarResourceCommodityService;
import com.miqtech.master.service.system.SysUserAreaService;
import com.miqtech.master.service.system.SysValueAddedCardService;
import com.miqtech.master.service.system.SystemAreaService;
import com.miqtech.master.thirdparty.util.img.ImgUploadUtil;
import com.miqtech.master.utils.BeanUtils;
import com.miqtech.master.utils.DateUtils;
import com.miqtech.master.utils.ExcelUtils;
import com.miqtech.master.utils.StringUtils;
import com.miqtech.master.vo.PageVO;

@Controller
@RequestMapping("netbar/resource")
public class NetbarResourceCommodityPropertyController extends BaseController {
	private static final Logger LOGGER = LoggerFactory.getLogger(NetbarResourceCommodityPropertyController.class);
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

	private final String VALUE_ADDED_CARD_2 = "uploads/imgs/netbar_resource/2016/05/23/2.png";
	private final String VALUE_ADDED_CARD_5 = "uploads/imgs/netbar_resource/2016/05/23/5.png";
	private final String VALUE_ADDED_CARD_10 = "uploads/imgs/netbar_resource/2016/05/23/10.png";
	private final String VALUE_ADDED_CARD_50 = "uploads/imgs/netbar_resource/2016/05/23/50.png";
	private final String VALUE_ADDED_CARD_100 = "uploads/imgs/netbar_resource/2016/05/23/100.png";

	/*
	 * 列表
	 */
	@RequestMapping("/list/{page}")
	public ModelAndView list(HttpServletRequest request, @PathVariable("page") int page, String cid, String pname,
			String beginDate, String endDate, String areaCode, String categoryPid, String categoryId, String status,
			String order, String qualifications) {
		ModelAndView mv = new ModelAndView("resource/commodityPropertyList");
		Map<String, Object> params = Maps.newHashMap();
		params.put("cid", cid);
		params.put("pname", pname);
		params.put("beginDate", beginDate);
		params.put("endDate", endDate);
		params.put("areaCode", areaCode);
		params.put("categoryId", categoryId);
		params.put("categoryPid", categoryPid);
		params.put("status", status);
		params.put("qualifications", qualifications);
		params.put("order", order);

		SystemUser user = Servlets.getSessionUser(request);
		boolean isActivityAdmin = SystemUserConstant.TYPE_ACTIVITY_ADMIN.equals(user.getUserType());
		if (!isActivityAdmin) { //最高管理员
			params.put("areaCode", areaCode); //精确到省级
			mv.addObject("provinces", systemAreaService.queryAllRoot());
		} else { //子账号
			String userAreaCode = sysUserAreaService.findUserAreaCode(user.getId()); // 匹配子账号的地区
			userAreaCode = null == userAreaCode ? "000000" : userAreaCode;
			params.put("areaCode", userAreaCode);

			mv.addObject("provinces", ImmutableList.of(systemAreaService.findByCode(userAreaCode)));
		}
		params.put("isActivityAdmin", isActivityAdmin);

		PageVO pageVO = netbarResourceCommodityPropertyService.pageList(page, params);
		pageModels(mv, pageVO.getList(), page, pageVO.getTotal());
		mv.addObject("params", params);
		mv.addObject("categorys", netbarCommodityCategoryService.getCategoryByPid(NumberUtils.toLong(categoryPid, -1)));
		mv.addObject("superCategorys", netbarCommodityCategoryService.getSuperCategory());
		mv.addObject("commoditys", netbarResourceCommodityService.getidsAndNames());
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

	/*
	 * 新增/更新 isredbag :代表增值券
	 */
	@ResponseBody
	@RequestMapping("/save")
	public JsonResponseMsg save(HttpServletRequest req, NetbarResourceCommodity netbarResourceCommodity,
			NetbarResourceCommodityProperty netbarResourceCommodityProperty, String isRedbag, int isNew,
			String categoryPid, String catePid, String redbagPriceStr, String isC, String cid) {
		JsonResponseMsg result = new JsonResponseMsg();

		if (isNew == 0) {
			netbarResourceCommodity.setId(netbarResourceCommodityProperty.getCommodityId());
		}
		int redbagPrice = NumberUtils.toInt(redbagPriceStr);
		if ("1".equals(isRedbag)) {
			netbarResourceCommodity.setName("网娱" + redbagPriceStr + "元增值券");
			netbarResourceCommodityProperty.setMeasure("/个");
			if (redbagPrice == 2) {
				netbarResourceCommodity.setUrl(VALUE_ADDED_CARD_2);
			} else if (redbagPrice == 5) {
				netbarResourceCommodity.setUrl(VALUE_ADDED_CARD_5);
			} else if (redbagPrice == 10) {
				netbarResourceCommodity.setUrl(VALUE_ADDED_CARD_10);
			} else if (redbagPrice == 50) {
				netbarResourceCommodity.setUrl(VALUE_ADDED_CARD_50);
			} else if (redbagPrice == 100) {
				netbarResourceCommodity.setUrl(VALUE_ADDED_CARD_100);
			}
		}
		if ("1".equals(isC)) { //商品页面新增或编辑
			netbarResourceCommodity = saveCommodity(req, netbarResourceCommodity);
			cid = netbarResourceCommodity.getId().toString();
			if (isNew == 1) {
				SystemUser sysUser = Servlets.getSessionUser(req);
				operateLogService.adminOperateLog(sysUser.getId(), netbarResourceCommodity.getId(),
						OperateLogConstant.TYPE_ADMIN_RESOURCE_ADD_COMMODITY,
						"录入商品,商品名:" + netbarResourceCommodity.getName());

				// 新增商品时，发送确认的短信通知
				if (isNew != 0 && !"1".equals(isRedbag)) {
					NetbarCommodityCategory category = netbarCommodityCategoryService
							.findById(netbarResourceCommodity.getCategoryId());
					if (category != null && category.getPid().longValue() == 1) {
						netbarResourceCommodityPropertyService
								.notifyWangyuCommodityAdmin(netbarResourceCommodity.getId());
					} else {
						//当是网娱的自由产品 只通知短信,不进行确认
						netbarResourceCommodityPropertyService.enableNotify(netbarResourceCommodity.getId());
					}
				}
			}
		} else {
			netbarResourceCommodity.setId(NumberUtils.toLong(cid));
			categoryPid = catePid;
		}

		netbarResourceCommodityProperty.setCommodityId(netbarResourceCommodity.getId());
		List<NetbarResourceCommodityProperty> blocks = netbarResourceCommodity.getBlocks();
		if (CollectionUtils.isNotEmpty(blocks)) {
			for (NetbarResourceCommodityProperty block : blocks) {
				int dateOrNum = block.getCateType();
				if (dateOrNum == 0) {
					String resultDate = checkSettlDate(netbarResourceCommodity.getId(), block.getPropertyName(),
							block.getSettlDates(), netbarResourceCommodityProperty.getId());
					if (StringUtils.isNotBlank(resultDate)) {
						result.fill(CommonConstant.CODE_ERROR_PARAM,
								block.getPropertyName() + "项目下已存在档期：" + resultDate);
						return result;
					}
				}

				int qualifyType = null == block.getQualifiType() ? 0 : block.getQualifiType();
				if (qualifyType == 2) {
					block.setConditions(NumberUtils.toFloat(block.getConditionsId().toString()));
				}
				block = BeanUtils.updateBean(block, netbarResourceCommodityProperty);

				if ("1".equals(isRedbag)) {
					block.setPropertyName(redbagPrice + "元增值券");
					block.setPrice((float) redbagPrice);
					block.setRedbagId(
							createSysValueAddedCard(redbagPrice, block.getValidity(), block.getConditions()).getId());
					block.setStatus(3);
				} else {
					block.setUnit(1);
					block.setStatus(1);
				}

				if (block.getRebate() == null && block.getPrice() != null) {
					block.setRebate(block.getPrice());
				}

				NetbarCommodityCategory category = netbarCommodityCategoryService
						.findById(NumberUtils.toLong(categoryPid));
				String phone = netbarResourceCommodityService.findById(NumberUtils.toLong(cid)).getExecutePhone();

				saveProperty(block, isNew, phone, category.getName());
			}
		}

		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
		return result;
	}

	@Autowired
	private SysValueAddedCardService sysValueAddedCardService;

	private SysValueAddedCard createSysValueAddedCard(int price, int validity, Float confitions) {
		SysValueAddedCard sysValueAddedCard = new SysValueAddedCard();
		sysValueAddedCard.setAmount(price);
		sysValueAddedCard.setExplain("支付网费时使用");
		sysValueAddedCard.setDay(validity);
		sysValueAddedCard.setCreateDate(new Date());
		sysValueAddedCard.setRestrict(1);
		sysValueAddedCard.setLimitMinMoney(confitions == null ? 0 : confitions.intValue());
		sysValueAddedCard.setValid(CommonConstant.INT_BOOLEAN_TRUE);
		return sysValueAddedCardService.save(sysValueAddedCard);
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

	private void saveProperty(NetbarResourceCommodityProperty netbarResourceCommodityProperty, int newFlag,
			String phone, String categoryName) {
		int dateOrNum = netbarResourceCommodityProperty.getCateType();
		if (0 == dateOrNum) {//档期
			String settlDates = netbarResourceCommodityProperty.getSettlDates();
			Iterable<String> dates = Splitter.on("|").split(handleSeparator(settlDates));
			for (String date : dates) {
				Date settlDate = null;
				try {
					settlDate = DateUtils.stringToDateYyyyMMdd(date);
					netbarResourceCommodityProperty.setSettlDate(settlDate);
					netbarResourceCommodityProperty.setInventoryTotal(1);
					netbarResourceCommodityProperty.setInventory(1);
					if (newFlag == 1) {
						netbarResourceCommodityProperty
								.setPropertyNo(netbarResourceCommodityPropertyService.getNewPropertyNo());
						netbarResourceCommodityProperty.setId(null);
						netbarResourceCommodityProperty.setInterestNum(0);
						if (categoryName.contains("第三方")) {
							netbarResourceCommodityProperty.setStatus(1); //待确认
						} else {
							netbarResourceCommodityProperty.setStatus(2); //发布
						}
					}
					NetbarResourceCommodityProperty newProperty = BeanUtils
							.updateBean(new NetbarResourceCommodityProperty(), netbarResourceCommodityProperty);
					netbarResourceCommodityPropertyService.save(newProperty);
				} catch (ParseException e) {
					LOGGER.error("档期转换异常：", e);
				}
			}
		} else if (1 == dateOrNum) {//数量
			if (newFlag == 1) {
				netbarResourceCommodityProperty
						.setPropertyNo(netbarResourceCommodityPropertyService.getNewPropertyNo());
			}
			netbarResourceCommodityProperty.setInventory(netbarResourceCommodityProperty.getInventoryTotal());
			netbarResourceCommodityProperty.setSettlDate(null);
			netbarResourceCommodityProperty.setInterestNum(0);
			netbarResourceCommodityPropertyService.save(netbarResourceCommodityProperty);
		}
	}

	private String checkSettlDate(Long commodityId, String propertyName, String settlDate, Long propertyId) {
		if (netbarResourceCommodityPropertyService.checkSettlDateByCidAndPropertyNameAndSettlDateExist(commodityId,
				propertyName, settlDate, propertyId)) {
			return settlDate;
		}
		return null;
	}

	private String handleSeparator(String s) {
		s = s.replaceAll(" ", "");
		if (StringUtils.isNotBlank(s)) {
			if (s.startsWith("|")) {
				s = s.substring(1);
			}
			if (s.endsWith("|")) {
				s = s.substring(0, s.length() - 1);
			}
		}
		return s;
	}

	/*
	 * 请求对象信息
	 */
	@ResponseBody
	@RequestMapping("/info/{id}")
	public JsonResponseMsg detail(@PathVariable("id") long id) {
		JsonResponseMsg result = new JsonResponseMsg();

		Map<String, Object> map = Maps.newHashMap();
		NetbarResourceCommodityProperty property = netbarResourceCommodityPropertyService.findById(id);
		if (null != property) {
			NetbarResourceCommodity commodity = netbarResourceCommodityService.findById(property.getCommodityId());
			map.put("commodity", commodity);
			map.put("property", property);
			long categoryId = null == commodity.getCategoryId() ? 0 : commodity.getCategoryId();
			map.put("category", netbarCommodityCategoryService.findById(categoryId));
		}

		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS, map);
		return result;
	}

	/*
	 * 判断类别是不是增值券
	 */
	@ResponseBody
	@RequestMapping("/isValueAddedCard/{id}")
	public JsonResponseMsg isRedbag(@PathVariable("id") long id) {
		JsonResponseMsg result = new JsonResponseMsg();

		Map<String, Object> map = Maps.newHashMap();
		NetbarResourceCommodity commodity = netbarResourceCommodityService.findById(id);
		if (null != commodity) {
			NetbarCommodityCategory category = netbarCommodityCategoryService.findById(commodity.getCategoryId());
			if (null != category && "增值券".equals(category.getName())) {
				map.put("isRedbag", "1");
			}
		} else {
			map.put("isRedbag", "0");
		}

		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS, map);
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
	 * 上下架（批量）
	 */
	@ResponseBody
	@RequestMapping("/changeStatus/{ids}/{oper}")
	public JsonResponseMsg changeStatus(HttpServletRequest req, @PathVariable("ids") String ids,
			@PathVariable("oper") int oper) {
		JsonResponseMsg result = new JsonResponseMsg();

		ids = handelIds(ids);
		if (StringUtils.isNotBlank(ids)) {
			if (!netbarResourceCommodityPropertyService.queryStatusSameByIds(ids)) {
				result.fill(CommonConstant.CODE_ERROR_PARAM, "批量所选类型不统一");
				return result;
			}

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
				if ("2".equals(oper)) {
					operate = "上架商品";
					logType = OperateLogConstant.TYPE_ADMIN_RESOURCE_RELEASE_COMMODITY;
				} else if ("0".equals(oper)) {
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

	/**
	 * 导出excel
	 */
	@RequestMapping("/exportExcel")
	public ModelAndView exportInfo(HttpServletResponse res, String page, String name, String beginDate, String endDate,
			String areaCode, String categoryId, String categoryPid, String status, String qualifications, String order,
			String cid) {
		// 查询报名数据
		int pageInt = NumberUtils.toInt(page, 1);
		Map<String, Object> params = Maps.newHashMap();
		params.put("cid", cid);
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
		List<Map<String, Object>> list = netbarResourceCommodityPropertyService.pageList(pageInt, params).getList();

		// Excel标题
		String title = "资源商城商品信息";

		// 编辑excel内容
		String[][] contents = new String[list.size() + 1][];

		// 设置标题行
		String[] contentTitle = new String[14];
		contentTitle[0] = "商品编号";
		contentTitle[1] = "商品名称";
		contentTitle[2] = "项目名称";
		contentTitle[3] = "价格";
		contentTitle[4] = "单位";
		contentTitle[5] = "大类别";
		contentTitle[6] = "细分";
		contentTitle[7] = "地区";
		contentTitle[8] = "资格";
		contentTitle[9] = "已售数量";
		contentTitle[10] = "在售数量";
		contentTitle[11] = "状态";
		contentTitle[12] = "联系人";
		contentTitle[13] = "联系人电话";
		contents[0] = contentTitle;

		// 设置内容
		if (CollectionUtils.isNotEmpty(list)) {
			for (int i = 0; i < list.size(); i++) {
				Map<String, Object> map = list.get(i);
				String[] row = new String[14];
				row[0] = MapUtils.getString(map, "propertyNo", "");
				row[1] = MapUtils.getString(map, "name", "");
				row[2] = MapUtils.getString(map, "propertyName", "");
				row[3] = MapUtils.getString(map, "price", "");
				row[4] = MapUtils.getString(map, "measure", "");
				row[5] = MapUtils.getString(map, "typeNameP", "");
				row[6] = MapUtils.getString(map, "typeName", "");
				row[7] = MapUtils.getString(map, "areaName", "");
				int qualificationInt = MapUtils.getIntValue(map, "qualifications", 0);
				row[8] = qualificationInt == 1 ? "会员"
						: qualificationInt == 2 ? "金牌" : qualificationInt == 3 ? "钻石" : "无";
				row[9] = String.valueOf(
						MapUtils.getIntValue(map, "inventoryTotal", 0) - MapUtils.getIntValue(map, "inventory", 0));
				row[10] = MapUtils.getString(map, "inventory", "");
				int statusInt = MapUtils.getIntValue(map, "status", 0);
				row[11] = statusInt == 1 ? "待确认" : statusInt == 2 ? "发布中" : "下架";
				row[12] = MapUtils.getString(map, "executes", "");
				row[13] = MapUtils.getString(map, "execute_phone", "");
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

	@RequestMapping("toConfirmUp")
	public String toConfirmUp(HttpServletRequest request, String commodityId, Model model) {
		request.getSession().setAttribute("sitemesh", "no");
		Map<String, Object> result = netbarResourceCommodityService.toConfirmUp(commodityId);
		model.addAttribute("info", result);
		model.addAttribute("subs", ((String) result.get("sub_name")).split(","));
		model.addAttribute("propertyIds", result.get("sub_id"));
		model.addAttribute("status", ((String) result.get("sub_status")).split(",")[0].equals("1") ? 1 : 2);
		return "resource/toConfirmUp";
	}

	@RequestMapping("confirmUp")
	@ResponseBody
	public int confirmUp(String propertyIds) {
		netbarResourceCommodityService.confirmUp(propertyIds);
		return 0;
	}

	@RequestMapping("toConfirmC")
	public String toConfirmC(HttpServletRequest request, String orderId, Model model) {
		request.getSession().setAttribute("sitemesh", "no");
		Map<String, Object> result = netbarResourceCommodityService.toConfirmC(orderId);
		model.addAttribute("info", result);
		return "resource/toConfirmC";
	}

	@RequestMapping("confirmC")
	@ResponseBody
	public int confirmC(String id) {
		netbarResourceCommodityService.confirmC(id);
		return 0;
	}

	@RequestMapping("toConfirmB")
	public String toConfirmB(HttpServletRequest request, String orderId, Model model) {
		request.getSession().setAttribute("sitemesh", "no");
		Map<String, Object> result = netbarResourceCommodityService.toConfirmB(orderId);
		model.addAttribute("info", result);
		return "resource/toConfirmB";
	}

	@RequestMapping("confirmB")
	@ResponseBody
	public int confirmB(String id) {
		netbarResourceCommodityService.confirmB(id);
		return 0;
	}
}
