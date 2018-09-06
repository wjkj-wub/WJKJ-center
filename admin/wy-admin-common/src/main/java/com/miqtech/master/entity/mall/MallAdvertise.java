package com.miqtech.master.entity.mall;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

/**
 * 商城顶部广告
 */
@Entity
@Table(name = "mall_t_advertise")
public class MallAdvertise extends IdEntity {
	private static final long serialVersionUID = 3004177467177812960L;

	private String banner; //
	private Integer type; //1-任务,2-邀请,3-商品,4-外连接,5-安卓下载文件
	private Long targetId; //
	private String url; //自定义url，优先使用url
	private Integer deviceType;// 设备类型：0-全部，1-IOS，2-Android

	@Column(name = "device_type")
	public Integer getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(Integer deviceType) {
		this.deviceType = deviceType;
	}

	@Column(name = "banner")
	public String getBanner() {
		return banner;
	}

	public void setBanner(String banner) {
		this.banner = banner;
	}

	@Column(name = "type")
	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	@Column(name = "target_id")
	public Long getTargetId() {
		return targetId;
	}

	public void setTargetId(Long targetId) {
		this.targetId = targetId;
	}

	@Column(name = "url")
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
