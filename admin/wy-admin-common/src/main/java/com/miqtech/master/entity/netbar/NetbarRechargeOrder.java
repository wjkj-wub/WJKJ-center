package com.miqtech.master.entity.netbar;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "netbar_recharge_order")
public class NetbarRechargeOrder extends IdEntity {

	private static final long serialVersionUID = -8017135040102902511L;

	public NetbarRechargeOrder() {
		super();
	}

	public NetbarRechargeOrder(Long netbarId, Integer payType, String outTradeNo, String tradeNo, Double amount,
			String prepayId, String nonceStr, Integer status, String codeUrl, String qrcode) {
		setNetbarId(netbarId);
		setPayType(payType);
		setOutTradeNo(outTradeNo);
		setTradeNo(tradeNo);
		setAmount(amount);
		setPrepayId(prepayId);
		setNonceStr(nonceStr);
		setStatus(status);
		setCodeUrl(codeUrl);
		setQrcode(qrcode);

		setValid(CommonConstant.INT_BOOLEAN_TRUE);
		Date now = new Date();
		setUpdateDate(now);
		setCreateDate(now);
	}

	private Long netbarId;
	private Integer payType;// 支付方式:1-支付宝,2-微信
	private String outTradeNo;// 商户订单号
	private String tradeNo;// 支付平台订单号
	private Double amount;// 充值金额
	private String prepayId;// 预支付ID
	private String nonceStr;// 支付时的随机字符串,做验签用
	private Date notifyDate;// 通知日期
	private Integer status;// 交易状态:1-待支付,2-支付成功,3-支付失败
	private String codeUrl;// 微信二维码链接内容
	private String qrcode;// 生成微信支付二维码地址

	@Column(name = "pay_type")
	public Integer getPayType() {
		return payType;
	}

	public void setPayType(Integer payType) {
		this.payType = payType;
	}

	@Column(name = "out_trade_no")
	public String getOutTradeNo() {
		return outTradeNo;
	}

	public void setOutTradeNo(String outTradeNo) {
		this.outTradeNo = outTradeNo;
	}

	@Column(name = "trade_no")
	public String getTradeNo() {
		return tradeNo;
	}

	public void setTradeNo(String tradeNo) {
		this.tradeNo = tradeNo;
	}

	@Column(name = "amount")
	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	@Column(name = "prepay_id")
	public String getPrepayId() {
		return prepayId;
	}

	public void setPrepayId(String prepayId) {
		this.prepayId = prepayId;
	}

	@Column(name = "nonce_str")
	public String getNonceStr() {
		return nonceStr;
	}

	public void setNonceStr(String nonceStr) {
		this.nonceStr = nonceStr;
	}

	@Column(name = "notify_date")
	public Date getNotifyDate() {
		return notifyDate;
	}

	public void setNotifyDate(Date notifyDate) {
		this.notifyDate = notifyDate;
	}

	@Column(name = "status")
	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	@Column(name = "code_url")
	public String getCodeUrl() {
		return codeUrl;
	}

	public void setCodeUrl(String codeUrl) {
		this.codeUrl = codeUrl;
	}

	@Column(name = "qrcode")
	public String getQrcode() {
		return qrcode;
	}

	public void setQrcode(String qrcode) {
		this.qrcode = qrcode;
	}

	@Column(name = "netbar_id")
	public Long getNetbarId() {
		return netbarId;
	}

	public void setNetbarId(Long netbarId) {
		this.netbarId = netbarId;
	}

}
