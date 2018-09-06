package com.miqtech.master.admin.web.controller.backend.resource;

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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.collect.Maps;
import com.miqtech.master.admin.web.controller.backend.BaseController;
import com.miqtech.master.admin.web.util.Servlets;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.consts.NetbarConstant;
import com.miqtech.master.consts.OperateLogConstant;
import com.miqtech.master.consts.SystemUserConstant;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.entity.common.SystemArea;
import com.miqtech.master.entity.common.SystemUser;
import com.miqtech.master.entity.netbar.resource.NetbarResourceCommodity;
import com.miqtech.master.entity.netbar.resource.NetbarResourceOrder;
import com.miqtech.master.service.common.OperateLogService;
import com.miqtech.master.service.netbar.resource.NetbarResourceCommodityService;
import com.miqtech.master.service.netbar.resource.NetbarResourceOrderService;
import com.miqtech.master.service.system.SysUserAreaService;
import com.miqtech.master.service.system.SystemAreaService;
import com.miqtech.master.utils.DateUtils;
import com.miqtech.master.utils.ExcelUtils;
import com.miqtech.master.utils.StringUtils;
import com.miqtech.master.vo.PageVO;

/**
 * 资源商城订单
 */
@Controller
@RequestMapping("netbar/resource/order")
public class NetbarResourceOrderController extends BaseController {

	private static final Logger LOGGER = LoggerFactory.getLogger(NetbarResourceOrderController.class);

	@Autowired
	private NetbarResourceOrderService netbarResourceOrderService;
	@Autowired
	private SystemAreaService systemAreaService;
	@Autowired
	private OperateLogService operateLogService;
	@Autowired
	private SysUserAreaService sysUserAreaService;
	@Autowired
	private NetbarResourceCommodityService netbarResourceCommodityService;

	/**
	 * 订单分页列表
	 */
	@RequestMapping("list/{page}")
	public ModelAndView page(HttpServletRequest request, @PathVariable("page") int page, String query, String beginDate,
			String endDate, String areaCode, String levels, String status, String hasComment, String hasRemark,
			String type) {
		ModelAndView mv = new ModelAndView("resource/orderList");

		Map<String, String> params = Maps.newHashMap();
		params.put("query", query);
		params.put("beginDate", beginDate);
		params.put("endDate", endDate);
		params.put("levels", levels);
		params.put("status", status);
		params.put("hasComment", hasComment);
		params.put("hasRemark", hasRemark);
		params.put("type", type);

		SystemUser user = Servlets.getSessionUser(request);
		boolean isActivityAdmin = SystemUserConstant.TYPE_ACTIVITY_ADMIN.equals(user.getUserType());
		if (!isActivityAdmin) { //最高管理员
			params.put("areaCode", areaCode); //精确到省级
		} else { //子账号
			String userAreaCode = sysUserAreaService.findUserAreaCode(user.getId()); // 匹配子账号的地区
			userAreaCode = null == userAreaCode ? "000000" : userAreaCode;
			params.put("areaCode", userAreaCode);
		}
		mv.addObject("isActivityAdmin", isActivityAdmin);
		mv.addObject("params", params);

		PageVO vo = netbarResourceOrderService.page(page, params);
		pageModels(mv, vo.getList(), page, vo.getTotal());

		List<SystemArea> roots = systemAreaService.queryValidRoot();
		mv.addObject("provinces", roots);

		Map<String, Object> statis = netbarResourceOrderService.statis(params);
		mv.addObject("statis", statis);

		return mv;
	}

	/**
	 * 导出
	 */
	@RequestMapping("export/{page}")
	public ModelAndView exportPersonal(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("page") int page, String query, String beginDate, String endDate, String areaCode,
			String levels, String status, String hasComment, String hasRemark, String type) {
		Map<String, String> params = Maps.newHashMap();
		params.put("query", query);
		params.put("beginDate", beginDate);
		params.put("endDate", endDate);
		params.put("levels", levels);
		params.put("status", status);
		params.put("hasComment", hasComment);
		params.put("hasRemark", hasRemark);
		params.put("type", type);

		SystemUser user = Servlets.getSessionUser(request);
		boolean isActivityAdmin = SystemUserConstant.TYPE_ACTIVITY_ADMIN.equals(user.getUserType());
		if (!isActivityAdmin) { //最高管理员
			params.put("areaCode", areaCode); //精确到省级
		} else { //子账号
			String userAreaCode = sysUserAreaService.findUserAreaCode(user.getId()); // 匹配子账号的地区
			userAreaCode = null == userAreaCode ? "000000" : userAreaCode;
			params.put("areaCode", userAreaCode);
		}

		PageVO vo = netbarResourceOrderService.page(page, params);
		List<Map<String, Object>> list = vo.getList();

		// 查询excel标题
		String title = "申诉列表";

		// 编辑excel内容
		String[][] contents = new String[(CollectionUtils.isNotEmpty(list) ? list.size() : 0) + 1][];

		// 设置标题行
		String[] contentTitle = new String[17];
		contentTitle[0] = "订单编号";
		contentTitle[1] = "订单时间";
		contentTitle[2] = "购买项目";
		contentTitle[3] = "总购买金额";
		contentTitle[4] = "使用资金";
		contentTitle[5] = "使用配额";
		contentTitle[6] = "购买网吧";
		contentTitle[7] = "网吧地址";
		contentTitle[8] = "业主电话";
		contentTitle[9] = "购买场次/数量";
		contentTitle[10] = "所在地区";
		contentTitle[11] = "网吧级别";
		contentTitle[12] = "对接人";
		contentTitle[13] = "对接人电话";
		contentTitle[14] = "状态";
		contentTitle[15] = "评价";
		contentTitle[16] = "备注";
		contents[0] = contentTitle;

		// 设置内容
		if (CollectionUtils.isNotEmpty(list)) {
			for (int i = 0; i < list.size(); i++) {
				Map<String, Object> verify = list.get(i);
				String[] row = new String[17];
				row[0] = MapUtils.getString(verify, "tradeNo");
				Date createDate = (Date) verify.get("createDate");
				if (createDate != null) {
					row[1] = DateUtils.dateToString(createDate, DateUtils.YYYY_MM_DD_HH_MM_SS);
				}
				row[2] = MapUtils.getString(verify, "propertyName");
				row[3] = MapUtils.getString(verify, "totalAmount");
				row[4] = MapUtils.getString(verify, "amount");
				row[5] = MapUtils.getString(verify, "quotaAmount");
				row[6] = MapUtils.getString(verify, "netbarName");
				row[7] = MapUtils.getString(verify, "netbarAddress");
				row[8] = MapUtils.getString(verify, "ownerTelephone");
				Integer cateType = MapUtils.getInteger(verify, "cateType");
				if (cateType != null && org.apache.commons.lang3.StringUtils.equals(cateType.toString(), "0")) {// 按日期
					Date serveDate = (Date) verify.get("serveDate");
					if (serveDate != null) {
						row[9] = DateUtils.dateToString(serveDate, DateUtils.YYYY_MM_DD_HH_MM_SS);
					}
				} else {
					Integer buyNum = MapUtils.getInteger(verify, "buyNum");
					if (buyNum != null) {
						row[9] = buyNum.toString();
					}
				}
				String provinceAreaCode = MapUtils.getString(verify, "provinceAreaCode");
				if (org.apache.commons.lang3.StringUtils.equals(provinceAreaCode, "000000")) {
					row[10] = "全国";
				} else {
					row[10] = MapUtils.getString(verify, "provinceName");
				}
				Integer netbarLevels = MapUtils.getInteger(verify, "levels");
				if (NetbarConstant.NETBAR_LEVELS_MEMBER.equals(netbarLevels)) {
					row[11] = "会员";
				} else if (NetbarConstant.NETBAR_LEVELS_GOLD.equals(netbarLevels)) {
					row[11] = "黄金";
				} else if (NetbarConstant.NETBAR_LEVELS_DIAMOND.equals(netbarLevels)) {
					row[11] = "钻石";
				}
				row[12] = MapUtils.getString(verify, "executes");
				row[13] = MapUtils.getString(verify, "executePhone");
				Integer orderStatus = MapUtils.getInteger(verify, "status");
				if (NetbarConstant.NETBAR_RESOURCE_ORDER_STATUS_CANCEL.equals(orderStatus)) {
					row[14] = "已取消";
				} else if (NetbarConstant.NETBAR_RESOURCE_ORDER_STATUS_UNCHECK.equals(orderStatus)) {
					row[14] = "未确认";
				} else if (NetbarConstant.NETBAR_RESOURCE_ORDER_STATUS_SERVER.equals(orderStatus)) {
					row[14] = "服务确认";
				} else if (NetbarConstant.NETBAR_RESOURCE_ORDER_STATUS_NETBAR.equals(orderStatus)) {
					row[14] = "网吧确认";
				} else if (NetbarConstant.NETBAR_RESOURCE_ORDER_STATUS_CHECKED.equals(orderStatus)) {
					row[14] = "订单成功";
				} else if (NetbarConstant.NETBAR_RESOURCE_ORDER_STATUS_SUCCESS_EXPIRE.equals(orderStatus)) {
					row[14] = "订单成功";
				}
				row[15] = MapUtils.getString(verify, "comments");
				row[16] = MapUtils.getString(verify, "remarks");
				contents[i + 1] = row;
			}
		}

		try {
			ExcelUtils.exportExcel(title, contents, false, response);
		} catch (Exception e) {
			LOGGER.error("导出数据异常：", e);
		}
		return null;
	}

	/**
	 * 加备注
	 */
	@ResponseBody
	@RequestMapping("addRemarks")
	public JsonResponseMsg addRemarks(String id, String remarks) {
		JsonResponseMsg result = new JsonResponseMsg();
		if (!NumberUtils.isNumber(id) || StringUtils.isBlank(remarks)) {
			return result.fill(CommonConstant.CODE_ERROR_PARAM, "参数不正确");
		}

		Long idLong = NumberUtils.toLong(id);
		NetbarResourceOrder o = netbarResourceOrderService.findValidById(idLong);
		if (o == null) {
			return result.fill(CommonConstant.CODE_ERROR_PARAM, "找不到订单信息");
		}

		o.setRemarks(remarks);
		netbarResourceOrderService.save(o);
		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
	}

	/**
	 * 取消订单
	 */
	@ResponseBody
	@RequestMapping("cancel/{id}")
	public JsonResponseMsg cancel(HttpServletRequest req, @PathVariable("id") long id) {
		JsonResponseMsg result = new JsonResponseMsg();

		// 检查订单信息是否正确
		NetbarResourceOrder o = netbarResourceOrderService.findValidById(id);
		if (o == null) {
			return result.fill(CommonConstant.CODE_ERROR_LOGIC, "找不到订单信息");
		}
		if (NetbarConstant.NETBAR_RESOURCE_ORDER_STATUS_CANCEL.equals(o.getStatus())
				|| NetbarConstant.NETBAR_RESOURCE_ORDER_STATUS_EXPIRED.equals(o.getStatus())) {
			return result.fill(CommonConstant.CODE_ERROR_LOGIC, "订单当前的状态不允许取消");
		}

		// 检查用户的操作类型是否正确
		SystemUser sysUser = Servlets.getSessionUser(req);
		boolean isActivityAdmin = SystemUserConstant.TYPE_ACTIVITY_ADMIN.equals(sysUser.getUserType());
		String userAreaCode = sysUserAreaService.findUserAreaCode(sysUser.getId()); // 匹配子账号的地区
		userAreaCode = null == userAreaCode ? "000000" : userAreaCode;
		if (isActivityAdmin && !org.apache.commons.lang3.StringUtils.equals(userAreaCode, "000000")) {
			// 获取订单商品所处的地区
			Long commodityId = o.getCommodityId();
			NetbarResourceCommodity commodity = netbarResourceCommodityService.findById(commodityId);
			String province = StringUtils.EMPTY;
			if (commodity != null) {
				province = commodity.getProvince();
			}
			if (StringUtils.isNotBlank(province) && province.length() >= 2) {
				if (!province.substring(0, 2).equals(userAreaCode.substring(0, 2))) {
					return result.fill(CommonConstant.CODE_ERROR_LOGIC, "您不能处理其他地区的订单");
				}
			}
		}

		NetbarResourceOrder order = netbarResourceOrderService.cancel(o);

		operateLogService.adminOperateLog(sysUser.getId(), id, OperateLogConstant.TYPE_ADMIN_RESOURCE_CANCEL_ORDER,
				"取消订单,订单号:" + order.getTradeNo());

		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
	}
}
