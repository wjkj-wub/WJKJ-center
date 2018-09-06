package com.miqtech.master.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;

/**
 * 生成树结构的工具类
 */
public class TreeUtils {

	private TreeUtils() {
	}
	
	/**
	 * 生成树形结构
	 */
	public static List<Map<String, Object>> buildOperateTree(List<Map<String, Object>> items) {
		return buildOperateTree(items, "pid", "children", true);
	}
	
	/**
	 * 生成树形结构
	 */
	public static List<Map<String, Object>> buildOperateTree(List<Map<String, Object>> items, boolean showLevel) {
		return buildOperateTree(items, "pid", "children", showLevel);
	}
	
	/**
	 * 生成树形结构
	 */
	public static List<Map<String, Object>> buildOperateTree(List<Map<String, Object>> items, String pidColumn, String childrenColumn, boolean showLevel) {
		List<Map<String, Object>> rootItems = new ArrayList<Map<String, Object>>();

		for (Iterator<Map<String, Object>> it = items.iterator(); it.hasNext();) {
			Map<String, Object> o = it.next();
			if (o.get(pidColumn).toString().equals("0")) {
				if(showLevel) {
					o.put("level", 1);
				}
				rootItems.add(o);
				it.remove();
			}
		}

		rootItems = matchTreeItem(rootItems, items, pidColumn, childrenColumn, showLevel);

		return rootItems;
	}

	/**
	 * 匹配节点
	 */
	private static List<Map<String, Object>> matchTreeItem(List<Map<String, Object>> parents,
			List<Map<String, Object>> childrens, String pidColumn, String childrenColumn, boolean showLevel) {
		for (Map<String, Object> parent : parents) {
			List<Map<String, Object>> children = new ArrayList<Map<String, Object>>();
			for (Iterator<Map<String, Object>> it = childrens.iterator(); it.hasNext();) {
				Map<String, Object> child = it.next();
				if (child.get(pidColumn).equals(parent.get("id"))) {
					if(showLevel) {
						child.put("level", (int) parent.get("level") + 1);
					}
					children.add(child);
					it.remove();
				}
			}
			if (CollectionUtils.isNotEmpty(children)) {
				children = matchTreeItem(children, childrens, pidColumn, childrenColumn, showLevel);
				parent.put(childrenColumn, children);
			}
		}
		return parents;
	}
}
