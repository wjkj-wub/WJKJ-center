package com.miqtech.master.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 本包下用的正则表达式工具类
 */
public class RegExpUtils {
	/**
	 * 金额格式
	 */
	public static boolean regExpAmount(String amount) {
		String regex = "^((\\d\\d{0,9})|0)(\\.\\d{0,2})?$";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(amount);
		return matcher.find(); //boolean
	}

	/**
	 * 手机号格式
	 */
	public static boolean regExpTelephone(String telephone) {
		String regex = "^(13[0-9]|15[0-9]|17[0-9]|18[0-9]|14[0-9])[0-9]{8}$";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(telephone);
		return matcher.find(); //boolean
	}

	/**
	 * 邮箱格式
	 */
	public static boolean regExpEmail(String email) {
		String regex = "[\\w!#$%&'*+/=?^_`{|}~-]+(?:\\.[\\w!#$%&'*+/=?^_`{|}~-]+)*@(?:[\\w](?:[\\w-]*[\\w])?\\.)+[\\w](?:[\\w-]*[\\w])?";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(email);
		return matcher.find(); //boolean
	}
	
	/**
	 * QQ号码
	 */
	public static boolean regExpQq(String Qq) {
		String regex = "[1-9][0-9]{4,}";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(Qq);
		return matcher.find(); //boolean
	}
	
}
