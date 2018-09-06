package com.miqtech.master.service.mall;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.support.atomic.RedisAtomicInteger;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.consts.Msg4UserType;
import com.miqtech.master.consts.MsgConstant;
import com.miqtech.master.consts.RedbagConstant;
import com.miqtech.master.consts.SystemUserConstant;
import com.miqtech.master.consts.mall.CoinConstant;
import com.miqtech.master.consts.mall.CommodityConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.award.AwardRecordDao;
import com.miqtech.master.dao.mall.CommodityHistoryCdkeyDao;
import com.miqtech.master.dao.mall.CommodityHistoryDao;
import com.miqtech.master.dao.mall.CommodityInfoDao;
import com.miqtech.master.dao.mall.MallCdkeyDao;
import com.miqtech.master.dao.mall.MallCdkeyStockDao;
import com.miqtech.master.dao.mall.MallMsgDao;
import com.miqtech.master.dao.user.UserInfoDao;
import com.miqtech.master.dao.user.UserRedbagDao;
import com.miqtech.master.entity.Pager;
import com.miqtech.master.entity.award.AwardRecord;
import com.miqtech.master.entity.bounty.Bounty;
import com.miqtech.master.entity.bounty.BountyPrize;
import com.miqtech.master.entity.common.SystemRedbag;
import com.miqtech.master.entity.mall.CoinHistory;
import com.miqtech.master.entity.mall.CommodityCategory;
import com.miqtech.master.entity.mall.CommodityHistory;
import com.miqtech.master.entity.mall.CommodityHistoryCdkey;
import com.miqtech.master.entity.mall.CommodityInfo;
import com.miqtech.master.entity.mall.MallCdkey;
import com.miqtech.master.entity.mall.MallCdkeyStock;
import com.miqtech.master.entity.mall.MallMsg;
import com.miqtech.master.entity.mall.TurntablePrize;
import com.miqtech.master.entity.mall.UserRecInfo;
import com.miqtech.master.entity.user.UserBlack;
import com.miqtech.master.entity.user.UserInfo;
import com.miqtech.master.entity.user.UserRedbag;
import com.miqtech.master.service.amuse.AmuseActivityInfoService;
import com.miqtech.master.service.boon.BoonCdkeyService;
import com.miqtech.master.service.bounty.BountyPrizeService;
import com.miqtech.master.service.bounty.BountyService;
import com.miqtech.master.service.system.SystemRedbagService;
import com.miqtech.master.service.user.UserBlackService;
import com.miqtech.master.thirdparty.service.JpushService;
import com.miqtech.master.thirdparty.service.juhe.JuheFlow;
import com.miqtech.master.thirdparty.service.juhe.JuheGameCharge;
import com.miqtech.master.thirdparty.service.juhe.JuheTelCharge;
import com.miqtech.master.thirdparty.util.SMSMessageUtil;
import com.miqtech.master.utils.DateUtils;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.vo.CommodityStatisticVO;
import com.miqtech.master.vo.PageVO;

/**
 * 商品兑换、抽奖历史操作service
 */
@Component
public class CommodityHistoryService {
	private static final Logger LOGGER = LoggerFactory.getLogger(CommodityHistoryService.class);
	@Autowired
	private CommodityHistoryDao commodityHistoryDao;
	@Autowired
	private CommodityInfoDao commodityDao;
	@Autowired
	private UserRedbagDao userRedbagDao;
	@Autowired
	private SystemRedbagService systemRedbagService;
	@Autowired
	private QueryDao queryDao;
	@Autowired
	private MallCdkeyDao mallCdkeyDao;
	@Autowired
	private MallCdkeyStockDao mallCdkeyStockDao;
	@Autowired
	private UserInfoDao userInfoDao;
	@Autowired
	private MallMsgDao mallMsgDao;
	@Autowired
	private JpushService msgOperateService;
	@Autowired
	private UserBlackService userBlackService;
	@Autowired
	private CommodityCategoryService commodityCategoryService;
	@Autowired
	private CommodityService commodityService;
	@Autowired
	private CoinHistoryService coinHistoryService;
	@Autowired
	private AwardRecordDao awardRecordDao;
	@Autowired
	private UserRecInfoService userRecInfoService;
	@Autowired
	private AmuseActivityInfoService amuseActivityInfoService;
	@Autowired
	private JedisConnectionFactory redisConnectionFactory;
	@Autowired
	BoonCdkeyService boonCdkeyService;
	@Autowired
	CommodityHistoryCdkeyDao commodityHistoryCdkeyDao;
	@Autowired
	TurntableVirtualService turntableVirtualService;
	@Autowired
	TurntablePrizeService turntablePrizeService;
	@Autowired
	BountyPrizeService bountyPrizeService;
	@Autowired
	BountyService bountyService;

	/**
	 * 保存商品历史
	 */
	public void save(CommodityHistory commodityHistory) {
		commodityHistoryDao.save(commodityHistory);
	}

	/**
	 * 增加金币商品历史（公用方法）
	 */
	public int addCommodityHistoryPub(int amout, long userId, Long commodityId, int isGet, String account,
			UserRecInfo userRecInfo) {
		CommodityInfo commodityInfo = null;
		UserInfo user = userInfoDao.findOne(userId);
		int type = 0;
		if (null != commodityId) {
			commodityInfo = commodityDao.findOne(commodityId);
			CommodityCategory commodityCategory = commodityCategoryService
					.getCommodityCategoryById(commodityInfo.getCategoryId());
			if (null != commodityCategory) {
				type = null == commodityCategory.getType() ? 0 : commodityCategory.getType();
			}
			// 1.若isGet为1，商品库存减1
			if (null != commodityInfo && CommodityConstant.INT_STATUS_TRUE == isGet) {
				commodityInfo.setInventory(commodityInfo.getInventory() - amout);
				int newInventory = null == commodityInfo.getInventory() ? 0 : commodityInfo.getInventory();
				if (!CommodityConstant.CATEGORY_TYPE_CDKEY.equals(type) && newInventory < 0) { //库存不足（说明：cdkey类型的商品以cdkey的库存为准）
					return -1;
				}
				commodityDao.save(commodityInfo);
				coinHistoryService.addGoldHistoryPub(userId, commodityId, CoinConstant.HISTORY_TYPE_COMMODITY,
						commodityInfo.getDiscountPrice(), CoinConstant.HISTORY_DIRECTION_EXPEND);
			}
		}

		// 2.新增商品历史
		CommodityHistory commodityHistory = new CommodityHistory();
		commodityHistory.setCommoditySource(1);
		commodityHistory.setCommodityId(commodityId);
		commodityHistory.setUserId(userId);
		if (CommodityConstant.CATEGORY_TYPE_REAL.equals(type) || CommodityConstant.CATEGORY_TYPE_RECHARGE.equals(type)
				|| CommodityConstant.CATEGORY_TYPE_CDKEY.equals(type)) {//实物类，虚拟充值类商品，待客服处理
			commodityHistory.setStatus(CommodityConstant.INT_STATUS_FALSE); //0-待处理
		} else {
			commodityHistory.setStatus(CommodityConstant.INT_STATUS_TRUE); //1-已处理
			//发送短信
			if (isGet == 1) {
				//              this.sendMsg(user.getUsername(), commodityInfo.getName());
			}
		}
		if (CommodityConstant.CATEGORY_TYPE_RECHARGE.equals(type)) {
			if (StringUtils.isNotBlank(account)) {
				commodityHistory.setAccount(account);
			}
		}
		CommodityCategory commodityCategory = commodityCategoryService
				.getCommodityCategoryById(commodityInfo.getCategoryId());
		commodityHistory.setCoin(commodityInfo.getDiscountPrice()); //交易价格（金币）
		commodityHistory.setNum(amout); //一次交易的商品数量
		commodityHistory.setTranNo(amuseActivityInfoService.genSerial(0, commodityCategory.getSuperType())); //工具类生成商品流水号-->与娱乐赛保持一致
		commodityHistory.setInformation(commodityInfo.getInformationDefualt()); //默认交易信息
		commodityHistory.setIsGet(isGet); //1-已获取，0-未获得
		commodityHistory.setValid(CommonConstant.INT_BOOLEAN_TRUE);
		commodityHistory.setCreateUserId(getSysUserIdOfLeastOrder(1)); //分配给订单最少的审核人员
		commodityHistory.setCreateDate(new Date());

		// 3.如果商品是红包，记录用户红包
		if (isGet == 1 && CommodityConstant.CATEGORY_TYPE_REDBAG.equals(type)) {
			UserRedbag userRedbag = new UserRedbag();
			userRedbag.setUserId(userId);
			userRedbag.setRedbagId(systemRedbagService.querySystemRedbagId(RedbagConstant.REDBAG_TYPE_MALL)); //系统红包ID
			userRedbag.setNetbarType(1); //全部网吧
			userRedbag.setNetbarId((long) 0);
			userRedbag.setValid(CommonConstant.INT_BOOLEAN_TRUE);
			Date createDate = new Date();
			userRedbag.setCreateDate(createDate);
			userRedbag.setCreateUserId(userId);
			userRedbag.setUsable(1); //可用
			userRedbag.setAmount(commodityInfo.getQuota()); //额度

			userRedbagDao.save(userRedbag);
			//刚保存的红包ID
			commodityHistory.setThirdpartyId(userRedbag.getId());
		}
		//cdkey商品发送cdkey短信
		Integer isProvide = commodityInfo.getIsProvide();
		Long categeryId = commodityInfo.getCategoryId();
		if (isProvide != null && isProvide.equals(1) && null != categeryId
				&& CommodityConstant.CATEGORY_TYPE_CDKEY.equals(type)) {
			if (!deliverCdkey(user, commodityHistory, commodityInfo)) {
				return 0;
			}
		} else {
			save(commodityHistory);
		}
		if (userRecInfo != null) {
			userRecInfo.setId(null);
			userRecInfo.setHistoryId(commodityHistory.getId());
			userRecInfo.setCreateDate(new Date());
			userRecInfoService.save(userRecInfo);
		}
		//插入兑换消息
		insertMsg(user, commodityHistory, commodityInfo);
		return 1;
	}

	/**购买众筹夺宝
	 * @param id
	 * @param userId
	 * @param amount
	 * @return
	 */
	public int buyRobTreasure(Long id, Long userId, Integer amount) {
		boolean autoLottery = false;
		CommodityInfo commodityInfo = commodityDao.findByIdAndStatusAndValid(id, 1, 1);
		if (commodityInfo == null) {
			return 1;//商品不存在或已下架
		} else if (commodityInfo.getCrowdfundStatus() == null) {
			return 6;//不是众筹夺宝商品
		} else if (commodityInfo.getCrowdfundStatus() >= 1) {
			return 4;//商品等待开奖或已开奖
		}
		Integer consumeCoin = amount * commodityInfo.getPrice();
		if (consumeCoin == null || consumeCoin <= 0) {
			return 5;//金币不足
		}
		UserInfo userInfo = userInfoDao.findOne(userId);
		if (userInfo == null || userInfo.getCoin() < consumeCoin) {
			return 5;//金币不足
		}
		UserBlack userBlack = userBlackService.findByUserIdAndChannel(userId, 2);
		if (userBlack != null) {
			return 2;//黑名单用户不能购买该商品
		} else {
			Number number = queryDao
					.query("select sum(coin) from mall_r_commodity_history where is_valid=1 and commodity_source=1 and commodity_id="
							+ id + " and user_id=" + userId);
			if (number == null) {
				number = 0;
			}
			if (number != null && commodityInfo.getPurTimes() != null) {
				if (number.intValue() + consumeCoin > commodityInfo.getPurTimes() * commodityInfo.getPrice()) {
					return -commodityInfo.getPurTimes();//你已达到购买该商品数量上限
				}
			}
		}
		RedisAtomicInteger leftCoin = new RedisAtomicInteger(CommonConstant.MALL_COMMODITY_ROBTREASURE_LEFT_NUM + id,
				redisConnectionFactory);
		if (leftCoin.get() == -1) {
			return 4;//商品等待开奖或已开奖
		} else {
			if (leftCoin.get() == 0) {
				Number number = queryDao
						.query("select sum(coin) from mall_r_commodity_history where is_valid=1 and commodity_source=1 and commodity_id="
								+ id + " group by commodity_id");
				if (number == null) {
					number = 0;
				}
				int leftNum = commodityInfo.getCoins() - number.intValue();
				if (leftNum <= 0) {
					return 4;//商品等待开奖或已开奖
				} else if (leftNum == consumeCoin) {
					commodityInfo.setCrowdfundStatus(1);
					commodityDao.save(commodityInfo);
					//自动开奖
					autoLottery = true;
				} else if (leftNum < consumeCoin) {
					return 7;
				}
			} else {
				if (leftCoin.get() - consumeCoin < 0) {
					return 7;
				}
			}
			int result = coinHistoryService.addGoldHistoryPub(userId, id, CoinConstant.HISTORY_TYPE_ROBTREASURE,
					consumeCoin, CoinConstant.HISTORY_DIRECTION_EXPEND);
			if (result == -1) {
				return 5;//金币不足
			}
		}
		CommodityHistory commodityHistory = new CommodityHistory();
		commodityHistory.setCommodityId(id);
		commodityHistory.setCommoditySource(1);
		commodityHistory.setUserId(userId);
		commodityHistory.setStatus(-1);
		commodityHistory.setCoin(consumeCoin);
		CommodityCategory commodityCategory = commodityCategoryService
				.getCommodityCategoryById(commodityInfo.getCategoryId());
		commodityHistory.setTranNo(amuseActivityInfoService.genSerial(0,
				commodityCategory == null ? 1 : commodityCategory.getSuperType()));
		commodityHistory.setIsGet(0);
		commodityHistory.setValid(1);
		commodityHistory.setCreateDate(new Date());
		commodityHistory.setNum(amount);
		commodityHistory.setInformation(commodityInfo.getInformationDefualt());
		commodityHistoryDao.save(commodityHistory);
		leftCoin.set(leftCoin.get() - consumeCoin <= 0 ? -1 : leftCoin.get() - consumeCoin);//金币剩余0时设为-1,用以区分不存在的key返回的0
		if (leftCoin.get() == -1) {
			commodityInfo.setCrowdfundStatus(1);
			commodityDao.save(commodityInfo);
			//自动开奖
			autoLottery = true;
		}
		List<CommodityHistoryCdkey> cdkeyList = new ArrayList<CommodityHistoryCdkey>();
		for (int i = 0; i < amount; i++) {
			CommodityHistoryCdkey commodityHistoryCdkey = new CommodityHistoryCdkey();
			commodityHistoryCdkey.setHistoryId(commodityHistory.getId());
			commodityHistoryCdkey.setCdkey(boonCdkeyService.genCdkey());
			commodityHistoryCdkey.setIsSelected(0);
			commodityHistoryCdkey.setValid(1);
			commodityHistoryCdkey.setCreateDate(new Date());
			cdkeyList.add(commodityHistoryCdkey);
		}
		commodityHistoryCdkeyDao.save(cdkeyList);
		if (autoLottery && commodityInfo.getAutoDrawn() == 1) {
			autoLottery(id, commodityInfo);
		}
		return 0;
	}

	/**自动开奖,发放金币红包
	 * @param id
	 */
	public void autoLottery(Long id, CommodityInfo commodityInfo) {
		Map<String, Object> map = queryDao.querySingleMap(
				"select a.id history_id, b.id cdkey_id,a.user_id from mall_r_commodity_history a left join mall_commodity_history_cdkey b on a.id = b.history_id where a.commodity_id ="
						+ id + " and a.is_valid = 1 and a.is_get = 0 and b.cdkey is not null order by rand() limit 1");
		if (map != null && !map.isEmpty()) {
			queryDao.update(
					"update mall_r_commodity_history a,mall_commodity_history_cdkey b,mall_t_commodity c set a.is_get=1,b.is_selected=1,c.crowdfund_status=2,c.lottery_time=now() where a.id="
							+ ((Number) map.get("history_id")).toString() + " and b.id="
							+ ((Number) map.get("cdkey_id")).toString() + " and c.id=" + id);
		}
		//开奖消息推送
		pushMsgRobTreasure(id);
		//发放
		if (map.get("user_id") != null) {
			this.autoExchangeRedbagAndCoin(id, NumberUtils.toInt(commodityInfo.getCategoryId().toString()),
					commodityInfo.getQuota(), ((Number) map.get("user_id")).longValue(),
					CoinConstant.HISTORY_TYPE_ROBTREASURE);
			//当中奖记录为真实记录且类型为实物和虚拟充值时需要设置审核用户id
			if (NumberUtils.toInt(commodityInfo.getSuperType().toString()) != 1) {
				CommodityHistory commodityHistory = commodityHistoryDao
						.findOne(((Number) map.get("history_id")).longValue());
				commodityHistory.setCreateUserId(getSysUserIdOfLeastOrder(1)); //分配给订单最少的审核人员
				turntableVirtualService.saveOrUpdate(commodityHistory);
			}
		}

	}

	/*
	 * 发放公用方法
	 * 参数说明：channel：1-商城，2-娱乐赛
	 * 返回：0-发放成功，-1-发放失败
	 */
	public int grantByTypePub(int type, int thirdType, int quota, long orderId, int flag) {
		CommodityHistory commodityHistory = findById(orderId);
		if (type == 1) { //红包
			return redbagGrant(commodityHistory, quota);
		} else if (type == 2) { //CDKEY
			CommodityInfo commodityInfo = commodityService.getCommodityById(commodityHistory.getCommodityId());
			UserInfo user = userInfoDao.findOne(commodityHistory.getUserId());
			if (deliverCdkey(user, commodityHistory, commodityInfo)) {
				newAwardRecord(commodityHistory, 3, 0, null, 3);
				changeStatusAndRemark(commodityHistory, "0", 0);
				return 0;
			} else {
				modifyAwardRecord(newAwardRecord(commodityHistory, 3, 0, null, 0), "", "", 0, "CDKEY库存不足");
				changeStatusAndRemark(commodityHistory, "CDKEY库存不足", 0);
				return -1;
			}
		} else if (type == 4) { //虚拟充值（接第三方）
			if (flag == 1) { //重新发放
				String modify = "1";
				String[] arrayTranNo = commodityHistory.getTranNo().split("_");
				if (arrayTranNo.length > 1) {
					modify = arrayTranNo[arrayTranNo.length - 1];
					modify = String.valueOf(NumberUtils.toInt(modify) + 1);
				}
				commodityHistory.setTranNo(commodityHistory.getTranNo() + "_" + modify);
			}
			if (thirdType == 1) { //Q币
				long awardReordid = newAwardRecord(commodityHistory, 2, 5, null, 0);
				return chargeQB(commodityHistory, quota, awardReordid);
			} else if (thirdType == 2) { //话费
				long awardReordid = newAwardRecord(commodityHistory, 2, 3, null, 0);
				UserRecInfo userRecInfo = userRecInfoService.findValidByUserIdAndHistoryId(commodityHistory.getUserId(),
						commodityHistory.getId());

				String telephone = null;
				if (userRecInfo != null) {
					telephone = userRecInfo.getTelephone();
				}

				return chargeTel(commodityHistory, telephone, quota, awardReordid);
			} else if (thirdType == 3) { //流量
				long awardReordid = newAwardRecord(commodityHistory, 2, 4, null, 0);
				return chargeFlow(commodityHistory, quota, awardReordid);
			}
		} else if (type == 5) { //金币
			UserInfo user = userInfoDao.findOne(commodityHistory.getUserId());
			user.setCoin(user.getCoin() + quota);
			userInfoDao.save(user);

			CoinHistory coinHistory = new CoinHistory();
			coinHistory.setUserId(user.getId());
			coinHistory.setType(5);
			coinHistory.setCoin(quota);
			coinHistory.setDirection(1);
			coinHistory.setValid(CommonConstant.INT_BOOLEAN_TRUE);
			coinHistory.setCreateDate(new Date());
			coinHistoryService.save(coinHistory);

			newAwardRecord(commodityHistory, 1, 2, coinHistory.getId().toString(), 3);

			return changeStatusAndRemark(commodityHistory, "0", 3);
		}

		return 0;
	}

	/*
	 * 红包发放
	 */
	private int redbagGrant(CommodityHistory commodityHistory, int quota) {
		UserRedbag userRedbag = new UserRedbag();
		userRedbag.setRedbagId(systemRedbagService.querySystemRedbagId(RedbagConstant.REDBAG_TYPE_MALL)); //系统红包ID
		userRedbag.setUserId(commodityHistory.getUserId());
		userRedbag.setNetbarType(1);
		userRedbag.setNetbarId((long) 0);
		userRedbag.setAmount(quota);
		userRedbag.setUsable(1);
		userRedbag.setValid(CommonConstant.INT_BOOLEAN_TRUE);
		userRedbag.setCreateDate(new Date());
		userRedbagDao.save(userRedbag);

		commodityHistory.setThirdpartyId(userRedbag.getId());

		newAwardRecord(commodityHistory, 1, 1, userRedbag.getId().toString(), 3);

		changeStatusAndRemark(commodityHistory, "0", 2);

		return 0;
	}

	/*
	 * Q币充值
	 */
	private int chargeQB(CommodityHistory commodityHistory, Integer quota, long awardRecordId) {
		String account = commodityHistory.getAccount();
		if (StringUtils.isBlank(account)) {
			UserRecInfo userRecInfo = userRecInfoService.getEntityById(commodityHistory.getId());
			if (userRecInfo != null && userRecInfo.getAccount() != null) {
				account = userRecInfo.getAccount();
			}
		}
		String result = null;
		if (StringUtils.isNotBlank(account)) {
			Map<String, String> params = Maps.newHashMap();
			params.put("cardid", "220612");
			params.put("cardnum", quota.toString());
			params.put("orderid", commodityHistory.getTranNo());
			params.put("game_userid", account);
			result = JuheGameCharge.order(params);

			if ("0".equals(juHeAwardRecord(result, awardRecordId))) {
				result = "0";
			}
		} else {
			result = "-1";
		}

		return changeStatusAndRemark(commodityHistory, result, 1);
	}

	/*
	 * 流量充值
	 */
	private int chargeFlow(CommodityHistory commodityHistory, Integer quota, long awardRecordId) {
		UserInfo userInfo = userInfoDao.findOne(commodityHistory.getUserId());
		Map<String, String> params = Maps.newHashMap();
		params.put("phone", userInfo.getTelephone());
		params.put("quota", quota.toString());
		params.put("orderid", commodityHistory.getTranNo());
		String result = JuheFlow.flowCharge(params);

		if ("0".equals(juHeAwardRecord(result, awardRecordId))) {
			result = "0";
		}
		return changeStatusAndRemark(commodityHistory, result, 1);
	}

	/*
	 * 话费充值
	 */
	private int chargeTel(CommodityHistory commodityHistory, String phone, int cardnum, long awardRecordId) {
		String result = null;
		try {
			result = JuheTelCharge.onlineOrder(phone, cardnum, commodityHistory.getTranNo());
		} catch (Exception e) {
			LOGGER.error("聚合平台话费充值接口异常：", e);
		}

		result = juHeAwardRecord(result, awardRecordId);
		if ("0".equals(result)) {
			result = "0";
		}

		return changeStatusAndRemark(commodityHistory, result, 1);
	}

	private String juHeAwardRecord(String result, long awardRecordId) {
		Gson gson = new Gson();
		JSONObject object = gson.fromJson(result, JSONObject.class);
		if (object.getInteger("error_code") == 0) {
			JSONObject resultJson = object.getJSONObject("result");
			modifyAwardRecord(awardRecordId, "A", resultJson.getString("ordercash"), 3, "");
			return "0";
		} else {
			return object.getString("reason");
		}
	}

	/*
	 * 记录财务信息
	 */
	private long newAwardRecord(CommodityHistory commodityHistory, int type, int subType, String targetId, int status) {
		Integer commoditySource = commodityHistory.getCommoditySource();
		Integer quota = 0;
		Integer sourceType = 2;
		if (CommodityConstant.COMMODITY_SOURCE_TURNTABLE.equals(commoditySource)) {// 转盘商品
			TurntablePrize commodityInfo = turntablePrizeService
					.getTurntablePrizeById(commodityHistory.getCommodityId());
			quota = commodityInfo.getPrizeQuota();
			sourceType = 2;
		} else if (CommodityConstant.COMMODITY_SOURCE_NOT_TURNTABLE.equals(commoditySource)) {// 非转盘商品
			CommodityInfo commodityInfo = commodityService.getCommodityById(commodityHistory.getCommodityId());
			quota = commodityInfo.getQuota();
			sourceType = 2;
		} else if (CommodityConstant.COMMODITY_SOURCE_BOUNTY.equals(commoditySource)) {//悬赏令商品{
			BountyPrize commodityInfo = bountyPrizeService.findById(commodityHistory.getCommodityId());
			quota = commodityInfo.getAwardNum();
			sourceType = 3;
		}
		AwardRecord awardRecord = new AwardRecord();
		awardRecord.setUserId(commodityHistory.getUserId());
		awardRecord.setType(type);
		awardRecord.setSubType(subType);
		if (StringUtils.isNotBlank(targetId)) {
			awardRecord.setTargetId(NumberUtils.toLong(targetId));
		}
		awardRecord.setAmount(NumberUtils.toDouble(quota.toString()));
		awardRecord.setStatus(status);
		awardRecord.setChecked(0);
		awardRecord.setSourceType(sourceType);
		awardRecord.setSourceTargetId(commodityHistory.getCommodityId());
		awardRecord.setValid(CommodityConstant.INT_STATUS_TRUE);
		awardRecord.setCreateDate(new Date());

		awardRecordDao.save(awardRecord);
		return awardRecord.getId();
	}

	private void modifyAwardRecord(long id, String channel, String money, int status, String reason) {
		AwardRecord awardRecord = awardRecordDao.findOne(id);
		if (null != awardRecord) {
			awardRecord.setChannelCode(channel);
			awardRecord.setMoney(NumberUtils.toDouble(money));
			awardRecord.setStatus(status);
			awardRecord.setRemark(reason);
			awardRecord.setUpdateDate(new Date());

			awardRecordDao.save(awardRecord);
		}
	}

	/*
	 * 处理订单状态和备注
	 */
	private int changeStatusAndRemark(CommodityHistory commodityHistory, String result, int msgType) {
		Integer oldStatus = commodityHistory.getStatus();
		if (result.equals("0")) { //发放成功
			if (CommodityConstant.VERIFY_STATUS_PASS.equals(oldStatus)
					|| CommodityConstant.VERIFY_STATUS_FAIL.equals(oldStatus)) {
				commodityHistory.setStatus(CommodityConstant.VERIFY_STATUS_FINISH);
			} else if (CommodityConstant.VERIFY_STATUS_APPEALPASS.equals(oldStatus)
					|| CommodityConstant.VERIFY_STATUS_APPEALFAIL.equals(oldStatus)) {
				commodityHistory.setStatus(CommodityConstant.VERIFY_STATUS_APPEALFINISH);
			}
			if (msgType > 0) {
				pushMsg(commodityHistory, msgType);
				sendMsg(commodityHistory, msgType);
			}
			commodityHistory.setIsGet(1);
		} else { //发放失败
			if (CommodityConstant.VERIFY_STATUS_PASS.equals(oldStatus)) {
				commodityHistory.setStatus(CommodityConstant.VERIFY_STATUS_FAIL);
			} else if (CommodityConstant.VERIFY_STATUS_APPEALPASS.equals(oldStatus)) {
				commodityHistory.setStatus(CommodityConstant.VERIFY_STATUS_APPEALFAIL);
			}
			commodityHistory.setRemark(commodityHistory.getRemark() + ",B" + result);
			return -1;
		}

		commodityHistory.setUpdateDate(new Date());
		save(commodityHistory);
		return 0;
	}

	/*
	 * 推送消息
	 */
	private void pushMsg(CommodityHistory commodityHistory, int msgType) {
		String prizeName = null;
		Integer prizeQuota = null;
		if (CommodityConstant.COMMODITY_SOURCE_TURNTABLE.equals(commodityHistory.getCommoditySource())) {
			TurntablePrize prize = turntablePrizeService.getTurntablePrizeById(commodityHistory.getCommodityId());
			if (prize != null) {
				prizeName = prize.getPrizeName();
				prizeQuota = prize.getPrizeQuota();
			}
		} else if (CommodityConstant.COMMODITY_SOURCE_NOT_TURNTABLE.equals(commodityHistory.getCommoditySource())) {
			CommodityInfo commodityInfo = commodityService.getCommodityById(commodityHistory.getCommodityId());
			if (commodityInfo != null) {
				prizeName = commodityInfo.getName();
				prizeQuota = commodityInfo.getQuota();
			}
		} else if (CommodityConstant.COMMODITY_SOURCE_BOUNTY.equals(commodityHistory.getCommoditySource())) {
			BountyPrize bountyPrize = bountyPrizeService.findById(commodityHistory.getCommodityId());
			if (bountyPrize != null) {
				if (bountyPrize.getAwardName() != null) {
					prizeName = bountyPrize.getAwardName();
				}
				prizeQuota = 1;
				if (bountyPrize.getAwardNum() != null) {
					prizeQuota = bountyPrize.getAwardNum();
				}
			}
		}

		String content = StringUtils.EMPTY;
		if (msgType == 1) { //第三方充值
			content = "【网娱大师】尊敬的用户，您兑换的{" + prizeName + "}已成功，请前往帐号{" + commodityHistory.getAccount()
					+ "}确认；若有异议，可在APP内客服反馈或拨打热线4006902530。";
		} else if (msgType == 2) { //数据库红包
			content = "【网娱大师】尊敬的用户，您兑换的{ " + prizeQuota + "元网娱红包}已成功，请登录APP查询确认；若有异议，可在APP内客服反馈或拨打热线4006902530。";
		} else if (msgType == 3) { //数据库金币
			content = "【网娱大师】尊敬的用户，您兑换的{" + prizeQuota + "网娱金币}已成功，请登录APP查询确认；若有异议，可在APP内客服反馈或拨打热线4006902530。";
		}
		if (commodityHistory != null && commodityHistory.getCommoditySource() != null
				&& commodityHistory.getCommoditySource() == 3) {
			msgOperateService.notifyMemberAliasMsg(Msg4UserType.SYS_NOTIFY.ordinal(), commodityHistory.getUserId(),
					MsgConstant.PUSH_MSG_TYPE_SYS, "商品发放提示信息", content, true, commodityHistory.getId());
		} else {
			msgOperateService.notifyMemberAliasMsg(Msg4UserType.SYS_NOTIFY.ordinal(), commodityHistory.getUserId(),
					MsgConstant.PUSH_MSG_TYPE_COMMODITY, "商品发放提示信息", content, true, commodityHistory.getId());
		}

	}

	/*
	 * 发送短信
	 */
	private void sendMsg(CommodityHistory commodityHistory, int msgType) {
		UserInfo userInfo = userInfoDao.findOne(commodityHistory.getUserId());

		String prizeName = null;
		if (CommodityConstant.COMMODITY_SOURCE_TURNTABLE.equals(commodityHistory.getCommoditySource())) {
			TurntablePrize prize = turntablePrizeService.getTurntablePrizeById(commodityHistory.getCommodityId());
			if (prize != null) {
				prizeName = prize.getPrizeName();
			}
		} else {
			CommodityInfo commodityInfo = commodityService.getCommodityById(commodityHistory.getCommodityId());
			if (commodityInfo != null) {
				prizeName = commodityInfo.getName();
			}
		}

		String[] phoneNum = { userInfo.getTelephone() };
		if (msgType == 1) { //金币商城第三方充值
			if (commodityHistory.getCommoditySource() == 3) {
				if (commodityHistory.getCommodityId() != null) {
					BountyPrize bountyPrize = bountyPrizeService.findById(commodityHistory.getCommodityId());
					if (bountyPrize.getBountyId() != null) {
						Bounty bounty = bountyService.findById(bountyPrize.getBountyId());
						if (bounty.getTitle() != null) {
							prizeName = bounty.getTitle();
						}
					}
				}
				String[] params = { prizeName, commodityHistory.getAccount() };
				SMSMessageUtil.sendTemplateMessage(phoneNum, "3020068", params);
			} else {
				String[] params = { prizeName, commodityHistory.getAccount() };
				SMSMessageUtil.sendTemplateMessage(phoneNum, "8122", params);
			}
		} else if (msgType == 2 || msgType == 3) { //金币商城自有商品（红包、金币）
			String[] params = { prizeName, commodityHistory.getAccount() };
			SMSMessageUtil.sendTemplateMessage(phoneNum, "7130", params);
		}
	}

	/**
	 * 根据商品概率计算是否抽中奖品（返回1-抽中）
	 */
	public int lottery(double probability) {
		if (probability > 0 && probability <= 1) {
			double random = Math.random(); //随机产生一个[0,1)的double浮点数
			if (random <= probability) {
				return 1;
			}
		}
		return 0;
	}

	/**
	 * 统计用户兑换历史（只统计兑换到或抽到的），分页
	 */

	public Map<String, Object> exchangeListByUserId(long userId, int page, int rows) {
		if (page <= 0) {
			page = 1;
		}
		if (rows <= 0) {
			rows = PageUtils.API_DEFAULT_PAGE_SIZE;//api分页大小默认10
		}
		Map<String, Object> params = new HashMap<>();
		params.put("pageStart", (page - 1) * rows);
		params.put("rows", rows);
		params.put("userId", userId);
		String sqlQuery = SqlJoiner.join(
				"select h.id, h.create_date date, h.commodity_id commodityId, ic.icon, c.name from mall_r_commodity_history h",
				" left join mall_t_commodity c on c.id = h.commodity_id",
				" left join mall_r_commodity_icon ic on ic.commodity_id=c.id and ic.is_main=1",
				" where h.create_date is not null and h.is_valid=1 and h.is_get=1 and h.user_id=:userId",
				" order by h.create_date desc", " limit :pageStart, :rows");
		String sqlTotal = "select count(1) from mall_r_commodity_history h  where h.create_date is not null and h.is_valid=1 and h.is_get=1 and h.user_id="
				+ userId;
		Map<String, Object> map = new HashMap<>();
		map.put("list", queryDao.queryMap(sqlQuery, params));
		Number total = queryDao.query(sqlTotal);
		if (page * rows >= total.intValue()) {
			map.put("isLast", 1); //是最后一页
		} else {
			map.put("isLast", 0); //不是最后一页
		}

		return map;
	}

	/**
	 * 统计用户今日金币收入总数
	 */
	public Map<String, Object> statisticUserTodayIncome(long userId) {
		String sqlSum = SqlJoiner.join("select sum(h.coin) from mall_r_coin_history h",
				" where h.is_valid=1 and h.direction=1 and date(create_date) = date(now()) and h.user_id=" + userId);
		Number income = queryDao.query(sqlSum);
		Map<String, Object> map = new HashMap<>();
		map.put("income", income == null ? 0 : income.intValue());
		return map;
	}

	/**
	 * 兑换详情
	 */
	public Map<String, Object> exchangeDetail(long exchangeId) {
		String sqlQuery = SqlJoiner.join(
				"select h.id, h.commodity_id commodityId, h.coin, c.name, c.information_defualt information, h.create_date date, h.tran_no tranNo, if( h.status in (1, 6),1,IF(h.status IN(0,4,8), 0, IF(h.status IN(2,5,9),2,if(h.status=7,3,h.status)))) status",
				" from mall_r_commodity_history h", " left join mall_t_commodity c on c.id=h.commodity_id",
				" where h.is_valid=1 and h.id=" + exchangeId);
		Map<String, Object> exchangeDetailMap = queryDao.querySingleMap(sqlQuery);
		String sqlIcon = "select i.icon from mall_r_commodity_icon i where i.commodity_id="
				+ exchangeDetailMap.get("commodityId");
		exchangeDetailMap.put("icons", queryDao.queryMap(sqlIcon));

		return exchangeDetailMap;
	}

	/**查询商品兑换记录
	 * @param name
	 * @param phone
	 * @param status
	 * @param startDate
	 * @param endDate
	 * @param page
	 * @return
	 */
	public PageVO queryCommodityHistory(String name, String phone, Integer status, Integer type, String startDate,
			String endDate, Integer page) {
		String sql = "";
		String nameSql = "";
		String phoneSql = "";
		String statusSql = "";
		String isGetSql = " and a.is_get=1";
		String dateSql = "";
		String typeSql = "";
		if (StringUtils.isNotBlank(name)) {
			nameSql = SqlJoiner.join(" and b.name like '%", name, "%'");
		}
		if (StringUtils.isNotBlank(phone)) {
			phoneSql = SqlJoiner.join(" and d.username like '%", phone, "%'");
		}
		if (status != null) {
			statusSql = SqlJoiner.join(" and a.status=", String.valueOf(status));
		}
		if (StringUtils.isNotBlank(startDate) && StringUtils.isNotBlank(endDate)) {
			dateSql = SqlJoiner.join(" and date_format(a.create_date, '%Y-%m-%d')>='", startDate, "'",
					" and date_format(a.create_date, '%Y-%m-%d')<='", endDate, "'");
		}
		if (type != null) {
			typeSql = SqlJoiner.join(" and c.id=", String.valueOf(type));
		}
		int pageSize = PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		if (page == null) {
			page = 1;
		}
		int start = (page - 1) * pageSize;
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("start", start);
		params.put("pageSize", pageSize);
		String limitSql = " limit :start,:pageSize";
		sql = SqlJoiner.join(
				"select count(1) from mall_r_commodity_history a,mall_t_commodity b,mall_t_commodity_category c,user_t_info d,mall_r_commodity_icon e where a.is_valid=1 and a.commodity_id=b.id and b.category_id=c.id and a.user_id=d.id and b.id=e.commodity_id and e.is_main=1",
				nameSql, phoneSql, statusSql, isGetSql, dateSql);
		PageVO vo = new PageVO();
		Number totalCount = queryDao.query(sql);
		if (totalCount != null) {
			vo.setTotal(totalCount.intValue());
		}

		if (start + pageSize >= vo.getTotal()) {
			vo.setIsLast(1);
		}
		sql = SqlJoiner.join(
				"select a.*,b.name commodity_name,b.discount_price,b.is_provide,c.name commodity_type,c.id type_id,d.username,e.icon from mall_r_commodity_history a,mall_t_commodity b,mall_t_commodity_category c,user_t_info d,mall_r_commodity_icon e where a.is_valid=1 and a.commodity_id=b.id and b.category_id=c.id and a.user_id=d.id and b.id=e.commodity_id and e.is_main=1",
				nameSql, phoneSql, statusSql, typeSql, isGetSql, dateSql, " order by a.create_date desc", limitSql);
		vo.setList(queryDao.queryMap(sql, params));
		vo.setCurrentPage(page);
		return vo;
	}

	/**查询商品兑换记录
	 * @param name
	 * @param phone
	 * @param status
	 * @param startDate
	 * @param endDate
	 * @param page
	 * @return
	 */
	public PageVO queryCommodityHistoryStatistic(String name, String phone, Integer status, Integer isGet,
			Long categoryId, String startDate, String endDate, Integer page) {
		String sql = "";
		String nameSql = "";
		String turntableNameSql = "";
		String phoneSql = "";
		String statusSql = "";
		String isGetSql = "";
		String dateSql = "";
		String categorySql = "";
		if (StringUtils.isNotBlank(name)) {
			nameSql = SqlJoiner.join(" and b.name like '%", name, "%'");
			turntableNameSql = SqlJoiner.join(" and b.prize_name like '%", name, "%'");
		}
		if (StringUtils.isNotBlank(phone)) {
			phoneSql = SqlJoiner.join(" and d.username like '%", phone, "%'");
		}
		if (status != null) {
			statusSql = SqlJoiner.join(" and a.status=", String.valueOf(status));
		}
		if (isGet != null) {
			isGetSql = SqlJoiner.join(" and a.is_get=", String.valueOf(isGet));
		}
		if (categoryId != null) {
			categorySql = SqlJoiner.join(" and b.category_id=", String.valueOf(categoryId));
		}
		if (StringUtils.isNotBlank(startDate) && StringUtils.isNotBlank(endDate)) {
			dateSql = SqlJoiner.join(" and date_format(a.create_date, '%Y-%m-%d')>='", startDate, "'",
					" and date_format(a.create_date, '%Y-%m-%d')<='", endDate, "'");
		}
		int pageSize = PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		if (page == null) {
			page = 1;
		}
		int start = (page - 1) * pageSize;
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("start", start);
		params.put("pageSize", pageSize);
		String limitSql = " limit :start,:pageSize";
		//		sql = SqlJoiner
		//				.join("select count(1) from mall_r_commodity_history a,mall_t_commodity b,mall_t_commodity_category c,user_t_info d,mall_r_commodity_icon e where a.is_valid=1 and a.commodity_id=b.id and b.category_id=c.id and a.user_id=d.id and b.id=e.commodity_id and e.is_main=1",
		//						nameSql, phoneSql, statusSql, isGetSql, categorySql, dateSql);
		sql = "select count(1) from ( select count(1) num from mall_r_commodity_history a, mall_t_commodity b, mall_t_commodity_category c, user_t_info d, mall_r_commodity_icon e where a.is_valid = 1 and a.commodity_id = b.id and b.category_id = c.id and a.user_id = d.id and b.id = e.commodity_id and e.is_main = 1 and ( a.commodity_source = 1 or a.commodity_source is null ) and b.area_id != 3"
				+ nameSql + phoneSql + statusSql + isGetSql + categorySql + dateSql + " group by a.id"
				+ " union all select count(1) num from mall_r_commodity_history a, mall_t_turntable_prize b, user_t_info d where a.is_valid = 1 and a.commodity_id = b.id and a.user_id = d.id and a.commodity_source = 2 "
				+ turntableNameSql + phoneSql + statusSql + isGetSql + categorySql + dateSql + " group by a.id"
				+ " UNION ALL SELECT COUNT(1) FROM mall_r_commodity_history a, user_t_info d, mall_t_commodity b, mall_r_commodity_icon e WHERE a.is_valid = 1 AND a.user_id = d.id and a.commodity_id = b.id and e.commodity_id = b.id AND b.area_id = 3 AND a.commodity_source = 1"
				+ nameSql + phoneSql + statusSql + isGetSql + categorySql + dateSql + " group by a.id" + ") a";
		PageVO vo = new PageVO();
		Number totalCount = queryDao.query(sql);
		if (totalCount != null) {
			vo.setTotal(totalCount.intValue());
		}

		if (start + pageSize >= vo.getTotal()) {
			vo.setIsLast(1);
		}
		//      sql = SqlJoiner
		//              .join("select a.*,b.name commodity_name,b.discount_price,c.name commodity_type,d.username,e.icon from mall_r_commodity_history a,mall_t_commodity b,mall_t_commodity_category c,user_t_info d,mall_r_commodity_icon e where a.is_valid=1 and a.commodity_id=b.id and b.category_id=c.id and a.user_id=d.id and b.id=e.commodity_id and e.is_main=1",
		//                      nameSql, phoneSql, statusSql, isGetSql, categorySql, dateSql, " order by a.create_date desc",
		//                      limitSql);
		sql = "select a.tran_no, a.coin, a.num, a.create_date, a. status, a.is_get, b. name commodity_name, c. name commodity_type, d.username, e.icon from mall_r_commodity_history a, mall_t_commodity b, mall_t_commodity_category c, user_t_info d, mall_r_commodity_icon e where a.is_valid = 1 and a.commodity_id = b.id and b.category_id = c.id and a.user_id = d.id and b.id = e.commodity_id and e.is_main = 1 and ( a.commodity_source = 1 or a.commodity_source is null ) and b.area_id != 3"
				+ nameSql + phoneSql + statusSql + isGetSql + categorySql + dateSql + " group by a.id"
				+ " union all select a.tran_no, a.coin, a.num, a.create_date, a. status, a.is_get, b.prize_name commodity_name, case when b.category_id = 1 then '红包' when b.category_id = 2 then 'cdkey' when b.category_id = 3 then '实物' when b.category_id = 4 then '虚拟充值' when b.category_id = 5 then '金币' when b.category_id = 6 then '流量' when b.category_id = 7 then 'Q币' when b.category_id = 8 then '话费' end commodity_type, d.username, b.prize_img icon from mall_r_commodity_history a, mall_t_turntable_prize b, user_t_info d where a.is_valid = 1 and a.commodity_id = b.id and a.user_id = d.id and a.commodity_source = 2 "
				+ turntableNameSql + phoneSql + statusSql + isGetSql + categorySql + dateSql + " group by a.id"
				+ " UNION ALL SELECT a.tran_no, a.coin, a.num, a.create_date, a. STATUS, a.is_get, b.name commodity_name, CASE WHEN b.category_id = 1 THEN '红包' WHEN b.category_id = 2 THEN 'cdkey' WHEN b.category_id = 3 THEN '实物' WHEN b.category_id = 4 THEN '虚拟充值' WHEN b.category_id = 5 THEN '金币' WHEN b.category_id = 6 THEN '流量' WHEN b.category_id = 7 THEN 'Q币' WHEN b.category_id = 8 THEN '话费' END commodity_type, d.username, e. icon FROM mall_r_commodity_history a, user_t_info d, mall_t_commodity b, mall_r_commodity_icon e WHERE a.is_valid = 1 AND a.user_id = d.id and a.commodity_id = b.id and e.commodity_id = b.id AND b.area_id = 3 AND a.commodity_source = 1"
				+ nameSql + phoneSql + statusSql + isGetSql + categorySql + dateSql + " group by a.id"
				+ " order by create_date desc " + limitSql;
		vo.setList(queryDao.queryMap(sql, params));
		vo.setCurrentPage(page);
		return vo;
	}

	/**查询导出商品兑换记录
	 * @param name
	 * @param phone
	 * @param status
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public List<Map<String, Object>> queryExportCommodityHistoryStatistic(String name, String phone, Integer status,
			Integer isGet, Long categoryId, String startDate, String endDate) {
		String sql = "";
		String nameSql = "";
		String turntableNameSql = "";
		String phoneSql = "";
		String statusSql = "";
		String isGetSql = "";
		String dateSql = "";
		String categorySql = "";
		if (StringUtils.isNotBlank(name)) {
			nameSql = SqlJoiner.join(" and b.name like '%", name, "%'");
			turntableNameSql = SqlJoiner.join(" and b.prize_name like '%", name, "%'");
		}
		if (StringUtils.isNotBlank(phone)) {
			phoneSql = SqlJoiner.join(" and d.username like '%", phone, "%'");
		}
		if (status != null) {
			statusSql = SqlJoiner.join(" and a.status=", String.valueOf(status));
		}
		if (isGet != null) {
			isGetSql = SqlJoiner.join(" and a.is_get=", String.valueOf(isGet));
		}
		if (categoryId != null) {
			categorySql = SqlJoiner.join(" and b.category_id=", String.valueOf(categoryId));
		}
		if (StringUtils.isNotBlank(startDate) && StringUtils.isNotBlank(endDate)) {
			dateSql = SqlJoiner.join(" and date_format(a.create_date, '%Y-%m-%d')>='", startDate, "'",
					" and date_format(a.create_date, '%Y-%m-%d')<='", endDate, "'");
		}

		//		sql = SqlJoiner
		//				.join("select a.*,b.name commodity_name,b.discount_price,c.name commodity_type,d.username,e.icon from mall_r_commodity_history a,mall_t_commodity b,mall_t_commodity_category c,user_t_info d,mall_r_commodity_icon e where a.is_valid=1 and a.commodity_id=b.id and b.category_id=c.id and a.user_id=d.id and b.id=e.commodity_id and e.is_main=1",
		//						nameSql, phoneSql, statusSql, isGetSql, categorySql, dateSql, " order by a.create_date desc");
		sql = "select a.tran_no, a.coin, a.num, a.create_date, a. status, a.is_get, b. name commodity_name, c. name commodity_type, d.username, e.icon from mall_r_commodity_history a, mall_t_commodity b, mall_t_commodity_category c, user_t_info d, mall_r_commodity_icon e where a.is_valid = 1 and a.commodity_id = b.id and b.category_id = c.id and a.user_id = d.id and b.id = e.commodity_id and e.is_main = 1 and ( a.commodity_source = 1 or a.commodity_source is null ) and b.area_id != 3"
				+ nameSql + phoneSql + statusSql + isGetSql + categorySql + dateSql + " group by a.id"
				+ " union all select a.tran_no, a.coin, a.num, a.create_date, a. status, a.is_get, b.prize_name commodity_name, case when b.category_id = 1 then '红包' when b.category_id = 2 then 'cdkey' when b.category_id = 3 then '实物' when b.category_id = 4 then '虚拟充值' when b.category_id = 5 then '金币' when b.category_id = 6 then '流量' when b.category_id = 7 then 'Q币' when b.category_id = 8 then '话费' end commodity_type, d.username, b.prize_img icon from mall_r_commodity_history a, mall_t_turntable_prize b, user_t_info d where a.is_valid = 1 and a.commodity_id = b.id and a.user_id = d.id and a.commodity_source = 2 "
				+ turntableNameSql + phoneSql + statusSql + isGetSql + categorySql + dateSql + " group by a.id"
				+ " UNION ALL SELECT a.tran_no, a.coin, a.num, a.create_date, a. STATUS, a.is_get, b.name commodity_name, CASE WHEN b.category_id = 1 THEN '红包' WHEN b.category_id = 2 THEN 'cdkey' WHEN b.category_id = 3 THEN '实物' WHEN b.category_id = 4 THEN '虚拟充值' WHEN b.category_id = 5 THEN '金币' WHEN b.category_id = 6 THEN '流量' WHEN b.category_id = 7 THEN 'Q币' WHEN b.category_id = 8 THEN '话费' END commodity_type, d.username, e. icon FROM mall_r_commodity_history a, user_t_info d, mall_t_commodity b, mall_r_commodity_icon e WHERE a.is_valid = 1 AND a.user_id = d.id and a.commodity_id = b.id and e.commodity_id = b.id AND b.area_id = 3 AND a.commodity_source = 1"
				+ nameSql + phoneSql + statusSql + isGetSql + categorySql + dateSql + " group by a.id"
				+ " order by create_date desc ";

		return queryDao.queryMap(sql);
	}

	/**
	 * 总计
	 */
	public CommodityStatisticVO sumStatistic(String name, String phone, Integer status, Long categoryId, String start,
			String end) {
		Date endDate = new Date();
		Date startDate = DateUtils.addDays(endDate, -(DateUtils.getDayOfWeek(endDate) - 1));
		String startDateStr = DateUtils.dateToString(startDate, DateUtils.YYYY_MM_DD);
		String endDateStr = DateUtils.dateToString(endDate, DateUtils.YYYY_MM_DD);
		String sql = SqlJoiner.join(
				"select sum(discount_price) coin,sum(num) num from mall_r_commodity_history a,mall_t_commodity b where a.is_valid=1 and a.commodity_id=b.id and date_format(a.create_date, '%Y-%m-%d')>='",
				startDateStr, "' and date_format(a.create_date, '%Y-%m-%d')<='", endDateStr, "'");
		List<Map<String, Object>> list = queryDao.queryMap(sql);
		CommodityStatisticVO vo = new CommodityStatisticVO();
		if (list != null && list.size() > 0) {
			Map<String, Object> map = list.get(0);
			vo.setWeekCoin(map.get("coin") == null ? 0 : ((BigDecimal) map.get("coin")).intValue());
			vo.setWeekNum(map.get("num") == null ? 0 : ((BigDecimal) map.get("num")).intValue());
			vo.setStartDateStr(startDateStr);
			vo.setEndDateStr(DateUtils.dateToString(DateUtils.addDays(startDate, 6), DateUtils.YYYY_MM_DD));
		}

		String nameSql = "";
		String turntableNameSql = "";
		String phoneSql = "";
		String statusSql = "";
		String isGetSql = "";
		String dateSql = "";
		String categorySql = "";
		if (StringUtils.isNotBlank(name)) {
			nameSql = SqlJoiner.join(" and b.name like '%", name, "%'");
			turntableNameSql = SqlJoiner.join(" and b.prize_name like '%", name, "%'");
		}
		if (StringUtils.isNotBlank(phone)) {
			phoneSql = SqlJoiner.join(" and d.username like '%", phone, "%'");
		}
		if (status != null) {
			statusSql = SqlJoiner.join(" and a.status=", String.valueOf(status));
		}
		if (categoryId != null) {
			categorySql = SqlJoiner.join(" and b.category_id=", String.valueOf(categoryId));
		}
		if (StringUtils.isNotBlank(start) && StringUtils.isNotBlank(end)) {
			dateSql = SqlJoiner.join(" and date_format(a.create_date, '%Y-%m-%d')>='", start, "'",
					" and date_format(a.create_date, '%Y-%m-%d')<='", end, "'");
		}
		int pageSize = PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("start", start);
		params.put("pageSize", pageSize);
		sql = "select sum(coin) coin, sum(num) num from (select a.tran_no, a.coin, a.num, a.create_date, a. status, a.is_get, b. name commodity_name, c. name commodity_type, d.username, e.icon from mall_r_commodity_history a, mall_t_commodity b, mall_t_commodity_category c, user_t_info d, mall_r_commodity_icon e where a.is_valid = 1 and a.commodity_id = b.id and b.category_id = c.id and a.user_id = d.id and b.id = e.commodity_id and e.is_main = 1 and ( a.commodity_source = 1 or a.commodity_source is null ) and b.area_id != 3"
				+ nameSql + phoneSql + statusSql + isGetSql + categorySql + dateSql + " group by a.id"
				+ " union all select a.tran_no, a.coin, a.num, a.create_date, a. status, a.is_get, b.prize_name commodity_name, case when b.category_id = 1 then '红包' when b.category_id = 2 then 'cdkey' when b.category_id = 3 then '实物' when b.category_id = 4 then '虚拟充值' when b.category_id = 5 then '金币' when b.category_id = 6 then '流量' when b.category_id = 7 then 'Q币' when b.category_id = 8 then '话费' end commodity_type, d.username, b.prize_img icon from mall_r_commodity_history a, mall_t_turntable_prize b, user_t_info d where a.is_valid = 1 and a.commodity_id = b.id and a.user_id = d.id and a.commodity_source = 2 "
				+ turntableNameSql + phoneSql + statusSql + isGetSql + categorySql + dateSql + " group by a.id"
				+ " UNION ALL SELECT a.tran_no, a.coin, a.num, a.create_date, a. STATUS, a.is_get, b.name commodity_name, CASE WHEN b.category_id = 1 THEN '红包' WHEN b.category_id = 2 THEN 'cdkey' WHEN b.category_id = 3 THEN '实物' WHEN b.category_id = 4 THEN '虚拟充值' WHEN b.category_id = 5 THEN '金币' WHEN b.category_id = 6 THEN '流量' WHEN b.category_id = 7 THEN 'Q币' WHEN b.category_id = 8 THEN '话费' END commodity_type, d.username, e. icon FROM mall_r_commodity_history a, user_t_info d, mall_t_commodity b, mall_r_commodity_icon e WHERE a.is_valid = 1 AND a.user_id = d.id and a.commodity_id = b.id and e.commodity_id = b.id AND b.area_id = 3 AND a.commodity_source = 1"
				+ nameSql + phoneSql + statusSql + isGetSql + categorySql + dateSql + " group by a.id"
				+ " order by create_date desc) a";
		Map<String, Object> stastic = queryDao.querySingleMap(sql);
		if (MapUtils.isNotEmpty(stastic)) {
			Integer coin = MapUtils.getInteger(stastic, "coin");
			if (coin == null) {
				coin = 0;
			}
			vo.setCoin(coin);
			Integer num = MapUtils.getInteger(stastic, "num");
			if (num == null) {
				num = 0;
			}
			vo.setNum(num);
		}
		return vo;
	}

	/**发放cdkey
	 * @param id
	 */
	public boolean deliverCdkey(UserInfo userInfo, CommodityHistory commodityHistory, CommodityInfo commodityInfo) {
		MallCdkeyStock stock = mallCdkeyStockDao.findByCommodityId(commodityInfo.getId());
		if (stock == null) {
			return false;
		}
		List<MallCdkey> list = mallCdkeyDao.findByStockIdAndIsUse(stock.getId(), 0);
		if (list.size() > 0 && list.size() >= commodityHistory.getNum()) {
			commodityHistory.setStatus(1);
			commodityHistoryDao.save(commodityHistory);
			//			StringBuilder text = new StringBuilder("您兑换的" + commodityInfo.getName() + "对应的码为：");
			StringBuilder exhangecode = new StringBuilder();
			for (int i = 0; i < commodityHistory.getNum(); i++) {
				MallCdkey cdkey = list.get(i);
				//发送短信
				if (i < commodityHistory.getNum() - 1) {
					exhangecode.append(cdkey.getCdkey() + ",");
				} else {
					exhangecode.append(cdkey.getCdkey());
				}
				cdkey.setHistoryId(commodityHistory.getId());
				cdkey.setIsUse(1);
				mallCdkeyDao.save(cdkey);
			}

			String[] phoneNum = { userInfo.getUsername() };
			String[] params = { commodityInfo.getName(), exhangecode.toString() };
			SMSMessageUtil.sendTemplateMessage(phoneNum, "4028", params);

			return true;
		} else {
			return false;
		}
	}

	/**异常重新发送
	 * @param id
	 */
	public boolean reDeliver(Long id) {
		CommodityHistory commodityHistory = commodityHistoryDao.findOne(id);
		if (commodityHistory != null) {
			List<MallCdkey> list = mallCdkeyDao.findByHistoryIdAndIsUse(commodityHistory.getId(), 1);
			CommodityInfo commodityInfo = commodityDao.findOne(commodityHistory.getCommodityId());
			//			StringBuilder text = new StringBuilder("您兑换的" + commodityInfo.getName() + "对应的码为：");
			StringBuilder exhangecode = new StringBuilder();
			for (int i = 0; i < list.size(); i++) {
				MallCdkey cdkey = list.get(i);
				if (i < list.size() - 1) {
					exhangecode.append(cdkey.getCdkey() + ",");
				} else {
					exhangecode.append(cdkey.getCdkey());
				}
			}
			UserInfo userInfo = userInfoDao.findOne(commodityHistory.getUserId());
			if (userInfo != null) {

				String[] phoneNum = { userInfo.getUsername() };
				String[] params = { commodityInfo.getName(), exhangecode.toString() };
				Map<String, Object> result = SMSMessageUtil.sendTemplateMessage(phoneNum, "4028", params);

				if (result.get(SMSMessageUtil.KEY_MAP_RESULT).toString().equals("1")) {
					commodityHistory.setStatus(1);
					commodityHistoryDao.save(commodityHistory);
					return true;
				}
			}
		}
		return false;
	}

	/**插入兑换消息
	 * @param userInfo
	 * @param commodityHistory
	 */
	public void insertMsg(UserInfo userInfo, CommodityHistory commodityHistory, CommodityInfo commodityInfo) {
		MallMsg mallMsg = new MallMsg();
		mallMsg.setUserId(userInfo.getId());
		mallMsg.setType(3);
		String content = userInfo.getNickname() + "于"
				+ DateUtils.dateToString(new Date(), DateUtils.YYYY_MM_DD_HH_MM_SS) + "兑换了" + commodityInfo.getName();
		mallMsg.setContent(content);
		mallMsg.setTargetId(commodityHistory.getId());
		mallMsg.setValid(1);
		mallMsgDao.save(mallMsg);
	}

	/**
	 * 商品使用排名
	 */
	public List<Map<String, Object>> rankCommodities(String beginDate, String endDate, Integer areaId) {
		String sql = SqlJoiner.join("SELECT c.id, c.`name`, sum(IF(mch.is_get = 1, 1, 0)) getted, count(mch.id) count",
				" FROM mall_r_commodity_history mch", " LEFT JOIN mall_t_commodity c ON mch.commodity_id = c.id",
				buildRankCondition(beginDate, endDate, areaId));

		return queryDao.queryMap(sql);
	}

	/**
	 * 产生商品使用排名的查询条件
	 */
	private String buildRankCondition(String beginDate, String endDate, Integer areaId) {
		String condition = " WHERE mch.is_valid = 1";
		if (StringUtils.isNotBlank(beginDate)) {
			condition = SqlJoiner.join(condition, " AND mch.create_date >= ':beginDate'").replaceAll(":beginDate",
					beginDate);
		}
		if (StringUtils.isNotBlank(endDate)) {
			condition = SqlJoiner.join(condition, " AND mch.create_date < ADDDATE(':endDate',INTERVAL 1 DAY)")
					.replaceAll(":endDate", endDate);
		}
		if (areaId != 1) {
			areaId = 2;
		}
		condition = SqlJoiner.join(condition, " AND c.area_id = :areaId GROUP BY c.id ORDER BY count DESC LIMIT 10")
				.replaceAll(":areaId", areaId.toString());
		return condition;
	}

	/**
	 * 查询用户商品兑换情况
	 */
	public PageVO userCommodityHistory(int page, Long userId, String username, String beginDate, String endDate,
			String orderColumn, String orderType) {
		if (StringUtils.isBlank(orderColumn)) {
			orderColumn = "mch.create_date";
		}
		if (StringUtils.isBlank(orderType)) {
			orderType = "DESC";
		}

		// 产生查询条件
		String condition = " WHERE mch.is_valid = 1";
		if (userId != null) {
			condition = SqlJoiner.join(condition, " AND mch.user_id = :userId").replaceAll(":userId",
					userId.toString());
		}
		if (StringUtils.isNotBlank(username)) {
			condition = SqlJoiner.join(condition, " AND u.username = ':username'").replaceAll(":username", username);
		}
		if (StringUtils.isNotBlank(beginDate)) {
			condition = SqlJoiner.join(condition, " AND mch.create_date >= ':beginDate'").replaceAll(":beginDate",
					beginDate);
		}
		if (StringUtils.isNotBlank(endDate)) {
			condition = SqlJoiner.join(condition, " AND mch.create_date < ADDDATE(':endDate', INTERVAL 1 DAY)")
					.replaceAll(":endDate", endDate);
		}

		Integer pageRows = PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		Integer startRow = (page - 1) * pageRows;
		String pageCondition = " LIMIT :startRow, :pageRow".replaceAll(":startRow", startRow.toString())
				.replaceAll(":pageRow", pageRows.toString());

		String sql = SqlJoiner
				.join("SELECT mch.id, mch.coin, c.name, mch.status, u.username, mch.create_date createDate,",
						" mch.commodity_source commoditySource, tp.prize_name prizeName",
						" FROM mall_r_commodity_history mch LEFT JOIN user_t_info u ON mch.user_id = u.id",
						" LEFT JOIN mall_t_commodity c ON mch.commodity_id = c.id and (mch.commodity_source = 1 or mch.commodity_source is null)",
						" LEFT JOIN mall_t_turntable_prize tp ON mch.commodity_id = tp.id and mch.commodity_source = 2",
						condition, " ORDER BY :orderColumn :orderType", pageCondition)
				.replaceAll(":orderColumn", orderColumn).replaceAll(":orderType", orderType);
		List<Map<String, Object>> list = queryDao.queryMap(sql);

		// 查询总数
		String countSql = SqlJoiner.join(
				"SELECT COUNT(1) FROM (SELECT mch.id, mch.coin, mch.status, u.username, mch.create_date",
				" FROM mall_r_commodity_history mch LEFT JOIN user_t_info u ON mch.user_id = u.id", condition, ") a");
		Number count = queryDao.query(countSql);
		if (count == null) {
			count = 0;
		}

		PageVO vo = new PageVO();
		vo.setList(list);
		vo.setTotal(count.longValue());
		vo.setIsLast(page * PageUtils.ADMIN_DEFAULT_PAGE_SIZE >= count.intValue() ? 1 : 0);
		return vo;
	}

	/**
	 * 统计用户商品兑换情况
	 */
	public Map<String, Object> statisCommodityHistory(Long userId, String username, String beginDate, String endDate) {
		// 产生查询条件
		String condition = " WHERE 1";
		if (userId != null) {
			condition = SqlJoiner.join(condition, " AND mch.user_id = :userId").replaceAll(":userId",
					userId.toString());
		}
		if (StringUtils.isNotBlank(username)) {
			condition = SqlJoiner.join(condition, " AND u.username = ':username'").replaceAll(":username", username);
		}
		if (StringUtils.isNotBlank(beginDate)) {
			condition = SqlJoiner.join(condition, " AND mch.create_date >= ':beginDate'").replaceAll(":beginDate",
					beginDate);
		}
		if (StringUtils.isNotBlank(endDate)) {
			condition = SqlJoiner.join(condition, " AND mch.create_date < ADDDATE(':endDate', INTERVAL 1 DAY)")
					.replaceAll(":endDate", endDate);
		}

		String sql = SqlJoiner.join("SELECT t.*, IF ( ISNULL(b.is_valid), 1, b.is_valid ) userValid FROM",
				" (SELECT sum(mch.coin) sum, u.id userId, u.username, u.coin",
				" FROM mall_r_commodity_history mch LEFT JOIN user_t_info u ON mch.user_id = u.id",
				" LEFT JOIN mall_t_commodity c ON mch.commodity_id = c.id", condition, ") t",
				" LEFT JOIN user_t_black b ON b.user_id = t.userId AND b.is_valid = 1 LIMIT 1");
		return queryDao.querySingleMap(sql);
	}

	/**
	 * 统计用户兑换商品的情况
	 */
	public Map<String, Object> userCommodityRepory(Long userId, String username, String beginDate, String endDate) {
		String condition = " WHERE 1";
		if (userId != null) {
			condition = SqlJoiner.join(condition, " AND u.id = :userId").replaceAll(":userId", userId.toString());
		}
		if (StringUtils.isNotBlank(username)) {
			condition = SqlJoiner.join(condition, " AND u.username = ':username'").replaceAll(":username", username);
		}

		String leftCondition = "";
		if (userId != null) {
			leftCondition = SqlJoiner.join(leftCondition, " AND mch.user_id = :userId").replaceAll(":userId",
					userId.toString());
		}
		if (StringUtils.isNotBlank(beginDate)) {
			leftCondition = SqlJoiner.join(leftCondition, " AND mch.create_date >= ':beginDate'")
					.replaceAll(":beginDate", beginDate);
		}
		if (StringUtils.isNotBlank(endDate)) {
			leftCondition = SqlJoiner.join(leftCondition, " AND mch.create_date < ADDDATE(':endDate', INTERVAL 1 DAY)")
					.replaceAll(":endDate", endDate);
		}

		String sql = SqlJoiner.join(
				"SELECT u.id, u.username, u.telephone, u.qq, u.coin, sum(mch.coin) sum, b.is_valid isValid",
				" FROM user_t_info u LEFT JOIN mall_r_commodity_history mch ON u.id = mch.user_id",
				" LEFT JOIN user_t_black b ON b.user_id = u.id", leftCondition, condition, " GROUP BY mch.user_id");
		return queryDao.querySingleMap(sql);
	}

	/**发放成功发送短信
	 * @return
	 */
	public boolean sendMsg(String phone, String name) {
		//		String text = "尊敬的网娱会员，您兑换的{" + name + "}已发放，请前往APP确认使用；若有异常，请前往兑换订单页提交异常申请，小娱将第一时间为您处理。";

		String[] phoneNum = { phone };
		String[] params = { name };
		Map<String, Object> result = SMSMessageUtil.sendTemplateMessage(phoneNum, "2039", params);

		if (result.get(SMSMessageUtil.KEY_MAP_RESULT).toString().equals("1")) {
			return true;
		}
		return false;
	}

	/**充值发放
	 * @param id
	 * @param msg
	 * @return
	 */
	public int pay(Long id, String code, String msg) {
		CommodityHistory history = commodityHistoryDao.findOne(id);
		UserInfo user = userInfoDao.findOne(history.getUserId());
		//		CommodityInfo commodity = commodityDao.findOne(history.getCommodityId());
		//		if (!sendMsg(user.getUsername(), commodity.getName())) {
		//			return 1;
		//		}
		history.setIsGet(1);
		history.setStatus(1);
		history.setCode(code);
		if (StringUtils.isNotBlank(msg)) {
			history.setPushMsg(msg);
			msgOperateService.notifyMemberAliasMsg(Msg4UserType.SYS_NOTIFY.ordinal(), user.getId(),
					MsgConstant.PUSH_MSG_TYPE_SYS, "充值成功消息", msg, true, history.getId());
		}
		commodityHistoryDao.save(history);
		return 0;
	}

	/**cdkey处理
	 * @param id
	 * @param msg
	 * @return
	 */
	public int cdkeyDeal(Long id, String msg) {
		CommodityHistory history = commodityHistoryDao.findOne(id);
		UserInfo user = userInfoDao.findOne(history.getUserId());
		//		CommodityInfo commodity = commodityDao.findOne(history.getCommodityId());
		//		if (!sendMsg(user.getUsername(), commodity.getName())) {
		//			return 1;
		//		}
		history.setIsGet(1);
		history.setStatus(1);
		if (StringUtils.isNotBlank(msg)) {
			history.setPushMsg(msg);
			msgOperateService.notifyMemberAliasMsg(Msg4UserType.SYS_NOTIFY.ordinal(), user.getId(),
					MsgConstant.PUSH_MSG_TYPE_SYS, "cdkey消息", msg, true, history.getId());
		}
		commodityHistoryDao.save(history);
		return 0;
	}

	public Map<String, Object> queryDeliverInfo(String id) {
		String sql = "select b.name,b.telephone,b.qq,b.address from mall_r_commodity_history a left join mall_r_user_info b on a.user_id=b.user_id where a.id="
				+ id;
		return queryDao.querySingleMap(sql);
	}

	/**实物发放
	 * @param id
	 * @param msg
	 * @return
	 */
	public int deliver(Long id, String msg) {
		CommodityHistory history = commodityHistoryDao.findOne(id);
		UserInfo user = userInfoDao.findOne(history.getUserId());
		//		CommodityInfo commodity = commodityDao.findOne(history.getCommodityId());
		//		if (!sendMsg(user.getUsername(), commodity.getName())) {
		//			return 1;
		//		}
		history.setIsGet(1);
		history.setStatus(1);
		if (StringUtils.isNotBlank(msg)) {
			history.setPushMsg(msg);
			msgOperateService.notifyMemberAliasMsg(Msg4UserType.SYS_NOTIFY.ordinal(), user.getId(),
					MsgConstant.PUSH_MSG_TYPE_SYS, "实物发放消息", msg, true, history.getId());
		}
		commodityHistoryDao.save(history);
		return 0;
	}

	public void dealData(Long commodityId, String msg) {
		String sql = SqlJoiner.join(
				"select id,user_id from mall_r_commodity_history where is_valid=1 and is_get=1 and status=0 and commodity_id=",
				String.valueOf(commodityId));
		List<Map<String, Object>> list = queryDao.queryMap(sql);
		for (Map<String, Object> map : list) {
			long userId = ((Number) map.get("user_id")).longValue();
			long id = ((Number) map.get("id")).longValue();
			msgOperateService.notifyMemberAliasMsg(Msg4UserType.SYS_NOTIFY.ordinal(), userId,
					MsgConstant.PUSH_MSG_TYPE_ROBTREASURE, "开奖消息", msg, true, id);
			CommodityHistory commodityHistory = commodityHistoryDao.findOne(id);
			commodityHistory.setStatus(1);
			commodityHistoryDao.save(commodityHistory);
		}
	}

	public CommodityHistory findById(Long id) {
		return commodityHistoryDao.findOne(id);
	}

	/*
	 * 查出订单量最少的商城（1审核2发放）用户id
	 */
	public long getSysUserIdOfLeastOrder(int type) {
		long mallUserId = 0;
		String whichId = "h.create_user_id"; //审核人员id
		String userType = SystemUserConstant.TYPE_MALL_VERIFY.toString();
		if (type == 2) {
			whichId = "h.update_user_id"; //发放人员id
			userType = SystemUserConstant.TYPE_MALL_GRANT.toString();
		}
		String sql = SqlJoiner.join("SELECT COUNT(", whichId, ") count, u.id FROM sys_t_user u",
				" LEFT JOIN mall_r_commodity_history h ON u.id = ", whichId, " AND h.is_valid = 1",
				" WHERE u.is_valid = 1 AND u.user_type = ", userType, " GROUP BY ", whichId, " ORDER BY count LIMIT 1");
		Map<String, Object> map = queryDao.querySingleMap(sql);
		if (MapUtils.isNotEmpty(map) && null != map.get("id")) {
			mallUserId = NumberUtils.toLong(map.get("id").toString());
		}
		return mallUserId;
	}

	/**
	 * 后台管理：商品兑换审核列表
	 */
	public PageVO verifyPage(int page, Map<String, Object> searchParams) {
		String sqlJoin = StringUtils.EMPTY;
		String sqlTotalJoin = StringUtils.EMPTY;
		String condition = " WHERE v.is_valid = 1 AND v.is_get=1";
		String totalCondition = " WHERE v.is_valid = 1 AND v.is_get=1";
		String await = String.valueOf(CommodityConstant.VERIFY_STATUS_AWAIT); //0
		String pass = String.valueOf(CommodityConstant.VERIFY_STATUS_PASS); //4
		String appealPass = String.valueOf(CommodityConstant.VERIFY_STATUS_APPEALPASS); //5
		String refuse = String.valueOf(CommodityConstant.VERIFY_STATUS_REFUSE); //6
		String applealRefuse = String.valueOf(CommodityConstant.VERIFY_STATUS_APPEALREFUSE); //7

		Map<String, Object> params = Maps.newHashMap();
		if (MapUtils.isNotEmpty(searchParams)) {
			String telephone = MapUtils.getString(searchParams, "telephone");
			if (StringUtils.isNotBlank(telephone)) {
				String likeTelephone = "%" + telephone + "%";
				sqlJoin = SqlJoiner.join(sqlJoin, " RIGHT JOIN user_t_info u ON u.id=v.user_id AND u.telephone LIKE '",
						likeTelephone, "'");
				sqlTotalJoin = SqlJoiner.join(sqlTotalJoin,
						" RIGHT JOIN user_t_info u ON u.id=v.user_id AND u.telephone LIKE '", likeTelephone, "'");
			} else {
				sqlJoin = SqlJoiner.join(sqlJoin, " LEFT JOIN user_t_info u ON v.user_id = u.id");
			}
			sqlTotalJoin = SqlJoiner.join(sqlTotalJoin,
					" LEFT JOIN mall_t_commodity c ON c.id = v.commodity_id and (commodity_source = 1 or commodity_source is null)",
					" LEFT JOIN mall_t_commodity_area ca ON c.area_id = ca.id");
			String commodityName = MapUtils.getString(searchParams, "commodityName");
			sqlJoin = SqlJoiner.join(sqlJoin,
					" LEFT JOIN mall_t_commodity c ON c.id = v.commodity_id and (commodity_source = 1 or commodity_source is null)");
			if (StringUtils.isNotBlank(commodityName)) {
				String likeCommodityName = "%" + commodityName + "%";
				condition = SqlJoiner.join(condition, " AND (c.name LIKE '", likeCommodityName,
						"' or tp.prize_name like '", likeCommodityName, "')");
				totalCondition = SqlJoiner.join(totalCondition, " AND (c.name LIKE '", likeCommodityName,
						"' or tp.prize_name like '", likeCommodityName, "')");
			}
			sqlJoin = SqlJoiner.join(sqlJoin, " LEFT JOIN mall_t_commodity_category d ON d.id = c.category_id",
					" LEFT JOIN mall_t_commodity_area ca ON c.area_id = ca.id");

			// 关联转盘表
			sqlJoin = SqlJoiner.join(sqlJoin,
					" left join mall_t_turntable_prize tp on tp.id = v.commodity_id and commodity_source = 2");
			sqlTotalJoin = SqlJoiner.join(sqlTotalJoin,
					" left join mall_t_turntable_prize tp on tp.id = v.commodity_id and commodity_source = 2");

			// 提交时间
			String beginDate = MapUtils.getString(searchParams, "beginDate");
			if (StringUtils.isNotBlank(beginDate)) {
				condition = SqlJoiner.join(condition, " AND v.create_date >= :beginDate");
				params.put("beginDate", beginDate);
				totalCondition = SqlJoiner.join(totalCondition, " AND v.create_date >= '", beginDate, "'");
			}
			String endDate = MapUtils.getString(searchParams, "endDate");
			if (StringUtils.isNotBlank(endDate)) {
				condition = SqlJoiner.join(condition, " AND v.create_date < ADDDATE(:endDate, INTERVAL 1 DAY)");
				params.put("endDate", endDate);
				totalCondition = SqlJoiner.join(totalCondition, " AND v.create_date < ADDDATE('", endDate,
						"', INTERVAL 1 DAY)");
			}
			String status = MapUtils.getString(searchParams, "status");
			if (NumberUtils.isNumber(status)) {
				int statusInt = NumberUtils.toInt(status);
				if (statusInt == 4) {
					condition = SqlJoiner.join(condition, " AND v.status IN (", pass, ",", appealPass, ")");
					totalCondition = SqlJoiner.join(totalCondition, " AND v.status IN (", pass, ",", appealPass, ")");
				} else if (statusInt == 6) {
					condition = SqlJoiner.join(condition, " AND v.status IN (", refuse, ",", applealRefuse, ")");
					totalCondition = SqlJoiner.join(totalCondition, " AND v.status IN (", refuse, ",", applealRefuse,
							")");
				} else {
					condition = SqlJoiner.join(condition, " AND v.status = ", status);
					totalCondition = SqlJoiner.join(totalCondition, " AND v.status = ", status);
				}
			} else {
				condition = SqlJoiner.join(condition, " AND v.status = ", await);
				totalCondition = SqlJoiner.join(totalCondition, " AND v.status = ", await);
			}
			String area = MapUtils.getString(searchParams, "area");
			if (NumberUtils.isNumber(area)) {
				int areaInt = NumberUtils.toInt(area);
				if (areaInt > 0) {
					condition = SqlJoiner.join(condition, " AND ca.id = ", area);
					totalCondition = SqlJoiner.join(totalCondition, " AND ca.id = ", area);
				} else if (areaInt == -1) {
					condition = SqlJoiner.join(condition, " AND v.commodity_source = 2");
					totalCondition = SqlJoiner.join(totalCondition, " AND v.commodity_source = 2");
				}
			}
			String userId = MapUtils.getString(searchParams, "userId");
			if (StringUtils.isNotBlank(userId)) {
				condition = SqlJoiner.join(condition, " AND v.create_user_id = :userId");
				params.put("userId", userId);
				totalCondition = SqlJoiner.join(totalCondition, " AND v.create_user_id = ", userId);
			}
		}

		Integer pageSize = PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		page = page < 1 ? 1 : page;
		Integer startRow = (page - 1) * pageSize;
		String sqlLimit = SqlJoiner.join(" LIMIT ", startRow.toString(), ", ", pageSize.toString());

		condition = SqlJoiner.join(condition,
				" AND ( v.user_id NOT IN(SELECT DISTINCT user_id from user_t_black where is_white<>1 AND is_valid=1 AND channel=2)",
				" OR v.account NOT IN(SELECT  account FROM user_t_black WHERE is_white<>1 AND is_valid=1 AND channel=2) )");
		totalCondition = SqlJoiner.join(totalCondition,
				" AND ( v.user_id NOT IN(SELECT DISTINCT user_id from user_t_black where is_white<>1 AND is_valid=1 AND channel=2)",
				" OR v.account NOT IN(SELECT  account FROM user_t_black WHERE is_white<>1 AND is_valid=1 AND channel=2) )");

		String sql = SqlJoiner.join(
				"SELECT v.id, v.create_date createDate, v.tran_no tranNo, v.coin, v.account, v.status, v.user_id userId, c.name commodityName,",
				" d.name typeName, u.username telephone,c.area_id areaId, d.name categoryName, v.commodity_source commoditySource, ca.area_name areaName,",
				" tp.prize_name prizeName, tp.category_id turntableCategoryId, d.type, c.super_type superType, c.category_id categoryId",
				" FROM mall_r_commodity_history v", sqlJoin, condition, " ORDER BY v.create_date DESC", sqlLimit);
		String totalSql = SqlJoiner.join("SELECT COUNT(1) FROM mall_r_commodity_history v ", sqlTotalJoin,
				totalCondition);
		Number totalNum = queryDao.query(totalSql);
		int total = 0;
		if (totalNum != null) {
			total = totalNum.intValue();
		}
		List<Map<String, Object>> list = null;
		if (total > 0) {
			list = queryDao.queryMap(sql, params);
		}

		PageVO vo = new PageVO();
		vo.setList(list);
		vo.setTotal(total);
		return vo;
	}

	/**
	 * 后台管理：商品兑换发放列表
	 */
	public PageVO grantPage(int page, Map<String, Object> searchParams) {
		String sqlJoin = StringUtils.EMPTY;
		String sqlTotalJoin = StringUtils.EMPTY;
		String condition = " WHERE v.is_valid = 1 AND v.is_get=1";
		String totalCondition = " WHERE v.is_valid = 1 AND v.is_get=1";
		String finish = String.valueOf(CommodityConstant.VERIFY_STATUS_FINISH); //1
		String appealFinish = String.valueOf(CommodityConstant.VERIFY_STATUS_APPEALFINISH); //3
		String pass = String.valueOf(CommodityConstant.VERIFY_STATUS_PASS); //4
		String appealPass = String.valueOf(CommodityConstant.VERIFY_STATUS_APPEALPASS); //5
		String fail = String.valueOf(CommodityConstant.VERIFY_STATUS_FAIL); //8
		String appealFail = String.valueOf(CommodityConstant.VERIFY_STATUS_APPEALFAIL); //9

		Map<String, Object> params = Maps.newHashMap();
		if (MapUtils.isNotEmpty(searchParams)) {
			String telephone = MapUtils.getString(searchParams, "telephone");
			if (StringUtils.isNotBlank(telephone)) {
				String likeTelephone = "%" + telephone + "%";
				sqlJoin = SqlJoiner.join(sqlJoin, " RIGHT JOIN user_t_info u ON u.id=v.user_id AND u.telephone LIKE '",
						likeTelephone, "'");
				sqlTotalJoin = SqlJoiner.join(sqlTotalJoin,
						" RIGHT JOIN user_t_info u ON u.id=v.user_id AND u.telephone LIKE '", likeTelephone, "'");
			} else {
				sqlJoin = SqlJoiner.join(sqlJoin, " LEFT JOIN user_t_info u ON v.user_id = u.id ");
			}
			sqlJoin = SqlJoiner.join(sqlJoin,
					" LEFT JOIN mall_t_commodity c ON c.id = v.commodity_id and (commodity_source = 1 or commodity_source is null)",
					" LEFT JOIN mall_t_commodity_category d ON d.id = c.category_id");
			sqlTotalJoin = SqlJoiner.join(sqlTotalJoin,
					" LEFT JOIN mall_t_commodity c ON c.id = v.commodity_id and (commodity_source = 1 or commodity_source is null)");
			String commodityName = MapUtils.getString(searchParams, "commodityName");
			if (StringUtils.isNotBlank(commodityName)) {
				String likeCommodityName = "%" + commodityName + "%";
				condition = SqlJoiner.join(condition, " AND (c.name LIKE '", likeCommodityName,
						"' or tp.prize_name like '", likeCommodityName, "')");
				totalCondition = SqlJoiner.join(totalCondition, " AND (c.name LIKE '", likeCommodityName,
						"' or tp.prize_name like '", likeCommodityName, "')");
			}
			sqlJoin = SqlJoiner.join(sqlJoin, " LEFT JOIN mall_t_commodity_area ca ON c.area_id = ca.id");
			sqlTotalJoin = SqlJoiner.join(sqlTotalJoin, " LEFT JOIN mall_t_commodity_area ca ON c.area_id = ca.id");
			// 关联转盘表
			sqlJoin = SqlJoiner.join(sqlJoin,
					" left join mall_t_turntable_prize tp on tp.id = v.commodity_id and commodity_source = 2");
			sqlTotalJoin = SqlJoiner.join(sqlTotalJoin,
					" left join mall_t_turntable_prize tp on tp.id = v.commodity_id and commodity_source = 2");
			//关联悬赏令
			sqlJoin = SqlJoiner.join(sqlJoin,
					" LEFT JOIN bounty_prize bp ON bp.id = v.commodity_id AND commodity_source = 3 and bp.is_valid=1");
			sqlTotalJoin = SqlJoiner.join(sqlTotalJoin,
					" LEFT JOIN bounty_prize bp ON bp.id = v.commodity_id AND commodity_source = 3 and bp.is_valid=1");

			// 提交时间
			String beginDate = MapUtils.getString(searchParams, "beginDate");
			if (StringUtils.isNotBlank(beginDate)) {
				condition = SqlJoiner.join(condition, " AND v.create_date >= :beginDate");
				params.put("beginDate", beginDate);
				totalCondition = SqlJoiner.join(totalCondition, " AND v.create_date >= '", beginDate, "'");
			}
			String endDate = MapUtils.getString(searchParams, "endDate");
			if (StringUtils.isNotBlank(endDate)) {
				condition = SqlJoiner.join(condition, " AND v.create_date < ADDDATE(:endDate, INTERVAL 1 DAY)");
				params.put("endDate", endDate);
				totalCondition = SqlJoiner.join(totalCondition, " AND v.create_date < ADDDATE('", endDate,
						"', INTERVAL 1 DAY)");
			}
			String status = MapUtils.getString(searchParams, "status");
			if (NumberUtils.isNumber(status)) {
				int statusInt = NumberUtils.toInt(status);
				if (statusInt == 4) {
					condition = SqlJoiner.join(condition, " AND v.status IN (", pass, ",", appealPass, ")");
					totalCondition = SqlJoiner.join(totalCondition, " AND v.status IN (", pass, ",", appealPass, ")");
				} else if (statusInt == 1) {
					condition = SqlJoiner.join(condition, " AND v.status IN (", finish, ",", appealFinish, ")");
					totalCondition = SqlJoiner.join(totalCondition, " AND v.status IN (", finish, ",", appealFinish,
							")");
				} else if (statusInt == 8) {
					condition = SqlJoiner.join(condition, " AND v.status IN (", fail, ",", appealFail, ")");
					totalCondition = SqlJoiner.join(totalCondition, " AND v.status IN (", fail, ",", appealFail, ")");
				} else {
					condition = SqlJoiner.join(condition, " AND v.status = ", status);
					totalCondition = SqlJoiner.join(totalCondition, " AND v.status = ", status);
				}
			} else {
				condition = SqlJoiner.join(condition, " AND v.status IN (", pass, ",", appealPass, ")");
				totalCondition = SqlJoiner.join(totalCondition, " AND v.status (", pass, ",", appealPass, ")");
			}
			String account = MapUtils.getString(searchParams, "account");
			if (NumberUtils.isNumber(account)) {
				String likeAccount = "%" + account + "%";
				condition = SqlJoiner.join(condition, " AND v.account LIKE '", likeAccount, "'");
				totalCondition = SqlJoiner.join(totalCondition, " AND v.account LIKE '", likeAccount, "'");
			}
			String userId = MapUtils.getString(searchParams, "userId");
			if (StringUtils.isNotBlank(userId)) {
				condition = SqlJoiner.join(condition, " AND v.update_user_id = :userId");
				params.put("userId", userId);
				totalCondition = SqlJoiner.join(totalCondition, " AND v.update_user_id = ", userId);
			}
			String area = MapUtils.getString(searchParams, "area");
			if (NumberUtils.isNumber(area)) {
				int areaInt = NumberUtils.toInt(area);
				if (areaInt > 0) {
					condition = SqlJoiner.join(condition, " AND ca.id = ", area);
					totalCondition = SqlJoiner.join(totalCondition, " AND ca.id = ", area);
				} else if (areaInt == -1) {
					condition = SqlJoiner.join(condition, " AND v.commodity_source = 2");
					totalCondition = SqlJoiner.join(totalCondition, " AND v.commodity_source = 2");
				} else if (areaInt == -2) {
					condition = SqlJoiner.join(condition, " AND v.commodity_source = 3");
					totalCondition = SqlJoiner.join(totalCondition, " AND v.commodity_source = 3");
				}
			}
		}

		Integer pageSize = PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		String sqlLimit = "";
		if (page >= 1) {
			Integer startRow = (page - 1) * pageSize;
			sqlLimit = SqlJoiner.join(" LIMIT ", startRow.toString(), ", ", pageSize.toString());
		}

		String blackUserIds = userBlackService.getUserIdsByChannel(2);
		if (StringUtils.isNotBlank(blackUserIds)) {
			condition = SqlJoiner.join(condition, " AND v.user_id NOT IN(", blackUserIds, ")");
			totalCondition = SqlJoiner.join(totalCondition, " AND v.user_id NOT IN(", blackUserIds, ")");
		}
		String sql = SqlJoiner.join(
				"SELECT v.id, v.create_date createDate, v.tran_no tranNo, v.account, v.status, v.user_id userId, v.remark,v.commodity_source commoditySource,",
				" c.name commodityName, c.third_type, c.quota, c.super_type superType, c.category_id categoryId, d.type,",
				" d.name typeName, u.telephone, c.area_id areaId, ca.area_name areaName, tp.prize_name prizeName, tp.category_id turntableCategoryId,bp.award_name awardName,bp.award_sub_type awardSubType",
				" FROM mall_r_commodity_history v", sqlJoin, condition, " ORDER BY v.create_date DESC", sqlLimit);
		String totalSql = SqlJoiner.join("SELECT COUNT(distinct v.id) FROM mall_r_commodity_history v ", sqlTotalJoin,
				totalCondition);
		Number totalNum = queryDao.query(totalSql);
		int total = 0;
		if (totalNum != null) {
			total = totalNum.intValue();
		}
		List<Map<String, Object>> list = null;
		if (total > 0) {
			list = queryDao.queryMap(sql, params);
		}

		PageVO vo = new PageVO();
		vo.setList(list);
		vo.setTotal(total);
		return vo;
	}

	/*
	 * 商品兑换数量总计
	 */
	public int exchangeTotal() {
		String sql = "SELECT SUM(num) total FROM mall_r_commodity_history WHERE is_valid=1 AND is_get=1";
		int total = 0;
		Number totalNum = queryDao.query(sql);
		if (null != totalNum) {
			total = totalNum.intValue();
		}
		return total;
	}

	/*
	 * 根据商品id统计近期该商品兑换趋势
	 */
	public List<Map<String, Object>> exchangeTrend(long id, Map<String, Object> params) {
		String condition = " AND DATE(create_date)<DATE(NOW())";
		if (MapUtils.isNotEmpty(params)) {
			int date = MapUtils.getInteger(params, "date", 0);
			if (date > 0) {
				condition = SqlJoiner.join(condition, " AND TO_DAYS(create_date)>=TO_DAYS(NOW()) - ",
						String.valueOf(date));
			} else {
				String begin = MapUtils.getString(params, "begin");
				if (StringUtils.isNotBlank(begin)) {
					condition = SqlJoiner.join(condition, " AND DATE(create_date)>=DATE('", begin, "')");
				}
				String end = MapUtils.getString(params, "end");
				if (StringUtils.isNotBlank(end)) {
					condition = SqlJoiner.join(condition, " AND DATE(create_date)<=DATE('", end, "')");
				}
			}
		}
		String sql = SqlJoiner.join(
				"SELECT COUNT(DATE(create_date)) num, RIGHT(DATE(create_date),5) date  FROM mall_r_commodity_history WHERE is_valid=1 AND is_get=1 AND commodity_id=",
				String.valueOf(id), condition, " GROUP BY DATE(create_date) ORDER BY create_date ");
		return queryDao.queryMap(sql);
	}

	/*
	 * 某段时间内邀请人数
	 */
	public List<Map<String, Object>> inviteNum(Map<String, Object> params) {
		String condition = " AND DATE(create_date)<DATE(NOW())";
		if (MapUtils.isNotEmpty(params)) {
			int date = MapUtils.getInteger(params, "date", 0);
			if (date > 0) {
				condition = SqlJoiner.join(condition, " AND TO_DAYS(create_date)>=TO_DAYS(NOW()) - ",
						String.valueOf(date));
			} else {
				String begin = MapUtils.getString(params, "begin");
				if (StringUtils.isNotBlank(begin)) {
					condition = SqlJoiner.join(condition, " AND DATE(create_date)>=DATE('", begin, "')");
				}
				String end = MapUtils.getString(params, "end");
				if (StringUtils.isNotBlank(end)) {
					condition = SqlJoiner.join(condition, " AND DATE(create_date)<=DATE('", end, "')");
				}
			}
		}
		String sql = SqlJoiner.join(
				"SELECT COUNT(DATE(create_date)) num, RIGHT(DATE(create_date),5) date  FROM activity_r_invite WHERE is_valid=1 AND status=2",
				condition, " GROUP BY DATE(create_date) ORDER BY create_date ");
		return queryDao.queryMap(sql);
	}

	/*
	 * 根据类型商品统计兑换数量占比
	 */
	public List<Map<String, Object>> exchangeRatio(Map<String, Object> params) {
		String condition = " AND DATE(h.create_date)<DATE(NOW())";
		if (MapUtils.isNotEmpty(params)) {
			int date = MapUtils.getInteger(params, "date", 0);
			if (date > 0) {
				condition = SqlJoiner.join(condition, " AND TO_DAYS(h.create_date)>=TO_DAYS(NOW()) - ",
						String.valueOf(date));
			} else {
				String begin = MapUtils.getString(params, "begin");
				if (StringUtils.isNotBlank(begin)) {
					condition = SqlJoiner.join(condition, " AND DATE(h.create_date)>=DATE('", begin, "')");
				}
				String end = MapUtils.getString(params, "end");
				if (StringUtils.isNotBlank(end)) {
					condition = SqlJoiner.join(condition, " AND DATE(h.create_date)<=DATE('", end, "')");
				}
			}
		}
		String sql = SqlJoiner.join("SELECT t.name, COUNT(c.category_id) num, RIGHT(DATE(h.create_date),5) date",
				" FROM mall_r_commodity_history h", " LEFT JOIN mall_t_commodity c ON c.id = h.commodity_id",
				" LEFT JOIN mall_t_commodity_category t ON t.id = c.category_id",
				" WHERE h.is_valid = 1 AND h.is_get=1", condition, " GROUP BY c.category_id ORDER BY c.category_id");
		return queryDao.queryMap(sql);
	}

	/*
	 * 查询某游戏帐号兑换记录
	 */
	public List<Map<String, Object>> exchangeInfo(String account) {
		String sql = SqlJoiner.join("SELECT h.id, h.create_date date, h.num, u.telephone",
				" FROM mall_r_commodity_history h", " LEFT JOIN user_t_info u ON u.id = h.user_id",
				" WHERE h.is_valid = 1 AND h.account = '", account, "' ORDER BY h.create_date DESC");
		return queryDao.queryMap(sql);
	}

	/*
	 * 查询某游戏帐号兑换总数量
	 */
	public int exchangeTotalNum(String account) {
		int total = 0;
		String sql = SqlJoiner.join("SELECT sum(num) FROM mall_r_commodity_history",
				" WHERE is_valid = 1 AND account = '", account, "'");
		Number totalNum = queryDao.query(sql);
		if (null != totalNum) {
			total = totalNum.intValue();
		}
		return total;
	}

	/*
	 * 兑换信息列表，分页
	 */
	public PageVO exchangeList(int page, String account) {
		Integer pageSize = PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		page = page < 1 ? 1 : page;
		Integer startRow = (page - 1) * pageSize;
		String sqlLimit = SqlJoiner.join(" LIMIT ", startRow.toString(), ", ", pageSize.toString());

		String sql = SqlJoiner.join(
				"SELECT h.id, h.create_date date, h.num, u.telephone FROM mall_r_commodity_history h",
				" LEFT JOIN user_t_info u ON u.id = h.user_id", " WHERE h.is_valid = 1 AND h.account = ", account,
				" ORDER BY h.create_date DESC", sqlLimit);

		String totalSql = SqlJoiner
				.join("SELECT COUNT(1) FROM mall_r_commodity_history WHERE is_valid=1 AND account = ", account);
		Number totalNum = queryDao.query(totalSql);
		int total = 0;
		if (totalNum != null) {
			total = totalNum.intValue();
		}
		List<Map<String, Object>> list = null;
		if (total > 0) {
			list = queryDao.queryMap(sql);
		}

		PageVO vo = new PageVO();
		vo.setList(list);
		vo.setTotal(total);
		return vo;
	}

	/*
	 * 批量清空用户兑换记录
	 */
	public void clearHistoryByUserIds(String ids) {
		String sql = SqlJoiner.join("UPDATE mall_r_commodity_history SET is_valid=",
				CommonConstant.INT_BOOLEAN_FALSE.toString(), " WHERE user_id IN(", ids, ")");
		queryDao.update(sql);
	}

	/*
	 * 查询不同手机号给同一账户兑换的所有用户ID
	 */
	public List<Map<String, Object>> queryAllBalckUserIds() {
		String sql = SqlJoiner.join(
				"SELECT DISTINCT user_id, account FROM mall_r_commodity_history WHERE is_valid=1 AND account IN (",
				" SELECT account FROM mall_r_commodity_history WHERE is_valid=1 AND account IS NOT NULL GROUP BY account HAVING count(DISTINCT user_id)>1",
				" )AND user_id NOT IN (",
				" SELECT user_id FROM user_t_black WHERE (is_valid=1 OR is_white=1) AND channel=2", ")");

		return queryDao.queryMap(sql);
	}

	public PageVO exchangeRecord(Long userId, Pager pager) {
		PageVO vo = new PageVO();
		Number number = queryDao
				.query("select count(1) from mall_r_commodity_history a where a.is_valid=1 and a.user_id=" + userId
						+ " and (case when a.commodity_source=2 then is_get=1 else 1=1 end)");
		if (number != null) {
			vo.setTotal(pager.total);
			if (number.intValue() <= pager.total) {
				vo.setIsLast(1);
			}
		}
		vo.setList(queryDao.queryMap(
				"select distinct a.id, case when a.commodity_source = 2 then ( select prize_name from mall_t_turntable_prize b where a.commodity_id = b.id ) else ( select name from mall_t_commodity c where a.commodity_id = c.id ) end name, if ( e.id is not null and e.crowdfund_status is not null, ( case when date_format(a.create_date,'%Y-%m-%d')<'2016-06-01' and a.status=1 then 5 when date_add( if ( e.lottery_time is null, a.create_date, e.lottery_time ), INTERVAL 7 day ) < now() then 9 when e.crowdfund_status < 2 then e.crowdfund_status when a.is_get = 0 then 2 when ( a.is_get = 1 and d.history_id is null and a.status=-1) then 3 when ( a.is_get = 1 and d.history_id is not null and a. status IN (0, 4)) then 4 when a. status in (1) then 5 when a. status in (6, 8) then 6 when a. status in (2,9) then 7 when a. status in (3,5,7) then 8  end ), ( case  when date_format(a.create_date,'%Y-%m-%d')<'2016-06-01' and a.status=1 then 2 when date_add( if ( e.lottery_time is null, a.create_date, e.lottery_time ), INTERVAL 7 day ) < now() then 6 when ( a. status =-1 and d.history_id is null ) then 0 when ( a. status in (0, 4) and d.history_id is not null ) then 1 when a. status = 1 then 2 when a. status in (6, 8) then 3 when a. status in (2,9) then 4 when a. status in (3,5,7) then 5 end )) state, case when a.commodity_source = 2 then 1 when e.crowdfund_status is not null then 2 else 3 end type, a.create_date from mall_r_commodity_history a left join mall_r_user_info d on a.id = d.history_id left join mall_t_commodity e on a.commodity_id = e.id and (a.commodity_source = 1 or a.commodity_source is null) left join mall_t_turntable_prize f on a.commodity_id = f.id and a.commodity_source = 2 where a.commodity_source<>3 and a.is_valid = 1 and a.user_id ="
						+ userId
						+ " and (e.category_id<>1 and e.category_id<>5 or e.category_id is null) and ((f.category_id<>1 and f.category_id<>5) or f.category_id is null) and (is_get = 1 or a.status=-1)  order by a.create_date desc limit "
						+ pager.start + "," + pager.pageSize));
		return vo;
	}

	public Map<String, Object> exchangeDetail(Long id) {
		return queryDao.querySingleMap(
				"select a.id, a.coin, case when a.commodity_source = 2 then ( select prize_name from mall_t_turntable_prize f where a.commodity_id = f.id ) else ( select name from mall_t_commodity d where a.commodity_id = d.id ) end name, a.tran_no, if ( b.lottery_time is null, a.create_date, b.lottery_time ) date, a.infomation, if ( e.id is not null and e.crowdfund_status is not null, ( case when date_format(a.create_date,'%Y-%m-%d')<'2016-06-01' and a.status=1 then 5 when date_add( if ( b.lottery_time is null, a.create_date, b.lottery_time ), INTERVAL 7 day ) < now() then 9 when e.crowdfund_status < 2 then e.crowdfund_status when a.is_get = 0 then 2 when ( a.is_get = 1 and c.history_id is null and a. status =- 1 ) then 3 when ( a.is_get = 1 and c.history_id is not null and a. status IN (0, 4)) then 4 when a. status in (1) then 5 when a. status in (6, 8) then 6 when a. status in (2, 9) then 7 when a. status in (3, 5, 7) then 8 end ), ( case when date_format(a.create_date,'%Y-%m-%d')<'2016-06-01' and a.status=1 then 2 when date_add( if ( b.lottery_time is null, a.create_date, b.lottery_time ), INTERVAL 7 day ) < now() then 6 when ( a. status =- 1 and c.history_id is null ) then 0 when ( a. status in (0, 4) and c.history_id is not null ) then 1 when a. status = 1 then 2 when a. status in (6, 8) then 3 when a. status in (2, 9) then 4 when a. status in (3, 5, 7) then 5 end )) state, case when a.commodity_source = 2 then 1 when e.crowdfund_status is not null then 2 else 3 end type, group_concat(x.cdkey) as cdkeys, case when if ( e.category_id is null, z.category_id, e.category_id ) in (1, 5) then 1 when if ( e.category_id is null, z.category_id, e.category_id ) = 3 then 2 when if ( e.category_id is null, z.category_id, e.category_id ) in (6, 8) then 3 else 4 end commodity_type, case when a.update_date is null then 0 when if ( if ( e.category_id is null, z.category_id, e.category_id ) = 3, date_add( a.update_date, INTERVAL 15 day ), date_add(a.update_date, INTERVAL 3 day)) < now() then 1 else 0 end exception_disabled, b.id commodity_id from mall_r_commodity_history a left join mall_t_commodity b on a.commodity_id = b.id left join mall_r_user_info c on a.id = c.history_id left join mall_t_commodity e on a.commodity_id = e.id and a.commodity_source = 1 left join mall_commodity_history_cdkey x on a.id = x.history_id left join mall_t_turntable_prize z on a.commodity_id = z.id and a.commodity_source = 2 where a.id ="
						+ id + " group by a.id");
	}

	/**自动发放红包金币
	 * @param historyId
	 * @param categoryId
	 * @param coin
	 * @param userId
	 * @param coinType
	 */
	public void autoExchangeRedbagAndCoin(Long commodityId, Integer categoryId, Integer coin, Long userId,
			int coinType) {
		if (categoryId == 1) {//发放红包
			SystemRedbag SystemRedbag = systemRedbagService.findByTypeAndValid(RedbagConstant.REDBAG_TYPE_MALL, 1);
			UserRedbag userRedbag = new UserRedbag();
			userRedbag.setUserId(userId);
			userRedbag.setRedbagId(SystemRedbag.getId());
			userRedbag.setUsable(1);
			userRedbag.setAmount(coin);
			userRedbag.setNetbarType(1);
			userRedbag.setNetbarId(0L);
			userRedbag.setValid(1);
			userRedbag.setCreateDate(new Date());
			userRedbagDao.save(userRedbag);
		} else if (categoryId == 5) {//发放金币
			coinHistoryService.addGoldHistoryPub(userId, commodityId, coinType, coin,
					CoinConstant.HISTORY_DIRECTION_INCOME);
		}
	}

	/**众筹夺宝开奖消息推送
	 * @param id
	 */
	public void pushMsgRobTreasure(Long id) {
		List<Map<String, Object>> userIdList = queryDao.queryMap(
				"select distinct user_id from mall_r_commodity_history where is_valid=1 and user_id is not null and commodity_id="
						+ id);
		if (CollectionUtils.isNotEmpty(userIdList)) {
			for (Map<String, Object> m : userIdList) {
				msgOperateService.notifyMemberAliasMsg(Msg4UserType.SYS_NOTIFY.ordinal(),
						((Number) m.get("user_id")).longValue(), MsgConstant.PUSH_MSG_TYPE_ROBTREASURE, "众筹夺宝开奖消息",
						"亲，你所购买的众筹夺宝已开奖，快去查看吧。", true, id);
			}
		}
	}

	/**
	 * 查询商品兑换审核列表，用于导出到excel
	 */
	public List<Map<String, Object>> verifyPage1(Map<String, Object> searchParams) {
		String sqlJoin = StringUtils.EMPTY;
		String sqlTotalJoin = StringUtils.EMPTY;
		String condition = " WHERE v.is_valid = 1 AND v.is_get=1";
		String await = String.valueOf(CommodityConstant.VERIFY_STATUS_AWAIT); //0
		String pass = String.valueOf(CommodityConstant.VERIFY_STATUS_PASS); //4
		String appealPass = String.valueOf(CommodityConstant.VERIFY_STATUS_APPEALPASS); //5
		String refuse = String.valueOf(CommodityConstant.VERIFY_STATUS_REFUSE); //6
		String applealRefuse = String.valueOf(CommodityConstant.VERIFY_STATUS_APPEALREFUSE); //7

		Map<String, Object> params = Maps.newHashMap();
		if (MapUtils.isNotEmpty(searchParams)) {
			String telephone = MapUtils.getString(searchParams, "telephone");
			if (StringUtils.isNotBlank(telephone)) {
				String likeTelephone = "%" + telephone + "%";
				sqlJoin = SqlJoiner.join(sqlJoin, " RIGHT JOIN user_t_info u ON u.id=v.user_id AND u.telephone LIKE '",
						likeTelephone, "'");
				sqlTotalJoin = SqlJoiner.join(sqlTotalJoin,
						" RIGHT JOIN user_t_info u ON u.id=v.user_id AND u.telephone LIKE '", likeTelephone, "'");
			} else {
				sqlJoin = SqlJoiner.join(sqlJoin, " LEFT JOIN user_t_info u ON v.user_id = u.id");
			}
			sqlTotalJoin = SqlJoiner.join(sqlTotalJoin,
					" LEFT JOIN mall_t_commodity c ON c.id = v.commodity_id and (commodity_source = 1 or commodity_source is null)",
					" LEFT JOIN mall_t_commodity_area ca ON c.area_id = ca.id");
			String commodityName = MapUtils.getString(searchParams, "commodityName");
			if (StringUtils.isNotBlank(commodityName)) {
				String likeCommodityName = "%" + commodityName + "%";
				sqlJoin = SqlJoiner.join(sqlJoin,
						" RIGHT JOIN mall_t_commodity c ON c.id = v.commodity_id AND c.name LIKE '", likeCommodityName,
						"' and (commodity_source = 1 or commodity_source is null)");
				sqlTotalJoin = SqlJoiner.join(sqlTotalJoin, " AND c.name LIKE '", likeCommodityName, "'");
			} else {
				sqlJoin = SqlJoiner.join(sqlJoin,
						" LEFT JOIN mall_t_commodity c ON c.id = v.commodity_id and (commodity_source = 1 or commodity_source is null)");
			}
			sqlJoin = SqlJoiner.join(sqlJoin, " LEFT JOIN mall_t_commodity_category d ON d.id = c.category_id",
					" LEFT JOIN mall_t_commodity_area ca ON c.area_id = ca.id");

			// 关联转盘表
			String turntableCondition = StringUtils.EMPTY;
			if (StringUtils.isNotBlank(commodityName)) {
				String likePrizeName = commodityName;
				turntableCondition = " and tp.prize_name LIKE '" + likePrizeName + "'";
			}
			sqlJoin = SqlJoiner.join(sqlJoin,
					" left join mall_t_turntable_prize tp on tp.id = v.commodity_id and commodity_source = 2",
					turntableCondition);
			sqlTotalJoin = SqlJoiner.join(sqlTotalJoin,
					" left join mall_t_turntable_prize tp on tp.id = v.commodity_id and commodity_source = 2",
					turntableCondition);

			// 提交时间
			String beginDate = MapUtils.getString(searchParams, "beginDate");
			if (StringUtils.isNotBlank(beginDate)) {
				condition = SqlJoiner.join(condition, " AND v.create_date >= :beginDate");
				params.put("beginDate", beginDate);
			}
			String endDate = MapUtils.getString(searchParams, "endDate");
			if (StringUtils.isNotBlank(endDate)) {
				condition = SqlJoiner.join(condition, " AND v.create_date < ADDDATE(:endDate, INTERVAL 1 DAY)");
				params.put("endDate", endDate);
			}
			String status = MapUtils.getString(searchParams, "status");
			if (NumberUtils.isNumber(status)) {
				int statusInt = NumberUtils.toInt(status);
				if (statusInt == 4) {
					condition = SqlJoiner.join(condition, " AND v.status IN (", pass, ",", appealPass, ")");
				} else if (statusInt == 6) {
					condition = SqlJoiner.join(condition, " AND v.status IN (", refuse, ",", applealRefuse, ")");
				} else {
					condition = SqlJoiner.join(condition, " AND v.status = ", status);
				}
			} else {
				condition = SqlJoiner.join(condition, " AND v.status = ", await);
			}
			String area = MapUtils.getString(searchParams, "area");
			if (NumberUtils.isNumber(area)) {
				int areaInt = NumberUtils.toInt(area);
				if (areaInt > 0) {
					condition = SqlJoiner.join(condition, " AND ca.id = ", area);
				} else if (areaInt == -1) {
					condition = SqlJoiner.join(condition, " AND v.commodity_source = 2");
				}
			}
			String userId = MapUtils.getString(searchParams, "userId");
			if (StringUtils.isNotBlank(userId)) {
				condition = SqlJoiner.join(condition, " AND v.create_user_id = :userId");
				params.put("userId", userId);
			}
		}

		condition = SqlJoiner.join(condition,
				" AND ( v.user_id NOT IN(SELECT DISTINCT user_id from user_t_black where is_white<>1 AND is_valid=1 AND channel=2)",
				" OR v.account NOT IN(SELECT  account FROM user_t_black WHERE is_white<>1 AND is_valid=1 AND channel=2) )");

		String sql = SqlJoiner.join(
				"SELECT v.id, v.create_date createDate, v.tran_no tranNo, v.coin, v.account, v.status, v.user_id userId, c.name commodityName,",
				" d.name typeName, u.username telephone,c.area_id areaId, d.name categoryName, v.commodity_source commoditySource, ca.area_name areaName,",
				" tp.prize_name prizeName, tp.category_id turntableCategoryId, d.type, c.super_type superType, c.category_id categoryId",
				" FROM mall_r_commodity_history v", sqlJoin, condition, " ORDER BY v.create_date DESC");

		List<Map<String, Object>> list = null;
		list = queryDao.queryMap(sql, params);
		return list;
	}
}
