package com.miqtech.master.entity.activity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "activity_t_matches")
public class ActivityMatch extends IdEntity {
	private static final long serialVersionUID = 3729125953568012324L;
	private Long userId;// 发布人
	private Long itemId;// 项目ID
	private String icon;// 缩略图
	private String iconMedia;
	private String iconThumb;
	private String title;// 标题
	private String cover;// 封面
	private String coverMedia;
	private String coverThumb;
	private Date beginTime;// 开始时间
	private Date overTime;// 结束时间
	private Date blockTime;// 报名截止时间
	private Integer peopleNum;// 报名人数
	private String rule;// 胜负规则
	private String server;// 服务器
	private Integer way;// 方式：1-线下；2-线上；
	private String address;// 地点
	private String spoils;// 战利品
	private String remark;// 说明
	private Integer isStart;// 是否开始：1-是；0-否；
	private Long netbarId;// 网吧ID
	private Integer byMerchant;// 是否为业主发布:1-是,0-否

	@Column(name = "user_id")
	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
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

	@Column(name = "icon_media")
	public String getIconMedia() {
		return iconMedia;
	}

	public void setIconMedia(String iconMedia) {
		this.iconMedia = iconMedia;
	}

	@Column(name = "icon_thumb")
	public String getIconThumb() {
		return iconThumb;
	}

	public void setIconThumb(String iconThumb) {
		this.iconThumb = iconThumb;
	}

	@Column(name = "title")
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Column(name = "cover")
	public String getCover() {
		return cover;
	}

	public void setCover(String cover) {
		this.cover = cover;
	}

	@Column(name = "cover_media")
	public String getCoverMedia() {
		return coverMedia;
	}

	public void setCoverMedia(String coverMedia) {
		this.coverMedia = coverMedia;
	}

	@Column(name = "cover_thumb")
	public String getCoverThumb() {
		return coverThumb;
	}

	public void setCoverThumb(String coverThumb) {
		this.coverThumb = coverThumb;
	}

	@Column(name = "begin_time")
	public Date getBeginTime() {
		return beginTime;
	}

	public void setBeginTime(Date beginTime) {
		this.beginTime = beginTime;
	}

	@Column(name = "over_time")
	public Date getOverTime() {
		return overTime;
	}

	public void setOverTime(Date overTime) {
		this.overTime = overTime;
	}

	@Column(name = "block_time")
	public Date getBlockTime() {
		return blockTime;
	}

	public void setBlockTime(Date blockTime) {
		this.blockTime = blockTime;
	}

	@Column(name = "people_num")
	public Integer getPeopleNum() {
		return peopleNum;
	}

	public void setPeopleNum(Integer peopleNum) {
		this.peopleNum = peopleNum;
	}

	@Column(name = "rule")
	public String getRule() {
		return rule;
	}

	public void setRule(String rule) {
		this.rule = rule;
	}

	@Column(name = "server")
	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	@Column(name = "address")
	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	@Column(name = "spoils")
	public String getSpoils() {
		return spoils;
	}

	public void setSpoils(String spoils) {
		this.spoils = spoils;
	}

	@Column(name = "remark")
	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	@Column(name = "is_start")
	public Integer getIsStart() {
		return isStart;
	}

	public void setIsStart(Integer isStart) {
		this.isStart = isStart;
	}

	@Column(name = "way")
	public Integer getWay() {
		return way;
	}

	public void setWay(Integer way) {
		this.way = way;
	}

	@Column(name = "netbar_id")
	public Long getNetbarId() {
		return netbarId;
	}

	public void setNetbarId(Long netbarId) {
		this.netbarId = netbarId;
	}

	@Column(name = "by_merchant")
	public Integer getByMerchant() {
		return byMerchant;
	}

	public void setByMerchant(Integer byMerchant) {
		this.byMerchant = byMerchant;
	}

}
