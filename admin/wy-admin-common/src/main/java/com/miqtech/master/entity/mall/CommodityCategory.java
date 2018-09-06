package com.miqtech.master.entity.mall;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "mall_t_commodity_category")
public class CommodityCategory extends IdEntity {
	private static final long serialVersionUID = -1778623389856461136L;

	private String name; //商品类别名称
	private Integer superType;// 所属类别:1-自有商品,2-充值,3-库存
	private Integer type;// 类别

	@Column(name = "name")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "super_type")
	public Integer getSuperType() {
		return superType;
	}

	public void setSuperType(Integer superType) {
		this.superType = superType;
	}

	@Column(name = "type")
	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}
}
