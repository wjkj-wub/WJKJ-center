package com.miqtech.master.admin.web.controller.backend.app;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.collect.Lists;
import com.miqtech.master.admin.web.controller.backend.BaseController;
import com.miqtech.master.admin.web.util.Servlets;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.entity.common.IndexAdvertise;
import com.miqtech.master.entity.common.IndexAdvertiseArea;
import com.miqtech.master.entity.common.IndexHot;
import com.miqtech.master.entity.common.IndexMidImg;
import com.miqtech.master.entity.common.SysUserArea;
import com.miqtech.master.entity.common.SystemUser;
import com.miqtech.master.service.activity.ActivityInfoService;
import com.miqtech.master.service.activity.ActivityOverActivityService;
import com.miqtech.master.service.amuse.AmuseActivityInfoService;
import com.miqtech.master.service.bounty.BountyService;
import com.miqtech.master.service.common.IndexHotService;
import com.miqtech.master.service.common.IndexMidImgService;
import com.miqtech.master.service.event.EventService;
import com.miqtech.master.service.index.IndexAdvertiseAreaService;
import com.miqtech.master.service.index.IndexAdvertiseService;
import com.miqtech.master.service.system.SysUserAreaService;
import com.miqtech.master.thirdparty.util.img.ImgUploadUtil;
import com.miqtech.master.utils.BeanUtils;
import com.miqtech.master.utils.JsonUtils;

/**
 * 首页推荐
 *
 */
@Controller
@RequestMapping("appRecommend")
public class AppRecommendController extends BaseController {
	@Autowired
	private IndexAdvertiseService indexAdvertiseService;
	@Autowired
	private IndexAdvertiseAreaService indexAdvertiseAreaService;
	@Autowired
	private SysUserAreaService sysUserAreaService;
	@Autowired
	private ActivityInfoService activityInfoService;
	@Autowired
	private AmuseActivityInfoService amuseActivityInfoService;
	@Autowired
	private IndexHotService indexHotService;
	@Autowired
	private IndexMidImgService indexMidImgService;
	@Autowired
	private EventService eventService;
	@Autowired
	private BountyService bountyService;
	@Autowired
	private ActivityOverActivityService activityOverActivityService;

	@RequestMapping("list")
	public String list(HttpServletRequest request, Model model, Integer tab) {
		String areaCode = sysUserAreaService.findUserAreaCode(Servlets.getSessionUser(request).getId());
		if (tab == null) {
			tab = 1;
		}
		if (tab == 1) {//首页banner
			model.addAttribute("result", indexAdvertiseService.indexBannerRecommend(areaCode, "0"));
		} else if (tab == 9) {//发现版块推荐
			model.addAttribute("result", indexAdvertiseService.indexBannerRecommend(areaCode, "2"));
		}
		model.addAttribute("tab", tab);
		return "app/indexRecommend";
	}

	@RequestMapping("activityList")
	@ResponseBody
	public String activityList(HttpServletRequest request, Integer type, Integer tab) {

		SystemUser user = Servlets.getSessionUser(request);

		String userAreaCode = sysUserAreaService.findUserAreaCode(user.getId());
		userAreaCode = null == userAreaCode ? "000000" : userAreaCode;

		List<Map<String, Object>> result = null;
		if (type == 10) {
			result = Lists.newArrayList();
			List<Map<String, Object>> tmp = activityInfoService.queryActivityForAppRecommend(tab < 5 ? 1 : 2);
			if (!StringUtils.equals(userAreaCode, "000000")) {
				for (Map<String, Object> activity : tmp) {
					String area = activity.get("areas") == null ? "" : activity.get("areas").toString();
					if (null == area || StringUtils.isBlank(area)) {
						result.add(activity);
						break;
					}
					String[] areas = StringUtils.split(area, ",");
					for (String ar : areas) {
						if (ar.startsWith(userAreaCode)) {
							result.add(activity);
							break;
						}
					}
				}
			} else {
				result = tmp;
			}
		} else if (type == 11) {
			result = amuseActivityInfoService.queryAmuseForAppRecommend(tab < 5 ? 1 : 2);
		} else if (type == 15) {
			result = activityOverActivityService.queryInfoForAppRecommend();
		} else if (type == 16) {
			result = eventService.queryEventForAppRecommend();
		} else if (type == 17) {
			result = bountyService.queryEventForAppRecommend();
		}
		return JsonUtils.objectToString(result);
	}

	@RequestMapping("save")
	@ResponseBody
	public String save(HttpServletRequest request, IndexAdvertise indexAdvertise, Integer tab) {
		JsonResponseMsg result = new JsonResponseMsg();
		Long userId = Servlets.getSessionUser(request).getId();
		SysUserArea area = sysUserAreaService.queryBySysUserId(userId);
		if (area == null) {
			result.fill(-1, "该账号未设置地区");
			return JsonUtils.objectToString(result);
		}
		String areaCode = area.getAreaCode();
		int num = indexAdvertiseService.queryAreaNum(areaCode, tab == 1 ? 0 : tab == 9 ? 2 : 1);//tab=1 belong=0, tab=9 belong=2
		if (tab == 1) {
			if (num >= 3 && !areaCode.equals("000000") && indexAdvertise.getId() == null) {
				result.fill(-1, "banner数量不能超过3个");
				return JsonUtils.objectToString(result);
			} else if (num >= 5 && areaCode.equals("000000") && indexAdvertise.getId() == null) {
				result.fill(-1, "banner数量不能超过5个");
				return JsonUtils.objectToString(result);
			}
		} else {
			if (num >= 8 && indexAdvertise.getId() == null) {
				result.fill(-1, "banner数量不能超过8个");
				return JsonUtils.objectToString(result);
			}
		}
		boolean flag = true;
		if (indexAdvertise.getId() != null) {
			IndexAdvertise old = indexAdvertiseService.findById(indexAdvertise.getId());
			if (old.getType() == indexAdvertise.getType() && old.getTargetId() == indexAdvertise.getTargetId()) {
				flag = false;
			}
		}
		if (activityInfoService.alreadyRecommend(indexAdvertise.getType(), tab < 5 ? 1 : 2,
				indexAdvertise.getTargetId()) && flag) {
			result.fill(-1, "该赛事已推荐,不能重复推荐");
			return JsonUtils.objectToString(result);
		}
		MultipartFile file = Servlets.getMultipartFile(request, "imgFile");
		if (file != null) {
			// 上传图片
			String systemName = "wy-web-admin";
			String src = ImgUploadUtil.genFilePath("indexRecommend");
			if (file != null) { // 有图片时上传
				Map<String, String> imgPaths = ImgUploadUtil.save(file, systemName, src);
				indexAdvertise.setImg(imgPaths.get(ImgUploadUtil.KEY_MAP_SRC));
				indexAdvertise.setImgMedia(imgPaths.get(ImgUploadUtil.KEY_MAP_MEDIA));
				indexAdvertise.setImgThumb(imgPaths.get(ImgUploadUtil.KEY_MAP_THUMB));
			}
		} else if (indexAdvertise.getId() == null) {
			result.fill(-1, "必须上传图片");
			return JsonUtils.objectToString(result);
		}

		indexAdvertise.setUpdateDate(new Date());
		indexAdvertise.setUpdateUserId(userId);
		Integer type = indexAdvertise.getType();
		if (indexAdvertise.getType() == 14) {
			indexAdvertise.setDeviceType(2);
		}
		if (indexAdvertise.getId() == null) {
			if (tab == 1) {
				indexAdvertise.setBelong(0);
			} else if (tab == 5) {
				indexAdvertise.setBelong(1);
			} else if (tab == 9) {
				indexAdvertise.setBelong(2);
			}
			indexAdvertise.setValid(1);
			indexAdvertise.setCreateDate(new Date());
			indexAdvertise.setCreateUserId(userId);
			indexAdvertiseService.saveOrUpdate(indexAdvertise);
			IndexAdvertiseArea indexAdvertiseArea = new IndexAdvertiseArea();
			indexAdvertiseArea.setAdvertiseId(indexAdvertise.getId());
			indexAdvertiseArea.setAreaCode(areaCode);
			indexAdvertiseArea.setValid(1);
			indexAdvertiseArea.setCreateDate(new Date());
			indexAdvertiseAreaService.save(indexAdvertiseArea);
			indexAdvertiseService.addToCache(areaCode, indexAdvertise.getBelong());
		} else {
			IndexAdvertise old = indexAdvertiseService.findById(indexAdvertise.getId());
			if (tab != 9) {
				activityInfoService.saveRecommendSign(type, tab < 5 ? 1 : 2, old.getTargetId(), false);
			}
			BeanUtils.updateBean(old, indexAdvertise);
			indexAdvertiseService.saveOrUpdate(old);
			indexAdvertiseService.addToCache(areaCode, old.getBelong());
		}
		if (type == 10 || type == 11) {
			activityInfoService.saveRecommendSign(type, tab < 5 ? 1 : 2, indexAdvertise.getTargetId(), true);
		}
		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
		return JsonUtils.objectToString(result);
	}

	@RequestMapping("hotSave")
	@ResponseBody
	public String hotSave(HttpServletRequest request, IndexHot indexHot, Integer tab) {
		JsonResponseMsg result = new JsonResponseMsg();
		Long userId = Servlets.getSessionUser(request).getId();
		SysUserArea area = sysUserAreaService.queryBySysUserId(userId);
		if (area == null) {
			result.fill(-1, "该账号未设置地区");
			return JsonUtils.objectToString(result);
		}
		String areaCode = area.getAreaCode();
		indexHot.setUpdateDate(new Date());
		indexHot.setUpdateUserId(userId);
		if (tab == 2) {
			if (indexHot.getType() == 10) {
				indexHot.setType(1);
			} else if (indexHot.getType() == 11) {
				indexHot.setType(2);
			}
		} else if (tab == 6) {
			int num = indexHotService.queryNumByAreaCode(tab, areaCode);
			if (num >= 4 && !areaCode.equals("000000") && indexHot.getId() == null) {
				result.fill(-1, "数量不能超过4个");
				return JsonUtils.objectToString(result);
			} else if (num >= 6 && areaCode.equals("000000") && indexHot.getId() == null) {
				result.fill(-1, "数量不能超过6个");
				return JsonUtils.objectToString(result);
			}
			if (indexHot.getType() == 10) {
				indexHot.setType(4);
			} else if (indexHot.getType() == 11) {
				indexHot.setType(5);
			}
		} else if (tab == 7) {
			int num = indexHotService.queryNumByAreaCode(tab, areaCode);
			if (num > 0 && indexHot.getId() == null) {
				result.fill(-1, "数量不能超过1个");
				return JsonUtils.objectToString(result);
			}
			if (indexHot.getType() == 10) {
				indexHot.setType(6);
			}
		} else if (tab == 8) {
			int num = indexHotService.queryNumByAreaCode(tab, areaCode);
			if (num > 0 && indexHot.getId() == null) {
				result.fill(-1, "数量不能超过1个");
				return JsonUtils.objectToString(result);
			}
			if (indexHot.getType() == 11) {
				indexHot.setType(7);
			}
		}
		Integer type = null;
		if (indexHot.getType() == 1 || indexHot.getType() == 4 || indexHot.getType() == 6) {
			type = 10;
		}
		if (indexHot.getType() == 2 || indexHot.getType() == 5 || indexHot.getType() == 7) {
			type = 11;
		}
		boolean flag = true;
		if (indexHot.getId() != null) {
			IndexHot old = indexHotService.findById(indexHot.getId());
			if (old.getType() == indexHot.getType() && old.getTargetId() == indexHot.getTargetId()) {
				flag = false;
			}
		}
		if (activityInfoService.alreadyRecommend(type, tab < 5 ? 1 : 2, indexHot.getTargetId()) && flag) {
			result.fill(-1, "该赛事已推荐,不能重复推荐");
			return JsonUtils.objectToString(result);
		}
		if (indexHot.getId() == null) {
			indexHot.setValid(1);
			indexHot.setCreateDate(new Date());
			indexHot.setCreateUserId(userId);
			indexHot.setAreaCode(areaCode);
			indexHotService.save(indexHot);
		} else {
			IndexHot old = indexHotService.findById(indexHot.getId());
			activityInfoService.saveRecommendSign(type, tab < 5 ? 1 : 2, old.getTargetId(), false);
			BeanUtils.updateBean(old, indexHot);
			indexHotService.save(old);
		}
		indexHotService.addToCache(areaCode, tab);
		activityInfoService.saveRecommendSign(type, tab < 5 ? 1 : 2, indexHot.getTargetId(), true);
		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
		return JsonUtils.objectToString(result);
	}

	@RequestMapping("midSave")
	@ResponseBody
	public String midSave(HttpServletRequest request, IndexMidImg indexMidImg, Integer tab) {
		JsonResponseMsg result = new JsonResponseMsg();
		Long userId = Servlets.getSessionUser(request).getId();
		SysUserArea area = sysUserAreaService.queryBySysUserId(userId);
		if (area == null) {
			result.fill(-1, "该账号未设置地区");
			return JsonUtils.objectToString(result);
		}
		String areaCode = area.getAreaCode();
		List<IndexMidImg> exist = indexMidImgService.findByCategoryAndAreaCodeAndValid(tab == 3 ? 1 : 2, areaCode, 1);
		if (exist.size() > 0 && indexMidImg.getId() == null) {
			result.fill(-1, "腰图只能有一个");
			return JsonUtils.objectToString(result);
		}
		Integer type = null;
		if (indexMidImg.getType() == 10 || indexMidImg.getType() == 11) {
			type = indexMidImg.getType();
		}
		boolean flag = true;
		if (indexMidImg.getId() != null) {
			IndexMidImg old = indexMidImgService.findById(indexMidImg.getId());
			if (old.getType() == indexMidImg.getType() && old.getTargetId() == indexMidImg.getTargetId()) {
				flag = false;
			}
		}
		if (activityInfoService.alreadyRecommend(type, tab < 5 ? 1 : 2, indexMidImg.getTargetId()) && flag) {
			result.fill(-1, "该赛事已推荐,不能重复推荐");
			return JsonUtils.objectToString(result);
		}
		if (indexMidImg.getType() == 5) {
			indexMidImg.setType(4);
		} else if (indexMidImg.getType() == 13) {
			indexMidImg.setType(5);
		}
		if (tab == 4 && indexMidImg.getType() != 10 && indexMidImg.getType() != 11) {
			MultipartFile file = Servlets.getMultipartFile(request, "imgFile");
			if (file != null) {
				// 上传图片
				String systemName = "wy-web-admin";
				String src = ImgUploadUtil.genFilePath("indexRecommend");
				if (file != null) { // 有图片时上传
					Map<String, String> imgPaths = ImgUploadUtil.save(file, systemName, src);
					indexMidImg.setImg(imgPaths.get(ImgUploadUtil.KEY_MAP_SRC));
				}
			} else if (indexMidImg.getId() == null) {
				result.fill(-1, "必须上传图片");
				return JsonUtils.objectToString(result);
			}
		}

		indexMidImg.setUpdateDate(new Date());
		indexMidImg.setUpdateUserId(userId);
		if (indexMidImg.getType() == 10) {
			indexMidImg.setType(1);
		} else if (indexMidImg.getType() == 11) {
			indexMidImg.setType(2);
		}
		if (indexMidImg.getId() == null) {
			if (tab == 3) {
				indexMidImg.setCategory(1);
			} else if (tab == 4) {
				indexMidImg.setCategory(2);
			}
			indexMidImg.setValid(1);
			indexMidImg.setCreateDate(new Date());
			indexMidImg.setCreateUserId(userId);
			indexMidImg.setAreaCode(areaCode);
			indexMidImgService.save(indexMidImg);
		} else {
			IndexMidImg old = indexMidImgService.findById(indexMidImg.getId());
			activityInfoService.saveRecommendSign(type, tab < 5 ? 1 : 2, old.getTargetId(), false);
			BeanUtils.updateBean(old, indexMidImg);
			indexMidImgService.save(old);
		}
		indexMidImgService.addToCache(areaCode, tab);
		activityInfoService.saveRecommendSign(type, tab < 5 ? 1 : 2, indexMidImg.getTargetId(), true);
		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
		return JsonUtils.objectToString(result);
	}

	@RequestMapping("detail")
	@ResponseBody
	public String detail(Long id, Integer tab, Integer type) {
		JsonResponseMsg result = new JsonResponseMsg();
		if (tab == 1 || tab == 5 || tab == 9) {
			result.setObject(indexAdvertiseService.findById(id));
		} else if (tab == 2 || tab == 6 || tab == 7 || tab == 8) {
			IndexHot indexHot = indexHotService.findById(id);
			if (indexHot.getType() == 1 || indexHot.getType() == 4 || indexHot.getType() == 6) {
				indexHot.setType(10);
			} else if (indexHot.getType() == 2 || indexHot.getType() == 5 || indexHot.getType() == 7) {
				indexHot.setType(11);
			}
			result.setObject(indexHot);
		} else if (tab == 3) {
			IndexMidImg indexMidImg = indexMidImgService.findById(id);
			if (indexMidImg.getType() == 1) {
				indexMidImg.setType(10);
			} else if (indexMidImg.getType() == 2) {
				indexMidImg.setType(11);
			}
			result.setObject(indexMidImg);
		} else if (tab == 4) {
			IndexMidImg indexMidImg = indexMidImgService.findById(id);
			if (indexMidImg.getType() == 1) {
				indexMidImg.setType(10);
			} else if (indexMidImg.getType() == 2) {
				indexMidImg.setType(11);
			} else if (indexMidImg.getType() == 4) {
				indexMidImg.setType(5);
			} else if (indexMidImg.getType() == 5) {
				indexMidImg.setType(13);
			}
			result.setObject(indexMidImg);
		}
		return JsonUtils.objectToString(result);
	}

	@RequestMapping("delete/{id}")
	@ResponseBody
	public String delete(@PathVariable Long id, Integer tab) {
		JsonResponseMsg result = new JsonResponseMsg();
		if (tab == 1 || tab == 5 || tab == 9) {
			IndexAdvertise indexAdvertise = indexAdvertiseService.findById(id);
			indexAdvertise.setValid(0);
			indexAdvertiseService.saveOrUpdate(indexAdvertise);
			if (indexAdvertise.getType() == 10 || indexAdvertise.getType() == 11) {
				activityInfoService.saveRecommendSign(indexAdvertise.getType(), tab < 5 ? 1 : 2,
						indexAdvertise.getTargetId(), false);
			}
			IndexAdvertiseArea area = indexAdvertiseAreaService.findByAdvertiseId(indexAdvertise.getId());
			if (area != null) {
				indexAdvertiseService.addToCache(area.getAreaCode(), indexAdvertise.getBelong());
			}
		} else if (tab == 2 || tab == 6 || tab == 7 || tab == 8) {
			IndexHot indexHot = indexHotService.findById(id);
			indexHot.setValid(0);
			indexHotService.save(indexHot);
			Integer type = indexHot.getType();
			if (type == 1 || type == 4 || type == 6) {
				type = 10;
			} else if (type == 2 || type == 5 || type == 7) {
				type = 11;
			}
			if (type == 10 || type == 11) {
				activityInfoService.saveRecommendSign(type, tab < 5 ? 1 : 2, indexHot.getTargetId(), false);
			}
			indexHotService.addToCache(indexHot.getAreaCode(), tab);
		} else if (tab == 3 || tab == 4) {
			IndexMidImg indexMidImg = indexMidImgService.findById(id);
			indexMidImg.setValid(0);
			indexMidImgService.save(indexMidImg);
			Integer type = indexMidImg.getType();
			if (type == 1) {
				type = 10;
			} else if (type == 2) {
				type = 11;
			}
			if (type == 10 || type == 11) {
				activityInfoService.saveRecommendSign(type, tab < 5 ? 1 : 2, indexMidImg.getTargetId(), false);
			}
			indexMidImgService.addToCache(indexMidImg.getAreaCode(), tab);
		}
		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
		return JsonUtils.objectToString(result);
	}
}
