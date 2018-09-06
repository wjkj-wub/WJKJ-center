package com.miqtech.master.entity.activity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "activity_r_items_server")
public class ActivityServer extends IdEntity {
	private static final long serialVersionUID = -2323731165453962517L;
	private Long itemId;// 赛事项目ID
	private String serverName;// 服务器名
	private Long parentServerId;
	private int serverRequired;

	@Column(name = "item_id")
	public Long getItemId() {
		return itemId;
	}

	public void setItemId(Long itemId) {
		this.itemId = itemId;
	}

	@Column(name = "server_name")
	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	@Column(name = "parent_server_id")
	public Long getParentServerId() {
		return parentServerId;
	}

	public void setParentServerId(Long parentServerId) {
		this.parentServerId = parentServerId;
	}

	@Column(name = "server_required")
	public int getServerRequired() {
		return serverRequired;
	}

	public void setServerRequired(int serverRequired) {
		this.serverRequired = serverRequired;
	}
}