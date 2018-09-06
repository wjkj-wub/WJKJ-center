package com.miqtech.master.entity.guessing;

import com.miqtech.master.entity.IdEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

/**
 * 竞猜实体，即由竞猜对象组成的竞猜比赛信息
 * @author zhangyuqi
 * 2017年06月01日
 */
@Entity
@Table(name = "guessing_info")
public class GuessingInfo extends IdEntity {
	private static final long serialVersionUID = 1L;

	private String title;// 竞猜主题
	private Date endDate;// 竞猜截止日期
	private Date releaseDate;// 竞猜发布日期
	private Integer status;// 竞猜状态：0未发布，1竞猜中，2等待结果，3已结束
	private Boolean top;// 是否置顶：0否，1是
	private Integer currentRemainder; // 当前余量额
	private Integer positiveRemainder; // 本场正余量
	private Integer negativeRemainder; // 本场负余量
	private Integer totalIncome; // 本场总收入=总押注金币数量
	private Integer totalExpenditure; // 本场总支出=押注胜本金+盈利

	@Column(name = "title")
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Column(name = "end_date")
	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	@Column(name = "release_date")
	public Date getReleaseDate() {
		return releaseDate;
	}

	public void setReleaseDate(Date releaseDate) {
		this.releaseDate = releaseDate;
	}

	@Column(name = "status")
	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	@Column(name = "is_top")
	public Boolean getTop() {
		return top;
	}

	public void setTop(Boolean top) {
		this.top = top;
	}

	@Column(name = "current_remainder")
	public Integer getCurrentRemainder() {
		return currentRemainder;
	}

	public void setCurrentRemainder(Integer currentRemainder) {
		this.currentRemainder = currentRemainder;
	}

	@Column(name = "positive_remainder")
	public Integer getPositiveRemainder() {
		return positiveRemainder;
	}

	public void setPositiveRemainder(Integer positiveRemainder) {
		this.positiveRemainder = positiveRemainder;
	}

	@Column(name = "negative_remainder")
	public Integer getNegativeRemainder() {
		return negativeRemainder;
	}

	public void setNegativeRemainder(Integer negativeRemainder) {
		this.negativeRemainder = negativeRemainder;
	}

	@Column(name = "total_income")
	public Integer getTotalIncome() {
		return totalIncome;
	}

	public void setTotalIncome(Integer totalIncome) {
		this.totalIncome = totalIncome;
	}

	@Column(name = "total_expenditure")
	public Integer getTotalExpenditure() {
		return totalExpenditure;
	}

	public void setTotalExpenditure(Integer totalExpenditure) {
		this.totalExpenditure = totalExpenditure;
	}

}
