package com.miqtech.master.admin.web.controller.backend;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.miqtech.master.admin.web.util.Servlets;
import com.miqtech.master.config.SystemConfig;
import com.miqtech.master.consts.CacheKeyConstant;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.consts.SystemUserConstant;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.entity.activity.*;
import com.miqtech.master.entity.common.SystemArea;
import com.miqtech.master.entity.common.SystemUser;
import com.miqtech.master.entity.user.UserInfo;
import com.miqtech.master.service.activity.*;
import com.miqtech.master.service.common.ObjectRedisOperateService;
import com.miqtech.master.service.netbar.NetbarInfoService;
import com.miqtech.master.service.system.SysUserAreaService;
import com.miqtech.master.service.system.SystemAreaService;
import com.miqtech.master.service.user.UserInfoService;
import com.miqtech.master.thirdparty.util.img.ImgUploadUtil;
import com.miqtech.master.utils.*;
import com.miqtech.master.vo.PageVO;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.awt.*;
import java.beans.PropertyEditorSupport;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

@Controller
@RequestMapping("activityInfo")
public class ActivityInfoController extends BaseController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ActivityInfoController.class);
	@Autowired
	private SystemConfig systemConfig;

	@Autowired
	private ActivityInfoService activityInfoService;
	@Autowired
	private ActivityItemService activityItemService;
	@Autowired
	private ActivityRoundService activityRoundService;
	@Autowired
	private SystemAreaService systemAreaService;
	@Autowired
	private SysUserAreaService sysUserAreaService;
	@Autowired
	private ObjectRedisOperateService objectRedisOperateService;
	@Autowired
	private ActivityNetbarQrcodeService activityNetbarQrcodeService;
	@Autowired
	private UserInfoService userInfoService;
	@Autowired
	private ActivityMemberService activityMemberService;
	@Autowired
	private ActivityTeamService activityTeamService;
	@Autowired
	private ActivityApplyService activityApplyService;
	@Autowired
	private NetbarInfoService netbarInfoService;

	/**
	 * 列表
	 */
	@RequestMapping("/list/{page}")
	public ModelAndView list(HttpServletRequest request, @PathVariable("page") int page, String title, String itemId,
			String areaCode, String beginDate, String endDate, String status) {
		ModelAndView mv = new ModelAndView("/activity/activityList");

		// 处理参数
		Map<String, Object> params = new HashMap<String, Object>(16);
		params.put("title", title);
		params.put("item_id", itemId);
		params.put("areaCode", areaCode);
		params.put("beginDate", beginDate);
		params.put("endDate", endDate);
		params.put("status", status);

		// 匹配登陆用户的地区设置
		SystemUser user = Servlets.getSessionUser(request);
		if (SystemUserConstant.TYPE_ACTIVITY_ADMIN.equals(user.getUserType())) {
			String userAreaCode = sysUserAreaService.findUserAreaCode(user.getId());
			mv.addObject("userAreaCode", userAreaCode);
			params.put("userAreaCode", userAreaCode);
		}

		PageVO vo = activityInfoService.adminActivities(page, params);
		pageModels(mv, vo.getList(), page, vo.getTotal());
		mv.addObject("params", params);

		// 查询赛事项目
		List<ActivityItem> items = activityItemService.findAll();
		mv.addObject("items", items);

		// 匹配地区树数据
		List<SystemArea> areas = systemAreaService.getTree(true);
		mv.addObject("areas", areas);
		mv.addObject("areasStr", JsonUtils.objectToString(areas));

		return mv;
	}

	/**
	 * 请求详细信息（单个对象），跳转url
	 */
	@ResponseBody
	@RequestMapping("/url/{id}")
	public ModelAndView syDetail(@PathVariable("id") long id) {
		ModelAndView mv = new ModelAndView("/activity/activityList"); //跳转到gameInfo.ftl页面
		ActivityInfo activityInfo = activityInfoService.findById(id);
		List<ActivityInfo> list = new ArrayList<>();
		list.add(activityInfo);
		pageModels(mv, list, 1, 1);
		return mv;
	}

	/**
	 * 请求详情数据
	 */
	@ResponseBody
	@RequestMapping("/detail/{id}")
	public JsonResponseMsg detail(@PathVariable("id") long id) {
		JsonResponseMsg result = new JsonResponseMsg();

		ActivityInfo activityInfo = activityInfoService.findById(id);

		// 添加场次信息
		List<ActivityRound> rounds = activityRoundService.getRoundsByActivityId(id);
		activityInfo.setRounds(rounds);

		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS, activityInfo);
		return result;
	}

	/**
	 * 保存
	 */
	@ResponseBody
	@RequestMapping("save")
	public JsonResponseMsg save(HttpServletRequest req, ActivityInfo activityInfo) {
		JsonResponseMsg result = new JsonResponseMsg();

		if (activityInfo != null) {
			// 检查是否被设置在首页推荐中，需先取消设置才能修改发布状态
			Long id = activityInfo.getId();
			if (null != id && !CommonConstant.INT_BOOLEAN_TRUE.equals(activityInfo.getIsGround())) {// 只针对 编辑 且 app中显示设置为取消 操作
				boolean inIndex = activityInfoService.isRecommend(id);
				if (inIndex) {
					return result.fill(CommonConstant.CODE_ERROR_LOGIC, "当前赛事被设置在展位中，请先下架，再设置为不发布");
				}
			}

			Date date = new Date();
			activityInfo.setUpdateDate(date);
			if (null != id) {// 新增时须基于旧值做更新
				activityInfo = BeanUtils.updateBean(activityInfoService.findById(activityInfo.getId()), activityInfo);
				//将原先的场次二维码删除
				activityNetbarQrcodeService.initChangciInfo(activityInfo.getId());
			} else {
				activityInfo.setRecommendSign(-1);
				activityInfo.setValid(CommonConstant.INT_BOOLEAN_TRUE);
				activityInfo.setCreateDate(date);
			}

			// 上传icon
			String systemName = "wy-web-admin";
			String src = ImgUploadUtil.genFilePath("activity");
			MultipartFile file = Servlets.getMultipartFile(req, "icon_file");
			if (file != null) {// 有图片时上传文件
				Map<String, String> imgPaths = ImgUploadUtil.save(file, systemName, src);
				activityInfo.setIcon(imgPaths.get(ImgUploadUtil.KEY_MAP_SRC));
				activityInfo.setIconMedia(imgPaths.get(ImgUploadUtil.KEY_MAP_MEDIA));
				activityInfo.setIconThumb(imgPaths.get(ImgUploadUtil.KEY_MAP_THUMB));
			}
			// 上传首图
			file = Servlets.getMultipartFile(req, "cover_file");
			if (file != null) {// 有图片时上传文件
				Map<String, String> imgPaths = ImgUploadUtil.save(file, systemName, src);
				activityInfo.setCover(imgPaths.get(ImgUploadUtil.KEY_MAP_SRC));
				activityInfo.setCoverMedia(imgPaths.get(ImgUploadUtil.KEY_MAP_MEDIA));
				activityInfo.setCoverThumb(imgPaths.get(ImgUploadUtil.KEY_MAP_THUMB));
			}

			// 保存场次信息
			SystemUser sysUser = Servlets.getSessionUser(req);
			activityInfo.setUpdateUserId(sysUser.getId());
			if (activityInfo.getId() == null) {
				activityInfo.setCreateUserId(sysUser.getId());
			}
			activityInfo = activityInfoService.save(activityInfo);
			id = activityInfo.getId();

			// 清理缓存
			Joiner joiner = Joiner.on("_");
			String cacheKey = joiner.join(CacheKeyConstant.API_CACHE_ACTIVITY_APPLY_DATES_NETBARS,
					activityInfo.getId().toString());
			objectRedisOperateService.delData(cacheKey);

			int roundCount = 0;
			if (activityInfo != null && CollectionUtils.isNotEmpty(activityInfo.getRounds())) {
				// 重新计算赛事的始末时间
				Date beginDate = null;
				Date overDate = null;
				String netbars = null;
				String areas = null;
				List<ActivityRound> rounds = activityInfo.getRounds();
				if (CollectionUtils.isNotEmpty(rounds)) {
					QrcodeUtil zu = new QrcodeUtil();
					zu.setLogopath(systemConfig.getQrLogoPath());
					zu.setLogo_height(200);
					zu.setLogo_width(200);
					zu.setQrcode_size(1000);
					zu.setFont(new Font("SimHei", Font.PLAIN, 30));
					zu.setLogopath(systemConfig.getQrLogoPath());
					String temp = "/" + "tmp" + "/" + systemName + "/";
					Joiner emptyJoiner = Joiner.on("");
					for (ActivityRound r : rounds) {
						if (r.getRound() == null) {
							continue;
						}

						// 重新设置netbars和areas，过滤掉空的id
						r.setNetbars(filterEmptyIds(r.getNetbars()));
						r.setAreas(filterEmptyIds(r.getAreas()));

						if (r.getRound() == 1) {
							netbars = r.getNetbars();
							areas = r.getAreas();
						} else {
							if (StringUtils.isNotBlank(netbars) && StringUtils.isNotBlank(r.getNetbars())) {
								netbars = emptyJoiner.join(netbars, ",");
							}
							netbars = emptyJoiner.join(netbars, r.getNetbars());
							if (StringUtils.isNotBlank(areas) && StringUtils.isNotBlank(r.getAreas())) {
								areas = emptyJoiner.join(areas, ",");
							}
							areas = emptyJoiner.join(areas, r.getAreas());
						}

						if (r.getBeginTime() == null) {
							r.setBeginTime(new Date());
						}
						if (r.getOverTime() == null) {
							Date overTime = r.getBeginTime();
							overTime = org.apache.commons.lang3.time.DateUtils.setHours(overTime, 23);
							overTime = org.apache.commons.lang3.time.DateUtils.setMinutes(overTime, 59);
							overTime = org.apache.commons.lang3.time.DateUtils.setSeconds(overTime, 59);
							r.setOverTime(overTime);
						}
						if (r.getEndTime() == null) {
							r.setEndTime(r.getOverTime());
						}
						if (beginDate == null || beginDate.after(r.getBeginTime())) {
							beginDate = r.getBeginTime();
						}
						if (overDate == null || overDate.before(r.getOverTime())) {
							overDate = r.getOverTime();
						}

						// 检查场次时间是否在比赛时间范围内
						if (activityInfo.getStartTime() == null) {
							return result.fill(CommonConstant.CODE_ERROR_LOGIC, "请填写比赛开始时间");
						}
						if (activityInfo.getEndTime() == null) {
							return result.fill(CommonConstant.CODE_ERROR_LOGIC, "请填写比赛结束时间");
						}
						if (r.getBeginTime().after(activityInfo.getEndTime())
								|| r.getOverTime().after(activityInfo.getEndTime())
								|| r.getEndTime().after(activityInfo.getEndTime())) {
							return result.fill(CommonConstant.CODE_ERROR_LOGIC, "场次" + r.getRound() + "的时间不应超过比赛结束时间");
						}
						if (r.getEndTime().before(activityInfo.getStartTime())) {
							return result.fill(CommonConstant.CODE_ERROR_LOGIC, "场次" + r.getRound() + "的结束时间不应在比赛开始之前");
						}

						r.setActivityId(activityInfo.getId());

						// 确保相同赛事的相同场次不重复
						if (r.getId() == null) {
							activityRoundService.deleteByActivityIdAndRound(r.getActivityId(), r.getRound());
						}

						activityRoundService.save(r);

						//添加场次二维码信息
						String nerbarIds = filterEmptyIds(r.getNetbars());
						if (nerbarIds != null) {
							StringTokenizer commaTokenizer = new StringTokenizer(nerbarIds, ",");
							while (commaTokenizer.hasMoreTokens()) {
								ActivityNetbarQrcode qr = new ActivityNetbarQrcode();
								qr.setActivityId(r.getActivityId());
								qr.setRound(r.getRound());
								String currentToken = commaTokenizer.nextToken();
								qr.setNetbarId(Long.parseLong(currentToken));
								//获取比赛地区、比赛赛点信息
								List<Map<String, Object>> list = activityNetbarQrcodeService
										.findActivityRoundNetbarInfo(Long.parseLong(currentToken));
								if (CollectionUtils.isNotEmpty(list)) {
									String nerbarName = (String) list.get(0).get("nerbarName");
									String areaName = (String) list.get(0).get("areaName");
									Date gameTime = r.getOverTime();
									//生成二维码信息
									SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
									String content = systemConfig.getAppDomain() + "load/wy?id=" + id + "&round="
											+ r.getRound() + "&netbarId=" + Long.parseLong(currentToken);// 内容
									String[][] mess = { { "类型：场次二维码", "用途：扫码报名参赛", "" }, { "比赛地区：" + areaName,
											"比赛地点：" + nerbarName, "比赛时间：" + format.format(gameTime) },
											//	{"对应比赛：","[浙江] 网娱大师杯","用途:扫码报名参赛","用途：品宣传播"},
									};
									String destFileName = "image_" + id + "_" + r.getRound() + "_"
											+ Long.parseLong(currentToken);
									String filepath = zu.createImageAndWord(content, mess, destFileName, temp);
									File uploadFile = new File(filepath);
									if (uploadFile != null) {// 有图片时上传文件
										Map<String, String> imgPaths = ImgUploadUtil.save(uploadFile, systemName, src,
												false);
										qr.setQrcode(imgPaths.get(ImgUploadUtil.KEY_MAP_SRC));
									}
									activityNetbarQrcodeService.saveQrcode(qr);
								}
							}
						}
						roundCount++;
					}

					String gameTitle = activityInfo.getTitle();
					//生成赛事二维码
					String content = systemConfig.getAppDomain() + "share/activity/" + id;// 内容
					String[][] mess = { { "对应比赛：", gameTitle, "类型：扫码报名参赛", "用途：品宣传播" },
							//	{"对应比赛：","[浙江] 网娱大师杯","用途:扫码报名参赛","用途：品宣传播"},
					};
					String destFileName = "image_" + id;
					String filepath = zu.createImageAndWord(content, mess, destFileName, temp);
					File uploadFile = new File(filepath);
					if (uploadFile != null) {// 有图片时上传文件
						Map<String, String> imgPaths = ImgUploadUtil.save(uploadFile, systemName, src, false);
						activityInfo.setQrcode(imgPaths.get(ImgUploadUtil.KEY_MAP_SRC));
						//activityInfo.(imgPaths.get(ImgUploadUtil.KEY_MAP_SRC));
					}

					// 更新始末时间
					if (activityInfo.getStartTime() == null) {
						activityInfo.setStartTime(beginDate);
					}
					if (activityInfo.getEndTime() == null) {
						activityInfo.setEndTime(overDate);
					}
					activityInfo.setBeginTime(beginDate);
					activityInfo.setOverTime(overDate);
					activityInfo.setNetbars(netbars);
					activityInfo.setAreas(areas);
					activityInfo.setRoundCount(roundCount);
					activityInfoService.save(activityInfo);
				}

			}
		}

		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
		return result;
	}

	/**
	 * 过滤掉id字符串中的空id
	 */
	private String filterEmptyIds(String ids) {
		// 去掉首端及中间空ID
		while (StringUtils.isNotBlank(ids) && ids.contains(",,")) {
			ids = ids.replaceAll(",,", ",");
		}

		// 去掉尾端空ID
		if (StringUtils.isNotBlank(ids)) {
			ids = ids.replaceAll(",$", "");
		}

		return ids;
	}

	/**
	 * 删除
	 */
	@ResponseBody
	@RequestMapping("delete/{id}")
	public JsonResponseMsg delete(@PathVariable("id") long id) {
		JsonResponseMsg result = new JsonResponseMsg();

		boolean inIndex = activityInfoService.isRecommend(id);
		if (inIndex) {
			return result.fill(CommonConstant.CODE_ERROR_LOGIC, "当前赛事被设置在展位中，请先下架，再设置为不发布");
		}

		activityInfoService.delete(id);
		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
		return result;
	}

	@InitBinder
	private void initDateBinder(WebDataBinder binder) {
		binder.registerCustomEditor(Date.class, new PropertyEditorSupport() {
			@Override
			public void setAsText(String value) {
				Date date = null;
				try {
					date = DateUtils.stringToDateYyyyMMdd(value);
				} catch (ParseException e) {
					LOGGER.error("格式化时间参数异常", e);
				}
				setValue(date);
			}
		});
	}

	/**
	 * 场次页面
	 */
	@ResponseBody
	@RequestMapping("/changciinfo/{page}")
	public ModelAndView aalist(HttpServletRequest request, @PathVariable("page") int page, Long activityId,
			String overTime, String areaInfoId, Long netbarInfoId) {
		ModelAndView mv = new ModelAndView("/activity/changciList");
		PageVO vo = activityInfoService.queryChangciInfo(page, activityId, overTime, areaInfoId, netbarInfoId);
		pageModels(mv, vo.getList(), page, vo.getTotal());
		//查询比赛地区
		List<Map<String, Object>> areaInfo = activityInfoService.queryAreaInfo(activityId);
		//查询比赛赛点
		List<Map<String, Object>> netbarInfo = activityInfoService.queryNetbarInfo(activityId);
		//赛事基本信息地区，报名开始时间，比赛开始时间比赛结束时间查询
		List<Map<String, Object>> gameInfo = activityInfoService.queryGameInfo(activityId);
		mv.addObject("areaInfo", areaInfo);
		mv.addObject("netbarInfo", netbarInfo);
		mv.addObject("activityId", activityId);
		mv.addObject("overTime", overTime);
		mv.addObject("areaInfoId", areaInfoId);
		mv.addObject("netbarInfoId", netbarInfoId);
		mv.addObject("gameInfo", gameInfo);
		mv.addObject("imgServer", systemConfig.getImgServerDomain());
		return mv;
	}

	/**
	 * 二维码信息初始化
	 */
	@ResponseBody
	@RequestMapping("init/{id}")
	public JsonResponseMsg init(@PathVariable("id") long id) {
		JsonResponseMsg result = new JsonResponseMsg();
		ActivityInfo activityInfo = activityInfoService.findById(id);
		String systemName = "wy-web-admin";
		String src = ImgUploadUtil.genFilePath("activity");
		String temp = "/" + "tmp" + "/" + systemName + "/";
		//初始化赛事二维码
		String content = systemConfig.getAppDomain() + "share/activity/" + id;// 内容
		String[][] mess = { { "对应比赛：", activityInfo.getTitle(), "类型：扫码报名参赛", "用途：品宣传播" },
				//	{"对应比赛：","[浙江] 网娱大师杯","用途:扫码报名参赛","用途：品宣传播"},
		};
		QrcodeUtil zu = new QrcodeUtil();
		zu.setLogo_height(200);
		zu.setLogo_width(200);
		zu.setQrcode_size(1000);
		zu.setFont(new Font("SimHei", Font.PLAIN, 30));
		zu.setLogopath(systemConfig.getQrLogoPath());
		String destFileName = "image_" + id;
		String filepath = zu.createImageAndWord(content, mess, destFileName, temp);
		File uploadFile = new File(filepath);
		if (uploadFile != null) {// 有图片时上传文件
			Map<String, String> imgPaths = ImgUploadUtil.save(uploadFile, systemName, src, false);
			activityInfo.setQrcode(imgPaths.get(ImgUploadUtil.KEY_MAP_SRC));
			//activityInfo.(imgPaths.get(ImgUploadUtil.KEY_MAP_SRC));
		}
		activityInfoService.save(activityInfo);
		//初始化场次二维码
		//获取场次、网吧信息
		List<Map<String, Object>> list1 = activityInfoService.queryRoundInfo(id);
		if (list1 != null) {
			for (Map<String, Object> m : list1) {
				Integer round = MapUtils.getInteger(m, "round");
				String netbars = MapUtils.getString(m, "netbars");

				StringTokenizer commaTokenizer = new StringTokenizer(netbars, ",");
				while (commaTokenizer.hasMoreTokens()) {
					ActivityNetbarQrcode qr = new ActivityNetbarQrcode();
					//获取比赛地区、比赛赛点信息
					String currentToken = commaTokenizer.nextToken();
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
					List<Map<String, Object>> list = activityNetbarQrcodeService
							.findActivityRoundNetbarInfo(Long.parseLong(currentToken), id, round);
					if (list != null) {
						String nerbarName = (String) list.get(0).get("nerbarName");
						String areaName = (String) list.get(0).get("areaName");
						Date gameTime = (Date) list.get(0).get("over_time");
						String[][] mess1 = { { "类型：场次二维码", "用途：扫码报名参赛", "" },
								{ "比赛地区：" + areaName, "比赛地点：" + nerbarName, "比赛时间：" + format.format(gameTime) },
								//	{"对应比赛：","[浙江] 网娱大师杯","用途:扫码报名参赛","用途：品宣传播"},
						};
						qr.setRound(round);
						qr.setNetbarId(Long.parseLong(currentToken));
						qr.setActivityId(id);
						//生成二维码信息

						String content1 = systemConfig.getAppDomain() + "load/wy?id=" + id + "&round=" + round
								+ "&netbarId=" + Long.parseLong(currentToken);// 内容
						String destFileName1 = "image_" + id + "_" + round + "_" + Long.parseLong(currentToken);
						String filepath1 = zu.createImageAndWord(content1, mess1, destFileName1, temp);
						File uploadFile1 = new File(filepath1);
						if (uploadFile1 != null) {// 有图片时上传文件
							Map<String, String> imgPaths = ImgUploadUtil.save(uploadFile1, systemName, src, false);
							qr.setQrcode(imgPaths.get(ImgUploadUtil.KEY_MAP_SRC));
						}
						activityNetbarQrcodeService.saveQrcode(qr);
					}

				}

			}
		}
		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
		return result;
	}

	/**
	 * 导入赛事战队页面
	 */
	@RequestMapping("addTeams")
	public ModelAndView addTeams() {
		return new ModelAndView("activity/addTeams");
	}

	/**
	 * 导入赛事战队
	 */
	@ResponseBody
	@RequestMapping("importTeams")
	public JsonResponseMsg importTeams(HttpServletRequest req) {
		JsonResponseMsg result = new JsonResponseMsg();

		MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) req;
		Iterator<String> fileNamesIt = multiRequest.getFileNames();
		if (!fileNamesIt.hasNext()) {
			return result.fill(CommonConstant.CODE_ERROR_LOGIC, "请上传文件");
		}

		String originFileName = fileNamesIt.next();
		int suffixIndex = originFileName.lastIndexOf(".");
		String fileName = originFileName.substring(0, suffixIndex);
		String[] params = fileName.split("_");
		if (params == null || params.length < 3) {
			return result.fill(CommonConstant.CODE_ERROR_LOGIC, "文件名不符合要求");
		}

		// 解析参数
		long activityId = NumberUtils.toLong(params[0], 0);
		int round = NumberUtils.toInt(params[1], 0);
		String netbarName = params[2];
		List<Map<String, Object>> netbars = netbarInfoService.queryActivityNetbar(activityId, round, netbarName);
		Long netbarId = null;
		if (CollectionUtils.isNotEmpty(netbars)) {
			netbarId = MapUtils.getLongValue(netbars.get(0), "id");
		}
		if (netbarId == null) {
			return result.fill(CommonConstant.CODE_ERROR_LOGIC, "找不到对应的网吧");
		}

		// 读取excel
		MultipartFile file = Servlets.getMultipartFile(req, originFileName);
		Workbook wb = ExcelUtils.readMultipartFile(file);
		Sheet s = wb.getSheet(0);
		int rows = s.getRows();
		if (rows <= 0) {
			return result.fill(CommonConstant.CODE_ERROR_LOGIC, "表格无数据");
		}

		String[] labors = new String[] { "adc", "ap", "gank", "mid", "top", "打野", "上单", "中野辅", "辅助", "清兵", "动脑筋", "上野",
				"除了辅助", "上中下野", "聊天", "全能", "躺赢", "挂机", "啥都行" };

		// 插入数据
		Long teamId = null;
		List<ActivityMember> members = Lists.newArrayList();
		int teamNumber = 0;
		int memberNumber = 0;
		for (int i = 0; i < rows; i++) {
			Cell[] cells = s.getRow(i);
			String telephone = cells.length >= 1 ? cells[0].getContents() : "";
			String teamName = cells.length >= 2 ? cells[1].getContents() : "";

			UserInfo user = userInfoService.queryByName(telephone);
			if (user == null) {
				activityMemberService.save(members);
				return result.fill(CommonConstant.CODE_ERROR_LOGIC,
						"找不到手机号:" + telephone + "对应的用户,已导入" + teamNumber + "个战队," + memberNumber + "个战队成员");
			}
			Long userId = user.getId();
			String nickname = user.getNickname();
			String idcard = user.getIdCard();
			String qq = user.getQq();
			String labor = labors[RandomUtils.nextInt(0, labors.length)];

			if (StringUtils.isNotBlank(teamName)) {// 创建战队
				ActivityTeam activityTeam = new ActivityTeam();
				activityTeam.setNetbarId(netbarId);
				activityTeam.setActivityId(activityId);
				activityTeam.setRound(round);
				activityTeam.setName(teamName);
				activityTeam.setServer(null);
				activityTeam.setValid(1);
				activityTeam.setCreateDate(new Date());
				activityTeam = activityTeamService.save(activityTeam);

				ActivityMember activityMember = new ActivityMember();
				activityMember.setActivityId(activityId);
				activityMember.setUserId(userId);
				activityMember.setTeamId(activityTeam.getId());
				activityMember.setRound(round);
				activityMember.setName(nickname);
				activityMember.setIdCard(idcard);
				activityMember.setTelephone(telephone);
				activityMember.setQq(qq);
				activityMember.setLabor(labor);
				activityMember.setIsMonitor(1);
				activityMember.setIsEnter(1);
				activityMember.setValid(1);
				activityMember.setCreateDate(new Date());
				activityMember.setSigned(1);
				activityMember = activityMemberService.save(activityMember);

				activityTeam.setMemId(activityMember.getId());
				activityTeam = activityTeamService.save(activityTeam);

				ActivityApply activityApply = new ActivityApply();
				activityApply.setActivityId(activityId);
				activityApply.setTargetId(activityTeam.getId());
				activityApply.setNetbarId(netbarId);
				activityApply.setRound(round);
				activityApply.setType(2);
				activityApply.setValid(1);
				activityApply.setCreateDate(new Date());
				activityApplyService.save(activityApply);

				teamId = activityTeam.getId();
				teamNumber++;
			} else {// 加入战队
				ActivityMember m = new ActivityMember();
				m.setName(nickname);
				m.setLabor(labor);
				m.setTelephone(telephone);
				m.setActivityId(activityId);
				m.setUserId(userId);
				m.setTeamId(teamId);
				m.setRound(round);
				m.setIsMonitor(0);
				m.setIsEnter(1);
				m.setValid(1);
				m.setIsAccept(1);
				m.setCreateDate(new Date());
				members.add(m);
				if (members.size() % 500 == 0) {
					activityMemberService.save(members);
				}
			}

			memberNumber++;
		}
		activityMemberService.save(members);

		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS,
				"操作完成,共导入" + teamNumber + "个战队," + memberNumber + "个战队成员");
	}

	public static void main(String[] args) {
		String[] labors = new String[] { "adc", "ap", "gank", "mid", "top", "打野", "上单", "中野辅", "辅助", "清兵", "上野", "除了辅助",
				"上中下野", "聊天", "全能", "躺赢", "挂机", "啥都行" };
		for (int i = 0; i < 100; i++) {
			System.out.println(labors[RandomUtils.nextInt(0, labors.length)]);
		}
	}

}
