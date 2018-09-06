package com.miqtech.master.service.activity;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.miqtech.master.consts.CacheKeyConstant;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.consts.InformationConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.activity.ActivityOverActivityDao;
import com.miqtech.master.dao.activity.ActivityOverActivityImgDao;
import com.miqtech.master.entity.Pager;
import com.miqtech.master.entity.activity.ActivityOverActivity;
import com.miqtech.master.entity.activity.ActivityOverActivityImg;
import com.miqtech.master.entity.activity.ActivityOverActivityPraise;
import com.miqtech.master.entity.user.UserFavor;
import com.miqtech.master.service.common.ObjectRedisOperateService;
import com.miqtech.master.service.common.StringRedisOperateService;
import com.miqtech.master.service.user.UserFavorService;
import com.miqtech.master.utils.BeanUtils;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.vo.ActivityOverActivityModuleVO;
import com.miqtech.master.vo.PageVO;

@Component
public class ActivityOverActivityService {

	@Autowired
	QueryDao queryDao;
	@Autowired
	ActivityOverActivityDao activityOverActivityDao;
	@Autowired
	ActivityOverActivityImgDao activityOverActivityImgDao;
	@Autowired
	ObjectRedisOperateService objectRedisOperateService;
	@Autowired
	private ActivityOverActivityPraiseService activityOverActivityPraiseService;
	@Autowired
	private StringRedisOperateService stringRedisOperateService;

	public List<ActivityOverActivity> findByIdIn(List<Long> ids) {
		if (CollectionUtils.isEmpty(ids)) {
			return null;
		}
		return activityOverActivityDao.findByIdIn(ids);
	}

	/**
	 * 获得 热门资讯
	 */
	public List<Map<String, Object>> getHotInfos() {
		String hotInfosSql = "select id, title, cover,cover_media ,cover_thumb ,read_num, if(isnull(is_subject), 0, is_subject) is_subject ,create_date from activity_t_over_activities where is_valid = 1 and is_hot = 1 order by create_date desc";
		return queryDao.queryMap(hotInfosSql);
	}

	/**
	 * 获得 赛事资讯（附加pid查询条件）
	 */
	public PageVO getInfos(int page, int rows, Long pid) {
		// 查询资讯列表
		String sqlInfos = "select id, title, if(isnull(brief), '', brief) brief, icon,icon_media,icon_thumb, if(isnull(is_subject), 0, is_subject) is_subject,read_num ,create_date from activity_t_over_activities where is_valid = 1"
				+ (pid == null ? " and (pid is null or pid < 1)" : " AND PID = " + pid)
				+ " order by create_date desc limit :pageStart, :pageNum";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("pageStart", (page - 1) * rows);
		params.put("pageNum", rows);
		List<Map<String, Object>> result = queryDao.queryMap(sqlInfos, params);

		// 组装vo
		if (result == null) {
			result = new ArrayList<>();
		}
		PageVO vo = new PageVO();
		vo.setList(result);
		String sqlTotal = "select count(1) from activity_t_over_activities where is_valid = 1"
				+ (pid == null ? "" : " AND PID = " + pid);
		BigInteger bi = (BigInteger) queryDao.query(sqlTotal);
		if (page * rows >= bi.intValue()) {
			vo.setIsLast(1);
		}

		return vo;
	}

	/*
	 * 获得 赛事资讯
	 */
	public PageVO getInfos(int page, int rows) {
		return getInfos(page, rows, null);
	}

	/**
	 * 获得某项 赛事资讯
	 */
	public Map<String, Object> getInfo(long id) {
		String sqlInfo = "select id, title, cover,create_date from activity_t_over_activities where id = " + id;
		return queryDao.querySingleMap(sqlInfo);
	}

	/**
	 * 根据ID查询
	 */
	public ActivityOverActivity findById(Long id) {
		return activityOverActivityDao.findOne(id);
	}

	/**
	 * 保存资讯
	 */
	public ActivityOverActivity saveOrUpdate(ActivityOverActivity item) {
		if (item != null) {
			Date now = new Date();
			item.setUpdateDate(now);
			if (item.getId() != null) {
				ActivityOverActivity old = findById(item.getId());
				if (old != null) {
					if (old.getReadNum() != null) {
						item.setReadNum(old.getReadNum());
					}
					item = BeanUtils.updateBean(old, item);
				}
			} else {
				item.setValid(CommonConstant.INT_BOOLEAN_TRUE);
				item.setCreateDate(now);
				if (item.getType() == 2) {
					// 新增专题记录,随机2000-3000阅读量
					item.setReadNum(RandomUtils.nextInt(10000, 25000));
				} else {
					// 新增记录,随机1000-2000阅读量
					item.setReadNum(RandomUtils.nextInt(5000, 20000));
				}
			}
			if (item.getReadNum() == null) {
				item.setReadNum(0);
			}
			if (item.getPraise() == null) {
				item.setPraise(0);
			}
			ActivityOverActivity info = activityOverActivityDao.save(item);
			removeRedisCache();
			return info;
		}
		return null;
	}

	private void removeRedisCache() {
		Joiner joiner = Joiner.on("_");
		String key = joiner.join(CacheKeyConstant.API_CACHE_INFOMATION_LIST, 1);
		objectRedisOperateService.setData(key, null);
	}

	/**
	 * 根据ID删除资讯 (is_valid置为0)
	 */
	public ActivityOverActivity deleteById(Long itemId) {
		ActivityOverActivity activityOverActivity = activityOverActivityDao.findOne(itemId);
		if (activityOverActivity != null) {
			activityOverActivity.setValid(CommonConstant.INT_BOOLEAN_FALSE);
			ActivityOverActivity info = activityOverActivityDao.save(activityOverActivity);
			removeRedisCache();
			return info;
		}
		return null;
	}

	public ActivityOverActivity delete(ActivityOverActivity info) {
		info.setValid(CommonConstant.INT_BOOLEAN_FALSE);
		return save(info);
	}

	/**
	 * 根据ID置顶或取消置顶
	 */
	public ActivityOverActivity topById(Long id, boolean top) {
		if (id == null) {
			return null;
		}

		ActivityOverActivity info = findById(id);
		if (info != null) {
			if (top) {
				info.setIsTop(CommonConstant.INT_BOOLEAN_TRUE);
				info.setTopDate(new Date());
			} else {
				info.setIsTop(CommonConstant.INT_BOOLEAN_FALSE);
				info.setTopDate(null);
			}
			info = save(info);

			// 置顶操作每个模块不超过4条,超过时取消最早置顶的数据(模块区分普通、专题、视频)
			if (!top) {
				return info;
			}
			String sql = SqlJoiner.join(
					"SELECT * FROM ( SELECT id, module_id, IF( @area = module_id ,@rank \\:=@rank + 1 ,@rank \\:= 1 ) AS rank, @area \\:= module_id FROM (",
					" SELECT i.*, mi.module_id FROM ( SELECT * FROM activity_over_activity_module_info mi WHERE mi.over_activity_id = ",
					info.getId().toString(), " ) mi", " JOIN activity_over_activity_module m ON mi.module_id = m.id",
					" JOIN activity_over_activity_module_info top_mi ON m.id = top_mi.module_id",
					" JOIN activity_t_over_activities i ON top_mi.over_activity_id = i.id AND i.is_valid = 1 AND i.is_top = 1",
					" ORDER BY module_id, i.top_date DESC",
					" ) DATA, (SELECT @area \\:= NULL, @rank \\:= 0) clearRank ) rank WHERE rank > 4");
			List<Map<String, Object>> cancelTopInfoes = queryDao.queryMap(sql);

			// 取消置顶超出部分的资讯
			List<Long> ids = Lists.newArrayList();
			if (CollectionUtils.isNotEmpty(cancelTopInfoes)) {
				for (Map<String, Object> cti : cancelTopInfoes) {
					Long i = MapUtils.getLong(cti, "id");
					ids.add(i);
				}
			}
			List<ActivityOverActivity> cancelInfoes = findByIdIn(ids);
			if (CollectionUtils.isNotEmpty(cancelInfoes)) {
				for (ActivityOverActivity ci : cancelInfoes) {
					ci.setIsTop(CommonConstant.INT_BOOLEAN_FALSE);
					ci.setTopDate(null);
				}
			}
			save(cancelInfoes);

			return info;
		}
		return null;
	}

	/**
	 * 根据ID设置为生效或不生效
	 */
	public ActivityOverActivity showById(Long id, boolean show) {
		if (id == null) {
			return null;
		}

		ActivityOverActivity info = findById(id);
		if (info == null) {
			return null;
		}

		Integer oldIsShow = info.getIsShow();
		boolean oldShow = CommonConstant.INT_BOOLEAN_TRUE.equals(oldIsShow);
		if (oldShow && show) {
			info.setTimerDate(new Date());
		} else if (oldShow && !show) {
			info.setIsShow(CommonConstant.INT_BOOLEAN_FALSE);
		} else if (!oldShow && show) {
			info.setIsShow(CommonConstant.INT_BOOLEAN_TRUE);
		}
		return saveOrUpdate(info);
	}

	/**
	 * 修改banner的生效状态
	 */
	public ActivityOverActivity bannerShowById(Long id, boolean show) {
		if (id == null) {
			return null;
		}

		ActivityOverActivity info = findById(id);
		if (info == null) {
			return null;
		}

		if (show) {// 生效操作
			Integer bannerValid = info.getBannerValid();
			if (CommonConstant.INT_BOOLEAN_TRUE.equals(bannerValid)) {// 原状态为待生效
				info.setBannerTimerDate(new Date());
			} else {// 原状态为失效
				info.setBannerTimerDate(new Date());
				info.setBannerValid(CommonConstant.INT_BOOLEAN_TRUE);
			}
		} else {// 失效操作
			info.setBannerValid(CommonConstant.INT_BOOLEAN_FALSE);
		}
		return saveOrUpdate(info);
	}

	/**
	 * 获取赛事资讯（专题）列表，分页
	 */
	public PageVO getInformationList(int page, int rows, Map<String, Object> params) {
		String sqlLimit = " limit :pageStart, :pageNum";

		String sqlCount = SqlJoiner.join("select count(1) from activity_t_over_activities a",
				" left join activity_t_info t on t.id = a.activity_id",
				" left join netbar_t_info n on n.id = a.netbar_id", " where a.is_valid = 1");
		String sqlServer = SqlJoiner.join(
				"select a.id, n.name netbarName, t.title activityTitle, a.title, a.icon, a.cover, a.brief, a.remark, a.begin_time beginTime, a.over_time overTime, a.pid, a.is_subject isSubject,a.create_date,  a.is_hot isHot, a.is_show isShow from activity_t_over_activities a",
				" left join activity_t_info t on t.id = a.activity_id",
				" left join netbar_t_info n on n.id = a.netbar_id", " where a.is_valid = 1");
		if (null != params.get("isSubject")) {
			sqlCount = SqlJoiner.join(sqlCount, " and is_subject = " + params.get("isSubject"));
			sqlServer = SqlJoiner.join(sqlServer, " and a.is_subject = :isSubject");
		} else if (null != params.get("title")) {
			sqlCount = SqlJoiner.join(sqlCount, " and title like '%" + params.get("title") + "%'");
			sqlServer = SqlJoiner.join(sqlServer, " and a.title like concat('%',:title,'%')");
		} else if (null != params.get("pid")) {
			sqlCount = SqlJoiner.join(sqlCount, " and pid = " + params.get("pid"));
			sqlServer = SqlJoiner.join(sqlServer, " and a.pid = :pid");
		} else if (null != params.get("areaCode")) {
			sqlCount = SqlJoiner.join(sqlCount,
					" and (isnull(a.activity_id) or left(t.area_code, 2) = left('" + params.get("areaCode") + "', 2))");
			sqlServer = SqlJoiner.join(sqlServer,
					" and (isnull(a.activity_id) or left(t.area_code, 2) = left(:areaCode, 2))");
		}
		sqlServer = SqlJoiner.join(sqlServer, sqlLimit);
		params.put("pageStart", (page - 1) * rows);
		params.put("pageNum", rows);

		List<Map<String, Object>> informationList = queryDao.queryMap(sqlServer, params);

		PageVO vo = new PageVO();
		vo.setList(informationList);
		// 分页
		Number total = (Number) queryDao.query(sqlCount);
		vo.setTotal(total.longValue());
		if (page * rows >= total.intValue()) {
			vo.setIsLast(1);
		}
		return vo;
	}

	@Autowired
	private UserFavorService userFavorService;

	/**
	 * 收藏赛事资讯(返回 0表示取消收藏成功,1表示收藏成功)
	 */
	public int fav(String infoId, long userId) {
		int result = -1;
		long infoIdLong = NumberUtils.toLong(infoId);
		UserFavor favor = userFavorService.findByUserIdAndSubIdAndTypeAndValid(userId, infoIdLong, 4, 1);
		if (favor != null) {
			if (favor.getValid() == 0) {
				favor.setValid(1);
				result = 1;
			} else {
				favor.setValid(0);
				result = 0;
			}
			userFavorService.save(favor);
		} else {
			favor = new UserFavor();
			favor.setCreateDate(new Date());
			favor.setSubId(infoIdLong);
			favor.setType(4);
			favor.setValid(1);
			favor.setUserId(userId);
			userFavorService.save(favor);
			result = 1;
		}
		return result;
	}

	/**
	 * 点赞操作
	 */
	public int praise(long infoId, long userId) {
		int result = -1;
		ActivityOverActivityPraise activityInfoPraise = activityOverActivityPraiseService
				.findByActivityOverActivityIdAndUserId(infoId, userId);
		if (activityInfoPraise != null) {
			Integer valid = activityInfoPraise.getValid();
			if (valid == 1) {
				activityInfoPraise.setValid(0);
				result = 0;
			} else {
				activityInfoPraise.setValid(1);
				result = 1;
			}
		} else {
			activityInfoPraise = new ActivityOverActivityPraise();
			activityInfoPraise.setActivityOverActivityId(infoId);
			activityInfoPraise.setUserId(userId);
			activityInfoPraise.setCreateDate(new Date());
			activityInfoPraise.setValid(1);
			result = 1;
		}
		activityOverActivityPraiseService.saveOrUpdate(activityInfoPraise);
		return result;
	}

	public List<Map<String, Object>> banner() {
		String sql = "select  id,type,title,IF ( banner_icon IS NULL, cover, banner_icon ) cover from activity_t_over_activities where is_show =1 and is_banner_show =1 and is_valid =1  order by  sort desc,create_date desc limit 8";
		return queryDao.queryMap(sql);
	}

	public PageVO favList(long userId, int page, int pageSize, int infoCount) {
		PageVO vo = new PageVO();
		Map<String, Object> params = Maps.newHashMap();
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		String sql = "select count(aa.id) total from activity_t_over_activities aa,user_r_favor  uf  where aa.id = uf.sub_id and uf.is_valid = 1 and aa.is_valid=1 and user_id= "
				+ userId;
		Map<String, Object> total = queryDao.querySingleMap(sql, params);
		Number totalCount = (Number) total.get("total");
		if (page * pageSize >= totalCount.intValue()) {
			vo.setIsLast(1);
		}
		int limitStart;
		if (infoCount == 0) {
			limitStart = (page - 1) * pageSize;
		} else {
			limitStart = (page - 1) * pageSize + totalCount.intValue() - infoCount;
		}

		if (totalCount.intValue() > 0) {
			sql = " select  aa.id, aa.type, aa.title, aa.icon, aa.read_num,aa.brief, group_concat(aai. img order by aai.create_date) as imgs  "
					+ " from activity_t_over_activities aa  left join activity_t_over_activities_img aai  on aa.id = aai.activity_id and aai.is_valid = 1"
					+ " where aa.id in(select sub_id from user_r_favor where type in (4,5,6) and user_id = :userId and is_valid =1)   and aa.is_valid = 1 group by aa.id   order by aa.create_date desc limit :start ,:pageSize";
			params.put("start", limitStart);
			params.put("pageSize", pageSize);
			params.put("userId", userId);
			result = queryDao.queryMap(sql, params);
			vo.setList(result);
		}
		return vo;
	}

	public PageVO subjectList(int page, int pageSize, long activityId, int infoCount) {
		ActivityOverActivity activity = activityOverActivityDao.findOne(activityId);
		PageVO vo = new PageVO();
		vo.setRemain(activity.getCover() == null ? activity.getIcon() : activity.getCover());
		vo.setTitle(activity.getTitle());
		Map<String, Object> params = Maps.newHashMap();
		String sql = "select count(id) total from activity_t_over_activities   where is_valid=1 and pid = "
				+ activityId;
		Map<String, Object> total = queryDao.querySingleMap(sql, params);
		Number totalCount = (Number) total.get("total");
		if (page * pageSize >= totalCount.intValue()) {
			vo.setIsLast(1);
		}
		int limitStart;
		if (infoCount == 0) {
			limitStart = (page - 1) * pageSize;
		} else {
			limitStart = (page - 1) * pageSize + totalCount.intValue() - infoCount;
		}
		if (totalCount.intValue() > 0) {
			sql = " select  aa.id, aa.type, aa.title, aa.icon, aa.read_num,aa.brief, group_concat(aai. img order by aai.create_date) as imgs  "
					+ " from activity_t_over_activities aa  left join activity_t_over_activities_img aai  on aa.id = aai.activity_id and aai.is_valid =1  "
					+ " where aa.pid = :activityId   and aa.is_valid = 1 and aa.is_show=1 group by aa.id   order by aa.is_top desc,aa.top_date desc,aa.timer_date desc limit :start ,:pageSize";
			params.put("start", limitStart);
			params.put("pageSize", pageSize);
			params.put("activityId", activityId);
			List<Map<String, Object>> result = queryDao.queryMap(sql, params);
			vo.setList(result);
		}
		return vo;

	}

	public PageVO list(int page, int pageSize, int infoCount) {
		PageVO vo = new PageVO();
		Map<String, Object> params = Maps.newHashMap();
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		String sql = "select count(id) total from activity_t_over_activities   where is_valid=1 and pid <=0 ";
		Map<String, Object> total = queryDao.querySingleMap(sql, params);
		Number totalCount = (Number) total.get("total");
		if (page * pageSize >= totalCount.intValue()) {
			vo.setIsLast(1);
		}
		vo.setTotal(totalCount.intValue());
		int limitStart;
		if (infoCount == 0) {
			limitStart = (page - 1) * pageSize;
		} else {
			limitStart = (page - 1) * pageSize + totalCount.intValue() - infoCount;
		}

		if (totalCount.intValue() > 0) {
			sql = " select  aa.id, aa.type, aa.title, aa.icon, aa.read_num,aa.brief, group_concat(aai.img order by aai.create_date) as imgs  "
					+ " from activity_t_over_activities aa  left join activity_t_over_activities_img aai  on aa.id = aai.activity_id and aai.is_valid=1 "
					+ " where  aa.is_valid = 1 and aa.pid <=0  and aa.type<=3 group by aa.id  order by aa.create_date desc limit :start ,:pageSize";
			params.put("start", limitStart);
			params.put("pageSize", pageSize);
			result = queryDao.queryMap(sql, params);
			vo.setList(result);
		}
		return vo;

	}

	/**资讯列表
	 * @param pager
	 * @param id
	 * @return
	 */
	public Map<String, Object> informationList(Pager pager, Long id, Long pid) {
		PageVO vo = new PageVO();
		String sql = "";
		StringBuilder moduleSql = new StringBuilder("");
		if (pid == 0) {
			sql = "select id from activity_over_activity_module where is_valid=1 and pid=" + id;
			List<Map<String, Object>> list = queryDao.queryMap(sql);
			if (CollectionUtils.isNotEmpty(list)) {
				for (Map<String, Object> map : list) {
					moduleSql.append(" or b.module_id=").append(map.get("id").toString());
				}
			}
		}
		sql = "SELECT count(1) FROM ( activity_t_over_activities aa, activity_over_activity_module_info b )  WHERE aa.is_valid = 1 and aa.is_show=1 and aa.timer_date<now() AND aa.pid <= 0 AND aa.id = b.over_activity_id AND (b.module_id = "
				+ id + moduleSql + ")";
		Number n = queryDao.query(sql);
		if (n != null && pager.total >= n.intValue()) {
			vo.setIsLast(1);
			vo.setTotal(n.intValue());
		}
		sql = "SELECT aa.id, aa.type, aa.title, if(aa.icon is null,aa.cover,aa.icon) icon, aa.read_num, ifnull(aa.brief,'') brief, group_concat(DISTINCT aai.img ORDER BY aai.img ) AS imgs,keyword,case when (UNIX_TIMESTAMP(now())-UNIX_TIMESTAMP(aa.timer_date))/3600<1 then '1小时以内' when  1<(UNIX_TIMESTAMP(now())-UNIX_TIMESTAMP(aa.timer_date))/3600 and (UNIX_TIMESTAMP(now())-UNIX_TIMESTAMP(aa.timer_date))/3600<24 then concat(ceil((UNIX_TIMESTAMP(now())-UNIX_TIMESTAMP(aa.timer_date))/3600),'小时以内') else timer_date end time FROM ( activity_t_over_activities aa, activity_over_activity_module_info b ) LEFT JOIN activity_t_over_activities_img aai ON aa.id = aai.activity_id AND aai.is_valid = 1 WHERE aa.is_valid = 1 and aa.is_show=1 and aa.timer_date<now() AND aa.pid <= 0 AND aa.id = b.over_activity_id AND (b.module_id ="
				+ id + moduleSql
				+ ") GROUP BY aa.id ORDER BY aa.is_top DESC, aa.top_date DESC, aa.timer_date DESC limit " + pager.start
				+ "," + pager.pageSize;
		vo.setList(queryDao.queryMap(sql));
		sql = "SELECT a.id, a.type, IF ( a.banner_title IS NULL, a.title, a.banner_title ) title, IF ( a.banner_icon IS NULL, a.cover, a.banner_icon ) cover FROM activity_t_over_activities a, activity_over_activity_module_info b WHERE a.id = b.over_activity_id AND b.module_id ="
				+ id
				+ " AND a.is_valid = 1 and a.is_show=1 and banner_valid=1 and a.banner_timer_date<now() AND a.pid <= 0 AND a.is_banner_show = 1  ORDER BY a.banner_sort LIMIT 8";
		Map<String, Object> result = new HashMap<String, Object>();
		if (pager.page == 1) {
			result.put("banner", queryDao.queryMap(sql));
		}
		result.put("information", vo);
		return result;
	}

	public Map<String, Object> detail(long infoId, long userId) {
		Map<String, Object> result = Maps.newHashMap();
		String sql = StringUtils.EMPTY;
		if (userId > 0) {
			sql = " select   aa.id, aa.title,aa.icon, aa.create_date, aa.remark, aa.read_num, aa.source,aa.type,aa.brief, count(distinct  uf.id) favNum, "
					+ " case   when count(distinct  uf1.id) > 0   then 1   else 0  end faved, count( distinct  aap.id) praiseNum, "
					+ "  case   when count( distinct  aap1.id) > 0  then 1   else 0 "
					+ "  end praised, group_concat(distinct  aai.img  order by aai.img) as imgs,group_concat(aai.remark order by aai.img SEPARATOR '|||') as introduces "
					+ " from activity_t_over_activities aa "
					+ "  left join activity_t_over_activities_img aai   on aai.activity_id = aa.id and aai.is_valid = 1"
					+ "  left join user_r_favor uf   on uf.type = 4  and uf.sub_id = aa.id  and uf.is_valid = 1 "
					+ "  left join user_r_favor uf1   on uf1.type = 4  and uf1.sub_id = aa.id   and uf1.user_id ="
					+ userId + "  and uf1.is_valid = 1 "
					+ "  left join activity_over_activity_praise aap  on aap.activity_over_activity_id = aa.id  and aap.is_valid = 1 "
					+ "  left join activity_over_activity_praise aap1  on aap1.user_id = " + userId
					+ "  and aap1.is_valid = 1 and aap1.activity_over_activity_id = aa.id where aa.id = " + infoId
					+ "  and aa.is_valid = 1  group by aa.id";
		} else {
			sql = " select  aa.id, aa.title,aa.icon, aa.create_date, aa.remark,aa.brief, aa.read_num, aa.source,aa.type, count(distinct uf.id) favNum,"
					+ "  0 faved, count(distinct aap.id) praiseNum,  0 praised, group_concat(distinct  aai.img  order by aai.img) as imgs,group_concat(aai.remark order by aai.img SEPARATOR '|||') as introduces "
					+ " from  activity_t_over_activities aa  left join activity_t_over_activities_img aai on aai.activity_id = aa.id and aai.is_valid = 1"
					+ "  left join user_r_favor uf   on uf.type = 4  and uf.sub_id = aa.id  and uf.is_valid = 1"
					+ "  left join activity_over_activity_praise aap  on aap.activity_over_activity_id = aa.id  and aap.is_valid = 1"
					+ "  and aa.is_valid = 1 where aa.id = " + infoId + "  and aa.is_valid = 1";
		}
		result = queryDao.querySingleMap(sql);

		return result;
	}

	public Map<String, Object> infoDetail(Long userId, Long id, boolean isShare, boolean isMp4) {
		Map<String, Object> result = Maps.newHashMap();
		if (userId == null) {
			userId = 0L;
		}
		String sql = "select a.id,pid from activity_over_activity_module a,activity_over_activity_module_info b where a.id=b.module_id and over_activity_id="
				+ id;
		Long categoryId = 0L;
		Long pid = 0L;
		Map<String, Object> module = queryDao.querySingleMap(sql);
		if (module != null) {
			categoryId = ((Number) module.get("id")).longValue();
			pid = ((Number) module.get("pid")).longValue();
		}
		String videoUrl = " concat('http://img.wangyuhudong.com/',video_url) video_url ";
		if (isMp4) {
			videoUrl = " concat('http://img.wangyuhudong.com/',replace(a.video_url,'m3u8','mp4')) video_url ";
		}
		sql = "SELECT a.video_cover_imgs,a.brief,a.keyword,a.icon,a.timer_date, a.id, a.read_num, a.remark, a.title, a.type, ifnull(b.favNum, 0) favNum, IF (c.id IS NULL, 0, 1) faved, group_concat(d.img ORDER BY d.img) AS imgs, group_concat( d.remark ORDER BY d.img SEPARATOR '|||' ) AS introduces, ifnull(e.praiseNum, 0) praiseNum, IF (f.id IS NULL, 0, 1) praised,a.source,"
				+ videoUrl
				+ ",g.comments_num FROM activity_t_over_activities a LEFT JOIN ( SELECT sub_id, count(1) favNum FROM user_r_favor WHERE is_valid = 1 AND type = 4 AND sub_id ="
				+ id
				+ " ) b ON a.id = b.sub_id LEFT JOIN user_r_favor c ON a.id = c.sub_id AND c.type = 4 AND c.is_valid = 1 AND c.user_id ="
				+ userId
				+ " LEFT JOIN activity_t_over_activities_img d ON a.id = d.activity_id AND d.is_valid = 1 LEFT JOIN ( SELECT activity_over_activity_id, count(1) praiseNum FROM activity_over_activity_praise WHERE is_valid = 1 AND activity_over_activity_id ="
				+ id
				+ " ) e ON a.id = e.activity_over_activity_id LEFT JOIN activity_over_activity_praise f ON a.id = f.activity_over_activity_id AND f.is_valid = 1 AND f.user_id ="
				+ userId
				+ " left join (select amuse_id,count(1) comments_num from amuse_r_activity_comment where is_valid=1 and type=3 and parent_id=0 and amuse_id="
				+ id + ")g on a.id=g.amuse_id WHERE a.id = " + id;
		Map<String, Object> info = queryDao.querySingleMap(sql);
		if (isShare) {
			//dealVideo(info);
		}
		result.put("info", info);
		result.put("upDown", upDown(userId, id));
		if (categoryId != null && pid != null) {
			StringBuilder moduleSql = new StringBuilder("");
			if (pid == 0) {
				sql = "select id from activity_over_activity_module where is_valid=1 and pid=" + categoryId;
				List<Map<String, Object>> list = queryDao.queryMap(sql);
				if (CollectionUtils.isNotEmpty(list)) {
					for (Map<String, Object> map : list) {
						moduleSql.append(" or b.module_id=").append(map.get("id").toString());
					}
				}
			}
			sql = "SELECT a.id, a.type, a.title, if(a.icon is null,a.cover,a.icon) icon, a.read_num, a.brief, group_concat(c.img ORDER BY c.img) AS imgs, keyword, CASE WHEN ( UNIX_TIMESTAMP(now()) - UNIX_TIMESTAMP(a.timer_date)) / 3600 < 1 THEN '1小时以内' WHEN 1 < ( UNIX_TIMESTAMP(now()) - UNIX_TIMESTAMP(a.timer_date)) / 3600 AND ( UNIX_TIMESTAMP(now()) - UNIX_TIMESTAMP(a.timer_date)) / 3600 < 24 THEN concat( ceil(( UNIX_TIMESTAMP(now()) - UNIX_TIMESTAMP(a.timer_date)) / 3600 ), '小时以内' ) ELSE timer_date END time FROM ( activity_t_over_activities a, activity_over_activity_module_info b ) LEFT JOIN activity_t_over_activities_img c ON a.id = c.activity_id AND c.is_valid = 1 WHERE a.id<>"
					+ id
					+ " and a.is_valid = 1 AND a.is_show = 1 AND a.timer_date > date_add(now(), INTERVAL - 2 DAY) AND a.timer_date < now() AND a.pid <= 0 AND a.id = b.over_activity_id AND ( b.module_id ="
					+ categoryId + moduleSql + " ) GROUP BY a.id ORDER BY read_num DESC, timer_date DESC LIMIT 10";
			List<Map<String, Object>> recommend = queryDao.queryMap(sql);
			List<Map<String, Object>> selectedRecommend = new ArrayList<Map<String, Object>>();
			List<Integer> selectedNum = new ArrayList<Integer>();
			if (CollectionUtils.isNotEmpty(recommend)) {
				if (recommend.size() > 3) {
					Random r = new Random();
					for (int i = 0; i < 3; i++) {
						int selected = r.nextInt(recommend.size());
						while (selectedNum.contains(selected)) {
							selected = r.nextInt(recommend.size());
							selectedNum.add(selected);
						}
						selectedRecommend.add(recommend.get(selected));
					}
					recommend = selectedRecommend;

				}
			}
			if (recommend.size() < 3) {
				sql = "SELECT a.id, a.type, a.title, if(a.icon is null,a.cover,a.icon) icon, a.read_num, a.brief, group_concat(c.img ORDER BY c.img) AS imgs, keyword, CASE WHEN ( UNIX_TIMESTAMP(now()) - UNIX_TIMESTAMP(a.timer_date)) / 3600 < 1 THEN '1小时以内' WHEN 1 < ( UNIX_TIMESTAMP(now()) - UNIX_TIMESTAMP(a.timer_date)) / 3600 AND ( UNIX_TIMESTAMP(now()) - UNIX_TIMESTAMP(a.timer_date)) / 3600 < 24 THEN concat( ceil(( UNIX_TIMESTAMP(now()) - UNIX_TIMESTAMP(a.timer_date)) / 3600 ), '小时以内' ) ELSE timer_date END time FROM ( activity_t_over_activities a, activity_over_activity_module_info b ) LEFT JOIN activity_t_over_activities_img c ON a.id = c.activity_id AND c.is_valid = 1 WHERE a.id<>"
						+ id
						+ " and a.is_valid = 1 AND a.is_show = 1 AND a.timer_date < now() AND a.pid <= 0 AND a.id = b.over_activity_id AND ( b.module_id ="
						+ categoryId + moduleSql + " ) GROUP BY a.id ORDER BY timer_date DESC LIMIT 3";
				recommend.addAll(queryDao.queryMap(sql));
			}
			Set<Map<String, Object>> set = new HashSet<Map<String, Object>>(recommend);
			int i = 0;
			if (set.size() > 3) {
				recommend.clear();
				for (Map<String, Object> map : set) {
					if (i > 3) {
						break;
					}
					recommend.add(map);
					i++;
				}
				result.put("recommend", recommend);
			} else {
				result.put("recommend", set);
			}
		}
		return result;
	}

	public void dealVideo(Map<String, Object> info) {
		if (info != null) {
			String remark = (String) info.get("remark");
			if (remark != null) {
				Document doc = new Document("");
				doc.html(remark);
				Elements elements = doc.getElementsByTag("embed");
				for (Element element : elements) {
					Element parent = element.parent();
					parent.append("<iframe  src=\"" + dealVideoSrc(element.attr("src")) + "\"/>");
					element.remove();
					info.put("remark", doc.toString());
				}
			}
		}
	}

	public String dealVideoSrc(String src) {
		if (src.contains("youku.com")) {
			src = src.replace("player.php/sid", "embed").replace("/v.swf", "");
		} else if (src.contains("qq.com")) {
			src = "http://v.qq.com/iframe/player.html" + src.substring(src.indexOf("?"), src.length());
		}
		return src;
	}

	public Map<String, Object> upDown(Long userId, Long id) {
		Map<String, Object> upDown = new HashMap<String, Object>();
		String state = stringRedisOperateService.getData(InformationConstant.USER_KEY + id + "_" + userId);
		Integer upTotalInt = 0;
		String upTotal = stringRedisOperateService.getData(InformationConstant.UP_TOTAL_KEY + id);
		Integer downTotalInt = 0;
		String downTotal = stringRedisOperateService.getData(InformationConstant.DOWN_TOTAL_KEY + id);
		if (upTotal != null) {
			upTotalInt = NumberUtils.toInt(upTotal);
		}
		if (downTotal != null) {
			downTotalInt = NumberUtils.toInt(downTotal);
		}
		upDown.put("state", state == null ? 0 : state);
		upDown.put("upTotal", upTotalInt);
		upDown.put("downTotal", downTotalInt);
		if (upTotalInt == 0 && downTotalInt == 0) {
			upDown.put("upPercent", 0);
			upDown.put("downPercent", 0);
		} else {
			int tmp = (int) Math.round(upTotalInt * 100d / (upTotalInt + downTotalInt));
			upDown.put("upPercent", tmp);
			upDown.put("downPercent", 100 - tmp);
		}
		return upDown;
	}

	/**
	 * 查询banner数量,作为默认排序序号
	 */
	public int defaultOrderNum() {
		String sortNumSql = "SELECT COUNT(1) FROM activity_t_over_activities WHERE is_banner_show = 1 and is_valid=1";
		Number sortNum = queryDao.query(sortNumSql);
		if (sortNum == null) {
			sortNum = 0;
		}
		return sortNum.intValue();
	}

	/**
	 * 根据mouldeID查询activityID
	 */
	public int queryActivityID(Long moulid) {
		String sortNumSql = "SELECT over_activity_id FROM activity_over_activity_module_info WHERE module_id= "
				+ moulid;
		Number sortNum = queryDao.query(sortNumSql);
		return sortNum.intValue();
	}

	public PageVO queryBanner(Long moduleId, Integer banner, String dateStart, String dateEnd, int page) {
		PageVO vo = new PageVO();
		String sql = "";
		String typeSql = "";
		String bannerSql = "";
		String dateStartSql = "";
		String dateEndSql = "";

		if (moduleId != null) {
			typeSql = " and (c.id=" + moduleId + " or  d.id=" + moduleId + ")";
		}
		if (banner != null && banner == 0) {
			bannerSql = " and a.banner_valid=1 and a.banner_timer_date<=now()";
		}
		if (banner != null && banner == 1) {
			bannerSql = " and a.banner_valid=1 and (a.banner_timer_date>now() or a.banner_timer_date is null)";
		}
		if (banner != null && banner == 2) {
			bannerSql = " and a.banner_valid=0";
		}

		if (StringUtils.isNotBlank(dateStart)) {
			dateStartSql = " and a.banner_timer_date>'" + dateStart + "'";
		}
		if (StringUtils.isNotBlank(dateEnd)) {
			dateEndSql = " and a.banner_timer_date<=ADDDATE('" + dateEnd + "', INTERVAL 1 DAY)";
		}

		sql = SqlJoiner.join(
				"select count(1) from (select 1 from activity_t_over_activities a left join activity_over_activity_module_info b on a.id=b.over_activity_id left join activity_over_activity_module c on b.module_id = c.id and c.pid!=0 left join activity_over_activity_module d on ((c.pid = d.id and d.pid=0) or (b.module_id = d.id and d.pid=0)) where a.is_valid=1 and a.is_banner_show=1",
				typeSql, bannerSql, dateStartSql, dateEndSql, " group by a.id) t");
		Number total = queryDao.query(sql);
		if (total != null) {
			vo.setTotal(total.longValue());
		}
		int pageSize = PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		int start = (page - 1) * pageSize;
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("start", start);
		params.put("pageSize", pageSize);
		sql = SqlJoiner.join(
				"select a.id bannerID,a.banner_sort,a.create_date,a.banner_valid,a.cover,a.title,a.banner_create_date,",
				"a.type,a.is_banner_show,a.sort,group_concat(distinct c.name) sname,group_concat(distinct d.name) fname,",
				"a.banner_timer_date, a.banner_icon,a.banner_title,c.order_num,c.id",
				" from activity_t_over_activities a",
				" left join activity_over_activity_module_info b on a.id=b.over_activity_id",
				" left join activity_over_activity_module c on b.module_id = c.id and c.pid!=0 and c.is_valid=1",
				" left join activity_over_activity_module d on ((c.pid = d.id and d.pid=0 and d.is_valid=1) or (b.module_id = d.id and d.pid=0 and d.is_valid=1))",
				" where a.is_valid=1 and a.is_banner_show=1", typeSql, bannerSql, dateStartSql, dateEndSql,
				" group by a.id order by a.banner_sort limit :start,:pageSize");
		vo.setList(queryDao.queryMap(sql, params));
		return vo;
	}

	/**
	 * 查询未设置为banner的资讯(排除当前编辑的banner)
	 */
	public List<Map<String, Object>> queryBannerInfoByTypeAndModuleId(Integer type, Long moduleId, Long infoId) {
		if (type == null && moduleId == null) {
			return null;
		}

		String condition = " where a.is_valid = 1";
		if (infoId != null) {
			condition = SqlJoiner.join(condition, " and (a.is_banner_show is null or a.is_banner_show != 1 or a.id = ",
					infoId.toString(), ")");
		} else {
			condition = SqlJoiner.join(condition, " and (a.is_banner_show is null or a.is_banner_show != 1)");
		}
		if (type != null) {
			condition = SqlJoiner.join(condition, " and a.type = ", type.toString());
		}
		if (moduleId != null) {
			condition = SqlJoiner.join(condition, " and c.id = ", moduleId.toString());
		}

		String sql = SqlJoiner.join("select a.id,a.title from activity_t_over_activities a",
				" left join activity_over_activity_module_info b on a.id=b.over_activity_id",
				" left join activity_over_activity_module c on b.module_id=c.id", condition, " group by a.id");
		return queryDao.queryMap(sql);
	}

	/*通过咨询id查找模块id*/
	public List<Map<String, Object>> queryMouleId(Long id) {
		String sql = "select c.id  from activity_t_over_activities a left join activity_over_activity_module_info b on a.id=b.over_activity_id left join activity_over_activity_module c on b.module_id=c.id where a.id="
				+ id + " limit 1";
		return queryDao.queryMap(sql);
	}

	/*查询模块信息*/
	public List<Map<String, Object>> queryInfoByModule() {
		String sql = "select a.id,a.name from activity_over_activity_module a where a.is_valid=1 and a.type!=2";
		return queryDao.queryMap(sql);
	}

	public List<Map<String, Object>> queryInfoByType(Integer type) {
		String sql = "select a.id,a.title from activity_t_over_activities a where a.is_valid=1 and pid=0 and type="
				+ type;
		return queryDao.queryMap(sql);
	}

	public ActivityOverActivity save(ActivityOverActivity activityOverActivity) {
		activityOverActivity = activityOverActivityDao.save(activityOverActivity);
		removeRedisCache();
		return activityOverActivity;
	}

	public ActivityOverActivity autoSave(ActivityOverActivity overActivity) {
		if (overActivity != null) {
			Date now = new Date();
			overActivity.setUpdateDate(now);
			if (overActivity.getId() != null) {
				ActivityOverActivity oldOverActivity = findById(overActivity.getId());
				if (oldOverActivity.getReadNum() != null) {
					overActivity.setReadNum(oldOverActivity.getReadNum());
				}
				overActivity = BeanUtils.updateBean(oldOverActivity, overActivity);
			} else {
				overActivity.setCreateDate(now);
				overActivity.setValid(CommonConstant.INT_BOOLEAN_TRUE);
				if (overActivity.getType() == 2) {
					// 新增专题记录,随机2000-3000阅读量
					overActivity.setReadNum(RandomUtils.nextInt(10000, 25000));
				} else {
					// 新增记录,随机1000-2000阅读量
					overActivity.setReadNum(RandomUtils.nextInt(5000, 20000));
				}
			}
			return activityOverActivityDao.save(overActivity);
		}
		return null;
	}

	/**普通资讯
	 * @param title
	 * @param type
	 * @param date
	 * @param page
	 * @return
	 */
	public PageVO queryCommonInfo(String title, Integer type, String date, int page, Integer status) {
		PageVO vo = new PageVO();
		String sql = "";
		String typeSql = "";
		String titleSql = "";
		String dateSql = "";
		String statusSql = "";
		if (type != null) {
			typeSql = " and a.type=" + type;
		}
		if (status != null && status == 2) {
			statusSql = " and a.is_valid=0 and a.timer_date is not null ";
		} else {
			statusSql = " and a.is_valid=1 ";
		}
		if (StringUtils.isNotBlank(title)) {
			titleSql = " and a.title like '" + title + "%'";
		}
		if (StringUtils.isNotBlank(date)) {
			dateSql = " and date_format(a.create_date, '%Y-%m-%d')='" + date + "'";
		}
		sql = SqlJoiner.join(
				"select count(1) from activity_t_over_activities a left JOIN (select activity_id,img from activity_t_over_activities_img group by activity_id)b on a.id=b.activity_id where  a.type!=2 and pid=0",
				typeSql, titleSql, dateSql, statusSql);
		Number total = queryDao.query(sql);
		if (total != null) {
			vo.setTotal(total.longValue());
		}
		int pageSize = PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		int start = (page - 1) * pageSize;
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("start", start);
		params.put("pageSize", pageSize);
		sql = SqlJoiner.join(
				"select a.id,a.create_date,a.icon,a.title,a.type,a.read_num from activity_t_over_activities a left JOIN (select activity_id,img from activity_t_over_activities_img oai where oai.is_valid = 1 group by activity_id)b on a.id=b.activity_id where  a.type!=2 and pid=0",
				typeSql, titleSql, dateSql, statusSql, " order by a.create_date desc limit :start,:pageSize");
		System.out.println(sql);
		vo.setList(queryDao.queryMap(sql, params));
		return vo;
	}

	public void saveImg(ActivityOverActivityImg img) {
		activityOverActivityImgDao.save(img);
	}

	public void delImg(long activityId) {
		String sql = "update activity_t_over_activities_img set is_valid=0 where activity_id=" + activityId;
		queryDao.update(sql);
	}

	public List<ActivityOverActivityImg> findByActivityIdAndValid(long id, int valid) {
		return activityOverActivityImgDao.findByActivityIdAndValidOrderByCreateDateDesc(id, valid);
	}

	/**
	 * 专题资讯分页
	 */
	public PageVO subjectPage(int page, Long pid, Map<String, Object> params) {
		String sqlCondition = " WHERE aoa.is_valid = 1";
		String totalCondition = " WHERE aoa.is_valid = 1";

		if (pid == null) {
			pid = -1L;
		}
		sqlCondition = SqlJoiner.join(sqlCondition, " and aoa.pid = ", pid.toString());
		totalCondition = SqlJoiner.join(totalCondition, " and aoa.pid = ", pid.toString());

		if (null != params.get("title")) {
			sqlCondition = SqlJoiner.join(sqlCondition, " and aoa.title like concat('%', :title, '%')");
			totalCondition = SqlJoiner.join(totalCondition, " and aoa.title like '%",
					MapUtils.getString(params, "title"), "%'");
		}
		if (null != params.get("beginDate")) {
			sqlCondition = SqlJoiner.join(sqlCondition, " and aoa.create_date >= :beginDate");
			totalCondition = SqlJoiner.join(totalCondition, " and aoa.create_date >= '",
					MapUtils.getString(params, "beginDate"), "'");
		}
		if (null != params.get("endDate")) {
			sqlCondition = SqlJoiner.join(sqlCondition, " and aoa.create_date < adddate(:endDate, interval 1 day)");
			totalCondition = SqlJoiner.join(totalCondition, " and aoa.create_date < adddate('",
					MapUtils.getString(params, "endDate"), "', interval 1 day");
		}

		String limit = "";
		Integer pageSize = PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		if (page > 0) {
			Integer startRow = (page - 1) * pageSize;
			limit = SqlJoiner.join(" LIMIT ", startRow.toString(), ", ", pageSize.toString());
		}

		String sql = SqlJoiner.join(
				"select aoa.id, aoa.icon icon, aoa.title, aoa.create_date createDate, aoa.read_num readNum",
				" from activity_t_over_activities aoa left join activity_t_over_activities_img aoai on aoa.id = aoai.activity_id and aoai.is_valid = 1",
				sqlCondition, " GROUP BY aoa.id ORDER BY aoa.create_date DESC", limit);
		List<Map<String, Object>> list = queryDao.queryMap(sql, params);

		String totalSql = SqlJoiner.join("select COUNT(1) from activity_t_over_activities aoa", totalCondition);
		Number total = queryDao.query(totalSql);
		if (total == null) {
			total = 0;
		}

		PageVO vo = new PageVO();
		vo.setList(list);
		vo.setTotal(total.intValue());
		int isLast = total.intValue() > page * pageSize ? 1 : 0;
		vo.setIsLast(isLast);
		return vo;
	}

	/**专题
	 * @param title
	 * @param type
	 * @param date
	 * @param page
	 * @return
	 */
	public PageVO querySubjectInfo(String title, String date, int page, Integer status) {
		PageVO vo = new PageVO();
		String sql = "";
		String titleSql = "";
		String statusSql = "";
		String dateSql = "";
		if (StringUtils.isNotBlank(title)) {
			titleSql = " and a.title like '" + title + "%'";
		}
		if (status != null && status == 2) {
			statusSql = " and a.is_valid=1 and a.timer_date is not null ";
		} else {
			statusSql = " and a.is_valid=1 ";
		}
		if (StringUtils.isNotBlank(date)) {
			dateSql = " and date_format(a.create_date, '%Y-%m-%d')='" + date + "'";
		}
		sql = SqlJoiner.join(
				"SELECT count(1) FROM activity_t_over_activities a  WHERE a.is_valid = 1 AND a.type = 2 AND a.pid = 0 ",
				titleSql, dateSql, statusSql);
		Number total = queryDao.query(sql);
		if (total != null) {
			vo.setTotal(total.longValue());
		}
		int pageSize = PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		int start = (page - 1) * pageSize;
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("start", start);
		params.put("pageSize", pageSize);
		sql = SqlJoiner.join(
				"SELECT a.id, a.create_date, b.img, a.title, count(c.id) num,a.icon FROM activity_t_over_activities a LEFT JOIN activity_t_over_activities_img b ON a.id = b.activity_id LEFT JOIN activity_t_over_activities c ON a.id = c.pid WHERE  a.type = 2 AND a.pid = 0 ",
				titleSql, dateSql, statusSql, " GROUP BY a.id order by a.create_date desc limit :start,:pageSize");
		vo.setList(queryDao.queryMap(sql, params));
		return vo;
	}

	/**
	 * 查找定时发布的资讯信息
	 */
	public List<ActivityOverActivity> findByTimerDateAndValid(Date now, int valid) {
		return activityOverActivityDao.findByTimerDateAndValid(now, valid);
	}

	public void save(List<ActivityOverActivity> activities) {
		activityOverActivityDao.save(activities);
	}

	public List<ActivityOverActivityModuleVO> queryCategory() {
		List<Map<String, Object>> list = queryDao.queryMap(
				"SELECT a.id, a.pid, a.type, a.name,a.img FROM activity_over_activity_module a LEFT JOIN activity_over_activity_module b ON a.pid = b.id WHERE a.is_valid = 1 ORDER BY CASE WHEN a.pid = 0 THEN a.order_num WHEN a.pid > 0 AND a.pid = b.id THEN b.order_num ELSE a.pid END, a.pid ASC, a.order_num");
		List<ActivityOverActivityModuleVO> result = new ArrayList<ActivityOverActivityModuleVO>();
		ActivityOverActivityModuleVO vo = null;
		for (Map<String, Object> obj : list) {
			if (((Number) obj.get("pid")).longValue() == 0) {
				if (vo != null) {
					result.add(vo);
					vo = new ActivityOverActivityModuleVO();
					vo.setParent(obj);
				} else {
					vo = new ActivityOverActivityModuleVO();
					vo.setParent(obj);
				}
			} else {
				vo.getSub().add(obj);
			}
		}
		result.add(vo);
		return result;
	}

	/**
	 * 查询后台资讯列表
	 */
	public PageVO adminPage(int page, Map<String, String> searchMap) {
		String expandColumn = "";
		String expandJoin = "";
		String condition = " WHERE i.is_valid = 1";
		String totalCondition = condition;
		String joinCondition = " WHERE i.is_valid = 1";
		String joinTotalCondition = joinCondition;
		Map<String, Object> params = Maps.newHashMap();

		String listType = MapUtils.getString(searchMap, "listType");// 1 普通资讯列表, 2 专题资讯列表, 3 专题内资讯列表, 4 视频列表, 5 banner列表
		if ("1".equals(listType)) {
			expandColumn = ", if(count(ml.id) > 0, 1, 0) isModel";
			expandJoin = " LEFT JOIN activity_over_activity_model ml ON i.id = ml.info_id AND ml.is_valid = 1";
			condition = SqlJoiner.join(condition, " AND i.is_subject = 0 AND i.pid = 0 AND type != ",
					InformationConstant.INFO_TYPE_VIDEO.toString());
			totalCondition = SqlJoiner.join(totalCondition, " AND i.is_subject = 0 AND i.pid = 0 AND type != ",
					InformationConstant.INFO_TYPE_VIDEO.toString());
		} else if ("2".equals(listType)) {
			expandColumn = ", count(DISTINCT ci.id) childrenCount";
			expandJoin = " LEFT JOIN activity_t_over_activities ci ON i.is_subject = 1 AND ci.pid = i.id";
			condition = SqlJoiner.join(condition, " AND i.is_subject = 1 AND i.pid = 0");
			totalCondition = SqlJoiner.join(totalCondition, " AND i.is_subject = 1 AND i.pid = 0");
		} else if ("4".equals(listType)) {
			condition = SqlJoiner.join(condition, " AND i.type = ", InformationConstant.INFO_TYPE_VIDEO.toString());
			totalCondition = SqlJoiner.join(totalCondition, " AND i.type = ",
					InformationConstant.INFO_TYPE_VIDEO.toString());
		}
		String id = MapUtils.getString(searchMap, "id");
		if (NumberUtils.isNumber(id)) {
			condition = SqlJoiner.join(condition, " AND i.id = ", id);
			totalCondition = SqlJoiner.join(totalCondition, " AND i.id = ", id);
		}
		String pid = MapUtils.getString(searchMap, "pid");
		if (NumberUtils.isNumber(pid)) {
			joinCondition = SqlJoiner.join(joinCondition, " AND i.pid = ", pid);
			totalCondition = SqlJoiner.join(totalCondition, " AND i.pid = ", pid);
		}
		String moduleId = MapUtils.getString(searchMap, "moduleId");
		if (NumberUtils.isNumber(moduleId)) {
			joinCondition = SqlJoiner.join(joinCondition, " AND (m.id = ", moduleId, " OR m.pid = ", moduleId, ")");
			joinTotalCondition = SqlJoiner.join(joinTotalCondition, " AND (m.id = ", moduleId, " OR m.pid = ", moduleId,
					")");
		}
		String title = MapUtils.getString(searchMap, "title");
		if (StringUtils.isNotBlank(title)) {
			String likeTitle = "%" + title + "%";
			condition = SqlJoiner.join(condition, " AND i.title LIKE :title");
			totalCondition = SqlJoiner.join(totalCondition, " AND i.title LIKE '", likeTitle, "'");
			params.put("title", likeTitle);
		}
		String type = MapUtils.getString(searchMap, "type");
		if (NumberUtils.isNumber(type)) {
			condition = SqlJoiner.join(condition, " AND i.type = ", type);
			totalCondition = SqlJoiner.join(totalCondition, " AND i.type = ", type);
		}
		String efficient = MapUtils.getString(searchMap, "efficient");
		if ("1".equals(efficient)) {// 已生效
			condition = SqlJoiner.join(condition, " AND i.is_show = 1 AND NOW() >= i.timer_date");
			totalCondition = SqlJoiner.join(totalCondition, " AND i.is_show = 1 AND NOW() >= i.timer_date");
		} else if ("2".equals(efficient)) {// 待生效
			condition = SqlJoiner.join(condition, " AND i.is_show = 1 AND NOW() < i.timer_date");
			totalCondition = SqlJoiner.join(totalCondition, " AND i.is_show = 1 AND NOW() < i.timer_date");
		} else if ("3".equals(efficient)) {// 失效
			condition = SqlJoiner.join(condition, " AND i.is_show != 1");
			totalCondition = SqlJoiner.join(totalCondition, " AND i.is_show != 1");
		}
		String isPublished = MapUtils.getString(searchMap, "isPublished");
		if ("1".equals(isPublished)) {// 已发布
			condition = SqlJoiner.join(condition, " AND i.is_published = 1");
			totalCondition = SqlJoiner.join(totalCondition, " AND i.is_published = 1");
		} else if ("2".equals(isPublished)) {// 草稿箱
			condition = SqlJoiner.join(condition, " AND i.is_published = 0");
			totalCondition = SqlJoiner.join(totalCondition, " AND i.is_published = 0");
		}

		String beginDate = MapUtils.getString(searchMap, "beginDate");
		if (StringUtils.isNotBlank(beginDate)) {
			condition = SqlJoiner.join(condition, " AND i.create_date > :beginDate");
			totalCondition = SqlJoiner.join(totalCondition, " AND i.create_date > '", beginDate, "'");
			params.put("beginDate", beginDate);
		}
		String endDate = MapUtils.getString(searchMap, "endDate");
		if (StringUtils.isNotBlank(endDate)) {
			condition = SqlJoiner.join(condition, " AND i.create_date <= ADDDATE(:endDate, INTERVAL 1 DAY)");
			totalCondition = SqlJoiner.join(totalCondition, " AND i.create_date <= ADDDATE('", endDate,
					"', INTERVAL 1 DAY)");
			params.put("endDate", endDate);
		}
		String timerBeginDate = MapUtils.getString(searchMap, "timerBeginDate");
		if (StringUtils.isNotBlank(timerBeginDate)) {
			condition = SqlJoiner.join(condition, " AND i.timer_date > :timerBeginDate");
			totalCondition = SqlJoiner.join(totalCondition, " AND i.timer_date > '", timerBeginDate, "'");
			params.put("timerBeginDate", timerBeginDate);
		}
		String timerEndDate = MapUtils.getString(searchMap, "timerEndDate");
		if (StringUtils.isNotBlank(timerEndDate)) {
			condition = SqlJoiner.join(condition, " AND i.timer_date <=  ADDDATE(:timerEndDate, INTERVAL 1 DAY)");
			totalCondition = SqlJoiner.join(totalCondition, " AND i.timer_date <= ADDDATE('", timerEndDate,
					"', INTERVAL 1 DAY)");
			params.put("timerEndDate", timerEndDate);
		}

		String totalSql = SqlJoiner.join("SELECT COUNT(1) FROM ( SELECT i.id",
				" FROM ( SELECT * FROM activity_t_over_activities i ", totalCondition, " ) i",
				" LEFT JOIN activity_over_activity_module_info mi ON mi.over_activity_id = i.id",
				" LEFT JOIN activity_over_activity_module m ON mi.module_id = m.id", joinTotalCondition,
				" GROUP BY i.id) t");
		Number total = queryDao.query(totalSql);

		List<Map<String, Object>> list = null;
		if (total != null && total.longValue() > 0) {
			String limit = PageUtils.getLimitSql(page);

			String sql = SqlJoiner.join(
					"SELECT i.id, i.icon, i.cover, i.title, i.type, i.is_top isTop, i.create_date createDate, i.creater,",
					" i.is_show isShow, i.timer_date timerDate, i.read_num readNum,",
					" IF(i.is_published = 1, 1 ,IF(i.is_published = 0, 2 ,0)) isPublished,",
					" GROUP_CONCAT(DISTINCT m.name) modules,",
					" IF(i.is_show = 1 AND NOW() >= i.timer_date, 1, if(i.is_show = 1 AND NOW() < i.timer_date, 2, 3)) efficient",
					expandColumn, " FROM ( SELECT * FROM activity_t_over_activities i ", condition, " ) i",
					" LEFT JOIN activity_over_activity_module_info mi ON mi.over_activity_id = i.id",
					" LEFT JOIN activity_over_activity_module m ON mi.module_id = m.id", expandJoin, joinCondition,
					" GROUP BY i.id ORDER BY i.is_top DESC, i.create_date DESC", limit);
			list = queryDao.queryMap(sql, params);
		}
		return new PageVO(page, list, total);
	}

	/**
	 * 查询资讯所属中，超过8个banner的模块
	 */
	public List<Map<String, Object>> queryTooMuchBannerModulesByInfoId(Long infoId, Integer maxCount) {
		if (infoId == null) {
			return null;
		}
		if (maxCount == null) {
			maxCount = 8;
		}

		String sql = SqlJoiner.join("SELECT m.id, m.name, count(i.id) bannerCount FROM ( ",
				"SELECT * FROM activity_over_activity_module_info mi WHERE mi.over_activity_id = ", infoId.toString(),
				" ) mi JOIN activity_over_activity_module m ON mi.module_id = m.id",
				" LEFT JOIN activity_over_activity_module_info bmi ON m.id = bmi.module_id",
				" LEFT JOIN activity_t_over_activities i ON bmi.over_activity_id = i.id AND i.is_banner_show = 1",
				" GROUP BY m.id HAVING bannerCount >= ", maxCount.toString());
		return queryDao.queryMap(sql);
	}

	/**
	 * 更改资讯的排序序号
	 */
	public void changeOrderNum(long bannerIDLong, int orderNumInt) {
		ActivityOverActivity info = findById(bannerIDLong);
		if (info == null) {
			return;
		}
		// 更新自身排序序号
		info.setOrderNum(orderNumInt);
		info.setId(bannerIDLong);
		info.setUpdateDate(new Date());
		save(info);
	}

	/**
	 * 获取专题的子资讯
	 */
	public List<Map<String, Object>> queryValidByPid(Long pid) {
		String sql = SqlJoiner.join("SELECT id FROM activity_t_over_activities i WHERE i.pid = ", pid.toString(),
				" AND i.is_valid = 1");
		return queryDao.queryMap(sql);
	}

	/**
	 * 后台管理系统 推送模块获取二级模块下的搜索资讯,如果二级模块不存在,则查询全部资讯
	 */
	public List<Map<String, Object>> findValidByTitleAndModule(String title, Long subModuleId) {
		String sql = "select id,title from activity_t_over_activities where is_valid =1 and is_show=1   and title like '%"
				+ title + "%' ";
		if (null != subModuleId && subModuleId.longValue() > 0) {
			sql = sql + " and id in (select over_activity_id from  activity_over_activity_module_info where module_id="
					+ subModuleId + ") ";
		}

		return queryDao.queryMap(sql);
	}

	public List<Map<String, Object>> queryInfoForAppRecommend() {
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		String sql = "select id,title from activity_t_over_activities a where a.is_valid=1 and type = 1 order by a.create_date desc";
		result.addAll(queryDao.queryMap(sql));
		return result;
	}

	public List<Map<String, Object>> queryInfoForAppRecommend(String startDate, String endDate) {
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		String sql = "select id,title from activity_t_over_activities a where a.is_valid=1 and type = 1 and create_date>'"
				+ startDate + "' and create_date <'" + endDate + "' order by a.create_date desc";
		result.addAll(queryDao.queryMap(sql));
		return result;
	}

}
