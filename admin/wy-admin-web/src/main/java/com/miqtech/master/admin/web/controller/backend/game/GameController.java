package com.miqtech.master.admin.web.controller.backend.game;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.miqtech.master.admin.web.controller.backend.BaseController;
import com.miqtech.master.admin.web.util.Servlets;
import com.miqtech.master.config.SystemConfig;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.entity.game.GameImg;
import com.miqtech.master.entity.game.GameInfo;
import com.miqtech.master.service.game.GameImgService;
import com.miqtech.master.service.game.GameInfoService;
import com.miqtech.master.thirdparty.util.img.ImgUploadUtil;
import com.miqtech.master.vo.PageVO;

@Controller
@RequestMapping("game")
public class GameController extends BaseController {

	@Autowired
	private SystemConfig systemConfig;
	@Autowired
	private GameInfoService gameInfoService;
	@Autowired
	private GameImgService gameImgService;

	/**
	 * 手游列表
	 */
	@RequestMapping("list/{page}")
	public ModelAndView list(@PathVariable("page") int page, String name) {
		ModelAndView mv = new ModelAndView("game/gameList");

		Map<String, String> params = Maps.newHashMap();
		params.put("name", name);
		mv.addObject("params", params);

		PageVO vo = gameInfoService.adminPage(page, params);
		pageModels(mv, vo.getList(), page, vo.getTotal());

		mv.addObject("imgServer", systemConfig.getImgServerDomain());
		return mv;
	}

	/**
	 * 手游详情
	 */
	@RequestMapping("edit")
	public ModelAndView edit(String id) {
		ModelAndView mv = new ModelAndView("game/gameEdit");

		if (NumberUtils.isNumber(id)) {
			long idLong = NumberUtils.toLong(id);
			GameInfo editObj = gameInfoService.findById(idLong);
			mv.addObject("editObj", editObj);

			List<GameImg> imgs = gameImgService.findValidByGameId(idLong);
			mv.addObject("imgs", imgs);

			mv.addObject("imgServer", systemConfig.getImgServerDomain());
		}

		return mv;
	}

	/**
	 * 保存
	 */
	@ResponseBody
	@RequestMapping("save")
	public JsonResponseMsg save(HttpServletRequest req, GameInfo game) {
		JsonResponseMsg result = new JsonResponseMsg();
		if (game == null) {
			return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS, game);
		}

		String systemName = "wy-web-admin";
		String src = ImgUploadUtil.genFilePath("game");
		MultipartFile iconFile = Servlets.getMultipartFile(req, "iconFile");
		// 保存icon
		if (iconFile != null) {
			if (iconFile != null) {// 有图片时上传文件
				Map<String, String> imgPaths = ImgUploadUtil.save(iconFile, systemName, src);
				game.setIcon(imgPaths.get(ImgUploadUtil.KEY_MAP_SRC));
			}
		}
		game = gameInfoService.saveOrUpdate(game);

		// 保存轮播图
		List<MultipartFile> coverFiles = Servlets.getMultipartFiles(req, "coverFiles");
		if (CollectionUtils.isNotEmpty(coverFiles)) {
			Long gameId = game.getId();
			Date now = new Date();
			List<GameImg> gameImgs = Lists.newArrayList();
			for (MultipartFile coverFile : coverFiles) {
				Map<String, String> imgPaths = ImgUploadUtil.save(coverFile, systemName, src);
				GameImg gameImg = new GameImg();
				gameImg.setGameId(gameId);
				String url = imgPaths.get(ImgUploadUtil.KEY_MAP_SRC);
				gameImg.setUrl(url);
				gameImg.setUrlMedia(url);
				gameImg.setUrlThumb(url);
				gameImg.setValid(CommonConstant.INT_BOOLEAN_TRUE);
				gameImg.setUpdateDate(now);
				gameImg.setCreateDate(now);
				gameImgs.add(gameImg);
			}
			gameImgService.save(gameImgs);
		}

		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS, game);
	}

	/**
	 * 删除
	 */
	@ResponseBody
	@RequestMapping("delete")
	public JsonResponseMsg delete(String id) {
		if (NumberUtils.isNumber(id)) {
			long idLong = NumberUtils.toLong(id);
			gameInfoService.disabled(idLong);
		}
		return new JsonResponseMsg().fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
	}

	/**
	 * 移除图片
	 */
	@ResponseBody
	@RequestMapping("removeImg")
	public JsonResponseMsg removeImg(String id) {
		if (NumberUtils.isNumber(id)) {
			gameImgService.disabled(NumberUtils.toLong(id));
		}
		return new JsonResponseMsg().fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
	}
}
