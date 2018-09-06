package com.miqtech.master.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TranNoUtils {

	private TranNoUtils() {
	}

	/**
	 * 生成交易流水号:年月日时分秒（14位）+纳秒（前8位）+3位随机数
	 */
	public static String genTranNo() {
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
		String date = format.format(new Date());
		String nano = Long.toString(System.nanoTime()).substring(0, 8);
		String radom = String.valueOf((int) ((Math.random() * (1000 - 100)) + 100));
		return date + nano + radom;
	}

	/**
	 * 生成[商品]交易流水号,23位:年（2位）+月+日+分+秒+商品编号+3位随机数
	 */
	public static String genCommodityTranNo(String itemNo) {
		SimpleDateFormat format = new SimpleDateFormat("yyMMddHHmmss");
		String date = format.format(new Date());
		String radom = String.valueOf((int) ((Math.random() * (1000 - 100)) + 100));
		return date + itemNo + radom;
	}

}
