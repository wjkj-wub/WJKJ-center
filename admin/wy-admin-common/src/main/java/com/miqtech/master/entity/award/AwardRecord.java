package com.miqtech.master.entity.award;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

/**
 * 奖品发放记录
 */
@Entity
@Table(name = "award_t_record")
public class AwardRecord extends IdEntity {

	private static final long serialVersionUID = -1232248899595133116L;

	private Long userId;// 奖品接收用户
	private String channelCode;// 充值渠道代码(非充值类型为空)
	private Integer type;// 发放类别:1-自有商品,2-充值,3-库存
	private Integer subType;// 发放小类别:0-库存(指向mall_t_commodity_category.id),1-自有红包,2-自有金币,3-充值话费,4-充值流量,5-充值Q币
	private Long targetId;// sub_type=0时指向mall_t_commodity_category.id, sub_type=1时指向user_t_redbag.id, sub_type=2时指向coin_r_history.id, sub_type=3/4/5时为null
	private String userInfo;// 充值时用的用户信息
	private Double amount;// 发放量(单位为类型对应单位)
	private Double money;// 花费的金额
	private Integer status;// 发放状态:0-已提交(未完成);1-接口失败;2-短信失败;3-成功
	private String remark;// 记录充值失败原因
	private Integer checked;// 财务是否复核:0-未复核,1-已复核
	private Integer sourceType;// 发放来自类型:1-娱乐赛,2-金币商城
	private Long sourceTargetId;// 发放来自类型:1-娱乐赛,2-金币商城
	private Long delUserId;// 黑名单删除时，记录操作人ID

	@Column(name = "user_id")
	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	@Column(name = "channel_code")
	public String getChannelCode() {
		return channelCode;
	}

	public void setChannelCode(String channelCode) {
		this.channelCode = channelCode;
	}

	@Column(name = "type")
	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	@Column(name = "sub_type")
	public Integer getSubType() {
		return subType;
	}

	public void setSubType(Integer subType) {
		this.subType = subType;
	}

	@Column(name = "target_id")
	public Long getTargetId() {
		return targetId;
	}

	public void setTargetId(Long targetId) {
		this.targetId = targetId;
	}

	@Column(name = "user_info")
	public String getUserInfo() {
		return userInfo;
	}

	public void setUserInfo(String userInfo) {
		this.userInfo = userInfo;
	}

	@Column(name = "amount")
	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	@Column(name = "money")
	public Double getMoney() {
		return money;
	}

	public void setMoney(Double money) {
		this.money = money;
	}

	@Column(name = "status")
	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	@Column(name = "remark")
	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	@Column(name = "checked")
	public Integer getChecked() {
		return checked;
	}

	public void setChecked(Integer checked) {
		this.checked = checked;
	}

	@Column(name = "source_type")
	public Integer getSourceType() {
		return sourceType;
	}

	public void setSourceType(Integer sourceType) {
		this.sourceType = sourceType;
	}

	@Column(name = "source_target_id")
	public Long getSourceTargetId() {
		return sourceTargetId;
	}

	public void setSourceTargetId(Long sourceTargetId) {
		this.sourceTargetId = sourceTargetId;
	}

	@Column(name = "del_user_id")
	public Long getDelUserId() {
		return delUserId;
	}

	public void setDelUserId(Long delUserId) {
		this.delUserId = delUserId;
	}

}
