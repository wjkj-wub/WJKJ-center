package com.miqtech.master.entity.netbar;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "netbar_t_merchant")
public class NetbarMerchant extends IdEntity {
	private static final long serialVersionUID = 6893190560453116658L;

	private Long netbarId;// 网吧ID

	private String username;// 用户名

	private String password;// 密码

	private String address;// 商户联系地址

	private String ownerName;// 业主姓名

	private String ownerIdcard;// 业主身份证号

	private String ownerTelephone;// 业主联系方式

	private String adminName;// 管理员姓名

	private String adminTelephone;// 管理人联系方式

	private String businessLicense;// 营业执照

	private String businessLicenseMedia;

	private String businessLicenseThumb;

	private String businessIdcard;// 执照人身份证号码

	private String businessIdcardMedia;

	private String businessIdcardThumb;

	private String weixinPayAccount;

	private String aliPayAccount;
	private String aliPayAccountName;

	private Long netbarTmpId;

	private String bankCardId;
	private String bankUsername;
	private String bankName;
	private String bankBranchName;
	private Integer source;//0网吧注册1员工录入

	@Column(name = "bank_branch_name")
	public String getBankBranchName() {
		return bankBranchName;
	}

	public void setBankBranchName(String bankBranchName) {
		this.bankBranchName = bankBranchName;
	}

	@Column(name = "netbar_id")
	public Long getNetbarId() {
		return netbarId;
	}

	public void setNetbarId(Long netbarId) {
		this.netbarId = netbarId;
	}

	@Column(name = "username")
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@Column(name = "password")
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Column(name = "address")
	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	@Column(name = "owner_name")
	public String getOwnerName() {
		return ownerName;
	}

	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
	}

	@Column(name = "owner_idcard")
	public String getOwnerIdcard() {
		return ownerIdcard;
	}

	public void setOwnerIdcard(String ownerIdcard) {
		this.ownerIdcard = ownerIdcard;
	}

	@Column(name = "owner_telephone")
	public String getOwnerTelephone() {
		return ownerTelephone;
	}

	public void setOwnerTelephone(String ownerTelephone) {
		this.ownerTelephone = ownerTelephone;
	}

	@Column(name = "admin_name")
	public String getAdminName() {
		return adminName;
	}

	public void setAdminName(String adminName) {
		this.adminName = adminName;
	}

	@Column(name = "admin_telephone")
	public String getAdminTelephone() {
		return adminTelephone;
	}

	public void setAdminTelephone(String adminTelephone) {
		this.adminTelephone = adminTelephone;
	}

	@Column(name = "business_license")
	public String getBusinessLicense() {
		return businessLicense;
	}

	public void setBusinessLicense(String bussinessLicense) {
		this.businessLicense = bussinessLicense;
	}

	@Column(name = "business_license_media")
	public String getBusinessLicenseMedia() {
		return businessLicenseMedia;
	}

	public void setBusinessLicenseMedia(String businessLicenseMedia) {
		this.businessLicenseMedia = businessLicenseMedia;
	}

	@Column(name = "business_license_thumb")
	public String getBusinessLicenseThumb() {
		return businessLicenseThumb;
	}

	public void setBusinessLicenseThumb(String businessLicenseThumb) {
		this.businessLicenseThumb = businessLicenseThumb;
	}

	@Column(name = "business_idcard")
	public String getBusinessIdcard() {
		return businessIdcard;
	}

	public void setBusinessIdcard(String businessIdcard) {
		this.businessIdcard = businessIdcard;
	}

	@Column(name = "business_idcard_media")
	public String getBusinessIdcardMedia() {
		return businessIdcardMedia;
	}

	public void setBusinessIdcardMedia(String businessIdcardMedia) {
		this.businessIdcardMedia = businessIdcardMedia;
	}

	@Column(name = "business_idcard_thumb")
	public String getBusinessIdcardThumb() {
		return businessIdcardThumb;
	}

	public void setBusinessIdcardThumb(String businessIdcardThumb) {
		this.businessIdcardThumb = businessIdcardThumb;
	}

	@Column(name = "weixin_pay_account")
	public String getWeixinPayAccount() {
		return weixinPayAccount;
	}

	public void setWeixinPayAccount(String weixinPayAccount) {
		this.weixinPayAccount = weixinPayAccount;
	}

	@Column(name = "ali_pay_account")
	public String getAliPayAccount() {
		return aliPayAccount;
	}

	public void setAliPayAccount(String aliPayAccount) {
		this.aliPayAccount = aliPayAccount;
	}

	@Column(name = "netbar_tmp_id")
	public Long getNetbarTmpId() {
		return netbarTmpId;
	}

	public void setNetbarTmpId(Long netbarTmpId) {
		this.netbarTmpId = netbarTmpId;
	}

	@Column(name = "bank_card_id")
	public String getBankCardId() {
		return bankCardId;
	}

	public void setBankCardId(String bankCardId) {
		this.bankCardId = bankCardId;
	}

	@Column(name = "bank_username")
	public String getBankUsername() {
		return bankUsername;
	}

	public void setBankUsername(String bankUsername) {
		this.bankUsername = bankUsername;
	}

	@Column(name = "bank_name")
	public String getBankName() {
		return bankName;
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
	}

	@Column(name = "ali_pay_account_name")
	public String getAliPayAccountName() {
		return aliPayAccountName;
	}

	public void setAliPayAccountName(String aliPayAccountName) {
		this.aliPayAccountName = aliPayAccountName;
	}

	@Column(name = "source")
	public Integer getSource() {
		return source;
	}

	public void setSource(Integer source) {
		this.source = source;
	}

}
