package com.miqtech.master.entity;

import java.io.Serializable;
import java.util.Map;

/**
 * 针对手机端接口调用响应数据
 */
public class JsonResponseMsg implements Serializable {
	private static final long serialVersionUID = -6067549589990462156L;

	private int code;//响应代码
	private String result;//响应代码对应信息
	private Object object;//响应对象数据
	private Map<String, Object> extend;// 扩展数据

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public Object getObject() {
		return object;
	}

	public void setObject(Object object) {
		this.object = object;
	}

	public Map<String, Object> getExtend() {
		return extend;
	}

	public void setExtend(Map<String, Object> extend) {
		this.extend = extend;
	}

	public JsonResponseMsg fill(int code, String result) {
		this.setCode(code);
		this.setResult(result);
		return this;
	}

	public JsonResponseMsg fill(int code, String result, Object object) {
		this.fill(code, result);
		this.setObject(object);
		return this;
	}

	public JsonResponseMsg fill(int code, String result, Object object, Map<String, Object> extend) {
		fill(code, result, object).setExtend(extend);
		return this;
	}

}
