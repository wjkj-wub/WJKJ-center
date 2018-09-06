package com.miqtech.master.entity.user;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.miqtech.master.entity.IdEntity;
import com.miqtech.master.utils.EncodeUtils;

@Entity
@Table(name = "user_t_info")
@JsonIgnoreProperties({ "createDate", "createUserId", "updateDate", "updateUserId", "valid", "password" })
public class UserInfo extends IdEntity {
	private static final long serialVersionUID = 917701978738037096L;
	private String username;// 用户名
	private String password;// 密码
	private String icon;// 头像
	private String iconMedia;// 头像（中等）
	private String iconThumb;// 头像（缩略图）
	private String telephone;// 联系号码
	private Integer score;// 积分
	private Integer coin;// 金币
	private String speech;// 发表的心情
	private String nickname;// 昵称
	private String invitationCode;// 邀请人的邀请码
	private String idCard;
	private String realName;
	private String cityCode;
	private String cityName;
	private String qq;
	private Integer sex;
	private String token;
	private int profileStatus;
	private Integer isReserve;// 预先注册的：0-否，1-是
	private Integer acceptAccess;
	private Integer acceptMatch;
	private String bgImg;// 背景图片
	private String deviceId;// 背景图片
	private String androidChannelName;// 背景图片
	private Integer activityTotal;//活动数
	private Integer concernTotal;//关注数
	private Integer fansTotal;//粉丝数
	private Integer isPasswordNull;
	private Integer isUpdated;
	private String moduleInfo;
	private Integer isUp;//是否是主播
	private Integer fans;//娱儿tv粉丝数
	
	@Column(name = "username")
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@Column(name = "password")
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setEncryptPassword(String password) {
		this.password = EncodeUtils.base64Md5(password);
	}

	@Column(name = "icon")
	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	@Column(name = "telephone")
	public String getTelephone() {
		return telephone;
	}

	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}

	@Column(name = "score")
	public Integer getScore() {
		return score;
	}

	public void setScore(Integer score) {
		this.score = score;
	}

	@Column(name = "coin")
	public Integer getCoin() {
		return coin;
	}

	public void setCoin(Integer coin) {
		this.coin = coin;
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

	@Column(name = "speech")
	public String getSpeech() {
		return speech;
	}

	public void setSpeech(String speech) {
		this.speech = speech;
	}

	@Column(name = "nickname")
	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	@Column(name = "idcard")
	public String getIdCard() {
		return idCard;
	}

	public void setIdCard(String idCard) {
		this.idCard = idCard;
	}

	@Column(name = "realname")
	public String getRealName() {
		return realName;
	}

	public void setRealName(String realName) {
		this.realName = realName;
	}

	@Column(name = "invitation_code")
	public String getInvitationCode() {
		return invitationCode;
	}

	public void setInvitationCode(String invitationCode) {
		this.invitationCode = invitationCode;
	}

	@Transient
	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	@Column(name = "city_code")
	public String getCityCode() {
		return cityCode;
	}

	public void setCityCode(String cityCode) {
		this.cityCode = cityCode;
	}

	@Column(name = "city_name")
	public String getCityName() {
		return cityName;
	}

	public void setCityName(String cityName) {
		this.cityName = cityName;
	}

	@Column(name = "qq")
	public String getQq() {
		return qq;
	}

	public void setQq(String qq) {
		this.qq = qq;
	}

	@Column(name = "sex")
	public Integer getSex() {
		return sex;
	}

	public void setSex(Integer sex) {
		this.sex = sex;
	}

	@Transient
	public int getProfileStatus() {
		return profileStatus;
	}

	public void setProfileStatus() {
		if (StringUtils.isBlank(qq) || StringUtils.isBlank(realName) || StringUtils.isBlank(idCard)) {
			this.profileStatus = 0;
			return;
		}
		this.profileStatus = 1;
	}

	@Column(name = "is_reserve")
	public Integer getIsReserve() {
		return isReserve;
	}

	public void setIsReserve(Integer isReserve) {
		this.isReserve = isReserve;
	}

	@Column(name = "accept_access")
	public Integer getAcceptAccess() {
		return acceptAccess;
	}

	public void setAcceptAccess(Integer acceptAccess) {
		this.acceptAccess = acceptAccess;
	}

	@Column(name = "accept_match")
	public Integer getAcceptMatch() {
		return acceptMatch;
	}

	public void setAcceptMatch(Integer acceptMatch) {
		this.acceptMatch = acceptMatch;
	}

	@Column(name = "bg_img")
	public String getBgImg() {
		return bgImg;
	}

	public void setBgImg(String bgImg) {
		this.bgImg = bgImg;
	}

	@Column(name = "device_id")
	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	@Column(name = "android_channel_name")
	public String getAndroidChannelName() {
		return androidChannelName;
	}

	public void setAndroidChannelName(String androidChannelName) {
		this.androidChannelName = androidChannelName;
	}

	@Transient
	public Integer getActivityTotal() {
		return activityTotal;
	}

	public void setActivityTotal(Integer activityTotal) {
		this.activityTotal = activityTotal;
	}

	@Transient
	public Integer getConcernTotal() {
		return concernTotal;
	}

	public void setConcernTotal(Integer concernTotal) {
		this.concernTotal = concernTotal;
	}

	@Transient
	public Integer getFansTotal() {
		return fansTotal;
	}

	public void setFansTotal(Integer fansTotal) {
		this.fansTotal = fansTotal;
	}

	@Transient
	public Integer getIsPasswordNull() {
		return isPasswordNull;
	}

	public void setIsPasswordNull(Integer isPasswordNull) {
		this.isPasswordNull = isPasswordNull;
	}

	@Column(name = "is_updated")
	public Integer getIsUpdated() {
		return isUpdated;
	}

	public void setIsUpdated(Integer isUpdated) {
		this.isUpdated = isUpdated;
	}

	@Transient
	public String getModuleInfo() {
		return moduleInfo;
	}

	public void setModuleInfo(String moduleInfo) {
		this.moduleInfo = moduleInfo;
	}

	@Transient
	public Integer getIsUp() {
		return isUp;
	}

	public void setIsUp(Integer isUp) {
		this.isUp = isUp;
	}

	@Transient
	public Integer getFans() {
		return fans;
	}

	public void setFans(Integer fans) {
		this.fans = fans;
	}


}
