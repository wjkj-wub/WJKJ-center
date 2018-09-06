package com.miqtech.master.entity.activity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.miqtech.master.entity.IdEntity;
import com.miqtech.master.entity.user.UserInfo;

@Entity
@Table(name = "activity_t_info")
public class ActivityInfo extends IdEntity {
	private static final long serialVersionUID = 3319443927432465152L;
	private Long userId;// 用户ID
	private Long netbarId;// 网吧ID
	private String releaser;// 发布人
	private String title;// 标题
	private Integer score;// 评分
	private String remark;// 说明
	private Date beginTime;// 开赛时间
	private Date overTime;// 结束时间
	private Date blockTime;// 报名截止时间
	private Date startTime;
	private Date endTime;
	private Integer peopleNum;// 报名人数
	private String icon;// 缩略图
	private String iconMedia;
	private String iconThumb;
	private String cover;// 首图
	private String coverMedia;
	private String coverThumb;
	private String netbars;// 参赛的网吧，多个网吧之间用","隔开
	private String areas;// 比赛的地区，多个地区之间用","隔开
	private Integer isGround;// 是否上架：0-否，1-是
	private Integer inWx;// 是否在微信中显示：0-否，1-是
	private Integer mobileRequired;
	private Integer idcardRequired;
	private Integer nicknameRequired;
	private Integer qqRequired;
	private Integer laborRequired;
	private Integer itemId;
	private Integer roundCount;
	private String areaCode;// 归属地区
	private String summary;// 摘要
	private Integer personAllow;// 允许个人报名
	private Integer teamAllow;// 允许战队报名
	private String spoils;// 战利品
	private Integer sortNum;// 排序序号
	private Integer recommendSign;//0首页竞技大厅都已推荐1首页已推荐2竞技大厅已推荐
	private Integer way;// 方式: 1-线下, 2-线上
	private String qrcode;//赛事二维码

	private List<UserInfo> userList;// 参赛用户
	private List<ActivityRound> rounds;// 场次信息

	@Column(name = "user_id")
	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	@Column(name = "netbar_id")
	public Long getNetbarId() {
		return netbarId;
	}

	public void setNetbarId(Long netbarId) {
		this.netbarId = netbarId;
	}

	@Column(name = "title")
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Column(name = "score")
	public Integer getScore() {
		return score;
	}

	public void setScore(Integer score) {
		this.score = score;
	}

	@Column(name = "remark")
	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
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

	@Column(name = "is_ground")
	public Integer getIsGround() {
		return isGround;
	}

	public void setIsGround(Integer isGround) {
		this.isGround = isGround;
	}

	@Column(name = "netbars")
	public String getNetbars() {
		return netbars;
	}

	public void setNetbars(String netbars) {
		this.netbars = netbars;
	}

	@Column(name = "releaser")
	public String getReleaser() {
		return releaser;
	}

	public void setReleaser(String releaser) {
		this.releaser = releaser;
	}

	@Column(name = "areas")
	public String getAreas() {
		return areas;
	}

	public void setAreas(String areas) {
		this.areas = areas;
	}

	@Transient
	public List<UserInfo> getUserList() {
		return userList;
	}

	public void setUserList(List<UserInfo> userList) {
		this.userList = userList;
	}

	@Column(name = "mobile_required")
	public Integer getMobileRequired() {
		return mobileRequired;
	}

	public void setMobileRequired(Integer mobileRequired) {
		this.mobileRequired = mobileRequired;
	}

	@Column(name = "idcard_required")
	public Integer getIdcardRequired() {
		return idcardRequired;
	}

	public void setIdcardRequired(Integer idcardRequired) {
		this.idcardRequired = idcardRequired;
	}

	@Column(name = "nickname_required")
	public Integer getNicknameRequired() {
		return nicknameRequired;
	}

	public void setNicknameRequired(Integer nicknameRequired) {
		this.nicknameRequired = nicknameRequired;
	}

	@Column(name = "qq_required")
	public Integer getQqRequired() {
		return qqRequired;
	}

	public void setQqRequired(Integer qqRequired) {
		this.qqRequired = qqRequired;
	}

	@Column(name = "item_id")
	public Integer getItemId() {
		return itemId;
	}

	public void setItemId(Integer itemId) {
		this.itemId = itemId;
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

	@Column(name = "round_count")
	public Integer getRoundCount() {
		return roundCount;
	}

	public void setRoundCount(Integer roundCount) {
		this.roundCount = roundCount;
	}

	@Transient
	public List<ActivityRound> getRounds() {
		return rounds;
	}

	public void setRounds(List<ActivityRound> rounds) {
		this.rounds = rounds;
	}

	/**
	 * 添加一场场次信息
	 */
	public List<ActivityRound> addRound(ActivityRound round) {
		// 初始化场次信息
		if (this.rounds == null) {
			this.rounds = new ArrayList<ActivityRound>();
		}

		// 添加内容并返回最新信息
		this.rounds.add(round);
		return getRounds();
	}

	@Column(name = "in_wx")
	public Integer getInWx() {
		return inWx;
	}

	public void setInWx(Integer inWx) {
		this.inWx = inWx;
	}

	@Column(name = "labor_required")
	public Integer getLaborRequired() {
		return laborRequired;
	}

	public void setLaborRequired(Integer laborRequired) {
		this.laborRequired = laborRequired;
	}

	@Column(name = "area_code")
	public String getAreaCode() {
		return areaCode;
	}

	public void setAreaCode(String areaCode) {
		this.areaCode = areaCode;
	}

	@Column(name = "summary")
	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	@Column(name = "person_allow")
	public Integer getPersonAllow() {
		return personAllow;
	}

	public void setPersonAllow(Integer personAllow) {
		this.personAllow = personAllow;
	}

	@Column(name = "team_allow")
	public Integer getTeamAllow() {
		return teamAllow;
	}

	public void setTeamAllow(Integer teamAllow) {
		this.teamAllow = teamAllow;
	}

	@Column(name = "spoils")
	public String getSpoils() {
		return spoils;
	}

	public void setSpoils(String spoils) {
		this.spoils = spoils;
	}

	@Column(name = "sort_num")
	public Integer getSortNum() {
		return sortNum;
	}

	public void setSortNum(Integer sortNum) {
		this.sortNum = sortNum;
	}

	@Column(name = "recommend_sign")
	public Integer getRecommendSign() {
		return recommendSign;
	}

	public void setRecommendSign(Integer recommendSign) {
		this.recommendSign = recommendSign;
	}

	public Integer getWay() {
		return way;
	}

	public void setWay(Integer way) {
		this.way = way;
	}

	@Column(name = "qrcode")
	public String getQrcode() {
		return qrcode;
	}

	public void setQrcode(String qrcode) {
		this.qrcode = qrcode;
	}

}