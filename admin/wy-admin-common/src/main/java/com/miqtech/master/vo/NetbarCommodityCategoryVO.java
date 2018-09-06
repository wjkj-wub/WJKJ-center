package com.miqtech.master.vo;

import java.util.ArrayList;
import java.util.List;

import com.miqtech.master.entity.netbar.resource.NetbarCommodityCategory;

public class NetbarCommodityCategoryVO {
	private NetbarCommodityCategory parent;
	private List<NetbarCommodityCategory> sub = new ArrayList<NetbarCommodityCategory>();

	public NetbarCommodityCategory getParent() {
		return parent;
	}

	public void setParent(NetbarCommodityCategory parent) {
		this.parent = parent;
	}

	public List<NetbarCommodityCategory> getSub() {
		return sub;
	}

	public void setSub(List<NetbarCommodityCategory> sub) {
		this.sub = sub;
	}

}
