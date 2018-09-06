package com.miqtech.master.entity.uwan;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "uwan_user")
public class UwanUser implements Serializable {

	private static final long serialVersionUID = 2210614408275418109L;

	private Long id;
	private String wyOpenId;// 网娱用户OPENID
	private Long userId;// 网娱用户ID
	private String uwanUserId;// 优玩用户ID
	private String nickanme;// 优玩昵称
	private String icon;// 优玩头像
	private String telephone;// 优玩用户手机号码
	private Integer valid;
	private Date updateDate;
	private Date createDate;

	@Id
	@Column(name = "id", nullable = false, unique = true)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name = "wy_open_id")
	public String getWyOpenId() {
		return wyOpenId;
	}

	public void setWyOpenId(String wyOpenId) {
		this.wyOpenId = wyOpenId;
	}

	@Column(name = "user_id")
	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	@Column(name = "uwan_user_id")
	public String getUwanUserId() {
		return uwanUserId;
	}

	public void setUwanUserId(String uwanUserId) {
		this.uwanUserId = uwanUserId;
	}

	@Column(name = "nickanme")
	public String getNickanme() {
		return nickanme;
	}

	public void setNickanme(String nickanme) {
		this.nickanme = nickanme;
	}

	@Column(name = "icon")
	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	@Column(name = "telephone")
	public String getTelephone() {
		return telephone;
	}

	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}

	@Column(name = "is_valid")
	public Integer getValid() {
		return valid;
	}

	public void setValid(Integer valid) {
		this.valid = valid;
	}

	@Column(name = "update_date")
	public Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}

	@Column(name = "create_date")
	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

}
