package com.miqtech.master.entity.netbar;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "netbar_t_evaluation")
@JsonIgnoreProperties({ "createDate", "createUserId", "updateDate", "updateUserId", "valid" })
public class NetbarEvaluation extends IdEntity {

	private static final long serialVersionUID = -1129006009600447326L;
	private Integer enviroment;//环境舒适分数
	private Integer equipment;//机器配置分数
	private Integer network;//网络环境分数
	private Integer service;//服务态度分数
	private Integer praised;//已赞数量
	private Long userId;//用户id
	private Long netbarId;//网吧id
	private Long orderId;//订单id
	private Integer isAnonymous;//是否匿名：1-匿名;0-实名;
	private String content;//评论内容

	@Column(name = "enviroment")
	public Integer getEnviroment() {
		return enviroment;
	}

	public void setEnviroment(Integer enviroment) {
		this.enviroment = enviroment;
	}

	@Column(name = "equipment")
	public Integer getEquipment() {
		return equipment;
	}

	public void setEquipment(Integer equipment) {
		this.equipment = equipment;
	}

	@Column(name = "network")
	public Integer getNetwork() {
		return network;
	}

	public void setNetwork(Integer network) {
		this.network = network;
	}

	@Column(name = "service")
	public Integer getService() {
		return service;
	}

	public void setService(Integer service) {
		this.service = service;
	}

	@Column(name = "praised")
	public Integer getPraised() {
		return praised;
	}

	public void setPraised(Integer praised) {
		this.praised = praised;
	}

	@Column(name = "user_id")
	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	@Column(name = "is_anonymous")
	public Integer getIsAnonymous() {
		return isAnonymous;
	}

	public void setIsAnonymous(Integer isAnonymous) {
		this.isAnonymous = isAnonymous;
	}

	@Column(name = "content")
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	@Column(name = "netbar_id")
	public Long getNetbarId() {
		return netbarId;
	}

	public void setNetbarId(Long netbarId) {
		this.netbarId = netbarId;
	}

	@Column(name = "order_id")
	public Long getOrderId() {
		return orderId;
	}

	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}

}
