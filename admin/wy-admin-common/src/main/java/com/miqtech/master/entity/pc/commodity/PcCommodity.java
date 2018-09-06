package com.miqtech.master.entity.pc.commodity;

import javax.persistence.*;
import java.util.Date;

@Table(name = "pc_commodity")
@Entity
public class PcCommodity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/**
	 * 奖品名称
	 */
	private String name;

	/**
	 * 奖品类型 1-Q币 2-话费 3-实物 4-英雄联盟点劵
	 */
	private Integer type;

	/**
	 * 奖品图片
	 */
	private String img;

	/**
	 * 兑换所需娱币
	 */
	private Integer chip;

	/**
	 * 兑换所需现金
	 */
	private Double cash;

	/**
	 * 奖品数量
	 */
	private Integer num;

	/**
	 * 是否上架 1-上架 0-不上架
	 */
	@Column(name = "is_sale")
	private Integer isSale;

	/**
	 * 是否置顶 1-置顶 0-不置顶
	 */
	@Column(name = "is_top")
	private Integer isTop;

	@Column(name = "is_valid")
	private Boolean isValid;

	@Column(name = "update_date")
	private Date updateDate;

	@Column(name = "create_date", updatable = false)
	private Date createDate;

	/**
	 * 奖品描述
	 */
	private String description;

	/**
	 * @return id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * 获取奖品名称
	 *
	 * @return name - 奖品名称
	 */
	public String getName() {
		return name;
	}

	/**
	 * 设置奖品名称
	 *
	 * @param name 奖品名称
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * 获取奖品类型 1-Q币 2-话费 3-实物 4-英雄联盟点劵
	 *
	 * @return type - 奖品类型 1-Q币 2-话费 3-实物 4-英雄联盟点劵
	 */
	public Integer getType() {
		return type;
	}

	/**
	 * 设置奖品类型 1-Q币 2-话费 3-实物 4-英雄联盟点劵
	 *
	 * @param type 奖品类型 1-Q币 2-话费 3-实物 4-英雄联盟点劵
	 */
	public void setType(Integer type) {
		this.type = type;
	}

	/**
	 * 获取奖品图片
	 *
	 * @return img - 奖品图片
	 */
	public String getImg() {
		return img;
	}

	/**
	 * 设置奖品图片
	 *
	 * @param img 奖品图片
	 */
	public void setImg(String img) {
		this.img = img;
	}

	/**
	 * 获取兑换所需娱币
	 *
	 * @return chip - 兑换所需娱币
	 */
	public Integer getChip() {
		return chip;
	}

	/**
	 * 设置兑换所需娱币
	 *
	 * @param chip 兑换所需娱币
	 */
	public void setChip(Integer chip) {
		this.chip = chip;
	}

	/**
	 * 获取兑换所需现金
	 *
	 * @return cash - 兑换所需现金
	 */
	public Double getCash() {
		return cash;
	}

	/**
	 * 设置兑换所需现金
	 *
	 * @param cash 兑换所需现金
	 */
	public void setCash(Double cash) {
		this.cash = cash;
	}

	/**
	 * 获取奖品数量
	 *
	 * @return num - 奖品数量
	 */
	public Integer getNum() {
		return num;
	}

	/**
	 * 设置奖品数量
	 *
	 * @param num 奖品数量
	 */
	public void setNum(Integer num) {
		this.num = num;
	}

	/**
	 * 获取是否上架 1-上架 0-不上架
	 *
	 * @return is_sale - 是否上架 1-上架 0-不上架
	 */
	public Integer getIsSale() {
		return isSale;
	}

	/**
	 * 设置是否上架 1-上架 0-不上架
	 *
	 * @param isSale 是否上架 1-上架 0-不上架
	 */
	public void setIsSale(Integer isSale) {
		this.isSale = isSale;
	}

	/**
	 * 获取是否置顶 1-置顶 0-不置顶
	 *
	 * @return is_top - 是否置顶 1-置顶 0-不置顶
	 */
	public Integer getIsTop() {
		return isTop;
	}

	/**
	 * 设置是否置顶 1-置顶 0-不置顶
	 *
	 * @param isTop 是否置顶 1-置顶 0-不置顶
	 */
	public void setIsTop(Integer isTop) {
		this.isTop = isTop;
	}

	/**
	 * @return is_valid
	 */
	public Boolean getIsValid() {
		return isValid;
	}

	/**
	 * @param isValid
	 */
	public void setIsValid(Boolean isValid) {
		this.isValid = isValid;
	}

	/**
	 * @return update_date
	 */
	public Date getUpdateDate() {
		return updateDate;
	}

	/**
	 * @param updateDate
	 */
	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}

	/**
	 * @return create_date
	 */
	public Date getCreateDate() {
		return createDate;
	}

	/**
	 * @param createDate
	 */
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	/**
	 * 获取奖品描述
	 *
	 * @return description - 奖品描述
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * 设置奖品描述
	 *
	 * @param description 奖品描述
	 */
	public void setDescription(String description) {
		this.description = description;
	}
}