package com.miqtech.master.entity.bounty;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.miqtech.master.entity.IdEntity;

/**
 * 悬赏令
 */
@Entity
@Table(name = "bounty")
public class Bounty extends IdEntity {

	private static final long serialVersionUID = 4313727994388587275L;

	private String title;// 标题
	private String target;// 目标
	private Long itemId;// 游戏项目ID
	private String icon;// 缩略图
	private String cover;// 详情图
	private Integer type;// 类型:1-普通,2-独占鳌头,3-排行榜
	private Integer orderType;// 排序方式:1-正序,2-倒序
	private String rule;// 规则说明
	private String reward;// 奖励说明
	private Date startTime;// 开始时间
	private Date endTime;// 结束时间
	private Integer status;//审核结束：0-否,1-是
	private Integer isPublish;// 是否已发布:0-否,1-是
	private Integer prizeVirtualNum; // 调整中奖人数
	private Integer targetType;//目标类型
	private String itemIcon;
	private List<BountyPrize> prizes;

	@Column(name = "title")
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Column(name = "target")
	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	@Column(name = "item_id")
	public Long getItemId() {
		return itemId;
	}

	public void setItemId(Long itemId) {
		this.itemId = itemId;
	}

	@Column(name = "icon")
	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	@Column(name = "cover")
	public String getCover() {
		return cover;
	}

	public void setCover(String cover) {
		this.cover = cover;
	}

	@Column(name = "type")
	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	@Column(name = "order_type")
	public Integer getOrderType() {
		return orderType;
	}

	public void setOrderType(Integer orderType) {
		this.orderType = orderType;
	}

	@Column(name = "rule")
	public String getRule() {
		return rule;
	}

	public void setRule(String rule) {
		this.rule = rule;
	}

	@Column(name = "reward")
	public String getReward() {
		return reward;
	}

	public void setReward(String reward) {
		this.reward = reward;
	}

	@Column(name = "start_time")
	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	@Column(name = "end_time")
	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	@Column(name = "status")
	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	@Column(name = "is_publish")
	public Integer getIsPublish() {
		return isPublish;
	}

	public void setIsPublish(Integer isPublish) {
		this.isPublish = isPublish;
	}

	@Transient
	public List<BountyPrize> getPrizes() {
		return prizes;
	}

	public void setPrizes(List<BountyPrize> prizes) {
		this.prizes = prizes;
	}

	@Column(name = "prize_virtual_num")
	public Integer getPrizeVirtualNum() {
		return prizeVirtualNum;
	}

	public void setPrizeVirtualNum(Integer prizeVirtualNum) {
		this.prizeVirtualNum = prizeVirtualNum;
	}

	@Column(name = "target_type")
	public Integer getTargetType() {
		return targetType;
	}

	public void setTargetType(Integer targetType) {
		this.targetType = targetType;
	}

	@Column(name = "item_icon")
	public String getItemIcon() {
		return itemIcon;
	}

	public void setItemIcon(String itemIcon) {
		this.itemIcon = itemIcon;
	}

}
