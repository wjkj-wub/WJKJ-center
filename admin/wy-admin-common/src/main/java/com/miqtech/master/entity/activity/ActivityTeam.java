package com.miqtech.master.entity.activity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "activity_t_team")
public class ActivityTeam extends IdEntity {
	private static final long serialVersionUID = -3910895878662568259L;
	private Long netbarId;// 所属网吧ID
	private Long memId;// 创建成员的ID
	private Long activityId;// 活动赛事ID
	private String name;// 战队名称
	private String server;// 所在服务器
	private Integer round;//场次
	private String qrcode;//二维码
	private Integer signed;//是否已签到:0-否,1-是

	@Column(name = "netbar_id")
	public Long getNetbarId() {
		return netbarId;
	}

	public void setNetbarId(Long netbarId) {
		this.netbarId = netbarId;
	}

	@Column(name = "mem_id")
	public Long getMemId() {
		return memId;
	}

	public void setMemId(Long memId) {
		this.memId = memId;
	}

	@Column(name = "activity_id")
	public Long getActivityId() {
		return activityId;
	}

	public void setActivityId(Long activityId) {
		this.activityId = activityId;
	}

	@Column(name = "name")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "server")
	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	@Column(name = "round")
	public Integer getRound() {
		return round;
	}

	public void setRound(Integer round) {
		this.round = round;
	}

	@Column(name = "qrcode")
	public String getQrcode() {
		return qrcode;
	}

	public void setQrcode(String qrcode) {
		this.qrcode = qrcode;
	}

	@Column(name = "signed")
	public Integer getSigned() {
		return signed;
	}

	public void setSigned(Integer signed) {
		this.signed = signed;
	}

}
