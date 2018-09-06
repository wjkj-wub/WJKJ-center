package com.miqtech.master.utils;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PinyinUtils {
	private static final Logger LOGGER = LoggerFactory.getLogger(PinyinUtils.class);
	private static final HanyuPinyinOutputFormat FORMAT = new HanyuPinyinOutputFormat();

	/**
	 * 转换汉字为简单的拼音
	 * 	注：多音字取首个拼音，多个字之间以空格隔开
	 */
	public static final String toSimplePinyin(String word) {
		FORMAT.setToneType(HanyuPinyinToneType.WITHOUT_TONE);

		StringBuffer result = new StringBuffer();

		for (char singleWord : word.toCharArray()) {
			try {
				String[] pys = PinyinHelper.toHanyuPinyinStringArray(singleWord, FORMAT);
				if (result.length() > 0) {
					result.append(" ");
				}
				result.append(pys[0]);
			} catch (Exception e) {
				LOGGER.error("转换拼音（" + singleWord + "）异常：", e);
			}
		}

		return result.toString();
	}

}
