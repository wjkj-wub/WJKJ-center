package com.miqtech.master.entity.game;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "game_t_info")
public class GameInfo extends IdEntity {
	private static final long serialVersionUID = -8553676869901175418L;
	private String name;// 名称
	private String version;// 版本
	private Integer score;// 评分
	private String intro;// 介绍
	private String urlAndroid;// 安卓下载地址
	private String urlIOS;// ios下载地址
	private String icon;// 缩略图
	private String iconMedia;
	private String iconThumb;
	private String cover;// 封面
	private String coverMedia;
	private String coverThumb;
	private Integer isRecommend;// 是否推荐：0-false,1-true
	private String desc;// 手游简介
	private String downloadCount;
	private String androidFileSize;
	private String iosFileSize;

	private List<GameImg> imgs;

	@Column(name = "score")
	public Integer getScore() {
		return score;
	}

	public void setScore(Integer score) {
		this.score = score;
	}

	@Column(name = "name")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "icon")
	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	@Column(name = "intro")
	public String getIntro() {
		return intro;
	}

	public void setIntro(String intro) {
		this.intro = intro;
	}

	@Column(name = "version")
	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	@Column(name = "url_android")
	public String getUrlAndroid() {
		return urlAndroid;
	}

	public void setUrlAndroid(String urlAndroid) {
		this.urlAndroid = urlAndroid;
	}

	@Column(name = "url_ios")
	public String getUrlIOS() {
		return urlIOS;
	}

	public void setUrlIOS(String urlIOS) {
		this.urlIOS = urlIOS;
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

	@Column(name = "is_recommend")
	public Integer getIsRecommend() {
		return isRecommend;
	}

	public void setIsRecommend(Integer isRecommend) {
		this.isRecommend = isRecommend;
	}

	@Column(name = "des")
	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	@Column(name = "download_count")
	public String getDownloadCount() {
		return downloadCount;
	}

	public void setDownloadCount(String downloadCount) {
		this.downloadCount = downloadCount;
	}

	@Column(name = "android_file_size")
	public String getAndroidFileSize() {
		return androidFileSize;
	}

	public void setAndroidFileSize(String androidFileSize) {
		this.androidFileSize = androidFileSize;
	}

	@Column(name = "ios_file_size")
	public String getIosFileSize() {
		return iosFileSize;
	}

	public void setIosFileSize(String iosFileSize) {
		this.iosFileSize = iosFileSize;
	}

	@Transient
	public List<GameImg> getImgs() {
		return imgs;
	}

	public void setImgs(List<GameImg> imgs) {
		this.imgs = imgs;
	}

}
