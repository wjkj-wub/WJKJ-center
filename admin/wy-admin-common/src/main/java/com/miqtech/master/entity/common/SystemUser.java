package com.miqtech.master.entity.common;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.miqtech.master.entity.IdEntity;
import com.miqtech.master.utils.EncodeUtils;

@Entity
@Table(name = "sys_t_user")
public class SystemUser extends IdEntity {
	private static final long serialVersionUID = -542706395439159499L;
	private String username;// 用户名
	private String password;// 密码
	private String realname;// 真实名
	private String qq;// qq
	private String telephone;// 联系号码
	private String email;// 邮箱地址
	private Integer userType;// 用户类型：0-系统管理员 1-普通管理员 2-录入子系统管理员 3-录入子系统审核人员 4-录入子系统录入人员5录入系统区域管理员一级账号6录入系统区域管理员二级账号7录入系统区域管理员三级账号8录入系统区域管理员四级账号9金币系统用户10财务系统管理员11邀请码系统用户12管理系统娱乐赛审核帐号13管理系统娱乐赛发放帐号14管理系统娱乐赛申诉帐号
	private Long parentId;// 父类id
	private String areaCode;// 区域code
	private String icon;// 头像
	private String oetName;// 自发赛账户名称

	private List<SysUserArea> areas;// 用户的地区

	@Column(name = "username")
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@Column(name = "password")
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setEncryptPassword(String password) {
		this.password = EncodeUtils.base64Md5(password);
	}

	@Column(name = "realname")
	public String getRealname() {
		return realname;
	}

	public void setRealname(String realname) {
		this.realname = realname;
	}

	@Column(name = "qq")
	public String getQq() {
		return qq;
	}

	public void setQq(String qq) {
		this.qq = qq;
	}

	@Column(name = "telephone")
	public String getTelephone() {
		return telephone;
	}

	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}

	@Column(name = "email")
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Column(name = "user_type")
	public Integer getUserType() {
		return userType;
	}

	public void setUserType(Integer userType) {
		this.userType = userType;
	}

	@Column(name = "parent_id")
	public Long getParentId() {
		return parentId;
	}

	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	@Column(name = "area_code")
	public String getAreaCode() {
		return areaCode;
	}

	public void setAreaCode(String areaCode) {
		this.areaCode = areaCode;
	}

	@Transient
	public List<SysUserArea> getAreas() {
		return areas;
	}

	public void setAreas(List<SysUserArea> areas) {
		this.areas = areas;
	}

	@Column(name = "icon")
	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	@Column(name = "oet_name")
	public String getOetName() {
		return oetName;
	}

	public void setOetName(String oetName) {
		this.oetName = oetName;
	}
}
