package com.miqtech.master.utils;

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

/**
 * 生成唯一ID的工具类.
 */
public class IdentityUtils {

	private IdentityUtils() {
		super();
	}

	/**
	 * JDK自带的UUID.
	 */
	public static String uuid() {
		return UUID.randomUUID().toString();
	}

	/**
	 * JDK自带的UUID,删除'-'分隔符.
	 */
	public static String uuidWithoutSplitter() {
		return UUID.randomUUID().toString().replaceAll("-", "");
	}

	/**
	 * 获取指定长度的uuid截取数据,不保证唯一.
	 * @param len 字符长度
	 */
	public static String gen(int len) {
		return StringUtils.substring(uuidWithoutSplitter(), 0, len);
	}

}
