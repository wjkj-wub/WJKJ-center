package com.miqtech.master.entity.msg;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * 赛事消息推送
 *
 * @author gaohanlin
 * @create 2017年09月02日
 */
@Entity
@Table(name = "pc_msg")
public class CompetitionMsg {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/**
	 * 推送标题
	 */
	private String title;

	/**
	 * 推送内容
	 */
	@Column(name = "msg_info")
	private String msgInfo;

	/**
	 * 推送类型1个人 2全体
	 */
	@Column(name = "object_type")
	private Byte objectType;

	/**
	 * 推送对象 object_type=1 为推送接收者
	 */
	private Long object;

	/**
	 * 发送推送用户
	 */
	@Column(name = "push_user")
	private Long pushUser;

	@Column(name = "is_valid")
	private Byte isValid;

	/**
	 * 推送开始时间
	 */
	@Column(name = "begin_time")
	private Date beginTime;

	@Transient
	private String nickname;
	/**
	 * 推送结束时间
	 */
	@Column(name = "end_time")
	private Date endTime;

	/**
	 * 推送创建时间
	 */
	@Column(name = "create_date")
	private Date createDate;

	@Column(name = "update_date")
	private Date updateDate;

	/**
	 * 0 未读，1已读
	 */
	@Transient
	private String isRead;

	public String getIsRead() {
		return isRead;
	}

	public void setIsRead(String isRead) {
		this.isRead = isRead;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

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
	 * 获取推送标题
	 *
	 * @return title - 推送标题
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * 设置推送标题
	 *
	 * @param title 推送标题
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * 获取推送内容
	 *
	 * @return msg_info - 推送内容
	 */
	public String getMsgInfo() {
		return msgInfo;
	}

	/**
	 * 设置推送内容
	 *
	 * @param msgInfo 推送内容
	 */
	public void setMsgInfo(String msgInfo) {
		this.msgInfo = msgInfo;
	}

	/**
	 * 获取推送类型1个人 2全体
	 *
	 * @return object_type - 推送类型1个人 2全体
	 */
	public Byte getObjectType() {
		return objectType;
	}

	/**
	 * 设置推送类型1个人 2全体
	 *
	 * @param objectType 推送类型1个人 2全体
	 */
	public void setObjectType(Byte objectType) {
		this.objectType = objectType;
	}

	/**
	 * 获取推送对象 object_type=1 为推送接收者
	 *
	 * @return object - 推送对象 object_type=1 为推送接收者
	 */
	public Long getObject() {
		return object;
	}

	/**
	 * 设置推送对象 object_type=1 为推送接收者
	 *
	 * @param object 推送对象 object_type=1 为推送接收者
	 */
	public void setObject(Long object) {
		this.object = object;
	}

	/**
	 * 获取发送推送用户
	 *
	 * @return push_user - 发送推送用户
	 */
	public Long getPushUser() {
		return pushUser;
	}

	/**
	 * 设置发送推送用户
	 *
	 * @param pushUser 发送推送用户
	 */
	public void setPushUser(Long pushUser) {
		this.pushUser = pushUser;
	}

	/**
	 * @return is_valid
	 */
	public Byte getIsValid() {
		return isValid;
	}

	/**
	 * @param isValid
	 */
	public void setIsValid(Byte isValid) {
		this.isValid = isValid;
	}

	/**
	 * 获取推送开始时间
	 *
	 * @return begin_time - 推送开始时间
	 */
	public Date getBeginTime() {
		return beginTime;
	}

	/**
	 * 设置推送开始时间
	 *
	 * @param beginTime 推送开始时间
	 */
	public void setBeginTime(Date beginTime) {
		this.beginTime = beginTime;
	}

	/**
	 * 获取推送结束时间
	 *
	 * @return end_time - 推送结束时间
	 */
	public Date getEndTime() {
		return endTime;
	}

	/**
	 * 设置推送结束时间
	 *
	 * @param endTime 推送结束时间
	 */
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	/**
	 * 获取推送创建时间
	 *
	 * @return create_date - 推送创建时间
	 */
	public Date getCreateDate() {
		return createDate;
	}

	/**
	 * 设置推送创建时间
	 *
	 * @param createDate 推送创建时间
	 */
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	/**
	 * @return update_date
	 */
	public Date getUpdateDate() {
		return updateDate;
	}

	/**
	 * @param updateDate
	 */
	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}
}