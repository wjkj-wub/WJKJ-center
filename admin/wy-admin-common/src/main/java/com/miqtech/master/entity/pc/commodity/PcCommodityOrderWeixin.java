package com.miqtech.master.entity.pc.commodity;

import javax.persistence.*;
import java.util.Date;

@Table(name = "pc_commodity_order_weixin")
@Entity
public class PcCommodityOrderWeixin {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/**
	 * 用户ID
	 */
	@Column(name = "user_id")
	private Long userId;

	/**
	 * 充值订单号
	 */
	@Column(name = "trade_no")
	private String tradeNo;

	/**
	 * 商户订单号
	 */
	@Column(name = "out_trade_no")
	private String outTradeNo;

	/**
	 * 消耗人民币
	 */
	private Double cash;

	/**
	 * 预支付ID
	 */
	@Column(name = "prepay_id")
	private String prepayId;

	/**
	 * 支付时的随机字符串,做验签用
	 */
	@Column(name = "nonce_str")
	private String nonceStr;

	/**
	 * 到帐时间
	 */
	@Column(name = "notify_date")
	private Date notifyDate;

	/**
	 * 交易状态:1-待支付,2-支付成功,3-支付失败
	 */
	private Integer status;

	/**
	 * 微信二维码链接内容
	 */
	@Column(name = "code_url")
	private String codeUrl;

	/**
	 * 生成微信支付二维码地址
	 */
	private String qrcode;

	@Column(name = "is_valid")
	private Boolean isValid;

	@Column(name = "create_date")
	private Date createDate;

	@Column(name = "update_date")
	private Date updateDate;

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
	 * 获取用户ID
	 *
	 * @return user_id - 用户ID
	 */
	public Long getUserId() {
		return userId;
	}

	/**
	 * 设置用户ID
	 *
	 * @param userId 用户ID
	 */
	public void setUserId(Long userId) {
		this.userId = userId;
	}

	/**
	 * 获取充值订单号
	 *
	 * @return trade_no - 充值订单号
	 */
	public String getTradeNo() {
		return tradeNo;
	}

	/**
	 * 设置充值订单号
	 *
	 * @param tradeNo 充值订单号
	 */
	public void setTradeNo(String tradeNo) {
		this.tradeNo = tradeNo;
	}

	/**
	 * 获取商户订单号
	 *
	 * @return out_trade_no - 商户订单号
	 */
	public String getOutTradeNo() {
		return outTradeNo;
	}

	/**
	 * 设置商户订单号
	 *
	 * @param outTradeNo 商户订单号
	 */
	public void setOutTradeNo(String outTradeNo) {
		this.outTradeNo = outTradeNo;
	}

	/**
	 * 获取消耗人民币
	 *
	 * @return cash - 消耗人民币
	 */
	public Double getCash() {
		return cash;
	}

	/**
	 * 设置消耗人民币
	 *
	 * @param cash 消耗人民币
	 */
	public void setCash(Double cash) {
		this.cash = cash;
	}

	/**
	 * 获取预支付ID
	 *
	 * @return prepay_id - 预支付ID
	 */
	public String getPrepayId() {
		return prepayId;
	}

	/**
	 * 设置预支付ID
	 *
	 * @param prepayId 预支付ID
	 */
	public void setPrepayId(String prepayId) {
		this.prepayId = prepayId;
	}

	/**
	 * 获取支付时的随机字符串,做验签用
	 *
	 * @return nonce_str - 支付时的随机字符串,做验签用
	 */
	public String getNonceStr() {
		return nonceStr;
	}

	/**
	 * 设置支付时的随机字符串,做验签用
	 *
	 * @param nonceStr 支付时的随机字符串,做验签用
	 */
	public void setNonceStr(String nonceStr) {
		this.nonceStr = nonceStr;
	}

	/**
	 * 获取到帐时间
	 *
	 * @return notify_date - 到帐时间
	 */
	public Date getNotifyDate() {
		return notifyDate;
	}

	/**
	 * 设置到帐时间
	 *
	 * @param notifyDate 到帐时间
	 */
	public void setNotifyDate(Date notifyDate) {
		this.notifyDate = notifyDate;
	}

	/**
	 * 获取交易状态:1-待支付,2-支付成功,3-支付失败
	 *
	 * @return status - 交易状态:1-待支付,2-支付成功,3-支付失败
	 */
	public Integer getStatus() {
		return status;
	}

	/**
	 * 设置交易状态:1-待支付,2-支付成功,3-支付失败
	 *
	 * @param status 交易状态:1-待支付,2-支付成功,3-支付失败
	 */
	public void setStatus(Integer status) {
		this.status = status;
	}

	/**
	 * 获取微信二维码链接内容
	 *
	 * @return code_url - 微信二维码链接内容
	 */
	public String getCodeUrl() {
		return codeUrl;
	}

	/**
	 * 设置微信二维码链接内容
	 *
	 * @param codeUrl 微信二维码链接内容
	 */
	public void setCodeUrl(String codeUrl) {
		this.codeUrl = codeUrl;
	}

	/**
	 * 获取生成微信支付二维码地址
	 *
	 * @return qrcode - 生成微信支付二维码地址
	 */
	public String getQrcode() {
		return qrcode;
	}

	/**
	 * 设置生成微信支付二维码地址
	 *
	 * @param qrcode 生成微信支付二维码地址
	 */
	public void setQrcode(String qrcode) {
		this.qrcode = qrcode;
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
}