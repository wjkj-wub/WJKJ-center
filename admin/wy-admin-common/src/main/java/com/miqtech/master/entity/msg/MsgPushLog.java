package com.miqtech.master.entity.msg;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "msg_push_log")
public class MsgPushLog extends IdEntity {
	private static final long serialVersionUID = -6148966600299134959L;

	private String title;//'推送标题',
	private String content;//'推送内容',
	private Integer clientType;// '推送目标 1个人 2全体 3地区',
	private String clientInfo;// '个人类型时 目标用户id 地区类型时:地区code',
	private Integer infoType;// '推送信息类型:1系统消息 2资讯 3官方赛 4娱乐赛',
	private Long infoId;// '当资讯 官方赛 娱乐赛 时 对应表id',
	private String moduleName;//
	private String subModuleName;
	private String infoTitle;

	public String getInfoTitle() {
		return infoTitle;
	}

	@Column(name = "info_title")
	public void setInfoTitle(String infoTitle) {
		this.infoTitle = infoTitle;
	}

	@Column(name = "module_name")
	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	@Column(name = "sub_module_name")
	public String getSubModuleName() {
		return subModuleName;
	}

	public void setSubModuleName(String subModuleName) {
		this.subModuleName = subModuleName;
	}

	@Column(name = "title")
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Column(name = "content")
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	@Column(name = "client_type")
	public Integer getClientType() {
		return clientType;
	}

	public void setClientType(Integer clientType) {
		this.clientType = clientType;
	}

	@Column(name = "client_info")
	public String getClientInfo() {
		return clientInfo;
	}

	public void setClientInfo(String clientInfo) {
		this.clientInfo = clientInfo;
	}

	@Column(name = "info_type")
	public Integer getInfoType() {
		return infoType;
	}

	public void setInfoType(Integer infoType) {
		this.infoType = infoType;
	}

	@Column(name = "info_id")
	public Long getInfoId() {
		return infoId;
	}

	public void setInfoId(Long infoId) {
		this.infoId = infoId;
	}

}
