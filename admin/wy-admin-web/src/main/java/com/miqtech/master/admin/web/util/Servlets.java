package com.miqtech.master.admin.web.util;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.TreeMap;

import javax.servlet.ServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.google.common.base.Charsets;
import com.google.common.net.HttpHeaders;
import com.miqtech.master.entity.common.SystemUser;
import com.miqtech.master.utils.EncodeUtils;

/**
 * Http与Servlet工具类.
 */
public class Servlets {

	private Servlets() {
		super();
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(Servlets.class);
	public static final long ONE_YEAR_SECONDS = 60 * 60 * 24 * 365;

	/**
	 * 设置客户端缓存过期时间 的Header.
	 */
	public static void setExpiresHeader(HttpServletResponse response, long expiresSeconds) {
		// Http 1.0 header, set a fix expires date.
		response.setDateHeader(HttpHeaders.EXPIRES, System.currentTimeMillis() + expiresSeconds * 1000);
		// Http 1.1 header, set a time after now.
		response.setHeader(HttpHeaders.CACHE_CONTROL, "private, max-age=" + expiresSeconds);
	}

	/**
	 * 设置禁止客户端缓存的Header.
	 */
	public static void setNoCacheHeader(HttpServletResponse response) {
		// Http 1.0 header
		response.setDateHeader(HttpHeaders.EXPIRES, 1L);
		response.addHeader(HttpHeaders.PRAGMA, "no-cache");
		// Http 1.1 header
		response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, max-age=0");
	}

	/**
	 * 设置LastModified Header.
	 */
	public static void setLastModifiedHeader(HttpServletResponse response, long lastModifiedDate) {
		response.setDateHeader(HttpHeaders.LAST_MODIFIED, lastModifiedDate);
	}

	/**
	 * 设置Etag Header.
	 */
	public static void setEtag(HttpServletResponse response, String etag) {
		response.setHeader(HttpHeaders.ETAG, etag);
	}

	/**
	 * 根据浏览器If-Modified-Since Header, 计算文件是否已被修改.
	 * 如果无修改, checkIfModify返回false ,设置304 not modify status.
	 * @param lastModified 内容的最后修改时间.
	 */
	public static boolean checkIfModifiedSince(HttpServletRequest request, HttpServletResponse response,
			long lastModified) {
		long ifModifiedSince = request.getDateHeader(HttpHeaders.IF_MODIFIED_SINCE);
		if (ifModifiedSince != -1 && lastModified < ifModifiedSince + 1000) {
			response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
			return false;
		}
		return true;
	}

	/**
	 * 根据浏览器 If-None-Match Header, 计算Etag是否已无效.
	 *
	 * 如果Etag有效, checkIfNoneMatch返回false, 设置304 not modify status.
	 *
	 * @param etag 内容的ETag.
	 */
	public static boolean checkIfNoneMatchEtag(HttpServletRequest request, HttpServletResponse response, String etag) {
		String headerValue = request.getHeader(HttpHeaders.IF_NONE_MATCH);
		if (headerValue != null) {
			boolean conditionSatisfied = false;
			if (!"*".equals(headerValue)) {
				StringTokenizer commaTokenizer = new StringTokenizer(headerValue, ",");
				while (!conditionSatisfied && commaTokenizer.hasMoreTokens()) {
					String currentToken = commaTokenizer.nextToken();
					if (currentToken.trim().equals(etag)) {
						conditionSatisfied = true;
					}
				}
			} else {
				conditionSatisfied = true;
			}
			if (conditionSatisfied) {
				response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
				response.setHeader(HttpHeaders.ETAG, etag);
				return false;
			}
		}
		return true;
	}

	/**
	 * 设置让浏览器弹出下载对话框的Header.
	 *
	 * @param fileName 下载后的文件名.
	 */
	public static void setFileDownloadHeader(HttpServletRequest request, HttpServletResponse response,
			String fileName) {
		// 中文文件名支持
		String encodedfileName = null;
		// 替换空格，否则firefox下有空格文件名会被截断,其他浏览器会将空格替换成+号
		encodedfileName = fileName.trim().replaceAll(" ", "_");
		String agent = request.getHeader("User-Agent");
		boolean isMSIE = agent != null && agent.toUpperCase().indexOf("MSIE") != -1;
		if (isMSIE) {
			try {
				encodedfileName = EncodeUtils.urlEncode(fileName);
			} catch (UnsupportedEncodingException e) {
				encodedfileName = fileName;
			}
		} else {
			encodedfileName = new String(fileName.getBytes(), Charsets.ISO_8859_1);
		}
		response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedfileName + "\"");

	}

	/**
	 * 取得带相同前缀的Request Parameters, copy from spring WebUtils.
	 *
	 * 返回的结果的Parameter名已去除前缀.
	 */
	@SuppressWarnings("rawtypes")
	public static Map<String, Object> getParametersStartingWith(ServletRequest request, String prefix) {
		Validate.notNull(request, "Request must not be null");
		Enumeration paramNames = request.getParameterNames();
		Map<String, Object> params = new TreeMap<String, Object>();
		if (prefix == null) {
			prefix = "";
		}
		while (paramNames != null && paramNames.hasMoreElements()) {
			String paramName = (String) paramNames.nextElement();
			if ("".equals(prefix) || paramName.startsWith(prefix)) {
				String unprefixed = paramName.substring(prefix.length());
				String[] values = request.getParameterValues(paramName);
				if (values == null || values.length == 0) {
				} else if (values.length > 1) {
					params.put(unprefixed, values);
				} else {
					params.put(unprefixed, values[0]);
				}
			}
		}
		return params;
	}

	/**
	 * 组合Parameters生成Query String的Parameter部分, 并在paramter name上加上prefix.
	 *
	 * @see #getParametersStartingWith
	 */
	public static String encodeParameterStringWithPrefix(Map<String, Object> params, String prefix) {
		if (CollectionUtils.isEmpty(params)) {
			return "";
		}

		if (prefix == null) {
			prefix = "";
		}

		StringBuilder queryStringBuilder = new StringBuilder();
		Iterator<Entry<String, Object>> it = params.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, Object> entry = it.next();
			queryStringBuilder.append(prefix).append(entry.getKey()).append('=').append(entry.getValue());
			if (it.hasNext()) {
				queryStringBuilder.append('&');
			}
		}
		return queryStringBuilder.toString();
	}

	/**
	 * 客户端对Http Basic验证的 Header进行编码.
	 */
	public static String encodeHttpBasic(String userName, String password) {
		String encode = userName + ":" + password;
		return "Basic " + EncodeUtils.encodeBase64(encode.getBytes());
	}

	public final static String getWebrootPath() {
		String root = Servlets.class.getResource("/").getFile();
		try {
			root = new File(root).getParentFile().getParentFile().getCanonicalPath();
			root += "/";
		} catch (IOException e) {
			LOGGER.error("获取项目根路径异常:", e.getMessage());
			return null;
		}
		return root;
	}

	public final static String getWebRootUrl(HttpServletRequest request) {
		return request == null ? "" : request.getServletContext().getContextPath();
	}

	public static String getRealPath(HttpServletRequest request, String string) {
		return request.getSession().getServletContext().getRealPath(string);
	}

	/**
	 * 获取request中单个MultipartFile对象
	 */
	public static MultipartFile getMultipartFile(HttpServletRequest req, String paramName) {
		MultipartFile file = null;
		try {
			file = ((MultipartHttpServletRequest) req).getFile(paramName);
		} catch (Exception e) {
			LOGGER.error("HttpServletRequest获取上传文件异常:", e.getMessage());
		}
		return file;
	}

	/**
	 * 获取request中多个MultipartFile对象
	 */
	public static List<MultipartFile> getMultipartFiles(HttpServletRequest req, String paramName) {
		List<MultipartFile> files = null;
		try {
			files = ((MultipartHttpServletRequest) req).getFiles(paramName);
		} catch (Exception e) {
			LOGGER.error("HttpServletRequest获取上传文件异常:", e.getMessage());
		}
		return files;
	}

	public static void clearSession(HttpServletRequest request) {
		request.getSession().invalidate();
	}

	/**
	 * 设置登陆用户
	 */
	public static void setSessionUser(HttpServletRequest request, SystemUser user) {
		request.getSession().setAttribute("user", user);
	}

	/**
	 * 获取登陆用户
	 */
	public static SystemUser getSessionUser(HttpServletRequest request) {
		return (SystemUser) request.getSession().getAttribute("user");
	}

	/**
	 * 获取Cookie值
	 *
	 * @param req
	 * @param cookieName
	 * @return
	 */
	public static String getCookie(HttpServletRequest req, String cookieName) {
		Cookie[] cookies = req.getCookies();
		if (cookies != null) {
			for (Cookie c : cookies) {
				if (c.getName().equals(cookieName)) {
					return c.getValue();
				}
			}
		}
		return null;
	}

	/**
	 * 设置cookie
	 */
	public static void addCookie(HttpServletRequest req, HttpServletResponse res, String cookieName,
			String cookieValue) {
		Cookie c = new Cookie(cookieName, cookieValue);
		c.setPath(req.getContextPath() + "/");
		c.setMaxAge(24 * 60 * 60);// 默认一天
		res.addCookie(c);
	}
}