package com.miqtech.master.entity.uwan;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "uwan_netbar")
public class UwanNetbar implements Serializable {

	private static final long serialVersionUID = -6274152749301043910L;

	private Long id;// AUTO_INCREMENT
	private Long uwanBarId;// 优玩网吧ID
	private Long netbarId;// 网娱网吧ID
	private Integer netbarType;// 场馆
	private Integer source;// 吧内赛及网吧大战
	private String wifiName;// WIFI名称
	private String wifiPassword;// WIFI密码
	private Date updateDate;// 以做更新频次控制

	@Id
	@Column(name = "id", nullable = false, unique = true)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name = "uwan_bar_id")
	public Long getUwanBarId() {
		return uwanBarId;
	}

	public void setUwanBarId(Long uwanBarId) {
		this.uwanBarId = uwanBarId;
	}

	@Column(name = "netbar_id")
	public Long getNetbarId() {
		return netbarId;
	}

	public void setNetbarId(Long netbarId) {
		this.netbarId = netbarId;
	}

	@Column(name = "netbar_type")
	public Integer getNetbarType() {
		return netbarType;
	}

	public void setNetbarType(Integer netbarType) {
		this.netbarType = netbarType;
	}

	@Column(name = "source")
	public Integer getSource() {
		return source;
	}

	public void setSource(Integer source) {
		this.source = source;
	}

	@Column(name = "wifi_name")
	public String getWifiName() {
		return wifiName;
	}

	public void setWifiName(String wifiName) {
		this.wifiName = wifiName;
	}

	@Column(name = "wifi_password")
	public String getWifiPassword() {
		return wifiPassword;
	}

	public void setWifiPassword(String wifiPassword) {
		this.wifiPassword = wifiPassword;
	}

	@Column(name = "update_date")
	public Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}

}
