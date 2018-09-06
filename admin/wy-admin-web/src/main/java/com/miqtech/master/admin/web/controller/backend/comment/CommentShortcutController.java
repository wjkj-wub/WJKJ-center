package com.miqtech.master.admin.web.controller.backend.comment;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.miqtech.master.admin.web.controller.backend.BaseController;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.entity.comment.CommentShortcut;
import com.miqtech.master.service.comment.CommentShortcutService;
import com.miqtech.master.thirdparty.util.img.ImgUploadUtil;
import com.miqtech.master.utils.BeanUtils;

@Controller
@RequestMapping("comment/shortcut")
public class CommentShortcutController extends BaseController {

	@Autowired
	private CommentShortcutService commentShortcutService;

	/**
	 * 列表
	 */
	@RequestMapping("list")
	public ModelAndView infoCommentPage(HttpServletRequest request) {
		ModelAndView mv = new ModelAndView("information/comment/shortcutList");
		List<CommentShortcut> lists = commentShortcutService.findListByValid();
		mv.addObject("list", lists);
		return mv;
	}

	/**
	 * 编辑详情
	 */
	@RequestMapping("info")
	@ResponseBody
	public JsonResponseMsg infoComment(HttpServletRequest request, Integer id) {
		JsonResponseMsg result = new JsonResponseMsg();
		CommentShortcut info = commentShortcutService.findById(id.longValue());
		if (info != null) {
			result.fill(CommonConstant.CODE_SUCCESS, "查询成功", info);
			return result;
		}
		result.fill(CommonConstant.CODE_ERROR_LOGIC, "查询失败", info);
		return result;
	}

	/**
	 * 保存
	 */
	@RequestMapping("save")
	@ResponseBody
	public JsonResponseMsg saveComment(HttpServletRequest request, CommentShortcut commentShortcut) {
		JsonResponseMsg result = new JsonResponseMsg();
		int limitCount = commentShortcutService.countValid();
		if (commentShortcut == null) {
			return result.fill(-1, "缺少参数");
		}
		if(commentShortcut.getId()==null){
			if (limitCount >= 8) {
				result.fill(CommonConstant.INT_BOOLEAN_TRUE, "不能超过8个");
				return result;
			}
		}
		if (commentShortcut.getId() != null) {
			CommentShortcut old = commentShortcutService.findById(commentShortcut.getId());
			if (old != null) {
				commentShortcut = BeanUtils.updateBean(old, commentShortcut);
			}
		}

		String systemName = "wy-web-admin";
		String src = ImgUploadUtil.genFilePath("comment");
		MultipartFile fileMain = ((MultipartHttpServletRequest) request).getFile("iconFile");
		if (fileMain != null) {
			Map<String, String> imgPath = ImgUploadUtil.save(fileMain, systemName, src);
			commentShortcut.setImg(imgPath.get(ImgUploadUtil.KEY_MAP_SRC));
		}
		int maxsortnum = commentShortcutService.getMaxSortNum();
		commentShortcut.setSortNum(maxsortnum + 1);
		commentShortcut.setValid(1);
		commentShortcut.setCreateDate(new Date());
		commentShortcutService.save(commentShortcut);
		result.fill(CommonConstant.CODE_SUCCESS, "保存成功");
		return result;
	}

	/**
	 * 删除
	 */
	@RequestMapping("delete")
	@ResponseBody
	public JsonResponseMsg deleteComment(HttpServletRequest request, Integer id) {
		JsonResponseMsg result = new JsonResponseMsg();
		int limitCount = commentShortcutService.countValid();
		if (limitCount < 5) {
			result.fill(CommonConstant.INT_BOOLEAN_TRUE, "不能少于4个");
			return result;
		}
		if (id != null) {
			commentShortcutService.delete(id);
			result.fill(CommonConstant.CODE_SUCCESS, "删除成功");
			return result;
		}
		result.fill(1, "无法成功");
		return result;
	}

	/**
	 * 移动
	 */
	@RequestMapping("move")
	@ResponseBody
	public JsonResponseMsg moveComment(HttpServletRequest request, Integer id, Integer move) {
		JsonResponseMsg result = new JsonResponseMsg();
		CommentShortcut comment = commentShortcutService.findById(id.longValue());
		int sortnum = comment.getSortNum();
		Map<String, Object> commentnext = null;
		if (move == -1) {
			commentnext = commentShortcutService.findBeforeBySortNum(sortnum);
		}
		if (move == 1) {
			commentnext = commentShortcutService.findAfterBySortNum(sortnum);
		}
		if (commentnext != null) {
			comment.setSortNum((int) commentnext.get("sortNum"));
			commentnext.put("sortNum", sortnum);
			commentShortcutService.save(comment);
			commentShortcutService.updateSortNumById(commentnext);
			result.fill(CommonConstant.CODE_SUCCESS, move == -1 ? "上移成功" : "下移成功");
			return result;
		}

		result.fill(1, "无法成功");
		return result;
	}

}
