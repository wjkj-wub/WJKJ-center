package com.miqtech.master.dao.game;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.game.GameInfo;

/**
 * 游戏信息操作DAO
 */
public interface GameInfoDao extends PagingAndSortingRepository<GameInfo, Long>, JpaSpecificationExecutor<GameInfo> {
}