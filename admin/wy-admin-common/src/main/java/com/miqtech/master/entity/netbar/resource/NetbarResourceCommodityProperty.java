package com.miqtech.master.entity.netbar.resource;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "netbar_resource_commodity_property")
public class NetbarResourceCommodityProperty extends IdEntity {
	private static final long serialVersionUID = 6848144236819767499L;
	private Long commodityId;
	private String propertyNo; //商品编号
	private String propertyName;
	private Float price;
	private Float rebate;//折后金额
	private Date settlDate;//可售日期
	private Float conditions;//满多少
	private String measure;//单位（描述）
	private Integer unit;//起售数量
	private Integer inventoryTotal;//总库存
	private Integer inventory;//剩余库存
	private Integer qualifiType;//购买资格类型0 无条件  1 流水满  2 必须买  3 满减
	private Integer status;//0下架 1 待确认  2 发布中3已确认
	private Float goldRebate;//金牌折扣
	private Float vipRatio;//会员折扣
	private Float jewelRatio;//钻石折扣
	private Integer validity;//商品有效期天数
	private Integer interestNum;//感兴趣数
	private Long redbagId;//系统红包id
	private Integer cateType;//商品出售类型 0 按照日期  1 按照数量
	private String settlDates;//Transient
	private Long conditionsId;//Transient
	private Integer isTop;//是否置顶

	private Integer fakeSoldNum;//已售数量

	@Column(name = "fake_sold_num")
	public Integer getFakeSoldNum() {
		return fakeSoldNum;
	}

	public void setFakeSoldNum(Integer fakeSoldNum) {
		this.fakeSoldNum = fakeSoldNum;
	}

	@Transient
	public String getSettlDates() {
		return settlDates;
	}

	public void setSettlDates(String settlDates) {
		this.settlDates = settlDates;
	}

	@Transient
	public Long getConditionsId() {
		return conditionsId;
	}

	public void setConditionsId(Long conditionsId) {
		this.conditionsId = conditionsId;
	}

	@Column(name = "property_no")
	public String getPropertyNo() {
		return propertyNo;
	}

	public void setPropertyNo(String propertyNo) {
		this.propertyNo = propertyNo;
	}

	@Column(name = "measure")
	public String getMeasure() {
		return measure;
	}

	public void setMeasure(String measure) {
		this.measure = measure;
	}

	@Column(name = "inventory_total")
	public Integer getInventoryTotal() {
		return inventoryTotal;
	}

	public void setInventoryTotal(Integer inventoryTotal) {
		this.inventoryTotal = inventoryTotal;
	}

	@Column(name = "inventory")
	public Integer getInventory() {
		return inventory;
	}

	public void setInventory(Integer inventory) {
		this.inventory = inventory;
	}

	@Column(name = "status")
	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	@Column(name = "commodity_id")
	public Long getCommodityId() {
		return commodityId;
	}

	public void setCommodityId(Long commodityId) {
		this.commodityId = commodityId;
	}

	@Column(name = "name")
	public String getPropertyName() {
		return propertyName;
	}

	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

	@Column(name = "price")
	public Float getPrice() {
		return price;
	}

	public void setPrice(Float price) {
		this.price = price;
	}

	@Column(name = "rebate")
	public Float getRebate() {
		return rebate;
	}

	public void setRebate(Float rebate) {
		this.rebate = rebate;
	}

	@Column(name = "settl_date")
	public Date getSettlDate() {
		return settlDate;
	}

	public void setSettlDate(Date settlDate) {
		this.settlDate = settlDate;
	}

	@Column(name = "conditions")
	public Float getConditions() {
		return conditions;
	}

	public void setConditions(Float conditions) {
		this.conditions = conditions;
	}

	@Column(name = "unit")
	public Integer getUnit() {
		return unit;
	}

	public void setUnit(Integer unit) {
		this.unit = unit;
	}

	@Column(name = "qualifi_type")
	public Integer getQualifiType() {
		return qualifiType;
	}

	public void setQualifiType(Integer qualifiType) {
		this.qualifiType = qualifiType;
	}

	@Column(name = "gold_rebate")
	public Float getGoldRebate() {
		return goldRebate;
	}

	public void setGoldRebate(Float goldRebate) {
		this.goldRebate = goldRebate;
	}

	@Column(name = "vip_ratio")
	public Float getVipRatio() {
		return vipRatio;
	}

	public void setVipRatio(Float vipRatio) {
		this.vipRatio = vipRatio;
	}

	@Column(name = "jewel_ratio")
	public Float getJewelRatio() {
		return jewelRatio;
	}

	public void setJewelRatio(Float jewelRatio) {
		this.jewelRatio = jewelRatio;
	}

	@Column(name = "validity")
	public Integer getValidity() {
		return validity;
	}

	public void setValidity(Integer validity) {
		this.validity = validity;
	}

	@Column(name = "interest_num")
	public Integer getInterestNum() {
		return interestNum;
	}

	public void setInterestNum(Integer interestNum) {
		this.interestNum = interestNum;
	}

	@Column(name = "redbag_id")
	public Long getRedbagId() {
		return redbagId;
	}

	public void setRedbagId(Long redbagId) {
		this.redbagId = redbagId;
	}

	@Column(name = "cate_type")
	public Integer getCateType() {
		return cateType;
	}

	public void setCateType(Integer cateType) {
		this.cateType = cateType;
	}

	@Column(name = "is_top")
	public Integer getIsTop() {
		return isTop;
	}

	public void setIsTop(Integer isTop) {
		this.isTop = isTop;
	}

}
