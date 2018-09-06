package com.miqtech.master.entity.msg;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 用户信息读取记录表
 *
 * @author gaohanlin
 * @create 2017年09月02日
 */
@Entity
@Table(name = "pc_msg_read")
public class CompetitionMsgRead {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/**
	 * 用户id
	 */
	@Column(name = "user_id")
	private Long userId;

	/**
	 * 已读信息id
	 */
	@Column(name = "msg_id")
	private Integer msgId;

	@Column(name = "create_date")
	private Date createDate;

	/**
	 * @return id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * 获取用户id
	 *
	 * @return user_id - 用户id
	 */
	public Long getUserId() {
		return userId;
	}

	/**
	 * 设置用户id
	 *
	 * @param userId 用户id
	 */
	public void setUserId(Long userId) {
		this.userId = userId;
	}

	/**
	 * 获取已读信息id
	 *
	 * @return msg_id - 已读信息id
	 */
	public Integer getMsgId() {
		return msgId;
	}

	/**
	 * 设置已读信息id
	 *
	 * @param msgId 已读信息id
	 */
	public void setMsgId(Integer msgId) {
		this.msgId = msgId;
	}

	/**
	 * @return create_date
	 */
	public Date getCreateDate() {
		return createDate;
	}

	/**
	 * @param createDate
	 */
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
}