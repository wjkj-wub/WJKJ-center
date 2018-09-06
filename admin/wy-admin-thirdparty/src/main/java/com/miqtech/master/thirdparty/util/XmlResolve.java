package com.miqtech.master.thirdparty.util;

import java.util.ArrayList;

/**
 * XML报文的相关处理
 */
public class XmlResolve {
	/**
	 * XML字符串
	 */
	private String iXMLString = "";

	/**
	 * 构造XMLDocument对象。
	 */
	public XmlResolve() {
	}

	/**
	 * 构造XMLDocument对象，并给定初始对象的XML字符串。
	 */
	public XmlResolve(String aXMLString) {
		iXMLString = aXMLString;
	}

	/**
	 * 回传XML字符串
	 * @return XML字符串
	 */
	@Override
	public String toString() {
		return iXMLString;
	}

	/**
	 * 回传XML文件中指定域的值
	 * @param aTag 域名
	 * @return 指定域的值
	 */
	public XmlResolve getValue(String aTag) {
		XmlResolve tXMLDocument = null;
		int tStartIndex = iXMLString.indexOf("<" + aTag.trim() + ">");
		int tEndIndex = iXMLString.indexOf("</" + aTag.trim() + ">");
		if ((tStartIndex >= 0) && (tEndIndex >= 0) && (tStartIndex < tEndIndex)) {
			tXMLDocument = new XmlResolve(iXMLString.substring(tStartIndex + aTag.length() + 2, tEndIndex));
		}
		return tXMLDocument;
	}

	/**
	 * 回传XML文件中指定域的值
	 * @param aTag 域名
	 * @return 指定域的值，若找不到该域则回传空字符串。
	 */
	public String getValueNoNull(String aTag) {
		String tValue = "";
		XmlResolve tXML = getValue(aTag);
		if (tXML != null) {
			tValue = tXML.toString();
		}
		return tValue;
	}

	/**
	 * 回传XML文件中指定域名的集合
	 * @param aTag 域名
	 * @return XMLDocument对象的ArrayList集合。
	 */
	public ArrayList<XmlResolve> getDocuments(String aTag) {
		String tXMLString = iXMLString;
		ArrayList<XmlResolve> tValues = new ArrayList<XmlResolve>();

		while (true) {
			XmlResolve tXMLDocument = null;
			int tStartIndex = tXMLString.indexOf("<" + aTag.trim() + ">");
			int tEndIndex = tXMLString.indexOf("</" + aTag.trim() + ">");
			if ((tStartIndex == -1) || (tEndIndex == -1) || (tStartIndex > tEndIndex)) {
				break;
			}
			tXMLDocument = new XmlResolve(tXMLString.substring(tStartIndex, tEndIndex + aTag.length() + 3));
			tValues.add(tXMLDocument);
			tXMLString = tXMLString.substring(tEndIndex + 1);
		}
		return tValues;
	}

	/**
	 * 回传经过格式化排版的XML文件
	 * @param  aTag 域名
	 * @return 经过格式化排版的XML文件
	 */
	public XmlResolve getFormatDocument(String aSpace) {
		return getFormatDocument(0, aSpace);
	}

	/**
	 * 回传经过格式化排版的XML文件
	 * @param  aTag 域名
	 * @return 经过格式化排版的XML文件
	 */
	private XmlResolve getFormatDocument(int aLevel, String aSpace) {
		String tSpace1 = aSpace;
		for (int i = 0; i < aLevel; i++) {
			tSpace1 += aSpace;
		}
		String tTagName = getFirstTagName();
		if (tTagName == null) {
			return this;
		}
		String tXMLString = "\n";
		XmlResolve tXMLDocument = new XmlResolve(iXMLString);
		while ((tTagName = tXMLDocument.getFirstTagName()) != null) {
			XmlResolve tTemp = tXMLDocument.getValue(tTagName);
			String tSpace = "";
			if (tTemp.getFirstTagName() != null) {
				tSpace = tSpace1;
			}
			tXMLString = tXMLString + tSpace1 + "<" + tTagName + ">" + tTemp.getFormatDocument(aLevel + 1, aSpace)
					+ tSpace + "</" + tTagName + ">\n";
			tXMLDocument = tXMLDocument.deleteFirstTagDocument();
		}
		return new XmlResolve(tXMLString);
	}

	/**
	 * 回传XML文件的第一个Tag名称
	 * @return String Tag名称
	 */
	public String getFirstTagName() {
		String tTagName = null;
		int tStartIndex = iXMLString.indexOf('<');
		int tEndIndex = iXMLString.indexOf('>');
		if (tEndIndex > tStartIndex) {
			tTagName = iXMLString.substring(tStartIndex + 1, tEndIndex);
		}
		return tTagName;
	}

	/**
	 * 删除XML文件中第一个Tab的资料段
	 * @return String Tag名称
	 */
	public XmlResolve deleteFirstTagDocument() {
		String tTagName = this.getFirstTagName();
		int tStartIndex = iXMLString.indexOf("<" + tTagName + ">");
		int tEndIndex = iXMLString.indexOf("</" + tTagName + ">");
		if (tEndIndex > tStartIndex) {
			iXMLString = iXMLString.substring(tEndIndex + tTagName.length() + 3);
		}
		return this;
	}

	//	public static void main(String[] argc) {
	//		XmlResolve tXMLDocument = new XmlResolve(
	//				"<?xml version=”1.0” encoding=”GB2312” ?><userinfo><err_msg>参数格式错误</err_msg><err_msg /><retcode>1</retcode><ret_leftcredit>352328.97</ret_leftcredit><userid>AXXXXX</userid></userinfo>");
	//		System.out.println(tXMLDocument.getFormatDocument("    ").toString());
	//		System.out.println(tXMLDocument.getValue("retcode"));
	//		System.out.println(EncodeUtils.base32Md5("wyds147852369"));
	//	}
}
