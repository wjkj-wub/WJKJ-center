package com.miqtech.master.entity.activity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "activity_t_advertises")
public class ActivityAdvertise extends IdEntity {
	private static final long serialVersionUID = -4128351190746795932L;
	private String pic;
	private String picMedia;
	private String picThumb;
	private String url;

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

	public String getPicThumb() {
		return picThumb;
	}

	public void setPicThumb(String picThumb) {
		this.picThumb = picThumb;
	}

	@Column(name = "url")
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
