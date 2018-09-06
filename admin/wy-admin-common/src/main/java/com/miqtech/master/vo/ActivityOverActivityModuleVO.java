package com.miqtech.master.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ActivityOverActivityModuleVO implements Serializable {
	private static final long serialVersionUID = 481848555500092756L;
	private Map<String, Object> parent;
	private List<Map<String, Object>> sub = new ArrayList<Map<String, Object>>();

	public Map<String, Object> getParent() {
		return parent;
	}

	public void setParent(Map<String, Object> parent) {
		this.parent = parent;
	}

	public List<Map<String, Object>> getSub() {
		return sub;
	}

	public void setSub(List<Map<String, Object>> sub) {
		this.sub = sub;
	}
}
