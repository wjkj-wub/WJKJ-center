package com.miqtech.master.service.game;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.dao.game.GameImgDao;
import com.miqtech.master.entity.game.GameImg;

@Component
public class GameImgService {

	@Autowired
	private GameImgDao gameImgDao;

	public GameImg findById(Long id) {
		if (id == null) {
			return null;
		}

		return gameImgDao.findOne(id);
	}

	public List<GameImg> findValidByGameId(Long gameId) {
		if (gameId == null) {
			return null;
		}

		return gameImgDao.findByGameIdAndValid(gameId, CommonConstant.INT_BOOLEAN_TRUE);
	}

	public GameImg save(GameImg img) {
		if (img == null) {
			return null;
		}

		return gameImgDao.save(img);
	}

	public List<GameImg> save(List<GameImg> imgs) {
		if (CollectionUtils.isEmpty(imgs)) {
			return null;
		}

		return (List<GameImg>) gameImgDao.save(imgs);
	}

	public GameImg disabled(Long id) {
		GameImg img = findById(id);
		if (img == null) {
			return null;
		}

		img.setValid(CommonConstant.INT_BOOLEAN_FALSE);
		return save(img);
	}
}