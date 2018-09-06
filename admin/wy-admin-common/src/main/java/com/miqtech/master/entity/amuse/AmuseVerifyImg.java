package com.miqtech.master.entity.amuse;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 娱乐赛审核图片
 */
@Entity
@Table(name = "amuse_r_verify_img")
public class AmuseVerifyImg {

	private Long id;
	private Long verifyId;// 审核ID
	private String img;// 图片路径

	@Id
	@Column(name = "id", nullable = false, unique = true)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name = "verify_id")
	public Long getVerifyId() {
		return verifyId;
	}

	public void setVerifyId(Long verifyId) {
		this.verifyId = verifyId;
	}

	@Column(name = "img")
	public String getImg() {
		return img;
	}

	public void setImg(String img) {
		this.img = img;
	}
}
