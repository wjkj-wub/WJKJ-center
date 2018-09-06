package com.miqtech.master.utils;

import java.math.BigDecimal;

public class ArithUtil {

	private static final int DEFAULT_SCALE = 2;

	private ArithUtil() {
	}

	public static Double add(double d1, double d2) {
		return add(d1, d2, DEFAULT_SCALE);
	}

	public static Double add(double d1, double d2, int scale) {
		BigDecimal b1 = new BigDecimal(d1);
		BigDecimal b2 = new BigDecimal(d2);
		return scale(b1.add(b2).doubleValue(), scale);
	}

	public static Double sub(double d1, double d2) {
		return sub(d1, d2, DEFAULT_SCALE);
	}

	public static Double sub(double d1, double d2, int scale) {
		BigDecimal b1 = new BigDecimal(d1);
		BigDecimal b2 = new BigDecimal(d2);
		return scale(b1.subtract(b2).doubleValue(), scale);
	}

	public static Double mul(double d1, double d2) {
		return mul(d1, d2, DEFAULT_SCALE);
	}

	public static Double mul(double d1, double d2, int scale) {
		BigDecimal b1 = new BigDecimal(d1);
		BigDecimal b2 = new BigDecimal(d2);
		return scale(b1.multiply(b2).doubleValue(), scale);
	}

	public static Double div(double d1, double d2) {
		return div(d1, d2, DEFAULT_SCALE);
	}

	public static Double div(double d1, double d2, int scale) {
		if (scale < 0) {
			throw new IllegalArgumentException("The scale must be a positive integer or zero");
		}

		BigDecimal b1 = new BigDecimal(d1);
		BigDecimal b2 = new BigDecimal(d2);
		return b1.divide(b2, scale, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	public static Double scale(Double value, int scale) {
		if (value == null) {
			return null;
		}

		BigDecimal bigValue = new BigDecimal(value);
		return bigValue.setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
	}
}
