package com.miqtech.master.entity.mall;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

/**
 * （金币）收入支出历史记录
 */
@Entity
@Table(name = "mall_r_coin_history")
public class CoinHistory extends IdEntity {
	private static final long serialVersionUID = 439997321462464523L;

	private Long userId; //用户ID
	private Integer type; //收支类型： 1 - 积分任务，target_id指向任务表；2 - 邀请得积分，target_id指向邀请记录表；3 - 商品兑换，target_id指向商品表 4 - cdkey兑换，target_id指向cdkey表；5 - 自有商品类型的奖品发放，target_id指向award_t_record.id6众筹夺宝
	private Long targetId; //目标ID
	private Integer coin; //收支金币数额
	private Integer direction; //收支方向：0-支出，1-收入
	private Long delUserId;// 黑名单中删除时,记录操作人ID

	@Column(name = "user_id")
	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	@Column(name = "type")
	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	@Column(name = "target_id")
	public Long getTargetId() {
		return targetId;
	}

	public void setTargetId(Long targetId) {
		this.targetId = targetId;
	}

	@Column(name = "coin")
	public Integer getCoin() {
		return coin;
	}

	public void setCoin(Integer coin) {
		this.coin = coin;
	}

	@Column(name = "direction")
	public Integer getDirection() {
		return direction;
	}

	public void setDirection(Integer direction) {
		this.direction = direction;
	}

	@Column(name = "del_user_id")
	public Long getDelUserId() {
		return delUserId;
	}

	public void setDelUserId(Long delUserId) {
		this.delUserId = delUserId;
	}

}
