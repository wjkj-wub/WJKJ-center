package com.miqtech.master.entity.guessing;


import com.miqtech.master.entity.IdEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 竞猜对象与竞猜对应关系表
 * @author zhangyuqi
 * 2017年06月05日
 */
@Entity
@Table(name = "guessing_info_item")
public class GuessingInfoItem extends IdEntity {
    private static final long serialVersionUID = 1L;

    private Long guessingInfoId;// 竞猜项ID
    private Long guessingItemId;// 竞猜对象ID
    private Integer score;// 当前竞猜对象的得分
    private Integer isWinner;// 当前竞猜对象的比赛结果
    private Integer position;// 当前竞猜对象的位置：0左边，1右边
	private String supportRate;// 当前竞猜对象的支持率

    @Column(name = "guessing_info_id")
    public Long getGuessingInfoId() {
        return guessingInfoId;
    }

    public void setGuessingInfoId(Long guessingInfoId) {
        this.guessingInfoId = guessingInfoId;
    }

    @Column(name = "guessing_item_id")
    public Long getGuessingItemId() {
        return guessingItemId;
    }

    public void setGuessingItemId(Long guessingItemId) {
        this.guessingItemId = guessingItemId;
    }

    @Column(name = "score")
    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    @Column(name = "is_winner")
    public Integer getIsWinner() {
        return isWinner;
    }

    public void setIsWinner(Integer isWinner) {
        this.isWinner = isWinner;
    }

    @Column(name = "position")
    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

	@Column(name = "supportRate")
	public String getSupportRate() {
		return supportRate;
	}

	public void setSupportRate(String supportRate) {
		this.supportRate = supportRate;
	}
}
