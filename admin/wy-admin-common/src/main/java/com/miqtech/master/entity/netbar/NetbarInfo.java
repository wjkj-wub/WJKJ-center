package com.miqtech.master.entity.netbar;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "netbar_t_info")
@JsonIgnoreProperties({ "createDate", "createUserId", "updateDate", "updateUserId", "valid" })
public class NetbarInfo extends IdEntity {
	private static final long serialVersionUID = -6194145788612749006L;
	private String name;// 网吧名称
	private Integer score;// 网吧星级
	private String address;// 网吧地址
	private Double longitude;// 经度
	private Double latitude;// 纬度
	private String presentation;// 商家介绍
	private String telephone;// 联系方式
	private String discountInfo;// 优惠信息
	private Integer seating;// 机位数
	private String icon;// 缩略图
	private String iconMedia;// 缩略图（中）
	private String iconThumb;// 缩略图（小）
	private String img;// 首图
	private String imgMedia;// 首图（中）
	private String imgThumb;// 首图（小）
	private Double price;// 单个作为的订金
	private Long areaId;// 地区ID
	private String areaCode;// 地区编码
	private String cpu;// 处理器
	private String memory;// 内存
	private String graphics;// 显卡
	private String display;// 显示器
	private Integer isRelease;// 是否发布：0-false,1-true
	private String invitationCode;// 网吧编码(邀请码)
	private int rebate;
	private Date rebateStartdate;
	private Date rebateEnddate;
	private String pricePerHour;//每小时上网费
	private List<NetbarImg> imgs;
	private Date releaseDate;
	private Integer source;//0网吧注册1员工录入
	private Integer levels;//网吧级别
	private Double quotaRatio;//网吧配比
	private String tag;//网吧标签
	private String qrcode;//网吧支付二维码

	@Column(name = "name")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "score")
	public Integer getScore() {
		return score;
	}

	public void setScore(Integer score) {
		this.score = score;
	}

	@Column(name = "address")
	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	@Column(name = "longitude")
	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	@Column(name = "latitude")
	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	@Column(name = "presentation")
	public String getPresentation() {
		return presentation;
	}

	public void setPresentation(String presentation) {
		this.presentation = presentation;
	}

	@Column(name = "telephone")
	public String getTelephone() {
		return telephone;
	}

	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}

	@Column(name = "discount_info")
	public String getDiscountInfo() {
		return discountInfo;
	}

	public void setDiscountInfo(String discountInfo) {
		this.discountInfo = discountInfo;
	}

	@Column(name = "seating")
	public Integer getSeating() {
		return seating;
	}

	public void setSeating(Integer seating) {
		this.seating = seating;
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

	@Column(name = "img")
	public String getImg() {
		return img;
	}

	public void setImg(String img) {
		this.img = img;
	}

	@Column(name = "img_media")
	public String getImgMedia() {
		return imgMedia;
	}

	public void setImgMedia(String imgMedia) {
		this.imgMedia = imgMedia;
	}

	@Column(name = "img_thumb")
	public String getImgThumb() {
		return imgThumb;
	}

	public void setImgThumb(String imgThumb) {
		this.imgThumb = imgThumb;
	}

	@Column(name = "price")
	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	@Column(name = "area_id")
	public Long getAreaId() {
		return areaId;
	}

	public void setAreaId(Long areaId) {
		this.areaId = areaId;
	}

	@Column(name = "area_code")
	public String getAreaCode() {
		return areaCode;
	}

	public void setAreaCode(String areaCode) {
		this.areaCode = areaCode;
	}

	@Column(name = "is_release")
	public Integer getIsRelease() {
		return isRelease;
	}

	public void setIsRelease(Integer isRelease) {
		this.isRelease = isRelease;
	}

	@Column(name = "cpu")
	public String getCpu() {
		return cpu;
	}

	public void setCpu(String cpu) {
		this.cpu = cpu;
	}

	@Column(name = "memory")
	public String getMemory() {
		return memory;
	}

	public void setMemory(String memory) {
		this.memory = memory;
	}

	@Column(name = "graphics")
	public String getGraphics() {
		return graphics;
	}

	public void setGraphics(String graphics) {
		this.graphics = graphics;
	}

	@Column(name = "display")
	public String getDisplay() {
		return display;
	}

	public void setDisplay(String display) {
		this.display = display;
	}

	@Column(name = "invitation_code")
	public String getInvitationCode() {
		return invitationCode;
	}

	public void setInvitationCode(String invitationCode) {
		this.invitationCode = invitationCode;
	}

	@Transient
	public List<NetbarImg> getImgs() {
		return imgs;
	}

	public void setImgs(List<NetbarImg> imgs) {
		this.imgs = imgs;
	}

	@Column(name = "rebate")
	public int getRebate() {
		return rebate;
	}

	public void setRebate(int rebate) {
		this.rebate = rebate;
	}

	@Column(name = "rebate_start_date")
	public Date getRebateStartdate() {
		return rebateStartdate;
	}

	public void setRebateStartdate(Date rebateStartdate) {
		this.rebateStartdate = rebateStartdate;
	}

	@Column(name = "rebate_end_date")
	public Date getRebateEnddate() {
		return rebateEnddate;
	}

	public void setRebateEnddate(Date rebateEnddate) {
		this.rebateEnddate = rebateEnddate;
	}

	@Column(name = "price_per_hour")
	public String getPricePerHour() {
		return pricePerHour;
	}

	public void setPricePerHour(String pricePerHour) {
		this.pricePerHour = pricePerHour;
	}

	@Column(name = "release_date")
	public Date getReleaseDate() {
		return releaseDate;
	}

	public void setReleaseDate(Date releaseDate) {
		this.releaseDate = releaseDate;
	}

	@Column(name = "source")
	public Integer getSource() {
		return source;
	}

	public void setSource(Integer source) {
		this.source = source;
	}

	@Column(name = "levels")
	public Integer getLevels() {
		return levels;
	}

	public void setLevels(Integer levels) {
		this.levels = levels;
	}

	@Column(name = "quota_ratio")
	public Double getQuotaRatio() {
		return quotaRatio;
	}

	public void setQuotaRatio(Double quotaRatio) {
		this.quotaRatio = quotaRatio;
	}

	@Column(name = "tag")
	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	@Column(name = "qrcode")
	public String getQrcode() {
		return qrcode;
	}

	public void setQrcode(String qrcode) {
		this.qrcode = qrcode;
	}
}
