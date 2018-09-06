package com.miqtech.master.entity.bounty;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 悬赏令每日提醒
 * @author Administrator
 *
 */
@Entity
@Table(name = "bounty_daily_tip")
public class BountyDailyTip {
	@Id
	@Column(name = "id", nullable = false, unique = true)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(name = "content")
	private String content;
	@Column(name = "bounty_id")
	private Long bountyId;
	@Column(name = "is_valid")
	private Integer valid;
	@Column(name = "create_date")
	private Date createDate;

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Long getBountyId() {
		return bountyId;
	}

	public void setBountyId(Long bountyId) {
		this.bountyId = bountyId;
	}

	public Integer getValid() {
		return valid;
	}

	public void setValid(Integer valid) {
		this.valid = valid;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

}
