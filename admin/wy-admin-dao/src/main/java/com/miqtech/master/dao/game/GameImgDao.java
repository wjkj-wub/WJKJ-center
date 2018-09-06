package com.miqtech.master.dao.game;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.game.GameImg;

/**
 * 游戏图片操作DAO
 */
public interface GameImgDao extends PagingAndSortingRepository<GameImg, Long>, JpaSpecificationExecutor<GameImg> {

	List<GameImg> findByGameIdAndValid(Long gameId, Integer valid);

}