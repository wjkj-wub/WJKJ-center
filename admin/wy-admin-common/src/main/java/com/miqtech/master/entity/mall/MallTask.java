package com.miqtech.master.entity.mall;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.miqtech.master.entity.IdEntity;

/**
 * 商城任务
 */
@JsonIgnoreProperties({ "createDate", "createUserId", "updateDate", "updateUserId", "valid" })
@Entity
@Table(name = "mall_t_task")
public class MallTask extends IdEntity {

	private static final long serialVersionUID = 6568727520235471006L;

	private Integer type;// 任务类型：1 - 每日任务；2 - 新手任务；
	private Integer identify;// 任务标识：type为每日任务时：1 - 打开APP；2 - 点击广告；3 - 发起约战；4 - 加入约战；5 - 预定网吧；6 - 完成付款；7 - 关注用户；8 - 收藏手游；9 - 分享； type为新手任务时：1 - 完善个人资料；2 - 完善参赛资料；3 - 首次支付网费；4 - 首次发布约战；5 - 首次兑换商品；
	private String iosIcon;
	private String androidIcon;
	private String name;// 任务名称，比较正统的说法（如：每天第一次打开APP）
	private Integer limit;// 任务限额
	private Integer coin;// 金币数
	private String text;// 任务描述（诙谐的说法，如：用奇怪的姿势打开网娱大师）
	private String remark;// 任务说明
	private String simpleRemark;// 新版任务说明,非富文本
	private Integer sortNum;

	@Column(name = "type")
	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	@Column(name = "identify")
	public Integer getIdentify() {
		return identify;
	}

	public void setIdentify(Integer identify) {
		this.identify = identify;
	}

	@Column(name = "ios_icon")
	public String getIosIcon() {
		return iosIcon;
	}

	public void setIosIcon(String iosIcon) {
		this.iosIcon = iosIcon;
	}

	@Column(name = "android_icon")
	public String getAndroidIcon() {
		return androidIcon;
	}

	public void setAndroidIcon(String androidIcon) {
		this.androidIcon = androidIcon;
	}

	@Column(name = "name")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "`limit`")
	public Integer getLimit() {
		return limit;
	}

	public void setLimit(Integer limit) {
		this.limit = limit;
	}

	@Column(name = "coin")
	public Integer getCoin() {
		return coin;
	}

	public void setCoin(Integer coin) {
		this.coin = coin;
	}

	@Column(name = "text")
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	@Column(name = "remark")
	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	@Column(name = "sort_num")
	public Integer getSortNum() {
		return sortNum;
	}

	public void setSortNum(Integer sortNum) {
		this.sortNum = sortNum;
	}

	@Column(name = "simple_remark")
	public String getSimpleRemark() {
		return simpleRemark;
	}

	public void setSimpleRemark(String simpleRemark) {
		this.simpleRemark = simpleRemark;
	}

}
