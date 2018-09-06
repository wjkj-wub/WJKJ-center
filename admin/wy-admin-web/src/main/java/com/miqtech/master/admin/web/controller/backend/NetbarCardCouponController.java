package com.miqtech.master.admin.web.controller.backend;

import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.entity.Pager;
import com.miqtech.master.entity.common.SysUserArea;
import com.miqtech.master.entity.netbar.NetbarCardCouponCategory;
import com.miqtech.master.service.netbar.NetbarCardCouponService;
import com.miqtech.master.service.system.SysUserAreaService;
import com.miqtech.master.utils.DateUtils;
import com.miqtech.master.utils.JsonUtils;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.vo.PageVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.util.List;

@Controller
@RequestMapping("/card/coupon")
public class NetbarCardCouponController extends BaseController {
	@Autowired
	private NetbarCardCouponService netbarCardCouponService;
	@Autowired
	private SysUserAreaService sysUserAreaService;

	@RequestMapping("list/{page}")
	public String list(HttpServletRequest request, Model model, String name, Integer type, @PathVariable Integer page) {
		PageVO vo = netbarCardCouponService.queryList(name, type, new Pager(page, PageUtils.ADMIN_DEFAULT_PAGE_SIZE));
		this.pageData(model, vo.getList(), page, vo.getTotal());
		List<SysUserArea> list = sysUserAreaService.findBySysUserId((Long) request.getSession().getAttribute("userId"));
		StringBuilder areaCodes = new StringBuilder();
		for (SysUserArea sysUserArea : list) {
			areaCodes.append(sysUserArea.getAreaCode() + ",");
		}
		model.addAttribute("areaCodes", areaCodes.toString().equals("000000,") ? null : areaCodes);
		model.addAttribute("couponName", name);
		model.addAttribute("type", type);
		return "netbar/cardCoupon";
	}

	@RequestMapping("save")
	@ResponseBody
	public String save(HttpServletRequest req, NetbarCardCouponCategory netbarCardCouponCategory, String netbars,
			String areas) throws ParseException {
		JsonResponseMsg result = new JsonResponseMsg();
		String startDate = req.getParameter("start_date");
		String endDate = req.getParameter("end_date");
		if (StringUtils.isBlank(netbars)) {
			return JsonUtils.objectToString(result.fill(-1, "网吧不能为空"));
		}
		if (StringUtils.isNotBlank(startDate) && StringUtils.isNotBlank(endDate)) {
			netbarCardCouponCategory.setStartDate(DateUtils.stringToDate(startDate, DateUtils.YYYY_MM_DD));
			netbarCardCouponCategory.setEndDate(DateUtils.stringToDate(endDate, DateUtils.YYYY_MM_DD));
		} else {
			return JsonUtils.objectToString(result.fill(-1, "有效期不能为空"));
		}
		netbarCardCouponService.save(netbarCardCouponCategory, netbars);
		return JsonUtils.objectToString(result);
	}

	@RequestMapping("detail/{id}")
	@ResponseBody
	public String detail(@PathVariable String id) {
		JsonResponseMsg result = new JsonResponseMsg();
		result.setObject(netbarCardCouponService.detail(id));
		return JsonUtils.objectToString(result);
	}

	@RequestMapping("switch/{id}")
	@ResponseBody
	public String switchover(@PathVariable String id) {
		JsonResponseMsg result = new JsonResponseMsg();
		netbarCardCouponService.switchover(id);
		return JsonUtils.objectToString(result);
	}

	@RequestMapping("del/{id}")
	@ResponseBody
	public String del(@PathVariable String id) {
		JsonResponseMsg result = new JsonResponseMsg();
		netbarCardCouponService.del(id);
		return JsonUtils.objectToString(result);
	}
}
