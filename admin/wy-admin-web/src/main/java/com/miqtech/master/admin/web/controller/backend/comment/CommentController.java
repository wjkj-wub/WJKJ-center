package com.miqtech.master.admin.web.controller.backend.comment;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.HtmlUtils;

import com.google.common.collect.Maps;
import com.miqtech.master.admin.web.controller.backend.BaseController;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.entity.activity.ActivityMatchComment;
import com.miqtech.master.entity.activity.ActivityOverActivityModule;
import com.miqtech.master.entity.amuse.AmuseActivityComment;
import com.miqtech.master.entity.user.UserGag;
import com.miqtech.master.entity.user.UserInfo;
import com.miqtech.master.service.activity.ActivityMatchCommentService;
import com.miqtech.master.service.activity.ActivityOverActivityModuleService;
import com.miqtech.master.service.amuse.AmuseActivityCommentService;
import com.miqtech.master.service.user.UserInfoService;
import com.miqtech.master.utils.JsonUtils;
import com.miqtech.master.vo.PageVO;

@Controller
@RequestMapping("comment")
public class CommentController extends BaseController {

	@Autowired
	private AmuseActivityCommentService amuseActivityCommentService;
	@Autowired
	private ActivityOverActivityModuleService activityOverActivityModuleService;

	/**
	 * 新闻模式评论列表分页-只查询新闻的评论
	 */
	@RequestMapping("info/list/{page}")
	public ModelAndView infoCommentPage(HttpServletRequest request, @PathVariable(value = "page") Integer page) {
		ModelAndView mv = new ModelAndView("information/comment/infoList");
		Map<String, Object> params = Maps.newHashMap();
		String infoIdString = request.getParameter("infoId");
		Long infoId = NumberUtils.toLong(infoIdString == null ? "0" : infoIdString.toString());
		if (infoId.longValue() > 0) {
			params.put("infoId", infoId);
		}
		if (page == null) {
			page = 1;
		}
		params.put("page", page);
		String title = request.getParameter("title");
		if (StringUtils.isNotBlank(title)) {
			params.put("title", title);
		}

		String orderCommentString = request.getParameter("orderComment");
		int orderComment = NumberUtils.toInt(orderCommentString == null ? "0" : orderCommentString);
		if (orderComment > 0) {
			params.put("orderComment", orderComment);
		}
		String startDate = request.getParameter("startDate");
		String endDate = request.getParameter("endDate");
		if (StringUtils.isNotBlank(startDate) && StringUtils.isNotBlank(endDate)) {
			params.put("startDate", startDate);
			params.put("endDate", endDate);
		}

		String tableTypeString = request.getParameter("tableType");
		int tableType = NumberUtils.toInt(tableTypeString == null ? "0" : tableTypeString);
		params.put("tableType", tableType);

		String moduleIdString = request.getParameter("moduleId");
		int moduleId = NumberUtils.toInt(moduleIdString == null ? "0" : moduleIdString);
		if (moduleId > 0) {
			params.put("moduleId", moduleId);
		}

		String commentTypeString = request.getParameter("commentType");
		int commentType = NumberUtils.toInt(commentTypeString == null ? "0" : commentTypeString);
		if (commentType > 0) {
			params.put("commentType", commentType);
		}

		mv.addObject("params", params);
		PageVO vo = amuseActivityCommentService.querySubject(params);
		pageModels(mv, vo.getList(), page, vo.getTotal());

		List<ActivityOverActivityModule> modules = activityOverActivityModuleService.findValidByPid(0L);
		mv.addObject("modules", modules);
		return mv;
	}

	/**
	 * 评论模式评论列表分页-只查询新闻的评论
	 */
	@RequestMapping("subcomment/list/{page}")
	public ModelAndView subCommentPage(HttpServletRequest request, @PathVariable(value = "page") Integer page) {
		ModelAndView mv = new ModelAndView("information/comment/subCommentList");
		Map<String, Object> params = Maps.newHashMap();

		String commentIdString = request.getParameter("commentId");
		Long commentId = NumberUtils.toLong(commentIdString == null ? "0" : commentIdString.toString());
		if (commentId.longValue() > 0) {
			params.put("commentId", commentId);
		}
		if (page == null) {
			page = 1;
		}
		params.put("page", page);

		String content = request.getParameter("content");
		if (StringUtils.isNotBlank(content)) {
			params.put("content", content);
		}
		String nickname = request.getParameter("nickname");
		if (StringUtils.isNotBlank(nickname)) {
			params.put("nickname", nickname);
		}

		String orderLikeCountString = request.getParameter("orderLikeCount");
		int orderLikeCount = NumberUtils.toInt(orderLikeCountString == null ? "0" : orderLikeCountString);
		if (orderLikeCount > 0) {
			params.put("orderLikeCount", orderLikeCount);
		}
		String startDate = request.getParameter("startDate");
		String endDate = request.getParameter("endDate");
		if (StringUtils.isNotBlank(startDate) && StringUtils.isNotBlank(endDate)) {
			params.put("startDate", startDate);
			params.put("endDate", endDate);
		}

		String tableTypeString = request.getParameter("tableType");
		int tableType = NumberUtils.toInt(tableTypeString == null ? "0" : tableTypeString);
		params.put("tableType", tableType);

		String moduleIdString = request.getParameter("moduleId");
		int moduleId = NumberUtils.toInt(moduleIdString == null ? "0" : moduleIdString);
		if (moduleId > 0) {
			params.put("moduleId", moduleId);
		}

		String commentTypeString = request.getParameter("commentType");
		int commentType = NumberUtils.toInt(commentTypeString == null ? "0" : commentTypeString);
		if (commentType > 0) {
			params.put("commentType", commentType);
		}

		mv.addObject("params", params);
		PageVO vo = amuseActivityCommentService.queryAllComments(params);
		pageModels(mv, vo.getList(), page, vo.getTotal());

		List<ActivityOverActivityModule> modules = activityOverActivityModuleService.findValidByPid(0L);
		mv.addObject("modules", modules);

		return mv;
	}

	@RequestMapping("edit")
	@ResponseBody
	public int edit(HttpServletRequest request) {
		Long id = NumberUtils.toLong(request.getParameter("commentId").toString());
		if (id > 0) {
			String content = request.getParameter("commentContent");
			if (StringUtils.isNotBlank(content)) {
				AmuseActivityComment comment = amuseActivityCommentService.findById(id);
				if (null != comment) {
					comment.setContent(content);
					amuseActivityCommentService.save(comment);
				}
			}
			return 0;
		}
		return -1;

	}

	@RequestMapping("delete/{id}")
	@ResponseBody
	public JsonResponseMsg delete(HttpServletRequest request, @PathVariable(value = "id") Long id) {

		JsonResponseMsg result = new JsonResponseMsg();
		if (id > 0) {
			AmuseActivityComment comment = amuseActivityCommentService.findById(id);
			if (null != comment) {
				comment.setValid(0);
				amuseActivityCommentService.save(comment);
			}
		}
		result.fill(0, "ok");
		return result;
	}

	@RequestMapping("reply")
	@ResponseBody
	public int reply(HttpServletRequest request) {
		String commentId = request.getParameter("replyCommentId");
		Long replyCommentId = NumberUtils.toLong(commentId == null ? "0" : commentId);
		if (replyCommentId <= 0) {
			return -1;
		}
		String content = request.getParameter("commentContent");
		String userId = request.getParameter("userId");
		Long replyUserId = NumberUtils.toLong(userId == null ? "0" : userId);
		AmuseActivityComment toReplyComment = amuseActivityCommentService.findById(replyCommentId);

		if (null == toReplyComment) {
			return -1;
		}

		if (StringUtils.isBlank(content)) {
			return -2;
		}
		String userNickname = request.getParameter("userNickname");
		if (replyUserId.longValue() <= 0) {
			if (StringUtils.isBlank(userNickname)) {
				return -3;
			}
			UserInfo user = userInfoService.findByNickname(userNickname);
			if (user == null) {
				return -4;
			}
			replyUserId = user.getId();
		}

		AmuseActivityComment comment = new AmuseActivityComment();
		content = HtmlUtils.htmlEscape(content);
		comment.setContent(content);
		comment.setCreateDate(new Date());
		comment.setLikeCount(0);
		comment.setType(toReplyComment.getType());
		Long parentId = toReplyComment.getParentId();
		comment.setParentId(parentId.longValue() == 0 ? replyCommentId : parentId);
		comment.setValid(1);
		comment.setUserId(replyUserId);
		comment.setAmuseId(toReplyComment.getAmuseId());
		comment.setReplyId(replyCommentId);
		amuseActivityCommentService.save(comment);
		return 0;

	}

	@Autowired
	private ActivityMatchCommentService activityMatchCommentService;

	@RequestMapping("replyactivity")
	@ResponseBody
	public int replyactivity(HttpServletRequest request) {
		String infoIdString = request.getParameter("infoId");
		Long infoId = NumberUtils.toLong(infoIdString == null ? "0" : infoIdString);
		if (infoId <= 0) {
			return -1;
		}
		String content = request.getParameter("commentContent");
		String userId = request.getParameter("userId");
		Long replyUserId = NumberUtils.toLong(userId == null ? "0" : userId);

		if (StringUtils.isBlank(content)) {
			return -2;
		}
		String userNickname = request.getParameter("userNickname");
		if (replyUserId.longValue() <= 0) {
			if (StringUtils.isBlank(userNickname)) {
				return -3;
			}
			UserInfo user = userInfoService.findByNickname(userNickname);
			if (user == null) {
				return -4;
			}
			replyUserId = user.getId();
		}

		ActivityMatchComment comment = new ActivityMatchComment();
		content = HtmlUtils.htmlEscape(content);
		comment.setContent(content);
		comment.setCreateDate(new Date());
		comment.setMatchId(infoId);

		comment.setValid(1);
		comment.setUserId(replyUserId);
		comment.setScore(0);
		activityMatchCommentService.save(comment);
		return 0;

	}

	@RequestMapping("replyInfo")
	@ResponseBody
	public int replyInfo(HttpServletRequest request) {

		String infoIdString = request.getParameter("infoId");
		Long infoId = NumberUtils.toLong(infoIdString == null ? "0" : infoIdString);
		if (infoId <= 0) {
			return -1;
		}
		String content = request.getParameter("commentContent");
		String userId = request.getParameter("userId");
		Long replyUserId = NumberUtils.toLong(userId == null ? "0" : userId);

		if (StringUtils.isBlank(content)) {
			return -2;
		}
		String userNickname = request.getParameter("userNickname");
		if (replyUserId.longValue() <= 0) {
			if (StringUtils.isBlank(userNickname)) {
				return -3;
			}
			UserInfo user = userInfoService.findByNickname(userNickname);
			if (user == null) {
				return -4;
			}
			replyUserId = user.getId();
		}

		AmuseActivityComment comment = new AmuseActivityComment();
		content = HtmlUtils.htmlEscape(content);
		comment.setContent(content);
		comment.setParentId(0L);
		comment.setReplyId(0L);
		comment.setCreateDate(new Date());
		comment.setLikeCount(0);
		String infoTypeString = request.getParameter("infoType");
		int infoType = NumberUtils.toInt(infoTypeString == null ? "0" : infoTypeString);

		if (infoType == 0) {
			comment.setType(3);//资讯
		}
		if (infoType == 1) {
			comment.setType(1);//娱乐赛
		}
		if (infoType == 2) {
			comment.setType(2);//官方赛
		}

		comment.setValid(1);
		comment.setUserId(replyUserId);
		comment.setAmuseId(infoId);
		amuseActivityCommentService.save(comment);
		return 0;

	}

	@Autowired
	private UserInfoService userInfoService;

	@RequestMapping("randomReplyUser")
	@ResponseBody
	public Map<String, Object> randomReplyUser() {
		Long id = RandomUtils.nextLong(4103656, 4104155);
		UserInfo user = userInfoService.findById(id);
		Map<String, Object> resutl = Maps.newHashMap();
		resutl.put("userId", user.getId());
		resutl.put("userNickname", user.getNickname());
		return resutl;
	}

	/**
	 * 保存禁言用户信息
	 */
	@RequestMapping("gagAdd/{userId}/{days}")
	@ResponseBody
	public String gagAdd(@PathVariable(value = "userId") String userId, @PathVariable(value = "days") String days) {
		JsonResponseMsg result = new JsonResponseMsg();
		UserGag userGag = new UserGag();
		userGag.setUserId(NumberUtils.toLong(userId));
		userGag.setDays(NumberUtils.toInt(days));
		userGag.setCreateDate(new Date());
		amuseActivityCommentService.save(userGag);
		return JsonUtils.objectToString(result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS));
	}

	/**
	 * 删除禁言用户信息
	 */
	@RequestMapping("deleteGag/{userId}")
	@ResponseBody
	public String gagAdd(@PathVariable(value = "userId") String userId) {
		JsonResponseMsg result = new JsonResponseMsg();
		amuseActivityCommentService.delete(NumberUtils.toLong(userId));
		return JsonUtils.objectToString(result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS));
	}

	/**
	 * 批量删除
	 */
	@RequestMapping("deleteBatch/{params}")
	@ResponseBody
	public JsonResponseMsg deleteBatch(HttpServletRequest request, @PathVariable(value = "params") String params) {
		JsonResponseMsg result = new JsonResponseMsg();
		String[] arr = params.split(",");
		for (int i = 0; i < arr.length; i++) {
			if (StringUtils.isNotBlank(arr[i])) {
				Long id = NumberUtils.toLong(arr[i]);
				if (id > 0) {
					AmuseActivityComment comment = amuseActivityCommentService.findById(id);
					if (null != comment) {
						comment.setValid(0);
						amuseActivityCommentService.save(comment);
					}
				}
			}
		}
		result.fill(0, "ok");
		return result;
	}

	/**
	 * 跳转至评论区页面
	 */
	@RequestMapping("commentlist/{amuseId}/{flag}/{page}")
	public ModelAndView queryCommentList(@PathVariable(value = "amuseId") String amuseId,
			@PathVariable(value = "page") String page, @PathVariable(value = "flag") String flag, String nickname,
			String content, String orderLikeCount) {
		ModelAndView mv = new ModelAndView("information/comment/commentList");
		PageVO vo = amuseActivityCommentService.queryComment(NumberUtils.toLong(amuseId), nickname, content,
				orderLikeCount, page);
		pageModels(mv, vo.getList(), NumberUtils.toInt(page), vo.getTotal());
		mv.addObject("nickname", nickname);
		mv.addObject("content", content);
		mv.addObject("orderLikeCount", NumberUtils.toInt(orderLikeCount));
		mv.addObject("flag", NumberUtils.toInt(flag));
		return mv;
	}
}
