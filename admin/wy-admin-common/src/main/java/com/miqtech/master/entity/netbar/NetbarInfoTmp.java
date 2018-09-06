package com.miqtech.master.entity.netbar;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "netbar_t_info_tmp")
public class NetbarInfoTmp extends IdEntity {
	private static final long serialVersionUID = -7370303912793899870L;
	private Long netbarId;//网吧id
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
	private String pricePerHour;//每小时上网单价范围
	private Long areaId;// 地区ID
	private String areaCode;// 地区编码
	private String cpu;// 处理器
	private String memory;// 内存
	private String graphics;// 显卡
	private String display;// 显示器
	private Integer status;//临时表状态 0录入状态 1 待审核状态 2 审核通过状态 3 审核被拒绝
	private Integer isApply;//申领状态 0 未申领 1已申领

	private Long clerkUserId;//录入人员id
	private Long auditorUserId;//审核人员id
	private Long merchantId;
	private Integer source;//0网吧注册1员工录入

	@Column(name = "presentation")
	public String getPresentation() {
		return presentation;
	}

	public void setPresentation(String presentation) {
		this.presentation = presentation;
	}

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

	@Column(name = "status")
	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	@Column(name = "clerk_user_id")
	public Long getClerkUserId() {
		return clerkUserId;
	}

	public void setClerkUserId(Long clerkUserId) {
		this.clerkUserId = clerkUserId;
	}

	@Column(name = "auditor_user_id")
	public Long getAuditorUserId() {
		return auditorUserId;
	}

	public void setAuditorUserId(Long auditorUserId) {
		this.auditorUserId = auditorUserId;
	}

	@Column(name = "netbar_id")
	public Long getNetbarId() {
		return netbarId;
	}

	public void setNetbarId(Long netbarId) {
		this.netbarId = netbarId;
	}

	@Column(name = "merchant_id")
	public Long getMerchantId() {
		return merchantId;
	}

	public void setMerchantId(Long merchantId) {
		this.merchantId = merchantId;
	}

	@Column(name = "is_apply")
	public Integer getIsApply() {
		return isApply;
	}

	public void setIsApply(Integer isApply) {
		this.isApply = isApply;
	}

	@Column(name = "price_per_hour")
	public String getPricePerHour() {
		return pricePerHour;
	}

	public void setPricePerHour(String pricePerHour) {
		this.pricePerHour = pricePerHour;
	}

	@Column(name = "source")
	public Integer getSource() {
		return source;
	}

	public void setSource(Integer source) {
		this.source = source;
	}

}
