package com.miqtech.master.entity.pc.commodity;

import javax.persistence.*;
import java.util.Date;

@Table(name = "pc_commodity_exchange")
@Entity
public class PcCommodityExchange {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id")
	private Long userId;

	/**
	 * 商品id
	 */
	@Column(name = "commodity_id")
	private Long commodityId;

	/**
	 * 兑换数量
	 */
	private Integer num;

	/**
	 * qq号
	 */
	private String qq;

	/**
	 * 手机
	 */
	private String telephone;

	/**
	 * 地区编号
	 */
	@Column(name = "area_code")
	private String areaCode;

	/**
	 * 详细地址
	 */
	private String address;

	/**
	 * 1-充值中 2-充值成功 3-充值失败
	 */
	private Integer status;

	@Column(name = "is_valid")
	private Boolean isValid;

	@Column(name = "update_date")
	private Date updateDate;

	@Column(name = "create_date")
	private Date createDate;

	@Column(name = "out_trade_no")
	private String outTradeNo;

	public PcCommodityExchange() {

	}

	public PcCommodityExchange(Long userId, Long commodityId, Integer num, String qq, String telephone, String areaCode,
			String addresss, Integer status, Boolean isValid, Date createDate, String outTradeNo) {
		this.userId = userId;
		this.commodityId = commodityId;
		this.num = num;
		this.qq = qq;
		this.telephone = telephone;
		this.areaCode = areaCode;
		this.address = address;
		this.status = status;
		this.isValid = isValid;
		this.createDate = createDate;
		this.outTradeNo = outTradeNo;

	}

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
	 * @return user_id
	 */
	public Long getUserId() {
		return userId;
	}

	/**
	 * @param userId
	 */
	public void setUserId(Long userId) {
		this.userId = userId;
	}

	/**
	 * 获取商品id
	 *
	 * @return commodity_id - 商品id
	 */
	public Long getCommodityId() {
		return commodityId;
	}

	/**
	 * 设置商品id
	 *
	 * @param commodityId 商品id
	 */
	public void setCommodityId(Long commodityId) {
		this.commodityId = commodityId;
	}

	/**
	 * 获取兑换数量
	 *
	 * @return num - 兑换数量
	 */
	public Integer getNum() {
		return num;
	}

	/**
	 * 设置兑换数量
	 *
	 * @param num 兑换数量
	 */
	public void setNum(Integer num) {
		this.num = num;
	}

	/**
	 * 获取qq号
	 *
	 * @return qq - qq号
	 */
	public String getQq() {
		return qq;
	}

	/**
	 * 设置qq号
	 *
	 * @param qq qq号
	 */
	public void setQq(String qq) {
		this.qq = qq;
	}

	/**
	 * 获取手机
	 *
	 * @return telephone - 手机
	 */
	public String getTelephone() {
		return telephone;
	}

	/**
	 * 设置手机
	 *
	 * @param telephone 手机
	 */
	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}

	/**
	 * 获取地区编号
	 *
	 * @return area_code - 地区编号
	 */
	public String getAreaCode() {
		return areaCode;
	}

	/**
	 * 设置地区编号
	 *
	 * @param areaCode 地区编号
	 */
	public void setAreaCode(String areaCode) {
		this.areaCode = areaCode;
	}

	/**
	 * 获取详细地址
	 *
	 * @return addresss - 详细地址
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * 设置详细地址
	 *
	 * @param address 详细地址
	 */
	public void setAddress(String address) {
		this.address = address;
	}

	/**
	 * 获取1-充值中 2-充值成功 3-充值失败
	 *
	 * @return status - 1-充值中 2-充值成功 3-充值失败
	 */
	public Integer getStatus() {
		return status;
	}

	/**
	 * 设置1-充值中 2-充值成功 3-充值失败
	 *
	 * @param status 1-充值中 2-充值成功 3-充值失败
	 */
	public void setStatus(Integer status) {
		this.status = status;
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

	public String getOutTradeNo() {
		return outTradeNo;
	}

	public void setOutTradeNo(String outTradeNo) {
		this.outTradeNo = outTradeNo;
	}
}