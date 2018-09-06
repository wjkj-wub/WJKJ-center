package com.miqtech.master.entity.netbar.resource;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "netbar_resource_commodity")
public class NetbarResourceCommodity extends IdEntity {
	private static final long serialVersionUID = 6848144236819767499L;
	private String name;
	private String title;
	private String url;//图
	private Integer qualifications;//基础资格
	private String province;
	private String introduce;
	private String description;
	private Float useQuoRatio;//使用配额奖金比例
	private Integer isTop;//是否置顶
	private Integer comTag;//商品标签：1 促销  2 打折  3 热销
	private Integer type;
	private Long categoryId;//netbar_commodity_category表ID
	private String executes;//执行人
	private String executePhone;
	private Integer isRecommend;//是否推荐，0-否，1-是

	private List<NetbarResourceCommodityProperty> properties;
	private List<NetbarResourceCommodityProperty> blocks;//Transient

	@Transient
	public List<NetbarResourceCommodityProperty> getBlocks() {
		return blocks;
	}

	public void setBlocks(List<NetbarResourceCommodityProperty> blocks) {
		this.blocks = blocks;
	}

	@Column(name = "com_tag")
	public Integer getComTag() {
		return comTag;
	}

	public void setComTag(Integer comTag) {
		this.comTag = comTag;
	}

	@Column(name = "name")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "tital")
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Column(name = "url")
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Column(name = "qualifications")
	public Integer getQualifications() {
		return qualifications;
	}

	public void setQualifications(Integer qualifications) {
		this.qualifications = qualifications;
	}

	@Column(name = "province")
	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	@Column(name = "introduce")
	public String getIntroduce() {
		return introduce;
	}

	public void setIntroduce(String introduce) {
		this.introduce = introduce;
	}

	@Column(name = "description")
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Column(name = "use_quo_ratio")
	public Float getUseQuoRatio() {
		return useQuoRatio;
	}

	public void setUseQuoRatio(Float useQuoRatio) {
		this.useQuoRatio = useQuoRatio;
	}

	@Column(name = "is_top")
	public Integer getIsTop() {
		return isTop;
	}

	public void setIsTop(Integer isTop) {
		this.isTop = isTop;
	}

	@Column(name = "type")
	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	@Column(name = "category_id")
	public Long getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(Long categoryId) {
		this.categoryId = categoryId;
	}

	@Column(name = "executes")
	public String getExecutes() {
		return executes;
	}

	public void setExecutes(String executes) {
		this.executes = executes;
	}

	@Column(name = "execute_phone")
	public String getExecutePhone() {
		return executePhone;
	}

	public void setExecutePhone(String executePhone) {
		this.executePhone = executePhone;
	}

	@Column(name = "is_recommend")
	public Integer getIsRecommend() {
		return isRecommend;
	}

	public void setIsRecommend(Integer isRecommend) {
		this.isRecommend = isRecommend;
	}

	@Transient
	public List<NetbarResourceCommodityProperty> getProperties() {
		return properties;
	}

	public void setProperties(List<NetbarResourceCommodityProperty> properties) {
		this.properties = properties;
	}

}
