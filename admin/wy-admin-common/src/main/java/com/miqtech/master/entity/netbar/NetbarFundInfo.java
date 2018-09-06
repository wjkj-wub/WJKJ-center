package com.miqtech.master.entity.netbar;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.entity.IdEntity;
import com.miqtech.master.utils.ArithUtil;

/**
 * 网吧资金信息
 */
@Entity
@Table(name = "netbar_fund_info")
public class NetbarFundInfo extends IdEntity {

	private static final long serialVersionUID = -2093091011154387500L;

	public NetbarFundInfo() {
		super();
	}

	public NetbarFundInfo(Long netbarId) {
		setNetbarId(netbarId);
		Double initValue = new Double(0);
		setQuota(initValue);
		setUsableQuota(initValue);
		setSettlAccounts(initValue);
		setAccounts(initValue);
		setQuotaRatio(initValue);
		setUsablePay(initValue);
		setValid(CommonConstant.INT_BOOLEAN_TRUE);
		Date now = new Date();
		setUpdateDate(now);
		setCreateDate(now);
	}

	private Long netbarId;
	private Double quota;// 配额奖金：配额=当期结算金额*配额比例
	private Double usableQuota;// 可用配额奖金
	private Double settlAccounts;// 计算周期内结算金额结算金额
	private Double accounts;// 可提现金额：计算金额才可提现
	private Double quotaRatio;// 配额奖金比例
	private Double usablePay;// 可用充值金额

	@Column(name = "netbar_id")
	public Long getNetbarId() {
		return netbarId;
	}

	public void setNetbarId(Long netbarId) {
		this.netbarId = netbarId;
	}

	@Column(name = "quota")
	public Double getQuota() {
		return quota;
	}

	public void setQuota(Double quota) {
		this.quota = quota;
	}

	public void addQuota(Double quota) {
		setQuota(ArithUtil.add(getQuota(), quota));
	}

	@Column(name = "usable_quota")
	public Double getUsableQuota() {
		return usableQuota;
	}

	public void setUsableQuota(Double usableQuota) {
		this.usableQuota = usableQuota;
	}

	public void addUsableQuota(Double usableQuota) {
		setUsableQuota(ArithUtil.add(getUsableQuota(), usableQuota));
	}

	@Column(name = "settl_accounts")
	public Double getSettlAccounts() {
		return settlAccounts;
	}

	public void setSettlAccounts(Double settlAccounts) {
		this.settlAccounts = settlAccounts;
	}

	@Column(name = "accounts")
	public Double getAccounts() {
		return accounts;
	}

	public void setAccounts(Double accounts) {
		this.accounts = accounts;
	}

	public void addAccounts(Double accounts) {
		setAccounts(ArithUtil.add(getAccounts(), accounts));
	}

	@Column(name = "quota_ratio")
	public Double getQuotaRatio() {
		return quotaRatio;
	}

	public void setQuotaRatio(Double quotaRatio) {
		this.quotaRatio = quotaRatio;
	}

	@Column(name = "usable_pay")
	public Double getUsablePay() {
		return usablePay;
	}

	public void setUsablePay(Double usablePay) {
		this.usablePay = usablePay;
	}

}
