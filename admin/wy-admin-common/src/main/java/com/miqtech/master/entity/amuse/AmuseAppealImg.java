package com.miqtech.master.entity.amuse;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 娱乐赛申诉图片
 */
@Entity
@Table(name = "amuse_r_appeal_img")
public class AmuseAppealImg {

	private Long id;
	private Long appealId;// 审核ID
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

	@Column(name = "appeal_id")
	public Long getAppealId() {
		return appealId;
	}

	public void setAppealId(Long appealId) {
		this.appealId = appealId;
	}

	@Column(name = "img")
	public String getImg() {
		return img;
	}

	public void setImg(String img) {
		this.img = img;
	}
}
