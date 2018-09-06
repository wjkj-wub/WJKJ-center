package com.miqtech.master.enumConstant.taskMatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 任务赛条件符号
 *
 * @author shilina
 * @create 2017年09月01日
 */
public enum TaskMatchConditionSymbolEnum {
	ADD(1, "加"), REDUCE(2, "减"), GREATER_THAN(3, "大于"), LESS_THAN(4, "小于");

	private Integer id;
	private String symbol;

	TaskMatchConditionSymbolEnum(Integer id, String symbol) {
		this.id = id;
		this.symbol = symbol;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}


	/**
	 * 获取符号列表
	 */
	public static List<Map<String,Object>> getList() {
		List<Map<String,Object>> list=new ArrayList<>();
		for (TaskMatchConditionSymbolEnum paramEnum : TaskMatchConditionSymbolEnum.values()) {
			Map<String,Object> map=new HashMap<>();
			map.put("id",paramEnum.getId());
			map.put("symbol",paramEnum.getSymbol());
			list.add(map);
		}
		return list;
	}
}
