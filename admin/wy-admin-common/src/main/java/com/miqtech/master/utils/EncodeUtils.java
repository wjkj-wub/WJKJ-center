package com.miqtech.master.utils;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.HtmlUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 封装常用格式的编码解码加密工具类.
 * 1.Commons-Codec的 hex/base64 编码
 * 2.Commons-Lang的xml/html escape
 * 3.JDK提供的URLEncoder
 * 4.MD5加密
 */
public class EncodeUtils {

	private EncodeUtils() {
		super();
	}

	private static final String DEFAULT_URL_ENCODING = "UTF-8";
	private static final Logger LOGGER = LoggerFactory.getLogger(EncodeUtils.class);

	/**
	 * Hex编码.
	 */
	public static String encodeHex(byte[] input) {
		return Hex.encodeHexString(input);
	}

	/**
	 * Hex解码.
	 * @throws DecoderException
	 */
	public static byte[] decodeHex(String input) throws DecoderException {
		return Hex.decodeHex(input.toCharArray());
	}

	/**
	 * Base64编码.
	 */
	public static String encodeBase64(byte[] input) {
		return Base64.encodeBase64String(input);
	}

	/**
	 * Base64解码.
	 */
	public static byte[] decodeBase64(String input) {
		return Base64.decodeBase64(input);
	}

	/**
	 * Base64编码, URL安全(将Base64中的URL非法字符'+'和'/'转为'-'和'_', 见RFC3548).
	 */
	public static String encodeUrlSafeBase64(byte[] input) {
		return Base64.encodeBase64URLSafeString(input);
	}

	/**
	 * Html 转码.
	 */
	public static String escapeHtml(String html) {
		return StringEscapeUtils.escapeHtml4(html);
	}

	/**
	 * Html 解码.
	 */
	public static String unescapeHtml(String htmlEscaped) {
		return StringEscapeUtils.unescapeHtml4(htmlEscaped);
	}

	/**
	 * Xml 转码.
	 */
	public static String escapeXml(String xml) {
		return StringEscapeUtils.escapeXml11(xml);
	}

	/**
	 * Xml 解码.
	 */
	public static String unescapeXml(String xmlEscaped) {
		return StringEscapeUtils.unescapeXml(xmlEscaped);
	}

	/**
	 * URL 编码, Encode默认为UTF-8.
	 * @throws UnsupportedEncodingException
	 */
	public static String urlEncode(String part) throws UnsupportedEncodingException {
		return URLEncoder.encode(part, DEFAULT_URL_ENCODING);
	}

	/**
	 * URL 解码, Decode默认为UTF-8.
	 * @throws UnsupportedEncodingException
	 */
	public static String urlDecode(String part) throws UnsupportedEncodingException {

		return URLDecoder.decode(part, DEFAULT_URL_ENCODING);
	}

	/**
	 * MD5加密
	 */
	public static String hexMd5(String input) {
		if (input == null || "".equals(input.trim())) {
			return null;
		}
		String encryptText = null;
		try {
			MessageDigest m = MessageDigest.getInstance("md5");
			m.update(input.getBytes(DEFAULT_URL_ENCODING));
			byte[] s = m.digest();
			return encodeHex(s);
		} catch (Exception e) {
			LOGGER.error("HEX-MD5加密[{}]时出现异常:{}", input, e);
		}
		return encryptText;
	}

	public static String base64Md5(String input) {
		if (input == null || "".equals(input.trim())) {
			return null;
		}
		String encryptText = null;
		try {
			MessageDigest m = MessageDigest.getInstance("md5");
			m.update(input.getBytes(DEFAULT_URL_ENCODING));
			byte[] s = m.digest();
			return encodeBase64(s);
		} catch (Exception e) {
			LOGGER.error("Base64-MD5加密[{}]时出现异常:{}", input, e);
		}
		return encryptText;
	}

	/**
	* @return 32位密文
	*/
	public static String base32Md5(String plainText) {
		String re_md5 = new String();
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(plainText.getBytes());
			byte[] b = md.digest();

			int i;

			StringBuffer buf = new StringBuffer("");
			for (int offset = 0; offset < b.length; offset++) {
				i = b[offset];
				if (i < 0) {
					i += 256;
				}
				if (i < 16) {
					buf.append("0");
				}
				buf.append(Integer.toHexString(i));
			}

			re_md5 = buf.toString();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return re_md5;
	}

	/**
	 * Java自带的默认MD5
	 */
	public final static String MD5(String s) {
		char[] hexDigits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
		try {
			byte[] btInput = s.getBytes();
			// 获得MD5摘要算法的 MessageDigest 对象
			MessageDigest mdInst = MessageDigest.getInstance("MD5");
			// 使用指定的字节更新摘要
			mdInst.update(btInput);
			// 获得密文
			byte[] md = mdInst.digest();
			// 把密文转换成十六进制的字符串形式
			int j = md.length;
			char[] str = new char[j * 2];
			int k = 0;
			for (int i = 0; i < j; i++) {
				byte byte0 = md[i];
				str[k++] = hexDigits[byte0 >>> 4 & 0xf];
				str[k++] = hexDigits[byte0 & 0xf];
			}
			return new String(str);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 过滤html标签
	 */
	public final static String htmlEscape(String str) {
		str = HtmlUtils.htmlUnescape(str);
		return HtmlUtils.htmlEscape(str);
	}

	/**
	 * 还原html标签
	 * @param str
	 * @return
	 */
	public final static String htmlUnEscape(String str) {
		return HtmlUtils.htmlUnescape(str);
	}

}
