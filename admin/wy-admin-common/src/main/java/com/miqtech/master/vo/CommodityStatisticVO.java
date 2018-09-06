package com.miqtech.master.vo;

public class CommodityStatisticVO {
	private int weekCoin;
	private int weekNum;
	private String startDateStr;
	private String endDateStr;
	private int coin;
	private int num;

	public int getCoin() {
		return coin;
	}

	public void setCoin(int coin) {
		this.coin = coin;
	}

	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}

	public String getStartDateStr() {
		return startDateStr;
	}

	public void setStartDateStr(String startDateStr) {
		this.startDateStr = startDateStr;
	}

	public String getEndDateStr() {
		return endDateStr;
	}

	public void setEndDateStr(String endDateStr) {
		this.endDateStr = endDateStr;
	}

	public int getWeekCoin() {
		return weekCoin;
	}

	public void setWeekCoin(int weekCoin) {
		this.weekCoin = weekCoin;
	}

	public int getWeekNum() {
		return weekNum;
	}

	public void setWeekNum(int weekNum) {
		this.weekNum = weekNum;
	}

}
