package com.miqtech.master.entity.netbar;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "netbar_r_order")
public class NetbarOrder extends IdEntity {
	private static final long serialVersionUID = -5011708022379560039L;
	@Column(name = "reserve_id")
	private Long reserveId;// 预约记录ID
	@Column(name = "out_trade_no")
	private String outTradeNo;// 商户订单号
	@Column(name = "trade_no")
	private String tradeNo;// 官方订单号
	@Column(name = "type")
	private Integer type;// 支付类型:1-支付宝,2-财付通
	@Column(name = "amount")
	private Double amount;// 付款金额（元）(网民->网娱)
	@Column(name = "status")
	private Integer status;// 订单状态 0网民->网娱支付未完成, 1网民->网娱完成支付, 网娱->网吧未付款  2网娱->网吧申请付款, 3网娱->网吧有异议, 4网娱->网吧已结款
	@Column(name = "netbar_type")
	private Integer netbarType;// 支付类型:1-支付宝,2-财付通
	@Column(name = "netbar_amount")
	private Double netbarAmount;// 付款金额（元）(网娱->网吧)
	@Column(name = "netbar_id")
	private Long netbarId;
	@Column(name = "user_nickname")
	private String userNickname;
	@Column(name = "user_id")
	private Long userId;
	@Column(name = "redbag_amount")
	private Double redbagAmount;
	@Column(name = "rebate_amount")
	private Double rebateAmount;
	@Column(name = "score_amount")
	private Double scoreAmount;
	@Column(name = "total_amount")
	private Double totalAmount;
	private String rids;
	@Column(name = "prepay_id")
	private String prepayId;
	@Column(name = "nonce_str")
	private String nonceStr;
	@Column(name = "order_type")
	private int orderType;
	@Column(name = "operate_staff_id")
	private Long operateStaffId;
	@Column(name = "user_use_status")
	private Integer userUseStatus;
	@Column(name = "already_lottery")
	private Integer alreadyLottery;//是否已抽奖1是0否
	@Column(name = "value_added_amount")
	private Integer valueAddedAmount;//使用增值券金额
	@Column(name = "value_added_id")
	private Long valueAddedId;//使用的增值券id
	@Column(name = "merchant_comment")
	private String merchantComment;
	@Column(name = "merchant_comment_status")
	private Integer merchantCommentStatus;//收银确认状态
	@Column(name = "pc_seat_num")
	private String pcSeatNum;// pc端充值座位号
	@Column(name = "pc_seat_card_num")
	private String pcSeatCardNum;// pc端充值用户卡号
	@Column(name = "code_url")
	private String codeUrl;// 二维码链接内容
	@Column(name = "qrcode")
	private String qrcode;// 支付二维码地址

	public Integer getValueAddedAmount() {
		return valueAddedAmount;
	}

	public void setValueAddedAmount(Integer valueAddedAmount) {
		this.valueAddedAmount = valueAddedAmount;
	}

	public Long getValueAddedId() {
		return valueAddedId;
	}

	public void setValueAddedId(Long valueAddedId) {
		this.valueAddedId = valueAddedId;
	}

	public Long getReserveId() {
		return reserveId;
	}

	public void setReserveId(Long reserveId) {
		this.reserveId = reserveId;
	}

	public String getOutTradeNo() {
		return outTradeNo;
	}

	public void setOutTradeNo(String outTradeNo) {
		this.outTradeNo = outTradeNo;
	}

	public String getTradeNo() {
		return tradeNo;
	}

	public void setTradeNo(String tradeNo) {
		this.tradeNo = tradeNo;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Integer getNetbarType() {
		return netbarType;
	}

	public void setNetbarType(Integer netbarType) {
		this.netbarType = netbarType;
	}

	public Double getNetbarAmount() {
		return netbarAmount;
	}

	public void setNetbarAmount(Double netbarAmount) {
		this.netbarAmount = netbarAmount;
	}

	public Long getNetbarId() {
		return netbarId;
	}

	public void setNetbarId(Long netbarId) {
		this.netbarId = netbarId;
	}

	public String getUserNickname() {
		return userNickname;
	}

	public void setUserNickname(String userNickname) {
		this.userNickname = userNickname;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Double getRedbagAmount() {
		return redbagAmount;
	}

	public void setRedbagAmount(Double redbagAmount) {
		this.redbagAmount = redbagAmount;
	}

	public Double getScoreAmount() {
		return scoreAmount;
	}

	public void setScoreAmount(Double scoreAmount) {
		this.scoreAmount = scoreAmount;
	}

	public Double getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(Double totalAmount) {
		this.totalAmount = totalAmount;
	}

	public String getPrepayId() {
		return prepayId;
	}

	public void setPrepayId(String prepayId) {
		this.prepayId = prepayId;
	}

	public Double getRebateAmount() {
		return rebateAmount;
	}

	public void setRebateAmount(Double rebateAmount) {
		this.rebateAmount = rebateAmount;
	}

	@Column(name = "redbag_id")
	public String getRids() {
		return rids;
	}

	public void setRids(String rids) {
		this.rids = rids;
	}

	public String getNonceStr() {
		return nonceStr;
	}

	public void setNonceStr(String nonceStr) {
		this.nonceStr = nonceStr;
	}

	/**
	 * 是否完成支付
	 */
	public boolean trueCompletePay() {
		return this.status != null && this.status >= 1;
	}

	public int getOrderType() {
		return orderType;
	}

	public void setOrderType(int orderType) {
		this.orderType = orderType;
	}

	public Long getOperateStaffId() {
		return operateStaffId;
	}

	public void setOperateStaffId(Long operateStaffId) {
		this.operateStaffId = operateStaffId;
	}

	public Integer getUserUseStatus() {
		return userUseStatus;
	}

	public void setUserUseStatus(Integer userUseStatus) {
		this.userUseStatus = userUseStatus;
	}

	public Integer getAlreadyLottery() {
		return alreadyLottery;
	}

	public void setAlreadyLottery(Integer alreadyLottery) {
		this.alreadyLottery = alreadyLottery;
	}

	public String getMerchantComment() {
		return merchantComment;
	}

	public void setMerchantComment(String merchantComment) {
		this.merchantComment = merchantComment;
	}

	public String getPcSeatNum() {
		return pcSeatNum;
	}

	public void setPcSeatNum(String pcSeatNum) {
		this.pcSeatNum = pcSeatNum;
	}

	public String getPcSeatCardNum() {
		return pcSeatCardNum;
	}

	public void setPcSeatCardNum(String pcSeatCardNum) {
		this.pcSeatCardNum = pcSeatCardNum;
	}

	public String getCodeUrl() {
		return codeUrl;
	}

	public void setCodeUrl(String codeUrl) {
		this.codeUrl = codeUrl;
	}

	public String getQrcode() {
		return qrcode;
	}

	public void setQrcode(String qrcode) {
		this.qrcode = qrcode;
	}

	public Integer getMerchantCommentStatus() {
		return merchantCommentStatus;
	}

	public void setMerchantCommentStatus(Integer merchantCommentStatus) {
		this.merchantCommentStatus = merchantCommentStatus;
	}

}