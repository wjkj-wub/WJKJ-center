package com.miqtech.master.entity.game;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.miqtech.master.entity.IdEntity;

@Entity
@Table(name = "game_r_imgs")
public class GameImg extends IdEntity {
	private static final long serialVersionUID = -2311700292856011891L;
	private Long gameId;// 手游ID
	private String url;// 图片地址
	private String urlMedia;
	private String urlThumb;

	@Column(name = "game_id")
	public Long getGameId() {
		return gameId;
	}

	public void setGameId(Long gameId) {
		this.gameId = gameId;
	}

	@Column(name = "url")
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Column(name = "url_media")
	public String getUrlMedia() {
		return urlMedia;
	}

	public void setUrlMedia(String urlMedia) {
		this.urlMedia = urlMedia;
	}

	@Column(name = "url_thumb")
	public String getUrlThumb() {
		return urlThumb;
	}

	public void setUrlThumb(String urlThumb) {
		this.urlThumb = urlThumb;
	}

}
