package com.miqtech.master.entity.pc.taskmatch;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * 主题任务赛表
 *
 * @author zhangyuqi
 * @create 2017年08月30日
 */
@Entity
@Table(name = "pc_task_match_theme")
public class TaskMatchTheme implements Serializable {
	private static final long serialVersionUID = 0L;
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	@Column(name = "name")
	private String name; // 任务赛名称
	@Column(name = "img_url")
	private String imgUrl; // 任务赛图片地址
	@Column(name = "total_award_type")
	private Byte totalAwardType; // 奖励类型：1-积分，2-娱币，3-人民币
	@Column(name = "total_award")
	private Integer totalAward; // 总奖金池
	@Column(name = "fee_type")
	private Byte feeType; // 费用类型：0-免费，1-积分，2-娱币，3-人民币
	@Column(name = "fee_amount")
	private Integer feeAmount; // 费用值
	@Column(name = "limit_times")
	private Integer limitTimes; // 限定场次
	@Column(name = "difficulty")
	private Byte difficulty; // 难度系数：1-一星，2-二星，3-三星，4-四星，5-五星
	@Column(name = "game_rule")
	private String gameRule; // 游戏规则
	@Column(name = "begin_date")
	private Date beginDate; // 开始时间
	@Column(name = "end_date")
	private Date endDate; // 结束时间
	@Column(name = "status")
	private Byte status; // 任务状态：1-进行中，2-已结束
	@Column(name = "is_valid")
	private Integer isValid;
	@Column(name = "create_date")
	private Date createDate;
	@Column(name = "create_user_id")
	private Long createUserId;
	@Column(name = "update_date")
	private Date updateDate;
	@Column(name = "is_release")
	private Byte isRelease;
	@Column(name = "type")
	private Integer type;

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

	public Integer getLimitTimes() {
		return limitTimes;
	}

	public void setLimitTimes(Integer limitTimes) {
		this.limitTimes = limitTimes;
	}

	public Byte getDifficulty() {
		return difficulty;
	}

	public void setDifficulty(Byte difficulty) {
		this.difficulty = difficulty;
	}

	public String getGameRule() {
		return gameRule;
	}

	public void setGameRule(String gameRule) {
		this.gameRule = gameRule;
	}

	public Date getBeginDate() {
		return beginDate;
	}

	public void setBeginDate(Date beginDate) {
		this.beginDate = beginDate;
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

	public Integer getIsValid() {
		return isValid;
	}

	public void setIsValid(Integer isValid) {
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

	public Byte getIsRelease() {
		return isRelease;
	}

	public void setIsRelease(Byte isRelease) {
		this.isRelease = isRelease;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}
}
