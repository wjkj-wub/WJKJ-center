package com.miqtech.master.entity.netbar;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "netbar_t_evaluation_praise")
@JsonIgnoreProperties({ "createDate", "createUserId", "updateDate", "updateUserId", "valid" })
public class NetbarEvaluationPraise extends IdEntity {

	private static final long serialVersionUID = -952942107725074412L;

	private Long userId;//用户id
	private Long evaId;//评论id
	private Long orderId;//订单id

	@Column(name = "eva_id")
	public Long getEvaId() {
		return evaId;
	}

	public void setEvaId(Long evaId) {
		this.evaId = evaId;
	}

	@Column(name = "user_id")
	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	@Column(name = "order_id")
	public Long getOrderId() {
		return orderId;
	}

	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}

}
