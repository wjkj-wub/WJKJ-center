package com.miqtech.master.entity.common;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.miqtech.master.entity.IdEntity;

/**
 * 首页广告
 */
@Entity
@Table(name = "index_t_advertise")
@JsonIgnoreProperties({ "createDate", "id", "createUserId", "updateDate", "updateUserId", "valid" })
public class IndexAdvertise extends IdEntity {
	private static final long serialVersionUID = -5857422713362177388L;
	private String title;// 标题
	private String describe;// 描述
	private String img;// 图片（原图）
	private String imgMedia;// 图片（中）
	private String imgThumb;// 图片（小）
	private Long targetId;// 目标对象的ID
	private Integer type;// 类型：1-网吧；2-手游(弃用)；3-赛事(弃用); 4-网娱官方活动(弃用); 5-推广/广告10官方比赛11娱乐赛12约战13福利14android下载推广
	private String url;// 地址(在类型为4和5的时候有效)
	private Integer deviceType;// 设备类型：0-全部，1-IOS，2-Android
	private Integer sort;//排序(数字越大,显示越靠前)
	private Integer belong;//0首页1竞技大厅
	private Date serverStartDate;//上架时间
	private Date serverEndDate;//下架时间
	private Integer status;//0下架 1上架

	@Column(name = "server_start_date")
	public Date getServerStartDate() {
		return serverStartDate;
	}

	public void setServerStartDate(Date serverStartDate) {
		this.serverStartDate = serverStartDate;
	}

	@Column(name = "server_end_date")
	public Date getServerEndDate() {
		return serverEndDate;
	}

	public void setServerEndDate(Date serverEndDate) {
		this.serverEndDate = serverEndDate;
	}

	@Column(name = "status")
	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	@Column(name = "device_type")
	public Integer getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(Integer deviceType) {
		this.deviceType = deviceType;
	}

	@Column(name = "title")
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Column(name = "`describe`")
	public String getDescribe() {
		return describe;
	}

	public void setDescribe(String describe) {
		this.describe = describe;
	}

	@Column(name = "img")
	public String getImg() {
		return img;
	}

	public void setImg(String img) {
		this.img = img;
	}

	@Column(name = "img_thumb")
	public String getImgThumb() {
		return imgThumb;
	}

	public void setImgThumb(String imgThumb) {
		this.imgThumb = imgThumb;
	}

	@Column(name = "target_id")
	public Long getTargetId() {
		return targetId;
	}

	public void setTargetId(Long targetId) {
		this.targetId = targetId;
	}

	@Column(name = "type")
	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	@Column(name = "img_media")
	public String getImgMedia() {
		return imgMedia;
	}

	public void setImgMedia(String imgMedia) {
		this.imgMedia = imgMedia;
	}

	@Column(name = "url")
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Column(name = "sort")
	public Integer getSort() {
		return sort;
	}

	public void setSort(Integer sort) {
		this.sort = sort;
	}

	@Column(name = "belong")
	public Integer getBelong() {
		return belong;
	}

	public void setBelong(Integer belong) {
		this.belong = belong;
	}

}
