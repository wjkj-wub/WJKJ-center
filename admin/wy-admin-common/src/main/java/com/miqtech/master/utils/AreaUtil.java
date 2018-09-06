package com.miqtech.master.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * areacode工具
 *
 */
public class AreaUtil {

	/**得到模糊areaCode
	 * @param areaCode
	 * @return
	 */
	public static String getAreaCode(String areaCode) {
		if (StringUtils.isNotBlank(areaCode)) {
			String[] array = areaCode.split(",");
			if (array != null && array.length > 0) {
				areaCode = array[array.length - 1];
				if (areaCode.length() == 6) {
					if (areaCode.substring(2, 6).equals("0000")) {
						areaCode = areaCode.substring(0, 2);
					} else if (areaCode.substring(4, 6).equals("00")) {
						areaCode = areaCode.substring(0, 4);
					}
				} else {
					return "3301";
				}

			} else {
				areaCode = "3301";
			}
		}
		return areaCode;
	}

	/**得到最后的areaCode
	 * @param areaCode
	 * @return
	 */
	public static String getLastAreaCode(String areaCode) {
		String[] array = areaCode.split(",");
		if (array.length > 0) {
			return array[array.length - 1];
		}
		return null;
	}

	/**得到模糊code列表
	 * @param areaCode
	 * @return
	 */
	public static List<String> getAreaCodeArray(String areaCode) {
		List<String> result = new ArrayList<String>();
		if (StringUtils.isNotBlank(areaCode)) {
			if (areaCode.charAt(0) == ',') {
				areaCode = areaCode.substring(1, areaCode.length());
			}
			String[] array = areaCode.split(",");
			if (array != null && array.length > 0) {
				for (String s : array) {
					if (s.substring(2, 6).equals("0000")) {
						s = s.substring(0, 2);
					} else if (s.substring(4, 6).equals("00")) {
						s = s.substring(0, 4);
					}
					result.add(s);
				}

			} else {
				areaCode = null;
			}
		}
		return result;
	}

	/**得到前两位省的code
	 * @param areaCode
	 * @return
	 */
	public static String getProvinceCode(String areaCode) {
		if (StringUtils.isBlank(areaCode) || areaCode.length() < 6) {
			return null;
		}
		return areaCode.substring(0, 2);
	}

}
