package com.miqtech.master.entity.activity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "activity_t_over_activities")
public class ActivityOverActivity extends IdEntity {
	private static final long serialVersionUID = -7827985766139522055L;
	private Long netbarId;// 网吧ID
	private Long activityId;// 赛事ID
	private String title;// 标题
	private String icon;// 缩略图
	private String iconMedia;
	private String iconThumb;
	private String cover;// 首图
	private String coverMedia;
	private String coverThumb;
	private String brief;// 简介
	private String remark;// 说明
	private Date beginTime;// 开始时间
	private Date overTime;// 结束时间
	private Long pId;//所属专题ID
	private Integer isHot;//是否热门
	private Integer isShow;//是否在App显示
	private Integer isTop;// 是否置顶:0-否,1-是
	private Integer isSubject;//是否为专题
	private Integer readNum = 1;//阅读量,默认为1
	private Integer praise = 0; //点赞数量默认是0;
	private Integer type;//资讯类型 1图文 2专题 3 图集
	private String source;// 文章来源
	private Integer isBannerShow;//资讯首页是否显示
	private Integer sort;//排序,越大越优先
	private Date timerDate;
	private String creater;// 创建人
	private Date bannerTimerDate;//banner生效时间
	private String bannerTitle;//banner标题
	private String bannerIcon;//banner图片
	private Integer bannerValid;//banner 状态
	private Date topDate;// 指定时间
	private String videoUrl;// 视频地址
	private String keyword;// 关键词
	private Integer orderNum;
	private Date bannerCreateDate;//banner创建时间
	private String videoCoverImgs;
	private Integer isPublished;//是否草稿状态
	private Integer isAd;//是否是广告资讯

	@Column(name = "is_ad")
	public Integer getIsAd() {
		return isAd;
	}

	public void setIsAd(Integer isAd) {
		this.isAd = isAd;
	}

	private Integer audition;//0非华体赛事 1 华体赛事

	@Column(name = "is_audition")
	public Integer getAudition() {
		return audition;
	}

	public void setAudition(Integer audition) {
		this.audition = audition;
	}

	@Column(name = "banner_create_date")
	public Date getBannerCreateDate() {
		return bannerCreateDate;
	}

	public void setBannerCreateDate(Date bannerCreateDate) {
		this.bannerCreateDate = bannerCreateDate;
	}

	@Column(name = "banner_sort")
	public Integer getOrderNum() {
		return orderNum;
	}

	public void setOrderNum(Integer orderNum) {
		this.orderNum = orderNum;
	}

	@Column(name = "banner_valid")
	public Integer getBannerValid() {
		return bannerValid;
	}

	public void setBannerValid(Integer bannerValid) {
		this.bannerValid = bannerValid;
	}

	@Column(name = "netbar_id")
	public Long getNetbarId() {
		return netbarId;
	}

	public void setNetbarId(Long netbarId) {
		this.netbarId = netbarId;
	}

	@Column(name = "activity_id")
	public Long getActivityId() {
		return activityId;
	}

	public void setActivityId(Long activityId) {
		this.activityId = activityId;
	}

	@Column(name = "title")
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
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

	@Column(name = "brief")
	public String getBrief() {
		return brief;
	}

	public void setBrief(String brief) {
		this.brief = brief;
	}

	@Column(name = "pid")
	public Long getpId() {
		return pId;
	}

	public void setpId(Long pId) {
		this.pId = pId;
	}

	@Column(name = "is_hot")
	public Integer getIsHot() {
		return isHot;
	}

	public void setIsHot(Integer isHot) {
		this.isHot = isHot;
	}

	@Column(name = "is_show")
	public Integer getIsShow() {
		return isShow;
	}

	public void setIsShow(Integer isShow) {
		this.isShow = isShow;
	}

	@Column(name = "is_subject")
	public Integer getIsSubject() {
		return isSubject;
	}

	public void setIsSubject(Integer isSubject) {
		this.isSubject = isSubject;
	}

	@Column(name = "read_num")
	public Integer getReadNum() {
		return readNum;
	}

	public void setReadNum(Integer readNum) {
		this.readNum = readNum;
	}

	@Column(name = "praise")
	public Integer getPraise() {
		return praise;
	}

	public void setPraise(Integer praise) {
		this.praise = praise;
	}

	@Column(name = "type")
	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	@Column(name = "source")
	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	@Column(name = "is_banner_show")
	public Integer getIsBannerShow() {
		return isBannerShow;
	}

	public void setIsBannerShow(Integer isBannerShow) {
		this.isBannerShow = isBannerShow;
	}

	@Column(name = "sort")
	public Integer getSort() {
		return sort;
	}

	public void setSort(Integer sort) {
		this.sort = sort;
	}

	@Column(name = "timer_date")
	public Date getTimerDate() {
		return timerDate;
	}

	public void setTimerDate(Date timerDate) {
		this.timerDate = timerDate;
	}

	@Column(name = "creater")
	public String getCreater() {
		return creater;
	}

	public void setCreater(String creater) {
		this.creater = creater;
	}

	@Column(name = "banner_timer_date")
	public Date getBannerTimerDate() {
		return bannerTimerDate;
	}

	public void setBannerTimerDate(Date bannerTimerDate) {
		this.bannerTimerDate = bannerTimerDate;
	}

	@Column(name = "banner_title")
	public String getBannerTitle() {
		return bannerTitle;
	}

	public void setBannerTitle(String bannerTitle) {
		this.bannerTitle = bannerTitle;
	}

	@Column(name = "banner_icon")
	public String getBannerIcon() {
		return bannerIcon;
	}

	public void setBannerIcon(String bannerIcon) {
		this.bannerIcon = bannerIcon;
	}

	@Column(name = "is_top")
	public Integer getIsTop() {
		return isTop;
	}

	public void setIsTop(Integer isTop) {
		this.isTop = isTop;
	}

	@Column(name = "top_date")
	public Date getTopDate() {
		return topDate;
	}

	public void setTopDate(Date topDate) {
		this.topDate = topDate;
	}

	@Column(name = "video_url")
	public String getVideoUrl() {
		return videoUrl;
	}

	public void setVideoUrl(String videoUrl) {
		this.videoUrl = videoUrl;
	}

	@Column(name = "keyword")
	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	@Column(name = "video_cover_imgs")
	public String getVideoCoverImgs() {
		return videoCoverImgs;
	}

	public void setVideoCoverImgs(String videoCoverImgs) {
		this.videoCoverImgs = videoCoverImgs;
	}

	@Column(name = "is_published")
	public Integer getIsPublished() {
		return isPublished;
	}

	public void setIsPublished(Integer isPublished) {
		this.isPublished = isPublished;
	}

}
