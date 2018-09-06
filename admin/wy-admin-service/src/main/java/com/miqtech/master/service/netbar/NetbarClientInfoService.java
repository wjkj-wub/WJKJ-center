package com.miqtech.master.service.netbar;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.netbar.NetbarClientInfoDao;
import com.miqtech.master.entity.netbar.NetbarClientInfo;
import com.miqtech.master.service.base.BaseService;
import com.miqtech.master.service.common.StringRedisOperateService;
import com.miqtech.master.vo.PageVO;

@Component
public class NetbarClientInfoService extends BaseService {
	private static Logger logger = org.slf4j.LoggerFactory.getLogger(NetbarClientInfoService.class);

	public final static String PC_STAFF_ONLINE_ALL = "pc_staff_online_all";
	public final static String PC_STAFF_ONLINE_PROVINCE = "pc_staff_online_province_";
	public final static String PC_STAFF_ONLINE_CITY = "pc_staff_online_city_";
	public final static String PC_STAFF_ONLINE_AREA = "pc_staff_online_area_";

	public final static String PC_STAFF_ONLINE_CHECK = "pc_staff_online_check_";

	public final static String PC_STAFF_CLIENT_ONLINE_ALL = "pc_staff_client_online_all_";
	public final static String PC_STAFF_CLIENT_ONLINE_PROVINCE = "pc_staff_client_online_province_";
	public final static String PC_STAFF_CLIENT_ONLINE_CITY = "pc_staff_client_online_city_";
	public final static String PC_STAFF_CLIENT_ONLINE_AREA = "pc_staff_client_online_area_";

	public final static String PC_STAFF_CLIENT_ONLINE_NETBAR = "pc_staff_client_online_netbar_";

	//
	public final static String PC_STAFF_CLIENT_NETBAR = "pc_staff_client_netbar_";
	@Autowired
	private StringRedisOperateService stringRedisOperateService;

	@Autowired
	private NetbarClientInfoDao netbarClientInfoDao;

	private final static ExecutorService dbTread = Executors
			.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);

	/**
	 * 实时计数
	 * @param netbarId 网吧id
	 * @param areaCode 网吧地区
	 * @param onlineCount 在线数量
	 */
	public void realtimeCounter(Long netbarId, String areaCode, int totalCount, int onlineCount, String ip) {
		logger.error(netbarId + "-" + areaCode + "-" + totalCount + "-" + onlineCount + "-" + ip);
		String[] splitArea = splitArea(areaCode);

		if (!stringRedisOperateService.isSetMember(PC_STAFF_ONLINE_ALL, netbarId.toString())) {
			dbTread.execute(new Runnable() {
				@Override
				public void run() {
					NetbarClientInfo clientInfo = netbarClientInfoDao.findByNetbarId(netbarId);
					if (null == clientInfo) {
						clientInfo = new NetbarClientInfo();
						clientInfo.setCreateDate(new Date());
					}
					clientInfo.setIp(ip);
					clientInfo.setNetbarId(netbarId);
					clientInfo.setOnlineCount(onlineCount);
					clientInfo.setStatus(1);
					clientInfo.setTotalCount(totalCount);
					save(clientInfo);
				}
			});
		} else {
			updateCount(netbarId, onlineCount, totalCount);
		}

		stringRedisOperateService.setData(PC_STAFF_ONLINE_CHECK + netbarId.toString(), "1", 10, TimeUnit.MINUTES);//设置key10分钟过期

		/*统计收银端信息数据*/
		{
			//添加全国统计在线收银端
			stringRedisOperateService.addValuesToSet(PC_STAFF_ONLINE_ALL, netbarId.toString());
			//添加省份统计在线收银端
			stringRedisOperateService.addValuesToSet(PC_STAFF_ONLINE_PROVINCE + splitArea[0], netbarId.toString());
			//添加市统计在线收银端
			stringRedisOperateService.addValuesToSet(PC_STAFF_ONLINE_CITY + splitArea[1], netbarId.toString());
			//添加地区统计在线收银端
			stringRedisOperateService.addValuesToSet(PC_STAFF_ONLINE_AREA + splitArea[2], netbarId.toString());
		}

		/*统计某个网吧客户端信息数据*/
		{

			int currentNetbarCount = NumberUtils
					.toInt(stringRedisOperateService.getData(PC_STAFF_CLIENT_ONLINE_NETBAR + netbarId.toString()));

			int currentAll = NumberUtils.toInt(stringRedisOperateService.getData(PC_STAFF_CLIENT_ONLINE_ALL));

			int currentProvince = NumberUtils
					.toInt(stringRedisOperateService.getData(PC_STAFF_CLIENT_ONLINE_PROVINCE + splitArea[0]));

			int currentCity = NumberUtils
					.toInt(stringRedisOperateService.getData(PC_STAFF_CLIENT_ONLINE_CITY + splitArea[1]));

			int currentArea = NumberUtils
					.toInt(stringRedisOperateService.getData(PC_STAFF_CLIENT_ONLINE_AREA + splitArea[2]));
			//添加全国统计在线客户端
			stringRedisOperateService.setData(PC_STAFF_CLIENT_ONLINE_ALL,
					String.valueOf(currentAll - currentNetbarCount + onlineCount));
			//添加省份统计在线客户端
			stringRedisOperateService.setData(PC_STAFF_CLIENT_ONLINE_PROVINCE + splitArea[0],
					String.valueOf(currentProvince - currentNetbarCount + onlineCount));
			//添加市统计在线客户端
			stringRedisOperateService.setData(PC_STAFF_CLIENT_ONLINE_CITY + splitArea[1],
					String.valueOf(currentCity - currentNetbarCount + onlineCount));
			//添加地区统计在线客户端
			stringRedisOperateService.setData(PC_STAFF_CLIENT_ONLINE_AREA + splitArea[2],
					String.valueOf(currentArea - currentNetbarCount + onlineCount));
			//添加地区统计在线客户端
			stringRedisOperateService.setData(PC_STAFF_CLIENT_ONLINE_NETBAR + netbarId.toString(),
					String.valueOf(onlineCount));
		}
		stringRedisOperateService.setData(PC_STAFF_CLIENT_NETBAR + netbarId.toString(), String.valueOf(onlineCount));

	}

	public NetbarClientInfo save(NetbarClientInfo clientInfo) {
		return netbarClientInfoDao.save(clientInfo);

	}

	public NetbarClientInfo findByNetbarId(Long netbarId) {
		return netbarClientInfoDao.findByNetbarId(netbarId);
	}

	public void realtimeCounterDataLanding() {
		Set<String> netbarIds = stringRedisOperateService.getSetValues(PC_STAFF_ONLINE_ALL);
		for (String netbarId : netbarIds) {
			dbTread.execute(new Runnable() {
				@Override
				public void run() {
					int currentNetbarCount = NumberUtils
							.toInt(stringRedisOperateService.getData(PC_STAFF_CLIENT_ONLINE_NETBAR + netbarId));
					int currentNetbarAllCount = NumberUtils
							.toInt(stringRedisOperateService.getData(PC_STAFF_CLIENT_NETBAR + netbarId));
					NetbarClientInfo clientInfo = netbarClientInfoDao.findByNetbarId(NumberUtils.toLong(netbarId));
					if (clientInfo != null) {
						clientInfo.setOnlineCount(currentNetbarCount);
						clientInfo.setTotalCount(currentNetbarAllCount);
						save(clientInfo);
					}
				}
			});
		}
	}

	public void updateCount(Long netbarId, int onlineCount, int allCount) {
		dbTread.execute(new Runnable() {
			@Override
			public void run() {
				if (null != netbarId) {
					NetbarClientInfo clientInfo = netbarClientInfoDao.findByNetbarId(netbarId);
					if (clientInfo != null) {
						clientInfo.setOnlineCount(onlineCount);
						clientInfo.setTotalCount(allCount);
						save(clientInfo);
					}
				}
			}
		});
	}

	public String[] splitArea(String area) {
		String[] areaData = new String[3];
		areaData[0] = StringUtils.substring(area, 0, 2);
		areaData[1] = StringUtils.substring(area, 0, 4);
		areaData[2] = StringUtils.substring(area, 0, 6);
		return areaData;
	}

	@Autowired
	private QueryDao queryDao;

	public int[] statisticOkp(String areaCode, String netbarName, String merchantName, String telephone) {
		int staffOnlineCount = 0;
		int totalCount = 0;
		int onlineClientCount = 0;
		String condition = "";
		if (StringUtils.isNotBlank(netbarName)) {
			condition += " and n.name ='" + netbarName + "'";
		}
		if (StringUtils.isNotBlank(merchantName)) {
			condition += " and m.username ='" + merchantName + "'";
		}
		if (StringUtils.isNotBlank(telephone)) {
			condition += " and m.owner_telephone ='" + telephone + "'";
		}

		if (StringUtils.isBlank(condition)) {
			int areaCodeLength = StringUtils.length(areaCode);
			String onlineClientCountKey = "";
			String onlineStaffCountKey = "";

			if (0 == areaCodeLength) {
				onlineClientCountKey = PC_STAFF_CLIENT_ONLINE_ALL;
				onlineStaffCountKey = PC_STAFF_ONLINE_ALL;

			} else if (2 == areaCodeLength) {
				onlineClientCountKey = PC_STAFF_CLIENT_ONLINE_PROVINCE + areaCode;
				onlineStaffCountKey = PC_STAFF_ONLINE_PROVINCE + areaCode;
				condition = " and n.area_code like '" + areaCode + "%'";

			} else if (4 == areaCodeLength) {
				onlineClientCountKey = PC_STAFF_CLIENT_ONLINE_CITY + areaCode;
				onlineStaffCountKey = PC_STAFF_ONLINE_CITY + areaCode;
				condition = " and n.area_code like '" + areaCode + "%'";

			} else if (6 == areaCodeLength) {
				onlineClientCountKey = PC_STAFF_CLIENT_ONLINE_AREA + areaCode;
				onlineStaffCountKey = PC_STAFF_ONLINE_AREA + areaCode;
				condition = " and n.area_code like '" + areaCode + "%'";

			}
			staffOnlineCount = stringRedisOperateService.getSetValues(onlineStaffCountKey).size();
			onlineClientCount = NumberUtils.toInt(stringRedisOperateService.getData(onlineClientCountKey));
			String sql = "select sum(n.seating) totalCount from netbar_t_info n,netbar_t_merchant m,netbar_t_clent_info ci  where n.id = m.netbar_id and n.is_valid = 1 and m.is_valid=1 and ci.netbar_id = n.id "
					+ condition;
			Number seatingCount = queryDao.query(sql);
			if (null == seatingCount) {
				totalCount = 0;
			} else {
				totalCount = seatingCount.intValue();
			}

		} else {
			String sql = "select n.id,n.seating from netbar_t_info n,netbar_t_merchant m,netbar_t_clent_info ci  where n.id = m.netbar_id and n.is_valid = 1 and ci.netbar_id = n.id "
					+ condition;
			List<Map<String, Object>> netbars = queryDao.queryMap(sql);
			if (CollectionUtils.isNotEmpty(netbars)) {
				for (Map<String, Object> map : netbars) {
					if (stringRedisOperateService.isSetMember(PC_STAFF_ONLINE_ALL, map.get("id").toString())) {
						staffOnlineCount++;
					}
					totalCount = totalCount + NumberUtils.toInt(map.get("seating").toString());
					onlineClientCount = onlineClientCount + NumberUtils.toInt(stringRedisOperateService
							.getData(PC_STAFF_CLIENT_ONLINE_NETBAR + map.get("id").toString()));
				}
			}

		}

		int[] result = new int[3];
		result[0] = staffOnlineCount;
		result[1] = totalCount;
		result[2] = onlineClientCount;
		return result;
	}

	public PageVO queryNetbarClientStatistics(String areaCode, String netbarName, String merchantName, String telephone,
			Integer page) {
		String condition = "";
		if (StringUtils.isNotBlank(netbarName)) {
			condition += " and n.name ='" + netbarName + "'";
		}
		if (StringUtils.isNotBlank(merchantName)) {
			condition += " and m.owner_name ='" + merchantName + "'";
		}
		if (StringUtils.isNotBlank(telephone)) {
			condition += " and m.owner_telephone ='" + telephone + "'";
		}

		if (StringUtils.isNotBlank(areaCode)) {
			condition += " and n.area_code like  '" + areaCode + "%' ";
		}

		String countSql = "select count( distinct ci.netbar_id) from  netbar_t_clent_info ci,  netbar_t_info n ,  netbar_t_merchant m where m.is_valid=1 and n.id = ci.netbar_id and n.id = m.netbar_id "
				+ condition;
		PageVO vo = new PageVO();
		Number count = queryDao.query(countSql);
		if (count == null || count.intValue() <= 0) {
			vo.setIsLast(1);
			vo.setTotal(0);
			vo.setCurrentPage(page);
		} else {
			String sql = "select  ci.ip,    ci.online_count, n.address , ci.status , n.seating total_count, n.name,  m.owner_name,  m.owner_telephone"
					+ " from  netbar_t_clent_info ci,  netbar_t_info n ,  netbar_t_merchant m where m.is_valid=1 and  n.id = ci.netbar_id and n.id = m.netbar_id "
					+ condition + " group by n.id  order  by ci.online_count desc ,ci.create_date desc limit "
					+ (page - 1) * 30 + ",30 ";

			List<Map<String, Object>> data = queryDao.queryMap(sql);
			vo.setList(data);
			vo.setTotal(count.intValue());
			vo.setCurrentPage(page);
			vo.setIsLast(count.intValue() > page * 30 ? 0 : 1);
		}
		return vo;
	}

	public List<Map<String, Object>> cientStatistics(String areaCode, String netbarName, String merchantName,
			String telephone) {
		String condition = "";
		if (StringUtils.isNotBlank(netbarName)) {
			condition += " and n.name ='" + netbarName + "'";
		}
		if (StringUtils.isNotBlank(merchantName)) {
			condition += " and m.username ='" + netbarName + "'";
		}
		if (StringUtils.isNotBlank(telephone)) {
			condition += " and m.owner_telephone ='" + telephone + "'";
		}

		if (StringUtils.isNotBlank(areaCode)) {
			condition += " and n.area_code like  '" + areaCode + "%' ";
		}

		String sql = "select  ci.ip,    ci.online_count, n.address , ci.status , n.seating total_count, n.name,  m.owner_name,  m.owner_telephone"
				+ " from  netbar_t_clent_info ci,  netbar_t_info n ,  netbar_t_merchant m where m.is_valid=1 and  n.id = ci.netbar_id and n.id = m.netbar_id "
				+ condition + " group by n.id  ";

		List<Map<String, Object>> data = queryDao.queryMap(sql);
		return data;
	}

	public void clearUnOnlineStaff() {

		Set<String> netbars = stringRedisOperateService.getSetValues(PC_STAFF_ONLINE_ALL);
		if (null != netbars && netbars.size() > 0) {
			for (String netbarId : netbars) {
				String data = stringRedisOperateService.getData(PC_STAFF_ONLINE_CHECK + netbarId);
				if (StringUtils.equals("1", data)) {
					continue;
				}
				long netbarIdLong = NumberUtils.toLong(netbarId);
				String areaCode = netbarArea.get(netbarIdLong);
				if (StringUtils.isBlank(areaCode)) {
					areaCode = netbarInfoService.findAreaCodeByNetbarId(netbarIdLong);
					if (!StringUtils.isBlank(areaCode)) {
						netbarArea.put(netbarIdLong, areaCode);
					}
				}
				String[] splitArea = splitArea(areaCode);
				/*
				 * 删除在线收银端记录
				 */
				stringRedisOperateService.removeSetValue(PC_STAFF_ONLINE_ALL, netbarId);
				stringRedisOperateService.removeSetValue(PC_STAFF_ONLINE_PROVINCE + splitArea[0], netbarId);
				stringRedisOperateService.removeSetValue(PC_STAFF_ONLINE_CITY + splitArea[1], netbarId);
				stringRedisOperateService.removeSetValue(PC_STAFF_ONLINE_AREA + splitArea[2], netbarId);
				/*
				 * 删除在线客户端记录
				 */
				int currentNetbarCount = NumberUtils
						.toInt(stringRedisOperateService.getData(PC_STAFF_CLIENT_ONLINE_NETBAR + netbarId.toString()));

				stringRedisOperateService.delData(PC_STAFF_CLIENT_ONLINE_NETBAR + netbarId.toString());//删除在线数据
				if (currentNetbarCount > 0) {

					int currentAll = NumberUtils.toInt(stringRedisOperateService.getData(PC_STAFF_CLIENT_ONLINE_ALL));
					int currentProvince = NumberUtils
							.toInt(stringRedisOperateService.getData(PC_STAFF_CLIENT_ONLINE_PROVINCE + splitArea[0]));
					int currentCity = NumberUtils
							.toInt(stringRedisOperateService.getData(PC_STAFF_CLIENT_ONLINE_CITY + splitArea[1]));
					int currentArea = NumberUtils
							.toInt(stringRedisOperateService.getData(PC_STAFF_CLIENT_ONLINE_AREA + splitArea[2]));
					//添加全国统计在线客户端
					stringRedisOperateService.setData(PC_STAFF_CLIENT_ONLINE_ALL,
							String.valueOf(currentAll - currentNetbarCount));
					//添加省份统计在线客户端
					stringRedisOperateService.setData(PC_STAFF_CLIENT_ONLINE_PROVINCE + splitArea[0],
							String.valueOf(currentProvince - currentNetbarCount));
					//添加市统计在线客户端
					stringRedisOperateService.setData(PC_STAFF_CLIENT_ONLINE_CITY + splitArea[1],
							String.valueOf(currentCity - currentNetbarCount));
					//添加地区统计在线客户端
					stringRedisOperateService.setData(PC_STAFF_CLIENT_ONLINE_AREA + splitArea[2],
							String.valueOf(currentArea - currentNetbarCount));

					dbTread.execute(new Runnable() {
						@Override
						public void run() {
							int currentNetbarCount = NumberUtils
									.toInt(stringRedisOperateService.getData(PC_STAFF_CLIENT_ONLINE_NETBAR + netbarId));
							NetbarClientInfo clientInfo = netbarClientInfoDao
									.findByNetbarId(NumberUtils.toLong(netbarId));
							if (clientInfo != null) {
								clientInfo.setOnlineCount(currentNetbarCount);
								save(clientInfo);
							}
						}
					});
				}
			}
		}
	}

	public static final Map<Long, String> netbarArea = Maps.newHashMap();
	@Autowired
	private NetbarInfoService netbarInfoService;
}