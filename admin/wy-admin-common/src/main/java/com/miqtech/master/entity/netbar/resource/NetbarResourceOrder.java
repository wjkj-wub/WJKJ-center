package com.miqtech.master.entity.netbar.resource;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;
import com.miqtech.master.utils.DateUtils;

@Entity
@Table(name = "netbar_resource_order")
public class NetbarResourceOrder extends IdEntity {
	private static final long serialVersionUID = 6848144236819767499L;
	private Long commodityId;
	private Long propertyId;
	private Long netbarId;
	private String tradeNo;
	private Float totalAmount;//总金额
	private Float quotaAmount;//配额奖金支付
	private Float payAmount;//充值支付
	private Float amount;//可提取支付
	private Integer buyNum;
	private String remarks;// 备注
	private String comments;// 评价
	private Date serveDate;
	private Integer status;// 订单状态,具体值见NetbarConstant
	private Date expireDate;

	@Column(name = "commodity_id")
	public Long getCommodityId() {
		return commodityId;
	}

	public void setCommodityId(Long commodityId) {
		this.commodityId = commodityId;
	}

	@Column(name = "property_id")
	public Long getPropertyId() {
		return propertyId;
	}

	public void setPropertyId(Long propertyId) {
		this.propertyId = propertyId;
	}

	@Column(name = "netbar_id")
	public Long getNetbarId() {
		return netbarId;
	}

	public void setNetbarId(Long netbarId) {
		this.netbarId = netbarId;
	}

	@Column(name = "trade_no")
	public String getTradeNo() {
		return tradeNo;
	}

	public void setTradeNo(String tradeNo) {
		this.tradeNo = tradeNo;
	}

	public String generateTradeNo() {
		String tradeNo = DateUtils.dateToString(new Date(), "yyyyMMddHHmmssSSS") + "_" + netbarId + "_" + propertyId;
		setTradeNo(tradeNo);
		return tradeNo;
	}

	@Column(name = "total_amount")
	public Float getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(Float totalAmount) {
		this.totalAmount = totalAmount;
	}

	@Column(name = "quota_amount")
	public Float getQuotaAmount() {
		return quotaAmount;
	}

	public void setQuotaAmount(Float quotaAmount) {
		this.quotaAmount = quotaAmount;
	}

	@Column(name = "pay_amount")
	public Float getPayAmount() {
		return payAmount;
	}

	public void setPayAmount(Float payAmount) {
		this.payAmount = payAmount;
	}

	@Column(name = "amount")
	public Float getAmount() {
		return amount;
	}

	public void setAmount(Float amount) {
		this.amount = amount;
	}

	@Column(name = "buy_num")
	public Integer getBuyNum() {
		return buyNum;
	}

	public void setBuyNum(Integer buyNum) {
		this.buyNum = buyNum;
	}

	@Column(name = "remarks")
	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	@Column(name = "comments")
	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	@Column(name = "serve_date")
	public Date getServeDate() {
		return serveDate;
	}

	public void setServeDate(Date serveDate) {
		this.serveDate = serveDate;
	}

	@Column(name = "status")
	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	@Column(name = "expire_date")
	public Date getExpireDate() {
		return expireDate;
	}

	public void setExpireDate(Date expireDate) {
		this.expireDate = expireDate;
	}

}
