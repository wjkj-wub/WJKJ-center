package com.miqtech.master.entity.log;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "sys_t_api_log")
public class AppUserLogInfo implements Serializable {

	private static final long serialVersionUID = 4129809586708023009L;
	private Long id;
	private String userId;
	private String userName;
	private String token;
	private String deviceId;
	private String apiName;
	private String os;
	private String osVersion;
	private String appVersion;
	private String ip;
	private String cityCode;
	private Date logDate;
	private Long logTime;
	
	private String apiExtend;

	@Id
	@Column(name = "id", nullable = false, unique = true)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name = "user_id")
	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	@Column(name = "token")
	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	@Column(name = "device_id")
	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	@Column(name = "os")
	public String getOs() {
		return os;
	}

	public void setOs(String os) {
		this.os = os;
	}

	@Column(name = "os_version")
	public String getOsVersion() {
		return osVersion;
	}

	public void setOsVersion(String osVersion) {
		this.osVersion = osVersion;
	}

	@Column(name = "app_version")
	public String getAppVersion() {
		return appVersion;
	}

	public void setAppVersion(String appVersion) {
		this.appVersion = appVersion;
	}

	@Column(name = "ip")
	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	@Column(name = "city_code")
	public String getCityCode() {
		return cityCode;
	}

	public void setCityCode(String cityCode) {
		this.cityCode = cityCode;
	}

	@Column(name = "log_date")
	public Date getLogDate() {
		return logDate;
	}

	public void setLogDate(Date logDate) {
		this.logDate = logDate;
	}

	@Column(name = "log_time")
	public Long getLogTime() {
		return logTime;
	}

	public void setLogTime(Long logTime) {
		this.logTime = logTime;
	}

	@Column(name = "api_name")
	public String getApiName() {
		return apiName;
	}

	public void setApiName(String apiName) {
		this.apiName = apiName;
	}
	@Column(name = "api_extend")
	public String getApiExtend() {
		return apiExtend;
	}

	public void setApiExtend(String apiExtend) {
		this.apiExtend = apiExtend;
	}

	@Column(name = "user_name")
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	@Override
	public String toString() {
		return logDate + "\001" + logTime + "\001" + deviceId + "\001" + apiName + "\001" + userName + "\001" + userId
				+ "\001" + token + "\001" + os + "\001" + osVersion + "\001" + appVersion + "\001" + ip + "\001"
				+ cityCode + "\001" + apiExtend;
	}

}
