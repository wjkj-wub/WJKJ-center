package com.miqtech.master.exception;

/**
 * 参数错误异常
 * @author zhangyuqi
 * 2017年06月19日
 */
public class ParameterErrorException extends Exception {
	private static final long serialVersionUID = 2038938485426526532L;

	public ParameterErrorException() {
	}

	public ParameterErrorException(String e) {
		super(e);
	}

}
