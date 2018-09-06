package com.miqtech.master.entity.netbar;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "netbar_r_reservation")
@JsonIgnoreProperties({ "createUserId", "updateDate", "updateUserId" })
public class NetbarReservation extends IdEntity {
	private static final long serialVersionUID = -8892622263111410127L;

	private Long netbarId;// 网吧ID

	private Long userId;// 预定用户ID

	private Integer seating;// 预定机位数

	private String telephone;// 联系方式

	private String remark;// 备注

	private Integer isRelated;// 是否连位

	private Integer isReceive;// 商家是否接收（默认应为否）

	private Date reservationTime;// 上机时间

	private Double amount;// 金额

	private Double overpay;// 愿意多付的金额

	private Integer arrive;// 是否已到店

	@Column(name = "hours")
	private Integer hours;// 时长

	private Long operateStaffId;//操作员id

	@Column(name = "netbar_id")
	public Long getNetbarId() {
		return netbarId;
	}

	public void setNetbarId(Long netbarId) {
		this.netbarId = netbarId;
	}

	@Column(name = "user_id")
	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	@Column(name = "seating")
	public Integer getSeating() {
		return seating;
	}

	public void setSeating(Integer seating) {
		this.seating = seating;
	}

	@Column(name = "telephone")
	public String getTelephone() {
		return telephone;
	}

	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}

	@Column(name = "remark")
	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	@Column(name = "is_related")
	public Integer getIsRelated() {
		return isRelated;
	}

	public void setIsRelated(Integer isRelated) {
		this.isRelated = isRelated;
	}

	@Column(name = "reservation_time")
	public Date getReservationTime() {
		return reservationTime;
	}

	public void setReservationTime(Date reservationTime) {
		this.reservationTime = reservationTime;
	}

	@Column(name = "is_receive")
	public Integer getIsReceive() {
		return isReceive;
	}

	public void setIsReceive(Integer isReceive) {
		this.isReceive = isReceive;
	}

	@Column(name = "amount")
	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	@Column(name = "overpay")
	public Double getOverpay() {
		return overpay;
	}

	public void setOverpay(Double overpay) {
		this.overpay = overpay;
	}

	@Column(name = "arrive")
	public Integer getArrive() {
		return arrive;
	}

	public void setArrive(Integer arrive) {
		this.arrive = arrive;
	}

	public Integer getHours() {
		return hours;
	}

	public void setHours(Integer hours) {
		this.hours = hours;
	}

	@Column(name = "operate_staff_id")
	public Long getOperateStaffId() {
		return operateStaffId;
	}

	public void setOperateStaffId(Long operateStaffId) {
		this.operateStaffId = operateStaffId;
	}

}
