package com.miqtech.master.utils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

public class CookieUtils {

	private CookieUtils() {
	}

	/**
	 * 设置cookie（过期时间为一天）
	 */
	public static void addCookie(HttpServletResponse response, String name, String value) {
		addCookie(response, name, value, 60 * 60 * 24);
	}

	/**
	 * 设置cookie
	 */
	public static void addCookie(HttpServletResponse response, String name, String value, int expiry) {
		if (StringUtils.isNotBlank(name) && StringUtils.isNotBlank(value)) {
			Cookie cookie = new Cookie(name, value);
			cookie.setPath("/");
			cookie.setMaxAge(expiry);
			response.addCookie(cookie);
		}
	}

	/**
	 * 获取cookie
	 */
	public static String getCookie(HttpServletRequest request, String name) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null && cookies.length > 0) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals(name)) {
					return cookie.getValue();
				}
			}
		}
		return null;
	}
}
