package com.miqtech.master.entity.mall;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

/**
 * （金币）商品历史记录
 */
@Entity
@Table(name = "mall_r_commodity_history")
public class CommodityHistory extends IdEntity {
	private static final long serialVersionUID = 7408425525343353278L;

	private Long commodityId; //商品ID
	private Long userId; //用户ID
	private Integer status; //-1未填写地址0-处理中(待审核)，1-已处理（发放成功），2-异常（异常待审核），3-异常已处理；4-审核通过（待发放），5-异常审核通过（待发放），6-审核拒绝，7-异常审核拒绝，8-发放失败，9-（异常）发放失败；
	private Integer coin; //交易价格
	private Integer num; //一次交易的商品数量
	private String tranNo; //交易流水号
	private String information; //交易信息
	private Long thirdpartyId; //关联的其他表ID，例如用户红包id
	private Integer isGet; //是否已经取得（兑换商品后都为1-是；抽奖商品中奖为1-是，未中奖为0-否）
	private String account;//用户的充值帐号
	private String code;//兑换码
	private String pushMsg;//推送消息verify_date
	private Date verifyDate;// 审核时间
	private String remark;//备注
	private Integer commoditySource;//1非转盘商品2转盘商品
	private String virtualPhone;//虚拟手机号

	@Column(name = "commodity_source")
	public Integer getCommoditySource() {
		return commoditySource;
	}

	public void setCommoditySource(Integer commoditySource) {
		this.commoditySource = commoditySource;
	}

	@Column(name = "virtual_phone")
	public String getVirtualPhone() {
		return virtualPhone;
	}

	public void setVirtualPhone(String virtualPhone) {
		this.virtualPhone = virtualPhone;
	}

	@Column(name = "verify_date")
	public Date getVerifyDate() {
		return verifyDate;
	}

	public void setVerifyDate(Date verifyDate) {
		this.verifyDate = verifyDate;
	}

	@Column(name = "remark")
	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	@Column(name = "account")
	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	@Column(name = "commodity_id")
	public Long getCommodityId() {
		return commodityId;
	}

	public void setCommodityId(Long commodityId) {
		this.commodityId = commodityId;
	}

	@Column(name = "user_id")
	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	@Column(name = "status")
	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	@Column(name = "coin")
	public Integer getCoin() {
		return coin;
	}

	public void setCoin(Integer coin) {
		this.coin = coin;
	}

	@Column(name = "num")
	public Integer getNum() {
		return num;
	}

	public void setNum(Integer num) {
		this.num = num;
	}

	@Column(name = "tran_no")
	public String getTranNo() {
		return tranNo;
	}

	public void setTranNo(String tranNo) {
		this.tranNo = tranNo;
	}

	@Column(name = "infomation")
	public String getInformation() {
		return information;
	}

	public void setInformation(String information) {
		this.information = information;
	}

	@Column(name = "thirdparty_id")
	public Long getThirdpartyId() {
		return thirdpartyId;
	}

	public void setThirdpartyId(Long thirdpartyId) {
		this.thirdpartyId = thirdpartyId;
	}

	@Column(name = "is_get")
	public Integer getIsGet() {
		return isGet;
	}

	public void setIsGet(Integer isGet) {
		this.isGet = isGet;
	}

	@Column(name = "code")
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	@Column(name = "push_msg")
	public String getPushMsg() {
		return pushMsg;
	}

	public void setPushMsg(String pushMsg) {
		this.pushMsg = pushMsg;
	}

}
