package com.miqtech.master.entity.audition;

import com.miqtech.master.entity.IdEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 海选赛-领取，下载发放金币量和推广APP 配置
 * @author zhangyuqi
 * 2017年06月14日
 */
@Table(name = "audition_award_config")
@Entity
public class AuditionAwardConfig extends IdEntity {
	private static final long serialVersionUID = -3109497768529890449L;

	private Integer receiveMinCount;// 领取发放金币最小值
	private Integer receiveMaxCount;// 领取发放金币最大值
	private Integer downloadMinCount;// 领取发放金币最小值
	private Integer downloadMaxCount;// 领取发放金币最大值
	private String appIds;// 推广app id 列表

	@Column(name = "receive_min_count")
	public Integer getReceiveMinCount() {
		return receiveMinCount;
	}

	public void setReceiveMinCount(Integer receiveMinCount) {
		this.receiveMinCount = receiveMinCount;
	}

	@Column(name = "receive_max_count")
	public Integer getReceiveMaxCount() {
		return receiveMaxCount;
	}

	public void setReceiveMaxCount(Integer receiveMaxCount) {
		this.receiveMaxCount = receiveMaxCount;
	}

	@Column(name = "download_min_count")
	public Integer getDownloadMinCount() {
		return downloadMinCount;
	}

	public void setDownloadMinCount(Integer downloadMinCount) {
		this.downloadMinCount = downloadMinCount;
	}

	@Column(name = "download_max_count")
	public Integer getDownloadMaxCount() {
		return downloadMaxCount;
	}

	public void setDownloadMaxCount(Integer downloadMaxCount) {
		this.downloadMaxCount = downloadMaxCount;
	}

	@Column(name = "app_ids")
	public String getAppIds() {
		return appIds;
	}

	public void setAppIds(String appIds) {
		this.appIds = appIds;
	}
}
