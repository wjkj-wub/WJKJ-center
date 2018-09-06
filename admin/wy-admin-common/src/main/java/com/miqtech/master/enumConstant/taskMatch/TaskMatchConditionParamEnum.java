package com.miqtech.master.enumConstant.taskMatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 任务赛条件参数
 *
 * @author shilina
 * @create 2017年09月01日
 */
public enum TaskMatchConditionParamEnum {
	KILL(1, "杀人数"), ASSIST(2, "助攻数"), DEATH(3, "死亡数"), MONEY(4, "金钱数"), VICTORY(5, "胜利数"), DEFEATED(6,
			"失败数"), DESTROY_TOWER(7, "推塔数");

	private Integer id;
	private String name;

	TaskMatchConditionParamEnum(Integer id, String name) {
		this.id = id;
		this.name = name;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * 获取条件参数列表
	 */
	public static List<Map<String,Object>> getList() {
		List<Map<String,Object>> list=new ArrayList<>();
		for (TaskMatchConditionParamEnum paramEnum : TaskMatchConditionParamEnum.values()) {
			Map<String,Object> map=new HashMap<>();
			map.put("id",paramEnum.getId());
			map.put("name",paramEnum.getName());
			list.add(map);
		}
		return list;
	}

}
