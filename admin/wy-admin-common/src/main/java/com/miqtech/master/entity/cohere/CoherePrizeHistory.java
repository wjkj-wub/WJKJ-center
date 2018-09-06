package com.miqtech.master.entity.cohere;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "cohere_prize_history")
public class CoherePrizeHistory extends IdEntity {

	private static final long serialVersionUID = -5967340650944899912L;
	@Column(name = "user_id")
	private Long userId;
	@Column(name = "prize_id")
	private Long prizeId;//奖品ID
	@Column(name = "account")
	private String account;// 充值账号
	@Column(name = "game_name")
	private String gameName;//  游戏ID
	@Column(name = "server_name")
	private String serverName;//所在区服
	@Column(name = "is_get")
	private Integer isGet;//  是否中奖 0 未中奖  1 中奖
	@Column(name = "state")
	private Integer state;//  发放状态  0 未申请兑奖  1 已申请发放 2 发放失败 3发放成功
	@Column(name = "question")
	private Integer question;// 兑奖是否有疑问 0 无疑问 1 有疑问
	@Column(name = "tran_no")
	private String tranNo;// 交易号

	public Long getPrizeId() {
		return prizeId;
	}

	public void setPrizeId(Long prizeId) {
		this.prizeId = prizeId;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public String getGameName() {
		return gameName;
	}

	public void setGameName(String gameName) {
		this.gameName = gameName;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public Integer getIsGet() {
		return isGet;
	}

	public void setIsGet(Integer isGet) {
		this.isGet = isGet;
	}

	public Integer getState() {
		return state;
	}

	public void setState(Integer state) {
		this.state = state;
	}

	public Integer getQuestion() {
		return question;
	}

	public void setQuestion(Integer question) {
		this.question = question;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getTranNo() {
		return tranNo;
	}

	public void setTranNo(String tranNo) {
		this.tranNo = tranNo;
	}

}
