package com.miqtech.master.entity.amuse;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "amuse_t_activity")
public class AmuseActivityInfo extends IdEntity {
	private static final long serialVersionUID = -8168767486418975015L;

	private String title; //活动标题
	private String subTitle;// 副标题
	private Integer type; //娱乐赛类型，1-官网线上；2-官方线下；3-官网网吧
	private Integer way; //方式：1-线下；2-线上
	private Long netbarId; //网吧ID
	private String remark; //审核网吧娱乐赛的备注信息
	private String server; //服务器（描述）
	private String reward; //奖励说明
	private String rule; //活动规则
	private Integer maxNum; //活动报名人数上限
	private String contact; //联系方式
	private Integer contactType; //联系方式类型
	private Integer state; //状态，0-待审核，1-审核被拒，2-发布，3-未发布
	private Integer isRelease; //是否发布：0-否；1-是;--改用state了
	private Integer isRecommend; //是否推荐
	private Date startDate; //活动开始时间
	private Date endDate; //活动结束时间
	private String summary;//摘要
	private String verifyContent;// 审核内容说明
	private Date verifyEndDate;// 截止认证时间
	private Integer virtualApply;// 虚拟报名人数
	private Integer takeType;// 参与方式，1-个人；2-团队
	private Long itemId;// 游戏项目id
	private Long awardTypeId;// 奖品项目id-->不用id，用type
	private Integer awardType;// 奖品类别:1-自有商品,2-充值,3-库存
	private Integer awardSubType;// 奖品小类别:0-库存(指向amuse_r_award_type.id),1-自有红包,2-自有金币,3-充值话费,4-充值流量,5-充值Q币
	private Integer awardAmount;// 奖品数量
	private Date applyStart;// 报名开始
	private Date releaseDate; //发布时间
	private Integer telReq;// 手机号是否必填，0-否（默认值）；1-是
	private Integer NameReq;// （真实）姓名是否必填，0-否（默认值）；1-是
	private Integer AccountReq;// 游戏帐号（昵称）是否必填，0-否（默认值）；1-是
	private Integer serverReq;// 游戏服务区（器）是否必填，0-否（默认值）；1-是
	private Integer qqReq;// QQ号是否必填，0-否（默认值）；1-是
	private Integer idCardReq;// 身份证号是否必填，0-否（默认值）；1-是
	private Integer teamNameReq;// 团队名是否必填，0-否（默认值）；1-是
	private Integer recommendSign;//0首页竞技大厅都已推荐1首页已推荐2竞技大厅已推荐
	private Integer deliverDay;//发放天数
	private String grantMsg;// 发放成功时推送的系统消息

	@Column(name = "sub_title")
	public String getSubTitle() {
		return subTitle;
	}

	public void setSubTitle(String subTitle) {
		this.subTitle = subTitle;
	}

	@Column(name = "virtual_apply")
	public Integer getVirtualApply() {
		return virtualApply;
	}

	public void setVirtualApply(Integer virtualApply) {
		this.virtualApply = virtualApply;
	}

	@Column(name = "take_type")
	public Integer getTakeType() {
		return takeType;
	}

	public void setTakeType(Integer takeType) {
		this.takeType = takeType;
	}

	@Column(name = "item_id")
	public Long getItemId() {
		return itemId;
	}

	public void setItemId(Long itemId) {
		this.itemId = itemId;
	}

	@Column(name = "award_type_id")
	public Long getAwardTypeId() {
		return awardTypeId;
	}

	public void setAwardTypeId(Long awardTypeId) {
		this.awardTypeId = awardTypeId;
	}

	@Column(name = "award_type")
	public Integer getAwardType() {
		return awardType;
	}

	public void setAwardType(Integer awardType) {
		this.awardType = awardType;
	}

	@Column(name = "award_sub_type")
	public Integer getAwardSubType() {
		return awardSubType;
	}

	public void setAwardSubType(Integer awardSubType) {
		this.awardSubType = awardSubType;
	}

	@Column(name = "award_amount")
	public Integer getAwardAmount() {
		return awardAmount;
	}

	public void setAwardAmount(Integer awardAmount) {
		this.awardAmount = awardAmount;
	}

	@Column(name = "apply_start")
	public Date getApplyStart() {
		return applyStart;
	}

	public void setApplyStart(Date applyStart) {
		this.applyStart = applyStart;
	}

	@Column(name = "tel_req")
	public Integer getTelReq() {
		return telReq;
	}

	public void setTelReq(Integer telReq) {
		this.telReq = telReq;
	}

	@Column(name = "name_req")
	public Integer getNameReq() {
		return NameReq;
	}

	public void setNameReq(Integer nameReq) {
		NameReq = nameReq;
	}

	@Column(name = "account_req")
	public Integer getAccountReq() {
		return AccountReq;
	}

	public void setAccountReq(Integer accountReq) {
		AccountReq = accountReq;
	}

	@Column(name = "server_req")
	public Integer getServerReq() {
		return serverReq;
	}

	public void setServerReq(Integer serverReq) {
		this.serverReq = serverReq;
	}

	@Column(name = "qq_req")
	public Integer getQqReq() {
		return qqReq;
	}

	public void setQqReq(Integer qqReq) {
		this.qqReq = qqReq;
	}

	@Column(name = "id_card_req")
	public Integer getIdCardReq() {
		return idCardReq;
	}

	public void setIdCardReq(Integer idCardReq) {
		this.idCardReq = idCardReq;
	}

	@Column(name = "team_name_req")
	public Integer getTeamNameReq() {
		return teamNameReq;
	}

	public void setTeamNameReq(Integer teamNameReq) {
		this.teamNameReq = teamNameReq;
	}

	@Column(name = "title")
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Column(name = "type")
	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	@Column(name = "way")
	public Integer getWay() {
		return way;
	}

	public void setWay(Integer way) {
		this.way = way;
	}

	@Column(name = "netbar_id")
	public Long getNetbarId() {
		return netbarId;
	}

	public void setNetbarId(Long netbarId) {
		this.netbarId = netbarId;
	}

	@Column(name = "remark")
	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	@Column(name = "server")
	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	@Column(name = "reward")
	public String getReward() {
		return reward;
	}

	public void setReward(String reward) {
		this.reward = reward;
	}

	@Column(name = "rule")
	public String getRule() {
		return rule;
	}

	public void setRule(String rule) {
		this.rule = rule;
	}

	@Column(name = "max_num")
	public Integer getMaxNum() {
		return maxNum;
	}

	public void setMaxNum(Integer maxNum) {
		this.maxNum = maxNum;
	}

	@Column(name = "contact")
	public String getContact() {
		return contact;
	}

	public void setContact(String contact) {
		this.contact = contact;
	}

	@Column(name = "contact_type")
	public Integer getContactType() {
		return contactType;
	}

	public void setContactType(Integer contactType) {
		this.contactType = contactType;
	}

	@Column(name = "state")
	public Integer getState() {
		return state;
	}

	public void setState(Integer state) {
		this.state = state;
	}

	@Column(name = "is_release")
	public Integer getIsRelease() {
		return isRelease;
	}

	public void setIsRelease(Integer isRelease) {
		this.isRelease = isRelease;
	}

	@Column(name = "is_recommend")
	public Integer getIsRecommend() {
		return isRecommend;
	}

	public void setIsRecommend(Integer isRecommend) {
		this.isRecommend = isRecommend;
	}

	@Column(name = "start_date")
	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	@Column(name = "end_date")
	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	@Column(name = "release_date")
	public Date getReleaseDate() {
		return releaseDate;
	}

	public void setReleaseDate(Date releaseDate) {
		this.releaseDate = releaseDate;
	}

	@Column(name = "summary")
	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	@Column(name = "verify_content")
	public String getVerifyContent() {
		return verifyContent;
	}

	public void setVerifyContent(String verifyContent) {
		this.verifyContent = verifyContent;
	}

	@Column(name = "verify_end_date")
	public Date getVerifyEndDate() {
		return verifyEndDate;
	}

	public void setVerifyEndDate(Date verifyEndDate) {
		this.verifyEndDate = verifyEndDate;
	}

	@Column(name = "recommend_sign")
	public Integer getRecommendSign() {
		return recommendSign;
	}

	public void setRecommendSign(Integer recommendSign) {
		this.recommendSign = recommendSign;
	}

	@Column(name = "deliver_day")
	public Integer getDeliverDay() {
		return deliverDay;
	}

	public void setDeliverDay(Integer deliverDay) {
		this.deliverDay = deliverDay;
	}

	@Column(name = "grant_msg")
	public String getGrantMsg() {
		return grantMsg;
	}

	public void setGrantMsg(String grantMsg) {
		this.grantMsg = grantMsg;
	}
}
