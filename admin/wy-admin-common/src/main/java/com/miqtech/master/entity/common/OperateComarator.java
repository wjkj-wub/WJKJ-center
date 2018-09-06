package com.miqtech.master.entity.common;

import java.util.Comparator;

public class OperateComarator implements Comparator<Operate> {

	@Override
	public int compare(Operate o1, Operate o2) {
		Integer o1OrderId = o1.getOrderId();
		Integer o2OrderId = o2.getOrderId();
		if (null == o1OrderId) {
			o1OrderId = Integer.MAX_VALUE;
		}
		if (null == o2OrderId) {
			o2OrderId = Integer.MAX_VALUE;
		}
		return o1OrderId - o2OrderId;
	}

}
