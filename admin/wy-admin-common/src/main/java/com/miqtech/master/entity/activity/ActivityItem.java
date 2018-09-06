package com.miqtech.master.entity.activity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "activity_r_items")
public class ActivityItem extends IdEntity {
	private static final long serialVersionUID = 4744871577382575186L;
	private String name;// 名称
	private String pic;// 图片
	private String picMedia;
	private String picThumb;
	private String icon;
	private Integer serverRequired; //是否需要服务器

	@Column(name = "name")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "pic")
	public String getPic() {
		return pic;
	}

	public void setPic(String pic) {
		this.pic = pic;
	}

	@Column(name = "pic_media")
	public String getPicMedia() {
		return picMedia;
	}

	public void setPicMedia(String picMedia) {
		this.picMedia = picMedia;
	}

	@Column(name = "pic_thumb")
	public String getPicThumb() {
		return picThumb;
	}

	public void setPicThumb(String picThumb) {
		this.picThumb = picThumb;
	}

	@Column(name = "icon")
	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	@Column(name = "server_required")
	public Integer getServerRequired() {
		return serverRequired;
	}

	public void setServerRequired(Integer serverRequired) {
		this.serverRequired = serverRequired;
	}

}