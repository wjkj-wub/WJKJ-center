package com.miqtech.master.admin.web.controller.api.guessing;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.miqtech.master.admin.web.annotation.CrossDomain;
import com.miqtech.master.admin.web.annotation.LoginValid;
import com.miqtech.master.admin.web.controller.api.BaseController;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.entity.guessing.GuessingItem;
import com.miqtech.master.service.guessing.GuessingItemService;
import com.miqtech.master.vo.PageVO;

/**
 *竞猜对象
 * @author 叶岸平
 */
@Controller
@RequestMapping("api/guessing/item")
public class GuessingItemController extends BaseController {

	@Autowired
	private GuessingItemService guessingItemService;

	/**
	 * 新增或保存竞猜对象
	 */
	@CrossDomain(value = true)
	@RequestMapping("save")
	@LoginValid(valid = true)
	@ResponseBody
	public JsonResponseMsg add(String itemId, String name, String logo) {
		JsonResponseMsg result = new JsonResponseMsg();
		if (StringUtils.isAnyBlank(name, logo)) {
			return result.fill(CommonConstant.CODE_ERROR_PARAM, CommonConstant.MSG_ERROR_PARAM);
		}
		if (!Pattern.matches("^[\u4e00-\u9fa5a-zA-Z0-9]+$", name)) {
			return result.fill(-4, "名称只能用中英文字符");
		}
		GuessingItem item;
		if (StringUtils.isNotBlank(itemId)) {
			item = guessingItemService.findById(NumberUtils.toLong(itemId));
			if (null == item) {
				return result.fill(-3, "该竞猜对象不存在");
			}
		} else {
			item = new GuessingItem();
			item.setValid(1);
			item.setCreateDate(new Date());
		}
		item.setName(name);
		item.setLogoUrl(logo);
		guessingItemService.saveOrUpdate(item);
		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
	}

	/**
	 * 删除竞猜对象(如果对象任被竞猜引用则不可删除)
	 */
	@CrossDomain(value = true)
	@RequestMapping("delete")
	@LoginValid(valid = true)
	@ResponseBody
	public JsonResponseMsg delete(Long itemId) {
		JsonResponseMsg result = new JsonResponseMsg();
		GuessingItem item = guessingItemService.findById(itemId);
		if (null == item) {
			return result.fill(-3, "该竞猜对象不存在");
		}
		//有竞猜的队伍 不能删
		Boolean usedInfo = guessingItemService.isItemUsed(itemId);
		if (usedInfo) {
			return result.fill(-6, "删除失败，队伍不可删除");
		}
		item.setValid(0);
		guessingItemService.saveOrUpdate(item);
		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
	}

	@CrossDomain(value = true)
	@RequestMapping("getInfo")
	@LoginValid(valid = true)
	@ResponseBody
	public JsonResponseMsg info(String itemId) {
		JsonResponseMsg result = new JsonResponseMsg();
		if (StringUtils.isBlank(itemId)) {
			return result.fill(-3, "该竞猜对象不存在");
		}
		GuessingItem item = guessingItemService.findById(NumberUtils.toLong(itemId));
		if (null == item) {
			return result.fill(-3, "该竞猜对象不存在");
		}
		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS, item);
	}

	/**
	 * 获取竞猜对象列表
	 */
	@CrossDomain(value = true)
	@RequestMapping("list")
	@LoginValid(valid = true)
	@ResponseBody
	public JsonResponseMsg list(String name, Integer page, Integer pageSize) {
		Integer pageNal = Optional.ofNullable(page).orElse(1);
		PageVO pageVo = guessingItemService.findItemList(name, pageNal, pageSize);
		return new JsonResponseMsg().fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS, pageVo);
	}

	/**
	 * 获取所有竞猜对象信息，不分页
	 * @return
	 */
	@RequestMapping(value = "/list/all", method = RequestMethod.GET)
	@LoginValid(valid = true)
	@ResponseBody
	public JsonResponseMsg listAll() {
		JsonResponseMsg result = new JsonResponseMsg();

		List<GuessingItem> guessingItems = guessingItemService.findAllItemList();
		if (guessingItems != null) {
			result.setObject(guessingItems);
			return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
		}
		return result.fill(CommonConstant.CODE_ERROR_LOGIC, CommonConstant.MSG_ERROR_LOGIC_NULL);
	}

}
