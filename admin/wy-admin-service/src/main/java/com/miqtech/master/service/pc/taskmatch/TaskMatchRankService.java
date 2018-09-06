package com.miqtech.master.service.pc.taskmatch;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Maps;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.consts.taskMatch.TaskMatchConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.msg.CompetitionMsgDao;
import com.miqtech.master.dao.pc.taskmatch.TaskMatchAwardDao;
import com.miqtech.master.dao.pc.taskmatch.TaskMatchFeeDao;
import com.miqtech.master.dao.pc.taskmatch.TaskMatchLimitHeroDao;
import com.miqtech.master.dao.pc.taskmatch.TaskMatchPrizeDao;
import com.miqtech.master.dao.pc.taskmatch.TaskMatchRankDao;
import com.miqtech.master.entity.msg.CompetitionMsg;
import com.miqtech.master.entity.pc.taskmatch.TaskMatchAward;
import com.miqtech.master.entity.pc.taskmatch.TaskMatchFee;
import com.miqtech.master.entity.pc.taskmatch.TaskMatchLimitHero;
import com.miqtech.master.entity.pc.taskmatch.TaskMatchPrize;
import com.miqtech.master.entity.pc.taskmatch.TaskMatchRank;
import com.miqtech.master.service.pc.userExtend.UserExtendService;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.utils.TradeNoUtil;
import com.miqtech.master.vo.PageVO;

/**
 * 排行榜任务赛 Service
 *
 * @author gaohanlin
 * @create 2017年09月01日
 */
@Service
public class TaskMatchRankService {
	private static final Logger LOGGER = LoggerFactory.getLogger(TaskMatchRankService.class);

	@Autowired
	private QueryDao queryDao;
	@Autowired
	private TaskMatchRankDao taskMatchRankDao;
	@Autowired
	private TaskMatchLimitHeroDao taskMatchLimitHeroDao;
	@Autowired
	private TaskMatchPrizeDao taskMatchPrizeDao;
	@Autowired
	private CompetitionMsgDao competitionMsgDao;
	@Autowired
	private UserExtendService userExtendService;
	@Autowired
	private TaskMatchFeeDao taskMatchFeeDao;
	@Autowired
	private TaskMatchAwardDao taskMatchAwardDao;

	/**
	 * 排行榜挑战赛列表
	 */
	public PageVO matchRankList(Integer page, Integer rows, String beginDate, String endDate, String status,
			String keyword) {
		String conditionSql = null;
		if (StringUtils.isNotBlank(beginDate)) {
			conditionSql = SqlJoiner.join(" and ptmr.create_date > '" + beginDate + "'");
			if (StringUtils.isNotBlank(endDate)) {
				conditionSql = SqlJoiner.join(conditionSql, " and ptmr.create_date < '" + endDate + "23:59:59' ");
			}
		}

		if (StringUtils.isNotBlank(status)) {
			conditionSql = SqlJoiner.join(" and ptmr.status =" + status);
		}

		if (StringUtils.isNotBlank(keyword)) {
			conditionSql = SqlJoiner.join(" and ptmr.name ='" + keyword + "' ");
		}

		String sqlQuery = SqlJoiner.join("select @rank \\:=@rank + 1 AS rank,name,ptmr.id,",
				"(select count(1) from pc_task_match_enter where task_id=ptmr.id and status <>0 and task_type=1 ) now_enter_num,",
				"(select count(1) from pc_task_match_enter where task_id=ptmr.id and status <>0 and task_type=1 )*fee_amount now_enter_total_cost,",
				"status,fee_type ", "from pc_task_match_rank ptmr,(SELECT @rank \\:= 0) B where 1=1 ");
		String sqlTotal = "select count(1) from pc_task_match_rank ptmr where 1=1 ";
		Map<String, Object> params = Maps.newHashMap();
		if (page < 1) {
			page = 1;
		}

		if (conditionSql != null) {
			sqlQuery = SqlJoiner.join(sqlQuery, conditionSql);
			sqlTotal = SqlJoiner.join(sqlTotal, conditionSql);
		}
		sqlQuery = SqlJoiner.join(sqlQuery, " order by ptmr.create_date desc limit :page, :row");

		if (rows == null || rows == 0) {
			rows = PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		}
		params.put("page", (page - 1) * rows);
		params.put("row", rows);

		PageVO pageVO = new PageVO();
		pageVO.setList(queryDao.queryMap(sqlQuery, params));
		Number total = queryDao.query(sqlTotal);
		if (total != null) {
			pageVO.setTotal(total.intValue());
			int isLast = total.intValue() > page * rows ? 1 : 0;
			pageVO.setIsLast(isLast);
			int totalPage = (int) Math.ceil((double) total.intValue() / rows);
			pageVO.setTotalPage(totalPage == 0 ? 1 : totalPage);
		}
		pageVO.setCurrentPage(page);

		return pageVO;
	}

	/**
	 * 删除排位赛
	 */
	public int delMatchRank(Long taskId) {
		try {
			taskMatchRankDao.delete(taskId);
			return 0;
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}

	/**
	 * 赛事排行榜
	 */
	public PageVO rankingList(Integer page, Integer rows, String taskId) {

		String sqlQuery = SqlJoiner.join(
				"select @rank \\:=@rank + 1 AS rank,A.* from(select pui.id,pui.nickname,play_times,",
				"(select cumulation_count from pc_task_match_rank_record where user_id=ptmrr.user_id and task_id=:taskId) cumulation_count,",
				"(select ifnull(sum(fee_amount),0) from pc_task_match_award where task_id=:taskId and user_id=ptmrr.user_id and task_type=1) awards,",
				"ptmr.fee_type,(select status from pc_task_match_enter where task_id=:taskId and user_id=pui.id and task_type=1) status ",
				"from pc_task_match_rank_record ptmrr LEFT JOIN pc_user_info pui on ptmrr.user_id=pui.id LEFT JOIN pc_task_match_rank ptmr ",
				"on ptmrr.task_id=ptmr.id where task_id=:taskId  ");
		String sqlTotal = "select count(1) from pc_task_match_rank_record  where task_id=" + taskId;
		Map<String, Object> params = Maps.newHashMap();
		if (page < 1) {
			page = 1;
		}
		sqlQuery = SqlJoiner.join(sqlQuery,
				" ORDER BY cumulation_count desc limit :page, :row)A,(SELECT @rank \\:= 0) B");

		if (rows == null || rows == 0) {
			rows = PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		}
		params.put("page", (page - 1) * rows);
		params.put("row", rows);
		params.put("taskId", taskId);

		PageVO pageVO = new PageVO();
		pageVO.setList(queryDao.queryMap(sqlQuery, params));
		Number total = queryDao.query(sqlTotal);
		if (total != null) {
			pageVO.setTotal(total.intValue());
			int isLast = total.intValue() > page * rows ? 1 : 0;
			pageVO.setIsLast(isLast);
			int totalPage = (int) Math.ceil((double) total.intValue() / rows);
			pageVO.setTotalPage(totalPage == 0 ? 1 : totalPage);
		}
		pageVO.setCurrentPage(page);

		return pageVO;
	}

	/**
	 * 新建排行榜赛
	 */
	@Transactional
	public int addRankTask(TaskMatchRank taskMatchRank, String limitHeroIds, String prizes) {

		taskMatchRank.setCreateDate(new Date());
		taskMatchRank.setStatus(Byte.parseByte("-1"));//未启用
		taskMatchRank = taskMatchRankDao.save(taskMatchRank);

		if (limitHeroIds != null && !limitHeroIds.equals("")) {
			//限定英雄
			String[] limitHeroIdArr = limitHeroIds.split(",");
			List<TaskMatchLimitHero> taskMatchLimitHeroList = new LinkedList<TaskMatchLimitHero>();
			for (String limitHeroId : limitHeroIdArr) {
				TaskMatchLimitHero taskMatchLimitHero = new TaskMatchLimitHero();
				taskMatchLimitHero.setTaskId(taskMatchRank.getId());
				taskMatchLimitHero.setTaskType(1);
				taskMatchLimitHero.setLolHeroId(Long.parseLong(limitHeroId));
				taskMatchLimitHero.setIsValid(CommonConstant.INT_BOOLEAN_TRUE);
				taskMatchLimitHero.setCreateDate(new Date());
				taskMatchLimitHeroList.add(taskMatchLimitHero);
			}
			taskMatchLimitHeroDao.save(taskMatchLimitHeroList);
		}

		if (prizes != null && !prizes.equals("")) {
			//设置奖项
			String[] prizesArr = prizes.split(";");
			List<TaskMatchPrize> taskMatchPrizeList = new LinkedList<TaskMatchPrize>();
			for (String prize : prizesArr) {
				TaskMatchPrize taskMatchPrize = new TaskMatchPrize();
				taskMatchPrize.setTaskId(taskMatchRank.getId());
				String[] prizeArr = prize.split(",");
				taskMatchPrize.setAwardLevel(Byte.parseByte(prizeArr[0]));
				taskMatchPrize.setRankStart(Integer.parseInt(prizeArr[1]));
				taskMatchPrize.setRankEnd(Integer.parseInt(prizeArr[2]));
				taskMatchPrize.setAwardAmount(Integer.parseInt(prizeArr[3]));
				taskMatchPrize.setAwardType(Byte.parseByte(prizeArr[4]));
				taskMatchPrize.setIsValid(Byte.valueOf("1"));
				taskMatchPrize.setCreateDate(new Date());
				taskMatchPrizeList.add(taskMatchPrize);
			}
			taskMatchPrizeDao.save(taskMatchPrizeList);
		}

		return 0;
	}

	/**
	 * 编辑排行榜赛
	 */
	@Transactional
	public int editRankTask(TaskMatchRank taskMatchRank, String limitHeroIds, String prizes) {

		TaskMatchRank findTaskMatchRank = taskMatchRankDao.findOne(taskMatchRank.getId());

		if (findTaskMatchRank != null) {

			if (taskMatchRank.getName() != null) {
				findTaskMatchRank.setName(taskMatchRank.getName());
			}
			if (taskMatchRank.getEnterDate() != null) {
				findTaskMatchRank.setEnterDate(taskMatchRank.getEnterDate());
			}
			if (taskMatchRank.getStartDate() != null) {
				findTaskMatchRank.setStartDate(taskMatchRank.getStartDate());
			}
			if (taskMatchRank.getEndDate() != null) {
				findTaskMatchRank.setEndDate(taskMatchRank.getEndDate());
			}
			if (taskMatchRank.getAwardRule() != null) {
				findTaskMatchRank.setAwardRule(taskMatchRank.getAwardRule());
			}
			if (taskMatchRank.getGameRule() != null) {
				findTaskMatchRank.setGameRule(taskMatchRank.getGameRule());
			}
			if (taskMatchRank.getTaskExplain() != null) {
				findTaskMatchRank.setTaskExplain(taskMatchRank.getTaskExplain());
			}
			if (taskMatchRank.getConditionExplain() != null) {
				findTaskMatchRank.setConditionExplain(taskMatchRank.getConditionExplain());
			}
			if (taskMatchRank.getFeeType() != null) {
				findTaskMatchRank.setFeeType(taskMatchRank.getFeeType());
			}
			if (taskMatchRank.getFeeAmount() != null) {
				findTaskMatchRank.setFeeAmount(taskMatchRank.getFeeAmount());
			}
			if (taskMatchRank.getType() != null) {
				findTaskMatchRank.setType(taskMatchRank.getType());
			}
			if (taskMatchRank.getLimitTimes() != null) {
				findTaskMatchRank.setLimitTimes(taskMatchRank.getLimitTimes());
			}
			if (taskMatchRank.getTotalAwardType() != null) {
				findTaskMatchRank.setTotalAwardType(taskMatchRank.getTotalAwardType());
			}
			if (taskMatchRank.getTotalAward() != null) {
				findTaskMatchRank.setTotalAward(taskMatchRank.getTotalAward());
			}
			if (taskMatchRank.getLabels() != null) {
				findTaskMatchRank.setLabels(taskMatchRank.getLabels());
			}
			if (taskMatchRank.getImgUrl() != null) {
				findTaskMatchRank.setImgUrl(taskMatchRank.getImgUrl());
			}
			taskMatchRankDao.save(findTaskMatchRank);
		}

		if (limitHeroIds != null) {
			//删除旧限定英雄
			taskMatchLimitHeroDao.deleteByTaskId(taskMatchRank.getId());
			if (!limitHeroIds.equals("")) {
				//限定英雄
				String[] limitHeroIdArr = limitHeroIds.split(",");
				List<TaskMatchLimitHero> taskMatchLimitHeroList = new LinkedList<TaskMatchLimitHero>();
				for (String limitHeroId : limitHeroIdArr) {
					TaskMatchLimitHero taskMatchLimitHero = new TaskMatchLimitHero();
					taskMatchLimitHero.setTaskId(taskMatchRank.getId());
					taskMatchLimitHero.setTaskType(1);
					taskMatchLimitHero.setLolHeroId(Long.parseLong(limitHeroId));
					taskMatchLimitHero.setIsValid(CommonConstant.INT_BOOLEAN_TRUE);
					taskMatchLimitHero.setCreateDate(new Date());
					taskMatchLimitHeroList.add(taskMatchLimitHero);
				}
				taskMatchLimitHeroDao.save(taskMatchLimitHeroList);
			}
		}

		if (prizes != null && !prizes.equals("")) {
			//删除旧设置奖项
			taskMatchPrizeDao.deleteByTaskId(taskMatchRank.getId());
			//设置奖项
			String[] prizesArr = prizes.split(";");
			List<TaskMatchPrize> taskMatchPrizeList = new LinkedList<TaskMatchPrize>();
			for (String prize : prizesArr) {
				TaskMatchPrize taskMatchPrize = new TaskMatchPrize();
				taskMatchPrize.setTaskId(taskMatchRank.getId());
				String[] prizeArr = prize.split(",");
				taskMatchPrize.setAwardLevel(Byte.parseByte(prizeArr[0]));
				taskMatchPrize.setRankStart(Integer.parseInt(prizeArr[1]));
				taskMatchPrize.setRankEnd(Integer.parseInt(prizeArr[2]));
				taskMatchPrize.setAwardAmount(Integer.parseInt(prizeArr[3]));
				taskMatchPrize.setAwardType(Byte.parseByte(prizeArr[4]));
				taskMatchPrize.setIsValid(Byte.valueOf("1"));
				taskMatchPrize.setCreateDate(new Date());
				taskMatchPrizeList.add(taskMatchPrize); //
			}
			taskMatchPrizeDao.save(taskMatchPrizeList);
		}

		return 0;
	}

	/**
	 * 获取排位赛
	 */
	public Map<String, Object> getMatchRank(Long taskId) {
		Map<String, Object> result = new HashMap<String, Object>();
		TaskMatchRank taskMatchRank = taskMatchRankDao.findOne(taskId);
		result.put("matchRank", taskMatchRank);

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("taskId", taskId);
		Map<String, Object> prizes = queryDao.querySingleMap(
				"select group_concat(concat_ws(',',award_level,rank_start,rank_end,award_amount,award_type) Separator ';') prizes "
						+ "from pc_task_match_prize where task_id=:taskId and is_valid=1 order by award_level desc",
				params);
		Map<String, Object> limitHero = queryDao.querySingleMap(
				"select group_concat(lol_hero_id Separator ',') limitHero from pc_task_match_limit_hero "
						+ "where task_id=:taskId and is_valid=1 order by id asc",
				params);
		result.putAll(limitHero);
		result.putAll(prizes);
		return result;
	}

	/**
	 * 编辑战绩
	 */
	@Transactional
	public int editMilitaryExploit(Long userId, Long taskId, Integer playTimes, Integer cumulativeKill) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("userId", userId);
		params.put("taskId", taskId);
		params.put("playTimes", playTimes);
		try {
			queryDao.update(
					"update pc_task_match_enter set play_times=:playTimes where user_id=:userId and task_id=:taskId",
					params);

			params.remove("playTimes");
			params.put("cumulationCount", cumulativeKill);

			queryDao.update(
					"update pc_task_match_rank_record set cumulation_count=:cumulationCount where user_id=:userId "
							+ "and task_id=:taskId",
					params);
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}

		return 0;
	}

	/**
	 * 消息记录
	 */
	public Map<String, Object> insertMsg(Long userId, String msg) {
		Map<String, Object> result = new HashMap<String, Object>();
		CompetitionMsg competitionMsg = new CompetitionMsg();
		competitionMsg.setTitle("站内消息");
		competitionMsg.setMsgInfo(msg);
		competitionMsg.setObjectType(Byte.valueOf("1"));
		competitionMsg.setObject(userId);
		competitionMsg.setPushUser(new Long(1));
		competitionMsg.setIsValid(Byte.valueOf("1"));
		competitionMsg.setCreateDate(new Date());
		competitionMsgDao.save(competitionMsg);
		return result;
	}

	/**
	 * 获取发奖信息
	 */
	public Map<String, Object> getAwardInfo(Long userId) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("userId", userId);
		return queryDao.querySingleMap("select alipay,phone from pc_task_match_award where user_id=:userId", params);
	}

	/**
	 * 完成审核
	 */
	public int audit(Long taskId) {
		TaskMatchRank taskMatchRank = taskMatchRankDao.findOne(taskId);
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("taskId", taskId);
		List<Map<String, Object>> enterResultList = queryDao.queryMap(
				"select user_id from pc_task_match_enter where task_id=:taskId and is_valid=1 and status=1", params);
		for (Map<String, Object> map : enterResultList) {
			try {
				saveFeeRecord(Long.valueOf(map.get("user_id").toString()), taskMatchRank.getFeeType(),
						taskMatchRank.getFeeAmount() == null ? 0 : taskMatchRank.getFeeAmount().intValue(),
						Byte.valueOf("2"), taskId, Byte.valueOf("1"));
			} catch (Exception e) {
				e.printStackTrace();
				LOGGER.info("UserId {} 结算错误！", map.get("user_id").toString());
				continue;
			}
		}
		return queryDao.update("update pc_task_match_rank set status=4 where id=" + taskId);
	}

	/**
	 * 取消资格
	 */
	public int cancelQualification(Long taskId, Long userId) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("taskId", taskId);
		params.put("userId", userId);
		return queryDao.update("update pc_task_match_enter set status=0 where user_id=:userId and task_id=:taskId",
				params);
	}

	/**
	 * 启用排位赛
	 */
	public int enable(Long taskId) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("taskId", taskId);
		return queryDao.update("update pc_task_match_rank set status=0 where id=:taskId", params);
	}

	/**
	 * 判断玩家的账户money(积分或娱币)是否充足, 充足则直接扣费并生成费用历史记录
	 * @param feeType       费用类型：0-免费，1-积分，2-娱币，3-人民币
	 * @param feeAmount     费用值
	 * @param recordType    费用记录类型：1-报名，2-奖励，3-退费，4-更新主题任务
	 * @param taskId        任务赛ID：当recordType=4时，该字段为0
	 * @param taskType      任务赛类型：1-排行榜，2-主题
	 * @param tradeNo		订单号
	 */
	public boolean saveFeeRecord(Long userId, Byte feeType, int feeAmount, Byte recordType, Long taskId,
			Byte taskType) {
		if (TaskMatchConstant.FEE_TYPE_FREE.equals(feeType)) {
			return true;
		}

		byte direction;
		if (TaskMatchConstant.TASK_MATCH_RECORD_TYPE_AWARD.equals(recordType)
				|| TaskMatchConstant.TASK_MATCH_RECORD_TYPE_RETURN.equals(recordType)) {
			direction = 1;
		} else {
			return false;
		}

		String tradeNo = TradeNoUtil.getTradeNoByModuleAndTypeAndUserId(TaskMatchConstant.TASK_MATCH_TYPE_RANK,
				recordType, userId);

		// 保存任务赛费用记录
		TaskMatchFee taskMatchFee = new TaskMatchFee();
		taskMatchFee.setUserId(userId);
		taskMatchFee.setRecordType(recordType);
		taskMatchFee.setFeeType(feeType);
		taskMatchFee.setFeeAmount(feeAmount);
		taskMatchFee.setTaskId(taskId);
		taskMatchFee.setTaskType(taskType);
		taskMatchFee.setTradeNo(tradeNo);
		taskMatchFee.setIsValid((byte) 1);
		taskMatchFee.setCreateDate(new Date());
		taskMatchFeeDao.save(taskMatchFee);

		//保存任务赛award记录
		TaskMatchAward taskMatchAward = new TaskMatchAward();
		taskMatchAward.setUserId(userId);
		taskMatchAward.setTaskId(taskId);
		taskMatchAward.setTaskType(taskType);
		taskMatchAward.setFeeType(feeType);
		taskMatchAward.setFeeAmount(feeAmount);
		taskMatchAward.setTradeNo(tradeNo);
		taskMatchAward.setStatus(Byte.valueOf("2"));
		taskMatchAward.setIsValid(Byte.valueOf("1"));
		taskMatchAward.setCreateDate(new Date());
		taskMatchAwardDao.save(taskMatchAward);

		return userExtendService.saveCoinOrChipHistory(userId, feeType, feeAmount, direction, tradeNo);
	}

	public List<TaskMatchRank> findByStatus() {
		String sql = "select * from pc_task_match_rank where  status BETWEEN 0 and 2 and is_valid=1";
		return queryDao.queryObject(sql, TaskMatchRank.class);
	}

	public void save(List<TaskMatchRank> list) {
		taskMatchRankDao.save(list);
	}

	public List<Map<String, Object>> queryInfoForAppRecommend(String startDate, String endDate) {
		String sql = "select id,name title from pc_task_match_rank where is_valid=1 and status>=0 and create_date>'"
				+ startDate + "' and create_date <'" + endDate + "' order by create_date desc";
		return queryDao.queryMap(sql);

	}

}
