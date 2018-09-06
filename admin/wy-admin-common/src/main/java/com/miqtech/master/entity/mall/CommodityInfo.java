package com.miqtech.master.entity.mall;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "mall_t_commodity")
public class CommodityInfo extends IdEntity {
	private static final long serialVersionUID = -4866663488352312015L;

	private Integer sortNo; //排序编号（越小越靠前）
	private String itemNo; //商品编号
	private Long areaId; //商品区ID
	private Long categoryId; //商品类别ID
	private Integer thirdType; //第三方充值类型：1-Q币，2-话费，3-流量
	private String name; //商品名
	private Integer price; //原价
	private Float discount; //折扣（现价=原价*（折扣/10））
	private Integer discountPrice; //折后价
	private Double probability; //抽中概率
	private String introduce; //商品介绍
	private String rule; //商品规则
	private String province; //商品地区（省份）
	private Integer topShow; //是否置顶：0-否，1-置顶，2-首页
	private Integer status; //状态：0-下架，1-上架
	private String informationDefualt; //交易信息（默认）
	private Integer inventory; //库存
	private Integer totalInventory; //原总库存
	private Integer limit; //购买限制（一个手机号对应的可兑换/购买数量）
	private Integer quota; //额度，例如红包数额
	private Date startDate; //活动商品的开始时间
	private Date endDate; //活动商品的结束时间
	private Integer isProvide;//是否需要发放兑换码0否1是

	private Integer superType;//大分类
	private Integer coins;//总金币
	private Integer purTimes;//购买次数
	private Integer autoDrawn;//自动开奖 1:是，0:否
	private Integer crowdfundStatus;//众筹状态:0-众筹中,1-等待开奖,10-已开奖
	private Integer shelveRecord;//购买记录 1有购买记录

	@Column(name = "shelve_record")
	public Integer getShelveRecord() {
		return shelveRecord;
	}

	public void setShelveRecord(Integer shelveRecord) {
		this.shelveRecord = shelveRecord;
	}

	@Column(name = "super_type")
	public Integer getSuperType() {
		return superType;
	}

	public void setSuperType(Integer superType) {
		this.superType = superType;
	}

	@Column(name = "auto_drawn")
	public Integer getAutoDrawn() {
		return autoDrawn;
	}

	public void setAutoDrawn(Integer autoDrawn) {
		this.autoDrawn = autoDrawn;
	}

	@Column(name = "sort_no")
	public Integer getSortNo() {
		return sortNo;
	}

	public void setSortNo(Integer sortNo) {
		this.sortNo = sortNo;
	}

	@Column(name = "item_no")
	public String getItemNo() {
		return itemNo;
	}

	public void setItemNo(String itemNo) {
		this.itemNo = itemNo;
	}

	@Column(name = "area_id")
	public Long getAreaId() {
		return areaId;
	}

	public void setAreaId(Long areaId) {
		this.areaId = areaId;
	}

	@Column(name = "category_id")
	public Long getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(Long categoryId) {
		this.categoryId = categoryId;
	}

	@Column(name = "third_type")
	public Integer getThirdType() {
		return thirdType;
	}

	public void setThirdType(Integer thirdType) {
		this.thirdType = thirdType;
	}

	@Column(name = "discount")
	public Float getDiscount() {
		return discount;
	}

	public void setDiscount(Float discount) {
		this.discount = discount;
	}

	@Column(name = "province")
	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	@Column(name = "top_show")
	public Integer getTopShow() {
		return topShow;
	}

	public void setTopShow(Integer topShow) {
		this.topShow = topShow;
	}

	@Column(name = "status")
	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	@Column(name = "total_inventory")
	public Integer getTotalInventory() {
		return totalInventory;
	}

	public void setTotalInventory(Integer totalInventory) {
		this.totalInventory = totalInventory;
	}

	@Column(name = "name")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "price")
	public Integer getPrice() {
		return price;
	}

	public void setPrice(Integer price) {
		this.price = price;
	}

	@Column(name = "discount_price")
	public Integer getDiscountPrice() {
		return discountPrice;
	}

	public void setDiscountPrice(Integer discountPrice) {
		this.discountPrice = discountPrice;
	}

	@Column(name = "probability")
	public Double getProbability() {
		return probability;
	}

	public void setProbability(Double probability) {
		this.probability = probability;
	}

	@Column(name = "introduce")
	public String getIntroduce() {
		return introduce;
	}

	public void setIntroduce(String introduce) {
		this.introduce = introduce;
	}

	@Column(name = "rule")
	public String getRule() {
		return rule;
	}

	public void setRule(String rule) {
		this.rule = rule;
	}

	@Column(name = "information_defualt")
	public String getInformationDefualt() {
		return informationDefualt;
	}

	public void setInformationDefualt(String informationDefualt) {
		this.informationDefualt = informationDefualt;
	}

	@Column(name = "inventory")
	public Integer getInventory() {
		return inventory;
	}

	public void setInventory(Integer inventory) {
		this.inventory = inventory;
	}

	@Column(name = "`limit`")
	public Integer getLimit() {
		return limit;
	}

	public void setLimit(Integer limit) {
		this.limit = limit;
	}

	@Column(name = "quota")
	public Integer getQuota() {
		return quota;
	}

	public void setQuota(Integer quota) {
		this.quota = quota;
	}

	@Column(name = "start_date")
	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	@Column(name = "end_date")
	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	@Column(name = "is_provide")
	public Integer getIsProvide() {
		return isProvide;
	}

	public void setIsProvide(Integer isProvide) {
		this.isProvide = isProvide;
	}

	@Column(name = "pur_times")
	public Integer getPurTimes() {
		return purTimes;
	}

	public void setPurTimes(Integer purTimes) {
		this.purTimes = purTimes;
	}

	@Column(name = "coins")
	public Integer getCoins() {
		return coins;
	}

	public void setCoins(Integer coins) {
		this.coins = coins;
	}

	@Column(name = "crowdfund_status")
	public Integer getCrowdfundStatus() {
		return crowdfundStatus;
	}

	public void setCrowdfundStatus(Integer crowdfundStatus) {
		this.crowdfundStatus = crowdfundStatus;
	}

}
