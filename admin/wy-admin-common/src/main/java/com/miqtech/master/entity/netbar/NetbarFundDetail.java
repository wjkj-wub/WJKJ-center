package com.miqtech.master.entity.netbar;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.consts.NetbarConstant;
import com.miqtech.master.entity.IdEntity;

/**
 * 网吧资金收支明细
 */
@Entity
@Table(name = "netbar_fund_detail")
public class NetbarFundDetail extends IdEntity {

	private static final long serialVersionUID = -2441101499145625955L;

	public NetbarFundDetail() {
		super();
	}

	public NetbarFundDetail(Long netbarId, Integer type, Integer direction, Double amount, String serNumbers) {
		setNetbarId(netbarId);
		setType(type);
		setDirection(direction);
		setAmount(amount);
		setSerNumbers(serNumbers);
		setStatus(NetbarConstant.NETBAR_FUND_DETAIL_STATUS_APPLY);
		setValid(CommonConstant.INT_BOOLEAN_TRUE);
		Date now = new Date();
		setUpdateDate(now);
		setCreateDate(now);
	}

	private Long netbarId;
	private Integer type;// 收支类型：0 消费 1 红包 2 提现 3 充值 4 结算 5 配额奖金
	private Integer direction;// -1 支出 1 收入
	private Double amount;// 产生金额
	private Double residual;// 交易后剩余可用余额
	private Double settlAccounts;// 当期核销金额
	private Double quotaRatio;// 配额奖金比例%
	private String serNumbers;// 收支编号,支出为订单号, 收入充值为充值id、配额奖金为当月配额奖金id、结算为空 提现状态 表示建行交易记录请求号
	private String creditNo;// 提现专用,记录建行的交易编号

	@Column(name = "credit_no")
	public String getCreditNo() {
		return creditNo;
	}

	public void setCreditNo(String creditNo) {
		this.creditNo = creditNo;
	}

	private Integer status;// 资金状态

	@Column(name = "netbar_id")
	public Long getNetbarId() {
		return netbarId;
	}

	public void setNetbarId(Long netbarId) {
		this.netbarId = netbarId;
	}

	@Column(name = "type")
	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	@Column(name = "direction")
	public Integer getDirection() {
		return direction;
	}

	public void setDirection(Integer direction) {
		this.direction = direction;
	}

	@Column(name = "amount")
	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	@Column(name = "residual")
	public Double getResidual() {
		return residual;
	}

	public void setResidual(Double residual) {
		this.residual = residual;
	}

	@Column(name = "settl_accounts")
	public Double getSettlAccounts() {
		return settlAccounts;
	}

	public void setSettlAccounts(Double settlAccounts) {
		this.settlAccounts = settlAccounts;
	}

	@Column(name = "quota_ratio")
	public Double getQuotaRatio() {
		return quotaRatio;
	}

	public void setQuotaRatio(Double quotaRatio) {
		this.quotaRatio = quotaRatio;
	}

	@Column(name = "ser_numbers")
	public String getSerNumbers() {
		return serNumbers;
	}

	public void setSerNumbers(String serNumbers) {
		this.serNumbers = serNumbers;
	}

	@Column(name = "status")
	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

}
