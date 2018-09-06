package com.miqtech.master.admin.web.controller.api.guessing;

import com.miqtech.master.admin.web.annotation.CrossDomain;
import com.miqtech.master.admin.web.annotation.LoginValid;
import com.miqtech.master.admin.web.controller.api.BaseController;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.service.guessing.GuessingRemainderService;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.vo.PageVO;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

/**
 *  竞猜余量管理操作
 * @author zhangyuqi
 * 2017年06月01日
 */
@Controller
@RequestMapping("api/guessing/remainder")
public class GuessingRemainderController extends BaseController {

	@Resource
	private GuessingRemainderService guessingRemainderService;

	/**
	 * 获取竞猜余量列表信息
	 * @param page		当前页数
	 * @param pageSize	单页显示数量
	 * @param keyTitle	竞猜标题关键字(搜索条件)
	 */
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	@LoginValid(valid = true)
	@ResponseBody
	public JsonResponseMsg findGuessingList(Integer page, Integer pageSize, String keyTitle) {
		JsonResponseMsg result = new JsonResponseMsg();

		// 判断page是否合法，返回合法值
		page = PageUtils.getPage(page);
		PageVO pageList = guessingRemainderService.findGuessingRemainderList(page, pageSize, keyTitle);
		result.setObject(pageList);
		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);

		return result;
	}

	/**
	 * 导出竞猜余量列表信息
	 */
	@RequestMapping(value = "/export", method = RequestMethod.GET)
	@LoginValid(valid = true)
	@CrossDomain(value = true)
	@ResponseBody
	public void export(HttpServletResponse res) throws Exception {
		guessingRemainderService.export(res);
	}
}
