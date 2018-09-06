package com.miqtech.master.dao.mall;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.mall.H5Game;

public interface H5GameDao extends PagingAndSortingRepository<H5Game, Long>, JpaSpecificationExecutor<H5Game> {
	
	public H5Game findByUserIdAndValid(Long userId, Integer valid);

	public H5Game findByUserIdAndValidAndGameId(Long userId, Integer valid, Integer gameId);
	
	
}
