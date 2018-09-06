package com.miqtech.master.entity.pc.taskmatch;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * 排行榜任务赛表
 *
 * @author gaohanlin
 * @create 2017年08月30日
 */
@Entity
@Table(name = "pc_task_match_rank")
public class TaskMatchRank implements Serializable {
	private static final long serialVersionUID = -8136178479053060111L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private Long id;
	@Column(name = "name")
	private String name; // 任务赛名称
	@Column(name = "img_url")
	private String imgUrl; // 任务赛图片地址
	@Column(name = "total_award_type")
	private Byte totalAwardType; // 总奖池奖励类型：1-积分，2-娱币，3-人民币
	@Column(name = "total_award")
	private Integer totalAward; // 总奖金池
	@Column(name = "fee_type")
	private Byte feeType; // 费用类型：1-积分，2-娱币，3-人民币
	@Column(name = "fee_amount")
	private Integer feeAmount; // 费用数量
	@Column(name = "type")
	private Integer type; // 任务类型：1-连胜数，2-杀人数，3-助攻数，4-补刀数，5-金钱数
	@Column(name = "labels")
	private String labels; // 标签
	@Column(name = "limit_times")
	private Integer limitTimes; // 限定场次
	@Column(name = "task_explain")
	private String taskExplain; // 任务说明
	@Column(name = "condition_explain")
	private String conditionExplain; // 条件说明
	@Column(name = "game_rule")
	private String gameRule; // 游戏规则
	@Column(name = "award_rule")
	private String awardRule; // 奖励规则
	@Column(name = "enter_date")
	private Date enterDate; // 报名时间
	@Column(name = "start_date")
	private Date startDate; // 开始时间
	@Column(name = "end_date")
	private Date endDate; // 结束时间
	@Column(name = "status")
	private Byte status; // 任务赛状态：0-等待报名，1-报名中，2-进行中，3-结算中、4-已结束
	@Column(name = "is_valid")
	private Byte isValid;
	@Column(name = "create_date") //创建时间
	private Date createDate;
	@Column(name = "create_user_id")
	private Long createUserId;
	@Column(name = "update_date") //更新时间
	private Date updateDate;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getImgUrl() {
		return imgUrl;
	}

	public void setImgUrl(String imgUrl) {
		this.imgUrl = imgUrl;
	}

	public Byte getTotalAwardType() {
		return totalAwardType;
	}

	public void setTotalAwardType(Byte totalAwardType) {
		this.totalAwardType = totalAwardType;
	}

	public Integer getTotalAward() {
		return totalAward;
	}

	public void setTotalAward(Integer totalAward) {
		this.totalAward = totalAward;
	}

	public Byte getFeeType() {
		return feeType;
	}

	public void setFeeType(Byte feeType) {
		this.feeType = feeType;
	}

	public Integer getFeeAmount() {
		return feeAmount;
	}

	public void setFeeAmount(Integer feeAmount) {
		this.feeAmount = feeAmount;
	}

	public Integer getType() {
		return type;
	}

	public String getLabels() {
		return labels;
	}

	public void setLabels(String labels) {
		this.labels = labels;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public Integer getLimitTimes() {
		return limitTimes;
	}

	public void setLimitTimes(Integer limitTimes) {
		this.limitTimes = limitTimes;
	}

	public String getTaskExplain() {
		return taskExplain;
	}

	public void setTaskExplain(String taskExplain) {
		this.taskExplain = taskExplain;
	}

	public String getConditionExplain() {
		return conditionExplain;
	}

	public void setConditionExplain(String conditionExplain) {
		this.conditionExplain = conditionExplain;
	}

	public String getGameRule() {
		return gameRule;
	}

	public void setGameRule(String gameRule) {
		this.gameRule = gameRule;
	}

	public String getAwardRule() {
		return awardRule;
	}

	public void setAwardRule(String awardRule) {
		this.awardRule = awardRule;
	}

	public Date getEnterDate() {
		return enterDate;
	}

	public void setEnterDate(Date enterDate) {
		this.enterDate = enterDate;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public Byte getStatus() {
		return status;
	}

	public void setStatus(Byte status) {
		this.status = status;
	}

	public Byte getIsValid() {
		return isValid;
	}

	public void setIsValid(Byte isValid) {
		this.isValid = isValid;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public Long getCreateUserId() {
		return createUserId;
	}

	public void setCreateUserId(Long createUserId) {
		this.createUserId = createUserId;
	}

	public Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}
}
