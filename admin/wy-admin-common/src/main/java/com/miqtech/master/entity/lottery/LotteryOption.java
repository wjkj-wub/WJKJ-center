package com.miqtech.master.entity.lottery;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.miqtech.master.entity.IdEntity;

/**
 * 抽奖活动设置
 */
@Entity
@Table(name = "lottery_t_option")
public class LotteryOption extends IdEntity {

	private static final long serialVersionUID = 8389694533389017927L;

	private String name;// 活动名
	private String plateImg;// 转盘图片
	private String introduce;// 活动介绍
	private Date startDate;// 开始时间
	private Date endDate;// 结束时间

	private List<LotteryAwardSeat> awardSeats;

	@Column(name = "name")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "plate_img")
	public String getPlateImg() {
		return plateImg;
	}

	public void setPlateImg(String plateImg) {
		this.plateImg = plateImg;
	}

	@Column(name = "introduce")
	public String getIntroduce() {
		return introduce;
	}

	public void setIntroduce(String introduce) {
		this.introduce = introduce;
	}

	@Column(name = "start_date")
	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	@Column(name = "end_date")
	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	/**
	 * 检查是否在活动期间
	 */
	@Transient
	public boolean duringActivity() {
		Date now = new Date();
		Date startDate = getStartDate();
		Date endDate = getEndDate();
		return (startDate == null || now.after(startDate)) && (endDate == null || now.before(endDate));
	}

	@Transient
	public List<LotteryAwardSeat> getAwardSeats() {
		return awardSeats;
	}

	public void setAwardSeats(List<LotteryAwardSeat> awardSeats) {
		this.awardSeats = awardSeats;
	}
}
