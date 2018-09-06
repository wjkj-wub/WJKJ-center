package com.miqtech.master.vo;

public class NetbarStatisticsVO {
	public int totalMemberSum = 0;//总会员总计
	public int newMemberSum = 0;//新增会员总计
	public int acceptSum = 0;//接单次数总计
	public int paySum = 0;//支付次数总计
	public double redbagAmountSum = 0;//红包金额总计
	public double amountSum = 0;//现金金额总计
	public int reserveSum = 0;//预订次数总计
	public int battleSum = 0;//约占次数

	public int getTotalMemberSum() {
		return totalMemberSum;
	}

	public int getNewMemberSum() {
		return newMemberSum;
	}

	public int getAcceptSum() {
		return acceptSum;
	}

	public int getPaySum() {
		return paySum;
	}

	public double getRedbagAmountSum() {
		return redbagAmountSum;
	}

	public double getAmountSum() {
		return amountSum;
	}

	public int getBattleSum() {
		return battleSum;
	}

	public int getReserveSum() {
		return reserveSum;
	}

}
